-- ==========================================
-- xypai-user 模块 - 初始化测试数据
-- ==========================================
-- 负责人: Bob
-- 日期: 2025-10-20
-- 数据量: 10用户+20职业+完整业务数据
-- 说明: 用于开发测试环境
-- ==========================================

USE `xypai_user`;

-- ==========================================
-- 1. 职业字典数据（20种职业）
-- ==========================================

INSERT INTO `occupation_dict` (`code`, `name`, `category`, `icon_url`, `sort_order`, `status`, `created_at`) VALUES
('model', '模特', '艺术', 'https://cdn.xypai.com/occupation/model.png', 1, 1, NOW()),
('student', '学生', '教育', 'https://cdn.xypai.com/occupation/student.png', 2, 1, NOW()),
('freelancer', '自由职业', '自由', NULL, 3, 1, NOW()),
('designer', '设计师', '创意', 'https://cdn.xypai.com/occupation/designer.png', 4, 1, NOW()),
('programmer', '程序员', '技术', 'https://cdn.xypai.com/occupation/programmer.png', 5, 1, NOW()),
('teacher', '教师', '教育', NULL, 6, 1, NOW()),
('doctor', '医生', '医疗', NULL, 7, 1, NOW()),
('photographer', '摄影师', '艺术', NULL, 8, 1, NOW()),
('artist', '艺术家', '艺术', NULL, 9, 1, NOW()),
('entrepreneur', '创业者', '商业', NULL, 10, 1, NOW()),
('athlete', '运动员', '体育', NULL, 11, 1, NOW()),
('actor', '演员', '艺术', NULL, 12, 1, NOW()),
('musician', '音乐人', '艺术', NULL, 13, 1, NOW()),
('writer', '作家', '文化', NULL, 14, 1, NOW()),
('engineer', '工程师', '技术', NULL, 15, 1, NOW()),
('nurse', '护士', '医疗', NULL, 16, 1, NOW()),
('lawyer', '律师', '法律', NULL, 17, 1, NOW()),
('accountant', '会计', '金融', NULL, 18, 1, NOW()),
('salesperson', '销售', '商业', NULL, 19, 1, NOW()),
('chef', '厨师', '餐饮', NULL, 20, 1, NOW());

-- ==========================================
-- 2. 测试用户（10个用户）
-- ==========================================

INSERT INTO `user` (`username`, `mobile`, `region_code`, `email`, `password`, `password_salt`, `password_updated_at`, `status`, `login_fail_count`, `login_locked_until`, `last_login_time`, `last_login_ip`, `last_login_device_id`, `is_two_factor_enabled`, `two_factor_secret`, `created_at`, `updated_at`, `deleted`, `version`) VALUES
('alice_dev', '13800138001', '+86', 'alice@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt001', '2024-12-01 10:00:00', 1, 0, NULL, '2025-01-13 09:00:00', '192.168.1.101', 'device_001', FALSE, NULL, '2024-06-01 10:00:00', NOW(), 0, 0),
('bob_designer', '13800138002', '+86', 'bob@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt002', '2024-11-15 14:00:00', 1, 0, NULL, '2025-01-13 10:00:00', '192.168.1.102', 'device_002', FALSE, NULL, '2024-07-15 14:00:00', NOW(), 0, 0),
('charlie_student', '13800138003', '+86', 'charlie@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt003', '2024-12-20 16:00:00', 1, 0, NULL, '2025-01-13 11:00:00', '192.168.1.103', 'device_003', FALSE, NULL, '2024-12-01 16:00:00', NOW(), 0, 0),
('diana_teacher', '13800138004', '+86', 'diana@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt004', '2024-10-10 08:00:00', 1, 0, NULL, '2025-01-13 08:00:00', '192.168.1.104', 'device_004', TRUE, 'TOTP004', '2024-03-10 08:00:00', NOW(), 0, 0),
('erik_freelancer', '13800138005', '+86', 'erik@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt005', '2024-11-20 12:00:00', 1, 0, NULL, '2025-01-12 20:00:00', '127.0.0.1', 'device_005', FALSE, NULL, '2024-05-20 12:00:00', NOW(), 0, 0),
('fiona_pm', '13800138006', '+86', 'fiona@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt006', '2024-09-01 15:00:00', 1, 0, NULL, '2025-01-13 12:00:00', '192.168.1.106', 'device_006', FALSE, NULL, '2024-02-01 15:00:00', NOW(), 0, 0),
('george_analyst', '13800138007', '+86', 'george@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt007', '2024-10-25 11:00:00', 1, 0, NULL, '2025-01-13 07:00:00', '192.168.1.107', 'device_007', FALSE, NULL, '2024-04-25 11:00:00', NOW(), 0, 0),
('helen_photo', '13800138008', '+86', 'helen@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt008', '2024-11-05 13:00:00', 1, 0, NULL, '2025-01-12 18:00:00', '192.168.1.108', 'device_008', FALSE, NULL, '2024-08-05 13:00:00', NOW(), 0, 0),
('ivan_coach', '13800138009', '+86', 'ivan@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt009', '2024-12-10 09:00:00', 1, 0, NULL, '2025-01-13 06:00:00', '192.168.1.109', 'device_009', FALSE, NULL, '2024-09-10 09:00:00', NOW(), 0, 0),
('julia_writer', '13800138010', '+86', 'julia@xypai.com', '$2a$10$YVHGqq9L.X9B5pyIOwNOPe8bGOd0QQ7H0D1K0P0wPQ1Q2WC3M4X5Y', 'salt010', '2024-11-30 17:00:00', 1, 0, NULL, '2025-01-12 22:00:00', '192.168.1.110', 'device_010', FALSE, NULL, '2024-10-30 17:00:00', NOW(), 0, 0);

