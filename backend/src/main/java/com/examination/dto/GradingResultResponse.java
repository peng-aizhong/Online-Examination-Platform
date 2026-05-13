package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingResultResponse {
    private String sessionId;
    private String assignmentId;
    private String assignmentName;
    private String paperName;
    private String studentName;
    private String studentId;
    private Double score;
    private Integer totalScore;
    private Integer accuracy;
    private Integer attemptNumber;
    private LocalDateTime submittedAt;
    private List<GradedQuestion> questions;
    private Map<String, TypeBreakdown> typeBreakdowns;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradedQuestion {
        private String questionId;
        private String content;
        private String questionType;
        private String knowledgeTag;
        private String yourAnswer;
        private String correctAnswer;
        private String feedback;
        private Boolean correct;
        private Double earned;
        private Integer score;
        private String difficulty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypeBreakdown {
        private String type;
        private Integer total;
        private Integer correct;
        private Double earned;
        private Integer possible;
    }
}
