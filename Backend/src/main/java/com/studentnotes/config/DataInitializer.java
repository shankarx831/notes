package com.studentnotes.config;

import com.studentnotes.model.User;
import com.studentnotes.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if teacher exists
            if (userRepository.findByEmail("teacher@test.com").isEmpty()) {
                User teacher = new User();
                teacher.setEmail("teacher@test.com");
                teacher.setPassword(passwordEncoder.encode("password"));
                teacher.setName("Test Teacher");
                teacher.setRole("ROLE_TEACHER");
                teacher.setAssignedDepartments(List.of("it"));
                teacher.setEnabled(true);
                userRepository.save(teacher);
                System.out.println("Created test teacher: teacher@test.com");
            }

            // Ensure admin exists AND has a known password (admin123)
            userRepository.findByEmail("admin@test.com").ifPresentOrElse(
                    admin -> {
                        admin.setPassword(passwordEncoder.encode("admin123"));
                        admin.setName("Super Admin");
                        admin.setRole("ROLE_ADMIN");
                        userRepository.save(admin);
                        System.out.println("🔄 Admin password reset to: admin123");
                    },
                    () -> {
                        User admin = new User();
                        admin.setEmail("admin@test.com");
                        admin.setPassword(passwordEncoder.encode("admin123"));
                        admin.setName("Super Admin");
                        admin.setRole("ROLE_ADMIN");
                        userRepository.save(admin);
                        System.out.println("✨ Created Super Admin: admin@test.com");
                    });
        };
    }
}
