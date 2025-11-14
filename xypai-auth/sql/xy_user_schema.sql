-- ==========================================
-- 🎯 XyPai Auth Module - Database Schema
-- ==========================================
-- Purpose: Reference schema for xypai-auth module
-- Database: xypai_user
-- Table: xy_user
-- Version: 1.0
-- Date: 2025-11-12
-- ==========================================

USE `xypai_user`;

-- This file is for REFERENCE ONLY
-- The actual table should already exist from: 
-- xypai-user/sql/00_SINGLE_TABLE_SIMPLIFIED.sql

-- Table structure (33 fields)
/*
CREATE TABLE IF NOT EXISTS `xy_user` (
  -- Core Fields (25)
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `tenant_id` VARCHAR(20) NOT NULL DEFAULT '000000' COMMENT '租户编号',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '简介',
  
  -- Certification Fields (8)
  `is_real_verified` TINYINT(1) DEFAULT 0 COMMENT '实名认证',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card_encrypted` VARCHAR(255) DEFAULT NULL COMMENT '身份证号(加密)',
  `id_card_last4` CHAR(4) DEFAULT NULL COMMENT '身份证后4位',
  `is_god_verified` TINYINT(1) DEFAULT 0 COMMENT '大神认证',
  `is_activity_expert` TINYINT(1) DEFAULT 0 COMMENT '组局达人',
  `is_vip` TINYINT(1) DEFAULT 0 COMMENT 'VIP用户',
  `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP过期时间',
  
  -- Status Fields (3)
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '账号状态',
  `online_status` TINYINT UNSIGNED DEFAULT 0 COMMENT '在线状态',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  
  -- Audit Fields (4)
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
  `version` INT UNSIGNED DEFAULT 0 COMMENT '乐观锁版本',
  
  -- JSON Fields (8)
  `profile_json` JSON DEFAULT NULL COMMENT '个人资料JSON',
  `certification_json` JSON DEFAULT NULL COMMENT '认证信息JSON',
  `stats_json` JSON DEFAULT NULL COMMENT '统计数据JSON',
  `wallet_json` JSON DEFAULT NULL COMMENT '钱包信息JSON',
  `settings_json` JSON DEFAULT NULL COMMENT '设置信息JSON',
  `social_json` JSON DEFAULT NULL COMMENT '社交信息JSON',
  `preferences_json` JSON DEFAULT NULL COMMENT '用户偏好JSON',
  `custom_json` JSON DEFAULT NULL COMMENT '自定义字段JSON',
  
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='XyPai用户核心表（单表设计）';
*/

-- ==========================================
-- Authentication Requirements
-- ==========================================
-- This module requires these fields for authentication:
-- 1. username - for password/email/sms login
-- 2. password - BCrypt hashed (for password auth)
-- 3. email - for email verification login
-- 4. mobile - for SMS code login
-- 5. status - account enabled/disabled
-- 6. tenant_id - multi-tenant support
-- 7. social_json - for social OAuth login (stores openid/unionid)
-- ==========================================

