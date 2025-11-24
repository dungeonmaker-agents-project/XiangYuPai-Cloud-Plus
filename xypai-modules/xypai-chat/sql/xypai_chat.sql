-- ============================================
-- XiangYuPai Chat Service Database Schema
-- Database: xypai_chat
-- Version: 1.0.0
-- Created: 2025-01-14
-- ============================================

CREATE DATABASE IF NOT EXISTS `xypai_chat` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xypai_chat`;

-- ============================================
-- Table: conversation
-- Description: 会话表 - 用户私信会话记录
-- ============================================
CREATE TABLE IF NOT EXISTS `conversation` (
    `id` BIGINT(20) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `other_user_id` BIGINT(20) NOT NULL COMMENT '对方用户ID',
    `last_message` TEXT COMMENT '最后一条消息内容',
    `last_message_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
    `unread_count` INT(11) NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除: 0=否, 1=是',
    `deleted_at` DATETIME NULL COMMENT '删除时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_deleted` (`user_id`, `deleted`),
    KEY `idx_user_other_deleted` (`user_id`, `other_user_id`, `deleted`),
    KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- ============================================
-- Table: message
-- Description: 消息表 - 聊天消息记录
-- ============================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT(20) NOT NULL COMMENT '消息ID',
    `conversation_id` BIGINT(20) NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT(20) NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型: text/image/voice/video',
    `content` TEXT COMMENT '消息内容(文字消息)',
    `media_url` VARCHAR(500) NULL COMMENT '媒体URL(图片/语音/视频)',
    `thumbnail_url` VARCHAR(500) NULL COMMENT '缩略图URL(视频消息)',
    `duration` INT(11) NULL COMMENT '时长(语音/视频,秒)',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '消息状态: 0=发送中, 1=已送达, 2=已读, 3=失败',
    `is_recalled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已撤回: 0=否, 1=是',
    `recalled_at` DATETIME NULL COMMENT '撤回时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除: 0=否, 1=是',
    `deleted_at` DATETIME NULL COMMENT '删除时间',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_deleted_created` (`conversation_id`, `deleted`, `create_time`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================
-- Test Data (Optional - for development)
-- ============================================

-- Insert test conversations
INSERT INTO `conversation` (`id`, `user_id`, `other_user_id`, `last_message`, `last_message_time`, `unread_count`)
VALUES
    (1, 1, 2, '你好，最近怎么样？', NOW(), 2),
    (2, 2, 1, '你好，最近怎么样？', NOW(), 0);

-- Insert test messages
INSERT INTO `message` (`id`, `conversation_id`, `sender_id`, `receiver_id`, `message_type`, `content`, `status`)
VALUES
    (1, 1, 1, 2, 'text', '你好，最近怎么样？', 1),
    (2, 1, 2, 1, 'text', '挺好的，你呢？', 2),
    (3, 1, 1, 2, 'text', '我也不错，有空一起吃饭吗？', 1);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- 会话表索引已在CREATE TABLE中定义
-- 消息表索引已在CREATE TABLE中定义

-- ============================================
-- Additional Notes
-- ============================================

/*
Performance Considerations:
1. conversation表使用复合索引(user_id, deleted)优化会话列表查询
2. message表使用复合索引(conversation_id, deleted, create_time)优化消息列表查询
3. 使用软删除机制,保留deleted字段
4. 使用乐观锁(version)防止并发更新冲突
5. 消息表可考虑按会话ID进行分表,提升高并发场景性能

Cache Strategy:
1. 未读消息数: Redis缓存3分钟
2. 会话列表: Redis缓存5分钟
3. 在线状态: Redis缓存5分钟(心跳刷新)

Scaling Considerations:
1. 消息表数据量大时,考虑按时间范围分表
2. 热数据(最近7天)缓存到Redis
3. 历史消息归档到历史库
4. 使用读写分离提升查询性能
*/
