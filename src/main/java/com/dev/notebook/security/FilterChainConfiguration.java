package com.dev.notebook.security;


import com.dev.notebook.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainConfiguration {
    private final BCryptPasswordEncoder encoder;
    private final IUserService iUserService;

        @Bean
        public AuthenticationManager authenticationManager(){
            ApiAuthenticationProvider authenticationProvider = new ApiAuthenticationProvider(iUserService,encoder);
            return new ProviderManager(authenticationProvider);
        }
}
