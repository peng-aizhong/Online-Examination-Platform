-- 创建数据库
CREATE DATABASE IF NOT EXISTS examination_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examination_db;

-- 创建users表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(100) COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL COMMENT '角色(STUDENT/TEACHER/ADMIN)',
    active TINYINT DEFAULT 1 COMMENT '是否活跃(1/0)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据
INSERT INTO users (username, email, password, real_name, role, active) VALUES
('student01', 'student01@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gZvWFm', '张三', 'STUDENT', 1),
('teacher01', 'teacher01@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gZvWFm', '王五', 'TEACHER', 1),
('admin01', 'admin01@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gZvWFm', '管理员', 'ADMIN', 1);

-- 注: 默认密码为 123456 (BCrypt加密)

COMMIT;
