package com.exam.repository;

import com.exam.entity.ExamSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, String>, JpaSpecificationExecutor<ExamSession> {
    
    /**
     * 根据考试分配ID查找考试会话
     */
    List<ExamSession> findByAssignmentId(String assignmentId);
    
    /**
     * 根据试卷ID查找考试会话
     */
    List<ExamSession> findByPaperId(String paperId);
    
    /**
     * 根据学生ID查找考试会话
     */
    List<ExamSession> findByStudentId(String studentId);
    
    /**
     * 根据状态查找考试会话
     */
    List<ExamSession> findByStatus(ExamSession.SessionStatus status);
    
    /**
     * 根据考试分配ID和状态查找考试会话
     */
    List<ExamSession> findByAssignmentIdAndStatus(String assignmentId, ExamSession.SessionStatus status);
    
    /**
     * 根据试卷ID和状态查找考试会话
     */
    List<ExamSession> findByPaperIdAndStatus(String paperId, ExamSession.SessionStatus status);
    
    /**
     * 根据学生ID和状态查找考试会话
     */
    List<ExamSession> findByStudentIdAndStatus(String studentId, ExamSession.SessionStatus status);
    
    /**
     * 根据考试分配ID和学生ID查找考试会话
     */
    List<ExamSession> findByAssignmentIdAndStudentId(String assignmentId, String studentId);
    
    /**
     * 根据考试分配ID、学生ID和状态查找考试会话
     */
    List<ExamSession> findByAssignmentIdAndStudentIdAndStatus(String assignmentId, String studentId, ExamSession.SessionStatus status);
    
    /**
     * 根据考试分配ID和学生ID查找最佳成绩的考试会话
     */
    ExamSession findByAssignmentIdAndStudentIdAndIsBestScoreTrue(String assignmentId, String studentId);
    
    /**
     * 根据考试分配ID和学生ID查找指定次数的考试会话
     */
    ExamSession findByAssignmentIdAndStudentIdAndAttemptNumber(String assignmentId, String studentId, Integer attemptNumber);
    
    /**
     * 根据考试分配ID和学生ID统计考试次数
     */
    long countByAssignmentIdAndStudentId(String assignmentId, String studentId);
    
    /**
     * 根据考试分配ID、学生ID和状态统计考试次数
     */
    long countByAssignmentIdAndStudentIdAndStatus(String assignmentId, String studentId, ExamSession.SessionStatus status);
    
    /**
     * 根据试卷ID和学生ID查找考试会话（向后兼容方法）
     */
    ExamSession findByPaperIdAndStudentId(String paperId, String studentId);
    
    /**
     * 根据开始时间范围查找考试会话
     */
    List<ExamSession> findByStartedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据提交时间范围查找考试会话
     */
    List<ExamSession> findBySubmittedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据考试时长范围查找考试会话
     */
    List<ExamSession> findByDurationMinutesBetween(int minDuration, int maxDuration);
    
    /**
     * 根据总分范围查找考试会话
     */
    List<ExamSession> findByTotalScoreBetween(double minScore, double maxScore);
    
    /**
     * 根据客观题得分范围查找考试会话
     */
    List<ExamSession> findByObjectiveScoreBetween(double minScore, double maxScore);
    
    /**
     * 根据主观题得分范围查找考试会话
     */
    List<ExamSession> findBySubjectiveScoreBetween(double minScore, double maxScore);
    
    /**
     * 复杂条件搜索考试会话（分页）
     */
    @Query("SELECT e FROM ExamSession e WHERE " +
           "(:paperId IS NULL OR e.paperId = :paperId) AND " +
           "(:studentId IS NULL OR e.studentId = :studentId) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<ExamSession> findByComplexCriteria(
        @Param("paperId") String paperId,
        @Param("studentId") String studentId,
        @Param("status") ExamSession.SessionStatus status,
        Pageable pageable
    );
    
    /**
     * 根据试卷ID统计考试会话数量
     */
    long countByPaperId(String paperId);
    
    /**
     * 根据学生ID统计考试会话数量
     */
    long countByStudentId(String studentId);
    
    /**
     * 根据状态统计考试会话数量
     */
    long countByStatus(ExamSession.SessionStatus status);
    
    /**
     * 根据考试分配ID和状态统计考试会话数量
     */
    long countByAssignmentIdAndStatus(String assignmentId, ExamSession.SessionStatus status);
    
    /**
     * 根据试卷ID和状态统计考试会话数量
     */
    long countByPaperIdAndStatus(String paperId, ExamSession.SessionStatus status);
    
    /**
     * 根据学生ID和状态统计考试会话数量
     */
    long countByStudentIdAndStatus(String studentId, ExamSession.SessionStatus status);
    
    /**
     * 统计指定时间后开始的考试会话数量
     */
    long countByStartedAtAfter(LocalDateTime dateTime);
    
    /**
     * 根据学生ID查找平均分
     */
    @Query("SELECT AVG(e.totalScore) FROM ExamSession e WHERE e.studentId = :studentId AND e.totalScore IS NOT NULL")
    Double findAverageScoreByStudentId(@Param("studentId") String studentId);
    
    /**
     * 获取所有考试会话（不分页）
     */
    @Query("SELECT e FROM ExamSession e ORDER BY e.startedAt DESC")
    List<ExamSession> findAllExamSessions();
    
    /**
     * 根据试卷ID获取所有考试会话（不分页）
     */
    @Query("SELECT e FROM ExamSession e WHERE e.paperId = :paperId ORDER BY e.startedAt DESC")
    List<ExamSession> findAllExamSessionsByPaperId(@Param("paperId") String paperId);
    
    /**
     * 根据学生ID获取所有考试会话（不分页）
     */
    @Query("SELECT e FROM ExamSession e WHERE e.studentId = :studentId ORDER BY e.startedAt DESC")
    List<ExamSession> findAllExamSessionsByStudentId(@Param("studentId") String studentId);
    
    /**
     * 根据状态获取所有考试会话（不分页）
     */
    @Query("SELECT e FROM ExamSession e WHERE e.status = :status ORDER BY e.startedAt DESC")
    List<ExamSession> findAllExamSessionsByStatus(@Param("status") String status);
    
    /**
     * 检查考试会话是否存在
     */
    boolean existsById(String id);
    
    /**
     * 根据考试分配ID和学生ID检查是否存在
     */
    boolean existsByAssignmentIdAndStudentId(String assignmentId, String studentId);
    
    /**
     * 根据试卷ID和学生ID检查是否存在
     */
    boolean existsByPaperIdAndStudentId(String paperId, String studentId);
    
    /**
     * 获取最新的考试会话
     */
    @Query("SELECT e FROM ExamSession e ORDER BY e.startedAt DESC LIMIT 1")
    ExamSession findLatestExamSession();
    
    /**
     * 根据试卷ID获取最新的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.paperId = :paperId ORDER BY e.startedAt DESC LIMIT 1")
    ExamSession findLatestExamSessionByPaperId(@Param("paperId") String paperId);
    
    /**
     * 根据学生ID获取最新的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.studentId = :studentId ORDER BY e.startedAt DESC LIMIT 1")
    ExamSession findLatestExamSessionByStudentId(@Param("studentId") String studentId);
    
    /**
     * 统计各状态的考试会话数量
     */
    @Query("SELECT e.status, COUNT(e) FROM ExamSession e GROUP BY e.status")
    List<Object[]> countExamSessionsByStatus();
    
    /**
     * 统计各试卷的考试会话数量
     */
    @Query("SELECT e.paperId, COUNT(e) FROM ExamSession e GROUP BY e.paperId")
    List<Object[]> countExamSessionsByPaper();
    
    /**
     * 统计各学生的考试会话数量
     */
    @Query("SELECT e.studentId, COUNT(e) FROM ExamSession e GROUP BY e.studentId")
    List<Object[]> countExamSessionsByStudent();
    
    /**
     * 获取进行中的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.status = 'ONGOING' ORDER BY e.startedAt DESC")
    List<ExamSession> findOngoingExamSessions();
    
    /**
     * 获取已提交的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.status = 'SUBMITTED' ORDER BY e.submittedAt DESC")
    List<ExamSession> findSubmittedExamSessions();
    
    /**
     * 获取待阅卷的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.status = 'SUBMITTED' AND e.subjectiveScore = 0 ORDER BY e.submittedAt DESC")
    List<ExamSession> findPendingGradingExamSessions();
    
    /**
     * 根据试卷ID获取待阅卷的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.paperId = :paperId AND e.status = 'SUBMITTED' AND e.subjectiveScore = 0 ORDER BY e.submittedAt DESC")
    List<ExamSession> findPendingGradingExamSessionsByPaperId(@Param("paperId") String paperId);
    
    /**
     * 根据考试分配ID获取待阅卷的考试会话
     */
    @Query("SELECT e FROM ExamSession e WHERE e.assignmentId = :assignmentId AND e.status = 'SUBMITTED' AND e.subjectiveScore = 0 ORDER BY e.submittedAt DESC")
    List<ExamSession> findPendingGradingExamSessionsByAssignmentId(@Param("assignmentId") String assignmentId);
    
    /**
     * 查找最大的session_id（用于生成递增ID）
     */
    @Query("SELECT MAX(e.sessionId) FROM ExamSession e WHERE e.sessionId LIKE 'ES%'")
    String findMaxSessionId();
    
    /**
     * 根据试卷ID删除所有考试会话
     */
    @Modifying
    @Query("DELETE FROM ExamSession e WHERE e.paperId = :paperId")
    void deleteByPaperId(@Param("paperId") String paperId);
    
    /**
     * 根据ID获取考试会话详情（预加载关联数据）
     */
    @Query("SELECT DISTINCT e FROM ExamSession e " +
           "LEFT JOIN FETCH e.student " +
           "LEFT JOIN FETCH e.paper p " +
           "LEFT JOIN FETCH p.subject " +
           "LEFT JOIN FETCH e.examAssignment ea " +
           "WHERE e.sessionId = :sessionId")
    ExamSession findByIdWithDetails(@Param("sessionId") String sessionId);
    
    /**
     * 获取所有考试会话（预加载关联数据）
     */
    @Query("SELECT DISTINCT e FROM ExamSession e " +
           "LEFT JOIN FETCH e.student " +
           "LEFT JOIN FETCH e.paper p " +
           "LEFT JOIN FETCH p.subject " +
           "LEFT JOIN FETCH e.examAssignment ea " +
           "ORDER BY e.startedAt DESC")
    List<ExamSession> findAllWithDetails();
    
    /**
     * 根据考试分配ID获取学生的最佳成绩
     */
    @Query("SELECT e FROM ExamSession e WHERE e.assignmentId = :assignmentId AND e.isBestScore = true ORDER BY e.totalScore DESC")
    List<ExamSession> findBestScoresByAssignmentId(@Param("assignmentId") String assignmentId);
    
    /**
     * 根据考试分配ID和学生ID获取所有考试会话（按考试次数排序）
     */
    @Query("SELECT e FROM ExamSession e WHERE e.assignmentId = :assignmentId AND e.studentId = :studentId ORDER BY e.attemptNumber ASC")
    List<ExamSession> findSessionsByAssignmentAndStudent(@Param("assignmentId") String assignmentId, @Param("studentId") String studentId);
} 