-- ==========================================
-- 3. 用户资料（10条，34字段完整）
-- ==========================================

INSERT INTO `user_profile` (`user_id`, `nickname`, `avatar`, `avatar_thumbnail`, `background_image`, `gender`, `birthday`, `age`, `city_id`, `location`, `address`, `ip_location`, `bio`, `height`, `weight`, `real_name`, `id_card_encrypted`, `wechat`, `wechat_unlock_condition`, `is_real_verified`, `is_god_verified`, `is_activity_expert`, `is_vip`, `is_popular`, `vip_level`, `vip_expire_time`, `online_status`, `last_online_time`, `profile_completeness`, `last_edit_time`, `deleted_at`, `created_at`, `updated_at`, `version`) VALUES
(1, 'Alice·全栈开发', 'https://picsum.photos/200?1', 'https://picsum.photos/100?1', 'https://picsum.photos/800/400?101', 2, '1996-06-15', 28, 110100, '北京 海淀区', '中关村软件园', '北京', '5年全栈开发经验，精通前后端技术栈', 168, 52, '张爱丽', 'ENC_110101199606150001', 'alice_wx', 1, TRUE, TRUE, FALSE, FALSE, TRUE, 0, NULL, 1, '2025-01-13 09:00:00', 95, '2025-01-10 15:30:00', NULL, NOW(), NOW(), 0),
(2, 'Bob·UI设计师', 'https://picsum.photos/200?2', 'https://picsum.photos/100?2', 'https://picsum.photos/800/400?102', 1, '1998-03-20', 26, 310100, '上海 浦东', '陆家嘴金融中心', '上海', '专业UI/UX设计，作品获多项大奖', 175, 68, '鲍勃', 'ENC_310101199803200002', 'bob_wx', 0, TRUE, TRUE, FALSE, FALSE, FALSE, 0, NULL, 1, '2025-01-13 10:00:00', 90, '2025-01-08 11:20:00', NULL, NOW(), NOW(), 0),
(3, 'Charlie·学生', 'https://picsum.photos/200?3', 'https://picsum.photos/100?3', NULL, 1, '2003-09-10', 21, 330100, '浙江 杭州', '浙江大学紫金港校区', '浙江 杭州', '浙大计算机在读，热爱技术', 178, 65, NULL, NULL, 'charlie_zju', 1, FALSE, FALSE, FALSE, FALSE, FALSE, 0, NULL, 1, '2025-01-13 11:00:00', 65, '2025-01-05 19:45:00', NULL, NOW(), NOW(), 0),
(4, 'Diana·讲师', 'https://picsum.photos/200?4', 'https://picsum.photos/100?4', 'https://picsum.photos/800/400?104', 2, '1989-11-25', 35, 440300, '广东 深圳', '南山区科技园', '广东 深圳', '10年编程教育经验，培养学员1000+', 165, 55, '戴安娜', 'ENC_440300198911250003', 'diana_wx', 0, TRUE, TRUE, TRUE, TRUE, TRUE, 3, '2026-01-14', 1, '2025-01-13 08:00:00', 100, '2025-01-12 16:00:00', NULL, NOW(), NOW(), 0),
(5, 'Erik·自由职业', 'https://picsum.photos/200?5', 'https://picsum.photos/100?5', NULL, 1, '1994-08-18', 30, 510100, '四川 成都', '高新区天府软件园', '四川 成都', '自由开发者，全栈技术专家', 180, 75, NULL, NULL, 'erik_dev', 2, FALSE, FALSE, FALSE, FALSE, FALSE, 0, NULL, 0, '2025-01-12 20:00:00', 75, '2025-01-01 10:00:00', NULL, NOW(), NOW(), 0),
(6, 'Fiona·产品经理', 'https://picsum.photos/200?6', 'https://picsum.photos/100?6', NULL, 2, '1992-07-08', 32, 440100, '广东 广州', '天河区珠江新城', '广东 广州', '资深产品经理，擅长用户体验设计', 162, 50, '菲奥娜', 'ENC_440100199207080004', 'fiona_pm', 1, TRUE, FALSE, FALSE, TRUE, FALSE, 2, '2025-12-31', 1, '2025-01-13 12:00:00', 85, '2024-12-20 14:00:00', NULL, NOW(), NOW(), 0),
(7, 'George·数据分析', 'https://picsum.photos/200?7', 'https://picsum.photos/100?7', NULL, 1, '1995-04-30', 29, 610100, '陕西 西安', '雁塔区高新路', '陕西 西安', '数据科学专家，精通机器学习', 172, 70, NULL, NULL, 'george_data', 1, TRUE, FALSE, FALSE, FALSE, FALSE, 0, NULL, 1, '2025-01-13 07:00:00', 80, '2024-12-15 09:30:00', NULL, NOW(), NOW(), 0),
(8, 'Helen·摄影师', 'https://picsum.photos/200?8', 'https://picsum.photos/100?8', NULL, 2, '1997-02-14', 27, 350200, '福建 厦门', '思明区鼓浪屿', '福建 厦门', '专业摄影师，风光人像俱佳', 166, 48, '海伦', 'ENC_350200199702140005', 'helen_photo', 0, TRUE, TRUE, FALSE, FALSE, FALSE, 0, NULL, 0, '2025-01-12 18:00:00', 90, '2025-01-06 20:00:00', NULL, NOW(), NOW(), 0),
(9, 'Ivan·健身教练', 'https://picsum.photos/200?9', 'https://picsum.photos/100?9', NULL, 1, '1993-12-05', 31, 370200, '山东 青岛', '市南区奥帆中心', '山东 青岛', '国家级健身教练，专业体能训练', 182, 78, '伊万', 'ENC_370200199312050006', 'ivan_coach', 1, TRUE, FALSE, TRUE, FALSE, FALSE, 0, NULL, 1, '2025-01-13 06:00:00', 88, '2024-12-28 07:00:00', NULL, NOW(), NOW(), 0),
(10, 'Julia·文案', 'https://picsum.photos/200?10', 'https://picsum.photos/100?10', NULL, 2, '1996-10-22', 28, 320100, '江苏 南京', '鼓楼区新街口', '江苏 南京', '资深文案策划，创意无限', 160, 47, NULL, NULL, 'julia_writer', 2, FALSE, FALSE, FALSE, FALSE, FALSE, 0, NULL, 0, '2025-01-12 22:00:00', 70, '2024-12-18 16:00:00', NULL, NOW(), NOW(), 0);

