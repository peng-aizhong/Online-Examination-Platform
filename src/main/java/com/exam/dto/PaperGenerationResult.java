package com.exam.dto;

import com.exam.entity.Paper;

public class PaperGenerationResult {
    private boolean success;
    private String message;
    private Paper paper;
    private String answerKey;

    // 构造函数
    public PaperGenerationResult() {}

    public PaperGenerationResult(boolean success, String message, Paper paper, String answerKey) {
        this.success = success;
        this.message = message;
        this.paper = paper;
        this.answerKey = answerKey;
    }

    // 静态工厂方法
    public static PaperGenerationResult success(Paper paper, String answerKey) {
        return new PaperGenerationResult(true, "试卷生成成功", paper, answerKey);
    }

    public static PaperGenerationResult failure(String message) {
        return new PaperGenerationResult(false, message, null, null);
    }

    // Getter和Setter方法
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    public String getAnswerKey() {
        return answerKey;
    }

    public void setAnswerKey(String answerKey) {
        this.answerKey = answerKey;
    }
} 