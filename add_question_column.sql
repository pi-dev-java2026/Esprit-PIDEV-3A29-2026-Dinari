-- Add question column to quiz table
ALTER TABLE quiz 
ADD COLUMN question TEXT NULL AFTER titre;

-- Update existing quizzes to use title as question temporarily
UPDATE quiz SET question = titre WHERE question IS NULL;

-- Verify the change
DESCRIBE quiz;
SELECT id_quiz, titre, question, liste_reponse, is_exam_mode, time_limit FROM quiz LIMIT 3;