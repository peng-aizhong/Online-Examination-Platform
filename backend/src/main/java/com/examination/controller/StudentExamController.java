package com.examination.controller;

import com.examination.common.ApiResponse;
import com.examination.dto.*;
import com.examination.service.StudentExamService;
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
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class StudentExamController {
    @Autowired
    private StudentExamService studentExamService;

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<AssignmentItemResponse>>> listAssignments() {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(studentExamService.listAssignments(username)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List assignments error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取考试列表失败"));
        }
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> getAssignmentDetail(@PathVariable String assignmentId) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(studentExamService.getAssignmentDetail(username, assignmentId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get assignment detail error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取考试详情失败"));
        }
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<ApiResponse<SubmitExamResponse>> submitExam(@PathVariable String assignmentId,
                                                                      @RequestBody SubmitExamRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success("提交成功", studentExamService.submitExam(username, assignmentId, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Submit exam error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("提交试卷失败"));
        }
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<SessionResultResponse>>> listResults() {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(studentExamService.listResults(username)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List results error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取成绩失败"));
        }
    }

    @GetMapping("/report/{sessionId}")
    public ResponseEntity<ApiResponse<ScoreReportResponse>> getScoreReport(@PathVariable String sessionId) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(studentExamService.getScoreReport(username, sessionId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get score report error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取成绩报告失败"));
        }
    }

    @GetMapping("/wrong-questions")
    public ResponseEntity<ApiResponse<WrongQuestionResponse>> listWrongQuestions() {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(studentExamService.listWrongQuestions(username)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List wrong questions error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取错题本失败"));
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
