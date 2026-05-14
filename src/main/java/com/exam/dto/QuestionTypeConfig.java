package com.exam.dto;

public class QuestionTypeConfig {
    private String questionType;
    private int count;
    private int score;

    // 构造函数
    public QuestionTypeConfig() {}

    public QuestionTypeConfig(String questionType, int count, int score) {
        this.questionType = questionType;
        this.count = count;
        this.score = score;
    }

    // Getter和Setter方法
    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
} 