package com.dev.notebook;

import com.dev.notebook.domain.RequestContext;
import com.dev.notebook.enumeration.Authority;
import com.dev.notebook.exceptions.ApiException;
import com.dev.notebook.models.Credential;
import com.dev.notebook.models.Role;
import com.dev.notebook.models.User;
import com.dev.notebook.repositories.ICredentialRepository;
import com.dev.notebook.repositories.IRoleRepository;
import com.dev.notebook.repositories.IUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;


@SpringBootApplication
@EnableJpaAuditing
public class NotebookApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotebookApplication.class, args);
	}


	@Bean
	CommandLineRunner commandLineRunner(IRoleRepository iRoleRepository, IUserRepository iUserRepository, ICredentialRepository iCredentialRepository, BCryptPasswordEncoder encoder){
		return args -> {
			RequestContext.setUserId(0L);
			if (iRoleRepository.findRoleByName(Authority.USER.name()).isEmpty()) {
				var userRole = new Role();
				userRole.setName(Authority.USER.name());
				userRole.setCode("USER_01");
				userRole.setAuthorities(new Authority[]{Authority.USER});
				iRoleRepository.save(userRole);
			}

			if (iRoleRepository.findRoleByName(Authority.ADMIN.name()).isEmpty()) {
				var adminRole = new Role();
				adminRole.setName(Authority.ADMIN.name());
				adminRole.setCode("ADMIN_01");
				adminRole.setAuthorities(new Authority[]{Authority.ADMIN});
				iRoleRepository.save(adminRole);
			}

			if (iUserRepository.findUserByEmail("admin@example.com").isEmpty()) {
				var adminRole = iRoleRepository.findRoleByName(Authority.ADMIN.name()).orElseThrow(() -> new ApiException("Admin role not found"));

				// Create and save the user first
				var user = new User();
				user.setEmail("admin@example.com");
				user.setAccountNonExpired(true);
				user.setAccountNonLocked(true);
				user.setEnabled(true);

				// Save user first before creating the credential
				iUserRepository.save(user);

				// Now create and associate the credential with the user
				String encryptedPassword = encoder.encode("abc@#123");
				Credential credential = new Credential(encryptedPassword, user);

				// Save the credential
				iCredentialRepository.save(credential);

				// Update user with the credential's password and role
				user.setPassword(credential.getPassword());
				user.setRole(adminRole);

				// Save the updated user with the role and password
				iUserRepository.save(user);
			}

			RequestContext.start();

		};
	}

}
