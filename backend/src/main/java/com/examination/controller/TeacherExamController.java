package com.examination.controller;

import com.examination.common.ApiResponse;
import com.examination.dto.ClassStatisticsResponse;
import com.examination.dto.GradingResultResponse;
import com.examination.service.TeacherExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeacherExamController {
    @Autowired
    private TeacherExamService teacherExamService;

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ClassStatisticsResponse>> getClassStatistics() {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(teacherExamService.getClassStatistics(username)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get class statistics error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取班级统计失败"));
        }
    }

    @GetMapping("/grading/{assignmentId}")
    public ResponseEntity<ApiResponse<List<GradingResultResponse>>> getGradingResults(@PathVariable String assignmentId) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(teacherExamService.getGradingResults(username, assignmentId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get grading results error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取阅卷结果失败"));
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        return authentication.getName();
    }
}
