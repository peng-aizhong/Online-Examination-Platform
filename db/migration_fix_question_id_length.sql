-- 数据库迁移脚本：修复question_id字段长度问题
-- 执行此脚本来更新exam_session_answer表的question_id字段长度

USE exam_system;

-- 修改exam_session_answer表的question_id字段长度
ALTER TABLE exam_session_answer MODIFY COLUMN question_id VARCHAR(20);

-- 验证修改结果
DESCRIBE exam_session_answer;

-- 显示修改后的表结构信息
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'exam_system' 
AND TABLE_NAME = 'exam_session_answer' 
AND COLUMN_NAME = 'question_id';
