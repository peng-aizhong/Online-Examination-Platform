package com.examination.service;

import com.examination.dto.PaperRequest;
import com.examination.dto.PaperResponse;
import com.examination.entity.*;
import com.examination.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaperManagementService {
    @Autowired
    private PaperRepository paperRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExamAssignmentRepository assignmentRepository;

    public List<PaperResponse> listPapers(String username) {
        User teacher = getTeacherUser(username);
        List<Paper> papers = paperRepository.findByCreator_UserId(teacher.getUserId());
        return papers.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PaperResponse getPaper(String username, String paperId) {
        getTeacherUser(username);
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        return toDetailResponse(paper);
    }

    public PaperResponse createPaper(String username, PaperRequest request) {
        User teacher = getTeacherUser(username);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("科目不存在"));

        String paperId = "P" + System.currentTimeMillis() % 1000000000;

        int totalScore = 0;
        if (request.getQuestions() != null) {
            for (PaperRequest.QuestionItem qi : request.getQuestions()) {
                totalScore += qi.getScore() != null ? qi.getScore() : 0;
            }
        }

        Paper paper = Paper.builder()
                .paperId(paperId)
                .paperName(request.getPaperName())
                .subject(subject)
                .creator(teacher)
                .duration(request.getDuration())
                .totalScore(request.getTotalScore() != null ? request.getTotalScore() : totalScore)
                .difficultyLevel(request.getDifficultyLevel())
                .build();

        paper = paperRepository.save(paper);

        if (request.getQuestions() != null) {
            for (PaperRequest.QuestionItem qi : request.getQuestions()) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paperId);
                pq.setQuestionId(qi.getQuestionId());
                pq.setScore(qi.getScore());
                paperQuestionRepository.save(pq);
            }
        }

        return toDetailResponse(paper);
    }

    public PaperResponse updatePaper(String username, String paperId, PaperRequest request) {
        getTeacherUser(username);
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("科目不存在"));

        paper.setPaperName(request.getPaperName());
        paper.setSubject(subject);
        paper.setDuration(request.getDuration());
        paper.setDifficultyLevel(request.getDifficultyLevel());

        paperQuestionRepository.deleteByPaperId(paperId);

        int totalScore = 0;
        if (request.getQuestions() != null) {
            for (PaperRequest.QuestionItem qi : request.getQuestions()) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paperId);
                pq.setQuestionId(qi.getQuestionId());
                pq.setScore(qi.getScore());
                paperQuestionRepository.save(pq);
                totalScore += qi.getScore() != null ? qi.getScore() : 0;
            }
        }

        paper.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : totalScore);
        paper = paperRepository.save(paper);
        return toDetailResponse(paper);
    }

    public void deletePaper(String username, String paperId) {
        getTeacherUser(username);
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));

        List<ExamAssignment> assignments = assignmentRepository.findByPaper(paper);
        if (!assignments.isEmpty()) {
            throw new RuntimeException("该试卷已被考试分配引用，无法删除");
        }

        paperQuestionRepository.deleteByPaperId(paperId);
        paperRepository.delete(paper);
    }

    public void addQuestionToPaper(String username, String paperId, String questionId, Integer score) {
        getTeacherUser(username);
        paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        PaperQuestionId id = new PaperQuestionId();
        id.setPaperId(paperId);
        id.setQuestionId(questionId);

        if (paperQuestionRepository.findById(id).isPresent()) {
            throw new RuntimeException("该题目已在试卷中");
        }

        PaperQuestion pq = new PaperQuestion();
        pq.setPaperId(paperId);
        pq.setQuestionId(questionId);
        pq.setScore(score != null ? score : 0);
        paperQuestionRepository.save(pq);

        updatePaperTotalScore(paperId);
    }

    public void removeQuestionFromPaper(String username, String paperId, String questionId) {
        getTeacherUser(username);
        PaperQuestionId id = new PaperQuestionId();
        id.setPaperId(paperId);
        id.setQuestionId(questionId);

        paperQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("该题目不在试卷中"));
        paperQuestionRepository.deleteById(id);

        updatePaperTotalScore(paperId);
    }

    private void updatePaperTotalScore(String paperId) {
        Paper paper = paperRepository.findById(paperId).orElse(null);
        if (paper == null) return;

        List<PaperQuestion> pqs = paperQuestionRepository.findByPaperId(paperId);
        int total = pqs.stream().mapToInt(pq -> pq.getScore() != null ? pq.getScore() : 0).sum();
        paper.setTotalScore(total);
        paperRepository.save(paper);
    }

    private PaperResponse toResponse(Paper p) {
        return PaperResponse.builder()
                .paperId(p.getPaperId())
                .paperName(p.getPaperName())
                .subjectId(p.getSubject() != null ? p.getSubject().getSubjectId() : "")
                .subjectName(p.getSubject() != null ? p.getSubject().getSubjectName() : "")
                .creatorId(p.getCreator() != null ? p.getCreator().getUserId() : "")
                .creatorName(p.getCreator() != null ? p.getCreator().getUsername() : "")
                .duration(p.getDuration())
                .totalScore(p.getTotalScore())
                .difficultyLevel(p.getDifficultyLevel())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PaperResponse toDetailResponse(Paper p) {
        PaperResponse resp = toResponse(p);

        List<PaperQuestion> pqs = paperQuestionRepository.findByPaperId(p.getPaperId());
        List<PaperResponse.QuestionItem> items = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question q = questionRepository.findById(pq.getQuestionId()).orElse(null);
            if (q == null) continue;
            items.add(PaperResponse.QuestionItem.builder()
                    .questionId(q.getId())
                    .content(q.getContent())
                    .questionType(q.getQuestionType())
                    .difficulty(q.getDifficulty())
                    .knowledgeTag(q.getKnowledgeTag())
                    .score(pq.getScore())
                    .build());
        }
        resp.setQuestions(items);
        return resp;
    }

    private User getTeacherUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getRole() != User.UserRole.teacher && user.getRole() != User.UserRole.admin) {
            throw new RuntimeException("仅教师可访问该功能");
        }
        return user;
    }
}
