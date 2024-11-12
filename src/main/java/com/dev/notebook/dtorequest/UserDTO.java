package com.dev.notebook.dtorequest;

import jakarta.persistence.Column;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String city;

    private boolean enabled = false;

    private Integer loginAttempts;

    private boolean accountNonLocked = false;

    private boolean accountNonExpired;

    private boolean credentialsNonExpired;

    private LocalDateTime lastLogin;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private String createdBy;

    private String lastModifiedBy;

    private String role;

    private String authorities;

}
