package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperRequest {
    private String paperName;
    private String subjectId;
    private Integer duration;
    private Integer totalScore;
    private String difficultyLevel;
    private List<QuestionItem> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionItem {
        private String questionId;
        private Integer score;
    }
}
