-- =====================================================
-- 筛选功能测试数据更新脚本
-- =====================================================
-- 用途: 为筛选功能API提供测试数据
-- 说明: 更新现有测试数据，添加 skill_type 等必要字段
-- 执行顺序: 在原有测试数据基础上执行本脚本
-- =====================================================

USE xypai_user;

-- =====================================================
-- 1. 更新 skills 表，添加 skill_type 字段值
-- =====================================================

-- 将所有现有技能设置为线上技能 (online)
UPDATE skills SET skill_type = 'online' WHERE skill_type IS NULL OR skill_type = '';

-- 添加 cover_image 和 description (如果为空)
UPDATE skills SET
    cover_image = CONCAT('https://cdn.example.com/skill/', skill_id, '.jpg'),
    description = CONCAT('专业', skill_name, '服务，技术过硬，态度认真，欢迎下单体验！')
WHERE cover_image IS NULL OR cover_image = '';

-- =====================================================
-- 2. 添加一些线下技能测试数据 (offline)
-- =====================================================

-- 先检查是否已有线下技能，避免重复插入
INSERT INTO skills (user_id, skill_name, skill_type, cover_image, description, price, price_unit, service_type, service_location, is_online, rating, order_count, created_at, updated_at, deleted)
SELECT * FROM (
    SELECT 1001 AS user_id, '台球陪练' AS skill_name, 'offline' AS skill_type,
           'https://cdn.example.com/skill/billiards.jpg' AS cover_image,
           '专业台球教练，10年经验，耐心教学' AS description,
           50.00 AS price, '小时' AS price_unit,
           '台球陪练' AS service_type, '北京市朝阳区三里屯' AS service_location,
           1 AS is_online, 4.8 AS rating, 120 AS order_count,
           NOW() AS created_at, NOW() AS updated_at, 0 AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE user_id = 1001 AND skill_type = 'offline'
);

INSERT INTO skills (user_id, skill_name, skill_type, cover_image, description, price, price_unit, service_type, service_location, is_online, rating, order_count, created_at, updated_at, deleted)
SELECT * FROM (
    SELECT 1011 AS user_id, '健身陪练' AS skill_name, 'offline' AS skill_type,
           'https://cdn.example.com/skill/fitness.jpg' AS cover_image,
           '专业健身教练，帮你科学健身' AS description,
           80.00 AS price, '小时' AS price_unit,
           '健身教练' AS service_type, '北京市海淀区中关村' AS service_location,
           1 AS is_online, 4.9 AS rating, 200 AS order_count,
           NOW() AS created_at, NOW() AS updated_at, 0 AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE user_id = 1011 AND skill_type = 'offline'
);

INSERT INTO skills (user_id, skill_name, skill_type, cover_image, description, price, price_unit, service_type, service_location, is_online, rating, order_count, created_at, updated_at, deleted)
SELECT * FROM (
    SELECT 1021 AS user_id, '桌游主持' AS skill_name, 'offline' AS skill_type,
           'https://cdn.example.com/skill/boardgame.jpg' AS cover_image,
           '资深桌游主持，氛围感拉满' AS description,
           60.00 AS price, '小时' AS price_unit,
           '桌游主持' AS service_type, '成都市锦江区春熙路' AS service_location,
           1 AS is_online, 4.7 AS rating, 90 AS order_count,
           NOW() AS created_at, NOW() AS updated_at, 0 AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE user_id = 1021 AND skill_type = 'offline'
);

-- =====================================================
-- 3. 验证数据
-- =====================================================

-- 查看线上技能数量
SELECT '线上技能数量' AS info, COUNT(*) AS count FROM skills WHERE skill_type = 'online' AND is_online = 1 AND deleted = 0;

-- 查看线下技能数量
SELECT '线下技能数量' AS info, COUNT(*) AS count FROM skills WHERE skill_type = 'offline' AND is_online = 1 AND deleted = 0;

-- 查看段位分布
SELECT '段位分布' AS info, game_rank, COUNT(*) AS count
FROM skills
WHERE skill_type = 'online' AND is_online = 1 AND deleted = 0 AND game_rank IS NOT NULL
GROUP BY game_rank
ORDER BY count DESC;

-- 查看价格范围
SELECT '价格范围' AS info, MIN(price) AS min_price, MAX(price) AS max_price
FROM skills
WHERE is_online = 1 AND deleted = 0;

-- =====================================================
-- 完成
-- =====================================================
-- 筛选功能测试数据已更新！
-- - 所有现有技能已设置 skill_type = 'online'
-- - 已添加 3 个线下技能用于测试
-- =====================================================
