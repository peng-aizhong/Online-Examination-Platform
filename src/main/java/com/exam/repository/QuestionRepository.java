package com.exam.repository;

import com.exam.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String>, JpaSpecificationExecutor<Question> {

    // 根据科目查找题目
    List<Question> findBySubjectId(String subjectId);

    // 根据题型查找题目
    List<Question> findByQuestionType(String questionType);

    // 根据难度查找题目
    List<Question> findByDifficulty(String difficulty);

    // 根据状态查找题目
    List<Question> findByStatus(String status);

    // 根据科目和题型查找题目
    List<Question> findBySubjectIdAndQuestionType(String subjectId, String questionType);

    // 根据科目和难度查找题目
    List<Question> findBySubjectIdAndDifficulty(String subjectId, String difficulty);

    // 根据题型和难度查找题目
    List<Question> findByQuestionTypeAndDifficulty(String questionType, String difficulty);

    // 根据科目、题型和难度查找题目
    List<Question> findBySubjectIdAndQuestionTypeAndDifficulty(String subjectId, String questionType, String difficulty);

    // 根据知识点标签查找题目
    @Query("SELECT q FROM Question q WHERE q.knowledgeTag LIKE %:knowledgeTag%")
    List<Question> findByKnowledgeTagContaining(@Param("knowledgeTag") String knowledgeTag);

    // 根据内容关键词查找题目
    @Query("SELECT q FROM Question q WHERE q.content LIKE %:keyword%")
    List<Question> findByContentContaining(@Param("keyword") String keyword);

    // 复杂查询：根据多个条件查找题目
    @Query("SELECT q FROM Question q WHERE " +
           "(:subjectId IS NULL OR q.subjectId = :subjectId) AND " +
           "(:questionId IS NULL OR q.id = :questionId) AND " +
           "(:questionType IS NULL OR q.questionType = :questionType) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:status IS NULL OR q.status = :status) AND " +
           "(:keyword IS NULL OR q.content LIKE %:keyword% OR q.knowledgeTag LIKE %:keyword%)")
    Page<Question> findByComplexCriteria(@Param("subjectId") String subjectId,
                                        @Param("questionId") String questionId,
                                        @Param("questionType") String questionType,
                                        @Param("difficulty") String difficulty,
                                        @Param("status") String status,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    // 随机查找指定数量的题目
    @Query(value = "SELECT * FROM question WHERE subject_id = :subjectId AND question_type = :questionType AND difficulty = :difficulty ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByTypeAndDifficulty(@Param("subjectId") String subjectId,
                                                         @Param("questionType") String questionType,
                                                         @Param("difficulty") String difficulty,
                                                         @Param("limit") int limit);

    // 获取所有知识点标签
    @Query("SELECT DISTINCT q.knowledgeTag FROM Question q WHERE q.knowledgeTag IS NOT NULL AND q.knowledgeTag != ''")
    List<String> findAllKnowledgeTags();

    // 统计各题型的题目数量
    @Query("SELECT q.questionType, COUNT(q) FROM Question q GROUP BY q.questionType")
    List<Object[]> countByQuestionType();

    // 统计各难度的题目数量
    @Query("SELECT q.difficulty, COUNT(q) FROM Question q GROUP BY q.difficulty")
    List<Object[]> countByDifficulty();

    // 统计各科目的题目数量
    @Query("SELECT q.subjectId, COUNT(q) FROM Question q GROUP BY q.subjectId")
    List<Object[]> countBySubjectId();

    // 根据科目ID统计题目数量
    long countBySubjectId(String subjectId);

    // 根据题型统计题目数量
    long countByQuestionType(String questionType);

    // 根据难度统计题目数量
    long countByDifficulty(String difficulty);

    // 根据状态统计题目数量
    long countByStatus(String status);

    // 根据科目ID获取所有知识点标签（去重）
    @Query("SELECT DISTINCT q.knowledgeTag FROM Question q WHERE q.subjectId = :subjectId AND q.knowledgeTag IS NOT NULL AND q.knowledgeTag != ''")
    List<String> findKnowledgeTagsBySubjectId(@Param("subjectId") String subjectId);

    // 统计各题型的题目数量（返回Object数组）
    @Query("SELECT q.questionType, COUNT(q) FROM Question q GROUP BY q.questionType")
    List<Object[]> countQuestionsByType();

    // 统计各难度的题目数量（返回Object数组）
    @Query("SELECT q.difficulty, COUNT(q) FROM Question q GROUP BY q.difficulty")
    List<Object[]> countQuestionsByDifficulty();
    
    // 统计指定题型的题目数量
    @Query("SELECT COUNT(q) FROM Question q WHERE q.questionType = :questionType")
    long countByQuestionTypeStartingWith(@Param("questionType") String questionType);
    
    // 根据状态和科目ID统计题目数量
    @Query("SELECT COUNT(q) FROM Question q WHERE q.status = :status AND (:subjectId IS NULL OR q.subjectId = :subjectId)")
    long countByStatusAndSubjectId(@Param("status") String status, @Param("subjectId") String subjectId);
    
    // 根据科目ID统计不同知识点标签数量
    @Query("SELECT COUNT(DISTINCT q.knowledgeTag) FROM Question q WHERE (:subjectId IS NULL OR q.subjectId = :subjectId) AND q.knowledgeTag IS NOT NULL AND q.knowledgeTag != ''")
    long countDistinctKnowledgeTagsBySubjectId(@Param("subjectId") String subjectId);
    
    // 根据科目ID统计各题型的题目数量
    @Query("SELECT q.questionType, COUNT(q) FROM Question q WHERE (:subjectId IS NULL OR q.subjectId = :subjectId) GROUP BY q.questionType")
    List<Object[]> countByQuestionTypeAndSubjectId(@Param("subjectId") String subjectId);
    
    // 根据科目ID统计各难度的题目数量
    @Query("SELECT q.difficulty, COUNT(q) FROM Question q WHERE (:subjectId IS NULL OR q.subjectId = :subjectId) GROUP BY q.difficulty")
    List<Object[]> countByDifficultyAndSubjectId(@Param("subjectId") String subjectId);
} 