package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "paper_question")
public class PaperQuestion {
    
    @EmbeddedId
    private PaperQuestionId id;
    
    @NotNull(message = "分值不能为空")
    @Column(name = "score", nullable = false)
    private Integer score;
    
    // 关联试卷
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    @JsonIgnore
    private Paper paper;
    
    // 关联题目
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    @JsonIgnore
    private Question question;
    
    // 构造函数
    public PaperQuestion() {}
    
    public PaperQuestion(String paperId, String questionId, Integer score) {
        this.id = new PaperQuestionId(paperId, questionId);
        this.score = score;
    }
    
    // Getter和Setter方法
    public PaperQuestionId getId() {
        return id;
    }
    
    public void setId(PaperQuestionId id) {
        this.id = id;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public Paper getPaper() {
        return paper;
    }
    
    public void setPaper(Paper paper) {
        this.paper = paper;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    // 获取试卷ID
    public String getPaperId() {
        return id != null ? id.getPaperId() : null;
    }
    
    // 获取题目ID
    public String getQuestionId() {
        return id != null ? id.getQuestionId() : null;
    }
    
    @Override
    public String toString() {
        return "PaperQuestion{" +
                "paperId='" + getPaperId() + '\'' +
                ", questionId='" + getQuestionId() + '\'' +
                ", score=" + score +
                '}';
    }
    
    // 复合主键类
    @Embeddable
    public static class PaperQuestionId implements java.io.Serializable {
        
        @Column(name = "paper_id", length = 10)
        private String paperId;
        
        @Column(name = "question_id", length = 10)
        private String questionId;
        
        public PaperQuestionId() {}
        
        public PaperQuestionId(String paperId, String questionId) {
            this.paperId = paperId;
            this.questionId = questionId;
        }
        
        public String getPaperId() {
            return paperId;
        }
        
        public void setPaperId(String paperId) {
            this.paperId = paperId;
        }
        
        public String getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            
            PaperQuestionId that = (PaperQuestionId) o;
            
            if (paperId != null ? !paperId.equals(that.paperId) : that.paperId != null) return false;
            return questionId != null ? questionId.equals(that.questionId) : that.questionId == null;
        }
        
        @Override
        public int hashCode() {
            int result = paperId != null ? paperId.hashCode() : 0;
            result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
            return result;
        }
    }
} 