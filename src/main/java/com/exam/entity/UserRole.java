package com.exam.entity;

public enum UserRole {
    ADMIN("admin", "管理员"),
    TEACHER("teacher", "教师"),
    STUDENT("student", "学生");
    
    private final String value;
    private final String displayName;
    
    UserRole(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role value: " + value);
    }
    
    // 根据枚举名称获取枚举值
    public static UserRole fromName(String name) {
        try {
            return UserRole.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role name: " + name);
        }
    }
} 