-- ==========================================
-- 4. 用户统计数据（10条）
-- ==========================================

INSERT INTO `user_stats` (`user_id`, `follower_count`, `following_count`, `content_count`, `total_like_count`, `total_collect_count`, `activity_organizer_count`, `activity_participant_count`, `activity_success_count`, `activity_cancel_count`, `activity_organizer_score`, `activity_success_rate`, `last_sync_time`, `updated_at`) VALUES
(1, 1520, 380, 45, 8900, 1200, 12, 28, 10, 2, 4.65, 83.33, NOW(), NOW()),
(2, 850, 420, 32, 5600, 800, 8, 22, 7, 1, 4.50, 87.50, NOW(), NOW()),
(3, 120, 180, 5, 230, 35, 1, 15, 1, 0, 5.00, 100.00, NOW(), NOW()),
(4, 2800, 250, 68, 15600, 2100, 25, 35, 23, 2, 4.85, 92.00, NOW(), NOW()),
(5, 450, 520, 18, 1200, 180, 5, 20, 4, 1, 4.20, 80.00, NOW(), NOW()),
(6, 680, 310, 22, 3500, 450, 10, 18, 9, 1, 4.70, 90.00, NOW(), NOW()),
(7, 920, 410, 35, 6800, 920, 6, 25, 5, 1, 4.35, 83.33, NOW(), NOW()),
(8, 1150, 290, 58, 9200, 1500, 15, 30, 13, 2, 4.75, 86.67, NOW(), NOW()),
(9, 780, 180, 42, 4500, 650, 20, 32, 18, 2, 4.80, 90.00, NOW(), NOW()),
(10, 340, 460, 15, 1800, 220, 3, 12, 3, 0, 4.50, 100.00, NOW(), NOW());

