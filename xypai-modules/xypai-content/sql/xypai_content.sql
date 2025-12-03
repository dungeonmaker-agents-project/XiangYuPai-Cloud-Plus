-- =========================================================================================
-- XiangYuPai Content Module Database Schema
-- Database: xypai_content
-- Version: 1.1.0
-- Created: 2025-11-14
-- Updated: 2025-12-01
-- Description: 内容模块数据库,包含动态、评论、话题、互动、草稿等功能
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
    `media_type` VARCHAR(20) DEFAULT 'image' COMMENT '媒体类型: image/video',
    `title` VARCHAR(50) DEFAULT NULL COMMENT '标题(0-50字符)',
    `content` VARCHAR(1000) NOT NULL COMMENT '内容(1-1000字符)',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `aspect_ratio` DECIMAL(5,3) DEFAULT 1.333 COMMENT '宽高比(width/height)，默认4:3',
    `duration` INT(11) DEFAULT 0 COMMENT '视频时长(秒)，仅video类型有效',
    `media_width` INT(11) DEFAULT 0 COMMENT '媒体原始宽度(px)',
    `media_height` INT(11) DEFAULT 0 COMMENT '媒体原始高度(px)',
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
    KEY `idx_location` (`longitude`, `latitude`),
    KEY `idx_media_type` (`media_type`)
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
-- 10. 话题分类表 (topic_category)
-- =========================================================================================
DROP TABLE IF EXISTS `topic_category`;
CREATE TABLE `topic_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '分类图标(emoji或icon名称)',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用,1=启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题分类表';

-- =========================================================================================
-- 11. 话题分类关联表 (topic_category_rel)
-- =========================================================================================
DROP TABLE IF EXISTS `topic_category_rel`;
CREATE TABLE `topic_category_rel` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `topic_id` BIGINT(20) NOT NULL COMMENT '话题ID',
    `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_topic_category` (`topic_id`, `category_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题分类关联表';

-- =========================================================================================
-- 12. 动态草稿表 (feed_draft)
-- =========================================================================================
DROP TABLE IF EXISTS `feed_draft`;
CREATE TABLE `feed_draft` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(50) DEFAULT NULL COMMENT '标题',
    `content` VARCHAR(2000) DEFAULT NULL COMMENT '内容',
    `media_list` TEXT DEFAULT NULL COMMENT '媒体列表JSON',
    `topic_list` TEXT DEFAULT NULL COMMENT '话题列表JSON',
    `location_id` VARCHAR(64) DEFAULT NULL COMMENT '地点ID',
    `location_name` VARCHAR(100) DEFAULT NULL COMMENT '地点名称',
    `location_address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
    `visibility` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可见范围: 0=公开,1=仅好友,2=仅自己',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态草稿表';

-- =========================================================================================
-- 13. 发布配置表 (publish_config)
-- =========================================================================================
DROP TABLE IF EXISTS `publish_config`;
CREATE TABLE `publish_config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(64) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(512) NOT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发布配置表';

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
(1, 1, 1, '今日王者五杀！', '刚刚用李白拿了个五杀，太爽了！有没有小伙伴一起来玩王者？', 'https://picsum.photos/seed/feed1/400/300', '深圳南山', 128, 6, 560, 0, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 HOUR)) * 1000),
(2, 1, 1, NULL, '今天天气不错，在公司楼下喝咖啡，生活就是这么惬意~', 'https://picsum.photos/seed/feed2/400/300', '深圳南山科技园', 56, 0, 230, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000),
(3, 1, 1, '程序员的日常', '又是肝代码的一天，但是能帮到小伙伴上分还是很开心的！', 'https://picsum.photos/seed/feed3/400/300', NULL, 89, 0, 420, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000),

-- 用户2 (小红姐姐) 的动态 - 2条
(4, 2, 1, '新买的耳机到了', '终于可以用更好的设备陪大家聊天啦~ 声音会更清晰哦', 'https://picsum.photos/seed/feed4/400/300', '深圳福田', 234, 4, 890, 0, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 5 HOUR)) * 1000),
(5, 2, 1, NULL, '周末在家追剧，有没有人想来聊聊天呀？', 'https://picsum.photos/seed/feed5/400/300', NULL, 178, 0, 670, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000),

-- 用户3 (游戏女神) 的动态 - 3条
(6, 3, 1, 'LOL钻石晋级成功！', '终于上钻石了！感谢一路陪我打的小伙伴们~', 'https://picsum.photos/seed/feed6/400/300', '深圳宝安', 312, 6, 1230, 0, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 HOUR)) * 1000),
(7, 3, 1, NULL, '今天直播三小时，嗓子都哑了，但是很开心！大家的支持就是我的动力', 'https://picsum.photos/seed/feed7/400/300', NULL, 456, 0, 1560, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000),
(8, 3, 1, '和平精英吃鸡', '四排吃鸡！队友给力，这把我拿了12个人头', 'https://picsum.photos/seed/feed8/400/300', '深圳宝安中心', 267, 0, 980, 0, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 DAY)) * 1000),

-- 用户4 (电竞小王子) 的动态 - 2条
(9, 4, 1, '前职业选手的日常训练', '保持手感，每天至少练习4小时。想变强，就要比别人更努力！', 'https://picsum.photos/seed/feed9/400/300', '深圳龙岗电竞馆', 567, 6, 2340, 0, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 HOUR)) * 1000),
(10, 4, 1, NULL, '今天带了5个学员上分，全部成功！教学的成就感满满~', 'https://picsum.photos/seed/feed10/400/300', NULL, 789, 0, 3120, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000),

