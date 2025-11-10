-- ==========================================
-- xypai-user 模块 - 创建表结构（修复版）
-- ==========================================
-- 负责人: Bob
-- 日期: 2025-10-20
-- 版本: v7.1
-- 表数量: 8张
-- 字段数量: 120个
-- 参考: PL.md v7.1完整设计
-- 修复: AUTO_INCREMENT + created_at + 字段补全
-- ==========================================

USE `xypai_user`;

-- ==========================================
-- 表1: user（用户基础表 - 19字段）
-- ==========================================
-- 🔧 修复：id使用AUTO_INCREMENT（雪花ID可选）
-- ==========================================

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识(雪花ID)',
  `username` VARCHAR(50) NOT NULL COMMENT '登录用户名(唯一)',
  `mobile` VARCHAR(20) NOT NULL COMMENT '手机号(唯一,主要登录凭证)',
  `region_code` VARCHAR(10) DEFAULT '+86' COMMENT '地区代码(如+86)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱(唯一,辅助登录凭证)',
  `password` VARCHAR(100) NOT NULL COMMENT '密码哈希值(BCrypt加密)',
  `password_salt` VARCHAR(100) DEFAULT NULL COMMENT '密码盐值',
  `password_updated_at` DATETIME DEFAULT NULL COMMENT '密码最后更新时间',
  `status` TINYINT DEFAULT 1 COMMENT '用户状态(0=禁用,1=正常,2=冻结,3=待激活)',
  `login_fail_count` INT DEFAULT 0 COMMENT '登录失败次数(防暴力破解)',
  `login_locked_until` DATETIME DEFAULT NULL COMMENT '账户锁定截止时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_device_id` VARCHAR(100) DEFAULT NULL COMMENT '最后登录设备ID',
  `is_two_factor_enabled` BOOLEAN DEFAULT FALSE COMMENT '是否启用双因子认证',
  `two_factor_secret` VARCHAR(100) DEFAULT NULL COMMENT '双因子认证密钥(TOTP)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='用户基础信息表';

-- ==========================================
-- 表2: user_profile（用户资料表 - 34字段）
-- ==========================================
-- 🔧 说明：符合PL.md v7.1设计，删除了冗余统计字段
-- ==========================================

