package com.examination.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateDTO {
    @NotBlank(message = "科目ID不能为空")
    private String subjectId;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    @NotBlank(message = "题目类型不能为空")
    private String questionType;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @NotBlank(message = "答案不能为空")
    private String answer;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    private List<String> tagIds; // 知识点标签ID列表
}