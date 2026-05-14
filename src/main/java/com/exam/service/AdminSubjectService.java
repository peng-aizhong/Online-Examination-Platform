package com.exam.service;

import com.exam.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 管理员科目管理服务接口
 */
public interface AdminSubjectService {
    
    /**
     * 获取所有科目（分页）
     */
    Page<Subject> getAllSubjects(Pageable pageable);
    
    /**
     * 根据条件搜索科目
     */
    Page<Subject> searchSubjects(String keyword, String status, Pageable pageable);
    
    /**
     * 获取科目详情
     */
    Subject getSubjectById(String subjectId);
    
    /**
     * 创建科目
     */
    Subject createSubject(Subject subject);
    
    /**
     * 更新科目信息
     */
    Subject updateSubject(Subject subject);
    
    /**
     * 更新科目状态
     */
    boolean updateSubjectStatus(String subjectId, Subject.SubjectStatus status);
    
    /**
     * 删除科目
     */
    boolean deleteSubject(String subjectId);
    
    /**
     * 获取科目统计信息
     */
    Map<String, Object> getSubjectStatistics();
    
    /**
     * 获取科目状态统计
     */
    Map<String, Long> getSubjectStatusStatistics();
    
    /**
     * 检查科目代码是否已存在
     */
    boolean existsBySubjectCode(String subjectCode);
    
    /**
     * 检查科目名称是否已存在
     */
    boolean existsBySubjectName(String subjectName);
    
    /**
     * 生成科目ID
     */
    String generateSubjectId();
}
