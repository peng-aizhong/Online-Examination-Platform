package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subject")
public class Subject {
    
    @Id
    @Column(name = "subject_id", length = 10)
    private String subjectId;
    
    @NotBlank(message = "科目名称不能为空")
    @Column(name = "subject_name", length = 100, nullable = false)
    private String subjectName;
    
    @Column(name = "subject_code", length = 20, unique = true)
    private String subjectCode;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubjectStatus status = SubjectStatus.active;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联题目
    @OneToMany(mappedBy = "subject", fetch = FetchType.EAGER)
    private List<Question> questions;
    
    // 关联试卷
    @OneToMany(mappedBy = "subject", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Paper> papers;
    
    // 关联学习资源
    @OneToMany(mappedBy = "subject", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<LearningResource> learningResources;
    
    // 科目状态枚举
    public enum SubjectStatus {
        active("启用"),
        inactive("禁用");
        
        private final String displayName;
        
        SubjectStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public Subject() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public SubjectStatus getStatus() {
        return status;
    }
    
    public void setStatus(SubjectStatus status) {
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
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public List<Paper> getPapers() {
        return papers;
    }
    
    public void setPapers(List<Paper> papers) {
        this.papers = papers;
    }
    
    public List<LearningResource> getLearningResources() {
        return learningResources;
    }
    
    public void setLearningResources(List<LearningResource> learningResources) {
        this.learningResources = learningResources;
    }
    
    // 获取题目数量
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }
    
    // 获取试卷数量
    public int getPaperCount() {
        return papers != null ? papers.size() : 0;
    }
    
    // 获取资源数量
    public int getResourceCount() {
        return learningResources != null ? learningResources.size() : 0;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Subject{" +
                "subjectId='" + subjectId + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", status=" + status +
                '}';
    }
} 