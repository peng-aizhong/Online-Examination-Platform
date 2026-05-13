package com.examination.service;

import com.examination.dto.QuestionRequest;
import com.examination.dto.QuestionResponse;
import com.examination.entity.Question;
import com.examination.entity.Subject;
import com.examination.entity.User;
import com.examination.repository.PaperQuestionRepository;
import com.examination.repository.QuestionRepository;
import com.examination.repository.SubjectRepository;
import com.examination.repository.UserRepository;
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
public class QuestionBankService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    public List<QuestionResponse> listQuestions(String username, String subjectId, String questionType, String keyword) {
        getTeacherUser(username);
        List<Question> questions;

        if (subjectId != null && !subjectId.isBlank()) {
            questions = questionRepository.findBySubject_SubjectId(subjectId);
        } else {
            questions = questionRepository.findAll();
        }

        if (questionType != null && !questionType.isBlank()) {
            questions = questions.stream()
                    .filter(q -> questionType.equals(q.getQuestionType()))
                    .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            questions = questions.stream()
                    .filter(q -> (q.getContent() != null && q.getContent().toLowerCase().contains(kw))
                            || (q.getKnowledgeTag() != null && q.getKnowledgeTag().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        return questions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public QuestionResponse getQuestion(String username, String questionId) {
        getTeacherUser(username);
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        return toResponse(q);
    }

    public QuestionResponse createQuestion(String username, QuestionRequest request) {
        getTeacherUser(username);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("科目不存在"));

        String id = "Q" + System.currentTimeMillis() % 1000000000;

        Question question = Question.builder()
                .id(id)
                .subject(subject)
                .content(request.getContent())
                .questionType(request.getQuestionType())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .answer(request.getAnswer())
                .difficulty(request.getDifficulty())
                .knowledgeTag(request.getKnowledgeTag())
                .build();

        question = questionRepository.save(question);
        return toResponse(question);
    }

    public QuestionResponse updateQuestion(String username, String questionId, QuestionRequest request) {
        getTeacherUser(username);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("科目不存在"));

        question.setSubject(subject);
        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setAnswer(request.getAnswer());
        question.setDifficulty(request.getDifficulty());
        question.setKnowledgeTag(request.getKnowledgeTag());

        question = questionRepository.save(question);
        return toResponse(question);
    }

    public void deleteQuestion(String username, String questionId) {
        getTeacherUser(username);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("题目不存在"));

        long refCount = paperQuestionRepository.findByPaperId("").stream()
                .filter(pq -> pq.getQuestionId().equals(questionId))
                .count();
        if (refCount > 0) {
            throw new RuntimeException("该题目已被试卷引用，无法删除");
        }

        questionRepository.delete(question);
    }

    private QuestionResponse toResponse(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .subjectId(q.getSubject() != null ? q.getSubject().getSubjectId() : "")
                .subjectName(q.getSubject() != null ? q.getSubject().getSubjectName() : "")
                .content(q.getContent())
                .questionType(q.getQuestionType())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .answer(q.getAnswer())
                .difficulty(q.getDifficulty())
                .knowledgeTag(q.getKnowledgeTag())
                .status(q.getStatus())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
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
