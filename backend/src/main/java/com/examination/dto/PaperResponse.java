package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperResponse {
    private String paperId;
    private String paperName;
    private String subjectId;
    private String subjectName;
    private String creatorId;
    private String creatorName;
    private Integer duration;
    private Integer totalScore;
    private String difficultyLevel;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionItem> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionItem {
        private String questionId;
        private String content;
        private String questionType;
        private String difficulty;
        private String knowledgeTag;
        private Integer score;
    }
}
