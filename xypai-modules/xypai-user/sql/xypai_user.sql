-- ========================================
-- XiangYuPai User Service - Complete Database Script
-- ========================================
-- Database: xypai_user
-- Version: 1.0.1
-- Created: 2025-11-14
-- Updated: 2025-11-18
-- Description: Complete database setup with fresh schema
--
-- This script will:
-- 1. DROP existing xypai_user database (⚠️ WARNING: All data will be lost!)
-- 2. CREATE new xypai_user database
-- 3. CREATE all tables with correct schema
-- 4. INSERT sample test data
--
-- Usage:
--   mysql -u root -p < xypai_user.sql
-- ========================================

-- ========================================
-- Step 1: Drop and Recreate Database
-- ========================================
DROP DATABASE IF EXISTS `xypai_user`;
CREATE DATABASE `xypai_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `xypai_user`;

-- ========================================
-- Step 2: Create All Tables
-- ========================================

-- ========================================
-- 1. users - 用户基本信息表
-- ========================================
CREATE TABLE `users` (
    -- Primary Key
    `user_id`           BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',

    -- Authentication Info
    `mobile`            VARCHAR(20)     NOT NULL COMMENT '手机号',
    `country_code`      VARCHAR(10)     NOT NULL DEFAULT '+86' COMMENT '国家区号',

    -- Basic Info (from frontend docs)
    `nickname`          VARCHAR(50)     NOT NULL COMMENT '昵称（2-20字符）',
    `avatar`            VARCHAR(500)    DEFAULT NULL COMMENT '头像URL',
    `gender`            VARCHAR(10)     DEFAULT NULL COMMENT '性别: male, female, other',
    `birthday`          DATE            DEFAULT NULL COMMENT '生日',
    `residence`         VARCHAR(200)    DEFAULT NULL COMMENT '居住地（省市区）',
    `height`            INT(11)         DEFAULT NULL COMMENT '身高（cm, 100-250）',
    `weight`            INT(11)         DEFAULT NULL COMMENT '体重（kg, 30-200）',
    `occupation`        VARCHAR(100)    DEFAULT NULL COMMENT '职业（1-30字符）',
    `wechat`            VARCHAR(50)     DEFAULT NULL COMMENT '微信号（6-20字符）',
    `bio`               VARCHAR(500)    DEFAULT NULL COMMENT '个性签名（0-200字符）',

    -- Location (for nearby queries)
    `location`          POINT SRID 4326 DEFAULT NULL COMMENT '地理位置（空间索引）',
    `latitude`          DECIMAL(10,7)   DEFAULT NULL COMMENT '纬度',
    `longitude`         DECIMAL(10,7)   DEFAULT NULL COMMENT '经度',

    -- Privacy Settings
    `privacy_profile`   TINYINT(1)      DEFAULT 1 COMMENT '资料可见性（1-公开，2-仅粉丝，3-私密）',

    -- Status
    `is_online`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否在线（0-否，1-是）',
    `last_login_at`     DATETIME        DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`     VARCHAR(50)     DEFAULT NULL COMMENT '最后登录IP',

    -- Audit Fields (MANDATORY)
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除（0-未删除，1-已删除）',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_mobile_country` (`mobile`, `country_code`, `deleted`),
    KEY `idx_nickname` (`nickname`),
    KEY `idx_wechat` (`wechat`),
    KEY `idx_latitude_longitude` (`latitude`, `longitude`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';


-- ========================================
-- 2. user_stats - 用户统计表
-- ========================================
CREATE TABLE `user_stats` (
    -- Primary Key
    `stat_id`           BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '统计ID（主键）',
    `user_id`           BIGINT(20)      NOT NULL COMMENT '用户ID',

    -- Counts
    `following_count`   INT(11)         NOT NULL DEFAULT 0 COMMENT '关注数',
    `fans_count`        INT(11)         NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `likes_count`       INT(11)         NOT NULL DEFAULT 0 COMMENT '获赞总数',
    `posts_count`       INT(11)         NOT NULL DEFAULT 0 COMMENT '动态数',
    `favorites_count`   INT(11)         NOT NULL DEFAULT 0 COMMENT '收藏数',
    `moments_count`     INT(11)         NOT NULL DEFAULT 0 COMMENT '动态数(moments)',
    `collections_count` INT(11)         NOT NULL DEFAULT 0 COMMENT '收藏数(collections)',
    `skills_count`      INT(11)         NOT NULL DEFAULT 0 COMMENT '技能数',
    `orders_count`      INT(11)         NOT NULL DEFAULT 0 COMMENT '订单数',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';


-- ========================================
-- 3. user_relations - 用户关系表（关注/粉丝）
-- ========================================
CREATE TABLE `user_relations` (
    -- Primary Key
    `relation_id`       BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '关系ID（主键）',

    -- Relation
    `follower_id`       BIGINT(20)      NOT NULL COMMENT '关注者ID（谁关注）',
    `following_id`      BIGINT(20)      NOT NULL COMMENT '被关注者ID（被谁关注）',

    -- Status
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'active' COMMENT '关系状态: active, blocked',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`relation_id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`, `deleted`),
    KEY `idx_follower_id` (`follower_id`),
    KEY `idx_following_id` (`following_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关系表';


-- ========================================
-- 4. user_blacklist - 用户黑名单表
-- ========================================
CREATE TABLE `user_blacklist` (
    -- Primary Key
    `blacklist_id`      BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '黑名单ID（主键）',

    -- Relation
    `user_id`           BIGINT(20)      NOT NULL COMMENT '用户ID（谁拉黑）',
    `blocked_user_id`   BIGINT(20)      NOT NULL COMMENT '被拉黑用户ID',

    -- Reason
    `reason`            VARCHAR(500)    DEFAULT NULL COMMENT '拉黑原因',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`blacklist_id`),
    UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`, `deleted`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_blocked_user_id` (`blocked_user_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户黑名单表';


-- ========================================
-- 5. user_reports - 用户举报表
-- ========================================
CREATE TABLE `user_reports` (
    -- Primary Key
    `report_id`         BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '举报ID（主键）',

    -- Relation
    `reporter_id`       BIGINT(20)      NOT NULL COMMENT '举报人ID',
    `reported_user_id`  BIGINT(20)      NOT NULL COMMENT '被举报用户ID',

    -- Report Details
    `reason`            VARCHAR(50)     NOT NULL COMMENT '举报理由',
    `description`       VARCHAR(1000)   DEFAULT NULL COMMENT '详细描述（0-500字符）',
    `evidence`          TEXT            DEFAULT NULL COMMENT '证据图片URL列表（JSON数组）',

    -- Status
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT '处理状态: pending, reviewing, resolved, rejected',
    `reviewed_at`       DATETIME        DEFAULT NULL COMMENT '审核时间',
    `reviewer_id`       BIGINT(20)      DEFAULT NULL COMMENT '审核人ID',
    `review_result`     VARCHAR(500)    DEFAULT NULL COMMENT '审核结果',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`report_id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_reported_user_id` (`reported_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户举报表';


-- ========================================
-- 6. skills - 技能表（线上 + 线下）
-- ========================================
CREATE TABLE `skills` (
    -- Primary Key
    `skill_id`          BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '技能ID（主键）',

    -- Owner
    `user_id`           BIGINT(20)      NOT NULL COMMENT '用户ID（技能拥有者）',

    -- Basic Info
    `skill_name`        VARCHAR(100)    NOT NULL COMMENT '技能名称（2-50字符）',
    `skill_type`        VARCHAR(20)     NOT NULL COMMENT '技能类型: online, offline',
    `cover_image`       VARCHAR(500)    NOT NULL COMMENT '封面图URL',
    `description`       VARCHAR(1000)   NOT NULL COMMENT '技能介绍（10-500字符）',

    -- Pricing
    `price`             DECIMAL(10,2)   NOT NULL COMMENT '价格（元）',
    `price_unit`        VARCHAR(20)     NOT NULL COMMENT '价格单位: 局, 小时',

    -- Status
    `is_online`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否上架（0-下架，1-上架）',

    -- Statistics
    `rating`            DECIMAL(3,2)    NOT NULL DEFAULT 0.00 COMMENT '评分（0-5.00）',
    `review_count`      INT(11)         NOT NULL DEFAULT 0 COMMENT '评价数量',
    `order_count`       INT(11)         NOT NULL DEFAULT 0 COMMENT '订单数量',

    -- Online Skill Specific Fields
    `game_name`         VARCHAR(100)    DEFAULT NULL COMMENT '游戏名称（线上技能专用）',
    `game_rank`         VARCHAR(50)     DEFAULT NULL COMMENT '游戏段位（线上技能专用）',
    `service_hours`     DECIMAL(4,2)    DEFAULT NULL COMMENT '服务时长（小时/局，线上技能专用）',

    -- Offline Skill Specific Fields (location required for offline skills, enforced in application)
    `service_type`      VARCHAR(100)    DEFAULT NULL COMMENT '服务类型（线下技能专用）',
    `service_location`  VARCHAR(500)    DEFAULT NULL COMMENT '服务地点（线下技能专用）',
    `location`          POINT SRID 4326 DEFAULT NULL COMMENT '地理位置（线下技能时必填，应用层校验）',
    `latitude`          DECIMAL(10,7)   DEFAULT NULL COMMENT '纬度（线下技能时必填）',
    `longitude`         DECIMAL(10,7)   DEFAULT NULL COMMENT '经度（线下技能时必填）',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`skill_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_skill_type` (`skill_type`),
    KEY `idx_is_online` (`is_online`),
    KEY `idx_game_name` (`game_name`),
    KEY `idx_service_type` (`service_type`),
    KEY `idx_latitude_longitude` (`latitude`, `longitude`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能表（线上+线下）';


-- ========================================
-- 7. skill_images - 技能展示图片表
-- ========================================
CREATE TABLE `skill_images` (
    -- Primary Key
    `image_id`          BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '图片ID（主键）',

    -- Skill Reference
    `skill_id`          BIGINT(20)      NOT NULL COMMENT '技能ID',

    -- Image Info
    `image_url`         VARCHAR(500)    NOT NULL COMMENT '图片URL',
    `sort_order`        INT(11)         NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`image_id`),
    KEY `idx_skill_id` (`skill_id`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能展示图片表';


-- ========================================
-- 8. skill_promises - 技能服务承诺表
-- ========================================
CREATE TABLE `skill_promises` (
    -- Primary Key
    `promise_id`        BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '承诺ID（主键）',

    -- Skill Reference
    `skill_id`          BIGINT(20)      NOT NULL COMMENT '技能ID',

    -- Promise Content
    `promise_text`      VARCHAR(200)    NOT NULL COMMENT '承诺内容',
    `sort_order`        INT(11)         NOT NULL DEFAULT 0 COMMENT '排序号',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`promise_id`),
    KEY `idx_skill_id` (`skill_id`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能服务承诺表';


-- ========================================
-- 9. skill_available_times - 技能可用时间表（线下技能专用）
-- ========================================
CREATE TABLE `skill_available_times` (
    -- Primary Key
    `time_id`           BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '时间ID（主键）',

    -- Skill Reference
    `skill_id`          BIGINT(20)      NOT NULL COMMENT '技能ID',

    -- Time Info
    `day_of_week`       TINYINT(1)      NOT NULL COMMENT '星期几（0-周日，1-周一，...，6-周六）',
    `start_time`        TIME            NOT NULL COMMENT '开始时间（HH:mm）',
    `end_time`          TIME            NOT NULL COMMENT '结束时间（HH:mm）',
    `enabled`           TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用（0-否，1-是）',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`time_id`),
    KEY `idx_skill_id` (`skill_id`),
    KEY `idx_day_of_week` (`day_of_week`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能可用时间表（线下技能专用）';


-- ========================================
-- Foreign Key Constraints (Optional - can be added if needed)
-- ========================================
-- ALTER TABLE `user_stats` ADD CONSTRAINT `fk_user_stats_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `user_relations` ADD CONSTRAINT `fk_user_relations_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `user_relations` ADD CONSTRAINT `fk_user_relations_following` FOREIGN KEY (`following_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `user_blacklist` ADD CONSTRAINT `fk_user_blacklist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `user_blacklist` ADD CONSTRAINT `fk_user_blacklist_blocked_user` FOREIGN KEY (`blocked_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `user_reports` ADD CONSTRAINT `fk_user_reports_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `user_reports` ADD CONSTRAINT `fk_user_reports_reported_user` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `skills` ADD CONSTRAINT `fk_skills_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `skill_images` ADD CONSTRAINT `fk_skill_images_skill_id` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`skill_id`) ON DELETE CASCADE;
-- ALTER TABLE `skill_promises` ADD CONSTRAINT `fk_skill_promises_skill_id` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`skill_id`) ON DELETE CASCADE;
-- ALTER TABLE `skill_available_times` ADD CONSTRAINT `fk_skill_available_times_skill_id` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`skill_id`) ON DELETE CASCADE;


-- ========================================
-- Step 3: Insert Initial Test Data
-- ========================================

-- Insert test user
INSERT INTO `users` (`user_id`, `mobile`, `country_code`, `nickname`, `avatar`, `gender`, `bio`) VALUES
(1, '13800138000', '+86', '测试用户', 'https://cdn.example.com/avatar/default.png', 'male', '这是一个测试用户');

-- Insert user stats for test user
INSERT INTO `user_stats` (`user_id`, `following_count`, `fans_count`, `likes_count`) VALUES
(1, 0, 0, 0);

-- ========================================
-- Step 4: Verification
-- ========================================
SELECT '✅ Database created successfully!' AS status;
SELECT 'Database: xypai_user' AS info;
SELECT COUNT(*) AS table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'xypai_user';

-- Show all tables
SHOW TABLES;

-- ========================================
-- End of Schema
-- ========================================
