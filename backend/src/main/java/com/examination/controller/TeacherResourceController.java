package com.examination.controller;

import com.examination.common.ApiResponse;
import com.examination.dto.*;
import com.examination.service.LearningResourceService;
import com.examination.service.PaperManagementService;
import com.examination.service.QuestionBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeacherResourceController {
    @Autowired
    private QuestionBankService questionBankService;
    @Autowired
    private PaperManagementService paperManagementService;
    @Autowired
    private LearningResourceService learningResourceService;

    // ==================== 题库管理 ====================

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> listQuestions(
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String keyword) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(questionBankService.listQuestions(username, subjectId, questionType, keyword)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List questions error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取题目列表失败"));
        }
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(@PathVariable String id) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(questionBankService.getQuestion(username, id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get question error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取题目详情失败"));
        }
    }

    @PostMapping("/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(@RequestBody QuestionRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(questionBankService.createQuestion(username, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Create question error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("创建题目失败"));
        }
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(@PathVariable String id, @RequestBody QuestionRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(questionBankService.updateQuestion(username, id, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Update question error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新题目失败"));
        }
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable String id) {
        try {
            String username = currentUsername();
            questionBankService.deleteQuestion(username, id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Delete question error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("删除题目失败"));
        }
    }

    // ==================== 试卷管理 ====================

    @GetMapping("/papers")
    public ResponseEntity<ApiResponse<List<PaperResponse>>> listPapers() {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(paperManagementService.listPapers(username)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List papers error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取试卷列表失败"));
        }
    }

    @GetMapping("/papers/{id}")
    public ResponseEntity<ApiResponse<PaperResponse>> getPaper(@PathVariable String id) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(paperManagementService.getPaper(username, id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get paper error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取试卷详情失败"));
        }
    }

    @PostMapping("/papers")
    public ResponseEntity<ApiResponse<PaperResponse>> createPaper(@RequestBody PaperRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(paperManagementService.createPaper(username, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Create paper error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("创建试卷失败"));
        }
    }

    @PutMapping("/papers/{id}")
    public ResponseEntity<ApiResponse<PaperResponse>> updatePaper(@PathVariable String id, @RequestBody PaperRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(paperManagementService.updatePaper(username, id, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Update paper error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新试卷失败"));
        }
    }

    @DeleteMapping("/papers/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaper(@PathVariable String id) {
        try {
            String username = currentUsername();
            paperManagementService.deletePaper(username, id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Delete paper error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("删除试卷失败"));
        }
    }

    @PostMapping("/papers/{paperId}/questions")
    public ResponseEntity<ApiResponse<Void>> addQuestionToPaper(
            @PathVariable String paperId,
            @RequestParam String questionId,
            @RequestParam(defaultValue = "0") Integer score) {
        try {
            String username = currentUsername();
            paperManagementService.addQuestionToPaper(username, paperId, questionId, score);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Add question to paper error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("添加题目失败"));
        }
    }

    @DeleteMapping("/papers/{paperId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> removeQuestionFromPaper(
            @PathVariable String paperId,
            @PathVariable String questionId) {
        try {
            String username = currentUsername();
            paperManagementService.removeQuestionFromPaper(username, paperId, questionId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Remove question from paper error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("移除题目失败"));
        }
    }

    // ==================== 学习资源管理 ====================

    @GetMapping("/resources")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> listResources(
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String type) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(learningResourceService.listResourcesForTeacher(username, subjectId, type)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List resources error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取资源列表失败"));
        }
    }

    @PostMapping("/resources/upload")
    public ResponseEntity<ApiResponse<ResourceResponse>> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "subjectId", required = false) String subjectId,
            @RequestParam(value = "type", required = false) String type) {
        try {
            String username = currentUsername();
            ResourceRequest request = ResourceRequest.builder()
                    .title(title)
                    .description(description)
                    .category(category)
                    .tags(tags)
                    .subjectId(subjectId)
                    .type(type)
                    .build();
            return ResponseEntity.ok(ApiResponse.success(learningResourceService.uploadResource(username, file, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Upload resource error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("上传资源失败"));
        }
    }

    @PostMapping("/resources/link")
    public ResponseEntity<ApiResponse<ResourceResponse>> createLinkResource(@RequestBody ResourceRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(learningResourceService.createLinkResource(username, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Create link resource error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("创建链接资源失败"));
        }
    }

    @PutMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(@PathVariable String id, @RequestBody ResourceRequest request) {
        try {
            String username = currentUsername();
            return ResponseEntity.ok(ApiResponse.success(learningResourceService.updateResource(username, id, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Update resource error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("更新资源失败"));
        }
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable String id) {
        try {
            String username = currentUsername();
            learningResourceService.deleteResource(username, id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Delete resource error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("删除资源失败"));
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
