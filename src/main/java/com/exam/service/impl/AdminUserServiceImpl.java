package com.exam.service.impl;

import com.exam.entity.User;
import com.exam.repository.UserRepository;
import com.exam.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员用户管理服务实现类
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Override
    public Page<User> searchUsers(String keyword, String role, String status, Pageable pageable) {
        // 由于UserRepository没有继承JpaSpecificationExecutor，我们使用简单的查询方法
        // 这里先获取所有用户，然后在内存中进行筛选（对于小量数据是可行的）
        // 在实际生产环境中，建议在UserRepository中添加相应的查询方法
        
        List<User> allUsers = userRepository.findAll();
        List<User> filteredUsers = new ArrayList<>();
        
        for (User user : allUsers) {
            boolean matches = true;
            
            // 关键词搜索（用户名、邮箱、部门）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchLower = keyword.trim().toLowerCase();
                boolean matchesSearch = 
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchLower)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) ||
                    (user.getDepartment() != null && user.getDepartment().toLowerCase().contains(searchLower));
                if (!matchesSearch) {
                    matches = false;
                }
            }
            
            // 角色筛选
            if (matches && role != null && !role.trim().isEmpty() && !"all".equals(role)) {
                if (!role.equals(user.getRole())) {
                    matches = false;
                }
            }
            
            // 状态筛选
            if (matches && status != null && !status.trim().isEmpty() && !"all".equals(status)) {
                boolean isActive = "active".equals(status);
                if (isActive != user.getIsActive()) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredUsers.add(user);
            }
        }
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());
        List<User> pageContent = filteredUsers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredUsers.size());
    }
    
    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    @Override
    public User updateUser(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        User existingUser = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新允许修改的字段
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setDepartment(user.getDepartment());
        existingUser.setRole(user.getRole());
        existingUser.setIsActive(user.getIsActive());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }
    
    @Override
    public boolean updateUserStatus(String userId, boolean isActive) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setIsActive(isActive);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean updateUserRole(String userId, String role) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 验证角色有效性
            if (!Arrays.asList("admin", "teacher", "student").contains(role)) {
                throw new IllegalArgumentException("无效的角色");
            }
            
            user.setRole(role);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public int batchUpdateUserStatus(List<String> userIds, boolean isActive) {
        int count = 0;
        for (String userId : userIds) {
            if (updateUserStatus(userId, isActive)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public int batchUpdateUserRole(List<String> userIds, String role) {
        int count = 0;
        for (String userId : userIds) {
            if (updateUserRole(userId, role)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public boolean deleteUser(String userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查是否为管理员用户，防止删除管理员
            if ("admin".equals(user.getRole())) {
                throw new RuntimeException("不能删除管理员用户");
            }
            
            userRepository.delete(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        statistics.put("totalUsers", totalUsers);
        
        // 各角色用户数
        Map<String, Long> roleStats = getRoleDistribution();
        statistics.put("roleDistribution", roleStats);
        
        // 状态统计
        Map<String, Long> statusStats = getUserStatusStatistics();
        statistics.put("statusDistribution", statusStats);
        
        // 活跃用户数（最近30天有活动的用户）
        long activeUsers = userRepository.countByIsActiveTrue();
        statistics.put("activeUsers", activeUsers);
        
        // 今日新增用户数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewUsers = userRepository.countByCreatedAtAfter(today);
        statistics.put("todayNewUsers", todayNewUsers);
        
        return statistics;
    }
    
    @Override
    public Map<String, Long> getRoleDistribution() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
            .collect(Collectors.groupingBy(
                User::getRole,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getUserStatusStatistics() {
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> statusStats = new HashMap<>();
        
        long activeCount = allUsers.stream()
            .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
            .count();
        long inactiveCount = allUsers.size() - activeCount;
        
        statusStats.put("active", activeCount);
        statusStats.put("inactive", inactiveCount);
        
        return statusStats;
    }
}
