-- ==========================================
-- XiangYuPai用户模块 - 技能测试数据初始化
-- ==========================================
-- 负责人: XyPai Team
-- 日期: 2025-11-28
-- 描述: 为限时专享/技能筛选功能提供测试数据
-- ==========================================

USE `xypai_user`;

-- ==========================================
-- 1. 首先创建测试用户（如果不存在）
-- ==========================================

-- 插入测试用户（忽略已存在的）
INSERT IGNORE INTO `users` (`user_id`, `mobile`, `country_code`, `nickname`, `avatar`, `gender`, `birthday`, `bio`, `is_online`, `residence`) VALUES
(1001, '13900001001', '+86', '王者小姐姐', 'https://picsum.photos/200?u=1001', 'female', '1998-03-15', '王者荣耀国服第一辅助，带你躺赢上王者！', 1, '北京市朝阳区'),
(1002, '13900001002', '+86', '峡谷霸主', 'https://picsum.photos/200?u=1002', 'male', '1996-07-22', '五年王者经验，专业带飞，不上王者不收费！', 1, '上海市浦东新区'),
(1003, '13900001003', '+86', 'LOL大神', 'https://picsum.photos/200?u=1003', 'male', '1997-11-08', '英雄联盟钻石选手，专业辅助/打野位', 0, '广州市天河区'),
(1004, '13900001004', '+86', '和平精英达人', 'https://picsum.photos/200?u=1004', 'female', '2000-05-20', '和平精英赛季前100，带你吃鸡！', 1, '深圳市南山区'),
(1005, '13900001005', '+86', '原神探索者', 'https://picsum.photos/200?u=1005', 'female', '1999-09-12', '原神满命神里，深渊12层轻松过', 1, '杭州市西湖区'),
(1006, '13900001006', '+86', '永劫高手', 'https://picsum.photos/200?u=1006', 'male', '1995-12-30', '永劫无间大师段位，刀刀致命', 0, '成都市武侯区'),
(1007, '13900001007', '+86', 'CSGO枪神', 'https://picsum.photos/200?u=1007', 'male', '1994-04-18', 'CSGO全球精英，教你成为枪法大师', 1, '武汉市洪山区'),
(1008, '13900001008', '+86', '瑜伽导师', 'https://picsum.photos/200?u=1008', 'female', '1993-08-25', '10年瑜伽教学经验，线下一对一指导', 1, '西安市雁塔区'),
(1009, '13900001009', '+86', '健身教练', 'https://picsum.photos/200?u=1009', 'male', '1992-02-14', '国家级健身教练，帮你打造完美身材', 1, '南京市鼓楼区'),
(1010, '13900001010', '+86', '摄影师小美', 'https://picsum.photos/200?u=1010', 'female', '1996-06-06', '专业摄影师，拍出你最美的一面', 0, '重庆市渝中区');

-- 插入用户统计数据（忽略已存在的）
INSERT IGNORE INTO `user_stats` (`user_id`, `following_count`, `fans_count`, `likes_count`, `posts_count`, `moments_count`, `collections_count`, `skills_count`, `orders_count`) VALUES
(1001, 120, 5800, 15000, 35, 28, 12, 2, 280),
(1002, 85, 3200, 8500, 22, 18, 8, 2, 150),
(1003, 200, 4500, 12000, 45, 35, 20, 1, 200),
(1004, 150, 6200, 18000, 50, 40, 25, 2, 320),
(1005, 95, 2800, 7200, 18, 15, 10, 1, 95),
(1006, 60, 1500, 4000, 12, 10, 5, 1, 60),
(1007, 180, 4000, 10500, 38, 30, 18, 2, 180),
(1008, 45, 1200, 3500, 25, 20, 8, 2, 85),
(1009, 78, 2000, 5500, 30, 25, 12, 2, 120),
(1010, 55, 1800, 4800, 20, 18, 10, 1, 70);

-- ==========================================
-- 2. 插入技能数据（关键：is_online = 1 表示上架）
-- ==========================================

-- 清理旧的测试技能数据（可选，防止重复）
DELETE FROM `skills` WHERE `user_id` BETWEEN 1001 AND 1010;

