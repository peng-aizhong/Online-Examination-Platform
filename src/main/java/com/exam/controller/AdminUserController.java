package com.exam.controller;

import com.exam.entity.User;
import com.exam.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController extends AdminController {
    
    @Autowired
    private AdminUserService adminUserService;
    
    /**
     * 用户列表页面
     */
    @GetMapping
    public String userList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", defaultValue = "all") String role,
            @RequestParam(value = "status", defaultValue = "all") String status,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            Model model) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        // 创建分页和排序对象
        Sort.Direction sortDirection = "asc".equals(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // 获取用户列表
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty() || 
            !"all".equals(role) || !"all".equals(status)) {
            users = adminUserService.searchUsers(keyword, role, status, pageable);
        } else {
            users = adminUserService.getAllUsers(pageable);
        }
        
        // 获取统计数据
        Map<String, Object> statistics = adminUserService.getUserStatistics();
        
        model.addAttribute("users", users);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        
        return "admin/user/list";
    }
    
    /**
     * 用户详情页面
     */
    @GetMapping("/{userId}")
    public String userDetail(@PathVariable String userId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        User user = adminUserService.getUserById(userId);
        if (user == null) {
            return "redirect:/admin/users?error=user_not_found";
        }
        
        model.addAttribute("user", user);
        return "admin/user/detail";
    }
    
    /**
     * 用户编辑页面
     */
    @GetMapping("/{userId}/edit")
    public String userEditForm(@PathVariable String userId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        User user = adminUserService.getUserById(userId);
        if (user == null) {
            return "redirect:/admin/users?error=user_not_found";
        }
        
        model.addAttribute("user", user);
        return "admin/user/edit";
    }
    
    /**
     * 更新用户信息
     */
    @PostMapping("/{userId}/update")
    public String updateUser(@PathVariable String userId, 
                           @ModelAttribute User user,
                           RedirectAttributes redirectAttributes) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        try {
            user.setUserId(userId);
            adminUserService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "用户信息更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败：" + e.getMessage());
        }
        
        return "redirect:/admin/users/" + userId;
    }
    
    /**
     * 更新用户状态
     */
    @PostMapping("/{userId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable String userId,
            @RequestParam boolean isActive) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = adminUserService.updateUserStatus(userId, isActive);
            response.put("success", success);
            response.put("message", success ? "状态更新成功" : "状态更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户角色
     */
    @PostMapping("/{userId}/role")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable String userId,
            @RequestParam String role) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = adminUserService.updateUserRole(userId, role);
            response.put("success", success);
            response.put("message", success ? "角色更新成功" : "角色更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量更新用户状态
     */
    @PostMapping("/batch/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdateUserStatus(
            @RequestParam List<String> userIds,
            @RequestParam boolean isActive) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            int count = adminUserService.batchUpdateUserStatus(userIds, isActive);
            response.put("success", true);
            response.put("message", "成功更新 " + count + " 个用户的状态");
            response.put("count", count);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量更新用户角色
     */
    @PostMapping("/batch/role")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdateUserRole(
            @RequestParam List<String> userIds,
            @RequestParam String role) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            int count = adminUserService.batchUpdateUserRole(userIds, role);
            response.put("success", true);
            response.put("message", "成功更新 " + count + " 个用户的角色");
            response.put("count", count);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除用户
     */
    @PostMapping("/{userId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = adminUserService.deleteUser(userId);
            response.put("success", success);
            response.put("message", success ? "用户删除成功" : "用户删除失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户统计数据的API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> statistics = adminUserService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}
