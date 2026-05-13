package com.examination.dto;

import com.examination.entity.User.UserRole;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private UserRole role;
    private String department;
    private String email;
    private String phone;
    private Boolean active;        // 启用/禁用
}