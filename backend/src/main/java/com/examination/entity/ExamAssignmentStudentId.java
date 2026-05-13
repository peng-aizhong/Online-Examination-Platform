package com.examination.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAssignmentStudentId implements Serializable {
    private String assignmentId;
    private String studentId;
}
