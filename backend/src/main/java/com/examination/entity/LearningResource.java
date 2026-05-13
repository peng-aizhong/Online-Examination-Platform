package com.examination.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningResource {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ResourceType type;

    @Column(length = 100)
    private String category;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "cover_path", length = 500)
    private String coverPath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @CreationTimestamp
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private ResourceStatus status = ResourceStatus.ACTIVE;

    @Column(length = 1000)
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    public enum ResourceType {
        DOCUMENT, VIDEO, LINK, IMAGE
    }

    public enum ResourceStatus {
        ACTIVE, INACTIVE
    }
}
