-- --------------------------------------------------------
-- UNINEST Community Posts Feature
-- Run this script to add community posts/highlights tables
-- --------------------------------------------------------

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

-- --------------------------------------------------------
-- Table: community_posts
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `community_posts` (
  `post_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `community_id` INT NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` ENUM('active', 'hidden', 'deleted') DEFAULT 'active',
  PRIMARY KEY (`post_id`),
  INDEX `idx_post_user` (`user_id`),
  INDEX `idx_post_community` (`community_id`),
  INDEX `idx_post_created` (`created_at` DESC),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`community_id`) REFERENCES `communities`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: post_likes
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_likes` (
  `like_id` INT NOT NULL AUTO_INCREMENT,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `unique_post_user_like` (`post_id`, `user_id`),
  INDEX `idx_like_post` (`post_id`),
  INDEX `idx_like_user` (`user_id`),
  FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`post_id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: post_comments
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_comments` (
  `comment_id` INT NOT NULL AUTO_INCREMENT,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` ENUM('active', 'hidden', 'deleted') DEFAULT 'active',
  PRIMARY KEY (`comment_id`),
  INDEX `idx_comment_post` (`post_id`),
  INDEX `idx_comment_user` (`user_id`),
  INDEX `idx_comment_created` (`created_at` DESC),
  FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`post_id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Demo Data
-- --------------------------------------------------------
UPDATE `users` SET `name` = 'Tharindu Fernando' WHERE `id` = 22;
UPDATE `users` SET `name` = 'Nuwan Silva' WHERE `id` = 23;
UPDATE `users` SET `name` = 'Hashen Udara' WHERE `id` = 24;

INSERT INTO `community_posts` (`user_id`, `community_id`, `content`, `created_at`) VALUES
(22, 1, 'Just finished the Data Structures quiz! The questions on binary trees were really helpful for understanding the concepts. Thanks to everyone who contributed! 🎉', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(23, 1, 'Looking for study partners for the upcoming Database exam. Anyone interested in forming a study group? Let''s ace this together! 📚', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(24, 1, 'Uploaded new notes on React Hooks! Check out the Resources section. Hope it helps with your assignments. Feel free to share your feedback! 💻', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `post_likes` (`post_id`, `user_id`) VALUES
(1, 23), (1, 24), (1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30),
(1, 31), (1, 32), (1, 33), (1, 34), (1, 35), (1, 36), (1, 37), (1, 38),
(1, 39), (1, 40), (1, 41), (1, 42), (1, 43), (1, 44), (1, 45), (1, 46),
(2, 22), (2, 24), (2, 25), (2, 26), (2, 27), (2, 28), (2, 29), (2, 30),
(2, 31), (2, 32), (2, 33), (2, 34), (2, 35), (2, 36), (2, 37), (2, 38),
(2, 39), (2, 40),
(3, 22), (3, 23), (3, 25), (3, 26), (3, 27), (3, 28), (3, 29), (3, 30),
(3, 31), (3, 32), (3, 33), (3, 34), (3, 35), (3, 36), (3, 37), (3, 38),
(3, 39), (3, 40), (3, 41), (3, 42), (3, 43), (3, 44), (3, 45), (3, 46),
(3, 47), (3, 48), (3, 49), (3, 50), (3, 51), (3, 52), (3, 53), (3, 54),
(3, 55), (3, 56), (3, 57);

INSERT INTO `post_comments` (`post_id`, `user_id`, `content`, `created_at`) VALUES
(1, 23, 'Great job! Binary trees can be tricky.', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 24, 'Which resources did you use for practice?', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(1, 25, 'Congrats! Keep it up!', DATE_SUB(NOW(), INTERVAL 100 MINUTE)),
(1, 26, 'The recursion questions were tough!', DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
(1, 27, 'Thanks for sharing your experience!', DATE_SUB(NOW(), INTERVAL 115 MINUTE)),
(1, 28, 'I found the AVL tree questions helpful.', DATE_SUB(NOW(), INTERVAL 118 MINUTE)),
(1, 29, 'Good luck for the finals!', DATE_SUB(NOW(), INTERVAL 119 MINUTE)),
(1, 30, 'Very motivating!', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(2, 22, 'I''m interested! Count me in!', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 24, 'Me too! When are you planning to meet?', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 25, 'Let''s create a WhatsApp group!', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 26, 'I can share my notes.', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 27, 'The normalization topic is confusing.', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 28, 'I can help with SQL queries!', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 29, 'Same here, need help with joins.', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 30, 'Library study session?', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 31, 'I prefer online study sessions.', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 32, 'Count me in for the group!', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 33, 'Let''s schedule for this weekend.', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 34, 'Great initiative!', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(3, 22, 'Thanks for sharing! Very helpful.', DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(3, 23, 'The useState explanation was clear!', DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(3, 25, 'Can you add useEffect examples?', DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(3, 26, 'Saved this for later reference.', DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(3, 27, 'Great notes! Well organized.', DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(3, 28, 'This helped me with my assignment!', DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(3, 29, 'Appreciate the effort!', DATE_SUB(NOW(), INTERVAL 22 HOUR)),
(3, 30, 'Can you cover custom hooks next?', DATE_SUB(NOW(), INTERVAL 22 HOUR)),
(3, 31, 'The examples are practical!', DATE_SUB(NOW(), INTERVAL 22 HOUR)),
(3, 32, 'Best React notes I''ve seen!', DATE_SUB(NOW(), INTERVAL 22 HOUR)),
(3, 33, 'Thanks, this clarified my doubts.', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(3, 34, 'Shared with my friends!', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(3, 35, 'More like this please!', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(3, 36, 'The code snippets are useful.', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(3, 37, 'Following for more updates!', DATE_SUB(NOW(), INTERVAL 24 HOUR));
