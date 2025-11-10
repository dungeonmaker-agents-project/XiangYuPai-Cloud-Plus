-- ==========================================
-- XY相遇派 - ServiceOrder表升级脚本 v7.1
-- 功能：字段展开 - 从JSON存储迁移到具体字段
-- 作者：Frank (后端交易工程师)
-- 日期：2025-01-14
-- 策略：双写兼容（保留data字段，新增具体字段）
-- ==========================================

-- 检查表是否存在
SELECT 'Starting ServiceOrder table upgrade to v7.1...' AS message;

-- ==========================================
-- 第一阶段：添加新字段（23个字段）
-- ==========================================

-- 1. 订单基础信息（5个字段）
ALTER TABLE service_order 
ADD COLUMN order_no VARCHAR(50) COMMENT '订单编号（格式：SO+雪花ID）' AFTER id,
ADD COLUMN service_type TINYINT NOT NULL DEFAULT 1 COMMENT '服务类型（1=游戏陪玩,2=生活服务,3=活动报名）' AFTER content_id,
ADD COLUMN service_name VARCHAR(200) COMMENT '服务名称（冗余存储，方便查询）' AFTER service_type,
ADD COLUMN service_time DATETIME COMMENT '服务时间（预约时间）' AFTER service_name,
ADD COLUMN service_duration INT COMMENT '服务时长（分钟）' AFTER duration;

-- 2. 参与人数（1个字段）
ALTER TABLE service_order 
ADD COLUMN participant_count INT DEFAULT 1 COMMENT '参与人数（活动组局使用）' AFTER service_duration;

-- 3. 费用明细（5个字段）⭐ 核心重点
ALTER TABLE service_order 
ADD COLUMN base_fee BIGINT DEFAULT 0 COMMENT '基础服务费（分）' AFTER amount,
ADD COLUMN person_fee BIGINT DEFAULT 0 COMMENT '人数费用（分，活动AA制使用）' AFTER base_fee,
ADD COLUMN platform_fee BIGINT DEFAULT 0 COMMENT '平台服务费（分，抽成）' AFTER person_fee,
ADD COLUMN discount_amount BIGINT DEFAULT 0 COMMENT '优惠减免金额（分）' AFTER platform_fee,
ADD COLUMN actual_amount BIGINT NOT NULL DEFAULT 0 COMMENT '实际支付金额（分）' AFTER discount_amount;

-- 4. 联系信息（3个字段）
ALTER TABLE service_order 
ADD COLUMN contact_name VARCHAR(50) COMMENT '联系人姓名' AFTER actual_amount,
ADD COLUMN contact_phone VARCHAR(20) COMMENT '联系电话' AFTER contact_name,
ADD COLUMN special_request VARCHAR(500) COMMENT '特殊要求/备注' AFTER contact_phone;

-- 5. 支付信息（2个字段）
ALTER TABLE service_order 
ADD COLUMN payment_method VARCHAR(20) COMMENT '支付方式（wallet/wechat/alipay）' AFTER special_request,
ADD COLUMN payment_time DATETIME COMMENT '支付时间' AFTER payment_method;

-- 6. 取消退款信息（2个字段）
ALTER TABLE service_order 
ADD COLUMN cancel_reason VARCHAR(500) COMMENT '取消原因' AFTER payment_time,
ADD COLUMN cancel_time DATETIME COMMENT '取消时间' AFTER cancel_reason;

-- 7. 完成时间（1个字段）
ALTER TABLE service_order 
ADD COLUMN completed_at DATETIME COMMENT '订单完成时间' AFTER cancel_time;

-- 8. 数据迁移标记（2个字段）⭐ 用于双写策略
ALTER TABLE service_order 
ADD COLUMN is_migrated BOOLEAN DEFAULT FALSE COMMENT '是否已迁移（data字段→具体字段）' AFTER version,
ADD COLUMN migrate_time DATETIME COMMENT '迁移时间' AFTER is_migrated;

