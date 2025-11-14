-- ========================================
-- XiangYuPai App User Table
-- ========================================
-- Purpose: App-side user authentication and basic profile
-- Features:
--   - SMS-based login & registration
--   - Password login (optional, user sets after first login)
--   - No multi-tenancy (single app user pool)
-- ========================================

DROP TABLE IF EXISTS `app_user`;

CREATE TABLE `app_user` (
    -- ==================== Primary Key ====================
    `user_id`            BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '用户ID（自增主键）',

    -- ==================== Login Credentials ====================
    `mobile`             VARCHAR(20)     NOT NULL                   COMMENT '手机号（用于登录，唯一）',
    `country_code`       VARCHAR(10)     NOT NULL DEFAULT '+86'     COMMENT '国家区号（例如：+86, +852）',
    `password`           VARCHAR(100)    DEFAULT NULL               COMMENT '密码（BCrypt加密，可选，用户首次登录后设置）',

    -- ==================== Basic Profile ====================
    `nickname`           VARCHAR(50)     DEFAULT NULL               COMMENT '用户昵称（默认：手机号前3位+****+后4位）',
    `avatar`             VARCHAR(500)    DEFAULT NULL               COMMENT '头像URL',
    `gender`             TINYINT(1)      DEFAULT 0                  COMMENT '性别：0=未设置，1=男，2=女',

    -- ==================== Status Fields ====================
    `status`             TINYINT(1)      NOT NULL DEFAULT 1         COMMENT '账号状态：0=禁用，1=正常，2=锁定',
    `is_new_user`        TINYINT(1)      NOT NULL DEFAULT 1         COMMENT '是否新用户：0=否，1=是（首次登录后完善资料后设为0）',

    -- ==================== Login Info ====================
    `last_login_ip`      VARCHAR(128)    DEFAULT NULL               COMMENT '最后登录IP',
    `last_login_time`    DATETIME        DEFAULT NULL               COMMENT '最后登录时间',
    `login_count`        INT(11)         NOT NULL DEFAULT 0         COMMENT '登录次数',

    -- ==================== Audit Fields ====================
    `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间（注册时间）',
    `updated_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`         DATETIME        DEFAULT NULL               COMMENT '软删除时间（NULL=未删除）',

    -- ==================== Version Lock ====================
    `version`            INT(11)         NOT NULL DEFAULT 0         COMMENT '乐观锁版本号',

    -- ==================== Primary & Indexes ====================
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_mobile_country` (`mobile`, `country_code`, `deleted_at`),  -- 手机号+区号唯一（支持软删除）
    KEY `idx_mobile` (`mobile`),                                              -- 手机号查询索引
    KEY `idx_status` (`status`),                                              -- 状态查询索引
    KEY `idx_created_at` (`created_at`)                                       -- 注册时间索引（统计用）

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='App用户表（用于登录认证）';

-- ========================================
-- Initial Data (Optional - For Testing)
-- ========================================
-- INSERT INTO `app_user` (`mobile`, `country_code`, `nickname`, `status`, `is_new_user`)
-- VALUES
-- ('13800138000', '+86', '测试用户', 1, 0);

-- ========================================
-- Notes
-- ========================================
-- 1. 手机号登录流程：
--    - 用户输入手机号 + 验证码
--    - 后端验证验证码成功后：
--      a. 查询 app_user 表是否存在该手机号
--      b. 不存在 → 自动注册（INSERT）→ 返回 isNewUser=true
--      c. 存在   → 登录成功 → 返回 isNewUser=false
--
-- 2. 密码登录流程：
--    - 用户输入手机号 + 密码
--    - 后端验证密码（BCrypt.checkpw）
--    - 登录成功
--
-- 3. 忘记密码流程：
--    - 用户输入手机号 + 验证码 → 验证成功
--    - 用户设置新密码 → 更新 password 字段
--
-- 4. 软删除：
--    - deleted_at 不为 NULL 表示已删除
--    - 唯一索引 uk_mobile_country 包含 deleted_at，支持删除后重新注册
--
-- 5. 密码策略：
--    - 长度：6-20位字符
--    - 限制：不可纯数字
--    - 加密：BCrypt
--    - 可选：用户首次登录无需密码（短信验证码即可），后续在个人中心设置密码
