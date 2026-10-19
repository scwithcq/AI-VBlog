-- ========================================
-- V-Blog 完整数据库表创建脚本
-- 创建时间：2025-12-24
-- 说明：包含项目中所有需要的数据库表
-- ========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 用户表 (tb_user)
-- ========================================
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `gender` tinyint(4) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `location` varchar(100) DEFAULT NULL COMMENT '地区',
  `website` varchar(200) DEFAULT NULL COMMENT '个人网站',
  `avatar` varchar(255) DEFAULT '/default-avatar.jpg' COMMENT '头像URL',
  `background` varchar(255) DEFAULT NULL COMMENT '背景图URL',
  `bio` text COMMENT '个人简介',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 帖子表 (tb_post)
-- ========================================
DROP TABLE IF EXISTS `tb_post`;
CREATE TABLE `tb_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（作者）',
  `content` text COMMENT '帖子内容',
  `images` text COMMENT '图片URL数组（JSON格式）',
  `video_url` varchar(500) DEFAULT NULL COMMENT '视频URL',
  `source_id` bigint(20) DEFAULT NULL COMMENT '源帖子ID（转发时使用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- ========================================
-- 3. 评论表 (tb_comment)
-- ========================================
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（评论者）',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID（回复时使用，顶级评论为NULL）',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ========================================
-- 4. 评论点赞表 (tb_comment_like)
-- ========================================
DROP TABLE IF EXISTS `tb_comment_like`;
CREATE TABLE `tb_comment_like` (
  `like_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `uk_comment_like` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- ========================================
-- 5. 帖子点赞表 (tb_post_like)
-- ========================================
DROP TABLE IF EXISTS `tb_post_like`;
CREATE TABLE `tb_post_like` (
  `like_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `uk_post_like` (`post_id`, `user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- 收藏与点赞是两种独立操作
DROP TABLE IF EXISTS `tb_post_favorite`;
CREATE TABLE `tb_post_favorite` (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `uk_post_favorite` (`post_id`, `user_id`),
  KEY `idx_favorite_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ========================================
-- 6. 关注表 (tb_follow)
-- ========================================
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow` (
  `follow_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `follower_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `followee_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`follow_id`),
  UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_followee_id` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- ========================================
-- 7. 话题表 (tb_topic)
-- ========================================
DROP TABLE IF EXISTS `tb_topic`;
CREATE TABLE `tb_topic` (
  `topic_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` varchar(100) NOT NULL COMMENT '话题名称',
  `heat` decimal(10, 2) DEFAULT 0.00 COMMENT '话题热度值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`topic_id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_heat` (`heat` DESC),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- ========================================
-- 8. 通知表 (tb_notification)
-- ========================================
DROP TABLE IF EXISTS `tb_notification`;
CREATE TABLE `tb_notification` (
  `notification_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `recipient_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `type` tinyint(4) NOT NULL COMMENT '通知类型：1-关注，2-点赞，3-评论，4-系统通知',
  `post_id` bigint(20) DEFAULT NULL COMMENT '关联的帖子ID',
  `comment_id` bigint(20) DEFAULT NULL COMMENT '关联的评论ID',
  `followee_id` bigint(20) DEFAULT NULL COMMENT '关联的被关注者ID',
  `content` varchar(500) DEFAULT NULL COMMENT '通知内容',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`notification_id`),
  KEY `idx_recipient_id` (`recipient_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ========================================
-- 9. 好友关系表 (tb_friendship)
-- ========================================
DROP TABLE IF EXISTS `tb_friendship`;
CREATE TABLE `tb_friendship` (
  `friendship_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '好友关系ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `friend_id` bigint(20) NOT NULL COMMENT '好友ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '好友状态：1-正常，2-已删除',
  `remark` varchar(50) DEFAULT NULL COMMENT '好友备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`friendship_id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- ========================================
-- 10. 好友申请表 (tb_friend_request)
-- ========================================
DROP TABLE IF EXISTS `tb_friend_request`;
CREATE TABLE `tb_friend_request` (
  `request_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '发起者用户ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `message` varchar(200) DEFAULT NULL COMMENT '申请留言',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '申请状态：0-待处理，1-已同意，2-已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`request_id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- ========================================
-- 11. 消息记录表 (tb_message)
-- ========================================
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message` (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` varchar(50) NOT NULL COMMENT '会话ID（两个用户ID组合）',
  `from_user_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-语音，4-视频，5-文件',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '消息状态：0-未读，1-已读，2-已撤回',
  `is_deleted_by_sender` tinyint(1) DEFAULT 0 COMMENT '发送者是否删除：0-否，1-是',
  `is_deleted_by_receiver` tinyint(1) DEFAULT 0 COMMENT '接收者是否删除：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_conversation` (`conversation_id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

-- ========================================
-- 12. 会话列表表 (tb_conversation)
-- ========================================
DROP TABLE IF EXISTS `tb_conversation`;
CREATE TABLE `tb_conversation` (
  `conversation_id` varchar(50) NOT NULL COMMENT '会话ID',
  `user1_id` bigint(20) NOT NULL COMMENT '用户1的ID（较小的ID）',
  `user2_id` bigint(20) NOT NULL COMMENT '用户2的ID（较大的ID）',
  `last_message_id` bigint(20) DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_content` varchar(200) DEFAULT NULL COMMENT '最后一条消息内容摘要',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `user1_unread_count` int(11) DEFAULT 0 COMMENT '用户1未读消息数',
  `user2_unread_count` int(11) DEFAULT 0 COMMENT '用户2未读消息数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`conversation_id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话列表表';


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


-- ========================================
-- 插入默认测试数据（可选）
-- ========================================

-- 插入测试用户（密码为 "123456" 的加密值，需根据实际加密算法调整）
-- INSERT INTO `tb_user` (`username`, `password`, `nickname`, `bio`)
-- VALUES ('testuser1', '$2a$10$encrypted_password_here', '测试用户1', '这是测试用户1的个人简介');

-- INSERT INTO `tb_user` (`username`, `password`, `nickname`, `bio`)
-- VALUES ('testuser2', '$2a$10$encrypted_password_here', '测试用户2', '这是测试用户2的个人简介');

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 脚本执行完成
-- ========================================
