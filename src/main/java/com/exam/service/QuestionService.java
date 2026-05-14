package com.exam.service;

import com.exam.entity.Question;
import com.exam.dto.ImportResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface QuestionService {
    
    /**
     * 添加题目
     */
    Question addQuestion(Question question);
    
    /**
     * 根据ID获取题目
     */
    Question getQuestionById(String id);
    
    /**
     * 更新题目
     */
    Question updateQuestion(String id, Question question);
    
    /**
     * 删除题目
     */
    void deleteQuestion(String id);
    
    /**
     * 搜索题目
     */
    Page<Question> searchQuestions(String subjectId, String questionId, String questionType, String difficulty, String search, Pageable pageable);
    
    /**
     * 批量导入题目（支持Excel和TXT格式）
     */
    ImportResult importQuestionsFromFile(MultipartFile file, String subjectId);
    
    /**
     * 导出题库
     */
    void exportQuestions(String subjectId, String questionType, 
                        String difficulty, String status, 
                        HttpServletResponse response);
    
    /**
     * 获取题目统计信息
     */
    Map<String, Object> getQuestionStatistics(String subjectId);
    
    /**
     * 获取题目总数
     */
    long getQuestionCount();
    
    /**
     * 获取所有知识点标签
     */
    List<String> getAllKnowledgeTags();
    
    /**
     * 根据题型和难度随机选择题目
     */
    List<Question> findRandomQuestionsByTypeAndDifficulty(String subjectId, 
                                                         String questionType, 
                                                         String difficulty, 
                                                         int count);

    /**
     * 获取所有题目
     */
    List<Question> getAllQuestions();

    /**
     * 分页获取所有题目
     */
    Page<Question> getAllQuestionsPageable(Pageable pageable);

    /**
     * 根据科目ID获取题目数量
     */
    long getQuestionCount(String subjectId);

    /**
     * 根据科目ID获取知识点标签
     */
    List<String> getKnowledgeTagsBySubjectId(String subjectId);
    
    /**
     * 根据创建者获取题目数量
     */
    long getQuestionCountByCreator(String creatorId);

    /**
     * 按科目清空题库，返回删除的题目数量
     */
    int clearQuestionsBySubject(String subjectId);
} 