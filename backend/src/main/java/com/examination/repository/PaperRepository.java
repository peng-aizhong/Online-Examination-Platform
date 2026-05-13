package com.examination.repository;

import com.examination.entity.Paper;
import com.examination.entity.Subject;
import com.examination.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, String> {
    List<Paper> findByCreator(User creator);
    List<Paper> findByCreator_UserId(String creatorId);
    List<Paper> findBySubject(Subject subject);
    List<Paper> findByStatus(String status);
}
