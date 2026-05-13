package com.examination.dto.question;

import lombok.Data;

@Data
public class QuestionQueryDTO {
    private String subjectId;
    private String questionType;
    private String status;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}