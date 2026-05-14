package com.exam.controller;

import com.exam.entity.User;
import com.exam.entity.UserRole;
import com.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        if (logout != null) {
            model.addAttribute("success", "您已成功退出登录");
        }
        // 为注册表单提供必要的模型属性
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        return "login";
    }
    
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        return "login";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                             BindingResult result, 
                             @RequestParam("confirmPassword") String confirmPassword,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        System.out.println("=== 注册请求 ===");
        System.out.println("用户名: " + user.getUsername());
        System.out.println("角色: " + user.getRole());
        System.out.println("邮箱: " + user.getEmail());
        System.out.println("部门: " + user.getDepartment());
        
        if (result.hasErrors()) {
            System.out.println("表单验证失败: " + result.getAllErrors());
            model.addAttribute("roles", UserRole.values());
            return "login";
        }
        
        // 验证确认密码
        if (!user.getPassword().equals(confirmPassword)) {
            result.rejectValue("password", "error.password", "两次输入的密码不一致");
            model.addAttribute("roles", UserRole.values());
            return "login";
        }
        
        // 转换角色为小写字符串
        if (user.getRole() != null) {
            user.setRole(user.getRole().toLowerCase());
        }
        
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", "注册成功！请登录。");
            return "redirect:/login";
        } catch (RuntimeException e) {
            System.err.println("注册失败: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            return "login";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        // 根据用户角色跳转到相应的页面
        String roleValue = user.getRole().toLowerCase();
        switch (roleValue) {
            case "admin":
                return "redirect:/admin";
            case "teacher":
                return "redirect:/teacher";
            case "student":
                return "redirect:/student";
            default:
                model.addAttribute("user", user);
                return "dashboard";
        }
    }
    
    @GetMapping("/admin")
    public String adminPanel(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null || !"admin".equals(user.getRole().toLowerCase())) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "admin/panel";
    }
    
    @GetMapping("/teacher")
    public String teacherPanel(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null || !"teacher".equals(user.getRole().toLowerCase())) {
            return "redirect:/login";
        }
        return "redirect:/teacher/dashboard";
    }
    

    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
} 