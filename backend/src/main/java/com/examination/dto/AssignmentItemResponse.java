package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentItemResponse {
    private String assignmentId;
    private String assignmentName;
    private String paperId;
    private String paperName;
    private String subjectName;
    private Integer durationMinutes;
    private Integer totalScore;
    private LocalDateTime examStartTime;
    private LocalDateTime examEndTime;
    private String status;
    private Integer myAttempts;
    private Integer maxAttempts;
    private Double bestScore;
    private String sessionStatus;
}
