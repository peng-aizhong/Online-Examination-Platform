package com.examination.repository;

import com.examination.entity.PaperQuestion;
import com.examination.entity.PaperQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, PaperQuestionId> {
    List<PaperQuestion> findByPaperId(String paperId);

    @Query("SELECT pq FROM PaperQuestion pq JOIN FETCH Question q ON pq.questionId = q.id WHERE pq.paperId = :paperId")
    List<PaperQuestion> findByPaperIdWithQuestion(@Param("paperId") String paperId);

    void deleteByPaperId(String paperId);
}
