package com.superball;

import com.superball.entity.User;
import com.superball.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SuperballApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuperballApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {

			if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

				User admin = new User();
				admin.setEmail("admin@gmail.com");
				admin.setNickname("admin");
				admin.setPassword(passwordEncoder.encode("admin12321"));
				admin.setRole("ADMIN");
				admin.setVerified(true);
				userRepository.save(admin);
			}
		};
	}
}