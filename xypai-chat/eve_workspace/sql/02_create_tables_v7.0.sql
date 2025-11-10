-- ==========================================
-- 02. 创建聊天模块表结构（v7.0基础版）
-- ==========================================
-- 负责人：Eve
-- 说明：这是v7.0的基础表结构，使用JSON字段
-- 后续：执行03_upgrade_to_v7.1.sql升级到v7.1
-- ==========================================

USE `xypai_chat`;

-- ========== 1. 聊天会话表（v7.0基础版）==========
CREATE TABLE IF NOT EXISTS `chat_conversation` (
    `id` BIGINT NOT NULL COMMENT '会话唯一ID(雪花ID)',
    `type` TINYINT NOT NULL COMMENT '会话类型(1=私聊,2=群聊,3=系统通知,4=订单会话)',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题(群聊名称,私聊可为空)',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID(群主/发起人)',
    `metadata` JSON DEFAULT NULL COMMENT '扩展信息JSON{description,avatar,settings...}',
    `status` TINYINT DEFAULT 1 COMMENT '会话状态(0=已解散,1=正常,2=已归档)',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='聊天会话表(v7.0)';

SELECT '✅ chat_conversation表创建成功（v7.0）' AS status;

-- ========== 2. 聊天消息表（v7.0基础版）==========
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL COMMENT '消息唯一ID',
    `conversation_id` BIGINT NOT NULL COMMENT '所属会话ID',
    `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID(NULL=系统消息)',
    `message_type` TINYINT NOT NULL COMMENT '消息类型(1=文本,2=图片,3=语音,4=视频,5=文件,6=系统通知,7=表情,8=位置)',
    `content` TEXT NOT NULL COMMENT '消息内容(文本/文件名/系统通知文本)',
    `media_data` JSON DEFAULT NULL COMMENT '媒体数据JSON{url,size,duration,thumbnail...}',
    `reply_to_id` BIGINT DEFAULT NULL COMMENT '回复的消息ID(引用回复)',
    `status` TINYINT DEFAULT 1 COMMENT '消息状态(0=已删除,1=正常,2=已撤回)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_reply_to_id` (`reply_to_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_chat_message_conversation` 
      FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_chat_message_reply` 
      FOREIGN KEY (`reply_to_id`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='聊天消息表(v7.0)';

SELECT '✅ chat_message表创建成功（v7.0）' AS status;

-- ========== 3. 会话参与者表（v7.0基础版）==========
CREATE TABLE IF NOT EXISTS `chat_participant` (
    `id` BIGINT NOT NULL COMMENT '参与记录ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '参与用户ID',
    `role` TINYINT DEFAULT 1 COMMENT '角色权限(1=成员,2=管理员,3=群主)',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `last_read_time` DATETIME DEFAULT NULL COMMENT '最后已读时间(未读消息计算)',
    `status` TINYINT DEFAULT 1 COMMENT '参与状态(0=已退出,1=正常,2=已禁言)',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`),
    KEY `idx_last_read_time` (`last_read_time`),
    UNIQUE KEY `uk_conversation_user` (`conversation_id`, `user_id`),
    CONSTRAINT `fk_chat_participant_conversation` 
      FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='会话参与者表(v7.0)';

SELECT '✅ chat_participant表创建成功（v7.0）' AS status;

-- ==========================================
-- v7.0基础表创建完成
-- ==========================================
SELECT 
  '✅ v7.0基础表创建完成' AS result,
  '3张表（chat_conversation, chat_message, chat_participant）' AS tables,
  '25个字段' AS total_fields,
  '下一步：执行03_upgrade_to_v7.1.sql升级到v7.1' AS next_step;

