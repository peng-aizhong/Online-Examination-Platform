package com.exam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员统计服务接口
 */
public interface AdminStatisticsService {
    
    // ========== 基础统计 ==========
    
    /**
     * 获取系统总览统计
     */
    Map<String, Object> getSystemOverview();
    
    /**
     * 获取用户统计
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取用户详情统计列表
     */
    List<Map<String, Object>> getUserDetails();
    
    /**
     * 获取科目统计
     */
    Map<String, Object> getSubjectStatistics();
    
    /**
     * 获取科目详细统计列表
     */
    List<Map<String, Object>> getSubjectDetailsStatistics();
    
    /**
     * 获取试卷统计
     */
    Map<String, Object> getPaperStatistics();
    
    /**
     * 获取考试会话统计
     */
    Map<String, Object> getExamSessionStatistics();
    
    /**
     * 获取考试统计
     */
    Map<String, Object> getExamStatistics();
    
    /**
     * 获取成绩统计
     */
    Map<String, Object> getScoreStatistics();
    
    // ========== 时间维度统计 ==========
    
    /**
     * 获取今日统计
     */
    Map<String, Object> getTodayStatistics();
    
    /**
     * 获取本周统计
     */
    Map<String, Object> getWeekStatistics();
    
    /**
     * 获取本月统计
     */
    Map<String, Object> getMonthStatistics();
    
    /**
     * 获取指定时间范围的统计
     */
    Map<String, Object> getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // ========== 趋势分析 ==========
    
    /**
     * 获取用户注册趋势（最近30天）
     */
    List<Map<String, Object>> getUserRegistrationTrend();
    
    /**
     * 获取用户注册趋势（指定天数）
     */
    List<Map<String, Object>> getUserRegistrationTrend(int days);
    
    /**
     * 获取考试完成趋势（最近30天）
     */
    List<Map<String, Object>> getExamCompletionTrend();
    
    /**
     * 获取考试完成趋势（指定天数）
     */
    List<Map<String, Object>> getExamCompletionTrend(int days);
    
    /**
     * 获取试卷创建趋势（最近30天）
     */
    List<Map<String, Object>> getPaperCreationTrend();
    
    // ========== 分布统计 ==========
    
    /**
     * 获取用户角色分布
     */
    Map<String, Long> getUserRoleDistribution();
    
    /**
     * 获取用户状态分布
     */
    Map<String, Long> getUserStatusDistribution();
    
    /**
     * 获取科目状态分布
     */
    Map<String, Long> getSubjectStatusDistribution();
    
    /**
     * 获取试卷状态分布
     */
    Map<String, Long> getPaperStatusDistribution();
    
    /**
     * 获取考试会话状态分布
     */
    Map<String, Long> getExamSessionStatusDistribution();
    
    /**
     * 获取试卷难度分布
     */
    Map<String, Long> getPaperDifficultyDistribution();
    
    /**
     * 获取各科目试卷数量分布
     */
    Map<String, Long> getPaperCountBySubject();
    
    /**
     * 获取各科目考试会话数量分布
     */
    Map<String, Long> getExamSessionCountBySubject();
    
    // ========== 成绩分析 ==========
    
    /**
     * 获取成绩分布统计
     */
    Map<String, Object> getScoreDistribution();
    
    /**
     * 获取各科目平均分
     */
    Map<String, Double> getAverageScoreBySubject();
    
    /**
     * 获取各难度试卷平均分
     */
    Map<String, Double> getAverageScoreByDifficulty();
    
    /**
     * 获取通过率统计
     */
    Map<String, Object> getPassRateStatistics();
    
    // ========== 活跃度分析 ==========
    
    /**
     * 获取用户活跃度统计
     */
    Map<String, Object> getUserActivityStatistics();
    
    /**
     * 获取系统使用情况统计
     */
    Map<String, Object> getSystemUsageStatistics();
}
