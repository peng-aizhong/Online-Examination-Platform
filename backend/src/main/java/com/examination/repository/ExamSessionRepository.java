package com.examination.repository;

import com.examination.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, String> {
    List<ExamSession> findByStudent_UserId(String studentId);

    List<ExamSession> findByAssignment_AssignmentId(String assignmentId);

    List<ExamSession> findByAssignment_AssignmentIdAndStudent_UserId(String assignmentId, String studentId);

    Optional<ExamSession> findByAssignment_AssignmentIdAndStudent_UserIdAndStatus(String assignmentId, String studentId, String status);

    List<ExamSession> findByStatus(String status);

    @Query("SELECT es FROM ExamSession es WHERE es.assignment.assignmentId = :assignmentId AND es.status = 'submitted'")
    List<ExamSession> findSubmittedByAssignmentId(@Param("assignmentId") String assignmentId);

    @Query("SELECT es FROM ExamSession es WHERE es.student.userId = :studentId AND es.status = 'submitted' ORDER BY es.submittedAt DESC")
    List<ExamSession> findSubmittedByStudentId(@Param("studentId") String studentId);

    @Query("SELECT COUNT(es) FROM ExamSession es WHERE es.assignment.assignmentId = :assignmentId AND es.status = 'submitted' AND es.totalScore > :score")
    long countByAssignmentIdAndScoreGreaterThan(@Param("assignmentId") String assignmentId, @Param("score") Double score);

    @Query("SELECT COUNT(es) FROM ExamSession es WHERE es.assignment.assignmentId = :assignmentId AND es.status = 'submitted'")
    long countSubmittedByAssignmentId(@Param("assignmentId") String assignmentId);
}
