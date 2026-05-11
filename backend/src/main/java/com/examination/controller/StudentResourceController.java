package com.examination.controller;

import com.examination.common.ApiResponse;
import com.examination.dto.ResourceResponse;
import com.examination.entity.LearningResource;
import com.examination.service.LearningResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class StudentResourceController {
    @Autowired
    private LearningResourceService learningResourceService;

    @GetMapping("/resources")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> listResources(
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String type) {
        try {
            currentUsername();
            return ResponseEntity.ok(ApiResponse.success(learningResourceService.listResourcesForStudent(subjectId, type)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("List resources error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取资源列表失败"));
        }
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResource(@PathVariable String id) {
        try {
            currentUsername();
            return ResponseEntity.ok(ApiResponse.success(learningResourceService.getResource(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Get resource error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取资源详情失败"));
        }
    }

    @GetMapping("/resources/{id}/download")
    public ResponseEntity<?> downloadResource(@PathVariable String id) {
        try {
            currentUsername();
            LearningResource resource = learningResourceService.getResourceEntity(id);

            if (resource.getFilePath() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "该资源没有可下载的文件"));
            }

            Path filePath = Paths.get(resource.getFilePath());
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "文件不存在"));
            }

            learningResourceService.incrementDownloadCount(id);

            String encodedFilename = URLEncoder.encode(
                    resource.getTitle() + getExtension(resource.getFilePath()),
                    StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new FileSystemResource(file));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Download resource error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("下载资源失败"));
        }
    }

    private String getExtension(String filePath) {
        if (filePath == null || !filePath.contains(".")) return "";
        return filePath.substring(filePath.lastIndexOf("."));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        return authentication.getName();
    }
}
