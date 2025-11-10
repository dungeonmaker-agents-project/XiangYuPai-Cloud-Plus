-- ==========================================
-- XY相遇派 - UserWallet用户钱包表创建脚本 v7.1
-- 功能：钱包管理 + 余额扣款 + 乐观锁并发控制
-- 作者：Frank (后端交易工程师)
-- 日期：2025-01-14
-- 核心：乐观锁version字段，确保并发扣款安全
-- ==========================================

-- 检查并删除已存在的表
DROP TABLE IF EXISTS user_wallet;

SELECT 'Starting UserWallet table creation...' AS message;

-- ==========================================
-- 创建UserWallet表
-- ==========================================

CREATE TABLE user_wallet (
    -- 主键（用户ID作为主键，一对一关系）
    user_id BIGINT PRIMARY KEY COMMENT '用户ID（主键）',
    
    -- ⭐ 余额字段（3个字段）- 核心功能
    balance BIGINT NOT NULL DEFAULT 0 COMMENT '可用余额（分）',
    frozen BIGINT NOT NULL DEFAULT 0 COMMENT '冻结金额（分，订单待支付时冻结）',
    coin_balance BIGINT NOT NULL DEFAULT 0 COMMENT '金币余额（虚拟货币）',
    
    -- 累计统计（2个字段）
    total_income BIGINT NOT NULL DEFAULT 0 COMMENT '累计收入（分）',
    total_expense BIGINT NOT NULL DEFAULT 0 COMMENT '累计支出（分）',
    
    -- ⭐ 乐观锁版本号（1个字段）- 核心安全机制
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（每次更新+1，防止并发冲突）',
    
    -- 时间字段（2个字段）
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    -- ==========================================
    -- 索引设计
    -- ==========================================
    
    -- 更新时间索引（监控最近活跃用户）
    INDEX idx_updated (updated_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

SELECT '✅ UserWallet table created successfully!' AS message;

-- ==========================================
-- 插入测试数据
-- ==========================================

-- 测试用户钱包1：土豪用户
INSERT INTO user_wallet (user_id, balance, frozen, coin_balance, total_income, total_expense, version) 
VALUES (10001, 100000, 0, 5000, 150000, 50000, 0);  -- 余额1000元，金币5000，累计收入1500元

-- 测试用户钱包2：普通用户
INSERT INTO user_wallet (user_id, balance, frozen, coin_balance, total_income, total_expense, version) 
VALUES (10002, 5000, 1000, 500, 20000, 15000, 0);  -- 余额50元，冻结10元，金币500

-- 测试用户钱包3：新用户
INSERT INTO user_wallet (user_id, balance, frozen, coin_balance, total_income, total_expense, version) 
VALUES (10003, 0, 0, 100, 0, 0, 0);  -- 零余额，赠送100金币

SELECT '✅ Test data inserted successfully!' AS message;

-- ==========================================
-- 统计查询示例
-- ==========================================

-- 查询钱包总览
SELECT 
    COUNT(*) AS total_users,
    SUM(balance) / 100.0 AS total_balance_yuan,
    SUM(frozen) / 100.0 AS total_frozen_yuan,
    SUM(balance + frozen) / 100.0 AS total_assets_yuan,
    AVG(balance) / 100.0 AS avg_balance_yuan,
    MAX(balance) / 100.0 AS max_balance_yuan,
    MIN(balance) / 100.0 AS min_balance_yuan
FROM user_wallet;

-- 查询余额分布
SELECT 
    CASE 
        WHEN balance = 0 THEN '零余额'
        WHEN balance BETWEEN 1 AND 10000 THEN '1-100元'
        WHEN balance BETWEEN 10001 AND 50000 THEN '100-500元'
        WHEN balance BETWEEN 50001 AND 100000 THEN '500-1000元'
        ELSE '1000元以上'
    END AS balance_range,
    COUNT(*) AS user_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM user_wallet), 2) AS percentage
FROM user_wallet
GROUP BY balance_range
ORDER BY MIN(balance);

