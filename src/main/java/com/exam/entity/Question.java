package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "question")
public class Question {
    
    @Id
    @Column(name = "id", length = 10)
    private String id;
    
    @NotBlank(message = "科目ID不能为空")
    @Column(name = "subject_id", length = 10, nullable = false)
    private String subjectId;
    
    @NotBlank(message = "题目内容不能为空")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @NotBlank(message = "题目类型不能为空")
    @Column(name = "question_type", length = 20, nullable = false)
    private String questionType; // C, F, R, S, P
    
    @Column(name = "option_a", columnDefinition = "TEXT")
    private String optionA; // 选择题选项A
    
    @Column(name = "option_b", columnDefinition = "TEXT")
    private String optionB; // 选择题选项B
    
    @Column(name = "option_c", columnDefinition = "TEXT")
    private String optionC; // 选择题选项C
    
    @Column(name = "option_d", columnDefinition = "TEXT")
    private String optionD; // 选择题选项D
    
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;
    
    @NotBlank(message = "难度不能为空")
    @Column(name = "difficulty", length = 10, nullable = false)
    private String difficulty; // easy, medium, hard
    
    @Column(name = "knowledge_tag", length = 100)
    private String knowledgeTag;
    
    @Column(name = "status", length = 10)
    private String status = "启用";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联科目
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    @JsonIgnore
    private Subject subject;
    
    // 构造函数
    public Question() {
        // 不设置时间，让数据库自动处理
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public String getOptionA() {
        return optionA;
    }
    
    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }
    
    public String getOptionB() {
        return optionB;
    }
    
    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }
    
    public String getOptionC() {
        return optionC;
    }
    
    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }
    
    public String getOptionD() {
        return optionD;
    }
    
    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }
    
    // 获取所有选项的数组形式（用于兼容现有代码）
    public String[] getOptionsArray() {
        if ("C".equals(questionType)) {
            return new String[]{optionA, optionB, optionC, optionD};
        }
        return new String[0];
    }
    
    // 获取非空选项的数组形式
    public String[] getNonEmptyOptionsArray() {
        if ("C".equals(questionType)) {
            java.util.List<String> options = new java.util.ArrayList<>();
            if (optionA != null && !optionA.trim().isEmpty()) options.add(optionA);
            if (optionB != null && !optionB.trim().isEmpty()) options.add(optionB);
            if (optionC != null && !optionC.trim().isEmpty()) options.add(optionC);
            if (optionD != null && !optionD.trim().isEmpty()) options.add(optionD);
            return options.toArray(new String[0]);
        }
        return new String[0];
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getKnowledgeTag() {
        return knowledgeTag;
    }
    
    public void setKnowledgeTag(String knowledgeTag) {
        this.knowledgeTag = knowledgeTag;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    // 获取题型显示名称
    public String getQuestionTypeDisplayName() {
        switch (questionType) {
            case "C": return "选择题";
            case "F": return "填空题";
            case "R": return "程序运行结果题";
            case "S": return "简答题";
            case "P": return "编程题";
            default: return "未知题型";
        }
    }
    
    // 获取难度显示名称
    public String getDifficultyDisplayName() {
        switch (difficulty) {
            case "easy": return "简单";
            case "medium": return "中等";
            case "hard": return "困难";
            default: return "未知";
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", content='" + content + '\'' +
                ", questionType='" + questionType + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
} 