package com.examination.service;

import com.examination.dto.*;
import com.examination.entity.User;
import com.examination.repository.UserRepository;
import com.examination.security.JwtTokenProvider;
import com.examination.repository.ExamAssignmentRepository;
import com.examination.repository.ExamAssignmentStudentRepository;
import com.examination.repository.ExamSessionRepository;
import com.examination.entity.ExamAssignment;
import com.examination.entity.ExamAssignmentStudent;
import com.examination.entity.ExamSession;
import com.examination.entity.Paper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ExamAssignmentRepository examAssignmentRepository;

    @Autowired
    private ExamAssignmentStudentRepository examAssignmentStudentRepository;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("用户账户已被禁用");
        }

        String storedPassword = user.getPassword();
        boolean passwordMatch = false;
        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
            passwordMatch = passwordEncoder.matches(loginRequest.getPassword(), storedPassword);
        } else {
            passwordMatch = loginRequest.getPassword() != null && loginRequest.getPassword().equals(storedPassword);
        }
        if (!passwordMatch) {
            throw new RuntimeException("密码错误");
        }

        if (loginRequest.getRole() != null && !loginRequest.getRole().isBlank()) {
            String requestRole = loginRequest.getRole().trim().toLowerCase();
            if (!user.getRole().name().equals(requestRole)) {
                throw new RuntimeException("用户身份不匹配");
            }
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        UserResponse userResponse = UserResponse.fromEntity(user);

        return LoginResponse.builder()
                .token(token)
                .userInfo(userResponse)
                .build();
    }

    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isBlank()
                && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User.UserRole role;
        try {
            role = User.UserRole.valueOf(registerRequest.getRole().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的用户角色");
        }

        String userId = registerRequest.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = "U" + System.currentTimeMillis() % 1000000000;
        }

        User user = User.builder()
                .userId(userId)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .department(registerRequest.getDepartment())
                .phone(registerRequest.getPhone())
                .active(true)
                .build();

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateProfile(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被占用");
        }

        if (request.getEmail() != null) user.setEmail(request.getEmail().isBlank() ? null : request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone().isBlank() ? null : request.getPhone());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment().isBlank() ? null : request.getDepartment());

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public void changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String storedPassword = user.getPassword();
        boolean oldPasswordMatch;
        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
            oldPasswordMatch = passwordEncoder.matches(request.getOldPassword(), storedPassword);
        } else {
            oldPasswordMatch = request.getOldPassword() != null && request.getOldPassword().equals(storedPassword);
        }

        if (!oldPasswordMatch) {
            throw new RuntimeException("原密码错误");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    // 学生模块：获取可参与的考试列表
    // 返回考试名称、考试时间、考试状态
    // 提供给前端学生首页展示

    public List<AssignmentItemResponse> getStudentAssignments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getRole() != User.UserRole.student) {
            throw new RuntimeException("仅学生可访问该功能");
        }

        LocalDateTime now = LocalDateTime.now();

        List<ExamAssignmentStudent> assignments = examAssignmentStudentRepository.findByStudentIdWithAssignment(user.getUserId());
        List<AssignmentItemResponse> result = new ArrayList<>();

        for (ExamAssignmentStudent eas : assignments) {
            ExamAssignment assignment = examAssignmentRepository.findById(eas.getAssignmentId()).orElse(null);
            if (assignment == null) continue;

            Paper paper = assignment.getPaper();
            List<ExamSession> sessions = examSessionRepository.findByAssignment_AssignmentIdAndStudent_UserId(
                    assignment.getAssignmentId(), user.getUserId());

            long submittedCount = sessions.stream()
                    .filter(s -> "submitted".equals(s.getStatus()))
                    .count();

            Optional<ExamSession> bestSession = sessions.stream()
                    .filter(s -> "submitted".equals(s.getStatus()) && Boolean.TRUE.equals(s.getIsBestScore()))
                    .findFirst();

            String sessionStatus = resolveAssignmentStatus(assignment, now, (int) submittedCount);

            result.add(AssignmentItemResponse.builder()
                    .assignmentId(assignment.getAssignmentId())
                    .assignmentName(assignment.getAssignmentName())
                    .paperId(paper.getPaperId())
                    .paperName(paper.getPaperName())
                    .subjectName(paper.getSubject() != null ? paper.getSubject().getSubjectName() : "")
                    .durationMinutes(assignment.getDurationMinutes())
                    .totalScore(paper.getTotalScore())
                    .examStartTime(assignment.getExamStartTime())
                    .examEndTime(assignment.getExamEndTime())
                    .status(sessionStatus)
                    .myAttempts((int) submittedCount)
                    .maxAttempts(assignment.getMaxAttempts())
                    .bestScore(bestSession.map(ExamSession::getTotalScore).orElse(null))
                    .sessionStatus(sessionStatus)
                    .build());
        }

        return result;
    }

    private String resolveAssignmentStatus(ExamAssignment assignment, LocalDateTime now, int submittedCount) {
        if (submittedCount >= assignment.getMaxAttempts()) {
            return "completed";
        }
        if (now.isBefore(assignment.getExamStartTime())) {
            return "scheduled";
        }
        if (now.isAfter(assignment.getExamEndTime())) {
            return "finished";
        }
        return "active";
    }
}
