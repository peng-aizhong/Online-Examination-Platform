package com.examination.repository;

import com.examination.entity.ExamAssignment;
import com.examination.entity.Paper;
import com.examination.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAssignmentRepository extends JpaRepository<ExamAssignment, String> {
    List<ExamAssignment> findByTeacher(User teacher);
    List<ExamAssignment> findByPaper(Paper paper);
    List<ExamAssignment> findByStatus(ExamAssignment.AssignmentStatus status);
    List<ExamAssignment> findByTeacherAndStatus(User teacher, ExamAssignment.AssignmentStatus status);
}
