package com.examination.dto.question;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionReviewDTO {
    @NotBlank(message = "试题ID不能为空")
    private String id;

    @NotBlank(message = "审核状态不能为空")
    private String status; // 已发布/已驳回

    private String reviewComment;
}