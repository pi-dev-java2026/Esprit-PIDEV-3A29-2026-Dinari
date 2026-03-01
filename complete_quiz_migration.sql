-- Complete Quiz Table Migration
-- Run this to ensure all required columns exist

-- Add question column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
AND table_name = 'quiz' 
AND column_name = 'question';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE quiz ADD COLUMN question TEXT NULL AFTER titre', 
    'SELECT "question column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add is_exam_mode column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
AND table_name = 'quiz' 
AND column_name = 'is_exam_mode';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE quiz ADD COLUMN is_exam_mode TINYINT(1) DEFAULT 0 AFTER date_creation', 
    'SELECT "is_exam_mode column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add time_limit column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
AND table_name = 'quiz' 
AND column_name = 'time_limit';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE quiz ADD COLUMN time_limit INT DEFAULT 0 AFTER is_exam_mode', 
    'SELECT "time_limit column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing quizzes to have question = titre if question is NULL
UPDATE quiz SET question = titre WHERE question IS NULL OR question = '';

-- Verify the final structure
DESCRIBE quiz;

-- Show sample data
SELECT id_quiz, titre, question, is_exam_mode, time_limit, liste_reponse 
FROM quiz 
ORDER BY id_quiz DESC 
LIMIT 5;