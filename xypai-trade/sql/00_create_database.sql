-- ==========================================
-- XY相遇派 - 创建 xypai_trade 数据库
-- 负责人：Frank (后端交易工程师)
-- 日期：2025-10-21
-- ==========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `xypai_trade` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `xypai_trade`;

SELECT '✅ 数据库 xypai_trade 创建成功！' AS message;
SELECT '📌 字符集：utf8mb4' AS '';
SELECT '📌 排序规则：utf8mb4_unicode_ci' AS '';
SELECT '' AS '';
SELECT '下一步：执行表结构创建脚本' AS '';
SELECT '  mysql -u root -proot xypai_trade < v7.1_service_order_upgrade.sql' AS '';
SELECT '  mysql -u root -proot xypai_trade < v7.1_service_review_create.sql' AS '';
SELECT '  mysql -u root -proot xypai_trade < v7.1_user_wallet_create.sql' AS '';
SELECT '  mysql -u root -proot xypai_trade < v7.1_transaction_create.sql' AS '';
SELECT '  mysql -u root -proot xypai_trade < v7.1_service_stats_create.sql' AS '';

