package com.examination.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitExamRequest {
    private String sessionId;
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private String questionId;
        private String answer;
    }
}
