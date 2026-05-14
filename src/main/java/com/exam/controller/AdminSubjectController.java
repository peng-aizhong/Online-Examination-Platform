package com.exam.controller;

import com.exam.entity.Subject;
import com.exam.service.AdminSubjectService;
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
import java.util.Map;

/**
 * 管理员科目管理控制器
 */
@Controller
@RequestMapping("/admin/subjects")
public class AdminSubjectController extends AdminController {
    
    @Autowired
    private AdminSubjectService adminSubjectService;
    
    /**
     * 科目列表页面
     */
    @GetMapping
    public String subjectList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
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
        
        // 获取科目列表
        Page<Subject> subjects;
        if (keyword != null && !keyword.trim().isEmpty() || !"all".equals(status)) {
            subjects = adminSubjectService.searchSubjects(keyword, status, pageable);
        } else {
            subjects = adminSubjectService.getAllSubjects(pageable);
        }
        
        // 获取统计数据
        Map<String, Object> statistics = adminSubjectService.getSubjectStatistics();
        
        model.addAttribute("subjects", subjects);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", subjects.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        
        return "admin/subject/list";
    }
    
    /**
     * 科目详情页面
     */
    @GetMapping("/{subjectId}")
    public String subjectDetail(@PathVariable String subjectId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        Subject subject = adminSubjectService.getSubjectById(subjectId);
        if (subject == null) {
            return "redirect:/admin/subjects?error=subject_not_found";
        }
        
        model.addAttribute("subject", subject);
        return "admin/subject/detail";
    }
    
    /**
     * 科目编辑页面
     */
    @GetMapping("/{subjectId}/edit")
    public String subjectEditForm(@PathVariable String subjectId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        Subject subject = adminSubjectService.getSubjectById(subjectId);
        if (subject == null) {
            return "redirect:/admin/subjects?error=subject_not_found";
        }
        
        model.addAttribute("subject", subject);
        return "admin/subject/edit";
    }
    
    /**
     * 新增科目页面
     */
    @GetMapping("/create")
    public String subjectCreateForm(Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        model.addAttribute("subject", new Subject());
        return "admin/subject/create";
    }
    
    /**
     * 创建科目
     */
    @PostMapping("/create")
    public String createSubject(@ModelAttribute Subject subject,
                              RedirectAttributes redirectAttributes) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        try {
            // 基本验证
            if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "科目名称不能为空");
                return "redirect:/admin/subjects/create";
            }
            
            if (subject.getSubjectCode() == null || subject.getSubjectCode().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "科目代码不能为空");
                return "redirect:/admin/subjects/create";
            }
            
            // 科目代码格式验证（后端验证）
            String subjectCode = subject.getSubjectCode().trim();
            if (subjectCode.length() < 3 || subjectCode.length() > 20) {
                redirectAttributes.addFlashAttribute("error", "科目代码长度应为3-20位字符");
                return "redirect:/admin/subjects/create";
            }
            
            if (!subjectCode.matches("^[A-Za-z0-9]+$")) {
                redirectAttributes.addFlashAttribute("error", "科目代码只能包含字母和数字");
                return "redirect:/admin/subjects/create";
            }
            
            // 验证科目代码是否已存在
            if (adminSubjectService.existsBySubjectCode(subjectCode)) {
                redirectAttributes.addFlashAttribute("error", "科目代码已存在");
                return "redirect:/admin/subjects/create";
            }
            
            // 验证科目名称是否已存在
            if (adminSubjectService.existsBySubjectName(subject.getSubjectName().trim())) {
                redirectAttributes.addFlashAttribute("error", "科目名称已存在");
                return "redirect:/admin/subjects/create";
            }
            
            // 设置清理后的数据
            subject.setSubjectCode(subjectCode);
            subject.setSubjectName(subject.getSubjectName().trim());
            if (subject.getDescription() != null) {
                subject.setDescription(subject.getDescription().trim());
            }
            
            Subject createdSubject = adminSubjectService.createSubject(subject);
            redirectAttributes.addFlashAttribute("success", "科目创建成功");
            return "redirect:/admin/subjects/" + createdSubject.getSubjectId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败：" + e.getMessage());
            return "redirect:/admin/subjects/create";
        }
    }
    
    /**
     * 更新科目信息
     */
    @PostMapping("/{subjectId}/update")
    public String updateSubject(@PathVariable String subjectId,
                              @ModelAttribute Subject subject,
                              RedirectAttributes redirectAttributes) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        try {
            // 基本验证
            if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "科目名称不能为空");
                return "redirect:/admin/subjects/" + subjectId + "/edit";
            }
            
            if (subject.getSubjectCode() == null || subject.getSubjectCode().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "科目代码不能为空");
                return "redirect:/admin/subjects/" + subjectId + "/edit";
            }
            
            // 科目代码格式验证（后端验证）
            String subjectCode = subject.getSubjectCode().trim();
            if (subjectCode.length() < 3 || subjectCode.length() > 20) {
                redirectAttributes.addFlashAttribute("error", "科目代码长度应为3-20位字符");
                return "redirect:/admin/subjects/" + subjectId + "/edit";
            }
            
            if (!subjectCode.matches("^[A-Za-z0-9]+$")) {
                redirectAttributes.addFlashAttribute("error", "科目代码只能包含字母和数字");
                return "redirect:/admin/subjects/" + subjectId + "/edit";
            }
            
            // 检查科目代码是否被其他科目使用
            Subject existingSubject = adminSubjectService.getSubjectById(subjectId);
            if (existingSubject != null && !existingSubject.getSubjectCode().equals(subjectCode)) {
                if (adminSubjectService.existsBySubjectCode(subjectCode)) {
                    redirectAttributes.addFlashAttribute("error", "科目代码已被其他科目使用");
                    return "redirect:/admin/subjects/" + subjectId + "/edit";
                }
            }
            
            // 检查科目名称是否被其他科目使用
            if (existingSubject != null && !existingSubject.getSubjectName().equals(subject.getSubjectName().trim())) {
                if (adminSubjectService.existsBySubjectName(subject.getSubjectName().trim())) {
                    redirectAttributes.addFlashAttribute("error", "科目名称已被其他科目使用");
                    return "redirect:/admin/subjects/" + subjectId + "/edit";
                }
            }
            
            // 设置清理后的数据
            subject.setSubjectId(subjectId);
            subject.setSubjectCode(subjectCode);
            subject.setSubjectName(subject.getSubjectName().trim());
            if (subject.getDescription() != null) {
                subject.setDescription(subject.getDescription().trim());
            }
            
            adminSubjectService.updateSubject(subject);
            redirectAttributes.addFlashAttribute("success", "科目信息更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败：" + e.getMessage());
        }
        
        return "redirect:/admin/subjects/" + subjectId;
    }
    
    /**
     * 更新科目状态
     */
    @PostMapping("/{subjectId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSubjectStatus(
            @PathVariable String subjectId,
            @RequestParam String status) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            Subject.SubjectStatus subjectStatus = Subject.SubjectStatus.valueOf(status.toUpperCase());
            boolean success = adminSubjectService.updateSubjectStatus(subjectId, subjectStatus);
            response.put("success", success);
            response.put("message", success ? "状态更新成功" : "状态更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除科目
     */
    @PostMapping("/{subjectId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSubject(@PathVariable String subjectId) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = adminSubjectService.deleteSubject(subjectId);
            response.put("success", success);
            response.put("message", success ? "科目删除成功" : "科目删除失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取科目统计数据的API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSubjectStatistics() {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> statistics = adminSubjectService.getSubjectStatistics();
        return ResponseEntity.ok(statistics);
    }
}
