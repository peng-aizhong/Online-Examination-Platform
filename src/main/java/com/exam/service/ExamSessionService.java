package com.exam.service;

import com.exam.entity.ExamSession;
import com.exam.entity.ExamSessionAnswer;
import com.exam.entity.Paper;
import com.exam.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ExamSessionService {
    
    /**
     * 创建考试会话
     */
    ExamSession createExamSession(String paperId, String studentId);
    
    /**
     * 根据考试分配创建考试会话
     */
    ExamSession createExamSessionFromAssignment(String assignmentId, String studentId);
    
    /**
     * 根据ID获取考试会话
     */
    ExamSession getSessionById(String sessionId);
    
    /**
     * 根据状态获取考试会话列表
     */
    List<ExamSession> getSessionsByStatus(ExamSession.SessionStatus status);
    
    /**
     * 更新考试会话
     */
    ExamSession updateSession(ExamSession session);
    
    /**
     * 删除考试会话
     */
    void deleteSession(String sessionId);
    
    /**
     * 开始考试
     */
    ExamSession startExam(String sessionId);
    
    /**
     * 提交考试
     */
    ExamSession submitExam(String sessionId);
    
    /**
     * 保存学生答案
     */
    void saveAnswer(String sessionId, String questionId, String answer);
    
    /**
     * 获取学生可参加的考试列表
     */
    List<Paper> getAvailableExams(String studentId);
    
    /**
     * 获取学生进行中的考试
     */
    List<ExamSession> getActiveExams(String studentId);
    
    /**
     * 获取学生进行中的考试会话
     */
    List<ExamSession> getActiveSessionsForStudent(String studentId);
    
    /**
     * 获取学生历史考试记录
     */
    List<ExamSession> getHistoryExams(String studentId);
    
    /**
     * 获取学生所有考试会话（包括进行中和已完成的）
     */
    List<ExamSession> getStudentAllSessions(String studentId);
    
    /**
     * 获取学生历史考试记录（分页）
     */
    Page<ExamSession> getHistoryExams(String studentId, Pageable pageable);
    
    /**
     * 获取考试会话的所有答案
     */
    List<ExamSessionAnswer> getSessionAnswers(String sessionId);
    
    /**
     * 获取考试会话的题目详情
     */
    List<Question> getSessionQuestions(String sessionId);
    
    /**
     * 自动评分客观题
     */
    void autoGradeObjectiveQuestions(String sessionId);
    
    /**
     * 获取需要阅卷的考试会话
     */
    List<ExamSession> getSessionsForGrading();
    
    /**
     * 获取已阅卷的考试会话
     */
    List<ExamSession> getGradedSessions();
    
    /**
     * 更新答案分数
     */
    void updateAnswerScore(String sessionId, String questionId, Double score);
    
    /**
     * 更新答案反馈
     */
    void updateAnswerFeedback(String sessionId, String questionId, String feedback);
    
    /**
     * 更新考试会话总分
     */
    void updateSessionTotalScore(String sessionId);
    
    /**
     * 更新考试会话状态
     */
    void updateSessionStatus(String sessionId, ExamSession.SessionStatus status);

    /**
     * 是否还有主观题未评分
     */
    boolean hasSubjectiveQuestionsNeedingGrading(String sessionId);
    
    /**
     * 获取学生考试统计信息
     */
    Map<String, Object> getStudentStatistics(String studentId);
    
    /**
     * 获取学生总考试数
     */
    long getTotalExamsByStudent(String studentId);
    
    /**
     * 获取学生已完成考试数
     */
    long getCompletedExamsByStudent(String studentId);
    
    /**
     * 获取学生进行中考试数
     */
    long getActiveExamsByStudent(String studentId);
    
    /**
     * 获取学生可参加考试数
     */
    long getAvailableExamsByStudent(String studentId);
    
    /**
     * 获取学生平均分
     */
    double getAverageScoreByStudent(String studentId);
    
    /**
     * 获取学生优秀成绩数（>=90分）
     */
    long getExcellentScoresByStudent(String studentId);
    
    /**
     * 检查学生是否可以参加指定考试
     */
    boolean canStudentTakeExam(String studentId, String paperId);
    
    /**
     * 获取学生和试卷的现有考试会话
     */
    ExamSession getExistingSession(String studentId, String paperId);
    
    /**
     * 获取学生和考试分配的现有考试会话
     */
    ExamSession getExistingSessionByAssignment(String studentId, String assignmentId);
    
    /**
     * 检查考试是否超时
     */
    boolean isExamTimeout(String sessionId);
    
    /**
     * 获取考试剩余时间（分钟）
     */
    int getRemainingTime(String sessionId);
    
    /**
     * 生成考试会话ID
     */
    String generateSessionId();
    
    /**
     * 获取学生成绩（分页查询）
     */
    Page<ExamSession> getStudentGrades(String studentName, String paperId, String startDate, String endDate, int page, int size);
    
    /**
     * 获取成绩统计信息
     */
    Map<String, Object> getGradeStatistics(String studentName, String paperId, String startDate, String endDate);
    
    /**
     * 获取教师创建的考试数量
     */
    long getExamCountByTeacher(String teacherId);
    
    /**
     * 获取教师待阅卷数量
     */
    long getGradingCountByTeacher(String teacherId);
    
    /**
     * 获取系统学生总数
     */
    long getTotalStudentCount();
    
    /**
     * 获取进行中的考试数量
     */
    long getOngoingExamCount();
}