SELECT 'Phase 1 completed: Added 23 new fields' AS message;

-- ==========================================
-- 第二阶段：创建索引（性能优化）
-- ==========================================

-- 订单编号唯一索引
CREATE UNIQUE INDEX uk_order_no ON service_order(order_no);

-- 服务类型索引（支持按类型筛选）
CREATE INDEX idx_service_type_status ON service_order(service_type, status, created_at);

-- 买家订单索引（优化）
CREATE INDEX idx_buyer_status_time ON service_order(buyer_id, status, created_at DESC);

-- 卖家订单索引（优化）
CREATE INDEX idx_seller_status_time ON service_order(seller_id, status, created_at DESC);

-- 支付时间索引（财务统计使用）
CREATE INDEX idx_payment_time ON service_order(payment_time);

-- 完成时间索引（评价系统使用）
CREATE INDEX idx_completed_at ON service_order(completed_at);

-- 内容订单索引（服务统计使用）
CREATE INDEX idx_content_status ON service_order(content_id, status, created_at);

SELECT 'Phase 2 completed: Created 7 indexes' AS message;

-- ==========================================
-- 第三阶段：数据迁移（现有订单数据）
-- ==========================================

-- 生成订单编号（如果为空）
UPDATE service_order 
SET order_no = CONCAT('SO', id) 
WHERE order_no IS NULL OR order_no = '';

-- 设置默认服务类型为游戏陪玩（1）
UPDATE service_order 
SET service_type = 1 
WHERE service_type IS NULL;

-- 设置实际支付金额等于订单金额（默认值）
UPDATE service_order 
SET actual_amount = amount 
WHERE actual_amount = 0 OR actual_amount IS NULL;

-- 设置基础费用等于订单金额（简化处理）
UPDATE service_order 
SET base_fee = amount 
WHERE base_fee = 0 OR base_fee IS NULL;

SELECT 'Phase 3 completed: Migrated existing order data' AS message;

-- ==========================================
-- 第四阶段：验证数据
-- ==========================================

-- 检查订单编号唯一性
SELECT 
    '订单编号唯一性检查' AS check_type,
    COUNT(*) AS total_orders,
    COUNT(DISTINCT order_no) AS unique_order_nos,
    CASE 
        WHEN COUNT(*) = COUNT(DISTINCT order_no) THEN '✅ 通过'
        ELSE '❌ 失败：存在重复订单编号'
    END AS result
FROM service_order;

-- 检查费用明细一致性
SELECT 
    '费用明细一致性检查' AS check_type,
    COUNT(*) AS total_orders,
    SUM(CASE WHEN amount = actual_amount THEN 1 ELSE 0 END) AS consistent_count,
    CASE 
        WHEN COUNT(*) = SUM(CASE WHEN amount = actual_amount THEN 1 ELSE 0 END) THEN '✅ 通过'
        ELSE '⚠️ 警告：部分订单费用不一致（可能正常）'
    END AS result
FROM service_order;

-- 检查必填字段
SELECT 
    '必填字段检查' AS check_type,
    COUNT(*) AS total_orders,
    SUM(CASE WHEN order_no IS NOT NULL AND order_no != '' THEN 1 ELSE 0 END) AS has_order_no,
    SUM(CASE WHEN service_type IS NOT NULL THEN 1 ELSE 0 END) AS has_service_type,
    SUM(CASE WHEN actual_amount IS NOT NULL AND actual_amount > 0 THEN 1 ELSE 0 END) AS has_actual_amount,
    CASE 
        WHEN COUNT(*) = SUM(CASE WHEN order_no IS NOT NULL THEN 1 ELSE 0 END) THEN '✅ 通过'
        ELSE '❌ 失败：存在空值'
    END AS result
FROM service_order;

