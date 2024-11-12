package com.dev.notebook.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class JwtConfiguration {
    @Value("${spring.jwt.expiration}")
    private Long expiration;
    @Value("${spring.jwt.secret}")
    private String secret;
}
