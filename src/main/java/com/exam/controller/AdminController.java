package com.exam.controller;

import com.exam.entity.User;
import com.exam.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 管理员控制器基类
 * 提供统一的权限验证、异常处理和数据模型
 */
public abstract class AdminController {
    
    @Autowired
    protected UserService userService;
    
    /**
     * 统一权限验证
     * 确保只有管理员可以访问
     */
    protected boolean isAdminAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }
        
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        return user != null && "admin".equals(user.getRole().toLowerCase());
    }
    
    /**
     * 获取当前登录的管理员用户
     */
    protected User getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        
        String username = auth.getName();
        return userService.getUserByUsername(username).orElse(null);
    }
    
    /**
     * 统一数据模型设置
     * 为所有管理员页面提供基础数据
     */
    @ModelAttribute
    protected void addCommonAttributes(Model model) {
        if (isAdminAuthenticated()) {
            User admin = getCurrentAdmin();
            model.addAttribute("admin", admin);
            model.addAttribute("user", admin); // 兼容现有模板
            
            // 添加基础统计数据
            model.addAttribute("totalUsers", userService.getAllUsers().size());
            model.addAttribute("totalStudents", userService.getStudents().size());
            model.addAttribute("totalTeachers", userService.getTeachers().size());
        }
    }
    
    /**
     * 检查权限并返回重定向路径
     * 如果权限不足，返回登录页面路径
     */
    protected String checkAdminPermission() {
        if (!isAdminAuthenticated()) {
            return "redirect:/login";
        }
        return null;
    }
}
