package com.exam.service;

import com.exam.entity.User;
import com.exam.dto.UserProfileUpdateRequest;
import com.exam.dto.PasswordChangeRequest;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User createUser(User user);
    
    User updateUser(User user);
    
    void deleteUser(String userId);
    
    Optional<User> getUserById(String userId);
    
    Optional<User> getUserByUsername(String username);
    
    List<User> getAllUsers();
    
    List<User> getUsersByRole(String role);
    
    /**
     * 获取所有学生
     */
    List<User> getStudents();
    
    /**
     * 根据部门获取学生
     */
    List<User> getStudentsByDepartment(String department);
    
    /**
     * 获取所有部门列表
     */
    List<String> getAllDepartments();
    
    /**
     * 获取所有教师
     */
    List<User> getTeachers();
    
    List<User> searchUsers(String keyword);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    User authenticateUser(String username, String password);
    
    String generateUserId(String role);
    
    boolean validatePassword(String password);
    
    boolean validateEmail(String email);
    
    boolean validatePhone(String phone);
    
    /**
     * 更新用户个人资料
     */
    User updateUserProfile(String userId, UserProfileUpdateRequest request);
    
    /**
     * 修改用户密码
     */
    boolean changePassword(String userId, PasswordChangeRequest request);
    
    /**
     * 更新用户头像
     */
    String updateUserAvatar(String userId, String avatarPath);
} 