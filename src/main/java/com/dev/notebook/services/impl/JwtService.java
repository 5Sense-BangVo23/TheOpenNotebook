package com.dev.notebook.services.impl;

import com.dev.notebook.domain.Token;
import com.dev.notebook.domain.TokenData;
import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.enumeration.TokenType;
import com.dev.notebook.function.TriConsumer;
import com.dev.notebook.models.User;
import com.dev.notebook.security.JwtConfiguration;
import com.dev.notebook.services.IJwtService;
import com.dev.notebook.services.IUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import static com.dev.notebook.enumeration.TokenType.REFRESH;
import static  org.springframework.boot.web.server.Cookie.SameSite.NONE;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dev.notebook.constants.Constant.*;
import static com.dev.notebook.enumeration.TokenType.ACCESS;
import static io.jsonwebtoken.Header.JWT_TYPE;
import static io.jsonwebtoken.Header.TYPE;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService extends JwtConfiguration implements IJwtService {

    private final IUserService iUserService;

    private final Supplier<SecretKey> key = () -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(getSecret()));

    private final Function<String, Claims> claimsFunction = token -> Jwts.parser().verifyWith(key.get()).build().parseSignedClaims(token).getPayload();

    private final BiFunction<HttpServletRequest, String, Optional<String>> extractToken = (request,cookieName) -> Optional.of(stream(request.getCookies() == null ? new Cookie[]{new Cookie(EMPTY_VALUE,EMPTY_VALUE)} : request.getCookies()).filter(cookie -> Objects.equals(cookieName,cookie.getName())).map(Cookie::getValue).findAny()).orElse(empty());

    private final Function<String, List<GrantedAuthority>> authorities = token ->commaSeparatedStringToAuthorityList(new StringJoiner(AUTHORITY_DELIMITER).add(claimsFunction.apply(token).get(AUTHORITIES, String.class)).add(ROLE_PREFIX + claimsFunction.apply(token).get(ROLE,String.class)).toString());

    private final BiFunction<HttpServletRequest, String, Optional<Cookie>> extractCookie = (request, cookieName) -> Optional.of(stream(request.getCookies() == null ? new Cookie[]{new Cookie(EMPTY_VALUE,EMPTY_VALUE)} : request.getCookies()).filter(cookie -> Objects.equals(cookieName, cookie.getName())).findAny()).orElse(empty());

    private final Supplier<JwtBuilder> builder = () -> Jwts.builder().header().add(Map.of(TYPE, JWT_TYPE)).and().audience().add("GET_ARRAYS_LLC").and().id(UUID.randomUUID().toString()).issuedAt(Date.from(Instant.now())).notBefore(new Date()).signWith(key.get(), Jwts.SIG.HS512);

    private final BiFunction<UserDTO, TokenType, String> buildToken = (user, type) ->Objects.equals(type,ACCESS) ? builder.get() . subject(String.valueOf(user.getId())).claim(AUTHORITIES, user.getAuthorities()).claim(ROLE,user.getRole()).expiration(Date.from(Instant.now().plusSeconds(getExpiration()))).compact() : builder.get().subject(String.valueOf(user.getId())).expiration(Date.from(Instant.now().plusSeconds(getExpiration()))).compact();

    private final TriConsumer<HttpServletResponse, UserDTO, TokenType> addCookie = ((response, user, tokenType) -> {
        switch (tokenType){
            case ACCESS -> {
                var accessToken = createToken(user, Token::getAccess);
                var cookie = new Cookie(tokenType.getValue(), accessToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setMaxAge(2 * 60);
                cookie.setPath("/");
                cookie.setAttribute("SameSite",NONE.name());
                response.addCookie(cookie);
            }
            case REFRESH -> {
                var refreshToken = createToken(user, Token::getRefresh);
                var cookie = new Cookie(tokenType.getValue(), refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setMaxAge(2 * 60 * 60);
                cookie.setAttribute("SameSite",NONE.name());
                response.addCookie(cookie);
            }
        }
    });


    private <T> T getClaimsValue(String token, Function<Claims,T> claims){
        return claimsFunction.andThen(claims).apply(token);
    }

    private final Function<String, Date> subject = token -> getClaimsValue(token, Claims::getExpiration);

    @Override
    public String createToken(UserDTO user, Function<Token, String> tokenFunction) {
        var token = Token.builder().access(buildToken.apply(user, ACCESS)).refresh(buildToken.apply(user, REFRESH)).build();
        return tokenFunction.apply(token);
    }

    @Override
    public Optional<String> extractToken(HttpServletRequest request, String cookieName) {
        return extractToken(request, cookieName);
    }

    @Override
    public void addCookie(HttpServletResponse response, UserDTO user, TokenType type) {
        addCookie.accept(response,user,type);
    }

    @Override
    public <T> T getTokenData(String token, Function<TokenData, T> tokenFunction) {
        String tokenSubject = claimsFunction.apply(token).getSubject();
        User user = iUserService.getUserById(subject.apply(token).getTime());

        return tokenFunction.apply(
                TokenData.builder()
                        .valid(user != null && Objects.equals(user.getId().toString(), tokenSubject))
                        .authorities(authorities.apply(token))
                        .claims(claimsFunction.apply(token))
                        .user(user)
                        .build()
        );
    }

    @Override
    public void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        var optionalCookie = extractCookie.apply(request, cookieName);
        if(optionalCookie.isPresent()){
            var cookie = optionalCookie.get();
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }
}
