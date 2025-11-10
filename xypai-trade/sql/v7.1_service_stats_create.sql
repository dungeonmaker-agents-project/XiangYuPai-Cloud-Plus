-- ==========================================
-- XY相遇派 - ServiceStats服务统计表创建脚本 v7.1
-- 功能：服务统计数据 + 异步同步机制
-- 作者：Frank (后端交易工程师)
-- 日期：2025-10-21
-- 参考：PL.md 第1156-1178行
-- ==========================================

-- 检查并删除已存在的表
DROP TABLE IF EXISTS service_stats;

SELECT 'Starting ServiceStats table creation...' AS message;

-- ==========================================
-- 创建ServiceStats表（9字段）
-- ==========================================

CREATE TABLE service_stats (
    -- ===== 主键（组合主键） =====
    service_id BIGINT NOT NULL COMMENT '服务ID（GameService.id 或 LifeService.id）',
    service_type TINYINT NOT NULL COMMENT '服务类型（1=游戏服务，2=生活服务）',
    
    -- ===== 统计数据（5个字段） =====
    service_count INT DEFAULT 0 COMMENT '已服务次数',
    avg_rating DECIMAL(3,2) COMMENT '平均评分（5分制）',
    good_rate DECIMAL(5,2) COMMENT '好评率（百分比）',
    avg_response_minutes INT COMMENT '平均响应时间（分钟）',
    total_revenue BIGINT DEFAULT 0 COMMENT '累计收入（分）',
    
    -- ===== 系统字段（2个字段） =====
    last_sync_time DATETIME COMMENT '最后同步时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- ===== 主键约束 =====
    PRIMARY KEY (service_id, service_type),
    
    -- ===== 索引设计 =====
    INDEX idx_type_rating (service_type, avg_rating DESC),
    INDEX idx_service_count (service_count DESC),
    INDEX idx_sync_time (last_sync_time)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务统计表（v7.1，9字段）';

SELECT '✅ ServiceStats table created successfully!' AS message;

-- ==========================================
-- 插入测试数据
-- ==========================================

-- 测试统计1：React辅导服务（2次服务，5星好评）
INSERT INTO service_stats (service_id, service_type, service_count, avg_rating, good_rate, avg_response_minutes, total_revenue, last_sync_time, updated_at) 
VALUES (2201, 1, 2, 4.75, 100.00, 15, 28500, NOW(), NOW());

-- 测试统计2：UI设计服务（2次服务，4.25星）
INSERT INTO service_stats (service_id, service_type, service_count, avg_rating, good_rate, avg_response_minutes, total_revenue, last_sync_time, updated_at) 
VALUES (2202, 1, 2, 4.25, 50.00, 20, 22800, NOW(), NOW());

-- 测试统计3：Java课程服务（1次服务，5星）
INSERT INTO service_stats (service_id, service_type, service_count, avg_rating, good_rate, avg_response_minutes, total_revenue, last_sync_time, updated_at) 
VALUES (2203, 1, 1, 5.00, 100.00, 30, 19000, NOW(), NOW());

-- 测试统计4：摄影指导服务（生活服务，1次，4星）
INSERT INTO service_stats (service_id, service_type, service_count, avg_rating, good_rate, avg_response_minutes, total_revenue, last_sync_time, updated_at) 
VALUES (2205, 2, 1, 4.00, 0.00, 10, 7600, NOW(), NOW());

-- 测试统计5：健身指导服务（生活服务，1次，4.5星）
INSERT INTO service_stats (service_id, service_type, service_count, avg_rating, good_rate, avg_response_minutes, total_revenue, last_sync_time, updated_at) 
VALUES (2206, 2, 1, 4.50, 100.00, 5, 25650, NOW(), NOW());

SELECT '✅ Test data inserted successfully!' AS message;

-- ==========================================
-- 统计查询示例
-- ==========================================

-- 查询游戏服务排行榜（按评分）
SELECT 
    service_id,
    service_count AS total_services,
    avg_rating,
    good_rate,
    total_revenue / 100.0 AS total_revenue_yuan,
    avg_response_minutes
FROM service_stats
WHERE service_type = 1  -- 游戏服务
ORDER BY avg_rating DESC, service_count DESC
LIMIT 10;

-- 查询生活服务排行榜（按服务次数）
SELECT 
    service_id,
    service_count AS total_services,
    avg_rating,
    good_rate,
    total_revenue / 100.0 AS total_revenue_yuan
FROM service_stats
WHERE service_type = 2  -- 生活服务
ORDER BY service_count DESC, avg_rating DESC
LIMIT 10;

-- 查询总体统计
SELECT 
    service_type,
    CASE service_type
        WHEN 1 THEN '游戏服务'
        WHEN 2 THEN '生活服务'
        ELSE '其他'
    END AS service_type_desc,
    COUNT(*) AS total_services,
    SUM(service_count) AS total_orders,
    AVG(avg_rating) AS overall_avg_rating,
    AVG(good_rate) AS overall_good_rate,
    SUM(total_revenue) / 100.0 AS total_revenue_yuan
FROM service_stats
GROUP BY service_type
ORDER BY service_type;

SELECT '========================================' AS message
UNION ALL SELECT '✅ ServiceStats表创建完成！' AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT CONCAT('表结构：9个字段') AS message
UNION ALL SELECT CONCAT('索引数量：3个') AS message
UNION ALL SELECT CONCAT('测试数据：5条') AS message
UNION ALL SELECT CONCAT('创建时间：', NOW()) AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT '📋 功能清单：' AS message
UNION ALL SELECT '✅ 统计服务次数' AS message
UNION ALL SELECT '✅ 平均评分计算' AS message
UNION ALL SELECT '✅ 好评率统计' AS message
UNION ALL SELECT '✅ 响应时间统计' AS message
UNION ALL SELECT '✅ 收入统计' AS message
UNION ALL SELECT '✅ 异步同步机制' AS message
UNION ALL SELECT '========================================' AS message;

-- ==========================================
-- 业务逻辑说明
-- ==========================================
-- 
-- 统计更新机制：
-- 1. 订单完成后 → 通过消息队列异步更新
-- 2. service_count += 1
-- 3. 重新计算 avg_rating（从service_review表）
-- 4. 重新计算 good_rate（好评数/总评价数*100）
-- 5. 更新 total_revenue（累计卖家收入）
-- 
-- 同步策略：
-- 1. 实时更新：订单完成时立即发送MQ消息
-- 2. 定时修正：每天凌晨2点执行全量统计修正
-- 3. 缓存策略：统计数据写入Redis，TTL 30分钟
-- 
-- Redis缓存结构：
-- Key: service_stats:{service_type}:{service_id}
-- Value: Hash {service_count, avg_rating, good_rate, total_revenue}
-- TTL: 1800秒（30分钟）
-- 
-- 查询优先级：
-- 1. 优先查Redis缓存
-- 2. Redis不存在则查MySQL
-- 3. 查询后写入Redis
-- ==========================================

-- ==========================================
-- 回滚脚本（如果需要）
-- ==========================================
-- DROP TABLE IF EXISTS service_stats;

