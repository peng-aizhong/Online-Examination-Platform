package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "paper")
public class Paper {
    
    @Id
    @Column(name = "paper_id", length = 10)
    private String paperId;
    
    @Column(name = "paper_name", length = 100)
    private String paperName;
    
    @NotBlank(message = "科目ID不能为空")
    @Column(name = "subject_id", length = 10, nullable = false)
    private String subjectId;
    
    @Column(name = "creator_id", length = 10)
    private String creatorId;
    
    @NotNull(message = "考试时长不能为空")
    @Column(name = "duration", nullable = false)
    private Integer duration; // 考试时长（分钟）
    
    @NotNull(message = "总分不能为空")
    @Column(name = "total_score", nullable = false)
    private Integer totalScore;
    
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;
    
    @Column(name = "status", length = 10)
    private String status = "启用"; // 默认启用状态
    
    @Column(name = "passing_score")
    private Integer passingScore; // 及格分数
    
    @Column(name = "question_count")
    private Integer questionCount = 0; // 题目数量
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 试卷描述
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联科目
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    @JsonIgnore
    private Subject subject;
    
    // 关联创建者
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    @JsonIgnore
    private User creator;
    
    // 试卷题目关联
    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PaperQuestion> paperQuestions;
    
    // 构造函数
    public Paper() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getPaperId() {
        return paperId;
    }
    
    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }
    
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
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Integer getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
    
        public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getPassingScore() {
        return passingScore;
    }
    
    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public List<PaperQuestion> getPaperQuestions() {
        return paperQuestions;
    }
    
    public void setPaperQuestions(List<PaperQuestion> paperQuestions) {
        this.paperQuestions = paperQuestions;
    }
    
    // 获取难度显示名称
    public String getDifficultyLevelDisplayName() {
        if (difficultyLevel == null) return "未设置";
        if ("easy".equals(difficultyLevel)) return "简单";
        if ("medium".equals(difficultyLevel)) return "中等";
        if ("hard".equals(difficultyLevel)) return "困难";
        return "未知";
    }
    
    // 获取题目数量
    public int getQuestionCount() {
        return paperQuestions != null ? paperQuestions.size() : 0;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Paper{" +
                "paperId='" + paperId + '\'' +
                ", paperName='" + paperName + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", duration=" + duration +
                ", totalScore=" + totalScore +
                ", difficultyLevel=" + difficultyLevel +
                '}';
    }
} 