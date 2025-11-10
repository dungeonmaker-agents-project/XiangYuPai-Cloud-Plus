-- ==========================================
-- XY相遇派 - xypai-trade 模块 v7.1 快速测试脚本
-- 功能：验证升级是否成功
-- 作者：Frank
-- 日期：2025-01-14
-- ==========================================

SELECT '========================================' AS '';
SELECT '🧪 开始测试 xypai-trade v7.1 升级' AS '';
SELECT '========================================' AS '';

-- ==========================================
-- 测试1: 验证ServiceOrder表结构
-- ==========================================

SELECT '【测试1】验证ServiceOrder表结构...' AS '';

-- 检查新字段是否存在
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'xypai_trade' 
  AND TABLE_NAME = 'service_order'
  AND COLUMN_NAME IN (
    'order_no', 'service_type', 'service_name', 
    'actual_amount', 'base_fee', 'platform_fee',
    'contact_name', 'payment_method', 'completed_at'
)
ORDER BY ORDINAL_POSITION;

-- 期望结果：9行（至少9个新字段存在）

-- ==========================================
-- 测试2: 验证新表是否创建
-- ==========================================

SELECT '【测试2】验证新表是否创建...' AS '';

SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'xypai_trade'
  AND TABLE_NAME IN ('service_review', 'user_wallet', 'transaction')
ORDER BY TABLE_NAME;

-- 期望结果：3行（3张新表）

-- ==========================================
-- 测试3: 验证索引是否创建
-- ==========================================

SELECT '【测试3】验证索引是否创建...' AS '';

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'xypai_trade'
  AND INDEX_NAME IN (
    'uk_order_no', 
    'idx_service_type_status',
    'uk_order',
    'idx_content_rating'
)
ORDER BY TABLE_NAME, INDEX_NAME;

-- 期望结果：多行（关键索引已创建）

-- ==========================================
-- 测试4: 插入测试订单（双写测试）
-- ==========================================

SELECT '【测试4】插入测试订单（双写测试）...' AS '';

-- 插入测试订单
INSERT INTO service_order (
    buyer_id, seller_id, content_id,
    order_no, service_type, service_name,
    amount, base_fee, person_fee, platform_fee, discount_amount, actual_amount,
    contact_name, contact_phone,
    status, created_at, updated_at, version
) VALUES (
    99998, 99999, 9999,                     -- 测试用户
    'SO9999999999999999', 1, '测试服务',    -- 订单信息
    10000, 10000, 0, 500, 600, 9900,        -- 费用明细
    '测试用户', '13800138000',              -- 联系信息
    0, NOW(), NOW(), 0                      -- 状态和时间
);

-- 验证插入
SELECT 
    id, order_no, service_name, 
    amount / 100.0 AS amount_yuan,
    actual_amount / 100.0 AS actual_amount_yuan,
    base_fee / 100.0 AS base_fee_yuan,
    platform_fee / 100.0 AS platform_fee_yuan,
    discount_amount / 100.0 AS discount_amount_yuan,
    contact_name
FROM service_order
WHERE order_no = 'SO9999999999999999';

-- 期望结果：1行，费用明细正确（100-6=99-5=94元实收）

-- ==========================================
-- 测试5: 创建测试钱包
-- ==========================================

SELECT '【测试5】创建测试钱包...' AS '';

-- 创建测试钱包
INSERT INTO user_wallet (user_id, balance, frozen, coin_balance, total_income, total_expense, version)
VALUES (99998, 100000, 0, 500, 100000, 0, 0);  -- 余额1000元

INSERT INTO user_wallet (user_id, balance, frozen, coin_balance, total_income, total_expense, version)
VALUES (99999, 0, 0, 100, 0, 0, 0);  -- 余额0元，卖家

-- 验证钱包
SELECT 
    user_id,
    balance / 100.0 AS balance_yuan,
    frozen / 100.0 AS frozen_yuan,
    coin_balance,
    version
FROM user_wallet
WHERE user_id IN (99998, 99999);

-- 期望结果：2行

