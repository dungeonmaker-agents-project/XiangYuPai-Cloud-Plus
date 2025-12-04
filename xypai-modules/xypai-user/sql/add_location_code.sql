-- ========================================
-- Migration: Add location_code field to users table
-- ========================================
-- Version: 1.0.5
-- Date: 2025-12-03
-- Description: 添加 location_code 字段（城市编码），用于地区查询
--              与 residence 字段形成冗余设计，提高查询效率
-- ========================================

USE `xypai_user`;

-- 添加 location_code 字段（在 residence 字段之后）
ALTER TABLE `users`
    ADD COLUMN `location_code` VARCHAR(20) DEFAULT NULL COMMENT '常居地编码（用于地区查询）'
    AFTER `residence`;

-- 添加索引（用于地区查询优化）
ALTER TABLE `users`
    ADD INDEX `idx_location_code` (`location_code`);

-- ========================================
-- Verification
-- ========================================
SELECT '✅ Migration completed: location_code field added' AS status;
SHOW COLUMNS FROM `users` LIKE 'location_code';
