package com.exam.service;

import com.exam.entity.Paper;
import com.exam.entity.ExamSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 管理员考试管理服务接口
 */
public interface AdminExamService {
    
    // ========== 试卷管理 ==========
    
    /**
     * 获取所有试卷（分页）
     */
    Page<Paper> getAllPapers(Pageable pageable);
    
    /**
     * 根据条件搜索试卷
     */
    Page<Paper> searchPapers(String keyword, String subjectId, String status, Pageable pageable);
    
    /**
     * 获取试卷详情
     */
    Paper getPaperById(String paperId);
    
    /**
     * 更新试卷信息
     */
    Paper updatePaper(Paper paper);
    
    /**
     * 更新试卷状态
     */
    boolean updatePaperStatus(String paperId, String status);
    
    /**
     * 删除试卷
     */
    boolean deletePaper(String paperId);
    
    // ========== 考试会话管理 ==========
    
    /**
     * 获取所有考试会话（分页）
     */
    Page<ExamSession> getAllExamSessions(Pageable pageable);
    
    /**
     * 根据条件搜索考试会话
     */
    Page<ExamSession> searchExamSessions(String keyword, String paperId, String studentId, String status, Pageable pageable);
    
    /**
     * 获取考试会话详情
     */
    ExamSession getExamSessionById(String sessionId);
    
    /**
     * 更新考试会话状态
     */
    boolean updateExamSessionStatus(String sessionId, String status);
    
    /**
     * 删除考试会话
     */
    boolean deleteExamSession(String sessionId);
    
    /**
     * 获取进行中的考试会话
     */
    List<ExamSession> getOngoingExamSessions();
    
    /**
     * 获取待阅卷的考试会话
     */
    List<ExamSession> getPendingGradingExamSessions();
    
    // ========== 统计功能 ==========
    
    /**
     * 获取考试统计信息
     */
    Map<String, Object> getExamStatistics();
    
    /**
     * 获取试卷统计信息
     */
    Map<String, Object> getPaperStatistics();
    
    /**
     * 获取考试会话统计信息
     */
    Map<String, Object> getExamSessionStatistics();
    
    /**
     * 获取各状态试卷数量统计
     */
    Map<String, Long> getPaperStatusStatistics();
    
    /**
     * 获取各状态考试会话数量统计
     */
    Map<String, Long> getExamSessionStatusStatistics();
    
    /**
     * 获取各科目试卷数量统计
     */
    Map<String, Long> getPaperCountBySubject();
    
    /**
     * 获取各科目考试会话数量统计
     */
    Map<String, Long> getExamSessionCountBySubject();
}
