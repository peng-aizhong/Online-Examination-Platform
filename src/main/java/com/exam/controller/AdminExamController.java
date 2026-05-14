package com.exam.controller;

import com.exam.entity.Paper;
import com.exam.entity.ExamSession;
import com.exam.entity.Subject;
import com.exam.service.AdminExamService;
import com.exam.service.SubjectService;
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
 * 管理员考试管理控制器
 */
@Controller
@RequestMapping("/admin/exams")
public class AdminExamController extends AdminController {
    
    @Autowired
    private AdminExamService adminExamService;
    
    @Autowired
    private SubjectService subjectService;
    
    /**
     * 考试管理首页 - 重定向到试卷列表
     */
    @GetMapping
    public String examHome() {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        return "redirect:/admin/exams/papers";
    }
    
    // ========== 试卷管理 ==========
    
    /**
     * 试卷列表页面
     */
    @GetMapping("/papers")
    public String paperList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "subjectId", defaultValue = "all") String subjectId,
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
        
        // 获取试卷列表
        Page<Paper> papers;
        if (keyword != null && !keyword.trim().isEmpty() || 
            !"all".equals(subjectId) || !"all".equals(status)) {
            papers = adminExamService.searchPapers(keyword, subjectId, status, pageable);
        } else {
            papers = adminExamService.getAllPapers(pageable);
        }
        
        // 获取科目列表用于筛选
        List<Subject> subjects = subjectService.getAllSubjects();
        
        // 获取统计数据
        Map<String, Object> statistics = adminExamService.getPaperStatistics();
        
        model.addAttribute("papers", papers);
        model.addAttribute("subjects", subjects);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", papers.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        
        return "admin/exam/paper-list";
    }
    
    /**
     * 试卷详情页面
     */
    @GetMapping("/papers/{paperId}")
    public String paperDetail(@PathVariable String paperId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        Paper paper = adminExamService.getPaperById(paperId);
        if (paper == null) {
            return "redirect:/admin/exams/papers?error=paper_not_found";
        }
        
        model.addAttribute("paper", paper);
        return "admin/exam/paper-detail";
    }
    
    /**
     * 试卷编辑页面
     */
    @GetMapping("/papers/{paperId}/edit")
    public String paperEditForm(@PathVariable String paperId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        Paper paper = adminExamService.getPaperById(paperId);
        if (paper == null) {
            return "redirect:/admin/exams/papers?error=paper_not_found";
        }
        
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("paper", paper);
        model.addAttribute("subjects", subjects);
        return "admin/exam/paper-edit";
    }
    
    /**
     * 更新试卷信息
     */
    @PostMapping("/papers/{paperId}/update")
    public String updatePaper(@PathVariable String paperId,
                            @ModelAttribute Paper paper,
                            RedirectAttributes redirectAttributes) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        try {
            paper.setPaperId(paperId);
            adminExamService.updatePaper(paper);
            redirectAttributes.addFlashAttribute("success", "试卷信息更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败：" + e.getMessage());
        }
        
        return "redirect:/admin/exams/papers/" + paperId;
    }
    
    /**
     * 更新试卷状态
     */
    @PostMapping("/papers/{paperId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePaperStatus(
            @PathVariable String paperId,
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
            boolean success = adminExamService.updatePaperStatus(paperId, status);
            response.put("success", success);
            response.put("message", success ? "状态更新成功" : "状态更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除试卷
     */
    @PostMapping("/papers/{paperId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePaper(@PathVariable String paperId) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = adminExamService.deletePaper(paperId);
            response.put("success", success);
            response.put("message", success ? "试卷删除成功" : "试卷删除失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // ========== 考试会话管理 ==========
    
    /**
     * 考试会话列表页面
     */
    @GetMapping("/sessions")
    public String examSessionList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "paperId", defaultValue = "all") String paperId,
            @RequestParam(value = "studentId", defaultValue = "all") String studentId,
            @RequestParam(value = "status", defaultValue = "all") String status,
            @RequestParam(value = "sort", defaultValue = "startedAt") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            Model model) {
        
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        // 创建分页和排序对象
        Sort.Direction sortDirection = "asc".equals(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // 获取考试会话列表
        Page<ExamSession> sessions;
        if (!"all".equals(paperId) || !"all".equals(studentId) || !"all".equals(status)) {
            sessions = adminExamService.searchExamSessions(null, paperId, studentId, status, pageable);
        } else {
            sessions = adminExamService.getAllExamSessions(pageable);
        }
        
        // 获取统计数据
        Map<String, Object> statistics = adminExamService.getExamSessionStatistics();
        
        model.addAttribute("sessions", sessions);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sessions.getTotalPages());
        model.addAttribute("paperId", paperId);
        model.addAttribute("studentId", studentId);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        
        return "admin/exam/session-list";
    }
    
    /**
     * 考试会话详情页面
     */
    @GetMapping("/sessions/{sessionId}")
    public String examSessionDetail(@PathVariable String sessionId, Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        ExamSession session = adminExamService.getExamSessionById(sessionId);
        if (session == null) {
            return "redirect:/admin/exams/sessions?error=session_not_found";
        }
        
        model.addAttribute("session", session);
        return "admin/exam/session-detail";
    }
    
    /**
     * 更新考试会话状态
     */
    @PostMapping("/sessions/{sessionId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateExamSessionStatus(
            @PathVariable String sessionId,
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
            boolean success = adminExamService.updateExamSessionStatus(sessionId, status);
            response.put("success", success);
            response.put("message", success ? "状态更新成功" : "状态更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除考试会话
     */
    @PostMapping("/sessions/{sessionId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteExamSession(@PathVariable String sessionId) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不足");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = adminExamService.deleteExamSession(sessionId);
            response.put("success", success);
            response.put("message", success ? "考试会话删除成功" : "考试会话删除失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // ========== 监控页面 ==========
    
    /**
     * 考试监控页面
     */
    @GetMapping("/monitor")
    public String examMonitor(Model model) {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return redirectPath;
        }
        
        // 获取进行中的考试会话
        List<ExamSession> ongoingSessions = adminExamService.getOngoingExamSessions();
        
        // 获取待阅卷的考试会话
        List<ExamSession> pendingGradingSessions = adminExamService.getPendingGradingExamSessions();
        
        // 获取统计数据
        Map<String, Object> statistics = adminExamService.getExamStatistics();
        
        model.addAttribute("ongoingSessions", ongoingSessions);
        model.addAttribute("pendingGradingSessions", pendingGradingSessions);
        model.addAttribute("statistics", statistics);
        
        return "admin/exam/monitor";
    }
    
    // ========== 统计API ==========
    
    /**
     * 获取考试统计数据的API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExamStatistics() {
        String redirectPath = checkAdminPermission();
        if (redirectPath != null) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> statistics = adminExamService.getExamStatistics();
        return ResponseEntity.ok(statistics);
    }
}
