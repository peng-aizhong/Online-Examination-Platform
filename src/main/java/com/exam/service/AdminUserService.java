package com.exam.service;

import com.exam.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理服务接口
 */
public interface AdminUserService {
    
    /**
     * 获取所有用户（分页）
     */
    Page<User> getAllUsers(Pageable pageable);
    
    /**
     * 根据条件搜索用户
     */
    Page<User> searchUsers(String keyword, String role, String status, Pageable pageable);
    
    /**
     * 获取用户详情
     */
    User getUserById(String userId);
    
    /**
     * 更新用户信息
     */
    User updateUser(User user);
    
    /**
     * 更新用户状态
     */
    boolean updateUserStatus(String userId, boolean isActive);
    
    /**
     * 更新用户角色
     */
    boolean updateUserRole(String userId, String role);
    
    /**
     * 批量更新用户状态
     */
    int batchUpdateUserStatus(List<String> userIds, boolean isActive);
    
    /**
     * 批量更新用户角色
     */
    int batchUpdateUserRole(List<String> userIds, String role);
    
    /**
     * 删除用户
     */
    boolean deleteUser(String userId);
    
    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取角色分布统计
     */
    Map<String, Long> getRoleDistribution();
    
    /**
     * 获取用户状态统计
     */
    Map<String, Long> getUserStatusStatistics();
}
