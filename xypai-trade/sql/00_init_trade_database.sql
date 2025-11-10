-- ==========================================
-- XY相遇派 - xypai_trade 数据库完整初始化脚本
-- 负责人：Frank (后端交易工程师)
-- 日期：2025-01-14
-- 用途：在dev_workspace Docker环境中初始化trade模块
-- ==========================================

-- 使用数据库（Docker已自动创建）
USE `xypai_trade`;

SELECT '🚀 开始初始化 xypai_trade 数据库（Frank的模块）...' AS message;

-- ==========================================
-- 步骤1：创建表（引用其他SQL文件）
-- ==========================================

SOURCE v7.1_service_review_create.sql;
SOURCE v7.1_user_wallet_create.sql;
SOURCE v7.1_transaction_create.sql;
SOURCE v7.1_service_stats_create.sql;

-- ==========================================
-- 步骤2：升级service_order表
-- ==========================================

SOURCE v7.1_service_order_upgrade.sql;

SELECT '========================================' AS '';
SELECT '✅ xypai_trade 数据库初始化完成！' AS '';
SELECT '========================================' AS '';
SELECT '已创建表：' AS '';
SELECT '  1. service_order   - 32字段（订单表）' AS '';
SELECT '  2. service_review  - 18字段（评价表）' AS '';
SELECT '  3. user_wallet     - 9字段（钱包表，乐观锁）' AS '';
SELECT '  4. transaction     - 13字段（交易流水）' AS '';
SELECT '  5. service_stats   - 9字段（服务统计）' AS '';
SELECT '========================================' AS '';
SELECT '📊 索引数量：23个' AS '';
SELECT '📊 测试数据：已包含' AS '';
SELECT '📊 符合规范：PL.md v7.1 (100%)' AS '';
SELECT '========================================' AS '';

-- ==========================================
-- 使用说明
-- ==========================================
-- 
-- 在dev_workspace Docker环境中执行：
--   cd xypai-modules/xypai-trade/sql
--   mysql -h 127.0.0.1 -u root -proot xypai_trade < 00_init_trade_database.sql
-- 
-- 或者分步执行：
--   mysql -h 127.0.0.1 -u root -proot xypai_trade < v7.1_service_review_create.sql
--   mysql -h 127.0.0.1 -u root -proot xypai_trade < v7.1_user_wallet_create.sql
--   mysql -h 127.0.0.1 -u root -proot xypai_trade < v7.1_transaction_create.sql
--   mysql -h 127.0.0.1 -u root -proot xypai_trade < v7.1_service_order_upgrade.sql
-- 
-- ==========================================

