package com.exam.repository;

import com.exam.entity.ExamAssignmentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAssignmentStudentRepository extends JpaRepository<ExamAssignmentStudent, ExamAssignmentStudent.ExamAssignmentStudentId> {
    
    // 根据考试分配ID查找分配的学生
    List<ExamAssignmentStudent> findByIdAssignmentId(String assignmentId);
    
    // 根据学生ID查找分配的考试
    List<ExamAssignmentStudent> findByIdStudentId(String studentId);
    
    // 检查学生是否已被分配到指定考试
    boolean existsByIdAssignmentIdAndIdStudentId(String assignmentId, String studentId);
    
    // 根据考试分配ID删除所有分配的学生
    @Modifying
    @Query("DELETE FROM ExamAssignmentStudent eas WHERE eas.id.assignmentId = :assignmentId")
    void deleteByIdAssignmentId(@Param("assignmentId") String assignmentId);
    
    // 根据学生ID删除所有分配
    void deleteByIdStudentId(String studentId);
    
    // 根据考试分配ID和学生ID删除特定分配
    @Modifying
    @Query("DELETE FROM ExamAssignmentStudent eas WHERE eas.id.assignmentId = :assignmentId AND eas.id.studentId = :studentId")
    void deleteByIdAssignmentIdAndIdStudentId(@Param("assignmentId") String assignmentId, @Param("studentId") String studentId);
    
    // 根据学生ID统计分配数量
    long countByIdStudentId(String studentId);
    
    // 查找学生可参加的考试分配（通过关联查询）
    @Query("SELECT eas FROM ExamAssignmentStudent eas " +
           "JOIN FETCH eas.examAssignment ea " +
           "WHERE eas.id.studentId = :studentId " +
           "AND (ea.status = 'active' OR ea.status = 'scheduled') " +
           "AND ea.examEndTime >= :now")
    List<ExamAssignmentStudent> findActiveAssignmentsForStudent(@Param("studentId") String studentId, 
                                                               @Param("now") java.time.LocalDateTime now);
}
