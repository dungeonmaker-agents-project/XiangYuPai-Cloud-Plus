-- ==========================================
-- XiangYuPai内容模块 - Feed动态测试数据初始化
-- ==========================================
-- 负责人: XyPai Team
-- 日期: 2025-11-27
-- 描述: 为首页Feed流功能提供测试数据
-- ==========================================

USE `xypai_content`;

-- ==========================================
-- 1. 清理旧的测试数据（可选）
-- ==========================================
DELETE FROM `feed_topic` WHERE feed_id BETWEEN 1001 AND 1020;
DELETE FROM `feed_media` WHERE feed_id BETWEEN 1001 AND 1020;
DELETE FROM `comment` WHERE feed_id BETWEEN 1001 AND 1020;
DELETE FROM `feed` WHERE id BETWEEN 1001 AND 1020;

-- ==========================================
-- 2. 插入Feed动态数据
-- ==========================================

-- 动态类型 (type=1) - 普通动态
INSERT INTO `feed` (
    `id`, `user_id`, `type`, `title`, `content`, `cover_image`,
    `location_name`, `location_address`, `longitude`, `latitude`, `city_id`,
    `like_count`, `comment_count`, `share_count`, `collect_count`, `view_count`,
    `visibility`, `status`, `created_at`, `created_timestamp`
) VALUES
-- 热门动态（高互动量，用于hot标签页）
(1001, 1001, 1, '王者荣耀上分心得',
 '分享一下我的王者荣耀上分心得！从青铜到王者只用了两个赛季，关键是找到适合自己的英雄和位置。我主玩打野，推荐新手玩赵云或者李白，容错率高而且伤害爆炸！',
 'https://picsum.photos/800/600?random=101', NULL, NULL, NULL, NULL, NULL,
 156, 28, 45, 38, 1580, 0, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 HOUR)) * 1000),

(1002, 1002, 1, '今日健身打卡',
 '坚持健身第100天！今天练的是背部和二头肌，感觉状态超好。分享一下我的训练计划：引体向上4组x8个，杠铃划船4组x12个，哑铃弯举3组x15个。大家一起加油！💪',
 'https://picsum.photos/800/600?random=102', '深圳湾健身房', '深圳市南山区科技园南路18号', 114.0549, 22.5428, 440305,
 289, 56, 78, 95, 3200, 0, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 5 HOUR)) * 1000),

(1003, 1003, 1, '探店｜发现一家超赞的咖啡店',
 '今天发现了一家隐藏在巷子里的咖啡店，环境超级棒！他们家的手冲咖啡是我喝过最好的，老板是个很有趣的人，还会给你讲咖啡豆的故事。强烈推荐他们家的埃塞俄比亚耶加雪菲！',
 'https://picsum.photos/800/600?random=103', '角落咖啡馆', '深圳市福田区华强北路88号', 114.0912, 22.5478, 440304,
 425, 89, 112, 156, 5680, 0, 0, DATE_SUB(NOW(), INTERVAL 8 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 8 HOUR)) * 1000),

(1004, 1004, 1, '周末爬山记录',
 '周末去爬了梧桐山，虽然累但是风景真的太美了！山顶可以看到整个深圳的全景，云海翻涌的感觉特别震撼。建议大家早点出发，6点开始爬，9点就能到山顶看日出。',
 'https://picsum.photos/800/600?random=104', '梧桐山', '深圳市罗湖区梧桐山国家森林公园', 114.2105, 22.5689, 440303,
 512, 76, 89, 178, 4520, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000),

(1005, 1005, 1, '学习React一个月的感悟',
 '作为一个后端转前端的程序员，学习React一个月了，分享一些心得：1.先把JS基础打牢 2.理解组件化思想 3.多写项目实践 4.学会看官方文档。推荐几个学习资源给大家～',
 'https://picsum.photos/800/600?random=105', NULL, NULL, NULL, NULL, NULL,
 198, 45, 67, 89, 2890, 0, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 12 HOUR)) * 1000),

