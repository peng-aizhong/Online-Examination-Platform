package com.examination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paper_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PaperQuestionId.class)
public class PaperQuestion {
    @Id
    @Column(name = "paper_id", length = 10)
    private String paperId;

    @Id
    @Column(name = "question_id", length = 10)
    private String questionId;

    private Integer score;
}
