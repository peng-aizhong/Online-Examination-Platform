package com.exam.repository;

import com.exam.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {

    // 根据科目名称查找科目
    Subject findBySubjectName(String subjectName);

    // 根据科目代码查找科目
    Subject findBySubjectCode(String subjectCode);

    // 根据状态查找科目
    List<Subject> findByStatus(String status);

    // 根据科目名称模糊查找
    List<Subject> findBySubjectNameContaining(String subjectName);

    // 根据科目代码模糊查找
    List<Subject> findBySubjectCodeContaining(String subjectCode);

    // 根据状态统计科目数量
    long countByStatus(Subject.SubjectStatus status);

    // 检查科目名称是否存在
    boolean existsBySubjectName(String subjectName);

    // 检查科目代码是否存在
    boolean existsBySubjectCode(String subjectCode);

    // 获取所有启用的科目
    @Query("SELECT s FROM Subject s WHERE s.status = 'active' ORDER BY s.subjectName")
    List<Subject> findAllActiveSubjects();
    
    /**
     * 统计指定时间后创建的科目数量
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * 查找最大的科目ID（用于生成递增ID）
     */
    @Query("SELECT MAX(s.subjectId) FROM Subject s WHERE s.subjectId LIKE 'SUB%'")
    String findMaxSubjectId();
} 