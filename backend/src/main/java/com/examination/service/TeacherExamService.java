package com.examination.service;

import com.examination.dto.*;
import com.examination.entity.*;
import com.examination.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TeacherExamService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExamAssignmentRepository assignmentRepository;
    @Autowired
    private ExamSessionRepository sessionRepository;
    @Autowired
    private ExamSessionAnswerRepository sessionAnswerRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ExamAssignmentStudentRepository assignmentStudentRepository;
    @Autowired
    private PaperRepository paperRepository;

    public ClassStatisticsResponse getClassStatistics(String username) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<ExamAssignment> assignments = assignmentRepository.findByTeacher(teacher);
        List<ExamSession> allSessions = new ArrayList<>();

        for (ExamAssignment a : assignments) {
            allSessions.addAll(sessionRepository.findSubmittedByAssignmentId(a.getAssignmentId()));
        }

        if (allSessions.isEmpty()) {
            return ClassStatisticsResponse.builder()
                    .totalStudents(0)
                    .totalAssignments(assignments.size())
                    .averageScore(0.0)
                    .highestScore(0.0)
                    .lowestScore(0.0)
                    .passRate(0.0)
                    .assignmentStats(Collections.emptyList())
                    .scoreDistribution(new LinkedHashMap<>())
                    .weakKnowledgePoints(Collections.emptyList())
                    .build();
        }

        Set<String> studentIds = allSessions.stream()
                .map(s -> s.getStudent().getUserId())
                .collect(Collectors.toSet());

        double avgScore = allSessions.stream().mapToDouble(ExamSession::getTotalScore).average().orElse(0);
        double highest = allSessions.stream().mapToDouble(ExamSession::getTotalScore).max().orElse(0);
        double lowest = allSessions.stream().mapToDouble(ExamSession::getTotalScore).min().orElse(0);

        long passCount = allSessions.stream().filter(s -> {
            int total = s.getPaper().getTotalScore();
            return total > 0 && (s.getTotalScore() / total) >= 0.6;
        }).count();
        double passRate = passCount * 100.0 / allSessions.size();

        List<ClassStatisticsResponse.AssignmentStat> assignmentStats = assignments.stream().map(assignment -> {
            List<ExamSession> sessions = sessionRepository.findSubmittedByAssignmentId(assignment.getAssignmentId());
            if (sessions.isEmpty()) {
                return ClassStatisticsResponse.AssignmentStat.builder()
                        .assignmentId(assignment.getAssignmentId())
                        .assignmentName(assignment.getAssignmentName())
                        .paperName(assignment.getPaper().getPaperName())
                        .averageScore(0.0).highestScore(0.0).lowestScore(0.0)
                        .passRate(0.0).participantCount(0).build();
            }
            double aAvg = sessions.stream().mapToDouble(ExamSession::getTotalScore).average().orElse(0);
            double aHigh = sessions.stream().mapToDouble(ExamSession::getTotalScore).max().orElse(0);
            double aLow = sessions.stream().mapToDouble(ExamSession::getTotalScore).min().orElse(0);
            long aPass = sessions.stream().filter(s -> {
                int total = s.getPaper().getTotalScore();
                return total > 0 && (s.getTotalScore() / total) >= 0.6;
            }).count();
            double aPassRate = aPass * 100.0 / sessions.size();
            return ClassStatisticsResponse.AssignmentStat.builder()
                    .assignmentId(assignment.getAssignmentId())
                    .assignmentName(assignment.getAssignmentName())
                    .paperName(assignment.getPaper().getPaperName())
                    .averageScore(Math.round(aAvg * 100.0) / 100.0)
                    .highestScore(aHigh)
                    .lowestScore(aLow)
                    .passRate(Math.round(aPassRate * 100.0) / 100.0)
                    .participantCount(sessions.size())
                    .build();
        }).collect(Collectors.toList());

        Map<String, Double> distribution = new LinkedHashMap<>();
        String[] labels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        int[] counts = new int[5];
        for (ExamSession s : allSessions) {
            int total = s.getPaper().getTotalScore();
            int pct = total == 0 ? 0 : (int) (s.getTotalScore() * 100 / total);
            if (pct < 60) counts[0]++;
            else if (pct < 70) counts[1]++;
            else if (pct < 80) counts[2]++;
            else if (pct < 90) counts[3]++;
            else counts[4]++;
        }
        for (int i = 0; i < labels.length; i++) {
            distribution.put(labels[i], (double) counts[i]);
        }

        List<ClassStatisticsResponse.KnowledgePointStat> weakPoints = getWeakKnowledgePoints(assignments);

        return ClassStatisticsResponse.builder()
                .totalStudents(studentIds.size())
                .totalAssignments(assignments.size())
                .averageScore(Math.round(avgScore * 100.0) / 100.0)
                .highestScore(highest)
                .lowestScore(lowest)
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .assignmentStats(assignmentStats)
                .scoreDistribution(distribution)
                .weakKnowledgePoints(weakPoints)
                .build();
    }

    private List<ClassStatisticsResponse.KnowledgePointStat> getWeakKnowledgePoints(List<ExamAssignment> assignments) {
        Map<String, int[]> kpStats = new HashMap<>();

        for (ExamAssignment assignment : assignments) {
            String paperId = assignment.getPaper().getPaperId();
            List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paperId);
            List<ExamSession> sessions = sessionRepository.findSubmittedByAssignmentId(assignment.getAssignmentId());

            for (PaperQuestion pq : paperQuestions) {
                Question q = questionRepository.findById(pq.getQuestionId()).orElse(null);
                if (q == null || q.getKnowledgeTag() == null || q.getKnowledgeTag().isBlank()) continue;

                String kp = q.getKnowledgeTag();
                kpStats.putIfAbsent(kp, new int[]{0, 0});
                kpStats.get(kp)[1]++;

                int correctCount = 0;
                for (ExamSession session : sessions) {
                    Optional<ExamSessionAnswer> ans = sessionAnswerRepository
                            .findBySessionIdAndQuestionId(session.getSessionId(), q.getId());
                    if (ans.isPresent() && ans.get().getScore() != null && ans.get().getScore() > 0) {
                        correctCount++;
                    }
                }
                kpStats.get(kp)[0] += correctCount;
            }
        }

        return kpStats.entrySet().stream()
                .map(e -> {
                    int correct = e.getValue()[0];
                    int total = e.getValue()[1];
                    double accuracy = total == 0 ? 0 : (correct * 100.0 / total);
                    return ClassStatisticsResponse.KnowledgePointStat.builder()
                            .knowledgeTag(e.getKey())
                            .accuracy(Math.round(accuracy * 100.0) / 100.0)
                            .totalQuestions(total)
                            .correctCount(correct)
                            .isWeak(accuracy < 60)
                            .build();
                })
                .sorted(Comparator.comparingDouble(ClassStatisticsResponse.KnowledgePointStat::getAccuracy))
                .collect(Collectors.toList());
    }

    public List<GradingResultResponse> getGradingResults(String username, String assignmentId) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        ExamAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("考试分配不存在"));

        if (!assignment.getTeacher().getUserId().equals(teacher.getUserId())) {
            throw new RuntimeException("无权查看此考试的阅卷结果");
        }

        List<ExamSession> sessions = sessionRepository.findSubmittedByAssignmentId(assignmentId);
        String paperId = assignment.getPaper().getPaperId();
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paperId);

        return sessions.stream().map(session -> {
            List<ExamSessionAnswer> answers = sessionAnswerRepository.findBySessionId(session.getSessionId());
            Map<String, ExamSessionAnswer> answerMap = answers.stream()
                    .collect(Collectors.toMap(ExamSessionAnswer::getQuestionId, a -> a));

            List<GradingResultResponse.GradedQuestion> gradedQuestions = new ArrayList<>();
            Map<String, GradingResultResponse.TypeBreakdown> typeBreakdowns = new LinkedHashMap<>();

            for (PaperQuestion pq : paperQuestions) {
                Question q = questionRepository.findById(pq.getQuestionId()).orElse(null);
                if (q == null) continue;

                ExamSessionAnswer ans = answerMap.get(q.getId());
                boolean correct = ans != null && ans.getScore() != null && ans.getScore() > 0;
                double earned = ans != null && ans.getScore() != null ? ans.getScore() : 0;

                gradedQuestions.add(GradingResultResponse.GradedQuestion.builder()
                        .questionId(q.getId())
                        .content(q.getContent())
                        .questionType(q.getQuestionType())
                        .knowledgeTag(q.getKnowledgeTag())
                        .yourAnswer(ans != null ? ans.getAnswerText() : "")
                        .correctAnswer(q.getAnswer())
                        .feedback(ans != null ? ans.getFeedback() : null)
                        .correct(correct)
                        .earned(earned)
                        .score(pq.getScore())
                        .difficulty(q.getDifficulty())
                        .build());

                typeBreakdowns.computeIfAbsent(q.getQuestionType(), t ->
                        GradingResultResponse.TypeBreakdown.builder()
                                .type(t).total(0).correct(0).earned(0.0).possible(0).build());
                GradingResultResponse.TypeBreakdown tb = typeBreakdowns.get(q.getQuestionType());
                tb.setTotal(tb.getTotal() + 1);
                tb.setPossible(tb.getPossible() + pq.getScore());
                tb.setEarned(tb.getEarned() + earned);
                if (correct) tb.setCorrect(tb.getCorrect() + 1);
            }

            int totalScore = assignment.getPaper().getTotalScore();
            int accuracy = totalScore == 0 ? 0 : (int) Math.round((session.getTotalScore() * 100.0) / totalScore);

            return GradingResultResponse.builder()
                    .sessionId(session.getSessionId())
                    .assignmentId(assignmentId)
                    .assignmentName(assignment.getAssignmentName())
                    .paperName(assignment.getPaper().getPaperName())
                    .studentName(session.getStudent().getUsername())
                    .studentId(session.getStudent().getUserId())
                    .score(session.getTotalScore())
                    .totalScore(totalScore)
                    .accuracy(accuracy)
                    .attemptNumber(session.getAttemptNumber())
                    .submittedAt(session.getSubmittedAt())
                    .questions(gradedQuestions)
                    .typeBreakdowns(typeBreakdowns)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponse createAssignment(String username, CreateAssignmentRequest request) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Paper paper = paperRepository.findById(request.getPaperId())
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        String assignmentId = "A" + System.currentTimeMillis() % 1000000000;

        ExamAssignment assignment = ExamAssignment.builder()
                .assignmentId(assignmentId)
                .paper(paper)
                .teacher(teacher)
                .assignmentName(request.getAssignmentName())
                .examStartTime(request.getExamStartTime())
                .examEndTime(request.getExamEndTime())
                .durationMinutes(request.getDurationMinutes())
                .maxAttempts(request.getMaxAttempts() != null ? request.getMaxAttempts() : 1)
                .status(ExamAssignment.AssignmentStatus.scheduled)
                .build();
        assignmentRepository.save(assignment);

        if (request.getStudentIds() != null) {
            for (String studentId : request.getStudentIds()) {
                ExamAssignmentStudent eas = ExamAssignmentStudent.builder()
                        .assignmentId(assignmentId)
                        .studentId(studentId)
                        .build();
                assignmentStudentRepository.save(eas);
            }
        }

        return AssignmentResponse.builder()
                .assignmentId(assignmentId)
                .assignmentName(assignment.getAssignmentName())
                .paperId(paper.getPaperId())
                .paperName(paper.getPaperName())
                .examStartTime(assignment.getExamStartTime())
                .examEndTime(assignment.getExamEndTime())
                .durationMinutes(assignment.getDurationMinutes())
                .maxAttempts(assignment.getMaxAttempts())
                .status(assignment.getStatus().name())
                .assignedStudentCount(request.getStudentIds() != null ? request.getStudentIds().size() : 0)
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    public List<AssignmentResponse> listAssignments(String username) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<ExamAssignment> assignments = assignmentRepository.findByTeacher(teacher);
        return assignments.stream().map(a -> {
            List<ExamAssignmentStudent> students = assignmentStudentRepository.findByAssignmentId(a.getAssignmentId());
            return AssignmentResponse.builder()
                    .assignmentId(a.getAssignmentId())
                    .assignmentName(a.getAssignmentName())
                    .paperId(a.getPaper().getPaperId())
                    .paperName(a.getPaper().getPaperName())
                    .examStartTime(a.getExamStartTime())
                    .examEndTime(a.getExamEndTime())
                    .durationMinutes(a.getDurationMinutes())
                    .maxAttempts(a.getMaxAttempts())
                    .status(a.getStatus().name())
                    .assignedStudentCount(students.size())
                    .createdAt(a.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<StudentInfoResponse> listStudents() {
        List<User> students = userRepository.findByRoleAndActiveTrue(User.UserRole.student);
        return students.stream().map(s -> StudentInfoResponse.builder()
                .userId(s.getUserId())
                .username(s.getUsername())
                .realName(s.getUsername())
                .department(s.getDepartment())
                .email(s.getEmail())
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public GradingResultResponse gradeSubjective(String username, String sessionId, GradeSubjectiveRequest request) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        ExamSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));

        ExamAssignment assignment = session.getAssignment();
        if (!assignment.getTeacher().getUserId().equals(teacher.getUserId())) {
            throw new RuntimeException("无权评阅此考试");
        }

        double subjectiveTotal = 0;
        for (GradeSubjectiveRequest.GradeItem item : request.getItems()) {
            ExamSessionAnswer answer = sessionAnswerRepository
                    .findBySessionIdAndQuestionId(sessionId, item.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("答题记录不存在: " + item.getQuestionId()));

            Question q = questionRepository.findById(item.getQuestionId()).orElse(null);
            if (q == null || !"subjective".equals(q.getQuestionType())) {
                throw new RuntimeException("题目不存在或非主观题: " + item.getQuestionId());
            }

            answer.setScore(item.getScore());
            answer.setFeedback(item.getFeedback());
            sessionAnswerRepository.save(answer);

            subjectiveTotal += item.getScore() != null ? item.getScore() : 0;
        }

        session.setSubjectiveScore(subjectiveTotal);
        session.setTotalScore(session.getObjectiveScore() + subjectiveTotal);
        sessionRepository.save(session);

        return getGradingResults(username, assignment.getAssignmentId()).stream()
                .filter(r -> r.getSessionId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("评分结果获取失败"));
    }
}
