package com.examination.dto.question;

import lombok.Data;

@Data
public class AutoGradeResult {
    private String questionId;
    private String questionType;
    private String studentAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    private boolean needManualGrade;
    private Double score;
}