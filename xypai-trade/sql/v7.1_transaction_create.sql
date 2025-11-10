-- ==========================================
-- XY相遇派 - Transaction交易流水表创建脚本 v7.1
-- 功能：交易记录 + 财务审计 + 对账支持
-- 作者：Frank (后端交易工程师)
-- 日期：2025-01-14
-- 核心：每笔钱包操作都必须有流水记录
-- ==========================================

-- 检查并删除已存在的表
DROP TABLE IF EXISTS transaction;

SELECT 'Starting Transaction table creation...' AS message;

-- ==========================================
-- 创建Transaction表
-- ==========================================

CREATE TABLE transaction (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易记录ID',
    
    -- 用户信息（1个字段）
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- ⭐ 交易金额（1个字段）- 核心字段
    amount BIGINT NOT NULL COMMENT '交易金额（分，正数=收入，负数=支出）',
    
    -- ⭐ 交易类型（1个字段）- 核心分类
    type VARCHAR(20) NOT NULL COMMENT '交易类型（recharge=充值/consume=消费/refund=退款/withdraw=提现/income=收入/transfer=转账）',
    
    -- 关联业务（2个字段）
    ref_type VARCHAR(20) COMMENT '关联类型（order=订单/activity=活动/reward=奖励/transfer=转账）',
    ref_id BIGINT COMMENT '关联业务ID',
    
    -- 交易状态（1个字段）
    status TINYINT DEFAULT 1 COMMENT '交易状态（0=处理中,1=成功,2=失败,3=已取消）',
    
    -- 支付信息（2个字段）
    payment_method VARCHAR(20) COMMENT '支付方式（wechat=微信/alipay=支付宝/balance=余额/bankcard=银行卡）',
    payment_no VARCHAR(100) COMMENT '第三方支付流水号',
    
    -- 交易描述（1个字段）
    description VARCHAR(500) COMMENT '交易描述',
    
    -- 余额快照（2个字段）- 用于对账
    balance_before BIGINT COMMENT '交易前余额（分）',
    balance_after BIGINT COMMENT '交易后余额（分）',
    
    -- 时间字段（1个字段）
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    
    -- ==========================================
    -- 索引设计（8个索引）
    -- ==========================================
    
    -- 用户交易历史索引（最常用）
    INDEX idx_user_time (user_id, created_at DESC),
    
    -- 交易类型索引
    INDEX idx_type_status (type, status, created_at DESC),
    
    -- 关联业务索引（根据订单查交易）
    INDEX idx_ref (ref_type, ref_id),
    
    -- 时间索引（财务统计）
    INDEX idx_created (created_at DESC),
    
    -- 支付流水号索引（第三方对账）
    INDEX idx_payment_no (payment_no),
    
    -- 状态索引（查询处理中/失败的交易）
    INDEX idx_status (status, created_at DESC),
    
    -- 金额索引（查询大额交易）
    INDEX idx_amount (amount DESC, created_at),
    
    -- 用户类型索引（按类型统计）
    INDEX idx_user_type (user_id, type, created_at DESC)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易流水表';

SELECT '✅ Transaction table created successfully!' AS message;

-- ==========================================
-- 插入测试数据
-- ==========================================

-- 测试交易1：充值
INSERT INTO transaction (user_id, amount, type, ref_type, ref_id, status, payment_method, payment_no, description, balance_before, balance_after, created_at) 
VALUES (10001, 100000, 'recharge', 'recharge', 1, 1, 'wechat', 'WX20250114001', '微信充值1000元', 0, 100000, NOW());

-- 测试交易2：消费（订单支付）
INSERT INTO transaction (user_id, amount, type, ref_type, ref_id, status, payment_method, description, balance_before, balance_after, created_at) 
VALUES (10001, -5000, 'consume', 'order', 1001, 1, 'balance', '购买游戏陪玩服务', 100000, 95000, DATE_ADD(NOW(), INTERVAL 1 HOUR));

-- 测试交易3：收入（卖家收款）
INSERT INTO transaction (user_id, amount, type, ref_type, ref_id, status, description, balance_before, balance_after, created_at) 
VALUES (10002, 4750, 'income', 'order', 1001, 1, '订单收入（扣除5%平台服务费）', 5000, 9750, DATE_ADD(NOW(), INTERVAL 2 HOUR));

-- 测试交易4：退款
INSERT INTO transaction (user_id, amount, type, ref_type, ref_id, status, payment_method, description, balance_before, balance_after, created_at) 
VALUES (10001, 5000, 'refund', 'order', 1002, 1, 'balance', '订单退款', 90000, 95000, DATE_ADD(NOW(), INTERVAL 3 HOUR));

-- 测试交易5：提现
INSERT INTO transaction (user_id, amount, type, ref_type, ref_id, status, payment_method, payment_no, description, balance_before, balance_after, created_at) 
VALUES (10002, -10000, 'withdraw', 'withdraw', 1, 1, 'bankcard', 'WD20250114001', '提现100元到银行卡', 15000, 5000, DATE_ADD(NOW(), INTERVAL 4 HOUR));

SELECT '✅ Test data inserted successfully!' AS message;

-- ==========================================
-- 统计查询示例
-- ==========================================

-- 查询用户交易历史（最近10条）
SELECT 
    id,
    CASE 
        WHEN amount > 0 THEN CONCAT('+', amount / 100.0)
        ELSE CONCAT(amount / 100.0)
    END AS amount_yuan,
    type,
    description,
    CASE status
        WHEN 0 THEN '处理中'
        WHEN 1 THEN '成功'
        WHEN 2 THEN '失败'
        ELSE '已取消'
    END AS status_desc,
    created_at
FROM transaction
WHERE user_id = 10001
ORDER BY created_at DESC
LIMIT 10;

-- 查询交易类型分布
SELECT 
    type,
    CASE type
        WHEN 'recharge' THEN '充值'
        WHEN 'consume' THEN '消费'
        WHEN 'refund' THEN '退款'
        WHEN 'withdraw' THEN '提现'
        WHEN 'income' THEN '收入'
        WHEN 'transfer' THEN '转账'
        ELSE '其他'
    END AS type_desc,
    COUNT(*) AS count,
    SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) / 100.0 AS total_income_yuan,
    SUM(CASE WHEN amount < 0 THEN ABS(amount) ELSE 0 END) / 100.0 AS total_expense_yuan,
    SUM(amount) / 100.0 AS net_amount_yuan
