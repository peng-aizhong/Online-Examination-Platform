package com.examination.repository;

import com.examination.entity.ExamAssignmentStudent;
import com.examination.entity.ExamAssignmentStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAssignmentStudentRepository extends JpaRepository<ExamAssignmentStudent, ExamAssignmentStudentId> {
    List<ExamAssignmentStudent> findByStudentId(String studentId);

    List<ExamAssignmentStudent> findByAssignmentId(String assignmentId);

    @Query("SELECT eas FROM ExamAssignmentStudent eas WHERE eas.studentId = :studentId")
    List<ExamAssignmentStudent> findByStudentIdWithAssignment(@Param("studentId") String studentId);

    boolean existsByAssignmentIdAndStudentId(String assignmentId, String studentId);
}
