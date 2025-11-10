-- ==========================================
-- XY相遇派 - ServiceReview服务评价表创建脚本 v7.1
-- 功能：多维度评价系统 + 商家回复 + 图片评价
-- 作者：Frank (后端交易工程师)
-- 日期：2025-01-14
-- 依赖：service_order表（订单完成后才能评价）
-- ==========================================

-- 检查并删除已存在的表
DROP TABLE IF EXISTS service_review;

SELECT 'Starting ServiceReview table creation...' AS message;

-- ==========================================
-- 创建ServiceReview表
-- ==========================================

CREATE TABLE service_review (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价记录ID',
    
    -- 关联信息（3个字段）
    order_id BIGINT NOT NULL COMMENT '关联订单ID',
    content_id BIGINT COMMENT '关联内容ID（服务/活动）',
    service_type TINYINT NOT NULL COMMENT '服务类型（1=游戏陪玩,2=生活服务,3=活动报名）',
    
    -- 用户信息（2个字段）
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID（买家）',
    reviewee_id BIGINT NOT NULL COMMENT '被评价人ID（卖家）',
    
    -- ⭐ 多维度评分（4个字段）- 核心功能
    rating_overall DECIMAL(3,2) NOT NULL COMMENT '综合评分（1.00-5.00，必填）',
    rating_service DECIMAL(3,2) COMMENT '服务评分（1.00-5.00，可选）',
    rating_attitude DECIMAL(3,2) COMMENT '态度评分（1.00-5.00，可选）',
    rating_quality DECIMAL(3,2) COMMENT '质量评分（1.00-5.00，可选）',
    
    -- 评价内容（2个字段）
    review_text VARCHAR(1000) COMMENT '评价文字内容（最多1000字）',
    review_images VARCHAR(1000) COMMENT '评价图片URLs（逗号分隔，最多9张）',
    
    -- 匿名评价（1个字段）
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名评价',
    
    -- 互动数据（1个字段）
    like_count INT DEFAULT 0 COMMENT '点赞数量（其他用户可以点赞评价）',
    
    -- ⭐ 商家回复（2个字段）- 核心功能
    reply_text VARCHAR(500) COMMENT '商家回复内容（最多500字）',
    reply_time DATETIME COMMENT '回复时间',
    
    -- 状态管理（1个字段）
    status TINYINT DEFAULT 1 COMMENT '评价状态（0=待审核,1=已发布,2=已隐藏,3=已删除）',
    
    -- 时间字段（2个字段）
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- ==========================================
    -- 索引设计（7个索引）
    -- ==========================================
    
    -- 订单索引（确保一个订单只能评价一次）
    UNIQUE INDEX uk_order (order_id),
    
    -- 内容评价索引（查询服务的所有评价，按评分排序）
    INDEX idx_content_rating (content_id, rating_overall DESC, status, created_at DESC),
    
    -- 被评价人索引（查询卖家收到的所有评价）
    INDEX idx_reviewee (reviewee_id, status, created_at DESC),
    
    -- 评价人索引（查询买家发表的所有评价）
    INDEX idx_reviewer (reviewer_id, status, created_at DESC),
    
    -- 服务类型索引（按类型统计评价）
    INDEX idx_service_type (service_type, status, rating_overall DESC),
    
    -- 时间索引（查询最新评价）
    INDEX idx_created (created_at DESC),
    
    -- 状态索引（管理后台筛选）
    INDEX idx_status (status, created_at DESC)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务评价表';

SELECT '✅ ServiceReview table created successfully!' AS message;

-- ==========================================
-- 插入测试数据（可选）
-- ==========================================

-- 示例评价1：游戏陪玩服务 - 五星好评
INSERT INTO service_review (
    order_id, content_id, service_type, 
    reviewer_id, reviewee_id,
    rating_overall, rating_service, rating_attitude, rating_quality,
    review_text, review_images, is_anonymous,
    like_count, status, created_at
) VALUES (
    1001, 2001, 1,  -- 订单1001，内容2001，游戏陪玩
    10001, 10002,   -- 评价人10001，被评价人10002
    5.00, 5.00, 5.00, 5.00,  -- 全五星
    '小姐姐声音超甜，技术也很好，带我上了王者！强烈推荐！🎮',
    'https://cdn.xypai.com/review/img1.jpg,https://cdn.xypai.com/review/img2.jpg',
    FALSE,  -- 实名评价
    15, 1, NOW()  -- 15个赞，已发布
);

-- 示例评价2：生活服务 - 四星好评
INSERT INTO service_review (
    order_id, content_id, service_type, 
    reviewer_id, reviewee_id,
    rating_overall, rating_service, rating_attitude, rating_quality,
    review_text, is_anonymous,
    reply_text, reply_time,
    like_count, status, created_at
) VALUES (
    1002, 2002, 2,  -- 订单1002，内容2002，生活服务
    10003, 10004,   -- 评价人10003，被评价人10004
    4.50, 4.50, 5.00, 4.00,  -- 综合4.5星
    '探店体验很不错，环境优雅，服务周到，美中不足是等位时间有点长。',
    FALSE,
    '感谢您的宝贵意见，我们会优化预约流程，期待您下次光临！',
    DATE_ADD(NOW(), INTERVAL 1 DAY),
    8, 1, NOW()  -- 8个赞，已发布，有商家回复
);

-- 示例评价3：匿名差评
INSERT INTO service_review (
    order_id, content_id, service_type, 
    reviewer_id, reviewee_id,
    rating_overall, rating_service, rating_attitude, rating_quality,
    review_text, is_anonymous,
    like_count, status, created_at
) VALUES (
    1003, 2003, 1,  -- 订单1003，内容2003，游戏陪玩
    10005, 10006,   -- 评价人10005，被评价人10006
    2.50, 2.00, 2.50, 3.00,  -- 低评分
    '技术一般，态度也不是很好，性价比不高。',
    TRUE,  -- 匿名评价
    3, 1, NOW()  -- 3个赞，已发布
);

SELECT '✅ Test data inserted successfully!' AS message;

-- ==========================================
-- 统计查询示例
-- ==========================================

-- 查询某个服务的评价统计
SELECT 
    content_id,
    COUNT(*) AS total_reviews,
    AVG(rating_overall) AS avg_rating,
    SUM(CASE WHEN rating_overall >= 4.5 THEN 1 ELSE 0 END) AS good_reviews,
    ROUND(SUM(CASE WHEN rating_overall >= 4.5 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS good_rate,
    MAX(created_at) AS latest_review_time
FROM service_review
WHERE content_id = 2001 AND status = 1
GROUP BY content_id;

-- 查询评分分布
SELECT 
    CASE 
        WHEN rating_overall >= 4.5 THEN '5星（好评）'
        WHEN rating_overall >= 3.5 THEN '4星（中评）'
        ELSE '1-3星（差评）'
    END AS rating_level,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM service_review WHERE status = 1), 2) AS percentage
FROM service_review
WHERE status = 1
GROUP BY rating_level
ORDER BY MIN(rating_overall) DESC;

-- 查询最新评价（带商家回复）
SELECT 
    sr.id,
    sr.rating_overall,
    sr.review_text,
    sr.is_anonymous,
    sr.reply_text,
    sr.reply_time,
    sr.like_count,
    sr.created_at,
    CASE WHEN sr.reply_text IS NOT NULL THEN '已回复' ELSE '未回复' END AS reply_status
FROM service_review sr
WHERE sr.status = 1
ORDER BY sr.created_at DESC
LIMIT 10;

SELECT '========================================' AS message
UNION ALL SELECT '✅ ServiceReview表创建完成！' AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT CONCAT('表结构：18个字段') AS message
UNION ALL SELECT CONCAT('索引数量：7个') AS message
UNION ALL SELECT CONCAT('测试数据：3条') AS message
UNION ALL SELECT CONCAT('创建时间：', NOW()) AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT '📋 功能清单：' AS message
UNION ALL SELECT '✅ 多维度评分（综合/服务/态度/质量）' AS message
UNION ALL SELECT '✅ 图片评价（最多9张）' AS message
UNION ALL SELECT '✅ 匿名评价支持' AS message
UNION ALL SELECT '✅ 商家回复功能' AS message
UNION ALL SELECT '✅ 评价点赞功能' AS message
UNION ALL SELECT '✅ 评价审核机制' AS message
UNION ALL SELECT '========================================' AS message;

-- ==========================================
-- 业务约束说明
-- ==========================================
-- 
-- 评价规则：
-- 1. 只有订单状态为"已完成"才能评价
-- 2. 每个订单只能评价一次（uk_order唯一索引）
-- 3. 评价时间限制：订单完成后7天内
-- 4. 评分范围：1.00 - 5.00（精确到小数点后2位）
-- 5. 匿名评价不显示评价人昵称和头像
-- 6. 商家回复后不能删除评价
-- 
-- 评价展示：
-- 1. 默认按时间倒序
-- 2. 支持按评分筛选（好评4.5+/中评3.5-4.5/差评<3.5）
-- 3. 有图评价优先展示
-- 4. 商家已回复的优先展示
-- 
-- 评价统计：
-- 1. 计算平均评分（保留1位小数）
-- 2. 计算好评率（4.5星及以上占比）
-- 3. 分维度统计（服务/态度/质量）
-- 4. 实时更新到ServiceStats表
-- ==========================================

