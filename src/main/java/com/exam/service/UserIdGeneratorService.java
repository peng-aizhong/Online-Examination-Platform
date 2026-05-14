package com.exam.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserIdGeneratorService {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    public String generateUserId(String role) {
        String prefix = getRolePrefix(role);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return prefix + timestamp.substring(timestamp.length() - 6);
    }
    
    private String getRolePrefix(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                return "ADMIN";
            case "teacher":
                return "T";
            case "student":
                return "S";
            default:
                return "U";
        }
    }
} 