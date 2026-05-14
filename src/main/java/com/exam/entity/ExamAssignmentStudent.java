package com.exam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_assignment_student")
public class ExamAssignmentStudent {
    
    @EmbeddedId
    private ExamAssignmentStudentId id;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    // 关联考试分配
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private ExamAssignment examAssignment;
    
    // 关联学生
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    // 构造函数
    public ExamAssignmentStudent() {
        this.assignedAt = LocalDateTime.now();
    }
    
    public ExamAssignmentStudent(String assignmentId, String studentId) {
        this.id = new ExamAssignmentStudentId(assignmentId, studentId);
        this.assignedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public ExamAssignmentStudentId getId() {
        return id;
    }
    
    public void setId(ExamAssignmentStudentId id) {
        this.id = id;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public ExamAssignment getExamAssignment() {
        return examAssignment;
    }
    
    public void setExamAssignment(ExamAssignment examAssignment) {
        this.examAssignment = examAssignment;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
    }
    
    // 获取分配ID
    public String getAssignmentId() {
        return id != null ? id.getAssignmentId() : null;
    }
    
    // 获取学生ID
    public String getStudentId() {
        return id != null ? id.getStudentId() : null;
    }
    
    @Override
    public String toString() {
        return "ExamAssignmentStudent{" +
                "assignmentId='" + getAssignmentId() + '\'' +
                ", studentId='" + getStudentId() + '\'' +
                ", assignedAt=" + assignedAt +
                '}';
    }
    
    // 复合主键类
    @Embeddable
    public static class ExamAssignmentStudentId implements java.io.Serializable {
        
        @Column(name = "assignment_id", length = 12)
        private String assignmentId;
        
        @Column(name = "student_id", length = 20)
        private String studentId;
        
        public ExamAssignmentStudentId() {}
        
        public ExamAssignmentStudentId(String assignmentId, String studentId) {
            this.assignmentId = assignmentId;
            this.studentId = studentId;
        }
        
        public String getAssignmentId() {
            return assignmentId;
        }
        
        public void setAssignmentId(String assignmentId) {
            this.assignmentId = assignmentId;
        }
        
        public String getStudentId() {
            return studentId;
        }
        
        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            
            ExamAssignmentStudentId that = (ExamAssignmentStudentId) o;
            
            if (assignmentId != null ? !assignmentId.equals(that.assignmentId) : that.assignmentId != null) return false;
            return studentId != null ? studentId.equals(that.studentId) : that.studentId == null;
        }
        
        @Override
        public int hashCode() {
            int result = assignmentId != null ? assignmentId.hashCode() : 0;
            result = 31 * result + (studentId != null ? studentId.hashCode() : 0);
            return result;
        }
    }
}
