package com.exam.controller;

import com.exam.entity.User;
import com.exam.service.UserService;
import com.exam.dto.UserProfileUpdateRequest;
import com.exam.dto.PasswordChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    // 头像上传目录
    private static final String UPLOAD_DIR = "uploads/avatars/";
    
    /**
     * 个人中心主页
     */
    @GetMapping
    public String profilePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            model.addAttribute("profileRequest", new UserProfileUpdateRequest());
            model.addAttribute("passwordRequest", new PasswordChangeRequest());
        }
        
        return "profile/index";
    }
    
    /**
     * 更新个人资料
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profileRequest") UserProfileUpdateRequest request,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "请检查输入信息");
            return "redirect:/profile";
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isPresent()) {
                User updatedUser = userService.updateUserProfile(userOpt.get().getUserId(), request);
                redirectAttributes.addFlashAttribute("success", "个人资料更新成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordRequest") PasswordChangeRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "请检查密码信息");
            return "redirect:/profile";
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isPresent()) {
                boolean success = userService.changePassword(userOpt.get().getUserId(), request);
                if (success) {
                    redirectAttributes.addFlashAttribute("success", "密码修改成功");
                } else {
                    redirectAttributes.addFlashAttribute("error", "密码修改失败");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请选择要上传的头像文件");
            return "redirect:/profile";
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "只能上传图片文件");
            return "redirect:/profile";
        }
        
        // 检查文件大小（限制为5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "头像文件大小不能超过5MB");
            return "redirect:/profile";
        }
        
        try {
            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isPresent()) {
                // 创建uploads/avatars目录（使用项目根目录）
                String projectRoot = System.getProperty("user.dir");
                String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "avatars";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        redirectAttributes.addFlashAttribute("error", "无法创建上传目录，请检查权限");
                        return "redirect:/profile";
                    }
                }
                
                // 生成唯一的文件名
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = "avatar_" + System.currentTimeMillis() + fileExtension;
                
                // 保存文件到uploads/avatars目录
                File destFile = new File(dir, newFilename);
                file.transferTo(destFile);
                
                // 验证文件是否成功保存
                if (!destFile.exists() || destFile.length() == 0) {
                    redirectAttributes.addFlashAttribute("error", "文件保存失败，请重试");
                    return "redirect:/profile";
                }
                
                // 更新用户头像路径到数据库（使用相对路径）
                String avatarPath = "/uploads/avatars/" + newFilename;
                userService.updateUserAvatar(userOpt.get().getUserId(), avatarPath);
                redirectAttributes.addFlashAttribute("success", "头像上传成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "头像上传失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/profile";
    }
} 