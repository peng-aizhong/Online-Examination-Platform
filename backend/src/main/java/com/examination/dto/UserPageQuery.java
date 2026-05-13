package com.examination.dto;

import com.examination.entity.User.UserRole;
import lombok.Data;

@Data
public class UserPageQuery {
    private Integer page = 1;
    private Integer size = 10;
    private UserRole role;
    private Boolean active;
}