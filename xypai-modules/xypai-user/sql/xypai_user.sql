-- ========================================
-- XiangYuPai User Service - Complete Database Script
-- ========================================
-- Database: xypai_user
-- Version: 1.0.4
-- Created: 2025-11-14
-- Updated: 2025-12-02
-- Description: Complete database setup with fresh schema
--              Added: user level system (等级系统), verification badges (认证徽章), VIP status
--              Added: wechat unlock tables (微信解锁记录表)
--              Added: skill_config tables (技能配置表，段位配置表) - 对应添加技能页UI文档
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

    -- Level System (等级系统)
    `level`             INT(11)         NOT NULL DEFAULT 1 COMMENT '用户等级（1-青铜，2-白银，3-黄金，4-铂金，5-钻石，6-大师，7-王者）',
    `level_exp`         INT(11)         NOT NULL DEFAULT 0 COMMENT '等级经验值',

    -- Verification & VIP (认证与VIP)
    `is_real_verified`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否实名认证（0-否，1-是）',
    `is_god_verified`   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否大神认证（0-否，1-是）',
    `is_vip`            TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否VIP（0-否，1-是）',
    `vip_expire_time`   DATETIME        DEFAULT NULL COMMENT 'VIP过期时间',

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
    KEY `idx_deleted` (`deleted`),
    KEY `idx_level` (`level`),
    KEY `idx_is_vip` (`is_vip`)
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

    -- Skill Config Reference (关联技能配置表)
    `skill_config_id`   BIGINT(20)      DEFAULT NULL COMMENT '技能配置ID（关联skill_config表）',

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
    `server`            VARCHAR(20)     DEFAULT NULL COMMENT '服务区: QQ区, 微信区（线上技能专用）',
    `service_hours`     DECIMAL(4,2)    DEFAULT NULL COMMENT '服务时长（小时/局，线上技能专用）',

    -- Offline Skill Specific Fields (location required for offline skills, enforced in application)
    `service_type`      VARCHAR(100)    DEFAULT NULL COMMENT '服务类型（线下技能专用）',
    `service_location`  VARCHAR(500)    DEFAULT NULL COMMENT '服务地点（线下技能专用）',
    `activity_time`     DATETIME        DEFAULT NULL COMMENT '活动时间（线下技能预约时间）',
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
    KEY `idx_skill_config_id` (`skill_config_id`),
    KEY `idx_skill_type` (`skill_type`),
    KEY `idx_is_online` (`is_online`),
    KEY `idx_game_name` (`game_name`),
    KEY `idx_server` (`server`),
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
-- 10. wechat_unlocks - 微信解锁记录表
-- ========================================
CREATE TABLE `wechat_unlocks` (
    -- Primary Key
    `unlock_id`         BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '解锁记录ID（主键）',

    -- Relation
    `user_id`           BIGINT(20)      NOT NULL COMMENT '解锁者用户ID（谁解锁的）',
    `target_user_id`    BIGINT(20)      NOT NULL COMMENT '被解锁者用户ID（解锁谁的微信）',

    -- Unlock Info
    `unlock_type`       VARCHAR(20)     NOT NULL DEFAULT 'coins' COMMENT '解锁方式: coins-金币解锁, vip-VIP免费解锁',
    `cost_coins`        INT(11)         NOT NULL DEFAULT 0 COMMENT '消耗金币数（VIP免费解锁时为0）',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`unlock_id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_user_id`, `deleted`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target_user_id` (`target_user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信解锁记录表';


-- ========================================
-- 11. wechat_unlock_config - 微信解锁配置表
-- ========================================
CREATE TABLE `wechat_unlock_config` (
    -- Primary Key
    `config_id`         BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '配置ID（主键）',

    -- Config Key
    `config_key`        VARCHAR(50)     NOT NULL COMMENT '配置键',
    `config_value`      VARCHAR(200)    NOT NULL COMMENT '配置值',
    `config_desc`       VARCHAR(500)    DEFAULT NULL COMMENT '配置描述',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`, `deleted`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信解锁配置表';


-- ========================================
-- 12. skill_config - 技能配置表（预定义技能模板）
-- 对应UI文档: 添加技能页_结构文档.md
-- ========================================
CREATE TABLE `skill_config` (
    -- Primary Key
    `config_id`         BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '配置ID（主键）',

    -- Basic Info
    `name`              VARCHAR(50)     NOT NULL COMMENT '技能名称（王者荣耀、探店等）',
    `icon`              VARCHAR(500)    NOT NULL COMMENT '技能图标URL',
    `skill_type`        VARCHAR(20)     NOT NULL COMMENT '技能类型: online=线上, offline=线下',
    `category`          VARCHAR(50)     DEFAULT NULL COMMENT '分类（游戏、生活服务等）',

    -- Sort & Status
    `sort_order`        INT(11)         NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',
    `status`            TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`config_id`),
    KEY `idx_skill_type` (`skill_type`),
    KEY `idx_status` (`status`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能配置表';


-- ========================================
-- 13. skill_rank_config - 段位配置表（线上技能的段位选项）
-- 对应UI文档: RankPickerModal
-- ========================================
CREATE TABLE `skill_rank_config` (
    -- Primary Key
    `rank_id`           BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '段位ID（主键）',

    -- Reference
    `skill_config_id`   BIGINT(20)      NOT NULL COMMENT '技能配置ID（关联skill_config）',

    -- Rank Info
    `server`            VARCHAR(20)     NOT NULL COMMENT '服务区: qq=QQ区, weixin=微信区, default=通用',
    `rank_name`         VARCHAR(50)     NOT NULL COMMENT '段位名称（永恒钻石、至尊星耀等）',
    `sort_order`        INT(11)         NOT NULL DEFAULT 0 COMMENT '排序序号',

    -- Status
    `status`            TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',

    PRIMARY KEY (`rank_id`),
    KEY `idx_skill_config_id` (`skill_config_id`),
    KEY `idx_server` (`server`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='段位配置表';


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
-- 注意：用户要出现在首页列表，必须满足：
-- 1. users.deleted = 0 (账号正常)
-- 2. 至少有一个 skills.is_online = 1 的技能 (有上架技能)

-- ========================================
-- 3.1 插入测试用户 (5个用户，覆盖不同场景)
-- ========================================
INSERT INTO `users` (`user_id`, `mobile`, `country_code`, `nickname`, `avatar`, `gender`, `birthday`, `residence`, `height`, `weight`, `occupation`, `bio`, `latitude`, `longitude`, `level`, `level_exp`, `is_real_verified`, `is_god_verified`, `is_vip`, `vip_expire_time`, `is_online`, `last_login_at`) VALUES
-- 用户1: 深圳南山 - 男性 - 在线 - 黄金等级 - 实名认证
(1, '13800138001', '+86', '小明同学', 'https://randomuser.me/api/portraits/men/32.jpg', 'male', '1998-05-15', '广东省深圳市南山区', 175, 70, '程序员', '热爱游戏，王者荣耀王者段位', 22.5431, 113.9298, 3, 2500, 1, 0, 0, NULL, 1, NOW()),
-- 用户2: 深圳福田 - 女性 - 在线 - 钻石等级 - 大神认证 - VIP
(2, '13800138002', '+86', '小红姐姐', 'https://randomuser.me/api/portraits/women/44.jpg', 'female', '1996-08-20', '广东省深圳市福田区', 165, 50, '设计师', '陪玩达人，声音好听', 22.5467, 114.0579, 5, 8000, 1, 1, 1, DATE_ADD(NOW(), INTERVAL 180 DAY), 1, NOW()),
-- 用户3: 深圳宝安 - 女性 - 离线 - 铂金等级 - 实名认证 - 大神认证
(3, '13800138003', '+86', '游戏女神', 'https://randomuser.me/api/portraits/women/68.jpg', 'female', '2000-03-10', '广东省深圳市宝安区', 168, 52, '主播', 'LOL钻石选手，期待与你组队', 22.5560, 113.8830, 4, 5200, 1, 1, 0, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
-- 用户4: 深圳龙岗 - 男性 - 在线 - 王者等级 - 全认证 - VIP
(4, '13800138004', '+86', '电竞小王子', 'https://randomuser.me/api/portraits/men/75.jpg', 'male', '1999-11-25', '广东省深圳市龙岗区', 180, 75, '电竞选手', '前职业选手，带你上分', 22.7200, 114.2470, 7, 15000, 1, 1, 1, DATE_ADD(NOW(), INTERVAL 365 DAY), 1, NOW()),
-- 用户5: 深圳罗湖 - 女性 - 在线 - 白银等级 - 新用户
(5, '13800138005', '+86', '甜心小姐姐', 'https://randomuser.me/api/portraits/women/90.jpg', 'female', '1997-07-07', '广东省深圳市罗湖区', 162, 48, '学生', '治愈系声音，聊天解压', 22.5485, 114.1315, 2, 800, 0, 0, 0, NULL, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- ========================================
-- 3.2 插入用户统计数据
-- ========================================
INSERT INTO `user_stats` (`user_id`, `following_count`, `fans_count`, `likes_count`, `skills_count`, `orders_count`) VALUES
(1, 10, 156, 520, 2, 45),
(2, 25, 890, 2100, 1, 120),
(3, 15, 450, 980, 2, 78),
(4, 8, 1200, 3500, 1, 200),
(5, 30, 320, 650, 1, 35);

-- ========================================
-- 3.3 插入技能数据 (关键！is_online=1 才会显示)
-- ========================================
INSERT INTO `skills` (`skill_id`, `user_id`, `skill_name`, `skill_type`, `cover_image`, `description`, `price`, `price_unit`, `is_online`, `rating`, `review_count`, `order_count`, `game_name`, `game_rank`) VALUES
-- 用户1的技能 (2个)
(1, 1, '王者荣耀陪玩', 'online', 'https://cdn.example.com/skill/wzry.png', '王者荣耀王者段位，可带上分、娱乐局', 30.00, '局', 1, 4.85, 120, 45, '王者荣耀', '王者'),
(2, 1, 'LOL陪玩', 'online', 'https://cdn.example.com/skill/lol.png', '英雄联盟钻石段位，擅长打野和中单', 25.00, '局', 1, 4.70, 85, 30, '英雄联盟', '钻石'),
-- 用户2的技能
(3, 2, '语音聊天', 'online', 'https://cdn.example.com/skill/chat.png', '甜美声音陪聊，解忧树洞，倾听你的故事', 50.00, '小时', 1, 4.95, 200, 120, NULL, NULL),
-- 用户3的技能 (2个)
(4, 3, 'LOL上分', 'online', 'https://cdn.example.com/skill/lol2.png', 'LOL钻石选手，专业带上分，不上分退款', 35.00, '局', 1, 4.80, 150, 78, '英雄联盟', '钻石'),
(5, 3, '和平精英陪玩', 'online', 'https://cdn.example.com/skill/pubg.png', '和平精英王牌选手，带你吃鸡', 28.00, '局', 1, 4.65, 60, 25, '和平精英', '王牌'),
-- 用户4的技能
(6, 4, '电竞教学', 'online', 'https://cdn.example.com/skill/teach.png', '前职业选手，一对一指导，快速提升', 100.00, '小时', 1, 4.98, 300, 200, '英雄联盟', '超凡大师'),
-- 用户5的技能
(7, 5, '治愈聊天', 'online', 'https://cdn.example.com/skill/heal.png', '温柔声线，治愈系陪伴，驱散你的烦恼', 40.00, '小时', 1, 4.75, 80, 35, NULL, NULL);

-- ========================================
-- 3.4 插入技能展示图片
-- ========================================
INSERT INTO `skill_images` (`skill_id`, `image_url`, `sort_order`) VALUES
(1, 'https://cdn.example.com/skill/wzry_1.png', 1),
(1, 'https://cdn.example.com/skill/wzry_2.png', 2),
(3, 'https://cdn.example.com/skill/chat_1.png', 1),
(6, 'https://cdn.example.com/skill/teach_1.png', 1),
(6, 'https://cdn.example.com/skill/teach_2.png', 2);

-- ========================================
-- 3.5 插入技能服务承诺
-- ========================================
INSERT INTO `skill_promises` (`skill_id`, `promise_text`, `sort_order`) VALUES
(1, '准时上线', 1),
(1, '态度友好', 2),
(1, '不满意退款', 3),
(3, '声音甜美', 1),
(3, '耐心倾听', 2),
(6, '专业指导', 1),
(6, '包教包会', 2);

-- ========================================
-- 3.6 插入微信解锁配置
-- ========================================
INSERT INTO `wechat_unlock_config` (`config_key`, `config_value`, `config_desc`) VALUES
('unlock_price', '50', '解锁微信默认价格（金币）'),
('vip_free_unlock', 'true', 'VIP是否免费解锁微信'),
('daily_unlock_limit', '10', '每日解锁次数限制');

-- ========================================
-- 3.7 插入技能配置数据（对应添加技能页UI文档）
-- ========================================
INSERT INTO `skill_config` (`config_id`, `name`, `icon`, `skill_type`, `category`, `sort_order`, `status`) VALUES
-- 线上技能（游戏类）
(1, '王者荣耀', 'https://cdn.example.com/skills/wzry.png', 'online', '游戏', 1, 1),
(2, '英雄联盟', 'https://cdn.example.com/skills/lol.png', 'online', '游戏', 2, 1),
(3, '和平精英', 'https://cdn.example.com/skills/pubg.png', 'online', '游戏', 3, 1),
(4, '荒野乱斗', 'https://cdn.example.com/skills/hyld.png', 'online', '游戏', 4, 1),
-- 线下技能（本地服务）
(5, '探店', 'https://cdn.example.com/skills/tanding.png', 'offline', '生活', 5, 1),
(6, '私影', 'https://cdn.example.com/skills/siying.png', 'offline', '生活', 6, 1),
(7, '台球', 'https://cdn.example.com/skills/taiqiu.png', 'offline', '运动', 7, 1),
(8, 'K歌', 'https://cdn.example.com/skills/kge.png', 'offline', '娱乐', 8, 1),
(9, '喝酒', 'https://cdn.example.com/skills/hejiu.png', 'offline', '生活', 9, 1),
(10, '按摩', 'https://cdn.example.com/skills/anmo.png', 'offline', '服务', 10, 1);

-- ========================================
-- 3.8 插入段位配置数据
-- ========================================
-- 王者荣耀 - QQ区 段位
INSERT INTO `skill_rank_config` (`skill_config_id`, `server`, `rank_name`, `sort_order`, `status`) VALUES
(1, 'qq', '永恒钻石', 1, 1),
(1, 'qq', '至尊星耀', 2, 1),
(1, 'qq', '最强王者', 3, 1),
(1, 'qq', '非凡王者', 4, 1),
(1, 'qq', '无双王者', 5, 1),
(1, 'qq', '荣耀王者', 6, 1),
(1, 'qq', '传奇王者', 7, 1);

-- 王者荣耀 - 微信区 段位
INSERT INTO `skill_rank_config` (`skill_config_id`, `server`, `rank_name`, `sort_order`, `status`) VALUES
(1, 'weixin', '永恒钻石', 1, 1),
(1, 'weixin', '至尊星耀', 2, 1),
(1, 'weixin', '最强王者', 3, 1),
(1, 'weixin', '非凡王者', 4, 1),
(1, 'weixin', '无双王者', 5, 1),
(1, 'weixin', '荣耀王者', 6, 1),
(1, 'weixin', '传奇王者', 7, 1);

-- 英雄联盟 段位（通用）
INSERT INTO `skill_rank_config` (`skill_config_id`, `server`, `rank_name`, `sort_order`, `status`) VALUES
(2, 'default', '黄金', 1, 1),
(2, 'default', '铂金', 2, 1),
(2, 'default', '翡翠', 3, 1),
(2, 'default', '钻石', 4, 1),
(2, 'default', '超凡大师', 5, 1),
(2, 'default', '傲世宗师', 6, 1),
(2, 'default', '最强王者', 7, 1);

-- 和平精英 段位
INSERT INTO `skill_rank_config` (`skill_config_id`, `server`, `rank_name`, `sort_order`, `status`) VALUES
(3, 'default', '铂金', 1, 1),
(3, 'default', '钻石', 2, 1),
(3, 'default', '皇冠', 3, 1),
(3, 'default', '王牌', 4, 1),
(3, 'default', '无敌战神', 5, 1),
(3, 'default', '荣耀战神', 6, 1);

-- 荒野乱斗 段位
INSERT INTO `skill_rank_config` (`skill_config_id`, `server`, `rank_name`, `sort_order`, `status`) VALUES
(4, 'default', '黄金', 1, 1),
(4, 'default', '钻石', 2, 1),
(4, 'default', '神话', 3, 1),
(4, 'default', '传奇', 4, 1);

-- ========================================
-- Step 4: Verification
-- ========================================
SELECT '✅ Database created successfully!' AS status;
SELECT 'Database: xypai_user (v1.0.4)' AS info;
SELECT COUNT(*) AS table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'xypai_user';

-- Verify new skill config tables
SELECT '技能配置表' AS table_name, COUNT(*) AS count FROM skill_config;
SELECT '段位配置表' AS table_name, COUNT(*) AS count FROM skill_rank_config;

-- Show all tables
SHOW TABLES;

-- ========================================
-- End of Schema
-- ========================================
