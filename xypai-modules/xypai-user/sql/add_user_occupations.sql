-- ========================================
-- 用户职业扩展表 - 支持多职业选择
-- ========================================
-- Version: 1.0.0
-- Created: 2025-12-02
-- Description: 支持UI文档中的多职业标签选择功能
--              对应 个人主页-编辑_结构文档.md 中的 BasicInfoData.occupation
--
-- Usage:
--   mysql -u root -p xypai_user < add_user_occupations.sql
-- ========================================

USE `xypai_user`;

-- ========================================
-- 1. user_occupations - 用户职业表（支持多职业）
-- ========================================
CREATE TABLE IF NOT EXISTS `user_occupations` (
    -- Primary Key
    `occupation_id`     BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '职业记录ID（主键）',

    -- User Reference
    `user_id`           BIGINT(20)      NOT NULL COMMENT '用户ID',

    -- Occupation Info
    `occupation_name`   VARCHAR(50)     NOT NULL COMMENT '职业名称（1-30字符）',
    `sort_order`        INT(11)         NOT NULL DEFAULT 0 COMMENT '排序序号（越小越靠前）',

    -- Audit Fields
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '软删除',
    `version`           INT(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`occupation_id`),
    UNIQUE KEY `uk_user_occupation` (`user_id`, `occupation_name`, `deleted`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户职业表（支持多职业）';

-- ========================================
-- 2. 插入测试数据
-- ========================================
INSERT INTO `user_occupations` (`user_id`, `occupation_name`, `sort_order`) VALUES
-- 用户1的职业
(1, '程序员', 1),
(1, '游戏博主', 2),
-- 用户2的职业
(2, '设计师', 1),
(2, '陪玩达人', 2),
-- 用户3的职业
(3, '主播', 1),
-- 用户4的职业
(4, '电竞选手', 1),
(4, '游戏教练', 2),
-- 用户5的职业
(5, '学生', 1);

-- ========================================
-- 3. 验证
-- ========================================
SELECT '✅ user_occupations 表创建成功!' AS status;
SELECT COUNT(*) AS occupation_count FROM user_occupations;
