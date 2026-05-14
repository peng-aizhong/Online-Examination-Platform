package com.exam.service.impl;

import com.exam.entity.*;
import com.exam.repository.*;
import com.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.*;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamSessionServiceImpl implements ExamSessionService {
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    @Autowired
    private ExamSessionAnswerRepository examSessionAnswerRepository;
    
    @Autowired
    private ExamAssignmentStudentRepository examAssignmentStudentRepository;
    
    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExamAssignmentRepository examAssignmentRepository;
    
    @Autowired
    @Lazy
    private ExamAssignmentService examAssignmentService;
    
        @Override
    public ExamSession createExamSession(String paperId, String studentId) {
        // 基于新的ExamAssignment结构创建考试会话
        // 首先找到学生被分配的相关考试
        List<ExamAssignment> assignments = examAssignmentService.getAvailableAssignmentsForStudent(studentId);
        ExamAssignment targetAssignment = null;
        
        for (ExamAssignment assignment : assignments) {
            if (paperId.equals(assignment.getPaperId())) {
                targetAssignment = assignment;
                break;
            }
        }
        
        if (targetAssignment == null) {
            throw new RuntimeException("学生未被分配参加此考试");
        }
        
        // 检查学生是否还有剩余考试次数
        int remainingAttempts = examAssignmentService.getRemainingAttempts(targetAssignment.getAssignmentId(), studentId);
        if (remainingAttempts <= 0) {
            throw new RuntimeException("学生已达到最大考试次数限制");
        }
        
        // 检查是否已存在该学生和分配的考试会话
        List<ExamSession> existingSessions = examSessionRepository.findByAssignmentIdAndStudentIdAndStatus(
            targetAssignment.getAssignmentId(), studentId, ExamSession.SessionStatus.ongoing);
        
        if (existingSessions != null && !existingSessions.isEmpty()) {
            // 如果已存在进行中的会话，返回现有会话
            ExamSession existingSession = existingSessions.get(0);
            System.out.println("Returning existing ongoing session: " + existingSession.getSessionId());
            return existingSession;
        }
        
        // 获取试卷信息
        Paper paper = paperRepository.findById(paperId)
            .orElseThrow(() -> new RuntimeException("试卷不存在"));
        
        // 计算这是第几次尝试
        int attemptNumber = (int) examSessionRepository.countByAssignmentIdAndStudentId(targetAssignment.getAssignmentId(), studentId) + 1;
        
        // 创建新的考试会话
        ExamSession session = new ExamSession();
        String generatedSessionId = generateSessionId();
        System.out.println("Generated session ID: " + generatedSessionId);
        
        // 设置所有NOT NULL字段
        session.setSessionId(generatedSessionId);
        session.setAssignmentId(targetAssignment.getAssignmentId());
        session.setPaperId(paperId);
        session.setStudentId(studentId);
        session.setAttemptNumber(attemptNumber);
        // 优先使用ExamAssignment中的durationMinutes，这是教师端设置的实际考试时长
        session.setDurationMinutes(targetAssignment.getDurationMinutes() != null ? targetAssignment.getDurationMinutes() : 
                                  (paper.getDuration() != null ? paper.getDuration() : 120));
        session.setStatus(ExamSession.SessionStatus.ongoing);
        
        // 设置可选字段
        session.setStartedAt(LocalDateTime.now());
        session.setObjectiveScore(0.0);
        session.setSubjectiveScore(0.0);
        session.setTotalScore(0.0);
        session.setIsBestScore(false);
        session.setAutoSubmitted(false);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Creating new exam session: " + session.getSessionId() + " for student: " + studentId + " paper: " + paperId + " assignment: " + targetAssignment.getAssignmentId() + " attempt: " + attemptNumber);
        ExamSession savedSession = examSessionRepository.save(session);
        System.out.println("Saved session ID: " + savedSession.getSessionId());
        return savedSession;
    }
    
    @Override
    public ExamSession createExamSessionFromAssignment(String assignmentId, String studentId) {
        // 这里可以调用ExamAssignmentService的方法
        // 暂时返回null，实际实现需要注入ExamAssignmentService
        throw new UnsupportedOperationException("此方法需要ExamAssignmentService支持");
    }
    
    @Override
    public ExamSession getSessionById(String sessionId) {
        try {
            return examSessionRepository.findById(sessionId).orElse(null);
        } catch (Exception e) {
            System.err.println("获取考试会话失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<ExamSession> getSessionsByStatus(ExamSession.SessionStatus status) {
        return examSessionRepository.findByStatus(status);
    }
    
    @Override
    public ExamSession updateSession(ExamSession session) {
        return examSessionRepository.save(session);
    }
    
    @Override
    public void deleteSession(String sessionId) {
        // 删除相关答案
        examSessionAnswerRepository.deleteBySessionId(sessionId);
        // 删除考试会话
        examSessionRepository.deleteById(sessionId);
    }
    
    @Override
    public ExamSession startExam(String sessionId) {
        ExamSession session = getSessionById(sessionId);
        if (!ExamSession.SessionStatus.not_started.equals(session.getStatus()) && 
            !ExamSession.SessionStatus.ongoing.equals(session.getStatus())) {
            throw new RuntimeException("考试状态不允许开始");
        }
        
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(ExamSession.SessionStatus.ongoing);
        session.setUpdatedAt(LocalDateTime.now());
        
        return examSessionRepository.save(session);
    }
    
    @Override
    public ExamSession submitExam(String sessionId) {
        ExamSession session = getSessionById(sessionId);
        
        // 自动评分客观题
        autoGradeObjectiveQuestions(sessionId);
        
        // 更新考试状态
        session.setStatus(ExamSession.SessionStatus.submitted);
        session.setSubmittedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算总分（包括客观题和主观题）
        updateSessionTotalScore(sessionId);
        
        // 保存会话
        ExamSession savedSession = examSessionRepository.save(session);
        
        // 更新最佳成绩标记（通过数据库触发器自动处理）
        updateBestScoreMark(session.getAssignmentId(), session.getStudentId());
        
        return savedSession;
    }
    
    /**
     * 更新最佳成绩标记
     */
    private void updateBestScoreMark(String assignmentId, String studentId) {
        if (assignmentId != null && studentId != null) {
            // 获取该学生在该考试分配中的所有已提交会话
            List<ExamSession> sessions = examSessionRepository.findByAssignmentIdAndStudentIdAndStatus(
                assignmentId, studentId, ExamSession.SessionStatus.submitted);
            
            if (!sessions.isEmpty()) {
                // 找到最高分
                ExamSession bestSession = sessions.stream()
                    .max(Comparator.comparing(ExamSession::getTotalScore))
                    .orElse(null);
                
                if (bestSession != null) {
                    // 将所有会话的isBestScore设为false
                    sessions.forEach(session -> {
                        session.setIsBestScore(false);
                        examSessionRepository.save(session);
                    });
                    
                    // 将最高分的会话设为最佳成绩
                    bestSession.setIsBestScore(true);
                    examSessionRepository.save(bestSession);
                }
            }
        }
    }
    
    @Override
    public void saveAnswer(String sessionId, String questionId, String answer) {
        try {
            System.out.println("ExamSessionService.saveAnswer - sessionId: " + sessionId + ", questionId: " + questionId + ", answer: " + answer);
            
            // 验证NOT NULL字段
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new IllegalArgumentException("sessionId不能为空");
            }
            if (questionId == null || questionId.trim().isEmpty()) {
                throw new IllegalArgumentException("questionId不能为空");
            }
            
            // 验证questionId是否存在
            boolean questionExists = questionRepository.existsById(questionId);
            System.out.println("Question exists check - questionId: " + questionId + ", exists: " + questionExists);
            
            if (!questionExists) {
                System.err.println("Question not found: " + questionId);
                throw new IllegalArgumentException("题目不存在: " + questionId);
            }
            
            // 验证sessionId是否存在
            boolean sessionExists = examSessionRepository.existsById(sessionId);
            System.out.println("Session exists check - sessionId: " + sessionId + ", exists: " + sessionExists);
            
            if (!sessionExists) {
                System.err.println("Session not found: " + sessionId);
                throw new IllegalArgumentException("考试会话不存在: " + sessionId);
            }
            
            ExamSessionAnswer.ExamSessionAnswerId answerId = new ExamSessionAnswer.ExamSessionAnswerId();
            answerId.setSessionId(sessionId.trim());
            answerId.setQuestionId(questionId.trim());
            
            // 查找现有答案
            ExamSessionAnswer sessionAnswer = examSessionAnswerRepository.findById(answerId).orElse(null);
            
            if (sessionAnswer == null) {
                // 创建新答案
                sessionAnswer = new ExamSessionAnswer();
                sessionAnswer.setId(answerId);
                sessionAnswer.setAnsweredAt(java.time.LocalDateTime.now());
            } else {
                // 更新现有答案的答题时间
                sessionAnswer.setAnsweredAt(java.time.LocalDateTime.now());
            }
            
            sessionAnswer.setAnswerText(answer);
            sessionAnswer.setScore(0.0); // 初始分数为0，需要评分
            
            examSessionAnswerRepository.save(sessionAnswer);
            System.out.println("答案保存成功");
        } catch (Exception e) {
            System.err.println("ExamSessionService.saveAnswer 失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public List<Paper> getAvailableExams(String studentId) {
        // 基于新的ExamAssignment结构获取学生可参加的考试
        List<ExamAssignment> assignments = examAssignmentService.getAvailableAssignmentsForStudent(studentId);
        
        // 获取这些试卷的详细信息
        List<Paper> availablePapers = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (ExamAssignment assignment : assignments) {
            // 检查考试是否在有效时间范围内
            if (now.isAfter(assignment.getExamStartTime()) && now.isBefore(assignment.getExamEndTime())) {
                Paper paper = paperRepository.findById(assignment.getPaperId()).orElse(null);
                if (paper != null && "启用".equals(paper.getStatus())) {
                    // 检查学生是否还有剩余考试次数
                    int remainingAttempts = examAssignmentService.getRemainingAttempts(assignment.getAssignmentId(), studentId);
                    if (remainingAttempts > 0) {
                        availablePapers.add(paper);
                    }
                }
            }
        }
        
        return availablePapers;
    }
    
    @Override
    public List<ExamSession> getActiveExams(String studentId) {
        return examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.ongoing);
    }
    
    @Override
    public List<ExamSession> getActiveSessionsForStudent(String studentId) {
        return examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.ongoing);
    }
    
    @Override
    public List<ExamSession> getHistoryExams(String studentId) {
        // 获取所有已完成的考试（包括已提交和已评阅）
        List<ExamSession> submittedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.submitted);
        List<ExamSession> gradedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.graded);
        
        List<ExamSession> allHistoryExams = new ArrayList<>();
        allHistoryExams.addAll(submittedExams);
        allHistoryExams.addAll(gradedExams);
        
        // 按提交时间倒序排列
        allHistoryExams.sort((a, b) -> {
            LocalDateTime timeA = a.getSubmittedAt() != null ? a.getSubmittedAt() : a.getCreatedAt();
            LocalDateTime timeB = b.getSubmittedAt() != null ? b.getSubmittedAt() : b.getCreatedAt();
            return timeB.compareTo(timeA);
        });
        
        return allHistoryExams;
    }
    
    @Override
    public List<ExamSession> getStudentAllSessions(String studentId) {
        return examSessionRepository.findByStudentId(studentId);
    }
    
    @Override
    public Page<ExamSession> getHistoryExams(String studentId, Pageable pageable) {
        // 获取所有已完成的考试（包括已提交和已评阅）
        List<ExamSession> submittedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.submitted);
        List<ExamSession> gradedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.graded);
        
        List<ExamSession> allHistoryExams = new ArrayList<>();
        allHistoryExams.addAll(submittedExams);
        allHistoryExams.addAll(gradedExams);
        
        // 按提交时间倒序排列
        allHistoryExams.sort((a, b) -> {
            LocalDateTime timeA = a.getSubmittedAt() != null ? a.getSubmittedAt() : a.getCreatedAt();
            LocalDateTime timeB = b.getSubmittedAt() != null ? b.getSubmittedAt() : b.getCreatedAt();
            return timeB.compareTo(timeA);
        });
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allHistoryExams.size());
        List<ExamSession> pageContent = allHistoryExams.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allHistoryExams.size());
    }
    
    @Override
    public List<ExamSessionAnswer> getSessionAnswers(String sessionId) {
        return examSessionAnswerRepository.findBySessionId(sessionId);
    }
    
    @Override
    public List<Question> getSessionQuestions(String sessionId) {
        try {
            System.out.println("=== 获取考试题目 ===");
            System.out.println("SessionId: " + sessionId);
            
            ExamSession session = getSessionById(sessionId);
            if (session == null) {
                System.err.println("考试会话不存在: " + sessionId);
                return new ArrayList<>();
            }
            
            System.out.println("PaperId: " + session.getPaperId());
            
            List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(session.getPaperId());
            System.out.println("试卷题目关联数量: " + (paperQuestions != null ? paperQuestions.size() : 0));
            
            if (paperQuestions == null || paperQuestions.isEmpty()) {
                System.err.println("试卷中没有题目关联");
                return new ArrayList<>();
            }
            
            // 使用Set去重，避免重复题目
            Set<String> processedQuestionIds = new HashSet<>();
            List<Question> questions = new ArrayList<>();
            
            for (PaperQuestion pq : paperQuestions) {
                String questionId = pq.getQuestionId();
                
                // 跳过已处理的题目ID
                if (processedQuestionIds.contains(questionId)) {
                    continue;
                }
                
                Optional<Question> questionOpt = questionRepository.findById(questionId);
                if (questionOpt.isPresent()) {
                    Question question = questionOpt.get();
                    questions.add(question);
                    processedQuestionIds.add(questionId);
                }
            }
            
            System.out.println("最终获取到题目数量: " + questions.size());
            return questions;
        } catch (Exception e) {
            System.err.println("获取考试题目失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public void autoGradeObjectiveQuestions(String sessionId) {
        List<ExamSessionAnswer> answers = getSessionAnswers(sessionId);
        double totalObjectiveScore = 0.0;
        
        for (ExamSessionAnswer answer : answers) {
            Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            if (question != null && isObjectiveQuestion(question.getQuestionType())) {
                // 获取该题在试卷中的分值
                PaperQuestion.PaperQuestionId paperQuestionId = new PaperQuestion.PaperQuestionId();
                paperQuestionId.setPaperId(getSessionById(sessionId).getPaperId());
                paperQuestionId.setQuestionId(question.getId());
                
                PaperQuestion paperQuestion = paperQuestionRepository.findById(paperQuestionId).orElse(null);
                int questionScore = paperQuestion != null ? paperQuestion.getScore() : 0;
                
                // 自动评分
                double score = autoGradeAnswer(question, answer.getAnswerText(), questionScore);
                answer.setScore(score);
                totalObjectiveScore += score;
                
                examSessionAnswerRepository.save(answer);
            }
        }
        
        // 更新考试会话的客观题分数
        ExamSession session = getSessionById(sessionId);
        session.setObjectiveScore(totalObjectiveScore);
        examSessionRepository.save(session);
    }
    
    @Override
    public Map<String, Object> getStudentStatistics(String studentId) {
        Map<String, Object> statistics = new HashMap<>();
        
        long totalExams = getTotalExamsByStudent(studentId);
        long completedExams = getCompletedExamsByStudent(studentId);
        long activeExams = getActiveExamsByStudent(studentId);
        double averageScore = getAverageScoreByStudent(studentId);
        long excellentScores = getExcellentScoresByStudent(studentId);
        
        // 获取可参加的考试数量（分配给学生的考试）
        long availableExams = getAvailableExamsByStudent(studentId);
        
        statistics.put("totalExams", totalExams);
        statistics.put("completedExams", completedExams);
        statistics.put("activeExams", activeExams);
        statistics.put("availableExams", availableExams); // 新增：可参加的考试
        statistics.put("averageScore", averageScore);
        statistics.put("excellentScores", excellentScores);
        
        return statistics;
    }
    
    @Override
    public long getTotalExamsByStudent(String studentId) {
        return examSessionRepository.countByStudentId(studentId);
    }
    
    @Override
    public long getCompletedExamsByStudent(String studentId) {
        // 统计所有已完成的考试（包括已提交和已评阅）
        long submittedCount = examSessionRepository.countByStudentIdAndStatus(studentId, ExamSession.SessionStatus.submitted);
        long gradedCount = examSessionRepository.countByStudentIdAndStatus(studentId, ExamSession.SessionStatus.graded);
        return submittedCount + gradedCount;
    }
    
    @Override
    public long getActiveExamsByStudent(String studentId) {
        return examSessionRepository.countByStudentIdAndStatus(studentId, ExamSession.SessionStatus.ongoing);
    }
    
    @Override
    public long getAvailableExamsByStudent(String studentId) {
        // 获取分配给学生的考试数量，统计所有分配给学生的考试（不管是否有剩余次数）
        LocalDateTime now = LocalDateTime.now();
        List<ExamAssignmentStudent> activeAssignments = examAssignmentStudentRepository.findActiveAssignmentsForStudent(studentId, now);
        return activeAssignments.size();
    }
    
    @Override
    public double getAverageScoreByStudent(String studentId) {
        // 获取所有已完成的考试（包括已提交和已评阅）
        List<ExamSession> submittedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.submitted);
        List<ExamSession> gradedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.graded);
        
        List<ExamSession> allFinishedExams = new ArrayList<>();
        allFinishedExams.addAll(submittedExams);
        allFinishedExams.addAll(gradedExams);
        
        if (allFinishedExams.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = allFinishedExams.stream()
                .mapToDouble(session -> session.getTotalScore() != null ? session.getTotalScore() : 0.0)
                .sum();
        
        return totalScore / allFinishedExams.size();
    }
    
    @Override
    public long getExcellentScoresByStudent(String studentId) {
        // 获取所有已完成的考试（包括已提交和已评阅）
        List<ExamSession> submittedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.submitted);
        List<ExamSession> gradedExams = examSessionRepository.findByStudentIdAndStatus(studentId, ExamSession.SessionStatus.graded);
        
        List<ExamSession> allFinishedExams = new ArrayList<>();
        allFinishedExams.addAll(submittedExams);
        allFinishedExams.addAll(gradedExams);
        
        return allFinishedExams.stream()
                .filter(session -> session.getTotalScore() != null && session.getTotalScore() >= 90.0)
                .count();
    }
    
    @Override
    public boolean canStudentTakeExam(String studentId, String paperId) {
        // 检查是否已经参加过该考试（只有状态为"SUBMITTED"的才算已参加）
        ExamSession existingSession = examSessionRepository.findByPaperIdAndStudentId(paperId, studentId);
        if (existingSession == null) {
            return true; // 没有考试会话，可以参加
        }
        
        // 如果考试会话存在，检查状态
        ExamSession.SessionStatus status = existingSession.getStatus();
        if (ExamSession.SessionStatus.submitted.equals(status)) {
            return false; // 已完成，不能再次参加
        } else if (ExamSession.SessionStatus.not_started.equals(status) || ExamSession.SessionStatus.ongoing.equals(status)) {
            return true; // 未开始或进行中，可以参加
        } else {
            return false; // 其他状态不允许参加
        }
    }
    
    @Override
    public ExamSession getExistingSession(String studentId, String paperId) {
        return examSessionRepository.findByPaperIdAndStudentId(paperId, studentId);
    }
    
    @Override
    public ExamSession getExistingSessionByAssignment(String studentId, String assignmentId) {
        List<ExamSession> sessions = examSessionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        // 返回最新的会话（按创建时间排序）
        return sessions.stream()
                .max(Comparator.comparing(ExamSession::getCreatedAt))
                .orElse(null);
    }
    
    @Override
    public boolean isExamTimeout(String sessionId) {
        ExamSession session = getSessionById(sessionId);
        LocalDateTime endTime = session.getStartedAt().plusMinutes(session.getDurationMinutes());
        return LocalDateTime.now().isAfter(endTime);
    }
    
    @Override
    public int getRemainingTime(String sessionId) {
        ExamSession session = getSessionById(sessionId);
        LocalDateTime endTime = session.getStartedAt().plusMinutes(session.getDurationMinutes());
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(endTime)) {
            return 0;
        }
        
        return (int) java.time.Duration.between(now, endTime).toMinutes();
    }
    
    @Override
    public String generateSessionId() {
        String maxId = examSessionRepository.findMaxSessionId();
        System.out.println("Max session ID found: " + maxId);
        int nextNumber = 1;
        
        if (maxId != null && maxId.startsWith("ES")) {
            try {
                String numberPart = maxId.substring(2); // 去掉"ES"前缀
                nextNumber = Integer.parseInt(numberPart) + 1;
                System.out.println("Parsed number part: " + numberPart + ", next number: " + nextNumber);
                
                // 确保不超过999，避免超出12位限制
                if (nextNumber > 999) {
                    nextNumber = 1; // 重新从1开始
                    System.out.println("警告：session ID已达到最大值，重新从ES001开始");
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextNumber = 1;
                System.out.println("解析session ID失败，从1开始: " + e.getMessage());
            }
        } else {
            System.out.println("No existing session IDs found, starting from 1");
        }
        
        String generatedId = String.format("ES%03d", nextNumber);
        System.out.println("Generated session ID: " + generatedId);
        return generatedId;
    }
    
    @Override
    public Page<ExamSession> getStudentGrades(String studentName, String paperId, String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        
        // 构建查询条件
        Specification<ExamSession> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只查询已完成的考试会话
            predicates.add(cb.or(
                cb.equal(root.get("status"), ExamSession.SessionStatus.submitted),
                cb.equal(root.get("status"), ExamSession.SessionStatus.graded)
            ));
            
            // 学生姓名筛选
            if (studentName != null && !studentName.trim().isEmpty()) {
                Join<ExamSession, User> studentJoin = root.join("student", JoinType.INNER);
                predicates.add(cb.like(cb.lower(studentJoin.get("username")), 
                    "%" + studentName.toLowerCase() + "%"));
            }
            
            // 试卷筛选
            if (paperId != null && !paperId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("paperId"), paperId));
            }
            
            // 日期范围筛选
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), start));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    predicates.add(cb.lessThanOrEqualTo(root.get("submittedAt"), end));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return examSessionRepository.findAll(spec, pageable);
    }
    
    @Override
    public Map<String, Object> getGradeStatistics(String studentName, String paperId, String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 构建查询条件（与getStudentGrades相同）
        Specification<ExamSession> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.or(
                cb.equal(root.get("status"), ExamSession.SessionStatus.submitted),
                cb.equal(root.get("status"), ExamSession.SessionStatus.graded)
            ));
            
            if (studentName != null && !studentName.trim().isEmpty()) {
                Join<ExamSession, User> studentJoin = root.join("student", JoinType.INNER);
                predicates.add(cb.like(cb.lower(studentJoin.get("username")), 
                    "%" + studentName.toLowerCase() + "%"));
            }
            
            if (paperId != null && !paperId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("paperId"), paperId));
            }
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), start));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    predicates.add(cb.lessThanOrEqualTo(root.get("submittedAt"), end));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        List<ExamSession> sessions = examSessionRepository.findAll(spec);
        
        // 计算统计信息
        int totalCount = sessions.size();
        double averageScore = 0.0;
        double maxScore = 0.0;
        double minScore = Double.MAX_VALUE;
        int passCount = 0; // 及格人数（假设60分及格）
        
        if (!sessions.isEmpty()) {
            double totalScore = 0.0;
            for (ExamSession session : sessions) {
                Double score = session.getTotalScore();
                if (score != null) {
                    totalScore += score;
                    maxScore = Math.max(maxScore, score);
                    minScore = Math.min(minScore, score);
                    if (score >= 60.0) {
                        passCount++;
                    }
                }
            }
            averageScore = totalScore / totalCount;
            if (minScore == Double.MAX_VALUE) {
                minScore = 0.0;
            }
        }
        
        statistics.put("totalCount", totalCount);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        statistics.put("passCount", passCount);
        statistics.put("passRate", totalCount > 0 ? Math.round((double) passCount / totalCount * 10000.0) / 100.0 : 0.0);
        
        return statistics;
    }
    
    // 辅助方法
    private boolean isObjectiveQuestion(String questionType) {
        return "C".equals(questionType) || "T".equals(questionType) || "F".equals(questionType) || "R".equals(questionType); // 选择题、判断题、填空题、程序运行结果题
    }
    
    private double autoGradeAnswer(Question question, String studentAnswer, int maxScore) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return 0.0;
        }
        
        String correctAnswer = question.getAnswer();
        if (correctAnswer == null) {
            return 0.0;
        }
        
        String questionType = question.getQuestionType();
        
        // 根据题目类型进行不同的评分逻辑
        if ("C".equals(questionType)) {
            // 选择题：比较选项编号
            return studentAnswer.trim().equals(correctAnswer.trim()) ? maxScore : 0.0;
        } else if ("T".equals(questionType)) {
            // 判断题：比较答案文本
            return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim()) ? maxScore : 0.0;
        } else if ("F".equals(questionType)) {
            // 填空题：比较答案文本（忽略大小写和前后空格）
            return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim()) ? maxScore : 0.0;
        } else if ("R".equals(questionType)) {
            // 程序运行结果题：比较输出结果（忽略大小写和前后空格）
            return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim()) ? maxScore : 0.0;
        } else {
            // 其他类型题目暂时不自动评分
            return 0.0;
        }
    }
    
    @Override
    public List<ExamSession> getSessionsForGrading() {
        // 获取状态为SUBMITTED且有主观题需要评分的考试会话
        List<ExamSession> finishedSessions = examSessionRepository.findByStatus(ExamSession.SessionStatus.submitted);
        List<ExamSession> sessionsForGrading = new ArrayList<>();
        
        for (ExamSession session : finishedSessions) {
            // 检查是否有主观题未评分
            if (hasSubjectiveQuestionsNeedingGrading(session.getSessionId())) {
                sessionsForGrading.add(session);
            }
        }
        
        return sessionsForGrading;
    }
    
    @Override
    public List<ExamSession> getGradedSessions() {
        // 直接获取状态为graded的考试会话
        return examSessionRepository.findByStatus(ExamSession.SessionStatus.graded);
    }
    
    @Override
    public void updateAnswerScore(String sessionId, String questionId, Double score) {
        ExamSessionAnswer.ExamSessionAnswerId answerId = new ExamSessionAnswer.ExamSessionAnswerId();
        answerId.setSessionId(sessionId);
        answerId.setQuestionId(questionId);
        
        ExamSessionAnswer answer = examSessionAnswerRepository.findById(answerId).orElse(null);
        if (answer != null) {
            answer.setScore(score);
            examSessionAnswerRepository.save(answer);
        }
    }
    
    @Override
    public void updateAnswerFeedback(String sessionId, String questionId, String feedback) {
        ExamSessionAnswer.ExamSessionAnswerId answerId = new ExamSessionAnswer.ExamSessionAnswerId();
        answerId.setSessionId(sessionId);
        answerId.setQuestionId(questionId);
        
        ExamSessionAnswer answer = examSessionAnswerRepository.findById(answerId).orElse(null);
        if (answer != null) {
            answer.setFeedback(feedback);
            examSessionAnswerRepository.save(answer);
        }
    }
    
    @Override
    public void updateSessionStatus(String sessionId, ExamSession.SessionStatus status) {
        ExamSession session = getSessionById(sessionId);
        if (session != null) {
            session.setStatus(status);
            session.setUpdatedAt(LocalDateTime.now());
            examSessionRepository.save(session);
        }
    }
    
    @Override
    public void updateSessionTotalScore(String sessionId) {
        ExamSession session = getSessionById(sessionId);
        if (session != null) {
            List<ExamSessionAnswer> answers = getSessionAnswers(sessionId);
            
            double objectiveScore = 0.0;
            double subjectiveScore = 0.0;
            
            // 批量获取所有题目信息
            Set<String> questionIds = answers.stream()
                    .map(ExamSessionAnswer::getQuestionId)
                    .collect(Collectors.toSet());
            
            Map<String, Question> questionMap = questionRepository.findAllById(questionIds)
                    .stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));
            
            for (ExamSessionAnswer answer : answers) {
                Question question = questionMap.get(answer.getQuestionId());
                if (question != null) {
                    double score = answer.getScore() != null ? answer.getScore() : 0.0;
                    if (isObjectiveQuestion(question.getQuestionType())) {
                        objectiveScore += score;
                    } else {
                        subjectiveScore += score;
                    }
                }
            }
            
            session.setObjectiveScore(objectiveScore);
            session.setSubjectiveScore(subjectiveScore);
            session.calculateTotalScore();
            session.setUpdatedAt(LocalDateTime.now());
            
            examSessionRepository.save(session);
        }
    }
    
    /**
     * 检查是否有主观题需要评分
     */
    @Override
    public boolean hasSubjectiveQuestionsNeedingGrading(String sessionId) {
        List<ExamSessionAnswer> answers = getSessionAnswers(sessionId);
        
        for (ExamSessionAnswer answer : answers) {
            Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            if (question != null && !isObjectiveQuestion(question.getQuestionType())) {
                // 主观题且分数为null，说明需要评分（分数为0是有效的评分结果）
                if (answer.getScore() == null) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public long getExamCountByTeacher(String teacherId) {
        // 统计教师创建的考试分配数量
        return examAssignmentRepository.countByTeacherId(teacherId);
    }
    
    @Override
    public long getGradingCountByTeacher(String teacherId) {
        // 统计需要该教师阅卷的考试会话数量
        // 获取该教师创建的试卷
        List<Paper> teacherPapers = paperRepository.findByCreatorId(teacherId);
        if (teacherPapers.isEmpty()) {
            return 0L;
        }
        
        // 获取这些试卷的考试会话中需要阅卷的数量
        long gradingCount = 0L;
        for (Paper paper : teacherPapers) {
            List<ExamSession> sessions = examSessionRepository.findByPaperIdAndStatus(
                paper.getPaperId(), ExamSession.SessionStatus.submitted);
            
            for (ExamSession session : sessions) {
                if (hasSubjectiveQuestionsNeedingGrading(session.getSessionId())) {
                    gradingCount++;
                }
            }
        }
        
        return gradingCount;
    }
    
    @Override
    public long getTotalStudentCount() {
        // 统计系统中学生总数
        return userRepository.countByRole("student");
    }
    
    @Override
    public long getOngoingExamCount() {
        // 统计进行中的考试数量（按考试分配统计，不是按考试会话）
        List<ExamAssignment> activeAssignments = examAssignmentRepository.findByStatus(
            ExamAssignment.AssignmentStatus.active);
        
        long ongoingCount = 0L;
        LocalDateTime now = LocalDateTime.now();
        
        for (ExamAssignment assignment : activeAssignments) {
            // 检查考试时间是否在有效范围内
            if (assignment.getExamStartTime() != null && assignment.getExamEndTime() != null) {
                if (now.isAfter(assignment.getExamStartTime()) && now.isBefore(assignment.getExamEndTime())) {
                    // 检查是否有学生正在进行考试
                    List<ExamSession> ongoingSessions = examSessionRepository.findByAssignmentIdAndStatus(
                        assignment.getAssignmentId(), ExamSession.SessionStatus.ongoing);
                    if (!ongoingSessions.isEmpty()) {
                        ongoingCount++;
                    }
                }
            }
        }
        
        return ongoingCount;
    }
}
