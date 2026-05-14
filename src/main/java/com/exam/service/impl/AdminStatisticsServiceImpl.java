package com.exam.service.impl;

import com.exam.entity.*;
import com.exam.repository.*;
import com.exam.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员统计服务实现类
 */
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private ExamSessionRepository examSessionRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    // ========== 基础统计 ==========
    
    @Override
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 用户统计
        Map<String, Object> userStats = getUserStatistics();
        overview.put("userStatistics", userStats);
        
        // 科目统计
        Map<String, Object> subjectStats = getSubjectStatistics();
        overview.put("subjectStatistics", subjectStats);
        
        // 试卷统计
        Map<String, Object> paperStats = getPaperStatistics();
        overview.put("paperStatistics", paperStats);
        
        // 考试会话统计
        Map<String, Object> sessionStats = getExamSessionStatistics();
        overview.put("sessionStatistics", sessionStats);
        
        // 题目统计
        long totalQuestions = questionRepository.count();
        overview.put("totalQuestions", totalQuestions);
        
        return overview;
    }
    
    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // 各角色用户数
        Map<String, Long> roleDistribution = getUserRoleDistribution();
        stats.put("roleDistribution", roleDistribution);
        
        // 状态分布
        Map<String, Long> statusDistribution = getUserStatusDistribution();
        stats.put("statusDistribution", statusDistribution);
        
        // 活跃用户数
        long activeUsers = userRepository.countByIsActiveTrue();
        stats.put("activeUsers", activeUsers);
        
        // 今日新增用户数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewUsers = userRepository.countByCreatedAtAfter(today);
        stats.put("todayNewUsers", todayNewUsers);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getSubjectStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总科目数
        long totalSubjects = subjectRepository.count();
        stats.put("totalSubjects", totalSubjects);
        
        // 状态分布
        Map<String, Long> statusDistribution = getSubjectStatusDistribution();
        stats.put("statusDistribution", statusDistribution);
        
        // 活跃科目数
        long activeSubjects = subjectRepository.countByStatus(Subject.SubjectStatus.active);
        stats.put("activeSubjects", activeSubjects);
        
        // 今日新增科目数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewSubjects = subjectRepository.countByCreatedAtAfter(today);
        stats.put("todayNewSubjects", todayNewSubjects);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getPaperStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总试卷数
        long totalPapers = paperRepository.count();
        stats.put("totalPapers", totalPapers);
        
        // 状态分布
        Map<String, Long> statusDistribution = getPaperStatusDistribution();
        stats.put("statusDistribution", statusDistribution);
        
        // 难度分布
        Map<String, Long> difficultyDistribution = getPaperDifficultyDistribution();
        stats.put("difficultyDistribution", difficultyDistribution);
        
        // 科目分布
        Map<String, Long> subjectDistribution = getPaperCountBySubject();
        stats.put("subjectDistribution", subjectDistribution);
        
        // 今日新增试卷数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewPapers = paperRepository.countByCreatedAtAfter(today);
        stats.put("todayNewPapers", todayNewPapers);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getExamSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总考试会话数
        long totalSessions = examSessionRepository.count();
        stats.put("totalSessions", totalSessions);
        
        // 状态分布
        Map<String, Long> statusDistribution = getExamSessionStatusDistribution();
        stats.put("statusDistribution", statusDistribution);
        
        // 科目分布
        Map<String, Long> subjectDistribution = getExamSessionCountBySubject();
        stats.put("subjectDistribution", subjectDistribution);
        
        // 进行中的考试数
        long ongoingSessions = examSessionRepository.countByStatus(ExamSession.SessionStatus.ongoing);
        stats.put("ongoingSessions", ongoingSessions);
        
        // 今日考试会话数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todaySessions = examSessionRepository.countByStartedAtAfter(today);
        stats.put("todaySessions", todaySessions);
        
        return stats;
    }
    
    // ========== 时间维度统计 ==========
    
    @Override
    public Map<String, Object> getTodayStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        // 今日新增用户
        long todayNewUsers = userRepository.countByCreatedAtAfter(today);
        stats.put("newUsers", todayNewUsers);
        
        // 今日新增科目
        long todayNewSubjects = subjectRepository.countByCreatedAtAfter(today);
        stats.put("newSubjects", todayNewSubjects);
        
        // 今日新增试卷
        long todayNewPapers = paperRepository.countByCreatedAtAfter(today);
        stats.put("newPapers", todayNewPapers);
        
        // 今日考试会话
        long todaySessions = examSessionRepository.countByStartedAtAfter(today);
        stats.put("newSessions", todaySessions);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getWeekStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0);
        
        // 本周新增用户
        long weekNewUsers = userRepository.countByCreatedAtAfter(weekStart);
        stats.put("newUsers", weekNewUsers);
        
        // 本周新增科目
        long weekNewSubjects = subjectRepository.countByCreatedAtAfter(weekStart);
        stats.put("newSubjects", weekNewSubjects);
        
        // 本周新增试卷
        long weekNewPapers = paperRepository.countByCreatedAtAfter(weekStart);
        stats.put("newPapers", weekNewPapers);
        
        // 本周考试会话
        long weekSessions = examSessionRepository.countByStartedAtAfter(weekStart);
        stats.put("newSessions", weekSessions);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getMonthStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0);
        
        // 本月新增用户
        long monthNewUsers = userRepository.countByCreatedAtAfter(monthStart);
        stats.put("newUsers", monthNewUsers);
        
        // 本月新增科目
        long monthNewSubjects = subjectRepository.countByCreatedAtAfter(monthStart);
        stats.put("newSubjects", monthNewSubjects);
        
        // 本月新增试卷
        long monthNewPapers = paperRepository.countByCreatedAtAfter(monthStart);
        stats.put("newPapers", monthNewPapers);
        
        // 本月考试会话
        long monthSessions = examSessionRepository.countByStartedAtAfter(monthStart);
        stats.put("newSessions", monthSessions);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // 这里需要根据具体的Repository方法来实现
        // 由于Repository可能没有按日期范围查询的方法，这里返回基础统计
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("message", "按日期范围统计功能待实现");
        
        return stats;
    }
    
    // ========== 趋势分析 ==========
    
    @Override
    public List<Map<String, Object>> getUserRegistrationTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 获取最近30天的用户注册趋势
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime nextDate = date.plusDays(1);
            
            // 这里需要Repository支持按日期范围查询
            // 暂时返回模拟数据
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("count", (int)(Math.random() * 10)); // 模拟数据
            
            trend.add(dayData);
        }
        
        return trend;
    }
    
    @Override
    public List<Map<String, Object>> getExamCompletionTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 获取最近30天的考试完成趋势
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("count", (int)(Math.random() * 20)); // 模拟数据
            
            trend.add(dayData);
        }
        
        return trend;
    }
    
    @Override
    public List<Map<String, Object>> getPaperCreationTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 获取最近30天的试卷创建趋势
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("count", (int)(Math.random() * 5)); // 模拟数据
            
            trend.add(dayData);
        }
        
        return trend;
    }
    
    // ========== 分布统计 ==========
    
    @Override
    public Map<String, Long> getUserRoleDistribution() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
            .collect(Collectors.groupingBy(
                User::getRole,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getUserStatusDistribution() {
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> statusStats = new HashMap<>();
        
        long activeCount = allUsers.stream()
            .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
            .count();
        long inactiveCount = allUsers.size() - activeCount;
        
        statusStats.put("active", activeCount);
        statusStats.put("inactive", inactiveCount);
        
        return statusStats;
    }
    
    @Override
    public Map<String, Long> getSubjectStatusDistribution() {
        List<Subject> allSubjects = subjectRepository.findAll();
        return allSubjects.stream()
            .collect(Collectors.groupingBy(
                subject -> subject.getStatus().name(),
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getPaperStatusDistribution() {
        List<Paper> allPapers = paperRepository.findAll();
        return allPapers.stream()
            .collect(Collectors.groupingBy(
                Paper::getStatus,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getExamSessionStatusDistribution() {
        List<ExamSession> allSessions = examSessionRepository.findAll();
        return allSessions.stream()
            .collect(Collectors.groupingBy(
                session -> session.getStatus().getDisplayName(),
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getPaperDifficultyDistribution() {
        List<Paper> allPapers = paperRepository.findAll();
        return allPapers.stream()
            .collect(Collectors.groupingBy(
                Paper::getDifficultyLevel,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<String, Long> getPaperCountBySubject() {
        List<Paper> allPapers = paperRepository.findAll();
        Map<String, Long> subjectCounts = new HashMap<>();
        
        for (Paper paper : allPapers) {
            Subject subject = subjectRepository.findById(paper.getSubjectId()).orElse(null);
            if (subject != null) {
                String subjectName = subject.getSubjectName();
                subjectCounts.put(subjectName, subjectCounts.getOrDefault(subjectName, 0L) + 1);
            }
        }
        
        return subjectCounts;
    }
    
    @Override
    public Map<String, Long> getExamSessionCountBySubject() {
        List<ExamSession> allSessions = examSessionRepository.findAll();
        Map<String, Long> subjectCounts = new HashMap<>();
        
        for (ExamSession session : allSessions) {
            Paper paper = paperRepository.findById(session.getPaperId()).orElse(null);
            if (paper != null) {
                Subject subject = subjectRepository.findById(paper.getSubjectId()).orElse(null);
                if (subject != null) {
                    String subjectName = subject.getSubjectName();
                    subjectCounts.put(subjectName, subjectCounts.getOrDefault(subjectName, 0L) + 1);
                }
            }
        }
        
        return subjectCounts;
    }
    
    // ========== 成绩分析 ==========
    
    @Override
    public Map<String, Object> getScoreDistribution() {
        Map<String, Object> stats = new HashMap<>();
        List<ExamSession> allSessions = examSessionRepository.findAll();
        
        // 计算平均分
        double averageScore = allSessions.stream()
            .filter(session -> session.getTotalScore() != null)
            .mapToDouble(ExamSession::getTotalScore)
            .average()
            .orElse(0.0);
        
        // 计算最高分和最低分
        double maxScore = allSessions.stream()
            .filter(session -> session.getTotalScore() != null)
            .mapToDouble(ExamSession::getTotalScore)
            .max()
            .orElse(0.0);
        
        double minScore = allSessions.stream()
            .filter(session -> session.getTotalScore() != null)
            .mapToDouble(ExamSession::getTotalScore)
            .min()
            .orElse(0.0);
        
        stats.put("averageScore", averageScore);
        stats.put("maxScore", maxScore);
        stats.put("minScore", minScore);
        stats.put("totalExams", allSessions.size());
        
        return stats;
    }
    
    @Override
    public Map<String, Double> getAverageScoreBySubject() {
        Map<String, Double> subjectScores = new HashMap<>();
        List<ExamSession> allSessions = examSessionRepository.findAll();
        
        Map<String, List<ExamSession>> sessionsBySubject = new HashMap<>();
        
        for (ExamSession session : allSessions) {
            Paper paper = paperRepository.findById(session.getPaperId()).orElse(null);
            if (paper != null) {
                Subject subject = subjectRepository.findById(paper.getSubjectId()).orElse(null);
                if (subject != null) {
                    String subjectName = subject.getSubjectName();
                    sessionsBySubject.computeIfAbsent(subjectName, k -> new ArrayList<>()).add(session);
                }
            }
        }
        
        for (Map.Entry<String, List<ExamSession>> entry : sessionsBySubject.entrySet()) {
            double averageScore = entry.getValue().stream()
                .filter(session -> session.getTotalScore() != null)
                .mapToDouble(ExamSession::getTotalScore)
                .average()
                .orElse(0.0);
            subjectScores.put(entry.getKey(), averageScore);
        }
        
        return subjectScores;
    }
    
    @Override
    public Map<String, Double> getAverageScoreByDifficulty() {
        Map<String, Double> difficultyScores = new HashMap<>();
        List<ExamSession> allSessions = examSessionRepository.findAll();
        
        Map<String, List<ExamSession>> sessionsByDifficulty = new HashMap<>();
        
        for (ExamSession session : allSessions) {
            Paper paper = paperRepository.findById(session.getPaperId()).orElse(null);
            if (paper != null) {
                String difficulty = paper.getDifficultyLevel();
                sessionsByDifficulty.computeIfAbsent(difficulty, k -> new ArrayList<>()).add(session);
            }
        }
        
        for (Map.Entry<String, List<ExamSession>> entry : sessionsByDifficulty.entrySet()) {
            double averageScore = entry.getValue().stream()
                .filter(session -> session.getTotalScore() != null)
                .mapToDouble(ExamSession::getTotalScore)
                .average()
                .orElse(0.0);
            difficultyScores.put(entry.getKey(), averageScore);
        }
        
        return difficultyScores;
    }
    
    @Override
    public Map<String, Object> getPassRateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<ExamSession> allSessions = examSessionRepository.findAll();
        
        // 假设60分为及格线
        double passScore = 60.0;
        
        long totalExams = allSessions.size();
        long passedExams = allSessions.stream()
            .filter(session -> session.getTotalScore() != null && session.getTotalScore() >= passScore)
            .count();
        
        double passRate = totalExams > 0 ? (double) passedExams / totalExams * 100 : 0.0;
        
        stats.put("totalExams", totalExams);
        stats.put("passedExams", passedExams);
        stats.put("failedExams", totalExams - passedExams);
        stats.put("passRate", passRate);
        
        return stats;
    }
    
    // ========== 活跃度分析 ==========
    
    @Override
    public Map<String, Object> getUserActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 活跃用户数（最近7天有考试记录的用户）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long activeUsers = examSessionRepository.findAll().stream()
            .filter(session -> session.getStartedAt() != null && session.getStartedAt().isAfter(weekAgo))
            .map(ExamSession::getStudentId)
            .distinct()
            .count();
        
        stats.put("activeUsers", activeUsers);
        stats.put("totalUsers", userRepository.count());
        stats.put("activityRate", userRepository.count() > 0 ? (double) activeUsers / userRepository.count() * 100 : 0.0);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getSystemUsageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 系统使用情况统计
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSubjects", subjectRepository.count());
        stats.put("totalPapers", paperRepository.count());
        stats.put("totalSessions", examSessionRepository.count());
        stats.put("totalQuestions", questionRepository.count());
        
        // 今日活跃度
        Map<String, Object> todayStats = getTodayStatistics();
        stats.put("todayActivity", todayStats);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getScoreStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<ExamSession> allSessions = examSessionRepository.findAll();
        
        // 计算平均分
        double averageScore = allSessions.stream()
            .filter(session -> session.getTotalScore() != null)
            .mapToDouble(ExamSession::getTotalScore)
            .average()
            .orElse(0.0);
        
        // 计算最高分和最低分
        double maxScore = allSessions.stream()
            .filter(session -> session.getTotalScore() != null)
            .mapToDouble(ExamSession::getTotalScore)
            .max()
            .orElse(0.0);
        
        double minScore = allSessions.stream()
            .filter(session -> session.getTotalScore() != null)
            .mapToDouble(ExamSession::getTotalScore)
            .min()
            .orElse(0.0);
        
        stats.put("averageScore", averageScore);
        stats.put("maxScore", maxScore);
        stats.put("minScore", minScore);
        stats.put("totalExams", allSessions.size());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getExamStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 试卷统计
        Map<String, Object> paperStats = getPaperStatistics();
        statistics.put("paperStatistics", paperStats);
        
        // 考试会话统计
        Map<String, Object> sessionStats = getExamSessionStatistics();
        statistics.put("sessionStatistics", sessionStats);
        
        // 进行中的考试数量
        long ongoingCount = examSessionRepository.countByStatus(ExamSession.SessionStatus.ongoing);
        statistics.put("ongoingExamCount", ongoingCount);
        
        // 待阅卷的考试数量
        long pendingGradingCount = examSessionRepository.findPendingGradingExamSessions().size();
        statistics.put("pendingGradingCount", pendingGradingCount);
        
        return statistics;
    }
    
    // ========== 新增方法 ==========
    
    @Override
    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 获取指定天数的用户注册趋势
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime nextDate = date.plusDays(1);
            
            // 这里需要Repository支持按日期范围查询
            // 暂时返回模拟数据
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("count", (int)(Math.random() * 10)); // 模拟数据
            
            trend.add(dayData);
        }
        
        return trend;
    }
    
    @Override
    public List<Map<String, Object>> getExamCompletionTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 获取指定天数的考试完成趋势
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("count", (int)(Math.random() * 20)); // 模拟数据
            
            trend.add(dayData);
        }
        
        return trend;
    }
    
    @Override
    public List<Map<String, Object>> getUserDetails() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userDetails = new ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("username", user.getUsername());
            userDetail.put("role", user.getRole());
            userDetail.put("department", user.getDepartment());
            userDetail.put("createdAt", user.getCreatedAt());
            userDetail.put("lastLoginAt", null);
            userDetail.put("isActive", user.getIsActive());
            
            // 获取用户考试统计
            long examCount = examSessionRepository.countByStudentId(user.getUserId());
            userDetail.put("examCount", examCount);
            
            // 计算平均分
            if (examCount > 0) {
                Double averageScore = examSessionRepository.findAverageScoreByStudentId(user.getUserId());
                userDetail.put("averageScore", averageScore != null ? averageScore : 0.0);
            } else {
                userDetail.put("averageScore", null);
            }
            
            userDetails.add(userDetail);
        }
        
        return userDetails;
    }
    
    @Override
    public List<Map<String, Object>> getSubjectDetailsStatistics() {
        List<Subject> subjects = subjectRepository.findAll();
        List<Map<String, Object>> subjectStats = new ArrayList<>();
        
        for (Subject subject : subjects) {
            Map<String, Object> subjectStat = new HashMap<>();
            subjectStat.put("subjectName", subject.getSubjectName());
            
            // 获取该科目的考试统计
            List<Paper> papers = paperRepository.findBySubjectId(subject.getSubjectId());
            long examCount = 0;
            double totalScore = 0.0;
            double maxScore = 0.0;
            double minScore = 100.0;
            long participantCount = 0;
            
            for (Paper paper : papers) {
                List<ExamSession> sessions = examSessionRepository.findByPaperId(paper.getPaperId());
                examCount += sessions.size();
                participantCount += sessions.stream().map(ExamSession::getStudentId).distinct().count();
                
                for (ExamSession session : sessions) {
                    if (session.getTotalScore() != null) {
                        totalScore += session.getTotalScore();
                        maxScore = Math.max(maxScore, session.getTotalScore());
                        minScore = Math.min(minScore, session.getTotalScore());
                    }
                }
            }
            
            subjectStat.put("examCount", examCount);
            subjectStat.put("averageScore", examCount > 0 ? totalScore / examCount : 0.0);
            subjectStat.put("maxScore", maxScore);
            subjectStat.put("minScore", minScore == 100.0 ? 0.0 : minScore);
            subjectStat.put("participantCount", participantCount);
            
            // 计算通过率
            long passedExams = 0;
            for (Paper paper : papers) {
                List<ExamSession> sessions = examSessionRepository.findByPaperId(paper.getPaperId());
                passedExams += sessions.stream()
                    .filter(session -> session.getTotalScore() != null && session.getTotalScore() >= 60.0)
                    .count();
            }
            subjectStat.put("passRate", examCount > 0 ? (double) passedExams / examCount * 100 : 0.0);
            
            subjectStats.add(subjectStat);
        }
        
        return subjectStats;
    }
}
