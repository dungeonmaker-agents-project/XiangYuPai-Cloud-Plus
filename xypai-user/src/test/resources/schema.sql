-- =============================================
-- XiangYuPai User Module - Test Database Schema
-- H2数据库测试表结构
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女',
    `birthday` DATE COMMENT '生日',
    `residence` VARCHAR(100) COMMENT '居住地',
    `height` INT COMMENT '身高(cm)',
    `weight` INT COMMENT '体重(kg)',
    `occupation` VARCHAR(50) COMMENT '职业',
    `wechat_id` VARCHAR(50) COMMENT '微信号',
    `bio` VARCHAR(500) COMMENT '个人简介',
    `privacy_profile` TINYINT DEFAULT 1 COMMENT '资料隐私: 1=公开, 2=仅粉丝, 3=私密',
    `privacy_moments` TINYINT DEFAULT 1 COMMENT '动态隐私',
    `privacy_skills` TINYINT DEFAULT 1 COMMENT '技能隐私',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志: 0=正常, 1=删除',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户表';

-- 用户统计表
CREATE TABLE IF NOT EXISTS `user_stats` (
    `stats_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `following_count` INT DEFAULT 0 COMMENT '关注数',
    `fans_count` INT DEFAULT 0 COMMENT '粉丝数',
    `likes_count` INT DEFAULT 0 COMMENT '获赞数',
    `moments_count` INT DEFAULT 0 COMMENT '动态数',
    `skills_count` INT DEFAULT 0 COMMENT '技能数',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`stats_id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) COMMENT='用户统计表';

-- 技能表
CREATE TABLE IF NOT EXISTS `skill` (
    `skill_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '技能ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `skill_type` TINYINT NOT NULL COMMENT '技能类型: 1=线上(陪玩), 2=线下(服务)',
    `game_id` VARCHAR(50) COMMENT '游戏ID(线上技能)',
    `game_name` VARCHAR(50) COMMENT '游戏名称',
    `rank` VARCHAR(50) COMMENT '游戏段位',
    `service_type_id` VARCHAR(50) COMMENT '服务类型ID(线下技能)',
    `service_type_name` VARCHAR(50) COMMENT '服务类型名称',
    `title` VARCHAR(100) COMMENT '标题',
    `description` VARCHAR(500) COMMENT '描述',
    `price_per_hour` DECIMAL(10,2) COMMENT '每小时价格(线上)',
    `price_per_service` DECIMAL(10,2) COMMENT '每次服务价格(线下)',
    `service_location` VARCHAR(200) COMMENT '服务地点',
    `latitude` DECIMAL(10,6) COMMENT '纬度',
    `longitude` DECIMAL(10,6) COMMENT '经度',
    `is_online` BOOLEAN DEFAULT TRUE COMMENT '是否上架',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `order_count` INT DEFAULT 0 COMMENT '订单量',
    `rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '评分',
    `version` INT DEFAULT 0 COMMENT '版本号',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`skill_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_skill_type` (`skill_type`),
    KEY `idx_location` (`latitude`, `longitude`)
) COMMENT='技能表';

-- 技能图片表
CREATE TABLE IF NOT EXISTS `skill_image` (
    `image_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `skill_id` BIGINT NOT NULL COMMENT '技能ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`image_id`),
    KEY `idx_skill_id` (`skill_id`)
) COMMENT='技能图片表';

-- 技能可用时间表
CREATE TABLE IF NOT EXISTS `skill_available_time` (
    `time_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '时间ID',
    `skill_id` BIGINT NOT NULL COMMENT '技能ID',
    `time_slot` VARCHAR(50) NOT NULL COMMENT '时间段',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`time_id`),
    KEY `idx_skill_id` (`skill_id`)
) COMMENT='技能可用时间表';

-- 技能服务承诺表
CREATE TABLE IF NOT EXISTS `skill_promise` (
    `promise_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '承诺ID',
    `skill_id` BIGINT NOT NULL COMMENT '技能ID',
    `promise_text` VARCHAR(200) NOT NULL COMMENT '承诺内容',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`promise_id`),
    KEY `idx_skill_id` (`skill_id`)
) COMMENT='技能服务承诺表';

-- 用户关系表
CREATE TABLE IF NOT EXISTS `user_relation` (
    `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`relation_id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    KEY `idx_follower` (`follower_id`),
    KEY `idx_following` (`following_id`)
) COMMENT='用户关系表';

-- 用户拉黑表
CREATE TABLE IF NOT EXISTS `user_blacklist` (
    `blacklist_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '拉黑ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `blocked_user_id` BIGINT NOT NULL COMMENT '被拉黑用户ID',
    `reason` VARCHAR(200) COMMENT '拉黑原因',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
    PRIMARY KEY (`blacklist_id`),
    UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_blocked` (`blocked_user_id`)
) COMMENT='用户拉黑表';

-- 用户举报表
CREATE TABLE IF NOT EXISTS `user_report` (
    `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
    `reported_user_id` BIGINT NOT NULL COMMENT '被举报用户ID',
    `report_type` TINYINT NOT NULL COMMENT '举报类型: 1=违规内容, 2=骚扰, 3=欺诈, 4=其他',
    `report_reason` VARCHAR(500) NOT NULL COMMENT '举报原因',
    `report_images` TEXT COMMENT '举报证据图片(JSON数组)',
    `status` TINYINT DEFAULT 0 COMMENT '处理状态: 0=待处理, 1=已处理, 2=已驳回',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`report_id`),
    KEY `idx_reporter` (`reporter_id`),
    KEY `idx_reported` (`reported_user_id`),
    KEY `idx_status` (`status`)
) COMMENT='用户举报表';
