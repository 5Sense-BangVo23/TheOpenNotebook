package com.dev.notebook.security;

import com.dev.notebook.domain.ApiAuthentication;
import com.dev.notebook.domain.UserPrincipal;
import com.dev.notebook.exceptions.ApiException;
import com.dev.notebook.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class ApiAuthenticationProvider implements AuthenticationProvider{

    private static final int NINETY_DAYS = 90;
    private final IUserService iUserService;
    private final BCryptPasswordEncoder encoder;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Extract email and password from the authentication object
        String email = authentication.getName(); // email from the UsernamePasswordAuthenticationToken
        String password = (String) authentication.getCredentials(); // password from the UsernamePasswordAuthenticationToken

        // Create a new ApiAuthentication object using the email and password
        var apiAuthentication = new ApiAuthentication(email, password);

        var user = iUserService.getUserByEmail(apiAuthentication.getEmail());
        if (user != null) {
            var userCredential = iUserService.getUserCredentialById(user.getId());

            // Commented out the expiration check for credentials
            // if (userCredential.getLastModifiedDate().minusDays(NINETY_DAYS).isAfter(LocalDateTime.now())) { throw new ApiException("Credentials are expired. Please reset your password"); }

            if (!user.isCredentialsNonExpired()) {
                throw new ApiException("Credentials are expired. Please reset your password");
            }

            var userPrincipal = new UserPrincipal(user, userCredential);
            validAccount.accept(userPrincipal);

            // Match password with the stored hash
            if (encoder.matches(apiAuthentication.getPassword(), userCredential.getPassword())) {
                return ApiAuthentication.authentication(user, userPrincipal.getAuthorities());
            } else {
                throw new BadCredentialsException("Email and/or password incorrect. Please try again");
            }
        } else {
            throw new ApiException("Unable to authenticate");
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private final Function<Authentication, ApiAuthentication> authenticationFunction = authentication -> (ApiAuthentication) authentication;

    private final Consumer<UserPrincipal> validAccount = userPrincipal -> {
        if (!userPrincipal.isAccountNonLocked()) {
            throw new LockedException("Your account is currently locked");
        }
        if (!userPrincipal.isEnabled()) {
            throw new DisabledException("Your account is currently disabled");
        }
        if (!userPrincipal.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("Your account credentials have expired");
        }
        if (!userPrincipal.isAccountNonExpired()) {
            throw new AccountExpiredException("Your account has expired");
        }
    };

}
