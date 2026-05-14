package com.exam.service.impl;

import com.exam.entity.Subject;
import com.exam.repository.SubjectRepository;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAllActiveSubjects();
    }
    
    @Override
    public Subject getSubjectById(String id) {
        return subjectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("科目不存在"));
    }
    
    @Override
    public List<Subject> getSubjectsByStatus(String status) {
        return subjectRepository.findByStatus(status);
    }
    
    @Override
    public Subject addSubject(Subject subject) {
        // 检查科目名称是否已存在
        if (subjectRepository.existsBySubjectName(subject.getSubjectName())) {
            throw new RuntimeException("科目名称已存在");
        }
        
        // 检查科目代码是否已存在
        if (subject.getSubjectCode() != null && subjectRepository.existsBySubjectCode(subject.getSubjectCode())) {
            throw new RuntimeException("科目代码已存在");
        }
        
        // 设置默认值
        if (subject.getStatus() == null) {
            subject.setStatus(Subject.SubjectStatus.active);
        }
        subject.setCreatedAt(LocalDateTime.now());
        subject.setUpdatedAt(LocalDateTime.now());
        
        return subjectRepository.save(subject);
    }
    
    @Override
    public Subject updateSubject(String id, Subject subject) {
        Subject existingSubject = getSubjectById(id);
        
        // 检查科目名称是否已被其他科目使用
        if (!existingSubject.getSubjectName().equals(subject.getSubjectName()) &&
            subjectRepository.existsBySubjectName(subject.getSubjectName())) {
            throw new RuntimeException("科目名称已存在");
        }
        
        // 检查科目代码是否已被其他科目使用
        if (subject.getSubjectCode() != null && 
            !subject.getSubjectCode().equals(existingSubject.getSubjectCode()) &&
            subjectRepository.existsBySubjectCode(subject.getSubjectCode())) {
            throw new RuntimeException("科目代码已存在");
        }
        
        // 更新字段
        existingSubject.setSubjectName(subject.getSubjectName());
        existingSubject.setSubjectCode(subject.getSubjectCode());
        existingSubject.setDescription(subject.getDescription());
        existingSubject.setStatus(subject.getStatus());
        existingSubject.setUpdatedAt(LocalDateTime.now());
        
        return subjectRepository.save(existingSubject);
    }
    
    @Override
    public void deleteSubject(String id) {
        Subject subject = getSubjectById(id);
        
        // 检查科目是否被使用（这里可以添加业务逻辑检查）
        // 例如：检查是否有题目、试卷、考试等使用该科目
        
        subjectRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        return subjectRepository.existsById(id);
    }
} 