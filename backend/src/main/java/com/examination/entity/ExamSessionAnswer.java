package com.examination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_session_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ExamSessionAnswerId.class)
public class ExamSessionAnswer {
    @Id
    @Column(name = "session_id", length = 12)
    private String sessionId;

    @Id
    @Column(name = "question_id", length = 10)
    private String questionId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column
    @Builder.Default
    private Double score = 0.0;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
