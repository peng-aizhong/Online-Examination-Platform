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
public class WrongQuestionResponse {
    private List<WrongQuestionItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongQuestionItem {
        private String assignmentId;
        private String assignmentName;
        private String questionId;
        private String questionContent;
        private String correctAnswer;
        private String yourAnswer;
        private String knowledgeTag;
        private String feedback;
        private Integer score;
    }
}
