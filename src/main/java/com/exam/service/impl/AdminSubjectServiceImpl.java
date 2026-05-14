package com.exam.service.impl;

import com.exam.entity.Subject;
import com.exam.repository.SubjectRepository;
import com.exam.service.AdminSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员科目管理服务实现类
 */
@Service
public class AdminSubjectServiceImpl implements AdminSubjectService {
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Override
    public Page<Subject> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }
    
    @Override
    public Page<Subject> searchSubjects(String keyword, String status, Pageable pageable) {
        // 由于SubjectRepository没有继承JpaSpecificationExecutor，我们使用简单的查询方法
        // 这里先获取所有科目，然后在内存中进行筛选（对于小量数据是可行的）
        // 在实际生产环境中，建议在SubjectRepository中添加相应的查询方法
        
        List<Subject> allSubjects = subjectRepository.findAll();
        List<Subject> filteredSubjects = new ArrayList<>();
        
        for (Subject subject : allSubjects) {
            boolean matches = true;
            
            // 关键词搜索（科目名称、科目代码、描述）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchLower = keyword.trim().toLowerCase();
                boolean matchesSearch = 
                    (subject.getSubjectName() != null && subject.getSubjectName().toLowerCase().contains(searchLower)) ||
                    (subject.getSubjectCode() != null && subject.getSubjectCode().toLowerCase().contains(searchLower)) ||
                    (subject.getDescription() != null && subject.getDescription().toLowerCase().contains(searchLower));
                if (!matchesSearch) {
                    matches = false;
                }
            }
            
            // 状态筛选
            if (matches && status != null && !status.trim().isEmpty() && !"all".equals(status)) {
                try {
                    Subject.SubjectStatus subjectStatus = Subject.SubjectStatus.valueOf(status.toUpperCase());
                    if (subject.getStatus() != subjectStatus) {
                        matches = false;
                    }
                } catch (IllegalArgumentException e) {
                    // 如果状态值无效，跳过筛选
                }
            }
            
            if (matches) {
                filteredSubjects.add(subject);
            }
        }
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredSubjects.size());
        List<Subject> pageContent = filteredSubjects.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredSubjects.size());
    }
    
    @Override
    public Subject getSubjectById(String subjectId) {
        return subjectRepository.findById(subjectId).orElse(null);
    }
    
    @Override
    public Subject createSubject(Subject subject) {
        // 生成科目ID
        String subjectId = generateSubjectId();
        subject.setSubjectId(subjectId);
        subject.setCreatedAt(LocalDateTime.now());
        subject.setUpdatedAt(LocalDateTime.now());
        
        return subjectRepository.save(subject);
    }
    
    @Override
    public Subject updateSubject(Subject subject) {
        if (subject.getSubjectId() == null) {
            throw new IllegalArgumentException("科目ID不能为空");
        }
        
        Subject existingSubject = subjectRepository.findById(subject.getSubjectId())
            .orElseThrow(() -> new RuntimeException("科目不存在"));
        
        // 更新允许修改的字段
        existingSubject.setSubjectName(subject.getSubjectName());
        existingSubject.setSubjectCode(subject.getSubjectCode());
        existingSubject.setDescription(subject.getDescription());
        existingSubject.setStatus(subject.getStatus());
        existingSubject.setUpdatedAt(LocalDateTime.now());
        
        return subjectRepository.save(existingSubject);
    }
    
    @Override
    public boolean updateSubjectStatus(String subjectId, Subject.SubjectStatus status) {
        try {
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("科目不存在"));
            
            subject.setStatus(status);
            subject.setUpdatedAt(LocalDateTime.now());
            subjectRepository.save(subject);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean deleteSubject(String subjectId) {
        try {
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("科目不存在"));
            
            // 检查科目是否有关联的题目或试卷
            if (subject.getQuestionCount() > 0 || subject.getPaperCount() > 0) {
                throw new RuntimeException("该科目下还有题目或试卷，无法删除");
            }
            
            subjectRepository.delete(subject);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getSubjectStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总科目数
        long totalSubjects = subjectRepository.count();
        statistics.put("totalSubjects", totalSubjects);
        
        // 状态统计
        Map<String, Long> statusStats = getSubjectStatusStatistics();
        statistics.put("statusDistribution", statusStats);
        
        // 活跃科目数
        long activeSubjects = subjectRepository.countByStatus(Subject.SubjectStatus.active);
        statistics.put("activeSubjects", activeSubjects);
        
        // 今日新增科目数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewSubjects = subjectRepository.countByCreatedAtAfter(today);
        statistics.put("todayNewSubjects", todayNewSubjects);
        
        return statistics;
    }
    
    @Override
    public Map<String, Long> getSubjectStatusStatistics() {
        List<Subject> allSubjects = subjectRepository.findAll();
        Map<String, Long> statusStats = new HashMap<>();
        
        long activeCount = allSubjects.stream()
            .filter(subject -> subject.getStatus() == Subject.SubjectStatus.active)
            .count();
        long inactiveCount = allSubjects.size() - activeCount;
        
        statusStats.put("active", activeCount);
        statusStats.put("inactive", inactiveCount);
        
        return statusStats;
    }
    
    @Override
    public boolean existsBySubjectCode(String subjectCode) {
        return subjectRepository.existsBySubjectCode(subjectCode);
    }
    
    @Override
    public boolean existsBySubjectName(String subjectName) {
        return subjectRepository.existsBySubjectName(subjectName);
    }
    
    /**
     * 生成科目ID
     */
    @Override
    public String generateSubjectId() {
        // 查找最大的科目ID
        String maxId = subjectRepository.findMaxSubjectId();
        if (maxId == null) {
            return "SUB001";
        }
        
        // 提取数字部分并递增
        String numberPart = maxId.substring(3); // 去掉"SUB"前缀
        int nextNumber = Integer.parseInt(numberPart) + 1;
        return String.format("SUB%03d", nextNumber);
    }
}
