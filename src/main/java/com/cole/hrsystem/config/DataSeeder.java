package com.cole.hrsystem.config;

import com.cole.hrsystem.model.AppUser;
import com.cole.hrsystem.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAdmin() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new AppUser(
                        "admin",
                        passwordEncoder.encode("password123"),
                        AppUser.Role.ROLE_ADMIN
                ));
            }
        };
    }
}