-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` VARCHAR(20) PRIMARY KEY,
    `username` VARCHAR(50) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'student',
    `department` VARCHAR(100),
    `email` VARCHAR(100) UNIQUE,
    `phone` VARCHAR(20),
    `avatar` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_active` BOOLEAN DEFAULT TRUE
); 

-- 插入科目数据的SQL语句
-- 这些语句可以单独执行来添加科目

-- 插入科目数据
INSERT INTO subject (subject_id, subject_name, subject_code, description) VALUES
('SUB001', '计算机基础', 'COMP001', '计算机基础知识，包括计算机组成、操作系统等'),
('SUB002', 'Java程序设计', 'JAVA001', 'Java语言基础语法、面向对象编程等'),
('SUB003', '数据结构', 'DS001', '线性表、栈、队列、树、图等数据结构'),
('SUB004', '数据库原理', 'DB001', '数据库设计、SQL语言、事务管理等'),
('SUB005', '计算机网络', 'NET001', '网络协议、网络架构、网络安全等'),
('SUB006', '软件工程', 'SE001', '软件开发流程、需求分析、设计模式等'),
('SUB007', '操作系统', 'OS001', '进程管理、内存管理、文件系统等'),
('SUB008', '算法设计', 'ALG001', '算法分析与设计、排序算法、搜索算法等');

-- 如果需要添加更多科目，可以使用以下格式：
-- INSERT INTO subject (subject_id, subject_name, subject_code, description) VALUES
-- ('SUB009', '新科目名称', 'NEW001', '新科目描述'); 