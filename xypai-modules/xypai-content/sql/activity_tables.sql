-- =============================================
-- 组局活动模块数据库表
-- 数据库: xypai_content
-- 创建时间: 2025-11-27
-- =============================================

-- ----------------------------
-- 1. 活动类型配置表 (字典表)
-- ----------------------------
DROP TABLE IF EXISTS `activity_type`;
CREATE TABLE `activity_type` (
    `type_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '类型ID',
    `type_code` VARCHAR(50) NOT NULL COMMENT '类型编码 (billiards, board_game, etc.)',
    `type_name` VARCHAR(100) NOT NULL COMMENT '类型名称 (台球, 桌游, etc.)',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否热门 0=否 1=是',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0=禁用 1=启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    PRIMARY KEY (`type_id`),
    UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动类型配置表';

-- 初始化活动类型数据
INSERT INTO `activity_type` (`type_code`, `type_name`, `icon_url`, `sort_order`, `is_hot`) VALUES
('billiards', '台球', 'https://cdn.xypai.com/icons/billiards.png', 1, 1),
('board_game', '桌游', 'https://cdn.xypai.com/icons/board_game.png', 2, 1),
('explore', '密室探索', 'https://cdn.xypai.com/icons/explore.png', 3, 0),
('sports', '运动健身', 'https://cdn.xypai.com/icons/sports.png', 4, 1),
('music', '音乐活动', 'https://cdn.xypai.com/icons/music.png', 5, 0),
('food', '美食聚餐', 'https://cdn.xypai.com/icons/food.png', 6, 1),
('movie', '电影观影', 'https://cdn.xypai.com/icons/movie.png', 7, 0),
('travel', '周边旅行', 'https://cdn.xypai.com/icons/travel.png', 8, 0),
('karaoke', 'KTV唱歌', 'https://cdn.xypai.com/icons/karaoke.png', 9, 0),
('other', '其他活动', 'https://cdn.xypai.com/icons/other.png', 99, 0);

-- ----------------------------
-- 2. 组局活动主表
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
    `activity_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `organizer_id` BIGINT NOT NULL COMMENT '发起人用户ID',
    `type_code` VARCHAR(50) NOT NULL COMMENT '活动类型编码',
    `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description` TEXT COMMENT '活动描述',
    `cover_image_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',

    -- 时间相关
    `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '活动结束时间',
    `registration_deadline` DATETIME DEFAULT NULL COMMENT '报名截止时间',

    -- 地点相关
    `location_name` VARCHAR(200) DEFAULT NULL COMMENT '地点名称',
    `location_address` VARCHAR(500) DEFAULT NULL COMMENT '详细地址',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
    `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',

    -- 人数与性别限制
    `min_members` INT DEFAULT 2 COMMENT '最少人数',
    `max_members` INT DEFAULT 10 COMMENT '最多人数',
    `current_members` INT DEFAULT 1 COMMENT '当前已报名人数(含发起人)',
    `gender_limit` VARCHAR(20) DEFAULT 'all' COMMENT '性别限制: all=不限, male=仅男, female=仅女',

    -- 费用相关
    `is_paid` TINYINT(1) DEFAULT 0 COMMENT '是否收费 0=免费 1=收费',
    `fee` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '费用金额(元/人)',
    `fee_description` VARCHAR(200) DEFAULT NULL COMMENT '费用说明',

    -- 状态与配置
    `status` VARCHAR(20) DEFAULT 'recruiting' COMMENT '活动状态: recruiting=招募中, full=已满员, ongoing=进行中, completed=已结束, cancelled=已取消',
    `need_approval` TINYINT(1) DEFAULT 0 COMMENT '是否需要审核 0=自动通过 1=需审核',
    `contact_info` VARCHAR(200) DEFAULT NULL COMMENT '联系方式',

    -- 统计
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `share_count` INT DEFAULT 0 COMMENT '分享次数',

    -- 系统字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    PRIMARY KEY (`activity_id`),
    KEY `idx_organizer_id` (`organizer_id`),
    KEY `idx_type_code` (`type_code`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_city_district` (`city`, `district`),
    KEY `idx_location` (`longitude`, `latitude`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组局活动主表';

-- ----------------------------
-- 3. 活动图片表 (一个活动可以有多张图片)
-- ----------------------------
DROP TABLE IF EXISTS `activity_image`;
CREATE TABLE `activity_image` (
    `image_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`image_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动图片表';

-- ----------------------------
-- 4. 活动标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `activity_tag`;
CREATE TABLE `activity_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动标签关联表';

-- ----------------------------
-- 5. 活动参与者表
-- ----------------------------
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant` (
    `participant_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending=待审核, approved=已通过, rejected=已拒绝, cancelled=已取消',
    `message` VARCHAR(500) DEFAULT NULL COMMENT '报名留言',
    `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',

    -- 系统字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    PRIMARY KEY (`participant_id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动参与者表';

-- ----------------------------
-- 6. 活动支付记录表
-- ----------------------------
DROP TABLE IF EXISTS `activity_payment`;
CREATE TABLE `activity_payment` (
    `payment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `participant_id` BIGINT DEFAULT NULL COMMENT '参与者记录ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式: wechat, alipay, balance',
    `payment_status` VARCHAR(20) DEFAULT 'pending' COMMENT '支付状态: pending=待支付, success=成功, failed=失败, refunded=已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
    `refund_amount` DECIMAL(10, 2) DEFAULT NULL COMMENT '退款金额',
    `refund_reason` VARCHAR(200) DEFAULT NULL COMMENT '退款原因',

    -- 第三方支付信息
    `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    `trade_status` VARCHAR(50) DEFAULT NULL COMMENT '第三方交易状态',

    -- 系统字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    PRIMARY KEY (`payment_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_participant_id` (`participant_id`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动支付记录表';

-- ----------------------------
-- 测试数据: 插入一些示例活动
-- ----------------------------
INSERT INTO `activity` (
    `organizer_id`, `type_code`, `title`, `description`, `cover_image_url`,
    `start_time`, `end_time`, `registration_deadline`,
    `location_name`, `location_address`, `city`, `district`, `longitude`, `latitude`,
    `min_members`, `max_members`, `current_members`, `gender_limit`,
    `is_paid`, `fee`, `fee_description`, `status`, `need_approval`, `contact_info`
) VALUES
-- 活动1: 台球局
(1764226842107, 'billiards', '周末台球局第1期', '欢迎大家来参加台球活动，新手老手都可以，重在参与和交流！',
 'https://picsum.photos/400/300?random=1001',
 DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 4 HOUR, DATE_ADD(NOW(), INTERVAL 2 DAY),
 '星球台球俱乐部', '深圳市南山区科技园南路88号', '深圳市', '南山区', 113.9430, 22.5440,
 2, 6, 2, 'all',
 0, 0.00, NULL, 'recruiting', 0, '微信: activity1'),

-- 活动2: 桌游局
(1764226842557, 'board_game', '周末桌游局第2期', '周末一起来玩桌游吧！剧本杀、狼人杀、UNO等多种选择！',
 'https://picsum.photos/400/300?random=1002',
 DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY) + INTERVAL 5 HOUR, DATE_ADD(NOW(), INTERVAL 4 DAY),
 '欢乐桌游馆', '深圳市南山区科技园南路89号', '深圳市', '南山区', 113.9431, 22.5441,
 4, 10, 3, 'all',
 1, 30.00, '包含场地费和饮料', 'recruiting', 0, '微信: activity2'),

-- 活动3: 密室逃脱
(1764226842107, 'explore', '周末密室探索第3期', '挑战高难度密室，需要有团队协作精神！',
 'https://picsum.photos/400/300?random=1003',
 DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 2 HOUR, DATE_ADD(NOW(), INTERVAL 6 DAY),
 '密室逃脱体验店', '深圳市南山区科技园南路90号', '深圳市', '南山区', 113.9432, 22.5442,
 4, 6, 4, 'all',
 1, 88.00, '门票费用AA', 'recruiting', 1, '微信: activity3'),

-- 活动4: 运动健身
(1764226842557, 'sports', '周末羽毛球局第4期', '一起来打羽毛球，水平不限，重在锻炼身体！',
 'https://picsum.photos/400/300?random=1004',
 DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 3 HOUR, DATE_ADD(NOW(), INTERVAL 1 DAY),
 '阳光健身房', '深圳市南山区科技园南路91号', '深圳市', '南山区', 113.9433, 22.5443,
 2, 4, 2, 'all',
 1, 25.00, '场地费', 'recruiting', 0, '微信: activity4'),

-- 活动5: 美食聚餐
(1764226842107, 'food', '周末火锅局第5期', '吃货集合！一起去吃正宗四川火锅！',
 'https://picsum.photos/400/300?random=1005',
 DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY) + INTERVAL 3 HOUR, DATE_ADD(NOW(), INTERVAL 3 DAY),
 '网红火锅店', '深圳市南山区科技园南路92号', '深圳市', '南山区', 113.9434, 22.5444,
 4, 8, 5, 'all',
 0, 0.00, '费用AA', 'recruiting', 0, '微信: activity5');

-- 插入参与者数据 (注意: 每个活动的每个用户只能有一条记录)
INSERT INTO `activity_participant` (`activity_id`, `user_id`, `status`, `message`, `register_time`, `approve_time`) VALUES
-- 活动1的参与者 (organizer: 1764226842107)
(1, 1764226842557, 'approved', '期待参加！', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
-- 活动2的参与者 (organizer: 1764226842557)
(2, 1764226842107, 'approved', '我要来玩桌游！', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(2, 1764226843001, 'approved', '算我一个！', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
-- 活动3的参与者 (organizer: 1764226842107)
(3, 1764226842557, 'approved', '挑战密室！', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(3, 1764226843001, 'pending', '想参加', NOW() - INTERVAL 1 DAY, NULL),
(3, 1764226843002, 'approved', '一起来', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
-- 活动4的参与者 (organizer: 1764226842557)
(4, 1764226842107, 'approved', '打羽毛球！', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
-- 活动5的参与者 (organizer: 1764226842107)
(5, 1764226842557, 'approved', '吃火锅！', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(5, 1764226843001, 'approved', '算我一份！', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(5, 1764226843002, 'approved', '火锅！', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(5, 1764226843003, 'approved', '必须来！', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- 插入活动标签
INSERT INTO `activity_tag` (`activity_id`, `tag_name`) VALUES
(1, '新手友好'), (1, '周末局'), (1, '台球'),
(2, '新手友好'), (2, '周末局'), (2, '桌游'),
(3, '挑战向'), (3, '密室'), (3, '团队'),
(4, '运动'), (4, '羽毛球'), (4, '健身'),
(5, '美食'), (5, 'AA制'), (5, '火锅');
