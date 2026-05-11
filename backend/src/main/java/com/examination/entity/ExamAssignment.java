package com.examination.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_assignment", indexes = {
        @Index(name = "idx_paper_id", columnList = "paper_id"),
        @Index(name = "idx_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_exam_time", columnList = "exam_start_time, exam_end_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAssignment {
    @Id
    @Column(name = "assignment_id", length = 12)
    private String assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "assignment_name", nullable = false, length = 100)
    private String assignmentName;

    @Column(name = "exam_start_time", nullable = false)
    private LocalDateTime examStartTime;

    @Column(name = "exam_end_time", nullable = false)
    private LocalDateTime examEndTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 1;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.scheduled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AssignmentStatus {
        scheduled, active, finished, cancelled
    }
}
