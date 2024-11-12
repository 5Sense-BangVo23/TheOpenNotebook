package com.dev.notebook.domain;

import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.models.Credential;
import com.dev.notebook.models.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    @Getter
    private final UserDTO userDTO;
    private final Credential credential;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(userDTO.getAuthorities());
    }

    @Override
    public String getPassword() {
        return credential.getPassword();
    }

    @Override
    public String getUsername() {
        return userDTO.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userDTO.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userDTO.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userDTO.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return userDTO.isEnabled();
    }
}