-- 用户5 (甜心小姐姐) 的动态 - 2条
(11, 5, 1, '今日份好心情', '阳光正好，微风不燥，适合和你聊聊天~', 'https://picsum.photos/seed/feed11/400/300', '深圳罗湖', 145, 4, 560, 0, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 HOUR)) * 1000),
(12, 5, 1, NULL, '期末考试终于结束了！可以好好陪大家聊天啦，有烦恼的来找我倾诉~', 'https://picsum.photos/seed/feed12/400/300', '深圳大学', 98, 0, 340, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000);

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

-- =========================================================================================
-- 插入测试评论数据（仅一级评论）
-- =========================================================================================
INSERT INTO `comment` (`id`, `feed_id`, `user_id`, `content`, `like_count`, `is_top`, `created_at`) VALUES
-- 动态1 (王者五杀) 的评论
(1, 1, 2, '哇，李白五杀太帅了！带带我呀~', 23, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 1, 3, '大佬带飞！我也想上王者', 15, 0, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(3, 1, 5, '好厉害，羡慕！', 8, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(4, 1, 1, '谢谢大家支持！有空一起玩', 12, 0, DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(5, 1, 4, '我也想组队，一起冲王者！', 5, 0, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(6, 1, 3, '加油加油！', 3, 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),

-- 动态4 (新耳机) 的评论
(7, 4, 1, '耳机是什么牌子的？推荐一下', 18, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(8, 4, 3, '期待姐姐的声音！', 45, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(9, 4, 5, '声音好听的小姐姐最棒了~', 22, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(10, 4, 2, '索尼的WH-1000XM5，降噪超棒！', 28, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),

-- 动态6 (LOL钻石) 的评论
(11, 6, 1, '恭喜恭喜！钻石选手了', 34, 0, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(12, 6, 4, '厉害，我还在铂金挣扎', 27, 0, DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(13, 6, 2, '女神太强了！', 56, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(14, 6, 3, '谢谢支持~一起上分呀', 19, 0, DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(15, 6, 5, '一起加油！', 8, 0, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(16, 6, 3, '铂金到钻石其实不难，多练习就好', 15, 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),

-- 动态9 (职业选手训练) 的评论
(17, 9, 2, '前职业选手太厉害了！想跟着学', 89, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(18, 9, 3, '每天4小时，太自律了', 67, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(19, 9, 1, '大神带带我！', 45, 0, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(20, 9, 4, '有空可以来直播间，免费教学', 56, 0, DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(21, 9, 5, '我也想学！', 12, 0, DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(22, 9, 4, '加油，基本功很重要', 23, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),

-- 动态11 (今日好心情) 的评论
(23, 11, 1, '小姐姐声音肯定很好听~', 28, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(24, 11, 2, '今天心情也很好！一起聊聊天呀', 35, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(25, 11, 4, '治愈系的小姐姐，赞！', 19, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(26, 11, 5, '谢谢夸奖，欢迎来聊天~', 15, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- =========================================================================================
-- 插入点赞测试数据
-- =========================================================================================
INSERT INTO `like` (`user_id`, `target_type`, `target_id`) VALUES
-- 动态点赞
(2, 'feed', 1), (3, 'feed', 1), (4, 'feed', 1), (5, 'feed', 1),
(1, 'feed', 4), (3, 'feed', 4), (5, 'feed', 4),
(1, 'feed', 6), (2, 'feed', 6), (4, 'feed', 6), (5, 'feed', 6),
(2, 'feed', 9), (3, 'feed', 9), (5, 'feed', 9),
(1, 'feed', 11), (2, 'feed', 11), (4, 'feed', 11),
-- 评论点赞
(1, 'comment', 1), (3, 'comment', 1), (4, 'comment', 1),
(2, 'comment', 8), (1, 'comment', 8), (5, 'comment', 8),
(2, 'comment', 13), (1, 'comment', 13), (4, 'comment', 13),
(1, 'comment', 17), (3, 'comment', 17), (5, 'comment', 17);

-- =========================================================================================
-- 初始化数据 - 话题分类
-- =========================================================================================
INSERT INTO `topic_category` (`id`, `name`, `icon`, `sort_order`, `status`) VALUES
(1, '游戏', '🎮', 1, 1),
(2, '生活', '🏠', 2, 1),
(3, '美食', '🍔', 3, 1),
(4, '运动', '⚽', 4, 1),
(5, '娱乐', '🎬', 5, 1),
(6, '其他', '📌', 99, 1);

-- =========================================================================================
-- 初始化数据 - 话题分类关联
-- =========================================================================================
INSERT INTO `topic_category_rel` (`topic_id`, `category_id`) VALUES
-- 游戏分类
(3, 1),  -- 王者荣耀
(6, 1),  -- 游戏陪玩
(7, 1),  -- LOL上分
(8, 1),  -- 电竞日常
-- 生活分类
(1, 2),  -- 探店日记
(4, 2),  -- 旅行vlog
-- 美食分类
(2, 3),  -- 美食推荐
-- 运动分类
(5, 4);  -- 健身打卡

-- =========================================================================================
-- 初始化数据 - 发布配置
-- =========================================================================================
INSERT INTO `publish_config` (`config_key`, `config_value`, `description`) VALUES
('max_title_length', '50', '标题最大长度'),
('max_content_length', '2000', '正文最大长度'),
('max_image_count', '9', '最多上传图片数量'),
('max_video_count', '1', '最多上传视频数量'),
('max_topic_count', '5', '最多选择话题数量'),
('max_image_size', '10485760', '单张图片最大大小(10MB)'),
('max_video_size', '104857600', '单个视频最大大小(100MB)'),
('supported_image_formats', 'jpg,jpeg,png,gif,webp', '支持的图片格式'),
('supported_video_formats', 'mp4,mov,avi', '支持的视频格式');
