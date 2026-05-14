package com.exam.service;

import com.exam.entity.ExamAssignment;
import com.exam.entity.ExamAssignmentStudent;
import com.exam.entity.ExamSession;
import com.exam.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamAssignmentService {
    
    /**
     * 创建考试分配
     */
    ExamAssignment createAssignment(ExamAssignment assignment, List<String> studentIds);
    
    /**
     * 创建考试分配（不分配学生）
     */
    ExamAssignment createAssignment(ExamAssignment assignment);
    
    /**
     * 更新考试分配
     */
    ExamAssignment updateAssignment(ExamAssignment assignment);
    
    /**
     * 删除考试分配
     */
    void deleteAssignment(String assignmentId);
    
    /**
     * 根据ID获取考试分配
     */
    ExamAssignment getAssignmentById(String assignmentId);
    
    /**
     * 根据教师ID获取考试分配列表
     */
    List<ExamAssignment> getAssignmentsByTeacherId(String teacherId);
    
    /**
     * 根据学生ID获取可参加的考试分配
     */
    List<ExamAssignment> getAvailableAssignmentsForStudent(String studentId);
    
    /**
     * 根据状态获取考试分配列表
     */
    List<ExamAssignment> getAssignmentsByStatus(ExamAssignment.AssignmentStatus status);
    
    /**
     * 获取正在进行的考试分配
     */
    List<ExamAssignment> getActiveAssignments();
    
    /**
     * 获取已结束的考试分配
     */
    List<ExamAssignment> getFinishedAssignments();
    
    /**
     * 为学生创建考试会话
     */
    ExamSession createExamSessionForStudent(String assignmentId, String studentId);
    
    /**
     * 检查学生是否可以参加考试
     */
    boolean canStudentTakeExam(String assignmentId, String studentId);
    
    /**
     * 获取学生在该考试中的剩余考试次数
     */
    int getRemainingAttempts(String assignmentId, String studentId);
    
    /**
     * 获取学生在该考试中的最佳成绩
     */
    ExamSession getBestScoreForStudent(String assignmentId, String studentId);
    
    /**
     * 获取学生在该考试中的所有成绩
     */
    List<ExamSession> getAllScoresForStudent(String assignmentId, String studentId);
    
    /**
     * 自动提交超时的考试会话
     */
    void autoSubmitTimeoutSessions();
    
    /**
     * 更新考试分配状态
     */
    void updateAssignmentStatus(String assignmentId, ExamAssignment.AssignmentStatus status);
    
    /**
     * 分页获取考试分配
     */
    Page<ExamAssignment> getAssignmentsPage(Pageable pageable);
    
    /**
     * 根据条件搜索考试分配
     */
    Page<ExamAssignment> searchAssignments(String teacherId, String paperId, 
                                         ExamAssignment.AssignmentStatus status, 
                                         LocalDateTime startTime, LocalDateTime endTime, 
                                         Pageable pageable);
    
    /**
     * 检查学生是否已分配考试
     */
    boolean isStudentAssigned(String assignmentId, String studentId);
    
    /**
     * 为学生分配考试
     */
    void assignToStudent(String assignmentId, String studentId);
}