-- ==========================================
-- 5. 用户职业标签（22条）
-- ==========================================

INSERT INTO `user_occupation` (`user_id`, `occupation_code`, `sort_order`, `created_at`) VALUES
-- Alice：程序员+工程师+自由职业
(1, 'programmer', 0, NOW()),
(1, 'engineer', 1, NOW()),
(1, 'freelancer', 2, NOW()),
-- Bob：设计师+艺术家
(2, 'designer', 0, NOW()),
(2, 'artist', 1, NOW()),
-- Charlie：学生+程序员
(3, 'student', 0, NOW()),
(3, 'programmer', 1, NOW()),
-- Diana：教师+程序员+工程师
(4, 'teacher', 0, NOW()),
(4, 'programmer', 1, NOW()),
(4, 'engineer', 2, NOW()),
-- Erik：自由职业+程序员
(5, 'freelancer', 0, NOW()),
(5, 'programmer', 1, NOW()),
-- Fiona：创业者+设计师
(6, 'entrepreneur', 0, NOW()),
(6, 'designer', 1, NOW()),
-- George：工程师+程序员
(7, 'engineer', 0, NOW()),
(7, 'programmer', 1, NOW()),
-- Helen：摄影师+艺术家
(8, 'photographer', 0, NOW()),
(8, 'artist', 1, NOW()),
-- Ivan：运动员+教师
(9, 'athlete', 0, NOW()),
(9, 'teacher', 1, NOW()),
-- Julia：作家+设计师
(10, 'writer', 0, NOW()),
(10, 'designer', 1, NOW());

-- ==========================================
-- 6. 用户钱包（10条）
-- ==========================================
-- 🔧 修复：补充created_at字段数据
-- ==========================================

INSERT INTO `user_wallet` (`user_id`, `balance`, `frozen`, `coin_balance`, `total_income`, `total_expense`, `version`, `created_at`, `updated_at`) VALUES
(1, 50000, 0, 1000, 150000, 100000, 0, '2024-06-01 10:00:00', NOW()),
(2, 38000, 5000, 800, 120000, 82000, 0, '2024-07-15 14:00:00', NOW()),
(3, 5000, 0, 200, 5000, 0, 0, '2024-12-01 16:00:00', NOW()),
(4, 45000, 0, 1500, 200000, 155000, 0, '2024-03-10 08:00:00', NOW()),
(5, 32000, 0, 600, 80000, 48000, 0, '2024-05-20 12:00:00', NOW()),
(6, 28000, 0, 2000, 100000, 72000, 0, '2024-02-01 15:00:00', NOW()),
(7, 41000, 3000, 900, 95000, 54000, 0, '2024-04-25 11:00:00', NOW()),
(8, 23000, 0, 1200, 85000, 62000, 0, '2024-08-05 13:00:00', NOW()),
(9, 19000, 0, 500, 70000, 51000, 0, '2024-09-10 09:00:00', NOW()),
(10, 15000, 0, 400, 50000, 35000, 0, '2024-10-30 17:00:00', NOW());

