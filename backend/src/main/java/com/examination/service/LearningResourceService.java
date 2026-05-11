package com.examination.service;

import com.examination.dto.ResourceRequest;
import com.examination.dto.ResourceResponse;
import com.examination.entity.LearningResource;
import com.examination.entity.LearningResource.ResourceStatus;
import com.examination.entity.LearningResource.ResourceType;
import com.examination.entity.Subject;
import com.examination.entity.User;
import com.examination.repository.LearningResourceRepository;
import com.examination.repository.SubjectRepository;
import com.examination.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class LearningResourceService {
    @Autowired
    private LearningResourceRepository resourceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubjectRepository subjectRepository;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public List<ResourceResponse> listResourcesForTeacher(String username, String subjectId, String type) {
        User teacher = getTeacherUser(username);
        List<LearningResource> resources = resourceRepository.findByUploader_UserIdOrderByUploadTimeDesc(teacher.getUserId());

        if (subjectId != null && !subjectId.isBlank()) {
            resources = resources.stream()
                    .filter(r -> r.getSubject() != null && subjectId.equals(r.getSubject().getSubjectId()))
                    .collect(Collectors.toList());
        }
        if (type != null && !type.isBlank()) {
            resources = resources.stream()
                    .filter(r -> type.equals(r.getType().name()))
                    .collect(Collectors.toList());
        }

        return resources.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ResourceResponse> listResourcesForStudent(String subjectId, String type) {
        List<LearningResource> resources = resourceRepository.findByStatusOrderByUploadTimeDesc(ResourceStatus.ACTIVE);

        if (subjectId != null && !subjectId.isBlank()) {
            resources = resources.stream()
                    .filter(r -> r.getSubject() != null && subjectId.equals(r.getSubject().getSubjectId()))
                    .collect(Collectors.toList());
        }
        if (type != null && !type.isBlank()) {
            resources = resources.stream()
                    .filter(r -> type.equals(r.getType().name()))
                    .collect(Collectors.toList());
        }

        return resources.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ResourceResponse getResource(String resourceId) {
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        resource.setViewCount(resource.getViewCount() + 1);
        resourceRepository.save(resource);
        return toResponse(resource);
    }

    public ResourceResponse uploadResource(String username, MultipartFile file, ResourceRequest request) {
        User teacher = getTeacherUser(username);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String storedFilename = UUID.randomUUID().toString() + ext;

        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(storedFilename);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        Subject subject = null;
        if (request.getSubjectId() != null && !request.getSubjectId().isBlank()) {
            subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        }

        ResourceType resourceType = resolveFileType(ext, request.getType());
        String id = "RES" + System.currentTimeMillis() % 1000000000;

        LearningResource resource = LearningResource.builder()
                .id(id)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(resourceType)
                .category(request.getCategory())
                .filePath(uploadDir + storedFilename)
                .fileSize(file.getSize())
                .uploader(teacher)
                .subject(subject)
                .tags(request.getTags())
                .build();

        resource = resourceRepository.save(resource);
        return toResponse(resource);
    }

    public ResourceResponse createLinkResource(String username, ResourceRequest request) {
        User teacher = getTeacherUser(username);

        Subject subject = null;
        if (request.getSubjectId() != null && !request.getSubjectId().isBlank()) {
            subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        }

        String id = "RES" + System.currentTimeMillis() % 1000000000;

        LearningResource resource = LearningResource.builder()
                .id(id)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(ResourceType.LINK)
                .category(request.getCategory())
                .externalUrl(request.getExternalUrl())
                .uploader(teacher)
                .subject(subject)
                .tags(request.getTags())
                .build();

        resource = resourceRepository.save(resource);
        return toResponse(resource);
    }

    public ResourceResponse updateResource(String username, String resourceId, ResourceRequest request) {
        getTeacherUser(username);
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));

        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCategory(request.getCategory());
        resource.setTags(request.getTags());

        if (request.getSubjectId() != null && !request.getSubjectId().isBlank()) {
            Subject subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
            resource.setSubject(subject);
        }
        if (request.getExternalUrl() != null) {
            resource.setExternalUrl(request.getExternalUrl());
        }

        resource = resourceRepository.save(resource);
        return toResponse(resource);
    }

    public void deleteResource(String username, String resourceId) {
        getTeacherUser(username);
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));

        if (resource.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(resource.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", resource.getFilePath(), e);
            }
        }

        resourceRepository.delete(resource);
    }

    public LearningResource getResourceEntity(String resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
    }

    public void incrementDownloadCount(String resourceId) {
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resourceRepository.save(resource);
    }

    private ResourceType resolveFileType(String ext, String typeHint) {
        if (typeHint != null && !typeHint.isBlank()) {
            try {
                return ResourceType.valueOf(typeHint.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        String lower = ext.toLowerCase();
        if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt")) {
            return ResourceType.DOCUMENT;
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".mkv")) {
            return ResourceType.VIDEO;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".bmp")) {
            return ResourceType.IMAGE;
        }
        return ResourceType.DOCUMENT;
    }

    private ResourceResponse toResponse(LearningResource r) {
        return ResourceResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .type(r.getType() != null ? r.getType().name() : "")
                .category(r.getCategory())
                .filePath(r.getFilePath())
                .coverPath(r.getCoverPath())
                .fileSize(r.getFileSize())
                .externalUrl(r.getExternalUrl())
                .uploadTime(r.getUploadTime())
                .uploaderId(r.getUploader() != null ? r.getUploader().getUserId() : "")
                .uploaderName(r.getUploader() != null ? r.getUploader().getUsername() : "")
                .downloadCount(r.getDownloadCount())
                .viewCount(r.getViewCount())
                .status(r.getStatus() != null ? r.getStatus().name() : "")
                .tags(r.getTags())
                .subjectId(r.getSubject() != null ? r.getSubject().getSubjectId() : "")
                .subjectName(r.getSubject() != null ? r.getSubject().getSubjectName() : "")
                .build();
    }

    private User getTeacherUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getRole() != User.UserRole.teacher && user.getRole() != User.UserRole.admin) {
            throw new RuntimeException("仅教师可访问该功能");
        }
        return user;
    }
}

    /**
     * 获取资源统计信息 [Copilot辅助]
     */
    public Map<String, Object> getResourceStatistics(String username) {
        Map<String, Object> stats = new HashMap<>();
        List<LearningResource> resources = listResourcesForTeacher(username, null, null);
        stats.put("totalResources", resources.size());
        stats.put("totalViews", resources.stream().mapToInt(r -> r.getViewCount() != null ? r.getViewCount() : 0).sum());
        stats.put("totalDownloads", resources.stream().mapToInt(r -> r.getDownloadCount() != null ? r.getDownloadCount() : 0).sum());
        return stats;
    }
