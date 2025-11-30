-- =========================================================================================
-- XiangYuPai Content Module Database Schema
-- Database: xypai_content
-- Version: 1.0.0
-- Created: 2025-11-14
-- Description: 内容模块数据库,包含动态、评论、话题、互动等功能
-- =========================================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `xypai_content` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `xypai_content`;

-- =========================================================================================
-- 1. 动态表 (feed)
-- =========================================================================================
DROP TABLE IF EXISTS `feed`;
CREATE TABLE `feed` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '动态类型: 1=动态,2=活动,3=技能',
    `title` VARCHAR(50) DEFAULT NULL COMMENT '标题(0-50字符)',
    `content` VARCHAR(1000) NOT NULL COMMENT '内容(1-1000字符)',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `location_name` VARCHAR(100) DEFAULT NULL COMMENT '地点名称',
    `location_address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
    `city_id` BIGINT(20) DEFAULT NULL COMMENT '城市ID',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT(11) NOT NULL DEFAULT 0 COMMENT '评论数',
    `share_count` INT(11) NOT NULL DEFAULT 0 COMMENT '分享数',
    `collect_count` INT(11) NOT NULL DEFAULT 0 COMMENT '收藏数',
    `view_count` INT(11) NOT NULL DEFAULT 0 COMMENT '浏览数',
    `visibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可见范围: 0=公开,1=仅好友,2=仅自己',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态: 0=正常,1=审核中,2=已下架',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0=未删除,1=已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_timestamp` BIGINT(20) NOT NULL COMMENT '创建时间戳(用于排序)',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`, `deleted`),
    KEY `idx_type_status` (`type`, `status`, `deleted`),
    KEY `idx_created_timestamp` (`created_timestamp`),
    KEY `idx_city_id` (`city_id`),
    KEY `idx_location` (`longitude`, `latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态表';

-- =========================================================================================
-- 2. 评论表 (comment)
-- =========================================================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feed_id` BIGINT(20) NOT NULL COMMENT '动态ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父评论ID(NULL=一级评论)',
    `reply_to_user_id` BIGINT(20) DEFAULT NULL COMMENT '回复的用户ID',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT(11) NOT NULL DEFAULT 0 COMMENT '回复数',
    `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶: 0=否,1=是',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0=未删除,1=已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_feed_id` (`feed_id`, `parent_id`, `deleted`),
    KEY `idx_user_id` (`user_id`, `deleted`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =========================================================================================
-- 3. 话题表 (topic)
-- =========================================================================================
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '话题名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '话题描述',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `participant_count` INT(11) NOT NULL DEFAULT 0 COMMENT '参与人数',
    `post_count` INT(11) NOT NULL DEFAULT 0 COMMENT '帖子数',
    `is_official` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否官方话题: 0=否,1=是',
    `is_hot` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否热门话题: 0=否,1=是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_post_count` (`post_count`),
    KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题表';

-- =========================================================================================
-- 4. 动态话题关联表 (feed_topic)
-- =========================================================================================
DROP TABLE IF EXISTS `feed_topic`;
CREATE TABLE `feed_topic` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feed_id` BIGINT(20) NOT NULL COMMENT '动态ID',
    `topic_name` VARCHAR(50) NOT NULL COMMENT '话题名称',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_feed_id` (`feed_id`),
    KEY `idx_topic_name` (`topic_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态话题关联表';

-- =========================================================================================
-- 5. 动态媒体关联表 (feed_media)
-- =========================================================================================
DROP TABLE IF EXISTS `feed_media`;
CREATE TABLE `feed_media` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `feed_id` BIGINT(20) NOT NULL COMMENT '动态ID',
    `media_id` BIGINT(20) NOT NULL COMMENT '媒体ID',
    `media_type` VARCHAR(20) NOT NULL COMMENT '媒体类型: image/video',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_feed_id` (`feed_id`, `sort_order`),
    KEY `idx_media_id` (`media_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态媒体关联表';

-- =========================================================================================
-- 6. 点赞表 (like)
-- =========================================================================================
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型: feed/comment',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- =========================================================================================
-- 7. 收藏表 (collection)
-- =========================================================================================
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型: feed',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- =========================================================================================
-- 8. 分享表 (share)
-- =========================================================================================
DROP TABLE IF EXISTS `share`;
CREATE TABLE `share` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型: feed',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `share_channel` VARCHAR(50) NOT NULL COMMENT '分享渠道: wechat/moments/qq/qzone/weibo/copy_link',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_created` (`user_id`, `created_at`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享表';

-- =========================================================================================
-- 9. 举报表 (report)
-- =========================================================================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '举报人用户ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型: feed/comment/user',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `reason_type` VARCHAR(50) NOT NULL COMMENT '举报类型: harassment/pornography/fraud/illegal/spam/other',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '举报描述(0-200字符)',
    `evidence_images` TEXT DEFAULT NULL COMMENT '举报图片(JSON数组,最多3张)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态: pending/processing/approved/rejected',
    `result` TEXT DEFAULT NULL COMMENT '审核结果说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_target_time` (`user_id`, `target_type`, `target_id`, `created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- =========================================================================================
-- 测试数据
-- =========================================================================================

-- 插入热门话题
INSERT INTO `topic` (`name`, `description`, `is_official`, `is_hot`, `post_count`) VALUES
('探店日记', '分享你的探店体验', 1, 1, 1250),
('美食推荐', '发现身边的美食', 1, 1, 2340),
('王者荣耀', '王者荣耀相关内容', 1, 1, 5678),
('旅行vlog', '记录旅行的美好瞬间', 1, 1, 980),
('健身打卡', '一起健身一起变美', 1, 0, 456),
('游戏陪玩', '找个好搭档一起玩游戏', 1, 1, 3200),
('LOL上分', '英雄联盟上分心得', 1, 1, 2100),
('电竞日常', '电竞选手的日常', 1, 0, 890);

-- =========================================================================================
-- 插入测试动态 (对应 xypai_user 的5个测试用户)
-- 注意: user_id 需要与 xypai_user.users 表的用户ID对应
-- =========================================================================================
INSERT INTO `feed` (`id`, `user_id`, `type`, `title`, `content`, `cover_image`, `location_name`, `like_count`, `comment_count`, `view_count`, `visibility`, `status`, `created_at`, `created_timestamp`) VALUES
-- 用户1 (小明同学) 的动态 - 3条
(1, 1, 1, '今日王者五杀！', '刚刚用李白拿了个五杀，太爽了！有没有小伙伴一起来玩王者？', 'https://picsum.photos/seed/feed1/400/300', '深圳南山', 128, 23, 560, 0, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 HOUR)) * 1000),
(2, 1, 1, NULL, '今天天气不错，在公司楼下喝咖啡，生活就是这么惬意~', 'https://picsum.photos/seed/feed2/400/300', '深圳南山科技园', 56, 8, 230, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000),
(3, 1, 1, '程序员的日常', '又是肝代码的一天，但是能帮到小伙伴上分还是很开心的！', 'https://picsum.photos/seed/feed3/400/300', NULL, 89, 15, 420, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000),

