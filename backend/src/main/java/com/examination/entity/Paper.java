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
@Table(name = "paper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paper {
    @Id
    @Column(name = "paper_id", length = 10)
    private String paperId;

    @Column(name = "paper_name", length = 100)
    private String paperName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Column(length = 10)
    @Builder.Default
    private String status = "启用";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
