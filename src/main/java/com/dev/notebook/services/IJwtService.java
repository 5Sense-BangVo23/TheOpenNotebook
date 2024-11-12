package com.dev.notebook.services;

import com.dev.notebook.domain.Token;
import com.dev.notebook.domain.TokenData;
import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.enumeration.TokenType;
import com.dev.notebook.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.function.Function;

public interface IJwtService {
    String createToken(UserDTO user, Function<Token, String> tokenFunction);
    Optional<String> extractToken(HttpServletRequest request, String tokenType);
    void addCookie(HttpServletResponse response, UserDTO user, TokenType type);
    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);

    void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);
}