-- 查询最近活跃钱包
SELECT 
    user_id,
    balance / 100.0 AS balance_yuan,
    frozen / 100.0 AS frozen_yuan,
    coin_balance,
    total_income / 100.0 AS total_income_yuan,
    total_expense / 100.0 AS total_expense_yuan,
    version,
    updated_at
FROM user_wallet
ORDER BY updated_at DESC
LIMIT 10;

SELECT '========================================' AS message
UNION ALL SELECT '✅ UserWallet表创建完成！' AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT CONCAT('表结构：9个字段') AS message
UNION ALL SELECT CONCAT('索引数量：1个') AS message
UNION ALL SELECT CONCAT('测试数据：3条') AS message
UNION ALL SELECT CONCAT('创建时间：', NOW()) AS message
UNION ALL SELECT '========================================' AS message
UNION ALL SELECT '📋 功能清单：' AS message
UNION ALL SELECT '✅ 余额管理（可用余额+冻结金额）' AS message
UNION ALL SELECT '✅ 金币管理（虚拟货币）' AS message
UNION ALL SELECT '✅ 累计统计（收入+支出）' AS message
UNION ALL SELECT '✅ 乐观锁并发控制（version字段）' AS message
UNION ALL SELECT '✅ 余额冻结/解冻机制' AS message
UNION ALL SELECT '========================================' AS message;

-- ==========================================
-- 业务逻辑说明
-- ==========================================
-- 
-- 钱包操作规则：
-- 1. 充值：balance += 充值金额, total_income += 充值金额
-- 2. 下单冻结：balance -= 订单金额, frozen += 订单金额
-- 3. 支付成功：frozen -= 订单金额, total_expense += 订单金额
-- 4. 取消订单：frozen -= 订单金额, balance += 订单金额（解冻）
-- 5. 退款：balance += 退款金额（如果已支付）
-- 6. 提现：balance -= 提现金额（需要实名认证）
-- 7. 收入到账：balance += 收入金额, total_income += 收入金额
-- 
-- 乐观锁使用：
-- 1. 查询时获取version值
-- 2. 更新时WHERE user_id = ? AND version = ?
-- 3. 如果影响行数=0，说明并发冲突，抛出异常
-- 4. 更新成功后version自动+1
-- 
-- 并发场景：
-- 场景1：用户同时下两个订单
--   线程A：读version=0，扣款100元，更新WHERE version=0 ✅
--   线程B：读version=0，扣款200元，更新WHERE version=0 ❌（失败，version已变为1）
--   线程B重试：读version=1，扣款200元，更新WHERE version=1 ✅
-- 
-- 场景2：订单支付+退款同时发生
--   线程A：支付订单，frozen -= 100，更新version
--   线程B：退款订单，balance += 100，更新version
--   乐观锁保证只有一个操作成功，另一个重试
-- 
-- 安全机制：
-- 1. 余额不能为负（UPDATE前校验）
-- 2. 冻结金额不能超过可用余额
-- 3. 所有操作必须有Transaction记录
-- 4. 定时对账任务（每日凌晨）
-- ==========================================

-- ==========================================
-- 乐观锁扣款示例SQL
-- ==========================================

-- 步骤1：查询钱包（获取version）
-- SELECT balance, frozen, version FROM user_wallet WHERE user_id = 10001;
-- 假设查到：balance=100000, frozen=0, version=5

-- 步骤2：校验余额是否足够
-- IF balance < 5000 THEN RAISE ERROR '余额不足'

-- 步骤3：扣款（乐观锁更新）
-- UPDATE user_wallet 
-- SET balance = balance - 5000,
--     total_expense = total_expense + 5000,
--     version = version + 1,
--     updated_at = NOW()
-- WHERE user_id = 10001 AND version = 5;

-- 步骤4：检查影响行数
-- IF affected_rows = 0 THEN
--     -- 并发冲突，version已变化，需要重试
--     RAISE ERROR '并发更新失败，请重试'
-- ELSE
--     -- 扣款成功
--     COMMIT
-- END IF

-- ==========================================
-- 回滚脚本（如果需要）
-- ==========================================
-- DROP TABLE IF EXISTS user_wallet;

