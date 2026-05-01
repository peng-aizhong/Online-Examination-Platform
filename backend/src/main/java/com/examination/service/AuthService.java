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

        if (!user.getActive()) {
            throw new RuntimeException("用户账户已被禁用");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        UserResponse userResponse = convertToUserResponse(user);

        return LoginResponse.builder()
                .token(token)
                .userInfo(userResponse)
                .build();
    }

    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .realName(registerRequest.getRealName())
                .active(true)
                .build();

        try {
            user.setRole(User.UserRole.valueOf(registerRequest.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的用户角色");
        }

        user = userRepository.save(user);

        return convertToUserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToUserResponse(user);
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getRealName())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}