-- ==========================================
-- 测试6: 模拟乐观锁扣款
-- ==========================================

SELECT '【测试6】模拟乐观锁扣款...' AS '';

-- 查询钱包（获取version）
SET @user_id = 99998;
SET @version = (SELECT version FROM user_wallet WHERE user_id = @user_id);
SET @deduct_amount = 5000;  -- 扣款50元

SELECT CONCAT('当前余额：', balance / 100.0, '元，version：', version) AS wallet_info
FROM user_wallet WHERE user_id = @user_id;

-- 执行乐观锁扣款
UPDATE user_wallet
SET balance = balance - @deduct_amount,
    total_expense = total_expense + @deduct_amount,
    version = version + 1,
    updated_at = NOW()
WHERE user_id = @user_id
  AND version = @version
  AND balance >= @deduct_amount;

-- 检查影响行数
SELECT ROW_COUNT() AS affected_rows, 
       CASE WHEN ROW_COUNT() > 0 THEN '✅ 扣款成功' ELSE '❌ 扣款失败（并发冲突）' END AS result;

-- 验证扣款结果
SELECT 
    user_id,
    balance / 100.0 AS balance_yuan,
    total_expense / 100.0 AS total_expense_yuan,
    version
FROM user_wallet WHERE user_id = @user_id;

-- 期望结果：余额减少50元，version增加1

-- ==========================================
-- 测试7: 创建测试评价
-- ==========================================

SELECT '【测试7】创建测试评价...' AS '';

-- 先将测试订单标记为已完成
UPDATE service_order 
SET status = 3, completed_at = NOW()
WHERE order_no = 'SO9999999999999999';

