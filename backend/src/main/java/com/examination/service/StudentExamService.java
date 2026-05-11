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
@Transactional
public class StudentExamService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExamAssignmentRepository assignmentRepository;
    @Autowired
    private ExamAssignmentStudentRepository assignmentStudentRepository;
    @Autowired
    private ExamSessionRepository sessionRepository;
    @Autowired
    private ExamSessionAnswerRepository sessionAnswerRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    @Autowired
    private QuestionRepository questionRepository;

    public List<AssignmentItemResponse> listAssignments(String username) {
        User user = getStudentUser(username);
        LocalDateTime now = LocalDateTime.now();

        List<ExamAssignmentStudent> assignments = assignmentStudentRepository.findByStudentIdWithAssignment(user.getUserId());
        List<AssignmentItemResponse> result = new ArrayList<>();

        for (ExamAssignmentStudent eas : assignments) {
            ExamAssignment assignment = assignmentRepository.findById(eas.getAssignmentId()).orElse(null);
            if (assignment == null) continue;

            Paper paper = assignment.getPaper();
            List<ExamSession> sessions = sessionRepository.findByAssignment_AssignmentIdAndStudent_UserId(
                    assignment.getAssignmentId(), user.getUserId());

            long submittedCount = sessions.stream()
                    .filter(s -> "submitted".equals(s.getStatus()))
                    .count();

            Optional<ExamSession> bestSession = sessions.stream()
                    .filter(s -> "submitted".equals(s.getStatus()) && Boolean.TRUE.equals(s.getIsBestScore()))
                    .findFirst();

            String sessionStatus = resolveAssignmentStatus(assignment, now, (int) submittedCount);

            result.add(AssignmentItemResponse.builder()
                    .assignmentId(assignment.getAssignmentId())
                    .assignmentName(assignment.getAssignmentName())
                    .paperId(paper.getPaperId())
                    .paperName(paper.getPaperName())
                    .subjectName(paper.getSubject() != null ? paper.getSubject().getSubjectName() : "")
                    .durationMinutes(assignment.getDurationMinutes())
                    .totalScore(paper.getTotalScore())
                    .examStartTime(assignment.getExamStartTime())
                    .examEndTime(assignment.getExamEndTime())
                    .status(sessionStatus)
                    .myAttempts((int) submittedCount)
                    .maxAttempts(assignment.getMaxAttempts())
                    .bestScore(bestSession.map(ExamSession::getTotalScore).orElse(null))
                    .sessionStatus(sessionStatus)
                    .build());
        }

        return result;
    }

    public AssignmentDetailResponse getAssignmentDetail(String username, String assignmentId) {
        User user = getStudentUser(username);
        ExamAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("考试分配不存在"));

        boolean isAssigned = assignmentStudentRepository.existsByAssignmentIdAndStudentId(assignmentId, user.getUserId());
        if (!isAssigned) {
            throw new RuntimeException("你未被分配参加此考试");
        }

        Paper paper = assignment.getPaper();
        LocalDateTime now = LocalDateTime.now();

        List<ExamSession> sessions = sessionRepository.findByAssignment_AssignmentIdAndStudent_UserId(assignmentId, user.getUserId());
        long submittedCount = sessions.stream().filter(s -> "submitted".equals(s.getStatus())).count();

        String status = resolveAssignmentStatus(assignment, now, (int) submittedCount);
        if ("scheduled".equals(status)) {
            throw new RuntimeException("考试尚未开始");
        }
        if ("finished".equals(status) && submittedCount >= assignment.getMaxAttempts()) {
            throw new RuntimeException("你已完成所有考试次数");
        }

        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperIdWithQuestion(paper.getPaperId());
        List<AssignmentDetailResponse.QuestionItem> questionItems = new ArrayList<>();
        int totalScore = 0;

        for (PaperQuestion pq : paperQuestions) {
            Question q = questionRepository.findById(pq.getQuestionId()).orElse(null);
            if (q == null) continue;

            questionItems.add(AssignmentDetailResponse.QuestionItem.builder()
                    .questionId(q.getId())
                    .content(q.getContent())
                    .questionType(q.getQuestionType())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .score(pq.getScore())
                    .difficulty(q.getDifficulty())
                    .knowledgeTag(q.getKnowledgeTag())
                    .build());
            totalScore += pq.getScore();
        }

        // Create or find an in_progress session
        Optional<ExamSession> existingSession = sessionRepository
                .findByAssignment_AssignmentIdAndStudent_UserIdAndStatus(assignmentId, user.getUserId(), "in_progress");

        String sessionId;
        if (existingSession.isPresent()) {
            sessionId = existingSession.get().getSessionId();
        } else {
            int nextAttempt = (int) submittedCount + 1;
            ExamSession newSession = ExamSession.builder()
                    .sessionId(generateSessionId())
                    .assignment(assignment)
                    .paper(paper)
                    .student(user)
                    .attemptNumber(nextAttempt)
                    .startedAt(now)
                    .durationMinutes(assignment.getDurationMinutes())
                    .status("in_progress")
                    .build();
            sessionRepository.save(newSession);
            sessionId = newSession.getSessionId();
        }

        return AssignmentDetailResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .assignmentName(assignment.getAssignmentName())
                .paperId(paper.getPaperId())
                .paperName(paper.getPaperName())
                .subjectName(paper.getSubject() != null ? paper.getSubject().getSubjectName() : "")
                .durationMinutes(assignment.getDurationMinutes())
                .totalScore(totalScore)
                .examStartTime(assignment.getExamStartTime())
                .examEndTime(assignment.getExamEndTime())
                .status(status)
                .questions(questionItems)
                .sessionId(sessionId)
                .build();
    }

    public SubmitExamResponse submitExam(String username, String assignmentId, SubmitExamRequest request) {
        User user = getStudentUser(username);
        ExamAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("考试分配不存在"));

        ExamSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("考试会话不存在"));

        if (!session.getStudent().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("无权提交此考试");
        }

        Paper paper = assignment.getPaper();
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paper.getPaperId());

        Map<String, String> answerMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (SubmitExamRequest.AnswerItem item : request.getAnswers()) {
                if (item.getQuestionId() != null) {
                    answerMap.put(item.getQuestionId(), item.getAnswer() == null ? "" : item.getAnswer().trim());
                }
            }
        }

        double totalEarned = 0;
        int totalPossible = 0;
        List<SubmitExamResponse.MistakeQuestion> mistakes = new ArrayList<>();

        for (PaperQuestion pq : paperQuestions) {
            Question q = questionRepository.findById(pq.getQuestionId()).orElse(null);
            if (q == null) continue;

            totalPossible += pq.getScore();
            String yourAnswer = answerMap.getOrDefault(q.getId(), "");
            boolean correct = q.getAnswer() != null && q.getAnswer().equalsIgnoreCase(yourAnswer);
            double score = correct ? pq.getScore() : 0;
            totalEarned += score;

            ExamSessionAnswer answer = sessionAnswerRepository
                    .findBySessionIdAndQuestionId(session.getSessionId(), q.getId())
                    .orElse(ExamSessionAnswer.builder()
                            .sessionId(session.getSessionId())
                            .questionId(q.getId())
                            .build());

            answer.setAnswerText(yourAnswer);
            answer.setScore(score);
            answer.setFeedback(correct ? null : "正确答案: " + q.getAnswer());
            answer.setAnsweredAt(LocalDateTime.now());
            sessionAnswerRepository.save(answer);

            if (!correct) {
                mistakes.add(SubmitExamResponse.MistakeQuestion.builder()
                        .questionId(q.getId())
                        .content(q.getContent())
                        .correctAnswer(q.getAnswer())
                        .yourAnswer(yourAnswer)
                        .score(pq.getScore())
                        .knowledgeTag(q.getKnowledgeTag())
                        .feedback("正确答案: " + q.getAnswer())
                        .build());
            }
        }

        int accuracy = totalPossible == 0 ? 0 : (int) Math.round((totalEarned * 100.0) / totalPossible);
        session.setTotalScore(totalEarned);
        session.setObjectiveScore(totalEarned);
        session.setStatus("submitted");
        session.setSubmittedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return SubmitExamResponse.builder()
                .sessionId(session.getSessionId())
                .score(totalEarned)
                .totalScore(totalPossible)
                .accuracy(accuracy)
                .mistakes(mistakes)
                .build();
    }

    public List<SessionResultResponse> listResults(String username) {
        User user = getStudentUser(username);
        List<ExamSession> sessions = sessionRepository.findSubmittedByStudentId(user.getUserId());

        return sessions.stream().map(session -> {
            ExamAssignment assignment = session.getAssignment();
            Paper paper = session.getPaper();
            int totalScore = paper.getTotalScore();
            int accuracy = totalScore == 0 ? 0 : (int) Math.round((session.getTotalScore() * 100.0) / totalScore);

            return SessionResultResponse.builder()
                    .sessionId(session.getSessionId())
                    .assignmentId(assignment.getAssignmentId())
                    .assignmentName(assignment.getAssignmentName())
                    .paperName(paper.getPaperName())
                    .score(session.getTotalScore())
                    .totalScore(totalScore)
                    .accuracy(accuracy)
                    .attemptNumber(session.getAttemptNumber())
                    .isBestScore(session.getIsBestScore())
                    .submittedAt(session.getSubmittedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public ScoreReportResponse getScoreReport(String username, String sessionId) {
        User user = getStudentUser(username);
        ExamSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("考试记录不存在"));

        if (!session.getStudent().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("无权查看该报告");
        }
        if (!"submitted".equals(session.getStatus())) {
            throw new RuntimeException("该考试尚未提交");
        }

        ExamAssignment assignment = session.getAssignment();
        Paper paper = session.getPaper();
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paper.getPaperId());
        List<ExamSessionAnswer> answers = sessionAnswerRepository.findBySessionId(sessionId);
        Map<String, ExamSessionAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(ExamSessionAnswer::getQuestionId, a -> a));

        long rank = sessionRepository.countByAssignmentIdAndScoreGreaterThan(
                assignment.getAssignmentId(), session.getTotalScore()) + 1;
        long totalParticipants = sessionRepository.countSubmittedByAssignmentId(assignment.getAssignmentId());

        int totalScore = paper.getTotalScore();
        int accuracy = totalScore == 0 ? 0 : (int) Math.round((session.getTotalScore() * 100.0) / totalScore);

        Map<String, ScoreReportResponse.TypeScore> typeScores = new LinkedHashMap<>();
        Map<String, ScoreReportResponse.KnowledgePointScore> kpScores = new LinkedHashMap<>();
        List<ScoreReportResponse.QuestionDetail> questionDetails = new ArrayList<>();

        for (PaperQuestion pq : paperQuestions) {
            Question q = questionRepository.findById(pq.getQuestionId()).orElse(null);
            if (q == null) continue;

            ExamSessionAnswer ans = answerMap.get(q.getId());
            boolean correct = ans != null && ans.getScore() != null && ans.getScore() > 0;
            double earned = ans != null && ans.getScore() != null ? ans.getScore() : 0;

            String type = q.getQuestionType();
            typeScores.computeIfAbsent(type, t -> ScoreReportResponse.TypeScore.builder()
                    .type(t).earned(0.0).total(0).count(0).correctCount(0).build());
            ScoreReportResponse.TypeScore ts = typeScores.get(type);
            ts.setEarned(ts.getEarned() + earned);
            ts.setTotal(ts.getTotal() + pq.getScore());
            ts.setCount(ts.getCount() + 1);
            if (correct) ts.setCorrectCount(ts.getCorrectCount() + 1);

            String kp = q.getKnowledgeTag();
            if (kp != null && !kp.isBlank()) {
                kpScores.computeIfAbsent(kp, k -> ScoreReportResponse.KnowledgePointScore.builder()
                        .knowledgeTag(k).earned(0.0).total(0).count(0).correctCount(0).accuracy(0).build());
                ScoreReportResponse.KnowledgePointScore ks = kpScores.get(kp);
                ks.setEarned(ks.getEarned() + earned);
                ks.setTotal(ks.getTotal() + pq.getScore());
                ks.setCount(ks.getCount() + 1);
                if (correct) ks.setCorrectCount(ks.getCorrectCount() + 1);
            }

            questionDetails.add(ScoreReportResponse.QuestionDetail.builder()
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
        }

        for (ScoreReportResponse.KnowledgePointScore ks : kpScores.values()) {
            ks.setAccuracy(ks.getTotal() == 0 ? 0 : (int) Math.round((ks.getEarned() * 100.0) / ks.getTotal()));
        }

        return ScoreReportResponse.builder()
                .sessionId(session.getSessionId())
                .assignmentId(assignment.getAssignmentId())
                .assignmentName(assignment.getAssignmentName())
                .paperName(paper.getPaperName())
                .score(session.getTotalScore())
                .totalScore(totalScore)
                .accuracy(accuracy)
                .rank(rank)
                .totalParticipants(totalParticipants)
                .attemptNumber(session.getAttemptNumber())
                .isBestScore(session.getIsBestScore())
                .submittedAt(session.getSubmittedAt())
                .typeScores(typeScores)
                .knowledgePointScores(kpScores)
                .questions(questionDetails)
                .build();
    }

    public WrongQuestionResponse listWrongQuestions(String username) {
        User user = getStudentUser(username);
        List<WrongQuestionResponse.WrongQuestionItem> items = new ArrayList<>();

        List<ExamSession> sessions = sessionRepository.findSubmittedByStudentId(user.getUserId());

        for (ExamSession session : sessions) {
            ExamAssignment assignment = session.getAssignment();
            List<ExamSessionAnswer> answers = sessionAnswerRepository.findBySessionId(session.getSessionId());

            for (ExamSessionAnswer answer : answers) {
                Question q = questionRepository.findById(answer.getQuestionId()).orElse(null);
                if (q == null) continue;

                boolean correct = answer.getScore() != null && answer.getScore() > 0;
                if (!correct) {
                    PaperQuestion pq = paperQuestionRepository.findByPaperId(assignment.getPaper().getPaperId())
                            .stream().filter(p -> p.getQuestionId().equals(q.getId())).findFirst().orElse(null);

                    items.add(WrongQuestionResponse.WrongQuestionItem.builder()
                            .assignmentId(assignment.getAssignmentId())
                            .assignmentName(assignment.getAssignmentName())
                            .questionId(q.getId())
                            .questionContent(q.getContent())
                            .correctAnswer(q.getAnswer())
                            .yourAnswer(answer.getAnswerText())
                            .knowledgeTag(q.getKnowledgeTag())
                            .feedback(answer.getFeedback())
                            .score(pq != null ? pq.getScore() : 0)
                            .build());
                }
            }
        }

        return WrongQuestionResponse.builder().items(items).build();
    }

    private User getStudentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getRole() != User.UserRole.student) {
            throw new RuntimeException("仅学生可访问该功能");
        }
        return user;
    }

    private String resolveAssignmentStatus(ExamAssignment assignment, LocalDateTime now, int submittedCount) {
        if (submittedCount >= assignment.getMaxAttempts()) {
            return "completed";
        }
        if (now.isBefore(assignment.getExamStartTime())) {
            return "scheduled";
        }
        if (now.isAfter(assignment.getExamEndTime())) {
            return "finished";
        }
        return "active";
    }

    private String generateSessionId() {
        return "S" + System.currentTimeMillis() % 1000000000;
    }
}
