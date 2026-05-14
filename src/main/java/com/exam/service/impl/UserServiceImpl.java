package com.exam.service.impl;

import com.exam.entity.User;
import com.exam.entity.UserRole;
import com.exam.repository.UserRepository;
import com.exam.service.UserService;
import com.exam.dto.UserProfileUpdateRequest;
import com.exam.dto.PasswordChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User createUser(User user) {
        try {
            System.out.println("=== 开始创建用户 ===");
            System.out.println("用户名: " + user.getUsername());
            System.out.println("角色: " + user.getRole());
            System.out.println("邮箱: " + user.getEmail());
            
            // 检查用户名是否已存在
            if (existsByUsername(user.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 检查邮箱是否已存在（只有当邮箱不为空时才检查）
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                if (existsByEmail(user.getEmail().trim())) {
                    throw new RuntimeException("邮箱已存在");
                }
            }
            
            // 验证密码强度
            if (!validatePassword(user.getPassword())) {
                throw new RuntimeException("密码不符合要求：至少6位");
            }
            
            // 验证邮箱格式
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty() && !validateEmail(user.getEmail().trim())) {
                throw new RuntimeException("邮箱格式不正确");
            }
            
            // 验证手机号格式
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty() && !validatePhone(user.getPhone().trim())) {
                throw new RuntimeException("手机号格式不正确");
            }
            
            // 生成用户ID
            String userId = generateUserId(user.getRole());
            user.setUserId(userId);
            System.out.println("生成的用户ID: " + userId);
            
            // 设置默认值
            if (user.getIsActive() == null) {
                user.setIsActive(true);
            }
            
            // 保存用户
            User savedUser = userRepository.save(user);
            System.out.println("用户创建成功: " + savedUser.getUsername() + ", ID: " + savedUser.getUserId() + ", 角色: " + savedUser.getRole());
            return savedUser;
            
        } catch (Exception e) {
            System.err.println("创建用户失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建用户失败: " + e.getMessage());
        }
    }
    
    @Override
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 如果用户名发生变化，检查是否与其他用户冲突
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 如果邮箱发生变化，检查是否与其他用户冲突
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail()) && 
            existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 如果密码发生变化，验证密码强度
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!validatePassword(user.getPassword())) {
                throw new RuntimeException("密码不符合要求：至少6位");
            }
            existingUser.setPassword(user.getPassword());
        } else {
            user.setPassword(existingUser.getPassword());
        }
        
        return userRepository.save(user);
    }
    
    @Override
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    @Override
    public List<User> getStudents() {
        return userRepository.findByRole("student");
    }
    
    @Override
    public List<User> getStudentsByDepartment(String department) {
        return userRepository.findByRoleAndDepartment("student", department);
    }
    
    @Override
    public List<String> getAllDepartments() {
        return userRepository.findDistinctDepartments();
    }
    
    @Override
    public List<User> getTeachers() {
        return userRepository.findByRole("teacher");
    }
    
    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameOrDepartmentContaining(keyword);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public User authenticateUser(String username, String password) {
        System.out.println("尝试登录用户: " + username);
        
        Optional<User> userOpt = getUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("找到用户: " + user.getUsername() + ", 角色: " + user.getRole() + ", 状态: " + user.getIsActive());
            System.out.println("输入密码: " + password);
            System.out.println("数据库密码: " + user.getPassword());
            
            // 直接比较密码，不加密
            if (password.equals(user.getPassword()) && user.getIsActive()) {
                System.out.println("登录成功: " + user.getUsername());
                return user;
            } else {
                System.out.println("登录失败: 密码不匹配或用户未激活");
                if (!password.equals(user.getPassword())) {
                    System.out.println("密码不匹配");
                }
                if (!user.getIsActive()) {
                    System.out.println("用户未激活");
                }
            }
        } else {
            System.out.println("未找到用户: " + username);
        }
        return null;
    }
    
    @Override
    public String generateUserId(String role) {
        String prefix;
        switch (role.toLowerCase()) {
            case "admin":
                prefix = "A";
                break;
            case "teacher":
                prefix = "T";
                break;
            case "student":
                prefix = "S";
                break;
            default:
                prefix = "U";
        }
        
        // 获取当前时间戳作为后缀，只取后4位数字
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        return prefix + timestamp.substring(timestamp.length() - 4);
    }
    
    @Override
    public boolean validatePassword(String password) {
        // 密码至少6位即可
        return password != null && password.length() >= 6;
    }
    
    @Override
    public boolean validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // 邮箱可以为空
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
    
    @Override
    public boolean validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // 手机号可以为空
        }
        String phoneRegex = "^1[3-9]\\d{9}$";
        return Pattern.compile(phoneRegex).matcher(phone).matches();
    }
    
    @Override
    public User updateUserProfile(String userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否与其他用户冲突
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && 
            existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被其他用户使用");
        }
        
        // 更新用户信息
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    @Override
    public boolean changePassword(String userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证当前密码
        if (!user.getPassword().equals(request.getCurrentPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        // 验证新密码格式
        if (!validatePassword(request.getNewPassword())) {
            throw new RuntimeException("新密码不符合要求：至少6位");
        }
        
        // 验证确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("新密码与确认密码不匹配");
        }
        
        // 更新密码
        user.setPassword(request.getNewPassword());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return true;
    }
    
    @Override
    public String updateUserAvatar(String userId, String avatarPath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setAvatar(avatarPath);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return avatarPath;
    }
} 