package com.exam.controller;

import com.exam.entity.*;
import com.exam.repository.ExamSessionRepository;
import com.exam.repository.ExamAssignmentStudentRepository;
import com.exam.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaperService paperService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private LearningResourceService learningResourceService;
    
    @Autowired
    private ExamSessionService examSessionService;
    
    @Autowired
    private ExamAssignmentService examAssignmentService;
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    @Autowired
    private ExamAssignmentStudentRepository examAssignmentStudentRepository;
    
    /**
     * 通用的用户获取方法，支持多种查找方式
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = null;
        
        // 首先尝试通过用户名查找
        user = userService.getUserByUsername(username).orElse(null);
        
        // 如果通过用户名找不到，尝试通过用户ID查找
        if (user == null) {
            user = userService.getUserById(username).orElse(null);
        }
        
        // 如果还是找不到，尝试通过Spring Security的Principal查找
        if (user == null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User springUser = 
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            String springUsername = springUser.getUsername();
            user = userService.getUserByUsername(springUsername).orElse(null);
        }
        
        return user;
    }
    
    // 学生端首页
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        if (user == null || !"student".equals(user.getRole().toLowerCase())) {
            return "redirect:/login";
        }
        
        // 获取学生统计数据
        Map<String, Object> statistics = examSessionService.getStudentStatistics(user.getUserId());
        
        // 添加调试信息
        System.out.println("Student ID: " + user.getUserId());
        System.out.println("Statistics: " + statistics);
        
        model.addAttribute("statistics", statistics);
        model.addAttribute("student", user);
        model.addAttribute("user", user);
        
        return "student/dashboard";
    }
    
    // ==================== 考试列表 ====================
    
    // 可参加的考试列表
    @GetMapping("/exams")
    public String examList(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        // 获取可参加的考试分配
        List<ExamAssignment> availableAssignments = examAssignmentService.getAvailableAssignmentsForStudent(user.getUserId());
        
        // 为每个分配获取试卷信息和剩余考试次数
        List<Map<String, Object>> examInfoList = new ArrayList<>();
        for (ExamAssignment assignment : availableAssignments) {
            // 检查考试分配状态，只显示未取消的考试
            if (assignment.getStatus() == null || 
                assignment.getStatus() == ExamAssignment.AssignmentStatus.cancelled) {
                System.out.println("跳过已取消的考试分配: " + assignment.getAssignmentId() + 
                    ", 状态: " + (assignment.getStatus() != null ? assignment.getStatus() : "null"));
                continue;
            }
            
            // 检查考试时间是否在有效范围内
            LocalDateTime now = LocalDateTime.now();
            if (assignment.getExamEndTime() != null && now.isAfter(assignment.getExamEndTime())) {
                System.out.println("跳过已过期的考试分配: " + assignment.getAssignmentId() + 
                    ", 结束时间: " + assignment.getExamEndTime());
                continue;
            }
            
            // 获取试卷信息
            Paper paper = paperService.getPaperById(assignment.getPaperId());
            if (paper != null && "启用".equals(paper.getStatus())) {
                Map<String, Object> examInfo = new HashMap<>();
                examInfo.put("assignment", assignment);
                examInfo.put("paper", paper);
                
                // 获取剩余考试次数
                int remainingAttempts = examAssignmentService.getRemainingAttempts(assignment.getAssignmentId(), user.getUserId());
                examInfo.put("remainingAttempts", remainingAttempts);
                
                // 获取已完成的考试次数
                long completedAttempts = examSessionRepository.countByAssignmentIdAndStudentId(assignment.getAssignmentId(), user.getUserId());
                examInfo.put("completedAttempts", completedAttempts);
                
                // 获取最佳成绩
                ExamSession bestScore = examAssignmentService.getBestScoreForStudent(assignment.getAssignmentId(), user.getUserId());
                examInfo.put("bestScore", bestScore);
                
                examInfoList.add(examInfo);
            }
        }
        
        // 添加调试信息
        System.out.println("=== 学生端考试列表调试 ===");
        System.out.println("Student ID: " + user.getUserId());
        System.out.println("Available assignments count: " + availableAssignments.size());
        
        // 检查所有分配给学生的考试（不管状态）
        List<ExamAssignmentStudent> allAssignments = examAssignmentStudentRepository.findByIdStudentId(user.getUserId());
        System.out.println("数据库中分配给该学生的考试总数: " + allAssignments.size());
        for (ExamAssignmentStudent as : allAssignments) {
            System.out.println("分配记录: " + as.getAssignmentId() + 
                ", 考试状态: " + (as.getExamAssignment() != null ? as.getExamAssignment().getStatus() : "null"));
        }
        
        System.out.println("Exam info list count: " + examInfoList.size());
        System.out.println("=== 调试结束 ===");
        
        model.addAttribute("user", user);
        model.addAttribute("examInfoList", examInfoList);
        return "student/exam/list";
    }
    
    // 进行中的考试
    @GetMapping("/exams/active")
    public String activeExams(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        // 获取学生正在进行的考试会话
        List<ExamSession> activeSessions = examSessionService.getActiveSessionsForStudent(user.getUserId());
        
        // 为每个会话获取试卷信息
        List<Map<String, Object>> activeExamList = new ArrayList<>();
        for (ExamSession session : activeSessions) {
            Map<String, Object> examInfo = new HashMap<>();
            examInfo.put("session", session);
            
            // 获取试卷信息
            Paper paper = paperService.getPaperById(session.getPaperId());
            examInfo.put("paper", paper);
            
            // 获取考试分配信息
            ExamAssignment assignment = examAssignmentService.getAssignmentById(session.getAssignmentId());
            examInfo.put("assignment", assignment);
            
            activeExamList.add(examInfo);
        }
        
        model.addAttribute("user", user);
        model.addAttribute("activeExams", activeExamList);
        return "student/exam/active";
    }
    
    // 历史考试记录
    @GetMapping("/exams/history")
    public String examHistory(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        // 获取历史考试记录
        List<ExamSession> historyExams = examSessionService.getHistoryExams(user.getUserId());
        
        // 为每个考试会话重新计算成绩并加载关联的试卷信息
        for (ExamSession exam : historyExams) {
            // 重新计算成绩（确保成绩是最新的）
            examSessionService.updateSessionTotalScore(exam.getSessionId());
            
            // 重新获取更新后的exam对象
            ExamSession updatedExam = examSessionService.getSessionById(exam.getSessionId());
            if (updatedExam != null) {
                // 更新原exam对象的成绩字段
                exam.setTotalScore(updatedExam.getTotalScore());
                exam.setObjectiveScore(updatedExam.getObjectiveScore());
                exam.setSubjectiveScore(updatedExam.getSubjectiveScore());
            }
            
            // 使用已加载的Paper对象，无需额外查询
            Paper paper = exam.getPaper();
            if (paper != null) {
                // 科目信息已经通过EAGER加载，无需额外查询
                // Subject subject = paper.getSubject();
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("exams", historyExams);
        return "student/exam/history";
    }
    
    // ==================== 考试进行 ====================
    
    // 创建考试会话并开始考试 - 已删除，直接使用assignmentId开始考试
    
    // 通过assignmentId开始考试（直接创建新会话并开始考试）
    @GetMapping("/exam/assignment/{assignmentId}/start")
    public String startExamByAssignment(@PathVariable String assignmentId, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== 通过assignmentId开始考试 ===");
        System.out.println("assignmentId: " + assignmentId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "用户信息获取失败");
            return "redirect:/student/exams";
        }
        
        // 检查学生是否可以参加此考试
        if (!examAssignmentService.canStudentTakeExam(assignmentId, user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "您无法参加此考试，可能已达到最大考试次数或考试时间已过");
            return "redirect:/student/exams";
        }
        
        // 获取考试分配信息
        ExamAssignment assignment = examAssignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            redirectAttributes.addFlashAttribute("error", "考试分配不存在");
            return "redirect:/student/exams";
        }
        
        // 检查考试分配状态
        if (assignment.getStatus() == ExamAssignment.AssignmentStatus.cancelled) {
            redirectAttributes.addFlashAttribute("error", "该考试已被教师撤销，无法参加");
            return "redirect:/student/exams";
        }
        
        // 获取试卷信息
        Paper paper = paperService.getPaperById(assignment.getPaperId());
        if (paper == null) {
            redirectAttributes.addFlashAttribute("error", "试卷不存在");
            return "redirect:/student/exams";
        }
        
        // 获取试卷题目
        List<Question> questions = paperService.getPaperQuestionDetails(paper.getPaperId());
        System.out.println("=== 获取试卷题目结果 ===");
        System.out.println("试卷ID: " + paper.getPaperId());
        System.out.println("题目数量: " + (questions != null ? questions.size() : 0));
        if (questions != null && !questions.isEmpty()) {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                System.out.println("题目 " + (i + 1) + ": ID=" + q.getId() + 
                    ", 类型=" + q.getQuestionType() + 
                    ", 内容长度=" + (q.getContent() != null ? q.getContent().length() : 0) +
                    ", 内容预览=" + (q.getContent() != null ? q.getContent().substring(0, Math.min(50, q.getContent().length())) + "..." : "null"));
            }
        }
        
        if (questions == null || questions.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "试卷中没有题目");
            return "redirect:/student/exams";
        }
        
        // 直接创建新的考试会话并开始考试
        ExamSession session;
        try {
            session = examAssignmentService.createExamSessionForStudent(assignmentId, user.getUserId());
            System.out.println("创建新的考试会话: " + (session != null ? session.getSessionId() : "null"));
            if (session == null) {
                throw new RuntimeException("创建考试会话失败");
            }
            
            // 立即设置为进行中状态并设置开始时间
            session.setStatus(ExamSession.SessionStatus.ongoing);
            session.setStartedAt(LocalDateTime.now());
            session = examSessionRepository.save(session);
            System.out.println("设置会话状态为ongoing，开始时间: " + session.getStartedAt());
            
        } catch (Exception e) {
            System.err.println("创建考试会话失败: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "创建考试会话失败: " + e.getMessage());
            return "redirect:/student/exams";
        }
        
        // 获取题目分值信息
        List<PaperQuestion> paperQuestions = paperService.getPaperQuestions(session.getPaperId());
        Map<String, Integer> questionScoreMap = new HashMap<>();
        if (paperQuestions != null) {
            for (PaperQuestion pq : paperQuestions) {
                questionScoreMap.put(pq.getQuestionId(), pq.getScore());
            }
        }
        
        // 设置模型属性
        model.addAttribute("session", session);
        model.addAttribute("paper", paper);
        model.addAttribute("questions", questions);
        model.addAttribute("questionScoreMap", questionScoreMap);
        model.addAttribute("assignment", assignment);
        model.addAttribute("user", user);
        
        System.out.println("模型属性设置完成，返回考试页面");
        System.out.println("传递给模板的session: " + session);
        System.out.println("传递给模板的sessionId: " + session.getSessionId());
        System.out.println("传递给模板的session状态: " + session.getStatus());
        System.out.println("传递给模板的用户信息: " + (user != null ? user.getUsername() + " (ID: " + user.getUserId() + ")" : "null"));
        System.out.println("传递给模板的题目数量: " + (questions != null ? questions.size() : 0));
        System.out.println("传递给模板的题目数据: " + questions);
        System.out.println("传递给模板的试卷信息: " + paper);
        System.out.println("传递给模板的题目分值映射: " + questionScoreMap);
        
        // 重定向到考试页面，使用sessionId
        return "redirect:/student/exam/" + session.getSessionId() + "/take";
    }
    
    // 通过sessionId访问考试页面
    @GetMapping("/exam/{sessionId}/take")
    public String takeExam(@PathVariable String sessionId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户信息获取失败");
                return "redirect:/student/exams";
            }
            
            // 获取考试会话
            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                redirectAttributes.addFlashAttribute("error", "考试会话不存在");
                return "redirect:/student/exams";
            }
            
            // 验证权限
            if (!session.getStudentId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "无权限访问此考试");
                return "redirect:/student/exams";
            }
            
            // 检查考试状态
            if (ExamSession.SessionStatus.submitted.equals(session.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "考试已提交，无法再次参加");
                return "redirect:/student/exam/" + sessionId + "/result";
            }
            
            // 获取试卷信息
            Paper paper = paperService.getPaperById(session.getPaperId());
            if (paper == null) {
                redirectAttributes.addFlashAttribute("error", "试卷不存在");
                return "redirect:/student/exams";
            }
            
            // 获取试卷题目
            List<Question> questions = paperService.getPaperQuestionDetails(paper.getPaperId());
            if (questions == null || questions.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "试卷中没有题目");
                return "redirect:/student/exams";
            }
            
            // 获取题目分值信息
            List<PaperQuestion> paperQuestions = paperService.getPaperQuestions(session.getPaperId());
            Map<String, Integer> questionScoreMap = new HashMap<>();
            if (paperQuestions != null) {
                for (PaperQuestion pq : paperQuestions) {
                    questionScoreMap.put(pq.getQuestionId(), pq.getScore());
                }
            }
            
            // 获取考试分配信息
            ExamAssignment assignment = examAssignmentService.getAssignmentById(session.getAssignmentId());
            
            // 设置模型属性
            model.addAttribute("session", session);
            model.addAttribute("paper", paper);
            model.addAttribute("questions", questions);
            model.addAttribute("questionScoreMap", questionScoreMap);
            model.addAttribute("assignment", assignment);
            model.addAttribute("user", user);
            
            return "student/exam/take";
        } catch (Exception e) {
            System.err.println("访问考试页面失败: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "访问考试页面失败: " + e.getMessage());
            return "redirect:/student/exams";
        }
    }
    
    // 开始考试（通过sessionId）- 已删除，不再支持
    
    // 提交考试
    @PostMapping("/exam/{sessionId}/submit")
    public String submitExam(@PathVariable String sessionId,
                           @RequestParam(required = false) Map<String, String> answers,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== 提交考试方法被调用 ===");
            System.out.println("提交考试 - sessionId: " + sessionId + ", answers: " + answers);
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/student/exams";
            }
            
            // 获取考试会话并验证权限
            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                System.err.println("考试会话不存在: " + sessionId);
                redirectAttributes.addFlashAttribute("error", "考试会话不存在");
                return "redirect:/student/exams";
            }
            
            if (!session.getStudentId().equals(user.getUserId())) {
                System.err.println("无权限提交此考试: " + sessionId);
                redirectAttributes.addFlashAttribute("error", "无权限提交此考试");
                return "redirect:/student/exams";
            }
            
            // 检查考试状态
            if (ExamSession.SessionStatus.submitted.equals(session.getStatus())) {
                System.err.println("考试已提交: " + sessionId);
                redirectAttributes.addFlashAttribute("error", "考试已提交，无法重复提交");
                return "redirect:/student/exam/" + sessionId + "/result";
            }
            
            if (!ExamSession.SessionStatus.ongoing.equals(session.getStatus())) {
                System.err.println("考试状态不允许提交: " + session.getStatus());
                redirectAttributes.addFlashAttribute("error", "考试状态不允许提交");
                return "redirect:/student/exam/" + sessionId + "/result";
            }
            
            // 保存学生答案
            if (answers != null && !answers.isEmpty()) {
                for (Map.Entry<String, String> entry : answers.entrySet()) {
                    String questionId = entry.getKey();
                    String answer = entry.getValue();
                    if (answer != null && !answer.trim().isEmpty()) {
                        System.out.println("保存答案 - questionId: " + questionId + ", answer: " + answer);
                        examSessionService.saveAnswer(sessionId, questionId, answer);
                    }
                }
            } else {
                System.out.println("警告：没有收到任何答案数据");
            }
            
            // 提交考试
            examSessionService.submitExam(sessionId);
            
            // 重新计算成绩（确保成绩是最新的）
            examSessionService.updateSessionTotalScore(sessionId);
            
            // 获取提交后的考试会话信息
            ExamSession submittedSession = examSessionService.getSessionById(sessionId);
            
            // 检查是否有主观题需要评分
            boolean hasSubjectiveQuestions = examSessionService.hasSubjectiveQuestionsNeedingGrading(sessionId);
            String successMessage;
            
            if (hasSubjectiveQuestions) {
                // 有主观题，显示客观题得分
                double objectiveScore = submittedSession.getObjectiveScore() != null ? submittedSession.getObjectiveScore() : 0.0;
                successMessage = String.format("考试提交成功！您的客观题得分为 %.1f 分，主观题待教师评阅。", objectiveScore);
            } else {
                // 纯客观题，显示总分
                double totalScore = submittedSession.getTotalScore() != null ? submittedSession.getTotalScore() : 0.0;
                successMessage = String.format("考试提交成功！您的成绩为 %.1f 分，请查看详细结果。", totalScore);
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/student/exam/" + sessionId + "/result";
        } catch (Exception e) {
            System.err.println("考试提交失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "考试提交失败: " + e.getMessage());
            return "redirect:/student/exam/" + sessionId + "/result";
        }
    }
    
    // 自动提交考试（学生中途退出）
    @PostMapping("/exam/{sessionId}/auto-submit")
    @ResponseBody
    public Map<String, Object> autoSubmitExam(@PathVariable String sessionId) {
        try {
            System.out.println("=== 自动提交考试方法被调用 ===");
            System.out.println("自动提交考试 - sessionId: " + sessionId);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                return Map.of("success", false, "message", "用户不存在");
            }
            
            // 获取考试会话并验证权限
            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                return Map.of("success", false, "message", "考试会话不存在");
            }
            
            if (!session.getStudentId().equals(user.getUserId())) {
                return Map.of("success", false, "message", "无权限提交此考试");
            }
            
            // 检查考试状态
            if (ExamSession.SessionStatus.submitted.equals(session.getStatus())) {
                return Map.of("success", true, "message", "考试已提交");
            }
            
            if (!ExamSession.SessionStatus.ongoing.equals(session.getStatus())) {
                return Map.of("success", false, "message", "考试状态不允许提交");
            }
            
            // 自动提交考试
            session.setAutoSubmitted(true);
            examSessionService.submitExam(sessionId);
            
            // 获取提交后的成绩
            ExamSession submittedSession = examSessionService.getSessionById(sessionId);
            String message = String.format("考试已自动提交！您的成绩为 %.1f 分。", 
                submittedSession.getTotalScore() != null ? submittedSession.getTotalScore() : 0.0);
            
            return Map.of("success", true, "message", message);
        } catch (Exception e) {
            System.err.println("自动提交考试失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", "自动提交失败: " + e.getMessage());
        }
    }
    
    // 保存答案（AJAX）
    @PostMapping("/exam/{sessionId}/save")
    @ResponseBody
    public Map<String, Object> saveAnswer(@PathVariable String sessionId,
                                         @RequestParam String questionId,
                                         @RequestParam String answer) {
        try {
            System.out.println("=== 保存答案方法被调用 ===");
            System.out.println("保存答案 - sessionId: " + sessionId + ", questionId: " + questionId + ", answer: " + answer);

            // 验证用户权限
            User user = getCurrentUser();
            if (user == null) {
                return Map.of("success", false, "message", "用户未登录");
            }

            // 验证考试会话权限
            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                return Map.of("success", false, "message", "考试会话不存在");
            }

            if (!session.getStudentId().equals(user.getUserId())) {
                return Map.of("success", false, "message", "无权限操作此考试");
            }

            examSessionService.saveAnswer(sessionId, questionId, answer);
            System.out.println("答案保存成功");
            return Map.of("success", true, "message", "答案保存成功");
        } catch (Exception e) {
            System.err.println("保存答案失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", "答案保存失败: " + e.getMessage());
        }
    }

    // 批量保存答案（AJAX）
    @PostMapping("/exam/{sessionId}/save-batch")
    @ResponseBody
    public Map<String, Object> saveAnswersBatch(@PathVariable String sessionId,
                                                @RequestParam(required = false) List<String> questionId,
                                                @RequestParam(required = false) List<String> answer) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return Map.of("success", false, "message", "用户未登录");
            }

            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                return Map.of("success", false, "message", "考试会话不存在");
            }

            if (!session.getStudentId().equals(user.getUserId())) {
                return Map.of("success", false, "message", "无权限操作此考试");
            }

            if (questionId != null && answer != null) {
                int saved = 0;
                for (int i = 0; i < questionId.size() && i < answer.size(); i++) {
                    String qid = questionId.get(i);
                    String ans = answer.get(i);
                    if (ans != null && !ans.trim().isEmpty()) {
                        examSessionService.saveAnswer(sessionId, qid, ans);
                        saved++;
                    }
                }
                return Map.of("success", true, "message", "已保存 " + saved + " 道题目的答案");
            }

            return Map.of("success", true, "message", "无答案需要保存");
        } catch (Exception e) {
            System.err.println("批量保存答案失败: " + e.getMessage());
            return Map.of("success", false, "message", "批量保存失败: " + e.getMessage());
        }
    }

    // 获取已保存的答案（AJAX）
    @GetMapping("/exam/{sessionId}/answers")
    @ResponseBody
    public Map<String, Object> getAnswers(@PathVariable String sessionId) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return Map.of("success", false, "message", "用户未登录");
            }

            ExamSession session = examSessionService.getSessionById(sessionId);
            if (session == null) {
                return Map.of("success", false, "message", "考试会话不存在");
            }

            if (!session.getStudentId().equals(user.getUserId())) {
                return Map.of("success", false, "message", "无权限操作此考试");
            }

            List<ExamSessionAnswer> sessionAnswers = examSessionService.getSessionAnswers(sessionId);
            Map<String, String> answersMap = new HashMap<>();
            for (ExamSessionAnswer sa : sessionAnswers) {
                if (sa.getAnswerText() != null && !sa.getAnswerText().trim().isEmpty()) {
                    answersMap.put(sa.getQuestionId(), sa.getAnswerText());
                }
            }

            return Map.of("success", true, "answers", answersMap);
        } catch (Exception e) {
            System.err.println("获取答案失败: " + e.getMessage());
            return Map.of("success", false, "message", "获取答案失败: " + e.getMessage());
        }
    }
    
    // ==================== 考试结果 ====================
    
    // 查看考试结果
    @GetMapping("/exam/{sessionId}/result")
    public String examResult(@PathVariable String sessionId, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        // 获取考试会话和结果
        ExamSession session = examSessionService.getSessionById(sessionId);
        if (session == null || !session.getStudentId().equals(user.getUserId())) {
            return "redirect:/student/exams/history";
        }
        
        // 获取试卷信息
        Paper paper = paperService.getPaperById(session.getPaperId());
        if (paper == null) {
            return "redirect:/student/exams/history";
        }
        
        // 获取试卷题目（确保只显示当前试卷的题目）
        List<Question> questions = paperService.getPaperQuestionDetails(paper.getPaperId());
        
        // 获取学生答案
        List<ExamSessionAnswer> answers = examSessionService.getSessionAnswers(sessionId);
        
        // 创建题目ID到答案的映射，只包含当前试卷的题目
        Set<String> paperQuestionIds = questions.stream()
            .map(Question::getId)
            .collect(Collectors.toSet());
        
        // 过滤答案，只保留当前试卷的题目答案
        List<ExamSessionAnswer> filteredAnswers = answers.stream()
            .filter(answer -> paperQuestionIds.contains(answer.getQuestionId()))
            .collect(Collectors.toList());
        
        // 为每个答案加载关联的题目信息
        for (ExamSessionAnswer answer : filteredAnswers) {
            Question question = answer.getQuestion();
            if (question != null) {
                // 题目信息已经通过EAGER加载，无需额外查询
            }
        }
        
        // 重新计算成绩（确保成绩是最新的）
        examSessionService.updateSessionTotalScore(sessionId);
        
        // 重新获取更新后的session
        session = examSessionService.getSessionById(sessionId);
        
        // 如果所有主观题都已评分且状态为submitted，则更新为graded
        if (session.getStatus() == ExamSession.SessionStatus.submitted) {
            boolean needMore = examSessionService.hasSubjectiveQuestionsNeedingGrading(sessionId);
            if (!needMore) {
                examSessionService.updateSessionStatus(sessionId, ExamSession.SessionStatus.graded);
                session = examSessionService.getSessionById(sessionId);
            }
        }
        
        // 确保使用最新的session数据
        session = examSessionService.getSessionById(sessionId);
        
        // 判断状态
        boolean isGraded = session.getStatus() != null && session.getStatus().name().equals("graded");
        boolean isSubmitted = session.getStatus() != null && session.getStatus().name().equals("submitted");
        
        model.addAttribute("session", session);
        model.addAttribute("paper", paper);
        model.addAttribute("answers", filteredAnswers);
        model.addAttribute("user", user);
        model.addAttribute("isGraded", isGraded);
        model.addAttribute("isSubmitted", isSubmitted);
        
        return "student/exam/result";
    }
    
    // ==================== 学习资源 ====================
    
    // 学习资源列表
    @GetMapping("/resources")
    public String resourceList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        // 获取所有可用的学习资源
        List<LearningResource> resources = learningResourceService.getAllResources();
        
        model.addAttribute("resources", resources);
        model.addAttribute("user", user);
        return "student/resource/list";
    }
    
    // 下载资源
    @GetMapping("/resources/{resourceId}/download")
    public ResponseEntity<Resource> downloadResource(@PathVariable String resourceId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // 获取学习资源
            LearningResource learningResource = learningResourceService.getResourceById(resourceId);
            if (learningResource == null) {
                redirectAttributes.addFlashAttribute("error", "资源不存在");
                return ResponseEntity.notFound().build();
            }
            
            // 检查资源类型和文件路径
            if (learningResource.getType() == LearningResource.ResourceType.LINK) {
                redirectAttributes.addFlashAttribute("error", "链接类型资源无法下载");
                return ResponseEntity.badRequest().build();
            }
            
            if (learningResource.getFilePath() == null || learningResource.getFilePath().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "资源文件不存在");
                return ResponseEntity.badRequest().build();
            }
            
            // 构建文件路径 - 数据库中的file_path已经包含完整路径
            String filePath = learningResource.getFilePath();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1); // 移除开头的斜杠
            }
            
            // 创建文件资源 - 直接使用数据库中的完整路径
            Path path = Paths.get(System.getProperty("user.dir") + "/" + filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (!resource.exists()) {
                redirectAttributes.addFlashAttribute("error", "资源文件不存在");
                return ResponseEntity.notFound().build();
            }
            
            // 更新下载次数
            learningResourceService.incrementDownloadCount(resourceId);
            
            // 使用更准确的内容类型检测
            String contentType = null;
            try {
                contentType = java.nio.file.Files.probeContentType(path);
            } catch (Exception e) {
                System.err.println("Error probing content type for download: " + e.getMessage());
            }
            
            // 如果自动检测失败，使用备用逻辑
            if (contentType == null) {
                String fileName = path.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (fileName.endsWith(".doc")) {
                    contentType = "application/msword";
                } else if (fileName.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                } else if (fileName.endsWith(".xls")) {
                    contentType = "application/vnd.ms-excel";
                } else if (fileName.endsWith(".xlsx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                } else if (fileName.endsWith(".ppt")) {
                    contentType = "application/vnd.ms-powerpoint";
                } else if (fileName.endsWith(".pptx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                } else if (fileName.endsWith(".txt")) {
                    contentType = "text/plain; charset=utf-8";
                } else if (fileName.endsWith(".mp4")) {
                    contentType = "video/mp4";
                } else if (fileName.endsWith(".avi")) {
                    contentType = "video/x-msvideo";
                } else if (fileName.endsWith(".mov")) {
                    contentType = "video/quicktime";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            // 保持原始文件名和扩展名
            String originalFileName = path.getFileName().toString();
            String encodedFilename = java.net.URLEncoder.encode(originalFileName, "UTF-8");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
                    
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "资源下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 预览资源
    @GetMapping("/resources/{resourceId}/preview")
    public ResponseEntity<Resource> previewResource(@PathVariable String resourceId) {
        try {
            // 获取学习资源
            LearningResource learningResource = learningResourceService.getResourceById(resourceId);
            if (learningResource == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 检查资源类型和文件路径
            if (learningResource.getType() == LearningResource.ResourceType.LINK) {
                return ResponseEntity.badRequest().build();
            }
            
            if (learningResource.getFilePath() == null || learningResource.getFilePath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // 构建文件路径 - 数据库中的file_path已经包含完整路径
            String filePath = learningResource.getFilePath();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1); // 移除开头的斜杠
            }
            
            // 创建文件资源 - 直接使用数据库中的完整路径
            Path path = Paths.get(System.getProperty("user.dir") + "/" + filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 使用更准确的内容类型检测
            String contentType = null;
            try {
                contentType = java.nio.file.Files.probeContentType(path);
            } catch (Exception e) {
                System.err.println("Error probing content type: " + e.getMessage());
            }
            
            // 如果自动检测失败，使用备用逻辑
            if (contentType == null) {
                if (learningResource.getType() == LearningResource.ResourceType.DOCUMENT) {
                    if (filePath.toLowerCase().endsWith(".pdf")) {
                        contentType = "application/pdf";
                    } else if (filePath.toLowerCase().endsWith(".doc")) {
                        contentType = "application/msword";
                    } else if (filePath.toLowerCase().endsWith(".docx")) {
                        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    } else if (filePath.toLowerCase().endsWith(".xls")) {
                        contentType = "application/vnd.ms-excel";
                    } else if (filePath.toLowerCase().endsWith(".xlsx")) {
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else if (filePath.toLowerCase().endsWith(".ppt")) {
                        contentType = "application/vnd.ms-powerpoint";
                    } else if (filePath.toLowerCase().endsWith(".pptx")) {
                        contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                    } else {
                        contentType = "application/octet-stream";
                    }
                } else if (learningResource.getType() == LearningResource.ResourceType.VIDEO) {
                    if (filePath.toLowerCase().endsWith(".mp4")) {
                        contentType = "video/mp4";
                    } else if (filePath.toLowerCase().endsWith(".avi")) {
                        contentType = "video/x-msvideo";
                    } else if (filePath.toLowerCase().endsWith(".mov")) {
                        contentType = "video/quicktime";
                    } else {
                        contentType = "video/mp4";
                    }
                } else if (learningResource.getType() == LearningResource.ResourceType.IMAGE) {
                    if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (filePath.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    } else if (filePath.toLowerCase().endsWith(".gif")) {
                        contentType = "image/gif";
                    } else {
                        contentType = "image/jpeg";
                    }
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            // 对文件名进行URL编码以支持中文字符
            String encodedFilename = java.net.URLEncoder.encode(learningResource.getTitle(), "UTF-8");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== 个人中心 ====================
    
    // 更新个人信息
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("email") String email,
                               @RequestParam("phone") String phone,
                               @RequestParam("department") String department,
                               @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username).orElse(null);
            
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/student/profile";
            }
            
            // 更新用户信息
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setDepartment(department);
            
            // 处理头像上传
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // 检查文件类型
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "请上传图片文件");
                    return "redirect:/student/profile";
                }
                
                // 生成文件名
                String originalFilename = avatarFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = "avatar_" + System.currentTimeMillis() + extension;
                
                // 创建上传目录
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                // 保存文件
                File file = new File(uploadDir + fileName);
                avatarFile.transferTo(file);
                
                // 删除旧头像文件（如果存在）
                if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                    File oldFile = new File(uploadDir + currentUser.getAvatar());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                
                // 更新用户头像
                currentUser.setAvatar(fileName);
            }
            
            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("success", "个人信息更新成功！");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "个人信息更新失败：" + e.getMessage());
        }
        
        return "redirect:/student/profile";
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
                return "redirect:/student/profile";
            }
            
            // 验证当前密码
            if (!user.getPassword().equals(currentPassword)) {
                redirectAttributes.addFlashAttribute("error", "当前密码不正确");
                return "redirect:/student/profile";
            }
            
            // 验证新密码
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "新密码与确认密码不匹配");
                return "redirect:/student/profile";
            }
            
            // 验证新密码长度
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "新密码长度不能少于6个字符");
                return "redirect:/student/profile";
            }
            
            // 更新密码
            user.setPassword(newPassword);
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("success", "密码修改成功！");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "密码修改失败：" + e.getMessage());
        }
        
        return "redirect:/student/profile";
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
                return "redirect:/student/profile";
            }
            
            // 验证文件
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择要上传的头像文件");
                return "redirect:/student/profile";
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "只能上传图片文件");
                return "redirect:/student/profile";
            }
            
            // 验证文件大小（限制为5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "头像文件大小不能超过5MB");
                return "redirect:/student/profile";
            }
            
            // 创建uploads/avatars目录（使用项目根目录）
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    redirectAttributes.addFlashAttribute("error", "无法创建上传目录，请检查权限");
                    return "redirect:/student/profile";
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
                return "redirect:/student/profile";
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
        
        return "redirect:/student/profile";
    }
    
    // 个人信息
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username).orElse(null);
        
        model.addAttribute("user", user);
        return "student/profile";
    }
    

} 