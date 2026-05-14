package com.exam.service;

import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.dto.PaperGenerationRequest;
import com.exam.dto.PaperGenerationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PaperService {
    
    /**
     * 创建试卷
     */
    Paper createPaper(Paper paper, List<String> questionIds, List<Integer> scores);
    
    /**
     * 创建试卷（新版本，自动计算分数）
     */
    Paper createPaper(Paper paper, List<String> questionIds);
    
    /**
     * 根据ID获取试卷
     */
    Paper getPaperById(String id);
    
    /**
     * 更新试卷
     */
    Paper updatePaper(String id, Paper paper, List<String> questionIds, List<Integer> scores);
    
    /**
     * 更新试卷（新版本，自动计算分数）
     */
    Paper updatePaper(Paper paper, List<String> questionIds);
    
    /**
     * 删除试卷
     */
    void deletePaper(String id);
    
    /**
     * 强制删除试卷（清理所有相关数据）
     */
    void forceDeletePaper(String id);
    
    /**
     * SQL强制删除试卷（使用原生SQL绕过外键约束）
     */
    void sqlForceDeletePaper(String id);
    
    /**
     * 切换试卷状态
     */
    void togglePaperStatus(String id);
    
    /**
     * 搜索试卷
     */
    Page<Paper> searchPapers(String subjectId, String keyword, Pageable pageable);
    
    /**
     * 搜索试卷（按创建者）
     */
    Page<Paper> searchPapers(String creatorId, String subjectId, String keyword, Pageable pageable);
    
    /**
     * 获取所有试卷
     */
    List<Paper> getAllPapers();
    
    /**
     * 根据创建者获取试卷
     */
    List<Paper> getPapersByCreator(String creatorId);
    
    /**
     * 获取试卷题目
     */
    List<PaperQuestion> getPaperQuestions(String paperId);
    
    /**
     * 获取试卷题目（返回Question对象）
     */
    List<Question> getPaperQuestionDetails(String paperId);
    
    /**
     * 智能组卷
     */
    PaperGenerationResult generatePaper(PaperGenerationRequest request);
    
    /**
     * 生成答案
     */
    String generateAnswerKey(String paperId);
    
    /**
     * 获取试卷总数
     */
    long getPaperCount();
    
    /**
     * 根据创建者获取试卷数量
     */
    long getPaperCountByCreator(String creatorId);
    
    /**
     * 验证试卷参数
     */
    boolean validatePaperParameters(PaperGenerationRequest request);
    
    /**
     * 计算试卷难度系数
     */
    double calculatePaperDifficulty(List<String> questionIds);
    
    /**
     * 计算题目分数
     */
    int calculateQuestionScore(Question question);
    
    /**
     * 获取试卷统计信息
     */
    Map<String, Object> getPaperStatistics(String creatorId);
} 