package com.dev.notebook.services.impl;

import com.dev.notebook.cache.CacheStore;
import com.dev.notebook.domain.RequestContext;
import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.enumeration.Authority;
import com.dev.notebook.enumeration.EventType;
import com.dev.notebook.enumeration.LoginType;
import com.dev.notebook.events.UserEvent;
import com.dev.notebook.exceptions.ApiException;
import com.dev.notebook.exceptions.UserNotFoundException;
import com.dev.notebook.models.Confirmation;
import com.dev.notebook.models.Credential;
import com.dev.notebook.models.Role;
import com.dev.notebook.models.User;
import com.dev.notebook.repositories.IConfirmationRepository;
import com.dev.notebook.repositories.ICredentialRepository;
import com.dev.notebook.repositories.IRoleRepository;
import com.dev.notebook.repositories.IUserRepository;
import com.dev.notebook.services.IUserService;
import com.dev.notebook.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.dev.notebook.utils.UserUtils.addUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository iUserRepository;
    private final IRoleRepository iRoleRepository;
    private final ICredentialRepository iCredentialRepository;
    private final IConfirmationRepository iConfirmationRepository;

    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;

    private final CacheStore<String, Integer> userCache;

    @Override
    public void createUser(String firstName, String lastName, String email, String password,String city) {
        var user = iUserRepository.save(createNewUser(firstName,lastName,email,city));
        var credential = new Credential(password,user);
        iCredentialRepository.save(credential);
        var confirmation = new Confirmation(user);
        iConfirmationRepository.save(confirmation);

        publisher.publishEvent(new UserEvent(user, EventType.REGISTRATION, Map.of("confirmationKey",confirmation.getConfirmationKey())));

    }

    @Override
    public void verifyAccountConfirmationKey(String confirmationKey) {
        Confirmation confirmation = getUserConfirmation(confirmationKey);
        User user = getUserEntityByEmail(confirmation.getUser().getEmail());
        user.setEnabled(true);
        iUserRepository.save(user);
        iConfirmationRepository.delete(confirmation);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        var userEmail = getUserEntityByEmail(email);
        return UserUtils.fromUser(userEmail, userEmail.getRole(), getUserCredentialById(userEmail.getId()));
    }

    @Override
    public User getUserById(Long id) {
        return iUserRepository.findUserById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public Credential getUserCredentialById(Long id) {
        var credentialById = iCredentialRepository.getCredentialByUserId(id);
        return credentialById.orElseThrow(() -> new ApiException("Unable to find user credential"));
    }

    private Confirmation getUserConfirmation(String confirmationKey) {
        return iConfirmationRepository.findConfirmationByConfirmationKey(confirmationKey).orElse(null);
    }

    private User getUserEntityByEmail(String email){
        var userEmail = iUserRepository.findUserByEmail(email);
        return userEmail.orElseThrow(() -> new ApiException("User not found"));
    }

    @Override
    public Role getRoleName(String name) {
        var role = iRoleRepository.findRoleByName(name);
        return role.orElseThrow(() -> new ApiException("Role not found"));
    }


    private User createNewUser(String firstName, String lastName, String email,String city) {
        var role = getRoleName(Authority.USER.name());
        return addUser(firstName,lastName,email,city,role);
    }

    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {
        var user = getUserEntityByEmail(email);
        RequestContext.setUserId(user.getId());
        switch (loginType){
            case LOGIN_ATTEMPT -> {
                if(userCache.get(user.getEmail()) == null){
                    user.setLoginAttempts(0);
                    user.setAccountNonLocked(true);
                }
                user.setLoginAttempts(user.getLoginAttempts() + 1);
                userCache.put(user.getEmail(), user.getLoginAttempts());
                if (userCache.get(user.getEmail()) > 5){
                    user.setAccountNonLocked(false);
                }
            }
            case LOGIN_SUCCESS -> {
                user.setAccountNonLocked(true);
                user.setLoginAttempts(0);
                user.setLastLogin(LocalDateTime.now());

                userCache.evict(user.getEmail());
            }
        }

        iUserRepository.save(user);
    }
}