-- 统计订单状态分布
SELECT 
    status,
    CASE 
        WHEN status = 0 THEN '待付款'
        WHEN status = 1 THEN '已付款'
        WHEN status = 2 THEN '服务中'
        WHEN status = 3 THEN '已完成'
        WHEN status = 4 THEN '已取消'
        WHEN status = 5 THEN '已退款'
        ELSE '未知'
    END AS status_desc,
    COUNT(*) AS order_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM service_order), 2) AS percentage
FROM service_order
GROUP BY status
ORDER BY status;

SELECT 'Phase 4 completed: Data validation finished' AS message;

-- ==========================================
-- 第五阶段：添加约束（确保数据质量）
-- ==========================================

-- 订单编号不能为空
ALTER TABLE service_order 
MODIFY COLUMN order_no VARCHAR(50) NOT NULL COMMENT '订单编号（格式：SO+雪花ID）';

-- 服务类型不能为空
ALTER TABLE service_order 
MODIFY COLUMN service_type TINYINT NOT NULL COMMENT '服务类型（1=游戏陪玩,2=生活服务,3=活动报名）';

-- 实际支付金额不能为空
ALTER TABLE service_order 
MODIFY COLUMN actual_amount BIGINT NOT NULL COMMENT '实际支付金额（分）';

SELECT 'Phase 5 completed: Added NOT NULL constraints' AS message;

-- ==========================================
-- 升级完成总结
-- ==========================================

SELECT '========================================' AS message
UNION ALL SELECT '✅ ServiceOrder表升级完成！' AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT CONCAT('新增字段：23个') AS message
UNION ALL SELECT CONCAT('新增索引：7个') AS message
UNION ALL SELECT CONCAT('升级版本：v7.1') AS message
UNION ALL SELECT CONCAT('升级时间：', NOW()) AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT '⚠️ 注意事项：' AS message
UNION ALL SELECT '1. data字段保留（兼容旧代码）' AS message
UNION ALL SELECT '2. 新代码优先使用具体字段' AS message
UNION ALL SELECT '3. 逐步迁移data数据到具体字段' AS message
UNION ALL SELECT '4. 灰度切换后可废弃data字段' AS message
UNION ALL SELECT '========================================' AS message;

-- ==========================================
-- 回滚脚本（如果需要）
-- ==========================================
-- DROP INDEX uk_order_no ON service_order;
-- DROP INDEX idx_service_type_status ON service_order;
-- DROP INDEX idx_buyer_status_time ON service_order;
-- DROP INDEX idx_seller_status_time ON service_order;
-- DROP INDEX idx_payment_time ON service_order;
-- DROP INDEX idx_completed_at ON service_order;
-- DROP INDEX idx_content_status ON service_order;
-- 
-- ALTER TABLE service_order DROP COLUMN order_no;
-- ALTER TABLE service_order DROP COLUMN service_type;
-- ALTER TABLE service_order DROP COLUMN service_name;
-- ALTER TABLE service_order DROP COLUMN service_time;
-- ALTER TABLE service_order DROP COLUMN service_duration;
-- ALTER TABLE service_order DROP COLUMN participant_count;
-- ALTER TABLE service_order DROP COLUMN base_fee;
-- ALTER TABLE service_order DROP COLUMN person_fee;
-- ALTER TABLE service_order DROP COLUMN platform_fee;
-- ALTER TABLE service_order DROP COLUMN discount_amount;
-- ALTER TABLE service_order DROP COLUMN actual_amount;
-- ALTER TABLE service_order DROP COLUMN contact_name;
-- ALTER TABLE service_order DROP COLUMN contact_phone;
-- ALTER TABLE service_order DROP COLUMN special_request;
-- ALTER TABLE service_order DROP COLUMN payment_method;
-- ALTER TABLE service_order DROP COLUMN payment_time;
-- ALTER TABLE service_order DROP COLUMN cancel_reason;
-- ALTER TABLE service_order DROP COLUMN cancel_time;
-- ALTER TABLE service_order DROP COLUMN completed_at;
-- ALTER TABLE service_order DROP COLUMN is_migrated;
-- ALTER TABLE service_order DROP COLUMN migrate_time;

