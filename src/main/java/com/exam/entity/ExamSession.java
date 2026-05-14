package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exam_session")
public class ExamSession {
    
    @Id
    @Column(name = "session_id", length = 12)
    private String sessionId;
    
    @Column(name = "assignment_id", length = 12, nullable = false)
    private String assignmentId;
    
    @Column(name = "paper_id", length = 10, nullable = false)
    private String paperId;
    
    @Column(name = "student_id", length = 20, nullable = false)
    private String studentId;
    
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @NotNull(message = "考试时长不能为空")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private SessionStatus status = SessionStatus.not_started;
    
    @Column(name = "objective_score")
    private Double objectiveScore = 0.0;
    
    @Column(name = "subjective_score")
    private Double subjectiveScore = 0.0;
    
    @Column(name = "total_score")
    private Double totalScore = 0.0;
    
    @Column(name = "is_best_score")
    private Boolean isBestScore = false;
    
    @Column(name = "auto_submitted")
    private Boolean autoSubmitted = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联考试分配
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    @JsonIgnore
    private ExamAssignment examAssignment;
    
    // 关联试卷
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    @JsonIgnore
    private Paper paper;
    
    // 关联学生
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnore
    private User student;
    
    // 考试答案
    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<ExamSessionAnswer> answers;
    
    // 枚举：考试会话状态
    public enum SessionStatus {
        not_started("未开始"),
        ongoing("进行中"),
        submitted("已提交"),
        graded("已阅卷"),
        timeout("超时"),
        cancelled("已取消");
        
        private final String displayName;
        
        SessionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public ExamSession() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public String getPaperId() {
        return paperId;
    }
    
    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public Integer getAttemptNumber() {
        return attemptNumber;
    }
    
    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public SessionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status;
    }
    
    public Double getObjectiveScore() {
        return objectiveScore;
    }
    
    public void setObjectiveScore(Double objectiveScore) {
        this.objectiveScore = objectiveScore;
    }
    
    public Double getSubjectiveScore() {
        return subjectiveScore;
    }
    
    public void setSubjectiveScore(Double subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }
    
    public Double getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }
    
    public Boolean getIsBestScore() {
        return isBestScore;
    }
    
    public void setIsBestScore(Boolean isBestScore) {
        this.isBestScore = isBestScore;
    }
    
    public Boolean getAutoSubmitted() {
        return autoSubmitted;
    }
    
    public void setAutoSubmitted(Boolean autoSubmitted) {
        this.autoSubmitted = autoSubmitted;
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
    
    public ExamAssignment getExamAssignment() {
        return examAssignment;
    }
    
    public void setExamAssignment(ExamAssignment examAssignment) {
        this.examAssignment = examAssignment;
    }
    
    public Paper getPaper() {
        return paper;
    }
    
    public void setPaper(Paper paper) {
        this.paper = paper;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
    }
    
    public List<ExamSessionAnswer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<ExamSessionAnswer> answers) {
        this.answers = answers;
    }
    
    // 获取状态显示名称
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "未知状态";
    }
    
    // 计算总分
    public void calculateTotalScore() {
        double objective = objectiveScore != null ? objectiveScore : 0.0;
        double subjective = subjectiveScore != null ? subjectiveScore : 0.0;
        this.totalScore = objective + subjective;
    }
    
    // 获取考试名称
    public String getExamName() {
        return paper != null ? paper.getPaperName() : "未知考试";
    }
    
    // 获取学生姓名
    public String getStudentName() {
        return student != null ? student.getUsername() : "未知学生";
    }
    
    @Override
    public String toString() {
        return "ExamSession{" +
                "sessionId='" + sessionId + '\'' +
                ", paperId='" + paperId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", status='" + status + '\'' +
                ", totalScore=" + totalScore +
                '}';
    }
} 