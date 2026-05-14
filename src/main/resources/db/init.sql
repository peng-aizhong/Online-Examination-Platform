-- 创建数据库
CREATE DATABASE IF NOT EXISTS exam_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('admin', 'teacher', 'student') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建科目表
CREATE TABLE IF NOT EXISTS subject (
    subject_id VARCHAR(10) PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建试卷表
CREATE TABLE IF NOT EXISTS paper (
    paper_id VARCHAR(10) PRIMARY KEY,
    paper_name VARCHAR(200) NOT NULL,
    subject_id VARCHAR(10) NOT NULL,
    creator_id VARCHAR(20),
    duration INT NOT NULL,
    total_score INT NOT NULL,
    difficulty_level VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(10) DEFAULT '启用',
    passing_score INT,
    question_count INT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (creator_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建题目表
CREATE TABLE IF NOT EXISTS question (
    id VARCHAR(10) PRIMARY KEY,
    subject_id VARCHAR(10) NOT NULL,
    content TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    answer TEXT,
    explanation TEXT,
    difficulty VARCHAR(20) DEFAULT 'medium',
    knowledge_tag VARCHAR(100),
    status VARCHAR(10) DEFAULT '启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建试卷题目关联表
CREATE TABLE IF NOT EXISTS paper_question (
    paper_id VARCHAR(10),
    question_id VARCHAR(10),
    score INT,
    PRIMARY KEY (paper_id, question_id),
    FOREIGN KEY (paper_id) REFERENCES paper(paper_id),
    FOREIGN KEY (question_id) REFERENCES question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建考试分配表
CREATE TABLE IF NOT EXISTS exam_assignment (
    assignment_id VARCHAR(12) PRIMARY KEY,
    paper_id VARCHAR(10) NOT NULL,
    teacher_id VARCHAR(20) NOT NULL,
    assignment_name VARCHAR(100) NOT NULL,
    exam_start_time TIMESTAMP NOT NULL,
    exam_end_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    max_attempts INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (paper_id) REFERENCES paper(paper_id),
    FOREIGN KEY (teacher_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建学生考试分配表
CREATE TABLE IF NOT EXISTS exam_assignment_student (
    assignment_id VARCHAR(12),
    student_id VARCHAR(20),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (assignment_id, student_id),
    FOREIGN KEY (assignment_id) REFERENCES exam_assignment(assignment_id),
    FOREIGN KEY (student_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建考试会话表
CREATE TABLE IF NOT EXISTS exam_session (
    session_id VARCHAR(12) PRIMARY KEY,
    assignment_id VARCHAR(12) NOT NULL,
    paper_id VARCHAR(10) NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    attempt_number INT NOT NULL DEFAULT 1,
    started_at TIMESTAMP NULL,
    submitted_at TIMESTAMP NULL,
    duration_minutes INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'not_started',
    objective_score DOUBLE DEFAULT 0.0,
    subjective_score DOUBLE DEFAULT 0.0,
    total_score DOUBLE DEFAULT 0.0,
    is_best_score BOOLEAN DEFAULT FALSE,
    auto_submitted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES exam_assignment(assignment_id),
    FOREIGN KEY (paper_id) REFERENCES paper(paper_id),
    FOREIGN KEY (student_id) REFERENCES user(user_id),
    INDEX idx_assignment_student (assignment_id, student_id),
    INDEX idx_student_paper (student_id, paper_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_assignment_student_attempt (assignment_id, student_id, attempt_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 重新创建考试答案表
CREATE TABLE IF NOT EXISTS exam_session_answer (
    session_id VARCHAR(12),
    question_id VARCHAR(10),
    answer_text TEXT,
    score DOUBLE DEFAULT 0.0,
    feedback TEXT,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, question_id),
    FOREIGN KEY (session_id) REFERENCES exam_session(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id),
    INDEX idx_session_id (session_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建学习资源表
CREATE TABLE IF NOT EXISTS learning_resource (
    resource_id VARCHAR(12) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    resource_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    subject_id VARCHAR(10),
    uploader_id VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (uploader_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建触发器 - 自动更新最佳成绩
DELIMITER $$

CREATE TRIGGER update_best_score_after_insert
AFTER INSERT ON exam_session
FOR EACH ROW
BEGIN
    -- 只有当考试提交时才更新最佳成绩
    IF NEW.status = 'submitted' AND NEW.total_score IS NOT NULL THEN
        -- 将当前记录设为最佳成绩
        UPDATE exam_session 
        SET is_best_score = TRUE 
        WHERE session_id = NEW.session_id;
        
        -- 将同一学生同一考试的其他记录设为非最佳成绩
        UPDATE exam_session 
        SET is_best_score = FALSE 
        WHERE assignment_id = NEW.assignment_id 
        AND student_id = NEW.student_id 
        AND session_id != NEW.session_id;
    END IF;
END$$

CREATE TRIGGER update_best_score_after_update
AFTER UPDATE ON exam_session
FOR EACH ROW
BEGIN
    -- 只有当考试提交时才更新最佳成绩
    IF NEW.status = 'submitted' AND NEW.total_score IS NOT NULL THEN
        -- 将当前记录设为最佳成绩
        UPDATE exam_session 
        SET is_best_score = TRUE 
        WHERE session_id = NEW.session_id;
        
        -- 将同一学生同一考试的其他记录设为非最佳成绩
        UPDATE exam_session 
        SET is_best_score = FALSE 
        WHERE assignment_id = NEW.assignment_id 
        AND student_id = NEW.student_id 
        AND session_id != NEW.session_id;
    END IF;
END$$

