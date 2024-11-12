package com.dev.notebook.utils;

import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.models.Credential;
import com.dev.notebook.models.Role;
import com.dev.notebook.models.User;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;

public class UserUtils {
    private static final int NINETY_DAYS = 90;

    public static User addUser(String firstName, String lastName, String email, String city, Role role){
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .lastLogin(LocalDateTime.now())
                .accountNonLocked(true)
                .accountNonExpired(true)
                .enabled(true)
                .loginAttempts(0)
                .city(city)
                .role(role)
                .build();
    }

    public static UserDTO fromUser(User user, Role role, Credential credential){
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setCredentialsNonExpired(isCredentialNonExpired(credential));
        userDTO.setCreatedDate(user.getCreatedDate());
        userDTO.setLastModifiedDate(user.getLastModifiedDate());
        userDTO.setRole(role.getName());
        userDTO.setAuthorities(role.getAuthoritiesValues());
        return userDTO;
    }

    private static boolean isCredentialNonExpired(Credential credential){
        return credential.getLastModifiedDate().plusDays(NINETY_DAYS).isAfter(LocalDateTime.now());
    }


}
