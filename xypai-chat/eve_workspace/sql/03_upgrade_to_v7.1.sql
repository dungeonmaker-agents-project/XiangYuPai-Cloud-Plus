-- ==========================================
-- 03. 升级到v7.1（字段展开+功能增强）
-- ==========================================
-- 负责人：Eve
-- 升级内容：
-- 1. ChatConversation +7字段（metadata展开）
-- 2. ChatMessage +13字段（media_data展开+消息管理）
-- 3. ChatParticipant +6字段（精确已读+个性化设置）
-- 4. MessageSettings 新表20字段
-- 5. TypingStatus 新表7字段
-- ==========================================

USE `xypai_chat`;

SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. ChatConversation表升级 (+7字段)
-- ==========================================

ALTER TABLE `chat_conversation` 
  ADD COLUMN `avatar_url` VARCHAR(500) COMMENT '会话头像URL' AFTER `creator_id`,
  ADD COLUMN `description` TEXT COMMENT '会话描述(群公告)' AFTER `avatar_url`,
  ADD COLUMN `order_id` BIGINT COMMENT '关联订单ID' AFTER `description`,
  ADD COLUMN `last_message_id` BIGINT COMMENT '最后一条消息ID(冗余优化)' AFTER `order_id`,
  ADD COLUMN `last_message_time` DATETIME COMMENT '最后消息时间' AFTER `last_message_id`,
  ADD COLUMN `total_message_count` INT DEFAULT 0 COMMENT '消息总数' AFTER `last_message_time`,
  ADD COLUMN `member_count` INT DEFAULT 0 COMMENT '成员数量' AFTER `total_message_count`,
  ADD COLUMN `deleted_at` DATETIME COMMENT '软删除时间' AFTER `version`;

-- 数据迁移
UPDATE `chat_conversation` 
SET 
  `avatar_url` = JSON_UNQUOTE(JSON_EXTRACT(`metadata`, '$.avatar')),
  `description` = JSON_UNQUOTE(JSON_EXTRACT(`metadata`, '$.description')),
  `order_id` = JSON_EXTRACT(`metadata`, '$.orderId')
WHERE `metadata` IS NOT NULL;

SELECT '✅ ChatConversation表升级完成：+7字段' AS status;

-- ==========================================
-- 2. ChatMessage表升级 (+13字段)
-- ==========================================

ALTER TABLE `chat_message`
  -- 媒体字段展开
  ADD COLUMN `media_url` VARCHAR(500) COMMENT '媒体文件URL' AFTER `content`,
  ADD COLUMN `thumbnail_url` VARCHAR(500) COMMENT '缩略图URL' AFTER `media_url`,
  ADD COLUMN `media_size` BIGINT COMMENT '文件大小(字节)' AFTER `thumbnail_url`,
  ADD COLUMN `media_width` INT COMMENT '媒体宽度' AFTER `media_size`,
  ADD COLUMN `media_height` INT COMMENT '媒体高度' AFTER `media_width`,
  ADD COLUMN `media_duration` INT COMMENT '媒体时长(秒)' AFTER `media_height`,
  ADD COLUMN `media_caption` VARCHAR(500) COMMENT '媒体配文' AFTER `media_duration`,
  
  -- 消息管理字段（核心）
  ADD COLUMN `client_id` VARCHAR(100) UNIQUE COMMENT '客户端消息ID(去重)' AFTER `reply_to_id`,
  ADD COLUMN `sequence_id` BIGINT COMMENT '消息序列号(有序性)' AFTER `client_id`,
  ADD COLUMN `delivery_status` TINYINT DEFAULT 0 COMMENT '投递状态(0=发送中,1=已发送,2=已送达,3=已读,4=失败)' AFTER `sequence_id`,
  
  -- 群聊功能增强
  ADD COLUMN `read_count` INT DEFAULT 0 COMMENT '已读人数' AFTER `delivery_status`,
  ADD COLUMN `like_count` INT DEFAULT 0 COMMENT '点赞数量' AFTER `read_count`,
  ADD COLUMN `recalled_by` BIGINT COMMENT '撤回操作人ID' AFTER `like_count`,
  
  -- 时间分离
  ADD COLUMN `send_time` DATETIME COMMENT '客户端发送时间' AFTER `recalled_by`,
  ADD COLUMN `server_time` DATETIME COMMENT '服务器接收时间' AFTER `send_time`,
  ADD COLUMN `deleted_at` DATETIME COMMENT '软删除时间' AFTER `status`;

-- 数据迁移
UPDATE `chat_message`
SET 
  `media_url` = JSON_UNQUOTE(JSON_EXTRACT(`media_data`, '$.url')),
  `thumbnail_url` = JSON_UNQUOTE(JSON_EXTRACT(`media_data`, '$.thumbnail')),
  `media_size` = JSON_EXTRACT(`media_data`, '$.size'),
  `media_duration` = JSON_EXTRACT(`media_data`, '$.duration'),
  `media_width` = JSON_EXTRACT(`media_data`, '$.width'),
  `media_height` = JSON_EXTRACT(`media_data`, '$.height'),
  `send_time` = `created_at`,
  `server_time` = `created_at`
