package com.exam.dto;

import java.util.List;

public class PaperGenerationRequest {
    private String paperName;
    private String subjectId;
    private String creatorId;
    private int duration;
    private int totalScore;
    private String difficultyLevel;
    private List<QuestionTypeConfig> questionTypeConfigs;

    // 构造函数
    public PaperGenerationRequest() {}

    public PaperGenerationRequest(String paperName, String subjectId, String creatorId, 
                                int duration, int totalScore, String difficultyLevel, 
                                List<QuestionTypeConfig> questionTypeConfigs) {
        this.paperName = paperName;
        this.subjectId = subjectId;
        this.creatorId = creatorId;
        this.duration = duration;
        this.totalScore = totalScore;
        this.difficultyLevel = difficultyLevel;
        this.questionTypeConfigs = questionTypeConfigs;
    }

    // Getter和Setter方法
    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public List<QuestionTypeConfig> getQuestionTypeConfigs() {
        return questionTypeConfigs;
    }

    public void setQuestionTypeConfigs(List<QuestionTypeConfig> questionTypeConfigs) {
        this.questionTypeConfigs = questionTypeConfigs;
    }
} 