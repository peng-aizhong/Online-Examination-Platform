package com.examination.service;

import com.examination.dto.UserCreateRequest;
import com.examination.dto.UserPageQuery;
import com.examination.dto.UserResponse;
import com.examination.dto.UserUpdateRequest;
import com.examination.entity.User;
import com.examination.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户（支持按角色和状态筛选）
     */
    public Page<UserResponse> listUsers(UserPageQuery query) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(query.getPage() - 1, query.getSize(), sort);
        Page<User> userPage;

        if (query.getRole() != null && query.getActive() != null) {
            // 角色 + 状态筛选
            userPage = userRepository.findByRoleAndActive(query.getRole(), query.getActive(), pageable);
        } else if (query.getRole() != null) {
            userPage = userRepository.findByRole(query.getRole(), pageable);
        } else if (query.getActive() != null) {
            userPage = userRepository.findByActive(query.getActive(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return userPage.map(UserResponse::fromEntity);
    }

    /**
     * 根据ID获取用户
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserResponse.fromEntity(user);
    }

    /**
     * 新增用户（管理员操作）
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 检查用户名唯一性
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱唯一性
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        String userId = request.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = "U" + System.currentTimeMillis() % 1000000000;
        }

        User user = User.builder()
                .userId(userId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment())
                .email(request.getEmail())
                .phone(request.getPhone())
                .active(true)
                .build();

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    /**
     * 更新用户信息（角色、部门、邮箱、手机、状态）
     */
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // 如果新邮箱已被其他用户使用，报错
            if (userRepository.existsByEmail(request.getEmail())
                    && !request.getEmail().equals(user.getEmail())) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getActive() != null) user.setActive(request.getActive());

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    /**
     * 重置用户密码
     * @param userId      用户ID
     * @param newPassword 新密码（明文）
     */
    @Transactional
    public void resetPassword(String userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 删除用户（软删除，设置 active = false）
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setActive(false);
        userRepository.save(user);
    }
}