package com.examination.repository;

import com.examination.entity.Question;
import com.examination.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    // ========== 原有方法（保留，不能删除） ==========
    List<Question> findBySubject(Subject subject);
    List<Question> findBySubject_SubjectId(String subjectId);
    List<Question> findBySubjectAndStatus(Subject subject, String status);
    List<Question> findByKnowledgeTag(String knowledgeTag);

    // ========== 新增方法（为试题创建与审核功能添加） ==========
    // 教师查询自己创建的试题（带条件筛选）
    @Query("SELECT q FROM Question q JOIN FETCH q.subject s JOIN FETCH q.creator c " +
            "WHERE (:subjectId IS NULL OR q.subject.id = :subjectId) " +
            "AND (:questionType IS NULL OR q.questionType = :questionType) " +
            "AND (:status IS NULL OR q.status = :status) " +
            "AND (:keyword IS NULL OR q.content LIKE %:keyword%) " +
            "AND q.creator.id = :creatorId")
    Page<Question> findByCreatorAndConditions(
            @Param("creatorId") String creatorId,
            @Param("subjectId") String subjectId,
            @Param("questionType") String questionType,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 查询试题详情（关联所有信息）
    @Query("SELECT q FROM Question q JOIN FETCH q.subject s JOIN FETCH q.creator c " +
            "LEFT JOIN FETCH q.reviewer r LEFT JOIN FETCH q.tags " +
            "WHERE q.id = :id")
    Question findDetailById(@Param("id") String id);
}