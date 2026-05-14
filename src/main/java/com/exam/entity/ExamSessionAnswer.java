package com.exam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "exam_session_answer")
public class ExamSessionAnswer {
    
    @EmbeddedId
    private ExamSessionAnswerId id;
    
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
    
    @Column(name = "score")
    private Double score = 0.0;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "answered_at")
    private java.time.LocalDateTime answeredAt;
    
    // 关联考试会话
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private ExamSession examSession;
    
    // 关联题目
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
    
    // 构造函数
    public ExamSessionAnswer() {}
    
    public ExamSessionAnswer(String sessionId, String questionId, String answerText) {
        this.id = new ExamSessionAnswerId(sessionId, questionId);
        this.answerText = answerText;
        this.answeredAt = java.time.LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public ExamSessionAnswerId getId() {
        return id;
    }
    
    public void setId(ExamSessionAnswerId id) {
        this.id = id;
    }
    
    public String getAnswerText() {
        return answerText;
    }
    
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public java.time.LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
    
    public void setAnsweredAt(java.time.LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
    
    public ExamSession getExamSession() {
        return examSession;
    }
    
    public void setExamSession(ExamSession examSession) {
        this.examSession = examSession;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    // 获取会话ID
    public String getSessionId() {
        return id != null ? id.getSessionId() : null;
    }
    
    // 获取题目ID
    public String getQuestionId() {
        return id != null ? id.getQuestionId() : null;
    }
    
    // 获取题目内容
    public String getQuestionContent() {
        return question != null ? question.getContent() : "";
    }
    
    // 获取题目类型
    public String getQuestionType() {
        return question != null ? question.getQuestionType() : "";
    }
    
    // 获取题目类型显示名称
    public String getQuestionTypeDisplayName() {
        return question != null ? question.getQuestionTypeDisplayName() : "";
    }
    
    @Override
    public String toString() {
        return "ExamSessionAnswer{" +
                "sessionId='" + getSessionId() + '\'' +
                ", questionId='" + getQuestionId() + '\'' +
                ", score=" + score +
                '}';
    }
    
    // 复合主键类
    @Embeddable
    public static class ExamSessionAnswerId implements java.io.Serializable {
        
        @Column(name = "session_id", length = 12, nullable = false)
        private String sessionId;
        
        @Column(name = "question_id", length = 20, nullable = false)
        private String questionId;
        
        public ExamSessionAnswerId() {}
        
        public ExamSessionAnswerId(String sessionId, String questionId) {
            this.sessionId = sessionId;
            this.questionId = questionId;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
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
            
            ExamSessionAnswerId that = (ExamSessionAnswerId) o;
            
            if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;
            return questionId != null ? questionId.equals(that.questionId) : that.questionId == null;
        }
        
        @Override
        public int hashCode() {
            int result = sessionId != null ? sessionId.hashCode() : 0;
            result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
            return result;
        }
    }
} 