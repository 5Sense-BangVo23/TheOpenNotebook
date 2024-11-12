package com.dev.notebook.resources;

import com.dev.notebook.domain.Response;
import com.dev.notebook.dtorequest.LoginAccountRequest;
import com.dev.notebook.dtorequest.RegisterUserRequest;
import com.dev.notebook.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import static com.dev.notebook.constants.Constant.ACCOUNT_CREATED_INFO;
import static com.dev.notebook.constants.Constant.ACCOUNT_VERIFY_INO;
import static com.dev.notebook.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
public class AuthResource {
    private final IUserService iUserService;
    private final AuthenticationManager authenticationManager;

    @PostMapping(path = "/accounts/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid RegisterUserRequest userRequest, HttpServletRequest request){
        iUserService.createUser(
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getCity());
        return ResponseEntity.created(getUri()).body(getResponse(request,emptyMap(),ACCOUNT_CREATED_INFO,CREATED));
    }

    @GetMapping(path = "/accounts/verify")
    public ResponseEntity<Response> verifyAccountConfirmationKey(@RequestParam("confirmationKey") String confirmationKey, HttpServletRequest request ){
        iUserService.verifyAccountConfirmationKey(confirmationKey);
        return ResponseEntity.ok().body(getResponse(request,emptyMap(),ACCOUNT_VERIFY_INO,OK));
    }

    @PostMapping(path = "/accounts/login")
    public ResponseEntity<?> authenticated(@RequestBody LoginAccountRequest loginRequest){
        System.out.println("Login attempt with email: " + loginRequest.getEmail());
        System.out.println("Password:" + loginRequest.getPassword());

        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword());
            System.out.println(authenticationToken);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);


            return ResponseEntity.ok().body(Map.of("user", authentication.getPrincipal()));
        } catch (Exception e) {

            return ResponseEntity.status(UNAUTHORIZED).body("Invalid email or password.");
        }
    }
    private URI getUri() {
        return URI.create("");
    }
}
