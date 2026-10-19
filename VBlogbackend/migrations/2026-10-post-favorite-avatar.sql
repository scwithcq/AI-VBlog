-- 现有数据库升级脚本。只更新没有头像或使用旧占位头像的账号。
CREATE TABLE IF NOT EXISTS tb_post_favorite (
  favorite_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_favorite (post_id, user_id),
  KEY idx_favorite_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE tb_user
SET avatar = '/default-avatar.jpg'
WHERE avatar IS NULL OR TRIM(avatar) = '' OR avatar = '/uploads/default-avatar.png';
