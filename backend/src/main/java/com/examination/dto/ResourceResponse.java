package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {
    private String id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String filePath;
    private String coverPath;
    private Long fileSize;
    private String externalUrl;
    private LocalDateTime uploadTime;
    private String uploaderId;
    private String uploaderName;
    private Integer downloadCount;
    private Integer viewCount;
    private String status;
    private String tags;
    private String subjectId;
    private String subjectName;
}
