package com.exam.repository;

import com.exam.entity.ExamSessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionAnswerRepository extends JpaRepository<ExamSessionAnswer, ExamSessionAnswer.ExamSessionAnswerId> {
    
    /**
     * 根据考试会话ID查找所有答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId")
    List<ExamSessionAnswer> findBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据题目ID查找所有答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.questionId = :questionId")
    List<ExamSessionAnswer> findByQuestionId(@Param("questionId") String questionId);
    
    /**
     * 根据考试会话ID和题目ID查找答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId AND ea.id.questionId = :questionId")
    ExamSessionAnswer findBySessionIdAndQuestionId(@Param("sessionId") String sessionId, @Param("questionId") String questionId);
    
    /**
     * 根据考试会话ID删除所有答案
     */
    @Modifying
    @Query("DELETE FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据题目ID删除所有答案
     */
    @Modifying
    @Query("DELETE FROM ExamSessionAnswer ea WHERE ea.id.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") String questionId);
    
    /**
     * 根据考试会话ID统计答案数量
     */
    @Query("SELECT COUNT(ea) FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据题目ID统计答案数量
     */
    @Query("SELECT COUNT(ea) FROM ExamSessionAnswer ea WHERE ea.id.questionId = :questionId")
    long countByQuestionId(@Param("questionId") String questionId);
    
    /**
     * 根据考试会话ID获取题目ID列表
     */
    @Query("SELECT ea.id.questionId FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId")
    List<String> findQuestionIdsBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取答案和分值
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId ORDER BY ea.id.questionId")
    List<ExamSessionAnswer> findAnswersWithScoresBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID计算总分
     */
    @Query("SELECT SUM(ea.score) FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId")
    Double calculateTotalScoreBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID计算客观题总分
     */
    @Query("SELECT SUM(ea.score) FROM ExamSessionAnswer ea JOIN Question q ON ea.id.questionId = q.id WHERE ea.id.sessionId = :sessionId AND (q.questionType = 'C001' OR q.questionType = 'F001')")
    Double calculateObjectiveScoreBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID计算主观题总分
     */
    @Query("SELECT SUM(ea.score) FROM ExamSessionAnswer ea JOIN Question q ON ea.id.questionId = q.id WHERE ea.id.sessionId = :sessionId AND (q.questionType = 'R001' OR q.questionType = 'S001' OR q.questionType = 'P001')")
    Double calculateSubjectiveScoreBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取各题型的答案数量
     */
    @Query("SELECT q.questionType, COUNT(ea) FROM ExamSessionAnswer ea JOIN Question q ON ea.id.questionId = q.id WHERE ea.id.sessionId = :sessionId GROUP BY q.questionType")
    List<Object[]> countAnswersByTypeInSession(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取各题型的得分情况
     */
    @Query("SELECT q.questionType, AVG(ea.score), MAX(ea.score), MIN(ea.score) FROM ExamSessionAnswer ea JOIN Question q ON ea.id.questionId = q.id WHERE ea.id.sessionId = :sessionId GROUP BY q.questionType")
    List<Object[]> getScoreStatisticsByTypeInSession(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取各知识点的得分情况
     */
    @Query("SELECT q.knowledgeTag, AVG(ea.score), MAX(ea.score), MIN(ea.score) FROM ExamSessionAnswer ea JOIN Question q ON ea.id.questionId = q.id WHERE ea.id.sessionId = :sessionId AND q.knowledgeTag IS NOT NULL GROUP BY q.knowledgeTag")
    List<Object[]> getScoreStatisticsByKnowledgeTagInSession(@Param("sessionId") String sessionId);
    
    /**
     * 检查考试会话中是否包含指定题目的答案
     */
    @Query("SELECT COUNT(ea) > 0 FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId AND ea.id.questionId = :questionId")
    boolean existsBySessionIdAndQuestionId(@Param("sessionId") String sessionId, @Param("questionId") String questionId);
    
    /**
     * 根据考试会话ID获取答案详情（包含题目信息）
     */
    @Query("SELECT ea, q FROM ExamSessionAnswer ea JOIN Question q ON ea.id.questionId = q.id WHERE ea.id.sessionId = :sessionId ORDER BY ea.id.questionId")
    List<Object[]> findAnswersWithDetailsBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取未评分的答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId AND ea.score = 0")
    List<ExamSessionAnswer> findUnscoredAnswersBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取已评分的答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId AND ea.score > 0")
    List<ExamSessionAnswer> findScoredAnswersBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取有反馈的答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId AND ea.feedback IS NOT NULL AND ea.feedback != ''")
    List<ExamSessionAnswer> findAnswersWithFeedbackBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取无反馈的答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId AND (ea.feedback IS NULL OR ea.feedback = '')")
    List<ExamSessionAnswer> findAnswersWithoutFeedbackBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID统计各分数段的答案数量
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN ea.score >= 90 THEN '90-100' " +
           "  WHEN ea.score >= 80 THEN '80-89' " +
           "  WHEN ea.score >= 70 THEN '70-79' " +
           "  WHEN ea.score >= 60 THEN '60-69' " +
           "  ELSE '0-59' " +
           "END as scoreRange, " +
           "COUNT(ea) as count " +
           "FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId GROUP BY scoreRange ORDER BY scoreRange DESC")
    List<Object[]> countAnswersByScoreRangeInSession(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取最高分的答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId ORDER BY ea.score DESC LIMIT 1")
    ExamSessionAnswer findHighestScoreAnswerBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据考试会话ID获取最低分的答案
     */
    @Query("SELECT ea FROM ExamSessionAnswer ea WHERE ea.id.sessionId = :sessionId ORDER BY ea.score ASC LIMIT 1")
    ExamSessionAnswer findLowestScoreAnswerBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据试卷ID删除所有考试会话答案
     */
    @Modifying
    @Query("DELETE FROM ExamSessionAnswer ea WHERE ea.id.sessionId IN (SELECT e.sessionId FROM ExamSession e WHERE e.paperId = :paperId)")
    void deleteByPaperId(@Param("paperId") String paperId);
} 