-- 普通动态（中等互动量）
(1006, 1001, 1, '今日穿搭分享',
 '今天的穿搭是简约风～白T配牛仔裤，再加一双小白鞋，简单但是很舒服。夏天就是要穿得清爽一点，你们今天穿的什么呀？',
 'https://picsum.photos/800/600?random=106', NULL, NULL, NULL, NULL, NULL,
 78, 23, 15, 28, 890, 0, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 HOUR)) * 1000),

(1007, 1002, 1, '推荐一部超好看的电影',
 '昨晚看了《奥本海默》，真的太震撼了！诺兰的叙事手法太牛了，三条时间线交织在一起，每一帧都是艺术。强烈推荐大家去IMAX看，视觉效果绝对值回票价！',
 'https://picsum.photos/800/600?random=107', '万达影城', '深圳市南山区海岸城购物中心', 113.9385, 22.5175, 440305,
 156, 34, 28, 45, 1560, 0, 0, DATE_SUB(NOW(), INTERVAL 18 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 18 HOUR)) * 1000),

(1008, 1003, 1, '自制奶茶教程',
 '在家也能做出奶茶店同款！材料：红茶包、牛奶、糖、珍珠。做法：1.煮珍珠15分钟 2.泡红茶5分钟 3.加入牛奶和糖搅拌 4.加冰块和珍珠。成本不到5块钱，比外面的健康多了！',
 'https://picsum.photos/800/600?random=108', NULL, NULL, NULL, NULL, NULL,
 234, 67, 89, 112, 2340, 0, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 6 HOUR)) * 1000),

-- 同城动态（有地理位置信息，用于local标签页）
(1009, 1004, 1, '深圳湾公园骑行',
 '今天在深圳湾公园骑了20公里，海风吹着超舒服！推荐傍晚来，可以看到超美的日落。这里有专门的骑行道，很安全，租车也很方便，扫码就能骑。',
 'https://picsum.photos/800/600?random=109', '深圳湾公园', '深圳市南山区深圳湾公园', 113.9502, 22.5089, 440305,
 167, 38, 42, 56, 1890, 0, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 HOUR)) * 1000),

(1010, 1005, 1, '华强北淘宝记',
 '来华强北淘了一些电子配件，这里真的是电子产品的天堂！买了一个机械键盘和一些数据线，老板人很好还送了我一个手机壳。记得货比三家，价格差距还是挺大的。',
 'https://picsum.photos/800/600?random=110', '华强北', '深圳市福田区华强北商业区', 114.0912, 22.5478, 440304,
 89, 21, 18, 25, 1120, 0, 0, DATE_SUB(NOW(), INTERVAL 10 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 10 HOUR)) * 1000),

-- 活动类型 (type=2)
(1011, 1001, 2, '周末王者开黑',
 '周六晚上8点组队打王者，需要3个队友！要求：钻石以上段位，会玩打野或者辅助优先。我是荣耀王者，主玩中单法师。有兴趣的小伙伴评论区报名～',
 'https://picsum.photos/800/600?random=111', NULL, NULL, NULL, NULL, NULL,
 45, 28, 12, 15, 680, 0, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 HOUR)) * 1000),

(1012, 1002, 2, '周日羽毛球约球',
 '周日下午2点在南山体育馆打羽毛球，已经有3个人了，再找2个！水平不限，主要是锻炼身体交朋友。场地费AA，预计人均30左右。',
 'https://picsum.photos/800/600?random=112', '南山体育馆', '深圳市南山区南山大道3838号', 113.9285, 22.5375, 440305,
 67, 35, 18, 22, 920, 0, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 6 HOUR)) * 1000),

(1013, 1003, 2, '摄影爱好者交流会',
 '下周六在深圳湾组织一次摄影交流活动，主题是"城市日落"。欢迎各种水平的摄影爱好者参加，可以互相学习交流。会后可以一起吃个饭，费用AA。',
 'https://picsum.photos/800/600?random=113', '深圳湾公园', '深圳市南山区深圳湾公园日出广场', 113.9502, 22.5089, 440305,
 123, 45, 32, 48, 1560, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 2 DAY)) * 1000),

