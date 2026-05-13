package com.examination.dto;

import com.examination.entity.User.UserRole;
import lombok.Data;

@Data
public class UserCreateRequest {
    private String userId;      // 可选，不填自动生成
    private String username;    // 必填
    private String password;    // 必填（明文）
    private UserRole role;
    private String department;
    private String email;
    private String phone;
}