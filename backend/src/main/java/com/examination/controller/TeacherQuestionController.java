package com.examination.controller;

import com.examination.common.ApiResponse;
import com.examination.dto.question.*;
import com.examination.entity.Question;
import com.examination.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
// 关键修改：根路径加了/v2后缀，彻底隔离所有接口
@RequestMapping("/api/teacher/questions/v2")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeacherQuestionController {

    @Autowired
    private QuestionService questionService;

    // 1. 创建试题
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Question>> createQuestion(@Valid @RequestBody QuestionCreateDTO dto) {
        try {
            String username = currentUsername();
            Question question = questionService.createQuestion(dto, username);
            return ResponseEntity.ok(ApiResponse.success("试题创建成功", question));
        } catch (RuntimeException e) {
            log.error("创建试题错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("创建试题错误: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建试题失败"));
        }
    }

    // 2. 提交审核
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> submitForReview(@PathVariable String id) {
        try {
            String username = currentUsername();
            questionService.submitForReview(id, username);
            return ResponseEntity.ok(ApiResponse.success("提交审核成功", null));
        } catch (RuntimeException e) {
            log.error("提交审核错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("提交审核错误: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交审核失败"));
        }
    }

    // 3. 审核试题（仅管理员）
    @PutMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reviewQuestion(@Valid @RequestBody QuestionReviewDTO dto) {
        try {
            String username = currentUsername();
            questionService.reviewQuestion(dto, username);
            return ResponseEntity.ok(ApiResponse.success("审核完成", null));
        } catch (RuntimeException e) {
            log.error("审核试题错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("审核试题错误: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("审核试题失败"));
        }
    }

    // 4. 分页查询自己的试题
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<Question>>> listQuestions(@Valid QuestionQueryDTO query) {
        try {
            String username = currentUsername();
            org.springframework.data.domain.Page<Question> questions = questionService.listTeacherQuestions(query, username);
            return ResponseEntity.ok(ApiResponse.success(questions));
        } catch (RuntimeException e) {
            log.error("查询试题错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("查询试题错误: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询试题失败"));
        }
    }

    // 5. 查询试题详情
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Question>> getQuestionDetail(@PathVariable String id) {
        try {
            Question question = questionService.getQuestionDetail(id);
            return ResponseEntity.ok(ApiResponse.success(question));
        } catch (RuntimeException e) {
            log.error("查询试题详情错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("查询试题详情错误: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询试题详情失败"));
        }
    }

    // 6. 测试自动判分
    @PostMapping("/{id}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<AutoGradeResult>> testGradeQuestion(
            @PathVariable String id,
            @RequestParam String studentAnswer) {
        try {
            AutoGradeResult result = questionService.gradeQuestion(id, studentAnswer);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            log.error("测试判分错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("测试判分错误: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("测试判分失败"));
        }
    }

    // 获取当前登录用户名（和AuthController完全一致）
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        return authentication.getName();
    }
}