CREATE TABLE IF NOT EXISTS `user_profile` (
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称(1-20字符)',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL(CDN地址)',
  `avatar_thumbnail` VARCHAR(500) DEFAULT NULL COMMENT '头像缩略图(列表显示)',
  `background_image` VARCHAR(500) DEFAULT NULL COMMENT '个人主页背景图',
  `gender` TINYINT DEFAULT 0 COMMENT '性别(0=未设置,1=男,2=女,3=其他)',
  `birthday` DATE DEFAULT NULL COMMENT '生日(YYYY-MM-DD)',
  `age` INT DEFAULT NULL COMMENT '年龄(根据生日自动计算)',
  `city_id` BIGINT DEFAULT NULL COMMENT '所在城市ID',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '城市位置信息(如"广东 深圳")',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `ip_location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地(显示用)',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介(0-30字符)',
  `height` INT DEFAULT NULL COMMENT '身高(cm,140-200)',
  `weight` INT DEFAULT NULL COMMENT '体重(kg,30-150)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名(实名认证)',
  `id_card_encrypted` VARCHAR(200) DEFAULT NULL COMMENT '身份证号(AES-256加密)',
  `wechat` VARCHAR(50) DEFAULT NULL COMMENT '微信号(6-20位)',
  `wechat_unlock_condition` TINYINT DEFAULT 0 COMMENT '微信解锁条件(0=公开,1=关注可见,2=付费,3=私密)',
  `is_real_verified` BOOLEAN DEFAULT FALSE COMMENT '是否实名认证',
  `is_god_verified` BOOLEAN DEFAULT FALSE COMMENT '是否大神认证',
  `is_activity_expert` BOOLEAN DEFAULT FALSE COMMENT '是否组局达人认证',
  `is_vip` BOOLEAN DEFAULT FALSE COMMENT '是否VIP用户',
  `is_popular` BOOLEAN DEFAULT FALSE COMMENT '是否人气用户',
  `vip_level` TINYINT DEFAULT 0 COMMENT 'VIP等级(1-5级)',
  `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP过期时间',
  `online_status` TINYINT DEFAULT 0 COMMENT '在线状态(0=离线,1=在线,2=忙碌,3=隐身)',
  `last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线时间',
  `profile_completeness` INT DEFAULT 0 COMMENT '资料完整度(0-100百分比)',
  `last_edit_time` DATETIME DEFAULT NULL COMMENT '最后编辑资料时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户资料扩展表(支持个人主页和编辑功能)';

-- ==========================================
-- 表3: user_stats（用户统计表 - 14字段）⭐
-- ==========================================
-- 🔧 核心：解决UserProfile冗余字段问题
-- 🔧 设计：Redis主存储 + MySQL持久化
-- ==========================================

CREATE TABLE IF NOT EXISTS `user_stats` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID(主键)',
  `follower_count` INT DEFAULT 0 COMMENT '粉丝数量',
  `following_count` INT DEFAULT 0 COMMENT '关注数量',
  `content_count` INT DEFAULT 0 COMMENT '发布内容数量',
  `total_like_count` INT DEFAULT 0 COMMENT '获赞总数',
  `total_collect_count` INT DEFAULT 0 COMMENT '被收藏总数',
  `activity_organizer_count` INT DEFAULT 0 COMMENT '发起组局总数',
  `activity_participant_count` INT DEFAULT 0 COMMENT '参与组局总数',
  `activity_success_count` INT DEFAULT 0 COMMENT '成功完成组局次数',
  `activity_cancel_count` INT DEFAULT 0 COMMENT '取消组局次数',
  `activity_organizer_score` DECIMAL(3,2) DEFAULT 0.00 COMMENT '组局信誉评分(5分制)',
  `activity_success_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '组局成功率(百分比)',
  `last_sync_time` DATETIME DEFAULT NULL COMMENT '最后同步时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户统计表(Redis缓存+异步同步)';

-- ==========================================
-- 表4: occupation_dict（职业字典表 - 7字段）⭐
-- ==========================================
-- 🔧 核心：统一管理职业枚举值
-- 🔧 设计：支持多语言扩展
-- ==========================================

CREATE TABLE IF NOT EXISTS `occupation_dict` (
  `code` VARCHAR(50) NOT NULL COMMENT '职业编码(主键,如model/student)',
  `name` VARCHAR(50) NOT NULL COMMENT '职业名称(如"模特"/"学生")',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '职业分类(如"艺术"/"教育")',
  `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标URL(前端展示)',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序(热门优先)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`code`),
  KEY `idx_category_sort` (`category`, `sort_order`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='职业字典表(统一管理职业类型)';

-- ==========================================
-- 表5: user_occupation（用户职业关联表 - 5字段）⭐
-- ==========================================
-- 🔧 核心：替代occupation_tags字符串字段
-- 🔧 设计：符合第一范式，支持高效查询
-- ==========================================

CREATE TABLE IF NOT EXISTS `user_occupation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `occupation_code` VARCHAR(50) NOT NULL COMMENT '职业编码(关联occupation_dict)',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序(用户自定义显示顺序)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_occupation` (`user_id`, `occupation_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_occupation_code` (`occupation_code`),
  CONSTRAINT `fk_user_occupation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_occupation_dict` FOREIGN KEY (`occupation_code`) REFERENCES `occupation_dict` (`code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户职业关联表(最多5个职业标签)';

-- ==========================================
-- 表6: user_wallet（用户钱包表 - 8字段）
-- ==========================================
-- 🔧 修复：补充created_at字段
-- ==========================================

CREATE TABLE IF NOT EXISTS `user_wallet` (
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `balance` BIGINT DEFAULT 0 COMMENT '可用余额(分)',
  `frozen` BIGINT DEFAULT 0 COMMENT '冻结金额(分)',
  `coin_balance` BIGINT DEFAULT 0 COMMENT '金币余额',
  `total_income` BIGINT DEFAULT 0 COMMENT '累计收入(分)',
  `total_expense` BIGINT DEFAULT 0 COMMENT '累计支出(分)',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  KEY `idx_balance` (`balance`),
  CONSTRAINT `fk_user_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户钱包表(支持余额和金币)';

-- ==========================================
-- 表7: transaction（交易流水表 - 11字段）
-- ==========================================
-- 🔧 修复：id使用AUTO_INCREMENT
-- ==========================================

CREATE TABLE IF NOT EXISTS `transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` BIGINT NOT NULL COMMENT '交易金额(分,正负表示收支)',
  `type` VARCHAR(20) NOT NULL COMMENT '交易类型(recharge/consume/refund/withdraw)',
  `ref_type` VARCHAR(20) DEFAULT NULL COMMENT '关联类型(order/activity/reward)',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `status` TINYINT DEFAULT 0 COMMENT '交易状态(0=处理中,1=成功,2=失败)',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式(wechat/alipay/balance)',
  `payment_no` VARCHAR(100) DEFAULT NULL COMMENT '支付流水号',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '交易描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`, `type`, `created_at`),
  KEY `idx_ref` (`ref_type`, `ref_id`),
  KEY `idx_status` (`status`, `created_at`),
  KEY `idx_payment_no` (`payment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='统一交易流水表';

-- ==========================================
-- 表8: user_relation（用户关系表 - 7字段）
-- ==========================================
-- 🔧 修复：id使用AUTO_INCREMENT
-- ==========================================

CREATE TABLE IF NOT EXISTS `user_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系记录ID',
  `user_id` BIGINT NOT NULL COMMENT '发起用户ID',
  `target_id` BIGINT NOT NULL COMMENT '目标用户ID',
  `type` INT NOT NULL COMMENT '关系类型(1=关注,2=拉黑,3=好友,4=特别关注)',
  `status` TINYINT DEFAULT 1 COMMENT '关系状态(0=已取消,1=正常)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立关系时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `type`),
  KEY `idx_user_type` (`user_id`, `type`, `status`),
  KEY `idx_target_type` (`target_id`, `type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户关系表(关注/拉黑/好友)';

-- ==========================================
-- 创建完成提示
-- ==========================================

SELECT '✅ xypai-user模块：8张表创建成功' AS status,
       '用户基础表 + 资料表 + 统计表 + 职业系统 + 钱包系统' AS tables,
       '已修复：AUTO_INCREMENT + created_at字段' AS fixes;

