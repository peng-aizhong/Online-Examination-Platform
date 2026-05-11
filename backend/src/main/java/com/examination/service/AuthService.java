package com.examination.service;

import com.examination.dto.LoginRequest;
import com.examination.dto.LoginResponse;
import com.examination.dto.RegisterRequest;
import com.examination.dto.UserResponse;
import com.examination.entity.User;
import com.examination.repository.UserRepository;
import com.examination.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("用户账户已被禁用");
        }

        String storedPassword = user.getPassword();
        boolean passwordMatch = false;
        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
            passwordMatch = passwordEncoder.matches(loginRequest.getPassword(), storedPassword);
        } else {
            passwordMatch = loginRequest.getPassword() != null && loginRequest.getPassword().equals(storedPassword);
        }
        if (!passwordMatch) {
            throw new RuntimeException("密码错误");
        }

        if (loginRequest.getRole() != null && !loginRequest.getRole().isBlank()) {
            String requestRole = loginRequest.getRole().trim().toLowerCase();
            if (!user.getRole().name().equals(requestRole)) {
                throw new RuntimeException("用户身份不匹配");
            }
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        UserResponse userResponse = UserResponse.fromEntity(user);

        return LoginResponse.builder()
                .token(token)
                .userInfo(userResponse)
                .build();
    }

    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isBlank()
                && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User.UserRole role;
        try {
            role = User.UserRole.valueOf(registerRequest.getRole().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的用户角色");
        }

        String userId = registerRequest.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = "U" + System.currentTimeMillis() % 1000000000;
        }

        User user = User.builder()
                .userId(userId)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .department(registerRequest.getDepartment())
                .phone(registerRequest.getPhone())
                .active(true)
                .build();

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserResponse.fromEntity(user);
    }
}

    /**
     * 记录登录日志，用于安全审计 [Copilot辅助]
     */
    private void logLoginAttempt(String username, boolean success, String ip) {
        String status = success ? "SUCCESS" : "FAILED";
        log.info("Login attempt - user: {}, status: {}, ip: {}", username, status, ip);
    }
