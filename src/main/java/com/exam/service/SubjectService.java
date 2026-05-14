package com.exam.service;

import com.exam.entity.Subject;
import java.util.List;

public interface SubjectService {
    
    /**
     * 获取所有科目
     */
    List<Subject> getAllSubjects();
    
    /**
     * 根据ID获取科目
     */
    Subject getSubjectById(String id);
    
    /**
     * 根据状态获取科目
     */
    List<Subject> getSubjectsByStatus(String status);
    
    /**
     * 添加科目
     */
    Subject addSubject(Subject subject);
    
    /**
     * 更新科目
     */
    Subject updateSubject(String id, Subject subject);
    
    /**
     * 删除科目
     */
    void deleteSubject(String id);
    
    /**
     * 检查科目是否存在
     */
    boolean existsById(String id);
} 