-- 技能服务类型 (type=3)
(1014, 1004, 3, '王者荣耀陪玩',
 '荣耀王者80星，可以陪玩或者教学。擅长打野和中单，可以帮你分析对局、教你基本操作和高级技巧。不满意可以退款，欢迎来撩～',
 'https://picsum.photos/800/600?random=114', NULL, NULL, NULL, NULL, NULL,
 89, 23, 15, 35, 1230, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 DAY)) * 1000),

(1015, 1005, 3, '编程一对一辅导',
 '5年Java开发经验，可以提供编程辅导。包括：Java基础、Spring Boot、MySQL、Redis等。适合想入门编程或者想提升技术的小伙伴，可以先试听一节课。',
 'https://picsum.photos/800/600?random=115', NULL, NULL, NULL, NULL, NULL,
 156, 42, 28, 67, 2340, 0, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 4 DAY)) * 1000),

-- 更多普通动态（用于分页测试）
(1016, 1001, 1, '今日美食',
 '自己做的番茄牛腩，炖了两个小时，牛肉软烂入味，番茄的酸甜和牛肉的香味完美融合。配上一碗米饭，简直是人间美味！',
 'https://picsum.photos/800/600?random=116', NULL, NULL, NULL, NULL, NULL,
 134, 28, 22, 35, 1450, 0, 0, DATE_SUB(NOW(), INTERVAL 7 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 HOUR)) * 1000),

(1017, 1002, 1, '新入手的机械键盘',
 '终于入手了心心念念的HHKB键盘！静电容的手感真的太舒服了，打字像在弹钢琴一样。虽然价格有点贵，但是用了之后觉得物超所值。',
 'https://picsum.photos/800/600?random=117', NULL, NULL, NULL, NULL, NULL,
 89, 34, 18, 28, 1120, 0, 0, DATE_SUB(NOW(), INTERVAL 9 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 9 HOUR)) * 1000),

(1018, 1003, 1, '读书笔记｜人类简史',
 '最近在读《人类简史》，书中有很多颠覆认知的观点。比如说人类之所以能统治地球，不是因为我们更强壮，而是因为我们能够编织故事、建立信任。推荐给喜欢思考的朋友！',
 'https://picsum.photos/800/600?random=118', NULL, NULL, NULL, NULL, NULL,
 178, 45, 38, 56, 1890, 0, 0, DATE_SUB(NOW(), INTERVAL 15 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 15 HOUR)) * 1000),

(1019, 1004, 1, '深圳夜景',
 '从莲花山顶拍的深圳夜景，灯火辉煌太美了！这座城市真的很有活力，每次看到这样的景色都会被感动。',
 'https://picsum.photos/800/600?random=119', '莲花山公园', '深圳市福田区莲花山公园', 114.0598, 22.5589, 440304,
 267, 56, 48, 78, 2890, 0, 0, DATE_SUB(NOW(), INTERVAL 20 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 20 HOUR)) * 1000),

(1020, 1005, 1, '程序员的日常',
 '今天又是debug的一天，一个bug找了三个小时，最后发现是少写了一个分号...程序员的日常就是这样，痛并快乐着😂',
 'https://picsum.photos/800/600?random=120', NULL, NULL, NULL, NULL, NULL,
 312, 89, 67, 95, 3560, 0, 0, DATE_SUB(NOW(), INTERVAL 11 HOUR), UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 11 HOUR)) * 1000);

-- ==========================================
-- 3. 插入动态话题关联数据
-- ==========================================

INSERT INTO `feed_topic` (`feed_id`, `topic_name`) VALUES
(1001, '王者荣耀'),
(1002, '健身打卡'),
(1003, '探店日记'),
(1003, '美食推荐'),
(1004, '旅行vlog'),
(1005, '学习'),
(1008, '美食推荐'),
(1009, '旅行vlog'),
(1011, '王者荣耀'),
(1013, '旅行vlog'),
(1016, '美食推荐'),
(1018, '学习'),
(1019, '旅行vlog');

-- ==========================================
-- 4. 插入评论数据
-- ==========================================

