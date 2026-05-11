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
public class ScoreReportResponse {
    private String sessionId;
    private String assignmentId;
    private String assignmentName;
    private String paperName;
    private Double score;
    private Integer totalScore;
    private Integer accuracy;
    private Long rank;
    private Long totalParticipants;
    private Integer attemptNumber;
    private Boolean isBestScore;
    private LocalDateTime submittedAt;

    private Map<String, TypeScore> typeScores;
    private Map<String, KnowledgePointScore> knowledgePointScores;
    private List<QuestionDetail> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypeScore {
        private String type;
        private Double earned;
        private Integer total;
        private Integer count;
        private Integer correctCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KnowledgePointScore {
        private String knowledgeTag;
        private Double earned;
        private Integer total;
        private Integer count;
        private Integer correctCount;
        private Integer accuracy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDetail {
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
}
