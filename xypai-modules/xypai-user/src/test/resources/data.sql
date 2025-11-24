-- =============================================
-- XiangYuPai User Module - Test Data
-- 测试数据初始化脚本
-- =============================================

-- 清空现有数据
DELETE FROM user_report;
DELETE FROM user_blacklist;
DELETE FROM user_relation;
DELETE FROM skill_promise;
DELETE FROM skill_available_time;
DELETE FROM skill_image;
DELETE FROM skill;
DELETE FROM user_stats;
DELETE FROM `user`;

-- 插入测试用户数据
INSERT INTO `user` (user_id, username, nickname, avatar, gender, birthday, residence, height, weight, occupation, wechat_id, bio, privacy_profile, privacy_moments, privacy_skills, version) VALUES
-- 主测试用户
(1000, 'testuser', '测试用户', 'https://cdn.example.com/avatars/1000.jpg', 1, '1995-05-15', '北京市朝阳区', 175, 65, '软件工程师', 'test_wechat_1000', '这是测试用户的个人简介', 1, 1, 1, 0),

-- 其他测试用户
(2000, 'otheruser', '其他用户', 'https://cdn.example.com/avatars/2000.jpg', 2, '1998-08-20', '上海市浦东新区', 165, 50, '产品经理', 'test_wechat_2000', '我是其他用户', 1, 1, 1, 0),
(2001, 'fan1', '粉丝1', 'https://cdn.example.com/avatars/2001.jpg', 1, '1996-03-10', '广州市天河区', 178, 70, '设计师', 'fan1_wechat', '粉丝用户1', 1, 1, 1, 0),
(2002, 'fan2', '粉丝2', 'https://cdn.example.com/avatars/2002.jpg', 2, '1997-06-25', '深圳市南山区', 160, 48, '市场专员', 'fan2_wechat', '粉丝用户2', 1, 1, 1, 0),
(2003, 'zhangsan', '张三', 'https://cdn.example.com/avatars/2003.jpg', 1, '1994-12-01', '杭州市西湖区', 172, 68, '后端工程师', 'zhangsan_wechat', '我是张三', 1, 1, 1, 0),
(2004, 'following1', '关注1', 'https://cdn.example.com/avatars/2004.jpg', 1, '1999-01-15', '成都市武侯区', 180, 75, '前端工程师', 'following1_wechat', '关注用户1', 1, 1, 1, 0),
(2005, 'following2', '关注2', 'https://cdn.example.com/avatars/2005.jpg', 2, '1995-09-30', '武汉市洪山区', 168, 55, '测试工程师', 'following2_wechat', '关注用户2', 1, 1, 1, 0),
(2006, 'lisi', '李四', 'https://cdn.example.com/avatars/2006.jpg', 1, '1993-04-18', '西安市雁塔区', 176, 72, '运维工程师', 'lisi_wechat', '我是李四', 1, 1, 1, 0),

-- 隐私测试用户
(3000, 'privateuser', '隐私用户', 'https://cdn.example.com/avatars/3000.jpg', 0, '1996-07-07', '北京市海淀区', 170, 60, '数据分析师', 'private_wechat', '隐私用户', 3, 3, 3, 0);

-- 插入用户统计数据
INSERT INTO user_stats (user_id, following_count, fans_count, likes_count, moments_count, skills_count) VALUES
(1000, 2, 2, 150, 10, 2),
(2000, 1, 1, 80, 5, 1),
(2001, 0, 0, 30, 2, 0),
(2002, 1, 0, 45, 3, 0),
(2003, 0, 1, 20, 1, 0),
(2004, 1, 1, 100, 8, 1),
(2005, 0, 1, 60, 4, 0),
(2006, 1, 0, 75, 6, 1),
(3000, 0, 0, 10, 1, 0);

