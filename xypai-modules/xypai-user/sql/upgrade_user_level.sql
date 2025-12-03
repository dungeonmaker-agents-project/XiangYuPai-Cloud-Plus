-- =========================================================================================
-- XiangYuPai User Module - Upgrade Script
-- Version: 1.0.2
-- Date: 2025-12-01
-- Description: 添加用户等级相关字段，支持动态详情页显示用户等级标签
-- =========================================================================================
--
-- 🚀 使用指南:
-- - 新安装: 直接使用 xypai_user.sql (已包含所有字段)
-- - 升级已有数据库: 使用此脚本 upgrade_user_level.sql
--
-- =========================================================================================

USE `xypai_user`;

-- =========================================================================================
-- 1. 添加用户等级字段到 users 表
-- =========================================================================================
-- 用户等级：1-青铜 2-白银 3-黄金 4-铂金 5-钻石 6-大师 7-王者
ALTER TABLE `users`
    ADD COLUMN `level` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户等级: 1-青铜,2-白银,3-黄金,4-铂金,5-钻石,6-大师,7-王者' AFTER `bio`,
    ADD COLUMN `level_exp` INT(11) NOT NULL DEFAULT 0 COMMENT '当前等级经验值' AFTER `level`,
    ADD COLUMN `is_real_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否实名认证: 0-否,1-是' AFTER `level_exp`,
    ADD COLUMN `is_god_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否大神认证: 0-否,1-是' AFTER `is_real_verified`,
    ADD COLUMN `is_vip` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否VIP: 0-否,1-是' AFTER `is_god_verified`,
    ADD COLUMN `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP过期时间' AFTER `is_vip`;

-- 添加索引
ALTER TABLE `users`
    ADD KEY `idx_level` (`level`),
    ADD KEY `idx_is_vip` (`is_vip`);

-- =========================================================================================
-- 2. 更新测试用户的等级数据
-- =========================================================================================
UPDATE `users` SET `level` = 5, `is_real_verified` = 1, `is_god_verified` = 1, `is_vip` = 1 WHERE `user_id` = 1;
UPDATE `users` SET `level` = 4, `is_real_verified` = 1, `is_god_verified` = 0, `is_vip` = 1 WHERE `user_id` = 2;
UPDATE `users` SET `level` = 6, `is_real_verified` = 1, `is_god_verified` = 1, `is_vip` = 0 WHERE `user_id` = 3;
UPDATE `users` SET `level` = 7, `is_real_verified` = 1, `is_god_verified` = 1, `is_vip` = 1 WHERE `user_id` = 4;
UPDATE `users` SET `level` = 3, `is_real_verified` = 0, `is_god_verified` = 0, `is_vip` = 0 WHERE `user_id` = 5;

-- =========================================================================================
-- 完成
-- =========================================================================================
SELECT '✅ Upgrade completed: Added user level fields!' AS status;
