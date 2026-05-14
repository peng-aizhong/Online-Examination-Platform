package com.exam.service.impl;

import com.exam.entity.Paper;
import com.exam.entity.ExamSession;
import com.exam.entity.Subject;
import com.exam.repository.PaperRepository;
import com.exam.repository.ExamSessionRepository;
import com.exam.repository.SubjectRepository;
import com.exam.service.AdminExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员考试管理服务实现类
 */
@Service
public class AdminExamServiceImpl implements AdminExamService {
    
    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    // ========== 试卷管理 ==========
    
    @Override
    public Page<Paper> getAllPapers(Pageable pageable) {
        return paperRepository.findAllWithSubject(pageable);
    }
    
    @Override
    public Page<Paper> searchPapers(String keyword, String subjectId, String status, Pageable pageable) {
        // 由于PaperRepository没有继承JpaSpecificationExecutor，我们使用简单的查询方法
        // 这里先获取所有试卷，然后在内存中进行筛选（对于小量数据是可行的）
        // 在实际生产环境中，建议在PaperRepository中添加相应的查询方法
        
        List<Paper> allPapers = paperRepository.findAllWithSubjectList();
        List<Paper> filteredPapers = new ArrayList<>();
        
        for (Paper paper : allPapers) {
            boolean matches = true;
            
            // 关键词搜索（试卷名称、描述）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchLower = keyword.trim().toLowerCase();
                boolean matchesSearch = 
                    (paper.getPaperName() != null && paper.getPaperName().toLowerCase().contains(searchLower));
                if (!matchesSearch) {
                    matches = false;
                }
            }
            
            // 科目筛选
            if (matches && subjectId != null && !subjectId.trim().isEmpty() && !"all".equals(subjectId)) {
                if (!subjectId.equals(paper.getSubjectId())) {
                    matches = false;
                }
            }
            
            // 状态筛选
            if (matches && status != null && !status.trim().isEmpty() && !"all".equals(status)) {
                if (!status.equals(paper.getStatus())) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredPapers.add(paper);
            }
        }
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredPapers.size());
        List<Paper> pageContent = filteredPapers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredPapers.size());
    }
    
    @Override
    public Paper getPaperById(String paperId) {
        return paperRepository.findById(paperId).orElse(null);
    }
    
    @Override
    public Paper updatePaper(Paper paper) {
        if (paper.getPaperId() == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        
        Paper existingPaper = paperRepository.findById(paper.getPaperId())
            .orElseThrow(() -> new RuntimeException("试卷不存在"));
        
        // 更新允许修改的字段
        existingPaper.setPaperName(paper.getPaperName());
        existingPaper.setDuration(paper.getDuration());
        existingPaper.setTotalScore(paper.getTotalScore());
        existingPaper.setDifficultyLevel(paper.getDifficultyLevel());
        existingPaper.setStatus(paper.getStatus());
        existingPaper.setUpdatedAt(LocalDateTime.now());
        
        return paperRepository.save(existingPaper);
    }
    
    @Override
    public boolean updatePaperStatus(String paperId, String status) {
        try {
            Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
            
            paper.setStatus(status);
            paper.setUpdatedAt(LocalDateTime.now());
            paperRepository.save(paper);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean deletePaper(String paperId) {
        try {
            Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
            
            // 检查试卷是否有关联的考试会话
            long sessionCount = examSessionRepository.countByPaperId(paperId);
            if (sessionCount > 0) {
                throw new RuntimeException("该试卷下还有考试会话，无法删除");
            }
            
            paperRepository.delete(paper);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== 考试会话管理 ==========
    
    @Override
    public Page<ExamSession> getAllExamSessions(Pageable pageable) {
        // 获取所有考试会话（预加载关联数据）
        List<ExamSession> allSessions = examSessionRepository.findAllWithDetails();
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSessions.size());
        List<ExamSession> pageContent = allSessions.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allSessions.size());
    }
    
    @Override
    public Page<ExamSession> searchExamSessions(String keyword, String paperId, String studentId, String status, Pageable pageable) {
        // 由于ExamSessionRepository没有继承JpaSpecificationExecutor，我们使用简单的查询方法
        // 这里先获取所有考试会话，然后在内存中进行筛选（对于小量数据是可行的）
        // 在实际生产环境中，建议在ExamSessionRepository中添加相应的查询方法
        
        List<ExamSession> allSessions = examSessionRepository.findAllWithDetails();
        List<ExamSession> filteredSessions = new ArrayList<>();
        
        for (ExamSession session : allSessions) {
            boolean matches = true;
            
            // 试卷ID筛选
            if (paperId != null && !paperId.trim().isEmpty() && !"all".equals(paperId)) {
                if (!paperId.equals(session.getPaperId())) {
                    matches = false;
                }
            }
            
            // 学生ID筛选
            if (matches && studentId != null && !studentId.trim().isEmpty() && !"all".equals(studentId)) {
                if (!studentId.equals(session.getStudentId())) {
                    matches = false;
                }
            }
            
            // 状态筛选
            if (matches && status != null && !status.trim().isEmpty() && !"all".equals(status)) {
                if (!status.equals(session.getStatus().getDisplayName())) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredSessions.add(session);
            }
        }
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredSessions.size());
        List<ExamSession> pageContent = filteredSessions.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filteredSessions.size());
    }
    
    @Override
    public ExamSession getExamSessionById(String sessionId) {
        return examSessionRepository.findByIdWithDetails(sessionId);
    }
    
    @Override
    public boolean updateExamSessionStatus(String sessionId, String status) {
        try {
            ExamSession examSession = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));
            
            // 将字符串状态转换为枚举状态
            ExamSession.SessionStatus sessionStatus = convertStringToSessionStatus(status);
            if (sessionStatus == null) {
                throw new RuntimeException("无效的状态值: " + status);
            }
            
            examSession.setStatus(sessionStatus);
            examSessionRepository.save(examSession);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 将字符串状态转换为SessionStatus枚举
     */
    private ExamSession.SessionStatus convertStringToSessionStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        switch (status.trim().toLowerCase()) {
            case "not_started":
            case "未开始":
                return ExamSession.SessionStatus.not_started;
            case "ongoing":
            case "进行中":
                return ExamSession.SessionStatus.ongoing;
            case "submitted":
            case "已提交":
            case "finished":
            case "已完成":
                return ExamSession.SessionStatus.submitted;
            case "timeout":
            case "超时":
                return ExamSession.SessionStatus.timeout;
            case "cancelled":
            case "已取消":
                return ExamSession.SessionStatus.cancelled;
            default:
                return null;
        }
    }
    
    @Override
    public boolean deleteExamSession(String sessionId) {
        try {
            ExamSession examSession = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));
            
            examSessionRepository.delete(examSession);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<ExamSession> getOngoingExamSessions() {
        return examSessionRepository.findOngoingExamSessions();
    }
    
    @Override
    public List<ExamSession> getPendingGradingExamSessions() {
        return examSessionRepository.findPendingGradingExamSessions();
    }
    
    // ========== 统计功能 ==========
    
    @Override
    public Map<String, Object> getExamStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 试卷统计
        Map<String, Object> paperStats = getPaperStatistics();
        statistics.put("paperStatistics", paperStats);
        
        // 考试会话统计
        Map<String, Object> sessionStats = getExamSessionStatistics();
        statistics.put("sessionStatistics", sessionStats);
        
        // 进行中的考试数量
        long ongoingCount = examSessionRepository.countByStatus(ExamSession.SessionStatus.ongoing);
        statistics.put("ongoingExamCount", ongoingCount);
        
        // 待阅卷的考试数量
        long pendingGradingCount = examSessionRepository.findPendingGradingExamSessions().size();
        statistics.put("pendingGradingCount", pendingGradingCount);
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> getPaperStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总试卷数
        long totalPapers = paperRepository.count();
        statistics.put("totalPapers", totalPapers);
        
        // 状态统计
        Map<String, Long> statusStats = getPaperStatusStatistics();
        statistics.put("statusDistribution", statusStats);
        
        // 科目统计
        Map<String, Long> subjectStats = getPaperCountBySubject();
        statistics.put("subjectDistribution", subjectStats);
        
        // 今日新增试卷数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewPapers = paperRepository.countByCreatedAtAfter(today);
        statistics.put("todayNewPapers", todayNewPapers);
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> getExamSessionStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总考试会话数
        long totalSessions = examSessionRepository.count();
        statistics.put("totalSessions", totalSessions);
        
        // 状态统计
        Map<String, Long> statusStats = getExamSessionStatusStatistics();
        statistics.put("statusDistribution", statusStats);
        
        // 科目统计
        Map<String, Long> subjectStats = getExamSessionCountBySubject();
        statistics.put("subjectDistribution", subjectStats);
        
        // 今日考试会话数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todaySessions = examSessionRepository.countByStartedAtAfter(today);
        statistics.put("todaySessions", todaySessions);
        
        return statistics;
    }
    
    @Override
    public Map<String, Long> getPaperStatusStatistics() {
        List<Paper> allPapers = paperRepository.findAll();
        return allPapers.stream()
            .collect(Collectors.groupingBy(
                Paper::getStatus,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getExamSessionStatusStatistics() {
        List<ExamSession> allSessions = examSessionRepository.findAll();
        return allSessions.stream()
            .collect(Collectors.groupingBy(
                session -> session.getStatus().getDisplayName(),
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getPaperCountBySubject() {
        List<Paper> allPapers = paperRepository.findAll();
        Map<String, Long> subjectCounts = new HashMap<>();
        
        for (Paper paper : allPapers) {
            Subject subject = subjectRepository.findById(paper.getSubjectId()).orElse(null);
            if (subject != null) {
                String subjectName = subject.getSubjectName();
                subjectCounts.put(subjectName, subjectCounts.getOrDefault(subjectName, 0L) + 1);
            }
        }
        
        return subjectCounts;
    }
    
    @Override
    public Map<String, Long> getExamSessionCountBySubject() {
        List<ExamSession> allSessions = examSessionRepository.findAll();
        Map<String, Long> subjectCounts = new HashMap<>();
        
        for (ExamSession session : allSessions) {
            Paper paper = paperRepository.findById(session.getPaperId()).orElse(null);
            if (paper != null) {
                Subject subject = subjectRepository.findById(paper.getSubjectId()).orElse(null);
                if (subject != null) {
                    String subjectName = subject.getSubjectName();
                    subjectCounts.put(subjectName, subjectCounts.getOrDefault(subjectName, 0L) + 1);
                }
            }
        }
        
        return subjectCounts;
    }
}
