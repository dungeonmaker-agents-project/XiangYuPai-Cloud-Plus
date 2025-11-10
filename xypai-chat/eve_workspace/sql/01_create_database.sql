-- ==========================================
-- 01. 创建聊天模块数据库
-- ==========================================
-- 负责人：Eve
-- 模块：xypai-chat
-- 版本：v7.1
-- ==========================================

DROP DATABASE IF EXISTS `xypai_chat`;

CREATE DATABASE `xypai_chat` 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci
  COMMENT 'XY相遇派-聊天模块数据库';

USE `xypai_chat`;

SELECT '✅ 数据库xypai_chat创建成功' AS status;
SELECT DATABASE() AS current_database;

