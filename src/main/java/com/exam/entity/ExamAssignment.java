package com.exam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exam_assignment")
public class ExamAssignment {
    
    @Id
    @Column(name = "assignment_id", length = 12)
    private String assignmentId;
    
    @Column(name = "paper_id", length = 10, nullable = false)
    private String paperId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    private Paper paper;
    
    @Column(name = "teacher_id", length = 20, nullable = false)
    private String teacherId;
    
    @NotBlank(message = "考试名称不能为空")
    @Column(name = "assignment_name", length = 100, nullable = false)
    private String assignmentName;
    
    @NotNull(message = "考试开始时间不能为空")
    @Column(name = "exam_start_time", nullable = false)
    private LocalDateTime examStartTime;
    
    @NotNull(message = "考试结束时间不能为空")
    @Column(name = "exam_end_time", nullable = false)
    private LocalDateTime examEndTime;
    
    @NotNull(message = "考试时长不能为空")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "max_attempts")
    private Integer maxAttempts = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssignmentStatus status = AssignmentStatus.scheduled;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    
    // 关联教师
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private User teacher;
    
    // 关联分配的学生
    @OneToMany(mappedBy = "examAssignment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ExamAssignmentStudent> assignedStudents;
    
    // 关联考试会话
    @OneToMany(mappedBy = "examAssignment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ExamSession> examSessions;
    
    // 枚举：考试分配状态
    public enum AssignmentStatus {
        scheduled("已安排"),
        active("进行中"),
        finished("已结束"),
        cancelled("已取消");
        
        private final String displayName;
        
        AssignmentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public ExamAssignment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
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
    
    public Paper getPaper() {
        return paper;
    }
    
    public void setPaper(Paper paper) {
        this.paper = paper;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getAssignmentName() {
        return assignmentName;
    }
    
    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }
    
    public LocalDateTime getExamStartTime() {
        return examStartTime;
    }
    
    public void setExamStartTime(LocalDateTime examStartTime) {
        this.examStartTime = examStartTime;
    }
    
    public LocalDateTime getExamEndTime() {
        return examEndTime;
    }
    
    public void setExamEndTime(LocalDateTime examEndTime) {
        this.examEndTime = examEndTime;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public Integer getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public AssignmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssignmentStatus status) {
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
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
    
    public List<ExamAssignmentStudent> getAssignedStudents() {
        return assignedStudents;
    }
    
    public void setAssignedStudents(List<ExamAssignmentStudent> assignedStudents) {
        this.assignedStudents = assignedStudents;
    }
    
    public List<ExamSession> getExamSessions() {
        return examSessions;
    }
    
    public void setExamSessions(List<ExamSession> examSessions) {
        this.examSessions = examSessions;
    }
    
    // 获取状态显示名称
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "未知状态";
    }
    
    // 检查考试是否正在进行
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(examStartTime) && now.isBefore(examEndTime);
    }
    
    // 检查考试是否已结束
    public boolean isFinished() {
        return LocalDateTime.now().isAfter(examEndTime);
    }
    
    // 检查考试是否已开始
    public boolean isStarted() {
        return LocalDateTime.now().isAfter(examStartTime);
    }
    
    @Override
    public String toString() {
        return "ExamAssignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", paperId='" + paperId + '\'' +
                ", assignmentName='" + assignmentName + '\'' +
                ", status=" + status +
                '}';
    }
}
