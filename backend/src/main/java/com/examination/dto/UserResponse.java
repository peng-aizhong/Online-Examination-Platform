package com.examination.dto;

import com.examination.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String realName;
    private String role;
    private String department;
    private String phone;
    private String avatar;
    private Boolean active;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getUsername())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .active(user.getActive())
                .build();
    }
}