FROM transaction
WHERE status = 1
GROUP BY type
ORDER BY count DESC;

-- 查询每日交易统计
SELECT 
    DATE(created_at) AS trade_date,
    COUNT(*) AS total_transactions,
    SUM(CASE WHEN amount > 0 THEN 1 ELSE 0 END) AS income_count,
    SUM(CASE WHEN amount < 0 THEN 1 ELSE 0 END) AS expense_count,
    SUM(amount) / 100.0 AS net_amount_yuan,
    SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) / 100.0 AS total_income_yuan,
    SUM(CASE WHEN amount < 0 THEN ABS(amount) ELSE 0 END) / 100.0 AS total_expense_yuan
FROM transaction
WHERE status = 1 AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(created_at)
ORDER BY trade_date DESC;

-- 查询大额交易（1000元以上）
SELECT 
    id,
    user_id,
    amount / 100.0 AS amount_yuan,
    type,
    description,
    payment_no,
    created_at
FROM transaction
WHERE ABS(amount) >= 100000 AND status = 1
ORDER BY ABS(amount) DESC, created_at DESC
LIMIT 10;

-- 对账：验证钱包余额与交易流水一致性
SELECT 
    tw.user_id,
    tw.balance / 100.0 AS wallet_balance_yuan,
    tw.total_income / 100.0 AS wallet_income_yuan,
    tw.total_expense / 100.0 AS wallet_expense_yuan,
    IFNULL(SUM(CASE WHEN t.amount > 0 THEN t.amount ELSE 0 END), 0) / 100.0 AS transaction_income_yuan,
    IFNULL(SUM(CASE WHEN t.amount < 0 THEN ABS(t.amount) ELSE 0 END), 0) / 100.0 AS transaction_expense_yuan,
    CASE 
        WHEN tw.total_income = IFNULL(SUM(CASE WHEN t.amount > 0 THEN t.amount ELSE 0 END), 0)
         AND tw.total_expense = IFNULL(SUM(CASE WHEN t.amount < 0 THEN ABS(t.amount) ELSE 0 END), 0)
        THEN '✅ 一致'
        ELSE '❌ 不一致'
    END AS check_result
