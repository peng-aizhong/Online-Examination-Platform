package com.exam.controller;

import com.exam.entity.*;
import com.exam.service.*;
import com.exam.repository.ExamSessionRepository;
import com.exam.repository.ExamAssignmentRepository;
import com.exam.repository.ExamAssignmentStudentRepository;
import com.exam.repository.LearningResourceRepository;
import com.exam.repository.PaperQuestionRepository;
import com.exam.dto.ImportResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


@Controller
@RequestMapping("/teacher")
public class TeacherController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private PaperService paperService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private LearningResourceService learningResourceService;
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    @Autowired
    private LearningResourceRepository learningResourceRepository;
    
    @Autowired
    private ExamSessionService examSessionService;
    
    @Autowired
    private ExamAssignmentService examAssignmentService;
    
    @Autowired
    private ExamAssignmentRepository examAssignmentRepository;
    
    @Autowired
    private ExamAssignmentStudentRepository examAssignmentStudentRepository;
    
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    
    // 教师端首页
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        if (user == null || !"teacher".equals(user.getRole().toLowerCase())) {
            return "redirect:/login";
        }
        
        // 获取统计数据
        Map<String, Object> statistics = getTeacherStatistics(user);
        model.addAttribute("statistics", statistics);
        model.addAttribute("teacher", user);
        model.addAttribute("user", user); // 添加user属性供模板使用
        
        return "teacher/dashboard";
    }
    
    // ==================== 题库管理模块 ====================
    

    
    // 题库管理首页
    @GetMapping("/questions")
    public String questionList(@RequestParam(required = false) String subjectId,
                              @RequestParam(required = false) String questionId,
                              @RequestParam(required = false) String questionType,
                              @RequestParam(required = false) String difficulty,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              Model model) {
        
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        model.addAttribute("user", user);
        
        Pageable pageable = PageRequest.of(page, size);
        
        // 获取题目列表
        Page<Question> questions;
        if (subjectId == null && questionId == null && questionType == null && 
            difficulty == null && status == null && (keyword == null || keyword.trim().isEmpty())) {
            // 如果没有筛选条件，获取所有题目
            questions = questionService.getAllQuestionsPageable(pageable);
        } else {
            // 有筛选条件时使用搜索
            // 修复：使用正确的参数顺序 (subjectId, questionId, questionType, difficulty, search, pageable)
            questions = questionService.searchQuestions(
                subjectId, questionId, questionType, difficulty, keyword, pageable);
        }
        
        // 获取科目列表
        List<Subject> subjects = subjectService.getAllSubjects();
        
        // 获取知识点标签
        List<String> knowledgeTags = questionService.getAllKnowledgeTags();
        
        model.addAttribute("questions", questions.getContent());
        model.addAttribute("totalPages", questions.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", questions.getTotalElements());
        model.addAttribute("subjects", subjects);
        model.addAttribute("knowledgeTags", knowledgeTags);
        Map<String, String> filters = new HashMap<>();
        filters.put("subjectId", subjectId);
        filters.put("questionId", questionId);
        filters.put("questionType", questionType);
        filters.put("difficulty", difficulty);
        filters.put("status", status);
        filters.put("keyword", keyword);
        model.addAttribute("filters", filters);
        
        return "teacher/question/list";
    }
    
    // 添加题目页面
    @GetMapping("/questions/add")
    public String addQuestionForm(Model model) {
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        model.addAttribute("user", user);
        
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        model.addAttribute("question", new Question());
        return "teacher/question/add";
    }
    
    // 添加题目
    @PostMapping("/questions")
    public String addQuestion(@RequestParam("subjectId") String subjectId,
                             @RequestParam("questionType") String questionType,
                             @RequestParam("difficulty") String difficulty,
                             @RequestParam(value = "knowledgeTag", required = false) String knowledgeTag,
                             @RequestParam("content") String content,
                             @RequestParam("answer") String answer,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "options", required = false) String options,
                             RedirectAttributes redirectAttributes) {
        
        System.out.println("=== 题目添加开始 ===");
        System.out.println("科目ID: " + subjectId);
        System.out.println("题目类型: " + questionType);
        System.out.println("难度: " + difficulty);
        System.out.println("知识点: " + knowledgeTag);
        System.out.println("题目内容: " + (content != null ? content.length() + "字符" : "null"));
        System.out.println("答案: " + (answer != null ? answer.length() + "字符" : "null"));
        System.out.println("状态: " + status);
        System.out.println("选项: " + options);
        
        try {
            // 验证必填参数
            if (subjectId == null || subjectId.trim().isEmpty()) {
                throw new RuntimeException("科目不能为空");
            }
            if (questionType == null || questionType.trim().isEmpty()) {
                throw new RuntimeException("题目类型不能为空");
            }
            if (difficulty == null || difficulty.trim().isEmpty()) {
                throw new RuntimeException("难度不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("题目内容不能为空");
            }
            if (answer == null || answer.trim().isEmpty()) {
                throw new RuntimeException("标准答案不能为空");
            }
            
            // 创建Question对象
            Question question = new Question();
            question.setSubjectId(subjectId.trim());
            question.setQuestionType(questionType.trim());
            question.setDifficulty(difficulty.trim());
            question.setKnowledgeTag(knowledgeTag != null ? knowledgeTag.trim() : null);
            question.setContent(content.trim());
            question.setAnswer(answer.trim());
            question.setStatus(status != null && !status.trim().isEmpty() ? status.trim() : "启用");
            
            // 处理选项字段
            if ("C".equals(questionType)) {

                
                // 优先使用表单提交的选项数据
                if (options != null && !options.trim().isEmpty()) {
                    try {
                        // 验证是否为有效的JSON格式
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        mapper.readTree(options); // 验证JSON格式
                        String parsedOptionsArray = options.trim();
                        System.out.println("使用表单提交的选项数据: " + parsedOptionsArray);
                    } catch (Exception e) {
                        // 如果不是有效JSON，则从内容中解析
                        String[] parsedOptionsArray = parseOptionsToArray(content);
                        question.setOptionA(parsedOptionsArray[0]);
                        question.setOptionB(parsedOptionsArray[1]);
                        question.setOptionC(parsedOptionsArray[2]);
                        question.setOptionD(parsedOptionsArray[3]);
                        System.out.println("表单选项数据无效，从内容解析");
                    }
                } else {
                    // 如果没有提交选项数据，从内容中解析
                    String[] parsedOptionsArray = parseOptionsToArray(content);
                    question.setOptionA(parsedOptionsArray[0]);
                    question.setOptionB(parsedOptionsArray[1]);
                    question.setOptionC(parsedOptionsArray[2]);
                    question.setOptionD(parsedOptionsArray[3]);
                    System.out.println("没有提交选项数据，从内容解析");
                }
                
                // 选项已在上面的解析中设置到独立字段
                
                // 清理题目内容，移除选项部分
                String cleanedContent = cleanContentFromOptions(content);
                question.setContent(cleanedContent);
                
                System.out.println("选择题选项已解析");
                System.out.println("清理后的题目内容: " + cleanedContent);
            } else {
                question.setOptionA(null); question.setOptionB(null); question.setOptionC(null); question.setOptionD(null); // 非选择题不存options
                System.out.println("非选择题，选项设置为null");
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            System.out.println("当前用户: " + (user != null ? user.getUsername() : "未知"));
            
            System.out.println("准备调用questionService.addQuestion()");
            Question savedQuestion = questionService.addQuestion(question);
            System.out.println("题目保存成功，ID: " + savedQuestion.getId());
            
            redirectAttributes.addFlashAttribute("success", "题目添加成功！题目ID: " + savedQuestion.getId());
            return "redirect:/teacher/questions/add";
        } catch (Exception e) {
            System.err.println("=== 题目添加失败 ===");
            e.printStackTrace(); // 打印详细错误信息到控制台
            redirectAttributes.addFlashAttribute("error", "题目添加失败：" + e.getMessage());
            return "redirect:/teacher/questions/add";
        }
    }
    
    // 编辑题目页面
    @GetMapping("/questions/{id}/edit")
    public String editQuestionForm(@PathVariable String id, Model model) {
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        model.addAttribute("user", user);
        
        Question question = questionService.getQuestionById(id);
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("question", question);
        model.addAttribute("subjects", subjects);
        return "teacher/question/edit";
    }
    
        // 更新题目
    @PostMapping("/questions/{id}")
    public String updateQuestion(@PathVariable String id,
                                @RequestParam String subjectId,
                                @RequestParam String questionType,
                                @RequestParam String difficulty,
                                @RequestParam(required = false) String knowledgeTag,
                                @RequestParam String content,
                                @RequestParam String answer,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String options,
                                RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== 更新题目开始 ===");
            System.out.println("题目ID: " + id);
            System.out.println("题目类型: " + questionType);
            System.out.println("难度: " + difficulty);
            System.out.println("知识点: " + knowledgeTag);
            System.out.println("题目内容: " + (content != null ? content.length() + "字符" : "null"));
            System.out.println("答案: " + (answer != null ? answer.length() + "字符" : "null"));
            System.out.println("状态: " + status);
            System.out.println("选项: " + options);
            
            // 验证必填参数
            if (subjectId == null || subjectId.trim().isEmpty()) {
                throw new RuntimeException("科目不能为空");
            }
            if (questionType == null || questionType.trim().isEmpty()) {
                throw new RuntimeException("题目类型不能为空");
            }
            if (difficulty == null || difficulty.trim().isEmpty()) {
                throw new RuntimeException("难度不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("题目内容不能为空");
            }
            if (answer == null || answer.trim().isEmpty()) {
                throw new RuntimeException("标准答案不能为空");
            }
            
            // 创建Question对象
            Question question = new Question();
            question.setId(id);
            question.setSubjectId(subjectId.trim());
            question.setQuestionType(questionType.trim());
            question.setDifficulty(difficulty.trim());
            question.setKnowledgeTag(knowledgeTag != null ? knowledgeTag.trim() : null);
            question.setContent(content.trim());
            question.setAnswer(answer.trim());
            question.setStatus(status != null && !status.trim().isEmpty() ? status.trim() : "启用");
            
            // 处理选项字段
            if ("C".equals(questionType)) {

                
                // 优先使用表单提交的选项数据
                if (options != null && !options.trim().isEmpty()) {
                    try {
                        // 验证是否为有效的JSON格式
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        mapper.readTree(options); // 验证JSON格式
                        String parsedOptionsArray = options.trim();
                        System.out.println("使用表单提交的选项数据: " + parsedOptionsArray);
                    } catch (Exception e) {
                        // 如果不是有效JSON，则从内容中解析
                        String[] parsedOptionsArray = parseOptionsToArray(content);
                        question.setOptionA(parsedOptionsArray[0]);
                        question.setOptionB(parsedOptionsArray[1]);
                        question.setOptionC(parsedOptionsArray[2]);
                        question.setOptionD(parsedOptionsArray[3]);
                        System.out.println("表单选项数据无效，从内容解析");
                    }
                } else {
                    // 如果没有提交选项数据，从内容中解析
                    String[] parsedOptionsArray = parseOptionsToArray(content);
                    question.setOptionA(parsedOptionsArray[0]);
                    question.setOptionB(parsedOptionsArray[1]);
                    question.setOptionC(parsedOptionsArray[2]);
                    question.setOptionD(parsedOptionsArray[3]);
                    System.out.println("没有提交选项数据，从内容解析");
                }
                
                // 选项已在上面的解析中设置到独立字段
                
                // 清理题目内容，移除选项部分
                String cleanedContent = cleanContentFromOptions(content);
                question.setContent(cleanedContent);
                
                System.out.println("选择题选项已解析");
                System.out.println("清理后的题目内容: " + cleanedContent);
            } else {
                question.setOptionA(null); question.setOptionB(null); question.setOptionC(null); question.setOptionD(null); // 非选择题不存options
                System.out.println("非选择题，选项设置为null");
            }
            
            questionService.updateQuestion(id, question);
            redirectAttributes.addFlashAttribute("success", "题目更新成功！");
            return "redirect:/teacher/questions";
        } catch (Exception e) {
            System.err.println("=== 题目更新失败 ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "题目更新失败：" + e.getMessage());
            return "redirect:/teacher/questions/" + id + "/edit";
        }
    }
    
    // 删除题目
    @PostMapping("/questions/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteQuestion(@PathVariable String id) {
        try {
            questionService.deleteQuestion(id);
                    Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "题目删除成功");
        return response;
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "题目删除失败：" + e.getMessage());
        return response;
        }
    }
    
    // 切换题目状态
    @PostMapping("/questions/{id}/toggle-status")
    @ResponseBody
    public Map<String, Object> toggleQuestionStatus(@PathVariable String id) {
        try {
            Question question = questionService.getQuestionById(id);
            String newStatus = "启用".equals(question.getStatus()) ? "禁用" : "启用";
            question.setStatus(newStatus);
            questionService.updateQuestion(id, question);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "题目状态已切换为：" + newStatus);
            response.put("newStatus", newStatus);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "状态切换失败：" + e.getMessage());
            return response;
        }
    }
    
    // 批量导入题目
    @PostMapping("/questions/import")
    @ResponseBody
    public Map<String, Object> importQuestions(@RequestParam("file") MultipartFile file,
                                              @RequestParam("subjectId") String subjectId) {
        try {
            ImportResult result = questionService.importQuestionsFromFile(file, subjectId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrorCount());
            if (result.getErrorDetails() != null && !result.getErrorDetails().isEmpty()) {
                response.put("errorDetails", result.getErrorDetails());
            }
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "导入失败：" + e.getMessage());
            return response;
        }
    }
    
    // 导出题库
    @GetMapping("/questions/export")
    public void exportQuestions(@RequestParam(required = false) String subjectId,
                               @RequestParam(required = false) String questionType,
                               @RequestParam(required = false) String difficulty,
                               @RequestParam(required = false) String status,
                               HttpServletResponse response) {
        try {
            questionService.exportQuestions(subjectId, questionType, difficulty, status, response);
        } catch (Exception e) {
            // 处理导出异常
        }
    }
    
    // 题目搜索
    @PostMapping("/questions/search")
    @ResponseBody
    public Map<String, Object> searchQuestions(@RequestParam String subjectId,
                                             @RequestParam(required = false) String questionId,
                                             @RequestParam(required = false) String questionType,
                                             @RequestParam(required = false) String difficulty,
                                             @RequestParam(required = false) String search,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Question> questions = questionService.searchQuestions(subjectId, questionId, questionType, difficulty, search, pageable);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("questions", questions.getContent());
            result.put("totalElements", questions.getTotalElements());
            result.put("totalPages", questions.getTotalPages());
            result.put("currentPage", questions.getNumber());
            result.put("size", questions.getSize());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "搜索失败: " + e.getMessage());
            return result;
        }
    }
    
    // 获取题目统计信息
    @GetMapping("/questions/statistics")
    @ResponseBody
    public Map<String, Object> getQuestionStatistics(@RequestParam(required = false) String subjectId) {
        return questionService.getQuestionStatistics(subjectId);
    }
    

    
    // 更新题目内容（用于试卷编辑）
    @PostMapping("/questions/update")
    @ResponseBody
    public Map<String, Object> updateQuestion(@RequestBody Map<String, Object> questionData) {
        try {
            String questionId = (String) questionData.get("id");
            String content = (String) questionData.get("content");
            String answer = (String) questionData.get("answer");
            
            Question question = questionService.getQuestionById(questionId);
            question.setContent(content);
            question.setAnswer(answer);
            
            // 如果是选择题，从内容中解析选项
            if ("C".equals(question.getQuestionType())) {
                String[] parsedOptionsArrayArray = parseOptionsToArray(content);
                question.setOptionA(parsedOptionsArrayArray[0]);
                question.setOptionB(parsedOptionsArrayArray[1]);
                question.setOptionC(parsedOptionsArrayArray[2]);
                question.setOptionD(parsedOptionsArrayArray[3]);
                // 选项已在上面的解析中设置到独立字段
                System.out.println("AJAX更新题目时解析选项: A=" + parsedOptionsArrayArray[0] + ", B=" + parsedOptionsArrayArray[1] + ", C=" + parsedOptionsArrayArray[2] + ", D=" + parsedOptionsArrayArray[3]);
            }
            
            questionService.updateQuestion(questionId, question);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "题目更新成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "题目更新失败: " + e.getMessage());
            return result;
        }
    }

    // 一键清空题库（按科目）
    @PostMapping("/questions/clear")
    @ResponseBody
    public Map<String, Object> clearQuestionBank(@RequestParam String subjectId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (subjectId == null || subjectId.trim().isEmpty()) {
                throw new RuntimeException("科目ID不能为空");
            }
            int deleted = questionService.clearQuestionsBySubject(subjectId);
            response.put("success", true);
            response.put("deleted", deleted);
            response.put("message", "清空成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    // 获取单个题目详情
    @GetMapping("/questions/{id}")
    @ResponseBody
    public Map<String, Object> getQuestionDetail(@PathVariable String id) {
        try {
            Question question = questionService.getQuestionById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("question", question);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取题目详情失败：" + e.getMessage());
            return response;
        }
    }
    
    // 随机选题接口
    @PostMapping("/questions/random")
    @ResponseBody
    public Map<String, Object> getRandomQuestions(@RequestParam String subjectId,
                                                 @RequestParam String questionType,
                                                 @RequestParam(required = false) String difficulty,
                                                 @RequestParam int count) {
        try {
            // 如果没有指定难度，根据难度系数自动选择
            String targetDifficulty = difficulty;
            if (targetDifficulty == null || targetDifficulty.isEmpty()) {
                targetDifficulty = "medium"; // 默认中等难度
            }
            
            List<Question> questions = questionService.findRandomQuestionsByTypeAndDifficulty(
                subjectId, questionType, targetDifficulty, count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", questions);
            response.put("count", questions.size());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "随机选题失败：" + e.getMessage());
            return response;
        }
    }
    
    // 自动生成试卷接口
    @PostMapping("/papers/auto-generate")
    @ResponseBody
    public Map<String, Object> autoGeneratePaper(@RequestParam String subjectId,
                                                @RequestParam String difficultyLevel,
                                                @RequestParam int choiceCount,
                                                @RequestParam int fillCount,
                                                @RequestParam int resultCount,
                                                @RequestParam int shortCount,
                                                @RequestParam int programCount) {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Question> allQuestions = new ArrayList<>();
            
            // 根据难度系数确定各难度级别的比例
            Map<String, Double> difficultyRatios = calculateDifficultyRatios(difficultyLevel);
            
            // 为每种题型随机选择题目
            if (choiceCount > 0) {
                List<Question> choiceQuestions = selectQuestionsByTypeAndDifficulty(subjectId, "C", choiceCount, difficultyRatios);
                allQuestions.addAll(choiceQuestions);
            }
            
            if (fillCount > 0) {
                List<Question> fillQuestions = selectQuestionsByTypeAndDifficulty(subjectId, "F", fillCount, difficultyRatios);
                allQuestions.addAll(fillQuestions);
            }
            
            if (resultCount > 0) {
                List<Question> resultQuestions = selectQuestionsByTypeAndDifficulty(subjectId, "R", resultCount, difficultyRatios);
                allQuestions.addAll(resultQuestions);
            }
            
            if (shortCount > 0) {
                List<Question> shortQuestions = selectQuestionsByTypeAndDifficulty(subjectId, "S", shortCount, difficultyRatios);
                allQuestions.addAll(shortQuestions);
            }
            
            if (programCount > 0) {
                List<Question> programQuestions = selectQuestionsByTypeAndDifficulty(subjectId, "P", programCount, difficultyRatios);
                allQuestions.addAll(programQuestions);
            }
            
            response.put("success", true);
            response.put("questions", allQuestions);
            response.put("message", "成功生成" + allQuestions.size() + "道题目");
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "自动生成试卷失败：" + e.getMessage());
            return response;
        }
    }
    
    // 计算难度比例
    private Map<String, Double> calculateDifficultyRatios(String difficultyLevel) {
        Map<String, Double> ratios = new HashMap<>();
        
        if ("easy".equals(difficultyLevel)) {
            // 简单试卷
            ratios.put("easy", 0.6);
            ratios.put("medium", 0.3);
            ratios.put("hard", 0.1);
        } else if ("medium".equals(difficultyLevel)) {
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
    private List<Question> selectQuestionsByTypeAndDifficulty(String subjectId, String questionType, 
                                                             int count, Map<String, Double> difficultyRatios) {
        List<Question> selectedQuestions = new ArrayList<>();
        Set<String> usedKnowledgePoints = new HashSet<>();
        
        // 如果只需要1道题，直接随机选择
        if (count == 1) {
            List<Question> questions = questionService.findRandomQuestionsByTypeAndDifficulty(
                subjectId, questionType, "medium", 5); // 获取5道候选题目
            if (!questions.isEmpty()) {
                selectedQuestions.add(questions.get(0));
            }
            return selectedQuestions;
        }
        
        for (Map.Entry<String, Double> entry : difficultyRatios.entrySet()) {
            String difficulty = entry.getKey();
            double ratio = entry.getValue();
            int difficultyCount = (int) Math.round(count * ratio);
            
            if (difficultyCount <= 0) continue;
            
            // 获取更多候选题目，确保有足够的选择
            List<Question> questions = questionService.findRandomQuestionsByTypeAndDifficulty(
                subjectId, questionType, difficulty, Math.max(difficultyCount * 3, 10));
            
            // 避免知识点重复
            for (Question question : questions) {
                if (selectedQuestions.size() >= difficultyCount) break;
                
                String knowledgePoint = question.getKnowledgeTag();
                if (knowledgePoint == null || !usedKnowledgePoints.contains(knowledgePoint)) {
                    selectedQuestions.add(question);
                    if (knowledgePoint != null) {
                        usedKnowledgePoints.add(knowledgePoint);
                    }
                }
            }
        }
        
        // 如果按难度分布没有选够题目，直接补充
        if (selectedQuestions.size() < count) {
            List<Question> additionalQuestions = questionService.findRandomQuestionsByTypeAndDifficulty(
                subjectId, questionType, "medium", count - selectedQuestions.size() + 5);
            
            for (Question question : additionalQuestions) {
                if (selectedQuestions.size() >= count) break;
                
                String knowledgePoint = question.getKnowledgeTag();
                if (knowledgePoint == null || !usedKnowledgePoints.contains(knowledgePoint)) {
                    selectedQuestions.add(question);
                    if (knowledgePoint != null) {
                        usedKnowledgePoints.add(knowledgePoint);
                    }
                }
            }
        }
        
        // 确保返回正确数量的题目
        if (selectedQuestions.size() > count) {
            selectedQuestions = selectedQuestions.subList(0, count);
        }
        
        return selectedQuestions;
    }

    // ==================== 试卷管理模块 ====================
    
    // 试卷列表
    @GetMapping("/papers")
    public String paperList(@RequestParam(required = false) String subjectId,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login?error=用户未找到";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        
        // 添加调试信息
        System.out.println("=== 试卷列表查询调试信息 ===");
        System.out.println("当前用户ID: " + user.getUserId());
        System.out.println("当前用户名: " + user.getUsername());
        System.out.println("查询参数 - subjectId: " + subjectId + ", keyword: " + keyword + ", page: " + page + ", size: " + size);
        
        // 修复：使用正确的查询方法，按创建者过滤
        Page<Paper> papers = paperService.searchPapers(user.getUserId(), subjectId, keyword, pageable);
        
        // 添加查询结果调试信息
        System.out.println("查询结果 - 总数量: " + papers.getTotalElements());
        System.out.println("查询结果 - 当前页数量: " + papers.getContent().size());
        System.out.println("查询结果 - 总页数: " + papers.getTotalPages());
        
        // 添加数据库直接查询调试信息
        try {
            List<Paper> allPapers = paperService.getAllPapers();
            System.out.println("数据库中所有试卷数量: " + allPapers.size());
            for (Paper p : allPapers) {
                System.out.println("试卷: " + p.getPaperId() + " - " + p.getPaperName() + " - 创建者: " + p.getCreatorId());
            }
            
            List<Paper> userPapers = paperService.getPapersByCreator(user.getUserId());
            System.out.println("当前用户创建的试卷数量: " + userPapers.size());
            for (Paper p : userPapers) {
                System.out.println("用户试卷: " + p.getPaperId() + " - " + p.getPaperName());
            }
        } catch (Exception e) {
            System.err.println("调试查询失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        List<Subject> subjects = subjectService.getAllSubjects();
        
        // 由于使用了JOIN FETCH，关联数据已经被预加载
        List<Paper> enrichedPapers = papers.getContent();
        
        // 添加试卷统计信息
        long totalPapers = papers.getTotalElements();
        long enabledPapers = enrichedPapers.stream()
                .filter(p -> "启用".equals(p.getStatus()))
                .count();
        long disabledPapers = enrichedPapers.stream()
                .filter(p -> "禁用".equals(p.getStatus()))
                .count();
        long highDifficultyPapers = enrichedPapers.stream()
                .filter(p -> p.getDifficultyLevel() != null && 
                           ("hard".equals(p.getDifficultyLevel()) || 
                            "困难".equals(p.getDifficultyLevel()) ||
                            "高难度".equals(p.getDifficultyLevel())))
                .count();
        
        // 修复：确保将用户信息添加到模型中
        model.addAttribute("user", user);
        model.addAttribute("papers", enrichedPapers);
        model.addAttribute("subjects", subjects);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", papers.getTotalPages());
        model.addAttribute("totalElements", totalPapers);
        model.addAttribute("totalPapers", totalPapers);
        model.addAttribute("enabledPapers", enabledPapers);
        model.addAttribute("disabledPapers", disabledPapers);
        model.addAttribute("highDifficultyPapers", highDifficultyPapers);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("keyword", keyword);
        
        return "teacher/paper/list";
    }
    
    // 调试端点：查看试卷数据
    @GetMapping("/papers/debug")
    @ResponseBody
    public Map<String, Object> debugPapers() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                result.put("error", "用户未找到");
                return result;
            }
            
            result.put("userId", user.getUserId());
            result.put("username", user.getUsername());
            
            // 获取所有试卷
            List<Paper> allPapers = paperService.getAllPapers();
            result.put("totalPapers", allPapers.size());
            
            List<Map<String, Object>> paperDetails = new ArrayList<>();
            for (Paper paper : allPapers) {
                Map<String, Object> paperInfo = new HashMap<>();
                paperInfo.put("paperId", paper.getPaperId());
                paperInfo.put("paperName", paper.getPaperName());
                paperInfo.put("creatorId", paper.getCreatorId());
                paperInfo.put("subjectId", paper.getSubjectId());
                paperInfo.put("status", paper.getStatus());
                paperInfo.put("createdAt", paper.getCreatedAt());
                paperDetails.add(paperInfo);
            }
            result.put("papers", paperDetails);
            
            // 获取用户创建的试卷
            List<Paper> userPapers = paperService.getPapersByCreator(user.getUserId());
            result.put("userPapers", userPapers.size());
            
            List<Map<String, Object>> userPaperDetails = new ArrayList<>();
            for (Paper paper : userPapers) {
                Map<String, Object> paperInfo = new HashMap<>();
                paperInfo.put("paperId", paper.getPaperId());
                paperInfo.put("paperName", paper.getPaperName());
                paperInfo.put("creatorId", paper.getCreatorId());
                paperInfo.put("subjectId", paper.getSubjectId());
                paperInfo.put("status", paper.getStatus());
                paperInfo.put("createdAt", paper.getCreatedAt());
                userPaperDetails.add(paperInfo);
            }
            result.put("userPaperDetails", userPaperDetails);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 创建试卷页面
    @GetMapping("/papers/create")
    public String createPaperForm(Model model) {
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        model.addAttribute("paper", new Paper());
        return "teacher/paper/create";
    }
    
    // 保存试卷
    @PostMapping("/papers")
    public String createPaper(@ModelAttribute Paper paper,
                           @RequestParam("questionIds") List<String> questionIds,
                           @RequestParam(value = "questionScores", required = false) List<Integer> questionScores,
                           @RequestParam(value = "difficultyLevel", required = false) String difficultyLevel,
                           @RequestParam(value = "choiceCount", required = false) Integer choiceCount,
                           @RequestParam(value = "fillCount", required = false) Integer fillCount,
                           @RequestParam(value = "resultCount", required = false) Integer resultCount,
                           @RequestParam(value = "shortCount", required = false) Integer shortCount,
                           @RequestParam(value = "programCount", required = false) Integer programCount,
                           @RequestParam(value = "choicePercent", required = false) Double choicePercent,
                           @RequestParam(value = "fillPercent", required = false) Double fillPercent,
                           @RequestParam(value = "resultPercent", required = false) Double resultPercent,
                           @RequestParam(value = "shortPercent", required = false) Double shortPercent,
                           @RequestParam(value = "programPercent", required = false) Double programPercent,
                           @RequestParam(value = "actualTotalScore", required = false) Integer actualTotalScore,
                           RedirectAttributes redirectAttributes) {
        try {
            // 打印所有接收到的参数
            System.out.println("=== 接收到的所有参数 ===");
            System.out.println("Paper对象: " + paper);
            System.out.println("QuestionIds数量: " + (questionIds != null ? questionIds.size() : "null"));
            System.out.println("DifficultyLevel参数: '" + difficultyLevel + "'");
            System.out.println("ChoiceCount: " + choiceCount);
            System.out.println("FillCount: " + fillCount);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户未登录");
                return "redirect:/teacher/papers/create";
            }
            
            // 设置试卷基本信息
            paper.setCreatorId(user.getUserId());
            paper.setCreatedAt(LocalDateTime.now());
            paper.setUpdatedAt(LocalDateTime.now());
            
            // 调试信息
            System.out.println("=== 试卷创建调试信息 ===");
            System.out.println("接收到的难度级别参数: " + difficultyLevel);
            System.out.println("Paper对象自动绑定的难度级别: " + paper.getDifficultyLevel());
            
            // 确保难度级别不为空
            if (paper.getDifficultyLevel() == null || paper.getDifficultyLevel().trim().isEmpty()) {
                System.out.println("难度级别为空，设置默认值");
                paper.setDifficultyLevel("medium");
            }
            
            // 设置及格分数（如果未设置，默认为总分的60%）
            if (paper.getPassingScore() == null) {
                int defaultPassingScore = (int) (paper.getTotalScore() * 0.6);
                paper.setPassingScore(defaultPassingScore);
                System.out.println("设置默认及格分数: " + defaultPassingScore);
            }
            
            // 设置题目数量
            paper.setQuestionCount(questionIds.size());
            System.out.println("设置题目数量: " + questionIds.size());
            
            // 设置实际总分（如果题目分数总和与预期总分不同）
            if (actualTotalScore != null && actualTotalScore > 0) {
                paper.setTotalScore(actualTotalScore);
            }
            
            // 创建试卷（包含题目和分数）
            Paper savedPaper;
            if (questionScores != null && !questionScores.isEmpty()) {
                savedPaper = paperService.createPaper(paper, questionIds, questionScores);
            } else {
                savedPaper = paperService.createPaper(paper, questionIds);
            }
            
            // 记录试卷创建的详细信息
            System.out.println("试卷创建成功:");
            System.out.println("试卷ID: " + savedPaper.getPaperId());
            System.out.println("试卷名称: " + savedPaper.getPaperName());
            System.out.println("科目ID: " + savedPaper.getSubjectId());
            System.out.println("题目数量: " + questionIds.size());
            System.out.println("总分: " + savedPaper.getTotalScore());
            System.out.println("难度系数: " + savedPaper.getDifficultyLevel());
            
            // 验证数据库中的实际值
            Paper verifyPaper = paperService.getPaperById(savedPaper.getPaperId());
            System.out.println("数据库验证 - 难度系数: " + (verifyPaper != null ? verifyPaper.getDifficultyLevel() : "null"));
            
            // 修复：确保提示信息正确显示
            redirectAttributes.addFlashAttribute("success", "试卷创建成功！试卷ID: " + savedPaper.getPaperId());
            redirectAttributes.addFlashAttribute("paperId", savedPaper.getPaperId());
            
            // 修复：重定向到试卷列表页面，确保能看到新创建的试卷
            return "redirect:/teacher/papers";
        } catch (Exception e) {
            System.err.println("试卷创建失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "试卷创建失败: " + e.getMessage());
            return "redirect:/teacher/papers/create";
        }
    }
    
    // 试卷详情
    @GetMapping("/papers/{paperId}")
    public String paperDetail(@PathVariable String paperId, Model model) {
        Paper paper = paperService.getPaperById(paperId);
        if (paper == null) {
            return "redirect:/teacher/papers";
        }
        
        // 获取试卷题目详情，包含分数信息
        List<PaperQuestion> paperQuestions = paperService.getPaperQuestions(paperId);
        List<Question> questions = paperService.getPaperQuestionDetails(paperId);
        
        // 创建题目和分数的映射
        Map<String, Integer> questionScoreMap = new HashMap<>();
        for (PaperQuestion pq : paperQuestions) {
            questionScoreMap.put(pq.getQuestionId(), pq.getScore());
        }
        
        model.addAttribute("paper", paper);
        model.addAttribute("questions", questions);
        model.addAttribute("questionScoreMap", questionScoreMap);
        return "teacher/paper/detail";
    }
    
    // 编辑试卷
    @GetMapping("/papers/{paperId}/edit")
    public String editPaperForm(@PathVariable String paperId, Model model) {
        Paper paper = paperService.getPaperById(paperId);
        if (paper == null) {
            return "redirect:/teacher/papers";
        }
        
        List<Subject> subjects = subjectService.getAllSubjects();
        List<Question> currentQuestions = paperService.getPaperQuestionDetails(paperId);
        
        // 获取所有可用题目（当前科目的题目）
        Page<Question> questionPage = questionService.searchQuestions(paper.getSubjectId(), null, null, null, null, Pageable.unpaged());
        List<Question> availableQuestions = questionPage.getContent();
        
        model.addAttribute("paper", paper);
        model.addAttribute("subjects", subjects);
        model.addAttribute("currentQuestions", currentQuestions);
        model.addAttribute("availableQuestions", availableQuestions);
        return "teacher/paper/edit";
    }
    
    // 更新试卷
    @PostMapping("/papers/{paperId}")
    public String updatePaper(@PathVariable String paperId,
                             @ModelAttribute Paper paper,
                             @RequestParam("questionIds") List<String> questionIds,
                             RedirectAttributes redirectAttributes) {
        try {
            paper.setPaperId(paperId);
            paper.setUpdatedAt(LocalDateTime.now());
            
            paperService.updatePaper(paper, questionIds);
            redirectAttributes.addFlashAttribute("success", "试卷更新成功！");
            return "redirect:/teacher/papers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "试卷更新失败: " + e.getMessage());
            return "redirect:/teacher/papers/" + paperId + "/edit";
        }
    }
    
    // 删除试卷
    @PostMapping("/papers/{paperId}/delete")
    public String deletePaper(@PathVariable String paperId, RedirectAttributes redirectAttributes) {
        try {
            paperService.deletePaper(paperId);
            redirectAttributes.addFlashAttribute("success", "试卷删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "试卷删除失败: " + e.getMessage());
        }
        return "redirect:/teacher/papers";
    }
    
    // 删除试卷 (支持DELETE方法)
    @DeleteMapping("/papers/{paperId}/delete")
    @ResponseBody
    public Map<String, Object> deletePaperAjax(@PathVariable String paperId) {
        Map<String, Object> result = new HashMap<>();
        try {
            paperService.deletePaper(paperId);
            result.put("success", true);
            result.put("message", "试卷删除成功！");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "试卷删除失败: " + e.getMessage());
        }
        return result;
    }
    
    // 强制删除试卷 (清理所有相关数据)
    @PostMapping("/papers/{paperId}/force-delete")
    @ResponseBody
    public Map<String, Object> forceDeletePaperAjax(@PathVariable String paperId) {
        Map<String, Object> result = new HashMap<>();
        try {
            paperService.forceDeletePaper(paperId);
            result.put("success", true);
            result.put("message", "试卷强制删除成功！");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "试卷强制删除失败: " + e.getMessage());
        }
        return result;
    }
    
    // SQL强制删除试卷 (使用原生SQL绕过外键约束)
    @PostMapping("/papers/{paperId}/sql-force-delete")
    @ResponseBody
    public Map<String, Object> sqlForceDeletePaperAjax(@PathVariable String paperId) {
        Map<String, Object> result = new HashMap<>();
        try {
            paperService.sqlForceDeletePaper(paperId);
            result.put("success", true);
            result.put("message", "试卷SQL强制删除成功！");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "试卷SQL强制删除失败: " + e.getMessage());
        }
        return result;
    }
    
    // 切换试卷状态
    @PostMapping("/papers/{paperId}/toggle-status")
    @ResponseBody
    public Map<String, Object> togglePaperStatus(@PathVariable String paperId) {
        Map<String, Object> result = new HashMap<>();
        try {
            paperService.togglePaperStatus(paperId);
            result.put("success", true);
            result.put("message", "试卷状态切换成功！");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "试卷状态切换失败: " + e.getMessage());
        }
        return result;
    }
    

    
    // 从题目内容中解析选择题选项到数组
    private String[] parseOptionsToArray(String content) {
        String[] options = new String[4]; // A, B, C, D
        if (content == null || content.trim().isEmpty()) {
            return options;
        }
        
        String[] lines = content.split("\n");
        
        // 首先尝试从多行格式解析（每行一个选项）
        for (String line : lines) {
            line = line.trim();
            // 匹配 A. 选项内容 格式
            if (line.matches("^[A-Z]\\.\\s*.+$")) {
                String letter = line.substring(0, 1);
                String optionContent = line.substring(2).trim();
                
                switch (letter) {
                    case "A": options[0] = optionContent; break;
                    case "B": options[1] = optionContent; break;
                    case "C": options[2] = optionContent; break;
                    case "D": options[3] = optionContent; break;
                }
            }
        }
        
        // 如果没有找到选项，尝试从单行内容中解析
        if (options[0] == null && options[1] == null && options[2] == null && options[3] == null) {
            String pattern = "([A-Z])\\.\\s*([^A-Z]+?)(?=[A-Z]\\.|$)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(content);
            
            while (m.find()) {
                String letter = m.group(1);
                String optionContent = m.group(2).trim();
                
                switch (letter) {
                    case "A": options[0] = optionContent; break;
                    case "B": options[1] = optionContent; break;
                    case "C": options[2] = optionContent; break;
                    case "D": options[3] = optionContent; break;
                }
            }
        }
        
        return options;
    }
    
    // 从题目内容中移除选项部分，只保留题目本身
    private String cleanContentFromOptions(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        String[] lines = content.split("\n");
        StringBuilder cleanedContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            // 如果不是选项行（不以 A. B. C. D. 等开头），则保留
            if (!line.matches("^[A-Z]\\.\\s*.+$")) {
                if (cleanedContent.length() > 0) {
                    cleanedContent.append("\n");
                }
                cleanedContent.append(line);
            }
        }
        
        // 如果多行格式没有找到选项，尝试从单行格式中移除选项
        if (cleanedContent.length() == 0 || cleanedContent.toString().trim().isEmpty()) {
            // 找到第一个选项的位置，只保留选项之前的内容
            String pattern = "([A-Z])\\.\\s*";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(content);
            
            if (m.find()) {
                // 找到第一个选项的位置，截取之前的内容
                int firstOptionIndex = m.start();
                String beforeOptions = content.substring(0, firstOptionIndex).trim();
                return beforeOptions;
            }
        }
        
        return cleanedContent.toString().trim();
    }
    
    // 试卷预览
    @GetMapping("/papers/{id}/preview")
    public String previewPaper(@PathVariable String id, Model model) {
        try {
            Paper paper = paperService.getPaperById(id);
            if (paper == null) {
                return "redirect:/teacher/papers?error=试卷不存在";
            }
            
            // 获取试卷的所有题目
            List<PaperQuestion> paperQuestions = paperService.getPaperQuestions(id);
            
            // 按题型分组题目
            Map<String, List<Question>> questionsByType = new HashMap<>();
            Map<String, Integer> questionCounts = new HashMap<>();
            Map<String, Double> questionScores = new HashMap<>();
            
            // 初始化各题型的题目列表
            questionsByType.put("choice", new ArrayList<>());
            questionsByType.put("fill", new ArrayList<>());
            questionsByType.put("result", new ArrayList<>());
            questionsByType.put("short", new ArrayList<>());
            questionsByType.put("program", new ArrayList<>());
            
            // 统计各题型的数量和分数
            questionCounts.put("choice", 0);
            questionCounts.put("fill", 0);
            questionCounts.put("result", 0);
            questionCounts.put("short", 0);
            questionCounts.put("program", 0);
            
            questionScores.put("choice", 0.0);
            questionScores.put("fill", 0.0);
            questionScores.put("result", 0.0);
            questionScores.put("short", 0.0);
            questionScores.put("program", 0.0);
            
            // 处理每道题目
            for (PaperQuestion pq : paperQuestions) {
                Question question = questionService.getQuestionById(pq.getQuestionId());
                if (question != null) {
                    String questionType = question.getQuestionType();
                    // 根据题目类型ID映射到对应的题型
                    String mappedType = mapQuestionType(questionType);
                    if (questionsByType.containsKey(mappedType)) {
                        questionsByType.get(mappedType).add(question);
                        questionCounts.put(mappedType, questionCounts.get(mappedType) + 1);
                        questionScores.put(mappedType, questionScores.get(mappedType) + pq.getScore());
                    }
                }
            }
            
            // 计算各题型的平均分数
            Map<String, Double> avgScores = new HashMap<>();
            for (String type : questionScores.keySet()) {
                int count = questionCounts.get(type);
                if (count > 0) {
                    avgScores.put(type, questionScores.get(type) / count);
                } else {
                    avgScores.put(type, 0.0);
                }
            }
            
            // 计算各题型的占比
            int totalQuestions = paperQuestions.size();
            Map<String, Integer> questionPercents = new HashMap<>();
            for (String type : questionCounts.keySet()) {
                int count = questionCounts.get(type);
                if (totalQuestions > 0) {
                    questionPercents.put(type, (int) Math.round((double) count / totalQuestions * 100));
                } else {
                    questionPercents.put(type, 0);
                }
            }
            
            // 添加到模型中
            model.addAttribute("paper", paper);
            model.addAttribute("choiceQuestions", questionsByType.get("choice"));
            model.addAttribute("fillQuestions", questionsByType.get("fill"));
            model.addAttribute("resultQuestions", questionsByType.get("result"));
            model.addAttribute("shortQuestions", questionsByType.get("short"));
            model.addAttribute("programQuestions", questionsByType.get("program"));
            
            // 题目数量
            model.addAttribute("choiceCount", questionCounts.get("choice"));
            model.addAttribute("fillCount", questionCounts.get("fill"));
            model.addAttribute("resultCount", questionCounts.get("result"));
            model.addAttribute("shortCount", questionCounts.get("short"));
            model.addAttribute("programCount", questionCounts.get("program"));
            
            // 题目分数
            model.addAttribute("choiceScore", avgScores.get("choice"));
            model.addAttribute("fillScore", avgScores.get("fill"));
            model.addAttribute("resultScore", avgScores.get("result"));
            model.addAttribute("shortScore", avgScores.get("short"));
            model.addAttribute("programScore", avgScores.get("program"));
            
            // 题目占比
            model.addAttribute("choicePercent", questionPercents.get("choice"));
            model.addAttribute("fillPercent", questionPercents.get("fill"));
            model.addAttribute("resultPercent", questionPercents.get("result"));
            model.addAttribute("shortPercent", questionPercents.get("short"));
            model.addAttribute("programPercent", questionPercents.get("program"));
            
            // 总题目数
            model.addAttribute("totalQuestions", totalQuestions);
            
            return "teacher/paper/preview";
            
        } catch (Exception e) {
            // log.error("预览试卷失败", e); // Original code had this line commented out
            return "redirect:/teacher/papers?error=预览试卷失败";
        }
    }
    
    // 试卷预览演示页面
    @GetMapping("/paper/preview-demo")
    public String previewDemo() {
        return "teacher/paper/preview-demo";
    }
    
    // ==================== 考试管理模块 ====================
    
    // 考试列表
    @GetMapping("/exams")
    public String examList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        // 获取该教师的考试分配
        List<ExamAssignment> assignments = examAssignmentService.getAssignmentsByTeacherId(user.getUserId());
        
        // 为每个分配获取试卷信息和统计信息
        List<Map<String, Object>> assignmentInfoList = new ArrayList<>();
        List<Map<String, Object>> activeAssignments = new ArrayList<>();
        List<Map<String, Object>> completedAssignments = new ArrayList<>();
        
        for (ExamAssignment assignment : assignments) {
            Paper paper = paperService.getPaperById(assignment.getPaperId());
            if (paper != null) {
                Map<String, Object> assignmentInfo = new HashMap<>();
                assignmentInfo.put("assignment", assignment);
                assignmentInfo.put("paper", paper);
                
                // 获取该分配的考试会话统计
                List<ExamSession> sessions = examSessionRepository.findByAssignmentId(assignment.getAssignmentId());
                long totalSessions = sessions.size();
                long ongoingSessions = sessions.stream().filter(s -> ExamSession.SessionStatus.ongoing.equals(s.getStatus())).count();
                long completedSessions = sessions.stream().filter(s -> ExamSession.SessionStatus.submitted.equals(s.getStatus())).count();
                
                assignmentInfo.put("totalSessions", totalSessions);
                assignmentInfo.put("ongoingSessions", ongoingSessions);
                assignmentInfo.put("completedSessions", completedSessions);
                
                assignmentInfoList.add(assignmentInfo);
                
                // 根据分配状态分类
                if (ExamAssignment.AssignmentStatus.active.equals(assignment.getStatus())) {
                    activeAssignments.add(assignmentInfo);
                } else if (ExamAssignment.AssignmentStatus.finished.equals(assignment.getStatus())) {
                    completedAssignments.add(assignmentInfo);
                }
            }
        }
        
        model.addAttribute("assignments", assignmentInfoList);
        model.addAttribute("activeAssignments", activeAssignments);
        model.addAttribute("completedAssignments", completedAssignments);
        model.addAttribute("user", user);
        return "teacher/exam/list";
    }
    
    // 安排考试
    @GetMapping("/exams/schedule")
    public String scheduleExamForm(@RequestParam(required = false) String paperId, 
                                 @RequestParam(required = false) String department, 
                                 Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        List<Paper> papers = paperService.getPapersByCreator(user.getUserId());
        List<String> departments = userService.getAllDepartments();
        
        // 根据部门筛选学生
        List<User> students;
        if (department != null && !department.isEmpty() && !"all".equals(department)) {
            students = userService.getStudentsByDepartment(department);
        } else {
            students = userService.getStudents();
        }
        
        model.addAttribute("papers", papers);
        model.addAttribute("students", students);
        model.addAttribute("departments", departments);
        model.addAttribute("user", user);
        model.addAttribute("selectedPaperId", paperId);
        model.addAttribute("selectedDepartment", department);
        return "teacher/exam/schedule";
    }
    
    // 发布考试
    @PostMapping("/exams/schedule")
    public String scheduleExam(@RequestParam String paperId,
                              @RequestParam List<String> studentIds,
                              @RequestParam String examStartTime,
                              @RequestParam String examEndTime,
                              @RequestParam Integer duration,
                              @RequestParam(defaultValue = "1") Integer maxAttempts,
                             RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== 考试安排调试信息 ===");
            System.out.println("试卷ID: " + paperId);
            System.out.println("学生ID列表: " + studentIds);
            System.out.println("考试开始时间: " + examStartTime);
            System.out.println("考试结束时间: " + examEndTime);
            System.out.println("考试时长: " + duration);
            System.out.println("最大尝试次数: " + maxAttempts);
            
            // 验证时间设置
            LocalDateTime startTime = LocalDateTime.parse(examStartTime);
            LocalDateTime endTime = LocalDateTime.parse(examEndTime);
            
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                redirectAttributes.addFlashAttribute("error", "考试结束时间必须晚于开始时间");
                return "redirect:/teacher/exams/schedule";
            }
            
            // 验证时间区间是否足够长
            long timeWindowMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            if (timeWindowMinutes < duration) {
                redirectAttributes.addFlashAttribute("error", "考试时间区间不能短于考试时长");
                return "redirect:/teacher/exams/schedule";
            }
            
            // 验证最大尝试次数
            if (maxAttempts == null || maxAttempts < 1) {
                maxAttempts = 1;
            }
            
            System.out.println("考试时间区间: " + timeWindowMinutes + "分钟");
            System.out.println("考试时长: " + duration + "分钟");
            System.out.println("缓冲时间: " + (timeWindowMinutes - duration) + "分钟");
            
            // 获取当前教师信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User teacher = userService.getUserByUsername(username).orElse(null);
            if (teacher == null) {
                redirectAttributes.addFlashAttribute("error", "无法获取教师信息");
                return "redirect:/teacher/exams/schedule";
            }
            
            // 获取试卷信息
            Paper paper = paperService.getPaperById(paperId);
            if (paper == null) {
                redirectAttributes.addFlashAttribute("error", "试卷不存在");
                return "redirect:/teacher/exams/schedule";
            }
            
            // 检查是否已存在相同试卷的活跃考试分配
            List<ExamAssignment> existingAssignments = examAssignmentRepository.findByPaperIdAndTeacherIdAndStatus(
                paperId, teacher.getUserId(), ExamAssignment.AssignmentStatus.active);
            
            ExamAssignment assignment;
            boolean isNewAssignment = false;
            
            if (existingAssignments != null && !existingAssignments.isEmpty()) {
                // 如果已存在活跃的考试分配，更新现有分配
                assignment = existingAssignments.get(0);
                assignment.setAssignmentName(paper.getPaperName() + " - " + startTime.toLocalDate().toString());
                assignment.setMaxAttempts(maxAttempts);
                assignment.setExamStartTime(startTime);
                assignment.setExamEndTime(endTime);
                assignment.setDurationMinutes(duration);
                assignment.setStatus(ExamAssignment.AssignmentStatus.active);
                assignment.setUpdatedAt(LocalDateTime.now());
                
                // 清理之前的学生分配（删除未开始和进行中的考试会话）
                List<ExamSession> existingSessions = examSessionRepository.findByAssignmentId(assignment.getAssignmentId());
                for (ExamSession session : existingSessions) {
                    if (ExamSession.SessionStatus.not_started.equals(session.getStatus()) || 
                        ExamSession.SessionStatus.ongoing.equals(session.getStatus())) {
                        examSessionRepository.delete(session);
                        System.out.println("删除现有会话: " + session.getSessionId());
                    }
                }
                
                // 删除所有学生分配记录，重新分配
                examAssignmentStudentRepository.deleteByIdAssignmentId(assignment.getAssignmentId());
                
                // 更新考试分配
                examAssignmentService.updateAssignment(assignment);
                System.out.println("成功更新考试分配: " + assignment.getAssignmentId());
            } else {
                // 创建新的考试分配
                assignment = new ExamAssignment();
                assignment.setAssignmentId(generateAssignmentId());
                assignment.setPaperId(paperId);
                assignment.setTeacherId(teacher.getUserId());
                assignment.setAssignmentName(paper.getPaperName() + " - " + startTime.toLocalDate().toString());
                assignment.setMaxAttempts(maxAttempts);
                assignment.setExamStartTime(startTime);
                assignment.setExamEndTime(endTime);
                assignment.setDurationMinutes(duration);
                assignment.setStatus(ExamAssignment.AssignmentStatus.active);
                assignment.setCreatedAt(LocalDateTime.now());
                assignment.setUpdatedAt(LocalDateTime.now());
                
                // 保存考试分配
                examAssignmentService.createAssignment(assignment);
                isNewAssignment = true;
                System.out.println("成功创建考试分配: " + assignment.getAssignmentId());
            }
            
            // 为每个学生分配考试
            int successCount = 0;
            int skipCount = 0;
            for (String studentId : studentIds) {
                try {
                    // 检查学生是否已经有该试卷的分配
                    if (examAssignmentService.isStudentAssigned(assignment.getAssignmentId(), studentId)) {
                        System.out.println("学生 " + studentId + " 已分配过此考试，跳过");
                    skipCount++;
                    continue;
                }
                
                    // 为学生分配考试
                    examAssignmentService.assignToStudent(assignment.getAssignmentId(), studentId);
                successCount++;
                    System.out.println("成功为学生 " + studentId + " 分配考试");
                    
                } catch (Exception e) {
                    System.err.println("为学生 " + studentId + " 分配考试失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            if (successCount > 0) {
                String action = isNewAssignment ? "创建" : "更新";
                String message = "考试安排" + action + "成功！共为 " + successCount + " 名学生安排了考试。学生可以在 " + examStartTime + " 到 " + examEndTime + " 时间段内开始考试。";
                if (maxAttempts > 1) {
                    message += " 每位学生最多可尝试 " + maxAttempts + " 次，系统将记录最高分。";
                }
                if (skipCount > 0) {
                    message += " 有 " + skipCount + " 名学生已安排过此考试，已跳过。";
                }
                redirectAttributes.addFlashAttribute("success", message);
            } else {
                redirectAttributes.addFlashAttribute("warning", "所有学生都已安排过此考试。");
            }
            return "redirect:/teacher/exams";
        } catch (Exception e) {
            System.err.println("考试安排失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "考试安排失败: " + e.getMessage());
            return "redirect:/teacher/exams/schedule";
        }
    }
    
    // 撤销考试安排
    @PostMapping("/exams/{assignmentId}/cancel")
    @Transactional
    public String cancelExam(@PathVariable String assignmentId,
                           RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            // 获取考试分配
            ExamAssignment assignment = examAssignmentService.getAssignmentById(assignmentId);
            if (assignment == null) {
                redirectAttributes.addFlashAttribute("error", "考试分配不存在");
                return "redirect:/teacher/exams";
            }
            
            // 验证考试分配是否属于当前教师
            if (!assignment.getTeacherId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "您没有权限撤销此考试");
                return "redirect:/teacher/exams";
            }
            
            // 检查考试分配状态
            if (ExamAssignment.AssignmentStatus.cancelled.equals(assignment.getStatus())) {
                redirectAttributes.addFlashAttribute("warning", "该考试已被撤销");
                return "redirect:/teacher/exams";
            }
            
            // 获取该考试分配的所有考试会话
            List<ExamSession> sessions = examSessionRepository.findByAssignmentId(assignmentId);
            
            int cancelledCount = 0;
            int ongoingCount = 0;
            int finishedCount = 0;
            
            for (ExamSession session : sessions) {
                ExamSession.SessionStatus status = session.getStatus();
                if (ExamSession.SessionStatus.not_started.equals(status) || ExamSession.SessionStatus.ongoing.equals(status)) {
                    // 统计进行中的会话数量
                    if (ExamSession.SessionStatus.ongoing.equals(status)) {
                        ongoingCount++;
                    }
                    // 使用服务层方法删除会话，确保同时删除相关答案
                    examSessionService.deleteSession(session.getSessionId());
                    cancelledCount++;
                    System.out.println("撤销考试会话: " + session.getSessionId() + " 学生: " + session.getStudentId() + " 状态: " + status);
                } else if (ExamSession.SessionStatus.submitted.equals(status)) {
                    finishedCount++;
                }
            }
            
            // 删除考试分配学生关系（只删除未开始和进行中的）
            if (cancelledCount > 0) {
                // 获取所有被撤销的学生ID
                List<String> cancelledStudentIds = sessions.stream()
                    .filter(session -> {
                        ExamSession.SessionStatus status = session.getStatus();
                        return ExamSession.SessionStatus.not_started.equals(status) || 
                               ExamSession.SessionStatus.ongoing.equals(status);
                    })
                    .map(ExamSession::getStudentId)
                    .toList();
                
                // 删除这些学生的分配关系
                for (String studentId : cancelledStudentIds) {
                    examAssignmentStudentRepository.deleteByIdAssignmentIdAndIdStudentId(assignmentId, studentId);
                }
            }
            
            // 更新考试分配状态为已取消
            assignment.setStatus(ExamAssignment.AssignmentStatus.cancelled);
            assignment.setUpdatedAt(LocalDateTime.now());
            examAssignmentService.updateAssignment(assignment);
            
            if (cancelledCount > 0) {
                redirectAttributes.addFlashAttribute("success", "成功撤销 " + cancelledCount + " 名学生的考试安排，包括 " + ongoingCount + " 个进行中的会话");
            } else {
                String message = "没有可撤销的考试安排";
                if (finishedCount > 0) {
                    message += "（有 " + finishedCount + " 名学生的考试已完成，无法撤销）";
                } else {
                    message += "（所有考试都已撤销）";
                }
                redirectAttributes.addFlashAttribute("warning", message);
            }
            
            return "redirect:/teacher/exams";
        } catch (Exception e) {
            System.err.println("撤销考试安排失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "撤销考试安排失败: " + e.getMessage());
            return "redirect:/teacher/exams";
        }
    }
    
    // 考试监控
    @GetMapping("/exams/{assignmentId}/monitor")
    public String examMonitor(@PathVariable String assignmentId, Model model) {
        // 获取考试分配信息
        ExamAssignment assignment = examAssignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            return "redirect:/teacher/exams";
        }
        
        // 通过考试分配获取试卷信息
        Paper paper = paperService.getPaperById(assignment.getPaperId());
        if (paper == null) {
            return "redirect:/teacher/exams";
        }
        
        // 获取该考试分配的所有考试会话
        List<ExamSession> sessions = examSessionRepository.findByAssignmentId(assignmentId);
        
        // 为每个考试会话加载学生信息
        for (ExamSession session : sessions) {
            if (session.getStudentId() != null) {
                Optional<User> studentOpt = userService.getUserById(session.getStudentId());
                if (studentOpt.isPresent()) {
                    session.setStudent(studentOpt.get());
                }
            }
        }
        
        // 统计各种状态的考试会话数量
        long totalCount = sessions.size();
        long ongoingCount = sessions.stream().filter(s -> ExamSession.SessionStatus.ongoing.equals(s.getStatus())).count();
        long completedCount = sessions.stream().filter(s -> 
            ExamSession.SessionStatus.submitted.equals(s.getStatus()) || 
            ExamSession.SessionStatus.graded.equals(s.getStatus())).count();
        long notStartedCount = sessions.stream().filter(s -> 
            ExamSession.SessionStatus.not_started.equals(s.getStatus()) ||
            ExamSession.SessionStatus.timeout.equals(s.getStatus()) ||
            ExamSession.SessionStatus.cancelled.equals(s.getStatus())).count();
        
        // 计算完成率
        double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("paper", paper);
        model.addAttribute("sessions", sessions);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("notStartedCount", notStartedCount);
        model.addAttribute("completionRate", completionRate);
        return "teacher/exam/monitor";
    }
    
    // ==================== 阅卷管理模块 ====================
    
    // 阅卷列表
    @GetMapping("/grading")
    public String gradingList(@RequestParam(defaultValue = "pending") String status, Model model) {
        System.out.println("=== 教师端阅卷列表请求 ===");
        System.out.println("请求状态: " + status);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        // 获取该教师创建的试卷
        List<Paper> teacherPapers = paperService.getPapersByCreator(user.getUserId());
        Set<String> teacherPaperIds = teacherPapers.stream()
                .map(Paper::getPaperId)
                .collect(Collectors.toSet());
        
        List<ExamSession> allSessions = new ArrayList<>();
        List<ExamSession> filteredSessions = new ArrayList<>();
        
        if ("pending".equals(status)) {
            // 获取需要阅卷的考试会话
            allSessions = examSessionService.getSessionsForGrading();
        } else if ("graded".equals(status)) {
            // 获取已阅卷的考试会话
            allSessions = examSessionService.getGradedSessions();
        } else {
            // 获取所有已完成的考试会话
            allSessions.addAll(examSessionService.getSessionsForGrading());
            allSessions.addAll(examSessionService.getGradedSessions());
        }
        
        // 只显示该教师创建的试卷的考试会话，并加载关联对象
        for (ExamSession session : allSessions) {
            if (teacherPaperIds.contains(session.getPaperId())) {
                // 手动加载关联的Paper和User对象
                Paper paper = paperService.getPaperById(session.getPaperId());
                if (paper != null) {
                    session.setPaper(paper);
                }
                
                Optional<User> studentOpt = userService.getUserById(session.getStudentId());
                if (studentOpt.isPresent()) {
                    session.setStudent(studentOpt.get());
                }
                
                filteredSessions.add(session);
            }
        }
        
        System.out.println("=== 教师端阅卷列表调试 ===");
        System.out.println("教师ID: " + user.getUserId());
        System.out.println("教师创建的试卷数量: " + teacherPaperIds.size());
        System.out.println("所有需要阅卷的会话数量: " + allSessions.size());
        System.out.println("过滤后的会话数量: " + filteredSessions.size());
        
        // 输出每个会话的详细信息
        for (ExamSession session : filteredSessions) {
            System.out.println("会话ID: " + session.getSessionId() + 
                             ", 试卷ID: " + session.getPaperId() + 
                             ", 学生ID: " + session.getStudentId() + 
                             ", 状态: " + session.getStatus() +
                             ", 试卷名称: " + (session.getPaper() != null ? session.getPaper().getPaperName() : "null") +
                             ", 学生姓名: " + (session.getStudent() != null ? session.getStudent().getUsername() : "null"));
        }
        
        model.addAttribute("user", user);
        model.addAttribute("sessions", filteredSessions);
        model.addAttribute("currentStatus", status);
        return "teacher/grading/list";
    }
    
    // 调试端点 - 查看所有考试会话
    @GetMapping("/grading/debug")
    @ResponseBody
    public Map<String, Object> debugGrading() {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        if (user == null) {
            result.put("error", "用户不存在");
            return result;
        }
        
        // 获取该教师创建的试卷
        List<Paper> teacherPapers = paperService.getPapersByCreator(user.getUserId());
        Set<String> teacherPaperIds = teacherPapers.stream()
                .map(Paper::getPaperId)
                .collect(Collectors.toSet());
        
        // 获取所有已提交的考试会话
        List<ExamSession> allSubmittedSessions = examSessionService.getSessionsByStatus(ExamSession.SessionStatus.submitted);
        
        // 获取需要阅卷的考试会话
        List<ExamSession> sessionsForGrading = examSessionService.getSessionsForGrading();
        
        // 过滤该教师的考试会话
        List<ExamSession> teacherSessions = new ArrayList<>();
        for (ExamSession session : allSubmittedSessions) {
            if (teacherPaperIds.contains(session.getPaperId())) {
                teacherSessions.add(session);
            }
        }
        
        result.put("teacherId", user.getUserId());
        result.put("teacherPapers", teacherPapers.size());
        result.put("teacherPaperIds", teacherPaperIds);
        result.put("allSubmittedSessions", allSubmittedSessions.size());
        result.put("sessionsForGrading", sessionsForGrading.size());
        result.put("teacherSessions", teacherSessions.size());
        
        // 详细会话信息
        List<Map<String, Object>> sessionDetails = new ArrayList<>();
        for (ExamSession session : teacherSessions) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("sessionId", session.getSessionId());
            detail.put("paperId", session.getPaperId());
            detail.put("studentId", session.getStudentId());
            detail.put("status", session.getStatus());
            detail.put("submittedAt", session.getSubmittedAt());
            detail.put("hasSubjectiveQuestions", examSessionService.hasSubjectiveQuestionsNeedingGrading(session.getSessionId()));
            sessionDetails.add(detail);
        }
        result.put("sessionDetails", sessionDetails);
        
        return result;
    }
    
    // 阅卷工作台
    @GetMapping("/grading/workbench")
    public String gradingWorkbench(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        // 获取该教师创建的试卷
        List<Paper> teacherPapers = paperService.getPapersByCreator(user.getUserId());
        Set<String> teacherPaperIds = teacherPapers.stream()
                .map(Paper::getPaperId)
                .collect(Collectors.toSet());
        
        // 获取需要阅卷的考试会话
        List<ExamSession> allPendingSessions = examSessionService.getSessionsByStatus(ExamSession.SessionStatus.submitted);
        
        // 只显示该教师创建的试卷的考试会话
        List<ExamSession> pendingSessions = new ArrayList<>();
        for (ExamSession session : allPendingSessions) {
            if (teacherPaperIds.contains(session.getPaperId())) {
                pendingSessions.add(session);
            }
        }
        
        // 获取所有需要阅卷的题目，按试卷名称分组
        Map<String, List<Map<String, Object>>> tasksByPaper = new LinkedHashMap<>();
        int totalTasks = 0;
        int completedTasks = 0;
        
        for (ExamSession session : pendingSessions) {
            // 获取试卷信息
            Paper paper = paperService.getPaperById(session.getPaperId());
            if (paper == null) continue;
            
            String paperName = paper.getPaperName();
            if (!tasksByPaper.containsKey(paperName)) {
                tasksByPaper.put(paperName, new ArrayList<>());
            }
            
            List<ExamSessionAnswer> answers = examSessionService.getSessionAnswers(session.getSessionId());
            for (ExamSessionAnswer answer : answers) {
                Question question = questionService.getQuestionById(answer.getQuestionId());
                if (question != null && !isObjectiveQuestion(question.getQuestionType())) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("sessionId", session.getSessionId());
                    task.put("questionId", answer.getQuestionId());
                    task.put("questionType", question.getQuestionType());
                    task.put("questionContent", question.getContent());
                    task.put("studentName", session.getStudentName());
                    task.put("paperName", paperName);
                    task.put("paperId", session.getPaperId());
                    task.put("maxScore", getQuestionScore(session.getPaperId(), answer.getQuestionId()));
                    task.put("currentScore", answer.getScore() != null ? answer.getScore() : 0.0);
                    task.put("studentAnswer", answer.getAnswerText());
                    task.put("standardAnswer", question.getAnswer());
                    task.put("explanation", question.getKnowledgeTag());
                    task.put("status", answer.getScore() != null ? "已完成" : "待阅卷");
                    
                    tasksByPaper.get(paperName).add(task);
                    totalTasks++;
                    if ("已完成".equals(task.get("status"))) {
                        completedTasks++;
                    }
                }
            }
        }
        
        model.addAttribute("tasksByPaper", tasksByPaper);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        
        return "teacher/grading/workbench";
    }
    
    // 获取单个任务详情（AJAX接口）
    @GetMapping("/grading/workbench/task/{sessionId}/{questionId}")
    @ResponseBody
    public Map<String, Object> getTaskDetail(@PathVariable String sessionId, @PathVariable String questionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取考试会话
            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                result.put("success", false);
                result.put("message", "考试会话不存在");
                return result;
            }
            
            // 获取试卷信息
            Paper paper = paperService.getPaperById(session.getPaperId());
            if (paper == null) {
                result.put("success", false);
                result.put("message", "试卷不存在");
                return result;
            }
            
            // 获取题目信息
            Question question = questionService.getQuestionById(questionId);
            if (question == null) {
                result.put("success", false);
                result.put("message", "题目不存在");
                return result;
            }
            
            // 获取学生答案
            List<ExamSessionAnswer> answers = examSessionService.getSessionAnswers(sessionId);
            ExamSessionAnswer answer = answers.stream()
                    .filter(a -> questionId.equals(a.getQuestionId()))
                    .findFirst()
                    .orElse(null);
            
            if (answer == null) {
                result.put("success", false);
                result.put("message", "学生答案不存在");
                return result;
            }
            
            // 构建返回数据
            result.put("success", true);
            result.put("questionContent", question.getContent());
            result.put("questionType", question.getQuestionType());
            result.put("standardAnswer", question.getAnswer());
            result.put("explanation", question.getKnowledgeTag());
            result.put("studentAnswer", answer.getAnswerText());
            result.put("currentScore", answer.getScore() != null ? answer.getScore() : 0.0);
            result.put("maxScore", getQuestionScore(session.getPaperId(), questionId));
            result.put("studentName", session.getStudentName());
            result.put("paperName", paper.getPaperName());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取任务详情失败: " + e.getMessage());
        }
        
        return result;
    }

    // AJAX 提交单题评分
    @PostMapping("/grading/workbench/grade")
    @ResponseBody
    public Map<String, Object> ajaxGrade(@RequestParam String sessionId,
                                         @RequestParam String questionId,
                                         @RequestParam Double score,
                                         @RequestParam(required = false) String feedback) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 更新该题得分与评语
            examSessionService.updateAnswerScore(sessionId, questionId, score);
            if (feedback != null && !feedback.trim().isEmpty()) {
                examSessionService.updateAnswerFeedback(sessionId, questionId, feedback);
            }

            // 重新计算会话总分
            examSessionService.updateSessionTotalScore(sessionId);

            // 如果所有主观题都已评分，设置状态为已阅卷
            boolean needMore = examSessionService.hasSubjectiveQuestionsNeedingGrading(sessionId);
            if (!needMore) {
                examSessionService.updateSessionStatus(sessionId, ExamSession.SessionStatus.graded);
            }

            ExamSession session = examSessionService.getSessionById(sessionId);
            result.put("success", true);
            result.put("needMoreGrading", needMore);
            result.put("totalScore", session != null ? session.getTotalScore() : null);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    // 阅卷详情
    @GetMapping("/grading/{sessionId}")
    public String gradingDetail(@PathVariable String sessionId, Model model) {
        System.out.println("=== 教师端阅卷详情请求 ===");
        System.out.println("SessionId: " + sessionId);
        ExamSession session = examSessionService.getSessionById(sessionId);
        if (session == null) {
            return "redirect:/teacher/grading";
        }
        
        // 手动加载关联的学生信息
        System.out.println("=== 调试学生信息加载 ===");
        System.out.println("Session ID: " + sessionId);
        System.out.println("Student ID: " + session.getStudentId());
        
        Optional<User> studentOpt = userService.getUserById(session.getStudentId());
        if (studentOpt.isPresent()) {
            User student = studentOpt.get();
            System.out.println("找到学生: " + student.getUsername() + ", 邮箱: " + student.getEmail());
            session.setStudent(student);
        } else {
            System.out.println("未找到学生，Student ID: " + session.getStudentId());
        }
        
        Paper paper = paperService.getPaperById(session.getPaperId());
        
        // 获取试卷中实际包含的题目
        List<PaperQuestion> paperQuestions = paperService.getPaperQuestions(session.getPaperId());
        Set<String> paperQuestionIds = new HashSet<>();
        for (PaperQuestion pq : paperQuestions) {
            paperQuestionIds.add(pq.getQuestionId());
        }
        
        // 获取所有答案，但只保留试卷中实际包含的题目
        List<ExamSessionAnswer> allAnswers = examSessionService.getSessionAnswers(sessionId);
        List<ExamSessionAnswer> answers = new ArrayList<>();
        
        // 手动加载Question关联并过滤
        for (ExamSessionAnswer answer : allAnswers) {
            if (answer.getQuestionId() != null && paperQuestionIds.contains(answer.getQuestionId())) {
                Question question = questionService.getQuestionById(answer.getQuestionId());
                answer.setQuestion(question);
                answers.add(answer);
                System.out.println("加载答案: " + answer.getQuestionId() + 
                                 ", 题型: " + (question != null ? question.getQuestionType() : "null") +
                                 ", 分数: " + answer.getScore());
            }
        }
        
        System.out.println("过滤后的答案数量: " + answers.size());
        
        // 获取题目分值信息
        Map<String, Integer> questionScoreMap = new HashMap<>();
        for (PaperQuestion pq : paperQuestions) {
            questionScoreMap.put(pq.getQuestionId(), pq.getScore());
        }
        
        // 计算各题型得分
        double choiceScore = 0.0; // 选择题得分 (C)
        double fillScore = 0.0;   // 填空题得分 (F)
        double runScore = 0.0;    // 程序运行结果题得分 (R)
        double shortScore = 0.0;  // 简答题得分 (S)
        double programScore = 0.0; // 编程题得分 (P)
        
        double choiceTotal = 0.0; // 选择题总分
        double fillTotal = 0.0;   // 填空题总分
        double runTotal = 0.0;    // 程序运行结果题总分
        double shortTotal = 0.0;  // 简答题总分
        double programTotal = 0.0; // 编程题总分
        
        for (ExamSessionAnswer answer : answers) {
            if (answer.getQuestion() != null) {
                String questionType = answer.getQuestion().getQuestionType();
                Integer maxScore = questionScoreMap.get(answer.getQuestionId());
                double maxScoreValue = maxScore != null ? maxScore : 0.0;
                double currentScore = answer.getScore() != null ? answer.getScore() : 0.0;
                
                switch (questionType) {
                    case "C": // 选择题
                        choiceScore += currentScore;
                        choiceTotal += maxScoreValue;
                        break;
                    case "F": // 填空题
                        fillScore += currentScore;
                        fillTotal += maxScoreValue;
                        break;
                    case "R": // 程序运行结果题
                        runScore += currentScore;
                        runTotal += maxScoreValue;
                        break;
                    case "S": // 简答题
                        shortScore += currentScore;
                        shortTotal += maxScoreValue;
                        break;
                    case "P": // 编程题
                        programScore += currentScore;
                        programTotal += maxScoreValue;
                        break;
                }
            }
        }
        
        // 单独传递学生信息
        User student = null;
        if (studentOpt.isPresent()) {
            student = studentOpt.get();
        }
        
        model.addAttribute("session", session);
        model.addAttribute("paper", paper);
        model.addAttribute("answers", answers);
        model.addAttribute("questionScoreMap", questionScoreMap);
        model.addAttribute("student", student);
        
        // 各题型得分
        model.addAttribute("choiceScore", choiceScore);
        model.addAttribute("fillScore", fillScore);
        model.addAttribute("runScore", runScore);
        model.addAttribute("shortScore", shortScore);
        model.addAttribute("programScore", programScore);
        
        // 各题型总分
        model.addAttribute("choiceTotal", choiceTotal);
        model.addAttribute("fillTotal", fillTotal);
        model.addAttribute("runTotal", runTotal);
        model.addAttribute("shortTotal", shortTotal);
        model.addAttribute("programTotal", programTotal);
        
        // 重新计算并更新session的成绩
        double totalScore = choiceScore + fillScore + runScore + shortScore + programScore;
        double objectiveScore = choiceScore + fillScore + runScore; // 选择题、填空题、程序运行结果题为客观题
        double subjectiveScore = shortScore + programScore; // 简答题、编程题为主观题
        
        System.out.println("=== 教师端阅卷详情调试信息 ===");
        System.out.println("Session ID: " + sessionId);
        System.out.println("各题型得分 - 选择题: " + choiceScore + ", 填空题: " + fillScore + 
                          ", 程序运行结果题: " + runScore + ", 简答题: " + shortScore + 
                          ", 编程题: " + programScore);
        System.out.println("计算后总分: " + totalScore);
        System.out.println("计算前状态: " + session.getStatus());
        
        session.setTotalScore(totalScore);
        session.setObjectiveScore(objectiveScore);
        session.setSubjectiveScore(subjectiveScore);
        session.setUpdatedAt(LocalDateTime.now());
        
        // 如果考试状态是submitted，则更新为graded
        if (session.getStatus() == ExamSession.SessionStatus.submitted) {
            session.setStatus(ExamSession.SessionStatus.graded);
            System.out.println("状态已更新为: " + session.getStatus());
        }
        
        // 保存更新后的session
        examSessionService.updateSession(session);
        
        System.out.println("最终总分: " + session.getTotalScore() + ", 状态: " + session.getStatus());
        System.out.println("=== 调试信息结束 ===");
        
        return "teacher/grading/detail";
    }
    
    // 提交评分
    @PostMapping("/grading/{sessionId}/grade")
    @Transactional
    public String submitGrading(@PathVariable String sessionId,
                               @RequestParam Map<String, String> allParams,
                               RedirectAttributes redirectAttributes) {
        try {
            // 处理评分逻辑
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                
                // 处理分数参数：scores[questionId]
                if (paramName.startsWith("scores[") && paramName.endsWith("]")) {
                    String questionId = paramName.substring(7, paramName.length() - 1);
                    if (paramValue != null && !paramValue.trim().isEmpty()) {
                        try {
                            Double score = Double.parseDouble(paramValue);
                            examSessionService.updateAnswerScore(sessionId, questionId, score);
                        } catch (NumberFormatException e) {
                            // 忽略无效的分数
                        }
                    }
                }
                // 处理反馈参数：feedback[questionId]
                else if (paramName.startsWith("feedback[") && paramName.endsWith("]")) {
                    String questionId = paramName.substring(9, paramName.length() - 1);
                    if (paramValue != null && !paramValue.trim().isEmpty()) {
                        examSessionService.updateAnswerFeedback(sessionId, questionId, paramValue);
                    }
                }
            }
            
            // 更新考试会话总分
            examSessionService.updateSessionTotalScore(sessionId);
            
            // 更新考试会话状态为已阅卷
            examSessionService.updateSessionStatus(sessionId, ExamSession.SessionStatus.graded);
            
            redirectAttributes.addFlashAttribute("success", "评分提交成功！");
            return "redirect:/teacher/grading?status=graded";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "评分提交失败: " + e.getMessage());
            return "redirect:/teacher/grading";
        }
    }
    
    // ==================== 成绩查询模块 ====================
    
    // 成绩查询页面
    @GetMapping("/grades")
    public String gradeQuery(@RequestParam(required = false) String studentName,
                            @RequestParam(required = false) String paperId,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        // 获取所有试卷列表（用于筛选）
        List<Paper> papers = paperService.getAllPapers();
        
        // 获取学生成绩数据
        Page<ExamSession> gradePage = examSessionService.getStudentGrades(studentName, paperId, startDate, endDate, page, size);
        
        // 计算统计信息
        Map<String, Object> statistics = examSessionService.getGradeStatistics(studentName, paperId, startDate, endDate);
        
        model.addAttribute("user", user);
        model.addAttribute("papers", papers);
        model.addAttribute("gradePage", gradePage);
        model.addAttribute("statistics", statistics);
        model.addAttribute("studentName", studentName);
        model.addAttribute("paperId", paperId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "teacher/grades/query";
    }
    
    // 学生成绩详情
    @GetMapping("/grades/{sessionId}")
    public String gradeDetail(@PathVariable String sessionId, Model model) {
        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        ExamSession session = examSessionService.getSessionById(sessionId);
        if (session == null) {
            return "redirect:/teacher/grades";
        }
        
        // 手动加载关联的学生信息
        Optional<User> studentOpt = userService.getUserById(session.getStudentId());
        if (studentOpt.isPresent()) {
            User student = studentOpt.get();
            session.setStudent(student);
        }
        
        Paper paper = paperService.getPaperById(session.getPaperId());
        
        // 获取试卷中实际包含的题目
        List<PaperQuestion> paperQuestions = paperService.getPaperQuestions(session.getPaperId());
        Set<String> paperQuestionIds = new HashSet<>();
        for (PaperQuestion pq : paperQuestions) {
            paperQuestionIds.add(pq.getQuestionId());
        }
        
        // 获取所有答案，但只保留试卷中实际包含的题目
        List<ExamSessionAnswer> allAnswers = examSessionService.getSessionAnswers(sessionId);
        List<ExamSessionAnswer> filteredAnswers = new ArrayList<>();
        
        System.out.println("试卷题目数量: " + paperQuestionIds.size());
        System.out.println("所有答案数量: " + allAnswers.size());
        
        // 手动加载Question关联并过滤
        for (ExamSessionAnswer answer : allAnswers) {
            if (answer.getQuestionId() != null && paperQuestionIds.contains(answer.getQuestionId())) {
                Question question = questionService.getQuestionById(answer.getQuestionId());
                answer.setQuestion(question);
                filteredAnswers.add(answer);
                System.out.println("保留题目: " + answer.getQuestionId() + ", 题目对象: " + (question != null ? "存在" : "null"));
            } else {
                System.out.println("过滤掉题目: " + answer.getQuestionId() + " (不在试卷中)");
            }
        }
        
        System.out.println("过滤后答案数量: " + filteredAnswers.size());
        
        model.addAttribute("user", user);
        model.addAttribute("examSession", session);
        model.addAttribute("paper", paper);
        model.addAttribute("answers", filteredAnswers);
        
        return "teacher/grades/detail";
    }
    
    // ==================== 学习资源管理模块 ====================
    
    // 学习资源列表
    @GetMapping("/resources")
    public String resourceList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        List<LearningResource> resources = learningResourceService.getResourcesByUploader(user.getUserId());
        
        model.addAttribute("resources", resources);
        model.addAttribute("user", user);
        return "teacher/resource/list";
    }
    
    // 上传资源页面
    @GetMapping("/resources/upload")
    public String uploadResourceForm(Model model) {
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        model.addAttribute("user", user);
        
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "teacher/resource/upload";
    }
    
    // 上传资源
    @PostMapping("/resources/upload")
    public String uploadResource(@ModelAttribute LearningResource resource,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            // 生成资源ID
            resource.setId(generateResourceId());
            resource.setUploaderId(user.getUserId());
            resource.setUploadTime(LocalDateTime.now());
            
            // 根据资源类型处理不同的上传逻辑
            if (resource.getType() == LearningResource.ResourceType.LINK) {
                // 链接类型不需要文件上传
                if (resource.getExternalUrl() == null || resource.getExternalUrl().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "链接类型必须提供外部链接地址");
                    return "redirect:/teacher/resources/upload";
                }
            } else {
                // 其他类型需要文件上传
                if (file == null || file.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "请选择要上传的文件");
                    return "redirect:/teacher/resources/upload";
                }
            }
            
            learningResourceService.uploadResource(resource, file, coverFile);
            redirectAttributes.addFlashAttribute("success", "资源上传成功！");
            return "redirect:/teacher/resources";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "资源上传失败: " + e.getMessage());
            return "redirect:/teacher/resources/upload";
        }
    }
    
    // 编辑资源页面
    @GetMapping("/resources/{id}/edit")
    public String editResourceForm(@PathVariable String id, Model model) {
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        model.addAttribute("user", user);
        
        LearningResource resource = learningResourceService.getResourceById(id);
        List<Subject> subjects = subjectService.getAllSubjects();
        
        model.addAttribute("resource", resource);
        model.addAttribute("subjects", subjects);
        
        return "teacher/resource/edit";
    }
    
    // 更新资源
    @PostMapping("/resources/{id}")
    public String updateResource(@PathVariable String id,
                                @ModelAttribute LearningResource resource,
                                @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                                RedirectAttributes redirectAttributes) {
        try {
            resource.setId(id);
            
            // 处理封面图片上传
            if (coverFile != null && !coverFile.isEmpty()) {
                learningResourceService.updateResource(id, resource, coverFile);
            } else {
                learningResourceService.updateResource(id, resource);
            }
            redirectAttributes.addFlashAttribute("success", "资源更新成功！");
            return "redirect:/teacher/resources";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "资源更新失败：" + e.getMessage());
            return "redirect:/teacher/resources/" + id + "/edit";
        }
    }
    
    // 删除资源
    @PostMapping("/resources/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteResource(@PathVariable String id) {
        try {
            learningResourceService.deleteResource(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "资源删除成功");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "资源删除失败：" + e.getMessage());
            return response;
        }
    }
    
    @GetMapping("/resources/{id}/download")
    public ResponseEntity<InputStreamResource> downloadResource(@PathVariable String id) {
        try {
            System.out.println("=== 下载调试信息 ===");
            System.out.println("资源ID: " + id);
            
            LearningResource resource = learningResourceService.getResourceById(id);
            
            if (resource == null) {
                System.out.println("资源不存在");
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("资源信息:");
            System.out.println("  - 标题: " + resource.getTitle());
            System.out.println("  - 文件路径: " + resource.getFilePath());
            System.out.println("  - 文件大小: " + resource.getFileSize());
            
            // 检查是否有文件路径或文件大小
            if ((resource.getFilePath() == null || resource.getFilePath().isEmpty()) && 
                (resource.getFileSize() == null || resource.getFileSize() == 0)) {
                System.out.println("文件路径和大小都为空，返回400错误");
                return ResponseEntity.badRequest().build();
            }
            
            // 构建文件路径
            String projectRoot = System.getProperty("user.dir");
            System.out.println("项目根目录: " + projectRoot);
            File file = null;
            
            if (resource.getFilePath() != null && !resource.getFilePath().isEmpty()) {
                // file_path存储的是相对路径，如 "uploads/files/filename.pdf"
                String fullPath = projectRoot + File.separator + resource.getFilePath();
                System.out.println("文件完整路径: " + fullPath);
                file = new File(fullPath);
                
                if (!file.exists()) {
                    System.out.println("文件不存在: " + fullPath);
                    return ResponseEntity.notFound().build();
                } else {
                    System.out.println("找到文件: " + file.getAbsolutePath());
                }
            } else {
                // 如果没有文件路径但有文件大小，尝试在多个可能的路径中查找
                // 这种情况可能是旧数据，文件可能在其他位置
                System.out.println("文件路径为空，但有文件大小，尝试在多个路径中查找文件");
                
                // 定义可能的文件存储路径
                String[] possiblePaths = {
                    projectRoot + File.separator + "uploads" + File.separator + "files" + File.separator,
                    projectRoot + File.separator + "uploads" + File.separator,
                    projectRoot + File.separator + "files" + File.separator,
                    projectRoot + File.separator + "temp" + File.separator,
                    projectRoot + File.separator + "data" + File.separator,
                    System.getProperty("java.io.tmpdir") + File.separator,
                    System.getProperty("user.home") + File.separator + "Downloads" + File.separator,
                    System.getProperty("user.home") + File.separator + "Desktop" + File.separator
                };
                
                boolean fileFound = false;
                
                for (String searchPath : possiblePaths) {
                    System.out.println("在目录中查找文件: " + searchPath);
                    
                    File searchDir = new File(searchPath);
                    if (searchDir.exists() && searchDir.isDirectory()) {
                        File[] files = searchDir.listFiles();
                        System.out.println("目录中的文件数量: " + (files != null ? files.length : 0));
                        
                        if (files != null) {
                            System.out.println("目标文件大小: " + resource.getFileSize() + " 字节");
                            
                            // 显示前几个文件的信息用于调试
                            int debugCount = Math.min(5, files.length);
                            for (int i = 0; i < debugCount; i++) {
                                File f = files[i];
                                if (f.isFile()) {
                                    System.out.println("  文件 " + (i+1) + ": " + f.getName() + ", 大小: " + f.length() + " 字节");
                                }
                            }
                            
                            // 尝试根据文件大小匹配文件
                            for (File f : files) {
                                if (f.isFile() && f.length() == resource.getFileSize()) {
                                    file = f;
                                    System.out.println("根据文件大小找到匹配文件: " + f.getName() + ", 大小: " + f.length() + ", 路径: " + f.getAbsolutePath());
                                    fileFound = true;
                                    break;
                                }
                            }
                            
                            if (fileFound) {
                                break;
                            }
                            
                            // 如果没找到精确匹配，尝试查找大小相近的文件（扩大误差范围）
                            System.out.println("没找到精确匹配，尝试查找大小相近的文件...");
                            for (File f : files) {
                                if (f.isFile()) {
                                    long sizeDiff = Math.abs(f.length() - resource.getFileSize());
                                    // 允许10%的误差或者最大10MB的误差
                                    long maxError = Math.max(resource.getFileSize() / 10, 10 * 1024 * 1024);
                                    if (sizeDiff <= maxError) {
                                        file = f;
                                        System.out.println("找到大小相近的文件: " + f.getName() + ", 大小: " + f.length() + " 字节, 误差: " + sizeDiff + " 字节, 路径: " + f.getAbsolutePath());
                                        fileFound = true;
                                        break;
                                    }
                                }
                            }
                            
                            if (fileFound) {
                                break;
                            }
                            
                            // 如果还是没找到，尝试查找最近上传的文件
                            if (files.length > 0) {
                                System.out.println("没找到大小相近的文件，尝试使用最新文件...");
                                // 按修改时间排序，取最新的文件
                                java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                                File latestFile = files[0];
                                
                                if (latestFile.isFile()) {
                                    file = latestFile;
                                    System.out.println("使用最新文件: " + file.getName() + ", 大小: " + file.length() + " 字节, 路径: " + file.getAbsolutePath());
                                    fileFound = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        System.out.println("目录不存在: " + searchPath);
                    }
                }
                
                if (!fileFound || file == null || !file.exists()) {
                    System.out.println("无法找到文件，返回404错误");
                    return ResponseEntity.notFound().build();
                }
            }
            
            // 更新下载次数
            learningResourceService.incrementDownloadCount(id);
            
                        // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String downloadFilename;
            
            if (resource.getFilePath() != null && !resource.getFilePath().isEmpty()) {
                // 从相对路径中提取文件名
                String fileName = resource.getFilePath().substring(resource.getFilePath().lastIndexOf('/') + 1);
                downloadFilename = fileName;
            } else {
                // 如果file_path为空，使用实际找到的文件名
                downloadFilename = file.getName();
            }
            
            System.out.println("下载文件名: " + downloadFilename);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                        java.net.URLEncoder.encode(downloadFilename, "UTF-8") + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            
            // 创建输入流
            InputStream inputStream = new FileInputStream(file);
            InputStreamResource resourceStream = new InputStreamResource(inputStream);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resourceStream);
                    
        } catch (Exception e) {
            System.err.println("下载资源失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ==================== 私有方法 ====================
    
    // ==================== 个人中心模块 ====================
    
    // 个人中心页面
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        System.out.println("教师个人中心 - 当前用户名: " + username);
        
        if (username == null || username.isEmpty()) {
            System.out.println("用户名为空，重定向到登录页面");
            return "redirect:/login";
        }
        
        User user = userService.getUserByUsername(username).orElse(null);
        
        if (user == null) {
            System.out.println("未找到用户: " + username);
            model.addAttribute("error", "用户信息不存在，请重新登录");
            return "redirect:/login";
        }
        
        System.out.println("找到用户: " + user.getUsername() + ", ID: " + user.getUserId() + ", 角色: " + user.getRole());
        model.addAttribute("user", user);
        return "teacher/profile";
    }
    
    // 更新个人信息
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User userUpdate,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username).orElse(null);
            
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/teacher/profile";
            }
            
            // 检查用户名是否已存在（排除当前用户）
            if (!currentUser.getUsername().equals(userUpdate.getUsername())) {
                Optional<User> existingUser = userService.getUserByUsername(userUpdate.getUsername());
                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(currentUser.getUserId())) {
                    redirectAttributes.addFlashAttribute("error", "用户名已存在，请选择其他用户名");
                    return "redirect:/teacher/profile";
                }
            }
            
            // 更新所有允许修改的字段
            currentUser.setUsername(userUpdate.getUsername());
            currentUser.setDepartment(userUpdate.getDepartment());
            currentUser.setEmail(userUpdate.getEmail());
            currentUser.setPhone(userUpdate.getPhone());
            
            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("success", "个人信息更新成功！");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "个人信息更新失败：" + e.getMessage());
        }
        
        return "redirect:/teacher/profile";
    }
    
    // 修改密码
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/teacher/profile";
            }
            
            // 验证当前密码
            if (!user.getPassword().equals(currentPassword)) {
                redirectAttributes.addFlashAttribute("error", "当前密码不正确");
                return "redirect:/teacher/profile";
            }
            
            // 验证新密码
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "新密码与确认密码不匹配");
                return "redirect:/teacher/profile";
            }
            
            // 验证新密码长度
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "新密码长度不能少于6个字符");
                return "redirect:/teacher/profile";
            }
            
            // 更新密码
            user.setPassword(newPassword);
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("success", "密码修改成功！");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "密码修改失败：" + e.getMessage());
        }
        
        return "redirect:/teacher/profile";
    }
    
    // 上传头像
    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/teacher/profile";
            }
            
            // 验证文件
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择要上传的头像文件");
                return "redirect:/teacher/profile";
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "只能上传图片文件");
                return "redirect:/teacher/profile";
            }
            
            // 验证文件大小（限制为5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "头像文件大小不能超过5MB");
                return "redirect:/teacher/profile";
            }
            
            // 创建uploads/avatars目录（使用项目根目录）
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    redirectAttributes.addFlashAttribute("error", "无法创建上传目录，请检查权限");
                    return "redirect:/teacher/profile";
                }
            }
            
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "avatar_" + System.currentTimeMillis() + fileExtension;
            
            // 保存文件到uploads/avatars目录
            File destFile = new File(dir, newFilename);
            file.transferTo(destFile);
            
            // 验证文件是否成功保存
            if (!destFile.exists() || destFile.length() == 0) {
                redirectAttributes.addFlashAttribute("error", "文件保存失败，请重试");
                return "redirect:/teacher/profile";
            }
            
            // 更新用户头像路径到数据库（使用相对路径）
            String avatarPath = "/uploads/avatars/" + newFilename;
            user.setAvatar(avatarPath);
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("success", "头像上传成功");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "头像上传失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/teacher/profile";
    }
    
    /**
     * 恢复头像
     */
    @GetMapping("/profile/recover-avatar")
    public String recoverAvatar(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/teacher/profile";
            }
            
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "没有可恢复的头像");
                return "redirect:/teacher/profile";
            }
            
            // 尝试恢复头像文件
            String avatarPath = user.getAvatar();
            String fileName = "";
            
            // 从路径中提取文件名
            if (avatarPath.contains("/")) {
                fileName = avatarPath.substring(avatarPath.lastIndexOf("/") + 1);
            } else {
                fileName = avatarPath;
            }
            
            // 尝试从多个可能的位置恢复文件
            String[] possiblePaths = {
                System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars" + File.separator + fileName,
                System.getProperty("user.dir") + File.separator + fileName,
                System.getProperty("user.dir") + File.separator + "target" + File.separator + "uploads" + File.separator + "avatars" + File.separator + fileName
            };
            
            File recoveredFile = null;
            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists() && file.length() > 0) {
                    recoveredFile = file;
                    break;
                }
            }
            
            if (recoveredFile != null) {
                // 创建正确的目录结构
                String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                // 复制文件到正确位置
                String newPath = uploadDir + File.separator + fileName;
                File newFile = new File(newPath);
                
                if (!newFile.exists()) {
                    java.nio.file.Files.copy(recoveredFile.toPath(), newFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                
                // 更新数据库中的路径
                String newAvatarPath = "/uploads/avatars/" + fileName;
                user.setAvatar(newAvatarPath);
                userService.updateUser(user);
                
                redirectAttributes.addFlashAttribute("success", "头像恢复成功！");
            } else {
                redirectAttributes.addFlashAttribute("error", "无法找到头像文件，请重新上传");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "头像恢复失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/teacher/profile";
    }
    
    /**
     * 重置头像
     */
    @GetMapping("/profile/reset-avatar")
    public String resetAvatar(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/teacher/profile";
            }
            
            // 清除头像路径
            user.setAvatar(null);
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("success", "头像已重置为默认头像");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "头像重置失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/teacher/profile";
    }
    
    // 获取教师统计信息
    private Map<String, Object> getTeacherStatistics(User teacher) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取题目数量
        long questionCount = questionService.getQuestionCountByCreator(teacher.getUserId());
        statistics.put("questionCount", questionCount);
        
        // 获取试卷数量
        long paperCount = paperService.getPaperCountByCreator(teacher.getUserId());
        statistics.put("paperCount", paperCount);
        
        // 获取考试数量
        long examCount = examSessionService.getExamCountByTeacher(teacher.getUserId());
        statistics.put("examCount", examCount);
        
        // 获取待阅卷数量
        long gradingCount = examSessionService.getGradingCountByTeacher(teacher.getUserId());
        statistics.put("gradingCount", gradingCount);
        
        // 获取学生总数
        long totalStudents = examSessionService.getTotalStudentCount();
        statistics.put("totalStudents", totalStudents);
        
        // 获取进行中考试数量
        long ongoingExams = examSessionService.getOngoingExamCount();
        statistics.put("ongoingExams", ongoingExams);
        
        return statistics;
    }
    
    // 判断是否为客观题
    private boolean isObjectiveQuestion(String questionType) {
        return "C".equals(questionType) || "T".equals(questionType) || "F".equals(questionType) || "R".equals(questionType);
    }
    
    // 获取题目在试卷中的分值
    private double getQuestionScore(String paperId, String questionId) {
        try {
            PaperQuestion.PaperQuestionId paperQuestionId = new PaperQuestion.PaperQuestionId();
            paperQuestionId.setPaperId(paperId);
            paperQuestionId.setQuestionId(questionId);
            
            Optional<PaperQuestion> paperQuestion = paperQuestionRepository.findById(paperQuestionId);
            return paperQuestion.map(pq -> (double) pq.getScore()).orElse(0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    // 生成会话ID
    private String generateSessionId() {
        // 查询数据库中最大的session_id，生成下一个递增ID
        String maxSessionId = examSessionRepository.findMaxSessionId();
        int nextNumber = 1;
        
        if (maxSessionId != null && maxSessionId.startsWith("ES")) {
            try {
                String numberPart = maxSessionId.substring(2); // 去掉"ES"前缀
                nextNumber = Integer.parseInt(numberPart) + 1;
                
                // 确保不超过999，避免超出12位限制
                if (nextNumber > 999) {
                    nextNumber = 1; // 重新从1开始，或者可以抛出异常
                    System.out.println("警告：session ID已达到最大值，重新从ES001开始");
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextNumber = 1;
            }
        }
        
        return String.format("ES%03d", nextNumber);
    }
    
    // 生成分配ID
    private String generateAssignmentId() {
        // 查询数据库中最大的assignment_id，生成下一个递增ID
        String maxAssignmentId = examAssignmentRepository.findMaxAssignmentId();
        int nextNumber = 1;
        
        if (maxAssignmentId != null && maxAssignmentId.startsWith("EA")) {
            try {
                String numberPart = maxAssignmentId.substring(2); // 去掉"EA"前缀
                nextNumber = Integer.parseInt(numberPart) + 1;
                
                // 确保不超过999，避免超出12位限制
                if (nextNumber > 999) {
                    nextNumber = 1; // 重新从1开始，或者可以抛出异常
                    System.out.println("警告：assignment ID已达到最大值，重新从EA001开始");
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextNumber = 1;
            }
        }
        
        return String.format("EA%03d", nextNumber);
    }
    
    // 生成资源ID
    private String generateResourceId() {
        // 查询数据库中最大的resource_id，生成下一个递增ID
        String maxResourceId = learningResourceRepository.findMaxResourceId();
        int nextNumber = 1;
        
        if (maxResourceId != null && maxResourceId.startsWith("LS")) {
            try {
                String numberPart = maxResourceId.substring(2); // 去掉"LS"前缀
                nextNumber = Integer.parseInt(numberPart) + 1;
                
                // 确保不超过999，避免超出长度限制
                if (nextNumber > 999) {
                    nextNumber = 1; // 重新从1开始
                    System.out.println("警告：resource ID已达到最大值，重新从LS001开始");
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从1开始
                nextNumber = 1;
            }
        }
        
        return String.format("LS%03d", nextNumber);
    }

    // 根据题目类型ID映射到对应的题型
    private String mapQuestionType(String questionTypeId) {
        if (questionTypeId == null) return "unknown";
        
        // 提取第一个字符作为题型标识
        String typeChar = questionTypeId.substring(0, 1);
        
        switch (typeChar) {
            case "C":
                return "choice";
            case "F":
                return "fill";
            case "R":
                return "result";
            case "S":
                return "short";
            case "P":
                return "program";
            default:
                return "unknown"; // 未知类型
        }
    }
} 
