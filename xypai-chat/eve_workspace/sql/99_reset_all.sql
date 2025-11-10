-- ==========================================
-- 99. 重置聊天模块所有数据
-- ==========================================
-- ⚠️ 警告：此脚本会删除所有数据！
-- 用途：开发测试环境重置
-- 生产环境：禁止使用！
-- ==========================================

USE `xypai_chat`;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE `typing_status`;
TRUNCATE TABLE `message_settings`;
TRUNCATE TABLE `chat_participant`;
TRUNCATE TABLE `chat_message`;
TRUNCATE TABLE `chat_conversation`;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '⚠️ 所有表数据已清空' AS warning;

-- 重置自增ID
ALTER TABLE `chat_conversation` AUTO_INCREMENT = 5001;
ALTER TABLE `chat_message` AUTO_INCREMENT = 6001;
ALTER TABLE `chat_participant` AUTO_INCREMENT = 7001;
ALTER TABLE `message_settings` AUTO_INCREMENT = 1;
ALTER TABLE `typing_status` AUTO_INCREMENT = 1;

SELECT '✅ 自增ID已重置' AS status;

-- 可选：重新初始化测试数据
-- SOURCE 05_init_test_data.sql;

SELECT 
  '✅ 重置完成' AS result,
  '所有数据已清空' AS data_status,
  '自增ID已重置' AS id_status,
  '执行05_init_test_data.sql可重新初始化测试数据' AS next_step;

