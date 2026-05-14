package com.exam.controller;

import com.exam.entity.User;
import com.exam.service.AdminStatisticsService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    @Autowired
    private UserService userService;

    /**
     * 统计概览页面
     */
    @GetMapping
    public String statisticsOverview(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (userOptional.isEmpty() || !"admin".equals(userOptional.get().getRole().toLowerCase())) {
            return "redirect:/login";
        }

        // 获取系统总览统计
        Map<String, Object> systemOverview = adminStatisticsService.getSystemOverview();
        model.addAttribute("systemOverview", systemOverview);

        // 获取今日统计
        Map<String, Object> todayStats = adminStatisticsService.getTodayStatistics();
        model.addAttribute("todayStats", todayStats);

        // 获取成绩统计
        Map<String, Object> scoreStats = adminStatisticsService.getScoreStatistics();
        model.addAttribute("scoreStats", scoreStats);

        // 获取通过率统计
        Map<String, Object> passRateStats = adminStatisticsService.getPassRateStatistics();
        model.addAttribute("passRateStats", passRateStats);

        // 获取用户活跃度统计
        Map<String, Object> userActivityStats = adminStatisticsService.getUserActivityStatistics();
        model.addAttribute("userActivityStats", userActivityStats);

        model.addAttribute("admin", userOptional.get());
        return "admin/statistics/overview";
    }

    /**
     * 用户统计页面
     */
    @GetMapping("/users")
    public String userStatistics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (userOptional.isEmpty() || !"admin".equals(userOptional.get().getRole().toLowerCase())) {
            return "redirect:/login";
        }

        // 获取用户统计
        Map<String, Object> userStats = adminStatisticsService.getUserStatistics();
        model.addAttribute("userStats", userStats);

        // 获取用户详情
        List<Map<String, Object>> userDetails = adminStatisticsService.getUserDetails();
        model.addAttribute("userDetails", userDetails);

        model.addAttribute("admin", userOptional.get());
        return "admin/statistics/users";
    }

    /**
     * 考试统计页面
     */
    @GetMapping("/exams")
    public String examStatistics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (userOptional.isEmpty() || !"admin".equals(userOptional.get().getRole().toLowerCase())) {
            return "redirect:/login";
        }

        // 获取考试统计
        Map<String, Object> examStats = adminStatisticsService.getExamStatistics();
        model.addAttribute("examStats", examStats);

        // 获取成绩分布
        Map<String, Object> scoreDistribution = adminStatisticsService.getScoreDistribution();
        model.addAttribute("scoreDistribution", scoreDistribution);

        // 获取科目统计
        List<Map<String, Object>> subjectStats = adminStatisticsService.getSubjectDetailsStatistics();
        model.addAttribute("subjectStats", subjectStats);

        model.addAttribute("admin", userOptional.get());
        return "admin/statistics/exams";
    }

    /**
     * 获取用户注册趋势数据
     */
    @GetMapping("/api/trends/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUserRegistrationTrend(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<Map<String, Object>> trendData = adminStatisticsService.getUserRegistrationTrend(days);
            return ResponseEntity.ok(trendData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取考试完成趋势数据
     */
    @GetMapping("/api/trends/exams")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getExamCompletionTrend(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<Map<String, Object>> trendData = adminStatisticsService.getExamCompletionTrend(days);
            return ResponseEntity.ok(trendData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取系统概览数据
     */
    @GetMapping("/api/overview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        try {
            Map<String, Object> overview = adminStatisticsService.getSystemOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取今日统计数据
     */
    @GetMapping("/api/today")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTodayStatistics() {
        try {
            Map<String, Object> todayStats = adminStatisticsService.getTodayStatistics();
            return ResponseEntity.ok(todayStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> userStats = adminStatisticsService.getUserStatistics();
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取考试统计数据
     */
    @GetMapping("/api/exams")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExamStatistics() {
        try {
            Map<String, Object> examStats = adminStatisticsService.getExamStatistics();
            return ResponseEntity.ok(examStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取成绩统计数据
     */
    @GetMapping("/api/scores")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getScoreStatistics() {
        try {
            Map<String, Object> scoreStats = adminStatisticsService.getScoreStatistics();
            return ResponseEntity.ok(scoreStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取通过率统计数据
     */
    @GetMapping("/api/pass-rate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPassRateStatistics() {
        try {
            Map<String, Object> passRateStats = adminStatisticsService.getPassRateStatistics();
            return ResponseEntity.ok(passRateStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户活跃度统计数据
     */
    @GetMapping("/api/user-activity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserActivityStatistics() {
        try {
            Map<String, Object> userActivityStats = adminStatisticsService.getUserActivityStatistics();
            return ResponseEntity.ok(userActivityStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取成绩分布数据
     */
    @GetMapping("/api/score-distribution")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getScoreDistribution() {
        try {
            Map<String, Object> scoreDistribution = adminStatisticsService.getScoreDistribution();
            return ResponseEntity.ok(scoreDistribution);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取科目统计数据
     */
    @GetMapping("/api/subjects")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSubjectStatistics() {
        try {
            List<Map<String, Object>> subjectStats = adminStatisticsService.getSubjectDetailsStatistics();
            return ResponseEntity.ok(subjectStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户详情数据
     */
    @GetMapping("/api/user-details")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUserDetails() {
        try {
            List<Map<String, Object>> userDetails = adminStatisticsService.getUserDetails();
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}