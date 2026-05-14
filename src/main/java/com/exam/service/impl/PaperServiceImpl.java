package com.exam.service.impl;

import com.exam.dto.PaperGenerationRequest;
import com.exam.dto.PaperGenerationResult;
import com.exam.dto.QuestionTypeConfig;
import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.repository.PaperQuestionRepository;
import com.exam.repository.PaperRepository;
import com.exam.repository.QuestionRepository;
import com.exam.repository.ExamSessionRepository;
import com.exam.repository.ExamSessionAnswerRepository;
import com.exam.repository.ExamAssignmentRepository;
import com.exam.repository.ExamAssignmentStudentRepository;
import com.exam.entity.ExamAssignment;
import com.exam.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PaperServiceImpl implements PaperService {
    
    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    @Autowired
    private ExamSessionAnswerRepository examSessionAnswerRepository;
    
    @Autowired
    private ExamAssignmentRepository examAssignmentRepository;
    
    @Autowired
    private ExamAssignmentStudentRepository examAssignmentStudentRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public Paper createPaper(Paper paper, List<String> questionIds, List<Integer> scores) {
        // 修复：生成试卷ID
        String paperId = generatePaperId();
        paper.setPaperId(paperId);
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        
        // 设置题目数量
        paper.setQuestionCount(questionIds.size());
        
        Paper savedPaper = paperRepository.save(paper);
        
        for (int i = 0; i < questionIds.size(); i++) {
            PaperQuestion paperQuestion = new PaperQuestion(savedPaper.getPaperId(), questionIds.get(i), scores.get(i));
            paperQuestionRepository.save(paperQuestion);
        }
        
        return savedPaper;
    }
    
    @Override
    public Paper getPaperById(String id) {
        return paperRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("试卷不存在"));
    }
    
    @Override
    public Paper updatePaper(String id, Paper paper, List<String> questionIds, List<Integer> scores) {
        Paper existingPaper = getPaperById(id);
        
        existingPaper.setPaperName(paper.getPaperName());
        existingPaper.setSubjectId(paper.getSubjectId());
        existingPaper.setDuration(paper.getDuration());
        existingPaper.setTotalScore(paper.getTotalScore());
        existingPaper.setDifficultyLevel(paper.getDifficultyLevel());
        existingPaper.setUpdatedAt(LocalDateTime.now());
        
        paperQuestionRepository.deleteByPaperId(id);
        
        for (int i = 0; i < questionIds.size(); i++) {
            PaperQuestion paperQuestion = new PaperQuestion(id, questionIds.get(i), scores.get(i));
            paperQuestionRepository.save(paperQuestion);
        }
        
        return paperRepository.save(existingPaper);
    }
    
    @Override
    @Transactional
    public void deletePaper(String id) {
        if (!paperRepository.existsById(id)) {
            throw new RuntimeException("试卷不存在");
        }
        
        System.out.println("开始删除试卷: " + id);
        
        // 使用SQL强制删除，按正确顺序删除相关记录
        try {
            // 1. 删除考试会话答案
            int deletedAnswers = jdbcTemplate.update("DELETE FROM exam_session_answer WHERE session_id IN (SELECT session_id FROM exam_session WHERE paper_id = ?)", id);
            System.out.println("删除考试会话答案数量: " + deletedAnswers);
            
            // 2. 删除考试会话
            int deletedSessions = jdbcTemplate.update("DELETE FROM exam_session WHERE paper_id = ?", id);
            System.out.println("删除考试会话数量: " + deletedSessions);
            
            // 3. 删除考试分配学生
            int deletedStudents = jdbcTemplate.update("DELETE FROM exam_assignment_student WHERE assignment_id IN (SELECT assignment_id FROM exam_assignment WHERE paper_id = ?)", id);
            System.out.println("删除考试分配学生数量: " + deletedStudents);
            
            // 4. 删除考试分配
            int deletedAssignments = jdbcTemplate.update("DELETE FROM exam_assignment WHERE paper_id = ?", id);
            System.out.println("删除考试分配数量: " + deletedAssignments);
            
            // 5. 删除试卷题目关系
            int deletedQuestions = jdbcTemplate.update("DELETE FROM paper_question WHERE paper_id = ?", id);
            System.out.println("删除试卷题目关系数量: " + deletedQuestions);
            
            // 6. 最后删除试卷
            int deletedPapers = jdbcTemplate.update("DELETE FROM paper WHERE paper_id = ?", id);
            System.out.println("删除试卷数量: " + deletedPapers);
            
            System.out.println("试卷删除完成: " + id);
            
        } catch (Exception e) {
            System.err.println("删除试卷时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除试卷失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void forceDeletePaper(String id) {
        if (!paperRepository.existsById(id)) {
            throw new RuntimeException("试卷不存在");
        }
        
        System.out.println("强制删除试卷: " + id);
        
        // 删除所有相关的考试分配（包括已取消的）
        List<ExamAssignment> allAssignments = examAssignmentRepository.findByPaperId(id);
        for (ExamAssignment assignment : allAssignments) {
            // 删除分配的学生
            examAssignmentStudentRepository.deleteByIdAssignmentId(assignment.getAssignmentId());
            // 删除考试分配
            examAssignmentRepository.delete(assignment);
            System.out.println("删除考试分配: " + assignment.getAssignmentId());
        }
        
        // 先删除所有相关的考试会话答案
        try {
            examSessionAnswerRepository.deleteByPaperId(id);
            System.out.println("删除考试会话答案完成");
        } catch (Exception e) {
            System.out.println("删除考试会话答案时出错: " + e.getMessage());
        }
        
        // 再删除所有相关的考试会话
        try {
            examSessionRepository.deleteByPaperId(id);
            System.out.println("删除考试会话完成");
        } catch (Exception e) {
            System.out.println("删除考试会话时出错: " + e.getMessage());
        }
        
        // 删除试卷题目关系
        paperQuestionRepository.deleteByPaperId(id);
        System.out.println("删除试卷题目关系完成");
        
        // 最后删除试卷
        paperRepository.deleteById(id);
        System.out.println("删除试卷完成");
    }
    
    @Override
    @Transactional
    public void sqlForceDeletePaper(String id) {
        if (!paperRepository.existsById(id)) {
            throw new RuntimeException("试卷不存在");
        }
        
        System.out.println("使用SQL强制删除试卷: " + id);
        
        try {
            // 使用原生SQL删除，按正确顺序删除相关记录
            // 1. 删除考试会话答案
            jdbcTemplate.update("DELETE FROM exam_session_answer WHERE session_id IN (SELECT session_id FROM exam_session WHERE paper_id = ?)", id);
            System.out.println("删除考试会话答案完成");
            
            // 2. 删除考试会话
            jdbcTemplate.update("DELETE FROM exam_session WHERE paper_id = ?", id);
            System.out.println("删除考试会话完成");
            
            // 3. 删除考试分配学生
            jdbcTemplate.update("DELETE FROM exam_assignment_student WHERE assignment_id IN (SELECT assignment_id FROM exam_assignment WHERE paper_id = ?)", id);
            System.out.println("删除考试分配学生完成");
            
            // 4. 删除考试分配
            jdbcTemplate.update("DELETE FROM exam_assignment WHERE paper_id = ?", id);
            System.out.println("删除考试分配完成");
            
            // 5. 删除试卷题目关系
            jdbcTemplate.update("DELETE FROM paper_question WHERE paper_id = ?", id);
            System.out.println("删除试卷题目关系完成");
            
            // 6. 最后删除试卷
            jdbcTemplate.update("DELETE FROM paper WHERE paper_id = ?", id);
            System.out.println("删除试卷完成");
            
        } catch (Exception e) {
            System.err.println("SQL强制删除失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("强制删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public void togglePaperStatus(String id) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("试卷不存在"));
        
        String currentStatus = paper.getStatus();
        String newStatus = "启用".equals(currentStatus) ? "禁用" : "启用";
        paper.setStatus(newStatus);
        paper.setUpdatedAt(LocalDateTime.now());
        
        paperRepository.save(paper);
    }
    
    @Override
    public Page<Paper> searchPapers(String subjectId, String keyword, Pageable pageable) {
        return paperRepository.findByComplexCriteria(subjectId, keyword, pageable);
    }
    
    @Override
    public List<Paper> getAllPapers() {
        return paperRepository.findAllPapers();
    }
    
    @Override
    public List<PaperQuestion> getPaperQuestions(String paperId) {
        return paperQuestionRepository.findByPaperId(paperId);
    }
    
    @Override
    public List<Question> getPaperQuestionDetails(String paperId) {
        List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paperId);
        
        if (paperQuestions == null || paperQuestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 使用Set去重，避免重复题目
        Set<String> processedQuestionIds = new HashSet<>();
        List<Question> questions = new ArrayList<>();
        
        for (PaperQuestion pq : paperQuestions) {
            String questionId = pq.getQuestionId();
            
            // 跳过已处理的题目ID
            if (processedQuestionIds.contains(questionId)) {
                continue;
            }
            
            Question question = questionRepository.findById(questionId).orElse(null);
            if (question != null) {
                questions.add(question);
                processedQuestionIds.add(questionId);
            }
        }
        
        return questions;
    }
    
    @Override
    public Paper createPaper(Paper paper, List<String> questionIds) {
        // 修复：生成试卷ID
        String paperId = generatePaperId();
        paper.setPaperId(paperId);
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        
        // 设置题目数量
        paper.setQuestionCount(questionIds.size());
        
        Paper savedPaper = paperRepository.save(paper);
        
        int totalScore = 0;
        for (String questionId : questionIds) {
            Question question = questionRepository.findById(questionId).orElse(null);
            if (question != null) {
                int score = calculateQuestionScore(question);
                PaperQuestion paperQuestion = new PaperQuestion(savedPaper.getPaperId(), questionId, score);
                paperQuestionRepository.save(paperQuestion);
                totalScore += score;
            }
        }
        
        // 更新试卷总分
        savedPaper.setTotalScore(totalScore);
        return paperRepository.save(savedPaper);
    }
    
    @Override
    public Paper updatePaper(Paper paper, List<String> questionIds) {
        Paper existingPaper = getPaperById(paper.getPaperId());
        
        existingPaper.setPaperName(paper.getPaperName());
        existingPaper.setSubjectId(paper.getSubjectId());
        existingPaper.setDuration(paper.getDuration());
        existingPaper.setDifficultyLevel(paper.getDifficultyLevel());
        existingPaper.setUpdatedAt(LocalDateTime.now());
        
        // 删除原有题目
        paperQuestionRepository.deleteByPaperId(paper.getPaperId());
        
        // 添加新题目
        int totalScore = 0;
        for (String questionId : questionIds) {
            Question question = questionRepository.findById(questionId).orElse(null);
            if (question != null) {
                int score = calculateQuestionScore(question);
                PaperQuestion paperQuestion = new PaperQuestion(paper.getPaperId(), questionId, score);
                paperQuestionRepository.save(paperQuestion);
                totalScore += score;
            }
        }
        
        existingPaper.setTotalScore(totalScore);
        return paperRepository.save(existingPaper);
    }
    
    @Override
    public Page<Paper> searchPapers(String creatorId, String subjectId, String keyword, Pageable pageable) {
        // 添加调试信息
        System.out.println("=== PaperServiceImpl.searchPapers 调试信息 ===");
        System.out.println("参数 - creatorId: " + creatorId + ", subjectId: " + subjectId + ", keyword: " + keyword);
        
        // 修复：使用正确的查询方法，按创建者过滤
        if (creatorId == null || creatorId.trim().isEmpty()) {
            // 如果没有创建者ID，返回空结果
            System.out.println("creatorId 为空，返回空结果");
            return Page.empty(pageable);
        }
        
        // 修复：处理空字符串参数
        String normalizedSubjectId = (subjectId == null || subjectId.trim().isEmpty()) ? null : subjectId;
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;
        
        Page<Paper> result = paperRepository.findByCreatorAndComplexCriteria(creatorId, normalizedSubjectId, normalizedKeyword, pageable);
        System.out.println("查询结果 - 总数量: " + result.getTotalElements());
        System.out.println("查询结果 - 当前页数量: " + result.getContent().size());
        
        return result;
    }
    
    @Override
    public List<Paper> getPapersByCreator(String creatorId) {
        if (creatorId == null || creatorId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return paperRepository.findByCreatorId(creatorId);
    }
    
    @Override
    public long getPaperCountByCreator(String creatorId) {
        // 简化实现，返回总数
        return paperRepository.count();
    }
    
    @Override
    public int calculateQuestionScore(Question question) {
        switch (question.getQuestionType()) {
            case "C": return 2;  // 选择题
            case "F": return 3;  // 填空题
            case "R": return 5;  // 程序运行结果题
            case "S": return 8;  // 简答题
            case "P": return 15; // 编程题
            default: return 5;
        }
    }
    
    @Override
    public Map<String, Object> getPaperStatistics(String creatorId) {
        Map<String, Object> statistics = new HashMap<>();
        
        long totalPapers = paperRepository.count();
        // 简化实现，假设所有试卷都是活跃的
        long activePapers = totalPapers;
        
        statistics.put("totalPapers", totalPapers);
        statistics.put("activePapers", activePapers);
        
        return statistics;
    }
    
    @Override
    public PaperGenerationResult generatePaper(PaperGenerationRequest request) {
        try {
            // 1. 验证请求参数
            if (!validatePaperParameters(request)) {
                return PaperGenerationResult.failure("请求参数无效");
            }

            // 2. 创建试卷
            Paper paper = new Paper();
            paper.setPaperName(request.getPaperName());
            paper.setSubjectId(request.getSubjectId());
            paper.setCreatorId(request.getCreatorId());
            paper.setDuration(request.getDuration());
            paper.setTotalScore(request.getTotalScore());
            paper.setDifficultyLevel(request.getDifficultyLevel());

            // 3. 智能选择题目
            List<String> selectedQuestionIds = selectQuestionsIntelligently(request);
            if (selectedQuestionIds.isEmpty()) {
                return PaperGenerationResult.failure("题库中没有足够的题目满足要求");
            }

            // 4. 计算各题目分值
            List<Integer> scores = calculateQuestionScores(selectedQuestionIds, request.getTotalScore());

            // 5. 保存试卷
            Paper savedPaper = createPaper(paper, selectedQuestionIds, scores);

            // 6. 生成答案
            String answerKey = generateAnswerKey(savedPaper.getPaperId());

            return PaperGenerationResult.success(savedPaper, answerKey);
        } catch (Exception e) {
            return PaperGenerationResult.failure("试卷生成失败：" + e.getMessage());
        }
    }
    
    @Override
    public String generateAnswerKey(String paperId) {
        List<PaperQuestion> paperQuestions = getPaperQuestions(paperId);
        StringBuilder answerKey = new StringBuilder();
        
        for (PaperQuestion pq : paperQuestions) {
            answerKey.append("题目ID: ").append(pq.getQuestionId()).append("\n");
            answerKey.append("分值: ").append(pq.getScore()).append("\n\n");
        }
        
        return answerKey.toString();
    }
    
    @Override
    public long getPaperCount() {
        return paperRepository.count();
    }
    
    @Override
    public boolean validatePaperParameters(PaperGenerationRequest request) {
        return request.getPaperName() != null && !request.getPaperName().trim().isEmpty();
    }
    
    @Override
    public double calculatePaperDifficulty(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return 0.0;
        }

        double totalDifficulty = 0.0;
        int count = 0;

        for (String questionId : questionIds) {
            try {
                Question question = questionRepository.findById(questionId).orElse(null);
                if (question != null) {
                    switch (question.getDifficulty()) {
                        case "easy":
                            totalDifficulty += 1.0;
                            break;
                        case "medium":
                            totalDifficulty += 3.0;
                            break;
                        case "hard":
                            totalDifficulty += 5.0;
                            break;
                    }
                    count++;
                }
            } catch (Exception e) {
                // 忽略无效题目
            }
        }

        return count > 0 ? totalDifficulty / count : 0.0;
    }

    // 智能选择题目
    private List<String> selectQuestionsIntelligently(PaperGenerationRequest request) {
        List<String> selectedQuestionIds = new ArrayList<>();
        String targetDifficulty = request.getDifficultyLevel();

        // 根据难度系数计算各难度级别的题目比例
        Map<String, Double> difficultyRatios = calculateDifficultyRatios(targetDifficulty);

        // 为每种题型选择题目
        for (QuestionTypeConfig config : request.getQuestionTypeConfigs()) {
            if (config.getCount() <= 0) continue;

            List<String> typeQuestionIds = selectQuestionsByTypeAndDifficulty(
                request.getSubjectId(),
                config.getQuestionType(),
                config.getCount(),
                difficultyRatios
            );

            selectedQuestionIds.addAll(typeQuestionIds);
        }

        return selectedQuestionIds;
    }

    // 计算难度比例
    private Map<String, Double> calculateDifficultyRatios(String targetDifficulty) {
        Map<String, Double> ratios = new HashMap<>();

        if ("easy".equals(targetDifficulty)) {
            // 简单试卷
            ratios.put("easy", 0.6);
            ratios.put("medium", 0.3);
            ratios.put("hard", 0.1);
        } else if ("medium".equals(targetDifficulty)) {
            // 中等试卷
            ratios.put("easy", 0.3);
            ratios.put("medium", 0.5);
            ratios.put("hard", 0.2);
        } else {
            // 困难试卷
            ratios.put("easy", 0.1);
            ratios.put("medium", 0.3);
            ratios.put("hard", 0.6);
        }

        return ratios;
    }

    // 根据题型和难度选择题目
    private List<String> selectQuestionsByTypeAndDifficulty(String subjectId, String questionType, 
                                                           int count, Map<String, Double> difficultyRatios) {
        List<String> selectedIds = new ArrayList<>();
        Set<String> usedKnowledgePoints = new HashSet<>();

        for (Map.Entry<String, Double> entry : difficultyRatios.entrySet()) {
            String difficulty = entry.getKey();
            double ratio = entry.getValue();
            int difficultyCount = (int) Math.round(count * ratio);

            if (difficultyCount <= 0) continue;

            List<Question> questions = questionRepository.findRandomQuestionsByTypeAndDifficulty(
                subjectId, questionType, difficulty, difficultyCount * 2); // 获取更多候选题目

            // 避免知识点重复
            for (Question question : questions) {
                if (selectedIds.size() >= difficultyCount) break;

                String knowledgePoint = question.getKnowledgeTag();
                if (knowledgePoint == null || !usedKnowledgePoints.contains(knowledgePoint)) {
                    selectedIds.add(question.getId());
                    if (knowledgePoint != null) {
                        usedKnowledgePoints.add(knowledgePoint);
                    }
                }
            }
        }

        return selectedIds;
    }

    // 计算题目分值
    private List<Integer> calculateQuestionScores(List<String> questionIds, int totalScore) {
        List<Integer> scores = new ArrayList<>();
        int questionCount = questionIds.size();

        if (questionCount == 0) return scores;

        // 简单平均分配
        int baseScore = totalScore / questionCount;
        int remainder = totalScore % questionCount;

        for (int i = 0; i < questionCount; i++) {
            int score = baseScore + (i < remainder ? 1 : 0);
            scores.add(score);
        }

        return scores;
    }
    
    // 生成试卷ID
    private String generatePaperId() {
        // 生成格式：P + 6位数字，如 P000001
        long count = paperRepository.count() + 1;
        return String.format("P%06d", count);
    }
} 