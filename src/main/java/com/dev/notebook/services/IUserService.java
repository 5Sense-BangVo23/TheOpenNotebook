package com.dev.notebook.services;

import com.dev.notebook.dtorequest.UserDTO;
import com.dev.notebook.enumeration.LoginType;
import com.dev.notebook.models.Credential;
import com.dev.notebook.models.Role;
import com.dev.notebook.models.User;

public interface IUserService{
    void createUser(String firstName, String lastName, String email, String password,String city);
    Role getRoleName(String name);

    void verifyAccountConfirmationKey(String confirmationKey);

    void updateLoginAttempt(String email, LoginType loginType);

    User getUserById(Long id);

    UserDTO getUserByEmail(String email);

    Credential getUserCredentialById(Long id);
}
