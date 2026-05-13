package com.examination.repository;

import com.examination.entity.ExamSessionAnswer;
import com.examination.entity.ExamSessionAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionAnswerRepository extends JpaRepository<ExamSessionAnswer, ExamSessionAnswerId> {
    List<ExamSessionAnswer> findBySessionId(String sessionId);

    Optional<ExamSessionAnswer> findBySessionIdAndQuestionId(String sessionId, String questionId);
}