-- 用户2 (小红姐姐) 的动态 - 2条
(4, 2, 1, '新买的耳机到了', '终于可以用更好的设备陪大家聊天啦~ 声音会更清晰哦', 'https://picsum.photos/seed/feed4/400/300', '深圳福田', 234, 45, 890, 0, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 5 HOUR)) * 1000),
(5, 2, 1, NULL, '周末在家追剧，有没有人想来聊聊天呀？', 'https://picsum.photos/seed/feed5/400/300', NULL, 178, 32, 670, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000),

-- 用户3 (游戏女神) 的动态 - 3条
(6, 3, 1, 'LOL钻石晋级成功！', '终于上钻石了！感谢一路陪我打的小伙伴们~', 'https://picsum.photos/seed/feed6/400/300', '深圳宝安', 312, 67, 1230, 0, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 HOUR)) * 1000),
(7, 3, 1, NULL, '今天直播三小时，嗓子都哑了，但是很开心！大家的支持就是我的动力', 'https://picsum.photos/seed/feed7/400/300', NULL, 456, 89, 1560, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000),
(8, 3, 1, '和平精英吃鸡', '四排吃鸡！队友给力，这把我拿了12个人头', 'https://picsum.photos/seed/feed8/400/300', '深圳宝安中心', 267, 43, 980, 0, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 DAY)) * 1000),

-- 用户4 (电竞小王子) 的动态 - 2条
(9, 4, 1, '前职业选手的日常训练', '保持手感，每天至少练习4小时。想变强，就要比别人更努力！', 'https://picsum.photos/seed/feed9/400/300', '深圳龙岗电竞馆', 567, 123, 2340, 0, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 HOUR)) * 1000),
(10, 4, 1, NULL, '今天带了5个学员上分，全部成功！教学的成就感满满~', 'https://picsum.photos/seed/feed10/400/300', NULL, 789, 156, 3120, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000),

-- 用户5 (甜心小姐姐) 的动态 - 2条
(11, 5, 1, '今日份好心情', '阳光正好，微风不燥，适合和你聊聊天~', 'https://picsum.photos/seed/feed11/400/300', '深圳罗湖', 145, 28, 560, 0, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 HOUR)) * 1000),
(12, 5, 1, NULL, '期末考试终于结束了！可以好好陪大家聊天啦，有烦恼的来找我倾诉~', 'https://picsum.photos/seed/feed12/400/300', '深圳大学', 98, 19, 340, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000);

-- =========================================================================================
-- 插入动态话题关联
-- =========================================================================================
INSERT INTO `feed_topic` (`feed_id`, `topic_name`) VALUES
(1, '王者荣耀'),
(3, '游戏陪玩'),
(6, 'LOL上分'),
(7, '电竞日常'),
(8, '游戏陪玩'),
(9, '电竞日常'),
(10, '游戏陪玩');

-- =========================================================================================
-- 插入动态媒体关联 (动态的图片)
-- =========================================================================================
INSERT INTO `feed_media` (`feed_id`, `media_id`, `media_type`, `sort_order`) VALUES
-- 每条动态可以有多张图片
(1, 1, 'image', 0),
(1, 2, 'image', 1),
(2, 3, 'image', 0),
(4, 4, 'image', 0),
(4, 5, 'image', 1),
(6, 6, 'image', 0),
(6, 7, 'image', 1),
(6, 8, 'image', 2),
(9, 9, 'image', 0),
(11, 10, 'image', 0);

-- =========================================================================================
-- 完成
-- =========================================================================================
SELECT 'Database xypai_content created successfully!' AS message;
