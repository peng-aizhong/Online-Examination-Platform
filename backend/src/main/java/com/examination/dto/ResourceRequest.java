package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequest {
    private String title;
    private String description;
    private String type;
    private String category;
    private String tags;
    private String subjectId;
    private String externalUrl;
}
