package com.exam.service.impl;

import com.exam.entity.*;
import com.exam.repository.*;
import com.exam.service.ExamAssignmentService;
import com.exam.service.ExamSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExamAssignmentServiceImpl implements ExamAssignmentService {
    
    @Autowired
    private ExamAssignmentRepository examAssignmentRepository;
    
    @Autowired
    private ExamAssignmentStudentRepository examAssignmentStudentRepository;
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    
    @Autowired
    private PaperRepository paperRepository;
    
    @Override
    public ExamAssignment createAssignment(ExamAssignment assignment, List<String> studentIds) {
        // 生成分配ID
        assignment.setAssignmentId(generateAssignmentId());
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // 保存考试分配
        ExamAssignment savedAssignment = examAssignmentRepository.save(assignment);
        
        // 分配学生
        for (String studentId : studentIds) {
            ExamAssignmentStudent assignmentStudent = new ExamAssignmentStudent();
            assignmentStudent.setId(new ExamAssignmentStudent.ExamAssignmentStudentId(
                savedAssignment.getAssignmentId(), studentId));
            assignmentStudent.setAssignedAt(LocalDateTime.now());
            examAssignmentStudentRepository.save(assignmentStudent);
        }
        
        return savedAssignment;
    }
    
    @Override
    public ExamAssignment createAssignment(ExamAssignment assignment) {
        // 生成分配ID
        if (assignment.getAssignmentId() == null) {
            assignment.setAssignmentId(generateAssignmentId());
        }
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // 保存考试分配
        return examAssignmentRepository.save(assignment);
    }
    
    @Override
    public boolean isStudentAssigned(String assignmentId, String studentId) {
        ExamAssignmentStudent.ExamAssignmentStudentId id = 
            new ExamAssignmentStudent.ExamAssignmentStudentId(assignmentId, studentId);
        return examAssignmentStudentRepository.existsById(id);
    }
    
    @Override
    public void assignToStudent(String assignmentId, String studentId) {
        // 检查是否已经分配
        if (isStudentAssigned(assignmentId, studentId)) {
            throw new RuntimeException("学生 " + studentId + " 已经分配过此考试");
        }
        
        // 创建分配记录
        ExamAssignmentStudent assignmentStudent = new ExamAssignmentStudent();
        assignmentStudent.setId(new ExamAssignmentStudent.ExamAssignmentStudentId(assignmentId, studentId));
        assignmentStudent.setAssignedAt(LocalDateTime.now());
        examAssignmentStudentRepository.save(assignmentStudent);
    }
    
    @Override
    public ExamAssignment updateAssignment(ExamAssignment assignment) {
        assignment.setUpdatedAt(LocalDateTime.now());
        return examAssignmentRepository.save(assignment);
    }
    
    @Override
    public void deleteAssignment(String assignmentId) {
        // 删除相关的考试会话
        List<ExamSession> sessions = examSessionRepository.findByAssignmentId(assignmentId);
        for (ExamSession session : sessions) {
            examSessionRepository.delete(session);
        }
        
        // 删除分配的学生
        examAssignmentStudentRepository.deleteByIdAssignmentId(assignmentId);
        
        // 删除考试分配
        examAssignmentRepository.deleteById(assignmentId);
    }
    
    @Override
    public ExamAssignment getAssignmentById(String assignmentId) {
        return examAssignmentRepository.findById(assignmentId).orElse(null);
    }
    
    @Override
    public List<ExamAssignment> getAssignmentsByTeacherId(String teacherId) {
        return examAssignmentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }
    
    @Override
    public List<ExamAssignment> getAvailableAssignmentsForStudent(String studentId) {
        LocalDateTime now = LocalDateTime.now();
        return examAssignmentStudentRepository.findActiveAssignmentsForStudent(studentId, now)
                .stream()
                .map(ExamAssignmentStudent::getExamAssignment)
                .toList();
    }
    
    @Override
    public List<ExamAssignment> getAssignmentsByStatus(ExamAssignment.AssignmentStatus status) {
        return examAssignmentRepository.findByStatus(status);
    }
    
    @Override
    public List<ExamAssignment> getActiveAssignments() {
        return examAssignmentRepository.findActiveAssignments(LocalDateTime.now());
    }
    
    @Override
    public List<ExamAssignment> getFinishedAssignments() {
        return examAssignmentRepository.findFinishedAssignments(LocalDateTime.now());
    }
    
    @Override
    public ExamSession createExamSessionForStudent(String assignmentId, String studentId) {
        ExamAssignment assignment = getAssignmentById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("考试分配不存在");
        }
        
        // 检查学生是否被分配参加此考试
        if (!examAssignmentStudentRepository.existsByIdAssignmentIdAndIdStudentId(assignmentId, studentId)) {
            throw new RuntimeException("学生未被分配参加此考试");
        }
        
        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(assignment.getExamStartTime())) {
            throw new RuntimeException("考试尚未开始");
        }
        if (now.isAfter(assignment.getExamEndTime())) {
            throw new RuntimeException("考试已结束");
        }
        
        // 检查考试次数限制
        long currentAttempts = examSessionRepository.countByAssignmentIdAndStudentId(assignmentId, studentId);
        if (currentAttempts >= assignment.getMaxAttempts()) {
            throw new RuntimeException("已达到最大考试次数限制");
        }
        
        // 检查是否有进行中的考试会话
        List<ExamSession> ongoingSessions = examSessionRepository.findByAssignmentIdAndStudentIdAndStatus(
            assignmentId, studentId, ExamSession.SessionStatus.ongoing);
        if (!ongoingSessions.isEmpty()) {
            return ongoingSessions.get(0); // 返回现有的进行中会话
        }
        
        // 创建新的考试会话
        ExamSession session = new ExamSession();
        String generatedSessionId = generateSessionId();
        System.out.println("ExamAssignmentService - Generated session ID: " + generatedSessionId);
        
        // 设置所有NOT NULL字段
        session.setSessionId(generatedSessionId);
        session.setAssignmentId(assignmentId);
        session.setPaperId(assignment.getPaperId());
        session.setStudentId(studentId);
        session.setAttemptNumber((int) (currentAttempts + 1));
        session.setDurationMinutes(assignment.getDurationMinutes() != null ? assignment.getDurationMinutes() : 120); // 确保durationMinutes不为null
        session.setStatus(ExamSession.SessionStatus.not_started);
        
        // 设置可选字段
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("ExamAssignmentService - Creating session with ID: " + session.getSessionId() + " for student: " + studentId + " assignment: " + assignmentId);
        ExamSession savedSession = examSessionRepository.save(session);
        System.out.println("ExamAssignmentService - Saved session ID: " + savedSession.getSessionId());
        return savedSession;
    }
    
    @Override
    public boolean canStudentTakeExam(String assignmentId, String studentId) {
        ExamAssignment assignment = getAssignmentById(assignmentId);
        if (assignment == null) {
            return false;
        }
        
        // 检查学生是否被分配
        if (!examAssignmentStudentRepository.existsByIdAssignmentIdAndIdStudentId(assignmentId, studentId)) {
            return false;
        }
        
        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(assignment.getExamStartTime()) || now.isAfter(assignment.getExamEndTime())) {
            return false;
        }
        
        // 检查考试次数
        long currentAttempts = examSessionRepository.countByAssignmentIdAndStudentId(assignmentId, studentId);
        return currentAttempts < assignment.getMaxAttempts();
    }
    
    @Override
    public int getRemainingAttempts(String assignmentId, String studentId) {
        ExamAssignment assignment = getAssignmentById(assignmentId);
        if (assignment == null) {
            return 0;
        }
        
        // 计算所有已完成的考试会话数量（包括已提交和已评阅）
        long submittedAttempts = examSessionRepository.countByAssignmentIdAndStudentIdAndStatus(
            assignmentId, studentId, ExamSession.SessionStatus.submitted);
        long gradedAttempts = examSessionRepository.countByAssignmentIdAndStudentIdAndStatus(
            assignmentId, studentId, ExamSession.SessionStatus.graded);
        
        int totalCompletedAttempts = (int) (submittedAttempts + gradedAttempts);
        return Math.max(0, assignment.getMaxAttempts() - totalCompletedAttempts);
    }
    
    @Override
    public ExamSession getBestScoreForStudent(String assignmentId, String studentId) {
        return examSessionRepository.findByAssignmentIdAndStudentIdAndIsBestScoreTrue(assignmentId, studentId);
    }
    
    @Override
    public List<ExamSession> getAllScoresForStudent(String assignmentId, String studentId) {
        return examSessionRepository.findSessionsByAssignmentAndStudent(assignmentId, studentId);
    }
    
    @Override
    public void autoSubmitTimeoutSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<ExamSession> ongoingSessions = examSessionRepository.findOngoingExamSessions();
        
        for (ExamSession session : ongoingSessions) {
            if (session.getStartedAt() != null) {
                LocalDateTime endTime = session.getStartedAt().plusMinutes(session.getDurationMinutes());
                if (now.isAfter(endTime)) {
                    // 自动提交超时的考试
                    session.setStatus(ExamSession.SessionStatus.submitted);
                    session.setSubmittedAt(now);
                    session.setAutoSubmitted(true);
                    session.setUpdatedAt(now);
                    examSessionRepository.save(session);
                }
            }
        }
    }
    
    @Override
    public void updateAssignmentStatus(String assignmentId, ExamAssignment.AssignmentStatus status) {
        ExamAssignment assignment = getAssignmentById(assignmentId);
        if (assignment != null) {
            assignment.setStatus(status);
            assignment.setUpdatedAt(LocalDateTime.now());
            examAssignmentRepository.save(assignment);
        }
    }
    
    @Override
    public Page<ExamAssignment> getAssignmentsPage(Pageable pageable) {
        return examAssignmentRepository.findAll(pageable);
    }
    
    @Override
    public Page<ExamAssignment> searchAssignments(String teacherId, String paperId, 
                                                ExamAssignment.AssignmentStatus status, 
                                                LocalDateTime startTime, LocalDateTime endTime, 
                                                Pageable pageable) {
        // 这里可以实现更复杂的搜索逻辑
        return examAssignmentRepository.findAll(pageable);
    }
    
    // 生成分配ID
    private String generateAssignmentId() {
        // 查询数据库中最大的assignment_id，生成下一个递增ID
        String maxAssignmentId = examAssignmentRepository.findMaxAssignmentId();
        int nextNumber = 1;
        
        if (maxAssignmentId != null && maxAssignmentId.startsWith("EA")) {
            try {
                String numberPart = maxAssignmentId.substring(2); // 去掉"EA"前缀
                nextNumber = Integer.parseInt(numberPart) + 1;
                
                // 确保不超过999，避免超出12位限制
                if (nextNumber > 999) {
                    nextNumber = 1; // 重新从1开始，或者可以抛出异常
                    System.out.println("警告：assignment ID已达到最大值，重新从EA001开始");
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextNumber = 1;
            }
        }
        
        return String.format("EA%03d", nextNumber);
    }
    
    // 生成会话ID
    private String generateSessionId() {
        // 查询数据库中最大的session_id，生成下一个递增ID
        String maxSessionId = examSessionRepository.findMaxSessionId();
        int nextNumber = 1;
        
        if (maxSessionId != null && maxSessionId.startsWith("ES")) {
            try {
                String numberPart = maxSessionId.substring(2); // 去掉"ES"前缀
                nextNumber = Integer.parseInt(numberPart) + 1;
                
                // 确保不超过999，避免超出12位限制
                if (nextNumber > 999) {
                    nextNumber = 1; // 重新从1开始，或者可以抛出异常
                    System.out.println("警告：session ID已达到最大值，重新从ES001开始");
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextNumber = 1;
            }
        }
        
        return String.format("ES%03d", nextNumber);
    }
}
