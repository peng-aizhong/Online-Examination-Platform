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
public class AssignmentResponse {
    private String assignmentId;
    private String assignmentName;
    private String paperId;
    private String paperName;
    private LocalDateTime examStartTime;
    private LocalDateTime examEndTime;
    private Integer durationMinutes;
    private Integer maxAttempts;
    private String status;
    private Integer assignedStudentCount;
    private LocalDateTime createdAt;
}
