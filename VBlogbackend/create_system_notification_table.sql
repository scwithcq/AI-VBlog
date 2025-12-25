-- ========================================
-- 系统互动通知表创建脚本
-- 创建时间：2025-12-24
-- 说明：用于记录用户的点赞、评论、@提及、关注等互动行为
-- ========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 系统互动通知表 (tb_system_notification)
-- ========================================
DROP TABLE IF EXISTS `tb_system_notification`;
CREATE TABLE `tb_system_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `recipient_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送者用户ID（触发通知的用户）',
  `sender_username` varchar(50) DEFAULT NULL COMMENT '发送者用户名（冗余字段，避免关联查询）',
  `sender_avatar` varchar(255) DEFAULT NULL COMMENT '发送者头像（冗余字段）',
  `type` varchar(20) NOT NULL COMMENT '通知类型：LIKE(点赞)、COMMENT(评论)、MENTION(提及)、FOLLOW(关注)、REPOST(转发)',
  `post_id` bigint(20) DEFAULT NULL COMMENT '相关帖子ID',
  `comment_id` bigint(20) DEFAULT NULL COMMENT '相关评论ID',
  `content` varchar(500) DEFAULT NULL COMMENT '通知内容文本',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_id` (`recipient_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_recipient_read` (`recipient_id`, `is_read`),
  KEY `idx_recipient_time` (`recipient_id`, `create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统互动通知表';

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 表创建完成
-- ========================================

-- 使用说明：
-- 1. 该表用于存储系统互动通知，与好友聊天通知（tb_notification）分开管理
-- 2. 通知类型包括：LIKE(点赞)、COMMENT(评论)、MENTION(提及)、FOLLOW(关注)、REPOST(转发)
-- 3. sender_username 和 sender_avatar 是冗余字段，用于提高查询效率，避免每次都关联查询用户表
-- 4. 建议定期清理已读且超过一定天数的通知，可使用 DELETE FROM tb_system_notification WHERE is_read = 1 AND create_time < DATE_SUB(NOW(), INTERVAL 30 DAY)