-- 插入线上技能（游戏陪玩类）
INSERT INTO `skills` (`user_id`, `skill_name`, `skill_type`, `cover_image`, `description`, `price`, `price_unit`, `is_online`, `rating`, `review_count`, `order_count`, `game_name`, `game_rank`, `service_hours`) VALUES
-- 用户1001的技能
(1001, '王者荣耀陪玩', 'online', 'https://picsum.photos/400/300?s=1001', '国服辅助带你躺赢，保证上分，不上分全额退款！专业辅助位，配合默契，让你游戏体验更上一层楼。', 50.00, '小时', 1, 4.85, 156, 280, '王者荣耀', '王者', 1.00),
(1001, '王者荣耀代练', 'online', 'https://picsum.photos/400/300?s=1002', '快速上分，安全可靠，不封号保证。钻石到王者只需3天！', 30.00, '局', 1, 4.90, 98, 180, '王者荣耀', '荣耀王者', 0.50),

-- 用户1002的技能
(1002, '王者荣耀打野教学', 'online', 'https://picsum.photos/400/300?s=1003', '专业打野教学，从青铜到王者的蜕变之路！教你看野怪刷新时间，反野技巧。', 80.00, '小时', 1, 4.75, 120, 150, '王者荣耀', '王者', 2.00),
(1002, '王者荣耀中路教学', 'online', 'https://picsum.photos/400/300?s=1004', '中路法师一哥，教你如何支援全图，控制节奏！', 60.00, '小时', 1, 4.80, 85, 100, '王者荣耀', '王者', 1.50),

-- 用户1003的技能
(1003, '英雄联盟陪玩', 'online', 'https://picsum.photos/400/300?s=1005', '钻石辅助/打野双修，带你体验峡谷乐趣！可语音开黑，气氛轻松愉快。', 45.00, '小时', 1, 4.70, 180, 200, '英雄联盟', '钻石', 1.00),

-- 用户1004的技能
(1004, '和平精英陪玩', 'online', 'https://picsum.photos/400/300?s=1006', '带你吃鸡！赛季前100实力，枪法准确，战术灵活。', 55.00, '小时', 1, 4.88, 210, 320, '和平精英', '无敌战神', 1.00),
(1004, '和平精英教学', 'online', 'https://picsum.photos/400/300?s=1007', '从落地成盒到落地98K，手把手教你成为吃鸡高手！', 100.00, '小时', 1, 4.92, 145, 180, '和平精英', '无敌战神', 2.00),

-- 用户1005的技能
(1005, '原神深渊陪玩', 'online', 'https://picsum.photos/400/300?s=1008', '深渊12层轻松过，带你拿满星！可以帮你规划角色养成路线。', 40.00, '小时', 1, 4.65, 78, 95, '原神', '满命神里', 1.00),

-- 用户1006的技能
(1006, '永劫无间陪玩', 'online', 'https://picsum.photos/400/300?s=1009', '大师段位，教你近战技巧和武器选择，让你成为永劫高手！', 70.00, '小时', 1, 4.60, 45, 60, '永劫无间', '大师', 1.50),

-- 用户1007的技能
(1007, 'CSGO陪玩', 'online', 'https://picsum.photos/400/300?s=1010', '全球精英带你上分！教你压枪、战术配合、地图烟点。', 65.00, '小时', 1, 4.78, 135, 180, 'CSGO', '全球精英', 1.00),
(1007, 'CSGO教学', 'online', 'https://picsum.photos/400/300?s=1011', '从银到精英的进阶之路，系统教学枪法、道具使用、团队配合。', 120.00, '小时', 1, 4.85, 88, 100, 'CSGO', '全球精英', 2.00);

-- 插入线下技能
INSERT INTO `skills` (`user_id`, `skill_name`, `skill_type`, `cover_image`, `description`, `price`, `price_unit`, `is_online`, `rating`, `review_count`, `order_count`, `service_type`, `service_location`, `latitude`, `longitude`) VALUES
-- 用户1008的线下技能
(1008, '瑜伽私教课程', 'offline', 'https://picsum.photos/400/300?s=1012', '10年瑜伽教学经验，根据你的身体状况定制专属课程，帮你改善体态、缓解压力。', 200.00, '小时', 1, 4.95, 65, 85, '瑜伽教学', '西安市雁塔区某瑜伽馆', 34.2274, 108.9452),
(1008, '普拉提入门课', 'offline', 'https://picsum.photos/400/300?s=1013', '普拉提核心训练，提升身体控制力，适合办公室久坐人群。', 180.00, '小时', 1, 4.88, 42, 55, '普拉提', '西安市雁塔区某健身房', 34.2280, 108.9460),

