package com.exam.repository;

import com.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(String role);
    
    List<User> findByRoleAndDepartment(String role, String department);
    
    List<User> findByIsActiveTrue();
    
    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL ORDER BY u.department")
    List<String> findDistinctDepartments();
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.department LIKE %:keyword%")
    List<User> findByUsernameOrDepartmentContaining(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);
    
    /**
     * 统计活跃用户数量
     */
    long countByIsActiveTrue();
    
    /**
     * 统计指定时间后创建的用户数量
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);
} 