INSERT INTO `comment` (`id`, `feed_id`, `user_id`, `content`, `parent_id`, `like_count`, `reply_count`) VALUES
-- 动态1001的评论
(2001, 1001, 1002, '大佬带带我，我还在青铜混呢😭', NULL, 15, 2),
(2002, 1001, 1003, '赵云确实好上手，但是李白还是需要一定操作的', NULL, 8, 1),
(2003, 1001, 1001, '加油！多练习就好了', 2001, 5, 0),
(2004, 1001, 1004, '我也是赵云起步的，现在已经王者了', 2001, 3, 0),
(2005, 1001, 1005, '李白确实需要手感，但是练好了很秀', 2002, 2, 0),

-- 动态1002的评论
(2006, 1002, 1001, '100天坚持太厉害了！我总是三天打鱼两天晒网', NULL, 28, 1),
(2007, 1002, 1003, '请问大佬用的什么蛋白粉？', NULL, 12, 1),
(2008, 1002, 1002, '谢谢支持！关键是要找到适合自己的节奏', 2006, 8, 0),
(2009, 1002, 1002, '我用的是ON的金标，性价比挺高的', 2007, 6, 0),

-- 动态1003的评论
(2010, 1003, 1004, '求地址！周末想去打卡', NULL, 35, 1),
(2011, 1003, 1005, '耶加雪菲确实好喝，果香很足', NULL, 18, 0),
(2012, 1003, 1003, '在福田华强北那边，具体地址私你～', 2010, 12, 0),

-- 动态1004的评论
(2013, 1004, 1001, '太美了！下周也想去爬', NULL, 22, 0),
(2014, 1004, 1002, '6点出发也太早了吧，我起不来😂', NULL, 15, 1),
(2015, 1004, 1004, '早起有早起的风景嘛，加油！', 2014, 8, 0);

-- ==========================================
-- 5. 插入点赞数据（示例）
-- ==========================================

INSERT INTO `like` (`user_id`, `target_type`, `target_id`) VALUES
(1002, 'feed', 1001),
(1003, 'feed', 1001),
(1004, 'feed', 1001),
(1005, 'feed', 1001),
(1001, 'feed', 1002),
(1003, 'feed', 1002),
(1004, 'feed', 1002),
(1001, 'feed', 1003),
(1002, 'feed', 1003),
(1005, 'feed', 1003),
(1001, 'comment', 2001),
(1003, 'comment', 2001),
(1002, 'comment', 2006);

-- ==========================================
-- 6. 验证数据
-- ==========================================

SELECT '==========================================' AS '';
SELECT 'Feed动态测试数据初始化完成' AS message;
SELECT '==========================================' AS '';

SELECT 'Feed动态数量:' AS stat, COUNT(*) AS count FROM `feed` WHERE id BETWEEN 1001 AND 1020;
SELECT '普通动态(type=1):' AS stat, COUNT(*) AS count FROM `feed` WHERE type = 1 AND id BETWEEN 1001 AND 1020;
SELECT '活动(type=2):' AS stat, COUNT(*) AS count FROM `feed` WHERE type = 2 AND id BETWEEN 1001 AND 1020;
SELECT '技能服务(type=3):' AS stat, COUNT(*) AS count FROM `feed` WHERE type = 3 AND id BETWEEN 1001 AND 1020;
SELECT '有地理位置的动态:' AS stat, COUNT(*) AS count FROM `feed` WHERE longitude IS NOT NULL AND id BETWEEN 1001 AND 1020;
SELECT '评论数量:' AS stat, COUNT(*) AS count FROM `comment` WHERE id BETWEEN 2001 AND 2020;
SELECT '话题关联数量:' AS stat, COUNT(*) AS count FROM `feed_topic` WHERE feed_id BETWEEN 1001 AND 1020;

SELECT '==========================================' AS '';
SELECT '热门动态测试（按热度排序，应返回互动量高的动态）:' AS test;
SELECT '==========================================' AS '';

SELECT
    id,
    title,
    (like_count + comment_count * 2 + share_count * 3 + collect_count * 2) AS hot_score,
    like_count,
    comment_count,
    created_at
FROM feed
WHERE status = 0 AND deleted = 0 AND id BETWEEN 1001 AND 1020
ORDER BY hot_score DESC
LIMIT 5;
