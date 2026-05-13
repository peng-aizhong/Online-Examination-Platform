package com.examination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDetailResponse {
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
    private List<QuestionItem> questions;
    private String sessionId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionItem {
        private String questionId;
        private String content;
        private String questionType;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private Integer score;
        private String difficulty;
        private String knowledgeTag;
    }
}
