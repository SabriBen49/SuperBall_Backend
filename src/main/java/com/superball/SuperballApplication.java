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
				admin.setProfileImageUrl("https://imgs.search.brave.com/vTJQvVyvKi6YFxtaaqeVSEw0CTWdBUpcwogaP5o21PI/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jZG4t/aWNvbnMtcG5nLmZy/ZWVwaWsuY29tLzI1/Ni84ODkvODg5NzM5/LnBuZz9zZW10PWFp/c193aGl0ZV9sYWJl/bA");
				admin.setVerified(true);
				userRepository.save(admin);
			}
		};
	}
}