WHERE `media_data` IS NOT NULL;

SELECT '✅ ChatMessage表升级完成：+13字段（核心功能）' AS status;

-- ==========================================
-- 3. ChatParticipant表升级 (+6字段)
-- ==========================================

ALTER TABLE `chat_participant`
  ADD COLUMN `last_read_message_id` BIGINT COMMENT '最后已读消息ID(精确定位)' AFTER `last_read_time`,
  ADD COLUMN `unread_count` INT DEFAULT 0 COMMENT '未读消息数量(冗余优化)' AFTER `last_read_message_id`,
  ADD COLUMN `is_pinned` BOOLEAN DEFAULT FALSE COMMENT '是否置顶' AFTER `unread_count`,
  ADD COLUMN `is_muted` BOOLEAN DEFAULT FALSE COMMENT '是否免打扰' AFTER `is_pinned`,
  ADD COLUMN `mute_until` DATETIME COMMENT '免打扰截止时间' AFTER `is_muted`,
  ADD COLUMN `nickname` VARCHAR(100) COMMENT '群昵称' AFTER `mute_until`,
  ADD COLUMN `leave_time` DATETIME COMMENT '退出时间' AFTER `status`;

SELECT '✅ ChatParticipant表升级完成：+6字段（个性化设置）' AS status;

-- ==========================================
-- 4. 创建MessageSettings表（v7.1新表）
-- ==========================================

CREATE TABLE IF NOT EXISTS `message_settings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置记录ID',
  `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID(唯一)',
  
  -- 推送设置(7字段)
  `push_enabled` BOOLEAN DEFAULT TRUE COMMENT '推送总开关',
  `push_sound_enabled` BOOLEAN DEFAULT TRUE COMMENT '推送声音',
  `push_vibrate_enabled` BOOLEAN DEFAULT TRUE COMMENT '推送震动',
  `push_preview_enabled` BOOLEAN DEFAULT TRUE COMMENT '内容预览',
  `push_start_time` VARCHAR(10) DEFAULT '08:00' COMMENT '推送时段开始',
  `push_end_time` VARCHAR(10) DEFAULT '22:00' COMMENT '推送时段结束',
  
  -- 分类推送(4字段)
  `push_like_enabled` BOOLEAN DEFAULT TRUE COMMENT '点赞消息推送',
  `push_comment_enabled` BOOLEAN DEFAULT TRUE COMMENT '评论消息推送',
  `push_follow_enabled` BOOLEAN DEFAULT TRUE COMMENT '关注消息推送',
  `push_system_enabled` BOOLEAN DEFAULT TRUE COMMENT '系统通知推送',
  
  -- 隐私设置(2字段)
  `who_can_message` TINYINT DEFAULT 0 COMMENT '谁可以发消息(0=所有人,1=我关注的,2=互相关注,3=不允许)',
  `who_can_add_friend` TINYINT DEFAULT 0 COMMENT '谁可以添加好友(0=所有人,1=需要验证,2=不允许)',
  
  -- 消息设置(2字段)
  `message_read_receipt` BOOLEAN DEFAULT TRUE COMMENT '消息已读回执',
  `online_status_visible` BOOLEAN DEFAULT TRUE COMMENT '在线状态可见',
  
  -- 自动下载(3字段)
  `auto_download_image` TINYINT DEFAULT 2 COMMENT '自动下载图片(0=永不,1=仅WIFI,2=始终)',
  `auto_download_video` TINYINT DEFAULT 1 COMMENT '自动下载视频(0=永不,1=仅WIFI,2=始终)',
  `auto_play_voice` BOOLEAN DEFAULT FALSE COMMENT '自动播放语音',
  
  -- 其他(1字段)
  `message_retention_days` INT DEFAULT 0 COMMENT '消息保存天数(0=永久)',
  
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='用户消息设置表(v7.1新增)';

SELECT '✅ message_settings表创建完成：20字段新表' AS status;

-- ==========================================
-- 5. 创建TypingStatus表（v7.1新表）
-- ==========================================

CREATE TABLE IF NOT EXISTS `typing_status` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态记录ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '正在输入的用户ID',
  `is_typing` BOOLEAN DEFAULT TRUE COMMENT '是否正在输入',
  `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始输入时间',
  `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `expire_time` DATETIME COMMENT '过期时间(10秒)',
  
  UNIQUE KEY `uk_conversation_user` (`conversation_id`, `user_id`),
  KEY `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='正在输入状态表(v7.1新增，建议用Redis)';

SELECT '✅ typing_status表创建完成：7字段新表' AS status;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- v7.1表结构创建完成
-- ==========================================
SELECT 
  '✅ v7.1表结构创建完成' AS result,
  '5张表' AS total_tables,
  '78个字段' AS total_fields;

SELECT 
  TABLE_NAME AS table_name,
  TABLE_COMMENT AS comment,
  (SELECT COUNT(*) FROM information_schema.COLUMNS 
   WHERE TABLE_SCHEMA = 'xypai_chat' AND TABLE_NAME = t.TABLE_NAME) AS field_count
FROM information_schema.TABLES t
WHERE TABLE_SCHEMA = 'xypai_chat'
ORDER BY TABLE_NAME;

