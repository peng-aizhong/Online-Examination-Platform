package com.exam.repository;

import com.exam.entity.ExamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamAssignmentRepository extends JpaRepository<ExamAssignment, String> {
    
    // 根据教师ID查找考试分配
    List<ExamAssignment> findByTeacherIdOrderByCreatedAtDesc(String teacherId);
    
    // 根据试卷ID查找考试分配
    List<ExamAssignment> findByPaperId(String paperId);
    
    // 根据状态查找考试分配
    List<ExamAssignment> findByStatus(ExamAssignment.AssignmentStatus status);
    
    // 查找指定时间范围内的考试分配
    @Query("SELECT ea FROM ExamAssignment ea WHERE ea.examStartTime <= :endTime AND ea.examEndTime >= :startTime")
    List<ExamAssignment> findAssignmentsInTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    // 查找正在进行的考试分配
    @Query("SELECT ea FROM ExamAssignment ea WHERE ea.examStartTime <= :now AND ea.examEndTime >= :now")
    List<ExamAssignment> findActiveAssignments(@Param("now") LocalDateTime now);
    
    // 查找已结束的考试分配
    @Query("SELECT ea FROM ExamAssignment ea WHERE ea.examEndTime < :now")
    List<ExamAssignment> findFinishedAssignments(@Param("now") LocalDateTime now);
    
    // 根据教师ID和状态查找考试分配
    List<ExamAssignment> findByTeacherIdAndStatus(String teacherId, ExamAssignment.AssignmentStatus status);
    
    // 根据试卷ID、教师ID和状态查找考试分配
    List<ExamAssignment> findByPaperIdAndTeacherIdAndStatus(String paperId, String teacherId, ExamAssignment.AssignmentStatus status);
    
    // 查找最大的assignment_id
    @Query("SELECT MAX(ea.assignmentId) FROM ExamAssignment ea")
    String findMaxAssignmentId();
    
    // 根据教师ID统计考试分配数量
    long countByTeacherId(String teacherId);
}
