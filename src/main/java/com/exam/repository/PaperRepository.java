package com.exam.repository;

import com.exam.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, String> {

    // 根据科目查找试卷
    List<Paper> findBySubjectId(String subjectId);

    // 根据创建者查找试卷
    @Query("SELECT DISTINCT p FROM Paper p " +
           "LEFT JOIN FETCH p.subject " +
           "LEFT JOIN FETCH p.paperQuestions " +
           "WHERE p.creatorId = :creatorId ORDER BY p.createdAt DESC")
    List<Paper> findByCreatorId(@Param("creatorId") String creatorId);

    // 根据科目和创建者查找试卷
    List<Paper> findBySubjectIdAndCreatorId(String subjectId, String creatorId);

    // 根据试卷名称查找试卷
    List<Paper> findByPaperNameContaining(String paperName);

    // 复杂查询：根据多个条件查找试卷
    @Query("SELECT p FROM Paper p WHERE " +
           "(:subjectId IS NULL OR p.subjectId = :subjectId) AND " +
           "(:keyword IS NULL OR p.paperName LIKE %:keyword%)")
    Page<Paper> findByComplexCriteria(@Param("subjectId") String subjectId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    // 复杂查询：根据创建者、科目和关键词查找试卷
    @Query("SELECT DISTINCT p FROM Paper p " +
           "LEFT JOIN FETCH p.subject " +
           "LEFT JOIN FETCH p.paperQuestions " +
           "WHERE p.creatorId = :creatorId AND " +
           "(:subjectId IS NULL OR p.subjectId = :subjectId) AND " +
           "(:keyword IS NULL OR p.paperName LIKE %:keyword%) " +
           "ORDER BY p.createdAt DESC")
    Page<Paper> findByCreatorAndComplexCriteria(@Param("creatorId") String creatorId,
                                              @Param("subjectId") String subjectId,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

    // 获取所有试卷
    @Query("SELECT p FROM Paper p ORDER BY p.createdAt DESC")
    List<Paper> findAllPapers();

    // 根据创建者获取试卷数量
    long countByCreatorId(String creatorId);

    // 根据科目获取试卷数量
    long countBySubjectId(String subjectId);

    // 根据难度级别获取试卷数量
    long countByDifficultyLevel(String difficultyLevel);
    
    /**
     * 统计指定时间后创建的试卷数量
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * 获取所有试卷（带分页，预加载关联）
     */
    @Query("SELECT DISTINCT p FROM Paper p " +
           "LEFT JOIN FETCH p.subject " +
           "ORDER BY p.createdAt DESC")
    Page<Paper> findAllWithSubject(Pageable pageable);
    
    /**
     * 获取所有试卷（预加载关联）
     */
    @Query("SELECT DISTINCT p FROM Paper p " +
           "LEFT JOIN FETCH p.subject " +
           "ORDER BY p.createdAt DESC")
    List<Paper> findAllWithSubjectList();
} 