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
public class SessionResultResponse {
    private String sessionId;
    private String assignmentId;
    private String assignmentName;
    private String paperName;
    private Double score;
    private Integer totalScore;
    private Integer accuracy;
    private Integer attemptNumber;
    private Boolean isBestScore;
    private LocalDateTime submittedAt;
}