-- 用户1009的线下技能
(1009, '健身私教课程', 'offline', 'https://picsum.photos/400/300?s=1014', '国家级健身教练，制定科学训练计划，帮你打造完美身材！', 250.00, '小时', 1, 4.90, 88, 120, '健身教练', '南京市鼓楼区某健身房', 32.0617, 118.7778),
(1009, '减脂塑形指导', 'offline', 'https://picsum.photos/400/300?s=1015', '科学减脂方案，饮食+运动双管齐下，3个月见证蜕变！', 300.00, '小时', 1, 4.92, 56, 75, '减脂塑形', '南京市鼓楼区某健身房', 32.0620, 118.7780),

-- 用户1010的线下技能
(1010, '人像摄影', 'offline', 'https://picsum.photos/400/300?s=1016', '专业摄影师，提供人像、写真、证件照拍摄服务，后期精修。', 500.00, '小时', 1, 4.82, 55, 70, '摄影服务', '重庆市渝中区解放碑', 29.5572, 106.5783);

-- ==========================================
-- 3. 插入技能图片
-- ==========================================

INSERT INTO `skill_images` (`skill_id`, `image_url`, `sort_order`)
SELECT s.skill_id, CONCAT('https://picsum.photos/800/600?img=', s.skill_id, '_1'), 1
FROM `skills` s WHERE s.user_id BETWEEN 1001 AND 1010;

INSERT INTO `skill_images` (`skill_id`, `image_url`, `sort_order`)
SELECT s.skill_id, CONCAT('https://picsum.photos/800/600?img=', s.skill_id, '_2'), 2
FROM `skills` s WHERE s.user_id BETWEEN 1001 AND 1010;

-- ==========================================
-- 4. 插入技能服务承诺
-- ==========================================

INSERT INTO `skill_promises` (`skill_id`, `promise_text`, `sort_order`)
SELECT s.skill_id, '准时开始服务', 1
FROM `skills` s WHERE s.user_id BETWEEN 1001 AND 1010;

INSERT INTO `skill_promises` (`skill_id`, `promise_text`, `sort_order`)
SELECT s.skill_id, '不满意可退款', 2
FROM `skills` s WHERE s.user_id BETWEEN 1001 AND 1010;

INSERT INTO `skill_promises` (`skill_id`, `promise_text`, `sort_order`)
SELECT s.skill_id, '专业耐心服务', 3
FROM `skills` s WHERE s.user_id BETWEEN 1001 AND 1010;

-- ==========================================
-- 5. 验证数据
-- ==========================================

SELECT '==========================================' AS '';
SELECT '技能测试数据初始化完成' AS message;
SELECT '==========================================' AS '';

SELECT '用户数量:' AS stat, COUNT(*) AS count FROM `users` WHERE user_id BETWEEN 1001 AND 1010;
SELECT '技能数量(总):' AS stat, COUNT(*) AS count FROM `skills` WHERE user_id BETWEEN 1001 AND 1010;
SELECT '上架技能数量:' AS stat, COUNT(*) AS count FROM `skills` WHERE user_id BETWEEN 1001 AND 1010 AND is_online = 1;
SELECT '线上技能:' AS stat, COUNT(*) AS count FROM `skills` WHERE user_id BETWEEN 1001 AND 1010 AND skill_type = 'online';
SELECT '线下技能:' AS stat, COUNT(*) AS count FROM `skills` WHERE user_id BETWEEN 1001 AND 1010 AND skill_type = 'offline';

SELECT '==========================================' AS '';
SELECT '限时专享查询测试（应返回有技能的用户）:' AS test;
SELECT '==========================================' AS '';

SELECT
    u.user_id,
    u.nickname,
    u.gender,
    COUNT(s.skill_id) AS skill_count,
    MIN(s.price) AS min_price
FROM users u
INNER JOIN skills s ON u.user_id = s.user_id
    AND s.is_online = 1
    AND s.deleted = 0
WHERE u.deleted = 0
GROUP BY u.user_id, u.nickname, u.gender
ORDER BY u.user_id
LIMIT 10;
