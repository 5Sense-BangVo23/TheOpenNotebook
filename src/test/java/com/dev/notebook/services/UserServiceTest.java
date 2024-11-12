package com.dev.notebook.services;

import com.dev.notebook.enumeration.Authority;
import com.dev.notebook.models.Credential;
import com.dev.notebook.models.Role;
import com.dev.notebook.models.User;
import com.dev.notebook.repositories.ICredentialRepository;
import com.dev.notebook.repositories.IUserRepository;
import com.dev.notebook.services.impl.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private IUserRepository iUserRepository;

    @Mock
    private ICredentialRepository iCredentialRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Test find user by ID")
    public void getUserIdByUserIdTest() {
        var userEntity = new User();
        userEntity.setFirstName("Bang");
        userEntity.setId(10L);

        var roleEntity = new Role("USER", Authority.USER);
        userEntity.setRole(roleEntity);

        var credentialEntity = new Credential();
        credentialEntity.setPassword("password");
        credentialEntity.setUser(userEntity);

        // Mock UserRepository
        when(iUserRepository.findUserById(10L)).thenReturn(Optional.of(userEntity));
        Mockito.lenient().when(iCredentialRepository.getCredentialByUserId(10L)).thenReturn(Optional.of(credentialEntity));

        var userById = userService.getUserById(10L);

        assertThat(userById.getFirstName()).isEqualTo(userEntity.getFirstName());
    }
}
