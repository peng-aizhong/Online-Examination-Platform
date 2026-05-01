package com.examination.config;

import com.examination.entity.User;
import com.examination.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Database already has data, skipping initialization");
            return;
        }

        log.info("Initializing database with test data...");

        // Create test users
        User student = User.builder()
                .username("student01")
                .email("student01@example.com")
                .password(passwordEncoder.encode("123456"))
                .realName("张三")
                .role(User.UserRole.STUDENT)
                .active(true)
                .build();

        User teacher = User.builder()
                .username("teacher01")
                .email("teacher01@example.com")
                .password(passwordEncoder.encode("123456"))
                .realName("王五")
                .role(User.UserRole.TEACHER)
                .active(true)
                .build();

        User admin = User.builder()
                .username("admin01")
                .email("admin01@example.com")
                .password(passwordEncoder.encode("123456"))
                .realName("管理员")
                .role(User.UserRole.ADMIN)
                .active(true)
                .build();

        userRepository.save(student);
        userRepository.save(teacher);
        userRepository.save(admin);

        log.info("Database initialization completed!");
        log.info("Test users created:");
        log.info("  - student01 / 123456 (Student)");
        log.info("  - teacher01 / 123456 (Teacher)");
        log.info("  - admin01 / 123456 (Admin)");
    }
}