FROM user_wallet tw
LEFT JOIN transaction t ON tw.user_id = t.user_id AND t.status = 1
GROUP BY tw.user_id
ORDER BY tw.user_id
LIMIT 10;

SELECT '========================================' AS message
UNION ALL SELECT '✅ Transaction表创建完成！' AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT CONCAT('表结构：13个字段') AS message
UNION ALL SELECT CONCAT('索引数量：8个') AS message
UNION ALL SELECT CONCAT('测试数据：5条') AS message
UNION ALL SELECT CONCAT('创建时间：', NOW()) AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT '📋 功能清单：' AS message
UNION ALL SELECT '✅ 完整交易流水记录' AS message
UNION ALL SELECT '✅ 多种交易类型支持' AS message
UNION ALL SELECT '✅ 余额快照（交易前/后）' AS message
UNION ALL SELECT '✅ 支付方式和流水号' AS message
UNION ALL SELECT '✅ 业务关联（订单/活动等）' AS message
UNION ALL SELECT '✅ 财务对账支持' AS message
UNION ALL SELECT '========================================' AS message;

-- ==========================================
-- 业务逻辑说明
-- ==========================================
-- 
-- 交易类型说明：
-- 1. recharge  - 充值（用户充值余额）
-- 2. consume   - 消费（购买服务/参加活动）
-- 3. refund    - 退款（订单取消/退款）
-- 4. withdraw  - 提现（余额提现到银行卡）
-- 5. income    - 收入（卖家收款/奖励发放）
-- 6. transfer  - 转账（用户之间转账）
-- 
-- 交易状态说明：
-- 0 - 处理中（第三方支付回调未到）
-- 1 - 成功（正常完成）
-- 2 - 失败（支付失败/余额不足等）
-- 3 - 已取消（用户主动取消）
-- 
-- 关联类型说明：
-- order    - 订单（service_order表）
-- activity - 活动（activity表）
-- reward   - 奖励（系统奖励/签到奖励）
-- transfer - 转账（用户之间）
-- 
-- 对账逻辑：
-- 1. 每日凌晨对账任务
-- 2. 比对user_wallet表和transaction表数据
-- 3. total_income = SUM(amount > 0)
-- 4. total_expense = SUM(amount < 0)
-- 5. balance = total_income - total_expense - frozen
-- 6. 不一致则告警并记录
-- 
-- 余额快照用途：
-- 1. 记录交易前后的余额变化
-- 2. 方便追踪余额异常
-- 3. 支持交易回滚
-- 4. 财务审计依据
-- ==========================================

-- ==========================================
-- 分表策略（未来优化）
-- ==========================================
-- 
-- 当交易量达到千万级时，可按月分表：
-- transaction_202501
-- transaction_202502
-- ...
-- 
-- 分表规则：
-- 1. 按created_at月份分表
-- 2. 热数据3个月（当前表）
-- 3. 历史数据归档（只读）
-- 4. 统计查询跨表聚合
-- ==========================================

-- ==========================================
-- 回滚脚本（如果需要）
-- ==========================================
-- DROP TABLE IF EXISTS transaction;

