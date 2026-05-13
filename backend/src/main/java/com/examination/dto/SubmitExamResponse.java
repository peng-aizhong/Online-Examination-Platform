package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExamResponse {
    private String sessionId;
    private Double score;
    private Integer totalScore;
    private Integer accuracy;
    private List<MistakeQuestion> mistakes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MistakeQuestion {
        private String questionId;
        private String content;
        private String correctAnswer;
        private String yourAnswer;
        private Integer score;
        private String knowledgeTag;
        private String feedback;
    }
}
