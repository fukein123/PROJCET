-- MySQL 8.0 - CVS 数据库初始化脚本
-- charset / collation 统一为 utf8mb4

CREATE DATABASE IF NOT EXISTS `CVS`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `CVS`;

-- 用户表（管理员/志愿者统一）
CREATE TABLE IF NOT EXISTS `cvs_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `email` VARCHAR(128) NULL,
  `phone` VARCHAR(32) NULL,
  `avatar_url` VARCHAR(512) NULL,
  `gender` TINYINT NULL COMMENT '0未知 1男 2女',
  `role` VARCHAR(32) NOT NULL COMMENT 'ADMIN / COMMUNITY_ADMIN / VOLUNTEER',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 活动分类
CREATE TABLE IF NOT EXISTS `cvs_activity_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 志愿活动
CREATE TABLE IF NOT EXISTS `cvs_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content` TEXT NULL,
  `address` VARCHAR(255) NOT NULL,
  `lat` DECIMAL(10,7) NULL,
  `lng` DECIMAL(10,7) NULL,
  `start_time` DATETIME(3) NOT NULL,
  `end_time` DATETIME(3) NOT NULL,
  `status` VARCHAR(32) NOT NULL COMMENT 'DRAFT / PUBLISHED / IN_PROGRESS / ENDED / CANCELLED',
  `target_people` INT NOT NULL DEFAULT 0,
  `signed_people` INT NOT NULL DEFAULT 0,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_activity_category` (`category_id`),
  KEY `idx_activity_status` (`status`),
  KEY `idx_activity_time` (`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 活动报名申请
CREATE TABLE IF NOT EXISTS `cvs_activity_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` VARCHAR(32) NOT NULL COMMENT 'PENDING / APPROVED / REJECTED',
  `reject_reason` VARCHAR(255) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_activity_user` (`activity_id`,`user_id`),
  KEY `idx_apply_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 活动打卡（签到/签退）
CREATE TABLE IF NOT EXISTS `cvs_activity_clock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `sign_in_time` DATETIME(3) NULL,
  `sign_out_time` DATETIME(3) NULL,
  `sign_in_lat` DECIMAL(10,7) NULL,
  `sign_in_lng` DECIMAL(10,7) NULL,
  `sign_out_lat` DECIMAL(10,7) NULL,
  `sign_out_lng` DECIMAL(10,7) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_clock_activity_user` (`activity_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 信息动态
CREATE TABLE IF NOT EXISTS `cvs_news` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `content` TEXT NOT NULL,
  `views` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(32) NOT NULL COMMENT 'DRAFT / PUBLISHED',
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_news_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 公告
CREATE TABLE IF NOT EXISTS `cvs_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `content` TEXT NOT NULL,
  `status` VARCHAR(32) NOT NULL COMMENT 'DRAFT / PUBLISHED',
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_notice_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 轮播图（可关联活动）
CREATE TABLE IF NOT EXISTS `cvs_banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NULL,
  `image_url` VARCHAR(512) NOT NULL,
  `activity_id` BIGINT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_banner_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 论坛帖子分类
CREATE TABLE IF NOT EXISTS `cvs_forum_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 帖子（需审核）
CREATE TABLE IF NOT EXISTS `cvs_forum_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content` TEXT NOT NULL,
  `views` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(32) NOT NULL COMMENT 'PENDING / APPROVED / REJECTED',
  `reject_reason` VARCHAR(255) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_post_status` (`status`),
  KEY `idx_post_category` (`category_id`),
  KEY `idx_post_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 评论（活动/帖子）
CREATE TABLE IF NOT EXISTS `cvs_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(16) NOT NULL COMMENT 'ACTIVITY / POST',
  `target_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_comment_target` (`type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 收藏（活动）
CREATE TABLE IF NOT EXISTS `cvs_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `activity_id` BIGINT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav_user_activity` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 兑换订单
CREATE TABLE IF NOT EXISTS `cvs_exchange_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `item_name` VARCHAR(128) NOT NULL,
  `points_cost` INT NOT NULL,
  `status` VARCHAR(32) NOT NULL COMMENT 'CREATED / PAID / CANCELLED / FINISHED',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 积分流水
CREATE TABLE IF NOT EXISTS `cvs_points_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `source` VARCHAR(64) NOT NULL,
  `points` INT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_points_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 初始化一个管理员账号：admin / Admin@123456（仅用于开发环境示例）
-- 注意：这里的密码哈希是占位，真正的哈希会在后端初始化/注册时生成
INSERT INTO `cvs_user` (`username`, `password_hash`, `email`, `role`, `status`)
VALUES ('admin', '$2a$10$PLACEHOLDER_BCRYPT_HASH', 'admin@example.com', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE `role` = VALUES(`role`), `status` = VALUES(`status`);

