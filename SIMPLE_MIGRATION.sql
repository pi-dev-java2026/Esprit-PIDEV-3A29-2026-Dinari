-- SIMPLE QUIZ MIGRATION - Run these commands one by one

-- 1. Add question column
ALTER TABLE quiz ADD COLUMN question TEXT NULL AFTER titre;

-- 2. Add exam mode columns (if not already added)
ALTER TABLE quiz ADD COLUMN is_exam_mode TINYINT(1) DEFAULT 0 AFTER date_creation;
ALTER TABLE quiz ADD COLUMN time_limit INT DEFAULT 0 AFTER is_exam_mode;

-- 3. Update existing quizzes
UPDATE quiz SET question = titre WHERE question IS NULL;
UPDATE quiz SET is_exam_mode = 0, time_limit = 0 WHERE is_exam_mode IS NULL;

-- 4. Verify structure
DESCRIBE quiz;

-- 5. Check data
SELECT id_quiz, titre, question, is_exam_mode, time_limit FROM quiz;