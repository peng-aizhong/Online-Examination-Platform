package com.examination.repository;

import com.examination.entity.Question;
import com.examination.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findBySubject(Subject subject);
    List<Question> findBySubject_SubjectId(String subjectId);
    List<Question> findBySubjectAndStatus(Subject subject, String status);
    List<Question> findByKnowledgeTag(String knowledgeTag);
}
