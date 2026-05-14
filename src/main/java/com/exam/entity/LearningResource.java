package com.exam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_resource")
public class LearningResource {
    
    @Id
    @Column(name = "id", length = 50)
    private String id;
    
    @NotBlank(message = "资源标题不能为空")
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "资源类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ResourceType type;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "cover_path", length = 500)
    private String coverPath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "external_url", length = 500)
    private String externalUrl;
    
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    @Column(name = "uploader_id", length = 10)
    private String uploaderId;
    
    @Column(name = "download_count")
    private Integer downloadCount = 0;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResourceStatus status = ResourceStatus.ACTIVE;
    
    @Column(name = "tags", length = 1000)
    private String tags;
    
    @Column(name = "subject_id", length = 10)
    private String subjectId;
    
    // 关联上传者
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id", insertable = false, updatable = false)
    private User uploader;
    
    // 关联科目
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;
    
    // 资源类型枚举
    public enum ResourceType {
        DOCUMENT("文档"),
        VIDEO("视频"),
        LINK("链接"),
        IMAGE("图片");
        
        private final String displayName;
        
        ResourceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 资源状态枚举
    public enum ResourceStatus {
        ACTIVE("启用"),
        INACTIVE("禁用");
        
        private final String displayName;
        
        ResourceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public LearningResource() {
        this.uploadTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ResourceType getType() {
        return type;
    }
    
    public void setType(ResourceType type) {
        this.type = type;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getCoverPath() {
        return coverPath;
    }
    
    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getExternalUrl() {
        return externalUrl;
    }
    
    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public String getUploaderId() {
        return uploaderId;
    }
    
    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }
    
    public Integer getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public ResourceStatus getStatus() {
        return status;
    }
    
    public void setStatus(ResourceStatus status) {
        this.status = status;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    
    public User getUploader() {
        return uploader;
    }
    
    public void setUploader(User uploader) {
        this.uploader = uploader;
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    
    // 获取文件大小显示
    public String getFileSizeDisplay() {
        if (fileSize == null) return "未知";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }
    
    // 获取上传者姓名
    public String getUploaderName() {
        return uploader != null ? uploader.getUsername() : "未知";
    }
    
    // 获取科目名称
    public String getSubjectName() {
        return subject != null ? subject.getSubjectName() : "未知";
    }
    
    // 增加下载次数
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount != null ? this.downloadCount : 0) + 1;
    }
    
    // 增加查看次数
    public void incrementViewCount() {
        this.viewCount = (this.viewCount != null ? this.viewCount : 0) + 1;
    }
    
    @Override
    public String toString() {
        return "LearningResource{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
} 