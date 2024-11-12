package com.dev.notebook.domain;

import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.exceptions.ApiException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class ApiAuthentication extends AbstractAuthenticationToken {
    private static final String PASSWORD_PROTECTED = "[PASSWORD_PROTECTED]";
    private static final String EMAIL_PROTECTED = "[EMAIL_PROTECTED]";
    private UserDTO userDTO;
    private final String email;
    private final String password;

    private boolean authenticated;

    // Constructor for unauthenticated users
    public ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.email = email;
        this.password = password;
        this.authenticated = false; // User is initially unauthenticated
    }

    // Static method to create unauthenticated instance
    public static ApiAuthentication unauthenticated(String email, String password) {
        return new ApiAuthentication(email, password);
    }

    // Static method to create authenticated instance with authorities
    public static ApiAuthentication authentication(UserDTO userDTO, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(userDTO, authorities);
    }

    // Constructor for authenticated users with authorities
    public ApiAuthentication(UserDTO userDTO, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDTO = userDTO;
        this.password = PASSWORD_PROTECTED;  // Do not expose real password
        this.email = EMAIL_PROTECTED;        // Do not expose real email
        this.authenticated = true;           // User is authenticated
    }

    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;  // Return a placeholder instead of the real password
    }

    @Override
    public Object getPrincipal() {
        return this.userDTO;  // The principal is the UserDTO object
    }

    // Getter methods for email and password (may be needed elsewhere in your app)
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    // Override the method to set authentication state
    @Override
    public void setAuthenticated(boolean authenticated) {
        if (this.authenticated && !authenticated) {
            throw new ApiException("You cannot de-authenticate this object.");
        }
        this.authenticated = authenticated;
    }

    // Return the authentication state
    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }
}
