package com.examination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_assignment_student")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ExamAssignmentStudentId.class)
public class ExamAssignmentStudent {
    @Id
    @Column(name = "assignment_id", length = 12)
    private String assignmentId;

    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @CreationTimestamp
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
