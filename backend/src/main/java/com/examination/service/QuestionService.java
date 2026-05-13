package com.examination.service;

import com.examination.dto.question.*;
import com.examination.entity.Question;
import org.springframework.data.domain.Page;

public interface QuestionService {
    Question createQuestion(QuestionCreateDTO dto, String username); // 改参数名
    void submitForReview(String id, String username); // 改参数名
    void reviewQuestion(QuestionReviewDTO dto, String username); // 改参数名
    Page<Question> listTeacherQuestions(QuestionQueryDTO query, String username); // 改参数名
    Question getQuestionDetail(String id);
    AutoGradeResult gradeQuestion(String questionId, String studentAnswer);
}