-- 插入技能数据
INSERT INTO skill (skill_id, user_id, skill_type, game_id, game_name, `rank`, price_per_hour, description, is_online, view_count, order_count, rating) VALUES
-- 用户1000的线上技能(王者荣耀)
(3000, 1000, 1, 'wzry', '王者荣耀', '王者', 50.00, '5年王者经验,可带上分,擅长打野和辅助', TRUE, 100, 20, 4.8),

-- 用户1000的线下技能(摄影服务)
(3001, 1000, 2, NULL, NULL, NULL, NULL, '专业摄影师 - 婚礼/活动拍摄', TRUE, 50, 10, 4.9),

-- 用户2000的线上技能(和平精英)
(3002, 2000, 1, 'hpjy', '和平精英', '王牌', 40.00, '3年和平精英经验,可带上分', TRUE, 80, 15, 4.7),

-- 用户2004的线上技能(英雄联盟)
(3003, 2004, 1, 'yxlm', '英雄联盟', '钻石', 30.00, '钻石段位,可教学', TRUE, 60, 8, 4.5),

-- 附近技能测试数据(北京地区)
(3004, 2005, 2, NULL, NULL, NULL, NULL, '家政服务 - 专业清洁', TRUE, 40, 12, 4.6),
(3005, 2006, 2, NULL, NULL, NULL, NULL, '维修服务 - 家电维修', TRUE, 30, 5, 4.4);

-- 更新技能的详细信息
UPDATE skill SET
    service_type_id = 'photography',
    service_type_name = '摄影服务',
    title = '专业摄影师 - 婚礼/活动拍摄',
    price_per_service = 800.00,
    service_location = '北京市朝阳区',
    latitude = 39.9042,
    longitude = 116.4074
WHERE skill_id = 3001;

UPDATE skill SET
    service_type_id = 'cleaning',
    service_type_name = '家政服务',
    title = '专业清洁服务',
    price_per_service = 150.00,
    service_location = '北京市海淀区',
    latitude = 39.9869,
    longitude = 116.3064
WHERE skill_id = 3004;

UPDATE skill SET
    service_type_id = 'repair',
    service_type_name = '维修服务',
    title = '家电维修',
    price_per_service = 200.00,
    service_location = '北京市丰台区',
    latitude = 39.8579,
    longitude = 116.2870
WHERE skill_id = 3005;

-- 插入技能图片数据
INSERT INTO skill_image (skill_id, image_url, sort_order) VALUES
(3000, 'https://cdn.example.com/skills/3000_1.jpg', 1),
(3000, 'https://cdn.example.com/skills/3000_2.jpg', 2),
(3001, 'https://cdn.example.com/skills/3001_1.jpg', 1),
(3001, 'https://cdn.example.com/skills/3001_2.jpg', 2),
(3001, 'https://cdn.example.com/skills/3001_3.jpg', 3),
(3002, 'https://cdn.example.com/skills/3002_1.jpg', 1);

-- 插入技能可用时间
INSERT INTO skill_available_time (skill_id, time_slot) VALUES
(3000, '每天18:00-23:00'),
(3000, '周末全天'),
(3001, '周末'),
(3001, '节假日'),
(3004, '周一至周五'),
(3005, '周末');

-- 插入技能服务承诺
INSERT INTO skill_promise (skill_id, promise_text, sort_order) VALUES
(3001, '满意为止', 1),
(3001, '提供后期修图', 2),
(3001, '按时交付', 3),
(3004, '专业清洁', 1),
(3004, '环保材料', 2),
(3005, '保修3个月', 1),
(3005, '配件正品', 2);

-- 插入用户关系数据
INSERT INTO user_relation (follower_id, following_id) VALUES
-- 用户1000关注了2004和2005
(1000, 2004),
(1000, 2005),

-- 用户2001和2002是1000的粉丝
(2001, 1000),
(2002, 1000),

-- 用户2004和1000互相关注
(2004, 1000),

-- 用户2006关注了1000
(2006, 1000);

-- 插入拉黑数据(测试用)
-- 暂不插入,避免影响测试

-- 插入举报数据(测试用)
-- 暂不插入,避免影响测试