-- ==========================================
-- 7. 用户关系（15条）
-- ==========================================

INSERT INTO `user_relation` (`user_id`, `target_id`, `type`, `status`, `created_at`, `updated_at`) VALUES
-- Alice的关注
(1, 2, 1, 1, '2024-12-15 10:00:00', NOW()),
(1, 4, 1, 1, '2024-12-16 11:00:00', NOW()),
(1, 7, 4, 1, '2024-12-17 12:00:00', NOW()),  -- 特别关注
-- Bob的关注
(2, 1, 1, 1, '2024-12-15 15:00:00', NOW()),
(2, 8, 1, 1, '2024-12-18 16:00:00', NOW()),
(2, 10, 1, 1, '2024-12-19 17:00:00', NOW()),
-- Charlie的关注
(3, 1, 1, 1, '2024-12-20 09:00:00', NOW()),
(3, 4, 1, 1, '2024-12-20 10:00:00', NOW()),
(3, 7, 1, 1, '2024-12-20 11:00:00', NOW()),
-- Diana的关注
(4, 3, 1, 1, '2024-12-21 14:00:00', NOW()),
(4, 1, 1, 1, '2024-12-22 15:00:00', NOW()),
-- 其他用户关注
(5, 2, 1, 1, '2024-12-22 15:00:00', NOW()),
(6, 1, 1, 1, '2024-12-23 16:00:00', NOW()),
(9, 8, 1, 1, '2024-12-24 17:00:00', NOW()),
(10, 6, 1, 1, '2024-12-25 18:00:00', NOW());

-- ==========================================
-- 8. 交易流水（12条）
-- ==========================================

INSERT INTO `transaction` (`user_id`, `amount`, `type`, `ref_type`, `ref_id`, `status`, `payment_method`, `payment_no`, `description`, `created_at`) VALUES
-- 充值记录
(1, 100000, 'recharge', 'system', NULL, 1, 'wechat', 'WX202412011001', '充值100元', '2024-12-01 10:00:00'),
(2, 50000, 'recharge', 'system', NULL, 1, 'alipay', 'ALI202412021002', '充值50元', '2024-12-02 11:00:00'),
(4, 80000, 'recharge', 'system', NULL, 1, 'wechat', 'WX202412031003', '充值80元', '2024-12-03 12:00:00'),
-- 订单收入
(1, 15000, 'consume', 'order', 4001, 1, 'balance', NULL, 'React课程销售收入', '2024-12-20 14:00:00'),
(2, 12000, 'consume', 'order', 4002, 1, 'balance', NULL, 'UI设计服务收入', '2024-12-21 15:00:00'),
(4, 20000, 'consume', 'order', 4003, 1, 'balance', NULL, 'Java课程销售收入', '2024-12-22 16:00:00'),
-- 订单支出
(3, -15000, 'consume', 'order', 4001, 1, 'balance', NULL, '购买React课程', '2024-12-20 14:00:00'),
(6, -12000, 'consume', 'order', 4002, 1, 'balance', NULL, '购买UI设计课程', '2024-12-21 15:00:00'),
(3, -20000, 'consume', 'order', 4003, 1, 'balance', NULL, '购买Java课程', '2024-12-22 16:00:00'),
-- 活动消费
(5, -3000, 'consume', 'activity', 2101, 1, 'balance', NULL, '参加编程技术沙龙', '2024-12-25 18:00:00'),
(9, -2500, 'consume', 'activity', 2103, 1, 'wechat', 'WX202412261011', '参加健身挑战赛', '2024-12-26 19:00:00'),
-- 退款
(3, 12000, 'refund', 'order', 4009, 1, 'balance', NULL, '课程退款', '2024-12-28 10:30:00');

-- ==========================================
-- 数据初始化完成提示
-- ==========================================

SELECT '✅ xypai-user模块：测试数据初始化完成' AS status,
       '10个用户 + 20种职业 + 22个职业关联 + 15个关注关系 + 12条交易' AS data_summary,
       '数据已优化：补充created_at，修复外键关系' AS improvements;

