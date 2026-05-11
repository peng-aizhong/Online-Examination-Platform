package com.examination.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userId;
    private String username;
    private String password;
    private String email;
    private String realName;
    private String role;
    private String department;
    private String phone;
}
