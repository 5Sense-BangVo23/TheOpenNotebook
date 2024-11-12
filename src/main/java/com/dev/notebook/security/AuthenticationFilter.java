package com.dev.notebook.security;

import com.dev.notebook.dtorequest.LoginAccountRequest;
import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.enumeration.LoginType;
import com.dev.notebook.enumeration.TokenType;
import com.dev.notebook.models.User;
import com.dev.notebook.services.IJwtService;
import com.dev.notebook.services.IUserService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static com.dev.notebook.domain.ApiAuthentication.unauthenticated;
import static com.dev.notebook.utils.RequestUtils.getResponse;
import static com.dev.notebook.utils.RequestUtils.handlerErrorResponse;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String LOGIN_PATH = "/user/login";
    private final IUserService iUserService;
    private final IJwtService iJwtService;
    public AuthenticationFilter(AuthenticationManager authenticationManager, IUserService iUserService, IJwtService iJwtService) {
        super(new AntPathRequestMatcher(LOGIN_PATH,POST.name()),authenticationManager);
        this.iUserService = iUserService;
        this.iJwtService = iJwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        try{
            var user = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE,true).readValue(request.getInputStream(), LoginAccountRequest.class);
            iUserService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_ATTEMPT);
            var authentication = unauthenticated(user.getEmail(), user.getPassword());
            return getAuthenticationManager().authenticate(authentication);
        }catch (Exception ex){
            log.error(ex.getMessage());
            handlerErrorResponse(request,response,ex);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
       var user = (UserDTO) authentication.getPrincipal();
       iUserService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_SUCCESS);
        var httpResponse = sendResponse(request,response,user);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());

        var out = response.getOutputStream();
        var mapper = new ObjectMapper();
        mapper.writeValue(out,httpResponse);
        out.flush();
    }

    private Object sendResponse(HttpServletRequest request, HttpServletResponse response, UserDTO user) {
        iJwtService.addCookie(response,user, TokenType.ACCESS);
        iJwtService.addCookie(response,user,TokenType.REFRESH);
        return getResponse(request, Map.of("user", user),"Login success", OK);
    }
}
