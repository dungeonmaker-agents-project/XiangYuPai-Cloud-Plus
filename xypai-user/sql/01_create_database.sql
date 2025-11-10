-- ==========================================
-- xypai-user 数据库初始化脚本
-- ==========================================
-- 负责人: Bob
-- 日期: 2025-10-20
-- 版本: v7.1
-- 说明: 创建xypai_user数据库
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `xypai_user` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

-- 选择数据库
USE `xypai_user`;

-- 设置时区
SET time_zone = '+08:00';

-- 显示创建成功信息
SELECT '✅ 数据库 xypai_user 创建成功' AS message,
       'CHARACTER SET: utf8mb4, COLLATE: utf8mb4_general_ci' AS config;

