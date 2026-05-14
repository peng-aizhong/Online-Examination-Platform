package com.exam.repository;

import com.exam.entity.PaperQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, PaperQuestion.PaperQuestionId> {

    // 根据试卷ID查找试卷题目
    @Query("SELECT pq FROM PaperQuestion pq WHERE pq.id.paperId = :paperId")
    List<PaperQuestion> findByPaperId(@Param("paperId") String paperId);

    // 根据题目ID查找试卷题目
    @Query("SELECT pq FROM PaperQuestion pq WHERE pq.id.questionId = :questionId")
    List<PaperQuestion> findByQuestionId(@Param("questionId") String questionId);

    // 根据试卷ID和题目ID查找试卷题目
    @Query("SELECT pq FROM PaperQuestion pq WHERE pq.id.paperId = :paperId AND pq.id.questionId = :questionId")
    PaperQuestion findByPaperIdAndQuestionId(@Param("paperId") String paperId, @Param("questionId") String questionId);

    // 根据试卷ID删除试卷题目
    @Modifying
    @Query("DELETE FROM PaperQuestion pq WHERE pq.id.paperId = :paperId")
    void deleteByPaperId(@Param("paperId") String paperId);

    // 根据题目ID删除试卷题目
    @Modifying
    @Query("DELETE FROM PaperQuestion pq WHERE pq.id.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") String questionId);

    // 根据试卷ID统计题目数量
    @Query("SELECT COUNT(pq) FROM PaperQuestion pq WHERE pq.id.paperId = :paperId")
    long countByPaperId(@Param("paperId") String paperId);

    // 根据试卷ID计算总分
    @Query("SELECT SUM(pq.score) FROM PaperQuestion pq WHERE pq.id.paperId = :paperId")
    Integer sumScoreByPaperId(@Param("paperId") String paperId);
} 