package com.examination.repository;

import com.examination.entity.KnowledgeTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeTagRepository extends JpaRepository<KnowledgeTag, String> {
    List<KnowledgeTag> findBySubject_SubjectId(String subjectId);
}