-- 插入测试评价
INSERT INTO service_review (
    order_id, content_id, service_type,
    reviewer_id, reviewee_id,
    rating_overall, rating_service, rating_attitude, rating_quality,
    review_text, is_anonymous, like_count, status, created_at
) VALUES (
    (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999'),
    9999, 1,
    99998, 99999,
    4.50, 5.00, 4.50, 4.00,
    '测试评价：服务非常好！',
    FALSE, 0, 1, NOW()
);

-- 验证评价
SELECT 
    id, order_id, 
    rating_overall, rating_service,
    review_text,
    created_at
FROM service_review
WHERE order_id = (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999');

-- 期望结果：1行

-- ==========================================
-- 测试8: 创建交易流水
-- ==========================================

SELECT '【测试8】创建交易流水...' AS '';

-- 插入测试交易（买家支付）
INSERT INTO transaction (
    user_id, amount, type, ref_type, ref_id,
    status, payment_method, description,
    balance_before, balance_after, created_at
) VALUES (
    99998, -9900, 'consume', 'order', 
    (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999'),
    1, 'balance', '测试订单支付',
    100000, 90100, NOW()
);

-- 插入测试交易（卖家收入）
INSERT INTO transaction (
    user_id, amount, type, ref_type, ref_id,
    status, description,
    balance_before, balance_after, created_at
) VALUES (
    99999, 9405, 'income', 'order',
    (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999'),
    1, '测试订单收入（扣5%平台费）',
    0, 9405, NOW()
);

-- 验证交易流水
SELECT 
    id, user_id,
    amount / 100.0 AS amount_yuan,
    type, description,
    balance_before / 100.0 AS before_yuan,
    balance_after / 100.0 AS after_yuan,
    created_at
FROM transaction
WHERE ref_id = (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999')
ORDER BY created_at;

-- 期望结果：2行（买家支出-99元，卖家收入+94.05元）

-- ==========================================
-- 测试9: 综合查询测试
-- ==========================================

SELECT '【测试9】综合查询测试...' AS '';

-- 查询订单+评价+交易的完整信息
SELECT 
    '订单信息' AS info_type,
    so.order_no,
    so.service_name,
    so.actual_amount / 100.0 AS amount_yuan,
    so.contact_name,
    CASE so.status
        WHEN 0 THEN '待付款'
        WHEN 1 THEN '已付款'
        WHEN 2 THEN '服务中'
        WHEN 3 THEN '已完成'
        ELSE '其他'
    END AS status_desc
FROM service_order so
WHERE so.order_no = 'SO9999999999999999'

UNION ALL

SELECT 
    '评价信息' AS info_type,
    CONCAT('评价ID: ', sr.id),
    CONCAT('评分: ', sr.rating_overall),
    sr.review_text,
    CONCAT('点赞: ', sr.like_count),
    ''
FROM service_review sr
WHERE sr.order_id = (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999')

UNION ALL

SELECT 
    '钱包信息' AS info_type,
    CONCAT('买家钱包: ', uw1.user_id),
    CONCAT('余额: ', uw1.balance / 100.0, '元'),
    CONCAT('版本: ', uw1.version),
    '',
    ''
FROM user_wallet uw1
WHERE uw1.user_id = 99998

UNION ALL

SELECT 
    '交易流水' AS info_type,
    t.type,
    CONCAT(t.amount / 100.0, '元'),
    t.description,
    '',
    ''
FROM transaction t
WHERE t.ref_id = (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999')
ORDER BY info_type;

-- ==========================================
-- 测试10: 性能测试（索引验证）
-- ==========================================

SELECT '【测试10】性能测试（索引验证）...' AS '';

-- 测试订单编号索引
EXPLAIN SELECT * FROM service_order WHERE order_no = 'SO9999999999999999';
-- 期望：type=const, key=uk_order_no

-- 测试服务类型索引
EXPLAIN SELECT * FROM service_order WHERE service_type = 1 AND status = 1;
-- 期望：type=ref, key=idx_service_type_status

-- 测试评价内容索引
EXPLAIN SELECT * FROM service_review WHERE content_id = 9999 ORDER BY rating_overall DESC;
-- 期望：type=ref, key=idx_content_rating

-- ==========================================
-- 清理测试数据
-- ==========================================

SELECT '【清理】删除测试数据...' AS '';

DELETE FROM transaction 
WHERE user_id IN (99998, 99999);

DELETE FROM service_review 
WHERE order_id = (SELECT id FROM service_order WHERE order_no = 'SO9999999999999999');

DELETE FROM service_order 
WHERE order_no = 'SO9999999999999999';

DELETE FROM user_wallet 
WHERE user_id IN (99998, 99999);

SELECT '✅ 测试数据已清理' AS '';

-- ==========================================
-- 测试总结
-- ==========================================

SELECT '========================================' AS '';
SELECT '✅ xypai-trade v7.1 升级测试完成！' AS '';
SELECT '========================================' AS '';
SELECT '测试项：10项' AS '';
SELECT '通过率：应该100%' AS '';
SELECT '========================================' AS '';
SELECT '如果所有测试都通过，说明升级成功！' AS '';
SELECT '如果有测试失败，请检查SQL脚本是否正确执行。' AS '';
SELECT '========================================' AS '';

-- ==========================================
-- 快速验证命令
-- ==========================================

-- 验证表数量
SELECT COUNT(*) AS table_count, 
       CASE WHEN COUNT(*) >= 4 THEN '✅ 通过' ELSE '❌ 失败' END AS result
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'xypai_trade'
  AND TABLE_NAME IN ('service_order', 'service_review', 'user_wallet', 'transaction');

-- 验证ServiceOrder字段数量
SELECT COUNT(*) AS column_count,
       CASE WHEN COUNT(*) >= 30 THEN '✅ 通过' ELSE '❌ 失败' END AS result
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'xypai_trade' 
  AND TABLE_NAME = 'service_order';

-- 验证关键索引
SELECT COUNT(*) AS index_count,
       CASE WHEN COUNT(*) >= 4 THEN '✅ 通过' ELSE '❌ 失败' END AS result
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'xypai_trade'
  AND INDEX_NAME IN ('uk_order_no', 'idx_service_type_status', 'uk_order', 'idx_content_rating');

SELECT '========================================' AS '';
SELECT '✅ 快速验证完成！' AS '';
SELECT '如果上面3个验证都是"✅ 通过"，说明升级成功！' AS '';
SELECT '========================================' AS '';

