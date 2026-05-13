package com.examination.service.impl;

import com.examination.dto.question.*;
import com.examination.entity.KnowledgeTag;
import com.examination.entity.Question;
import com.examination.entity.QuestionHistory;
import com.examination.entity.User;
import com.examination.repository.*;
import com.examination.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final KnowledgeTagRepository tagRepository;
    private final QuestionHistoryRepository historyRepository;

    @Override
    @Transactional
    public Question createQuestion(QuestionCreateDTO dto, String username) {
        String questionId = "Q" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        var subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("科目不存在"));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Question question = new Question();
        question.setId(questionId);
        question.setSubject(subject);
        question.setContent(dto.getContent());
        question.setQuestionType(dto.getQuestionType());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setAnswer(dto.getAnswer());
        question.setDifficulty(dto.getDifficulty());
        question.setCreator(creator);
        question.setStatus("草稿");
        question.setVersion(1);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            question.setTags(new HashSet<>(tagRepository.findAllById(dto.getTagIds())));
        }

        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public void submitForReview(String id, String username) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试题不存在"));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!question.getCreator().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("无权限提交该试题");
        }

        if (!"草稿".equals(question.getStatus()) && !"已驳回".equals(question.getStatus())) {
            throw new RuntimeException("只有草稿或已驳回状态的试题才能提交审核");
        }

        question.setStatus("待审核");
        questionRepository.save(question);
    }

    @Override
    @Transactional
    public void reviewQuestion(QuestionReviewDTO dto, String username) {
        Question question = questionRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("试题不存在"));

        if (!"待审核".equals(question.getStatus())) {
            throw new RuntimeException("只有待审核状态的试题才能审核");
        }

        User reviewer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));

        saveQuestionHistory(question);

        question.setStatus(dto.getStatus());
        question.setReviewer(reviewer);
        question.setReviewComment(dto.getReviewComment());
        question.setVersion(question.getVersion() + 1);

        questionRepository.save(question);
    }

    @Override
    public Page<Question> listTeacherQuestions(QuestionQueryDTO query, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Pageable pageable = PageRequest.of(query.getPageNum() - 1, query.getPageSize());
        return questionRepository.findByCreatorAndConditions(
                currentUser.getUserId(),
                query.getSubjectId(),
                query.getQuestionType(),
                query.getStatus(),
                query.getKeyword(),
                pageable
        );
    }

    @Override
    public Question getQuestionDetail(String id) {
        return questionRepository.findDetailById(id);
    }

    @Override
    public AutoGradeResult gradeQuestion(String questionId, String studentAnswer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("试题不存在"));

        AutoGradeResult result = new AutoGradeResult();
        result.setQuestionId(questionId);
        result.setQuestionType(question.getQuestionType());
        result.setStudentAnswer(studentAnswer);
        result.setCorrectAnswer(question.getAnswer());

        switch (question.getQuestionType()) {
            case "单选":
            case "判断":
                result.setCorrect(question.getAnswer().equalsIgnoreCase(studentAnswer.trim()));
                result.setScore(result.isCorrect() ? 1.0 : 0.0);
                break;
            case "多选":
                String[] correctAnswers = question.getAnswer().split(",");
                String[] studentAnswers = studentAnswer.trim().split(",");
                java.util.Arrays.sort(correctAnswers);
                java.util.Arrays.sort(studentAnswers);
                result.setCorrect(java.util.Arrays.equals(correctAnswers, studentAnswers));
                result.setScore(result.isCorrect() ? 1.0 : 0.0);
                break;
            case "填空":
                result.setCorrect(studentAnswer.trim().contains(question.getAnswer().trim()));
                result.setScore(result.isCorrect() ? 1.0 : 0.0);
                break;
            case "简答":
                result.setNeedManualGrade(true);
                result.setScore(0.0);
                break;
            default:
                throw new RuntimeException("不支持的题型：" + question.getQuestionType());
        }

        return result;
    }

    private void saveQuestionHistory(Question question) {
        String historyId = "H" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        QuestionHistory history = new QuestionHistory();
        history.setHistoryId(historyId);
        history.setQuestionId(question.getId());
        history.setSubject(question.getSubject());
        history.setContent(question.getContent());
        history.setQuestionType(question.getQuestionType());
        history.setOptionA(question.getOptionA());
        history.setOptionB(question.getOptionB());
        history.setOptionC(question.getOptionC());
        history.setOptionD(question.getOptionD());
        history.setAnswer(question.getAnswer());
        history.setDifficulty(question.getDifficulty());
        history.setStatus(question.getStatus());
        history.setCreator(question.getCreator());
        history.setVersion(question.getVersion());
        historyRepository.save(history);
    }
}