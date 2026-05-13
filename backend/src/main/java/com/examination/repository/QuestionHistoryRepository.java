package com.examination.repository;

import com.examination.entity.QuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionHistoryRepository extends JpaRepository<QuestionHistory, String> {
    List<QuestionHistory> findByQuestionIdOrderByVersionDesc(String questionId);
}