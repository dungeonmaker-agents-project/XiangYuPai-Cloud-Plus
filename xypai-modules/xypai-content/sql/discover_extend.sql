-- =========================================================================================
-- XiangYuPai Content Module - Discover Page Database Extension
-- Database: xypai_content
-- Version: 1.1.0
-- Created: 2025-12-01
-- Description: 扩展 feed 表，添加发现页所需的媒体详情字段
-- =========================================================================================

USE `xypai_content`;

-- =========================================================================================
-- 1. 扩展 feed 表 - 添加媒体详情字段
-- =========================================================================================

-- 添加媒体类型字段 (image/video)
ALTER TABLE `feed` ADD COLUMN IF NOT EXISTS `media_type` VARCHAR(20) DEFAULT 'image' COMMENT '媒体类型: image/video' AFTER `type`;

-- 添加宽高比字段 (用于瀑布流布局计算)
ALTER TABLE `feed` ADD COLUMN IF NOT EXISTS `aspect_ratio` DECIMAL(5,3) DEFAULT 1.333 COMMENT '宽高比(width/height)，默认4:3' AFTER `cover_image`;

-- 添加视频时长字段 (秒)
ALTER TABLE `feed` ADD COLUMN IF NOT EXISTS `duration` INT DEFAULT 0 COMMENT '视频时长(秒)，仅video类型有效' AFTER `aspect_ratio`;

-- 添加媒体原始尺寸字段
ALTER TABLE `feed` ADD COLUMN IF NOT EXISTS `media_width` INT DEFAULT 0 COMMENT '媒体原始宽度(px)' AFTER `duration`;
ALTER TABLE `feed` ADD COLUMN IF NOT EXISTS `media_height` INT DEFAULT 0 COMMENT '媒体原始高度(px)' AFTER `media_width`;

-- 添加媒体类型索引
ALTER TABLE `feed` ADD INDEX IF NOT EXISTS `idx_media_type` (`media_type`);

-- =========================================================================================
-- 2. 更新现有测试数据 - 设置媒体详情
-- =========================================================================================

-- 更新现有动态的媒体信息（随机设置不同的宽高比以模拟瀑布流效果）
UPDATE `feed` SET
    `media_type` = 'image',
    `aspect_ratio` = CASE
        WHEN MOD(id, 4) = 0 THEN 0.750  -- 3:4 竖图
        WHEN MOD(id, 4) = 1 THEN 1.333  -- 4:3 横图
        WHEN MOD(id, 4) = 2 THEN 1.000  -- 1:1 方图
        ELSE 0.800                       -- 4:5 竖图
    END,
    `media_width` = CASE
        WHEN MOD(id, 4) = 0 THEN 1080
        WHEN MOD(id, 4) = 1 THEN 1440
        WHEN MOD(id, 4) = 2 THEN 1080
        ELSE 1080
    END,
    `media_height` = CASE
        WHEN MOD(id, 4) = 0 THEN 1440
        WHEN MOD(id, 4) = 1 THEN 1080
        WHEN MOD(id, 4) = 2 THEN 1080
        ELSE 1350
    END
WHERE `media_type` IS NULL OR `media_type` = '';

-- 设置部分动态为视频类型（用于测试）
UPDATE `feed` SET
    `media_type` = 'video',
    `duration` = 60,
    `aspect_ratio` = 0.5625,  -- 9:16 竖屏视频
    `media_width` = 1080,
    `media_height` = 1920
WHERE id IN (1, 6, 9);  -- 选择几条动态设为视频

-- =========================================================================================
-- 3. 验证更新
-- =========================================================================================
SELECT
    id,
    title,
    media_type,
    aspect_ratio,
    duration,
    media_width,
    media_height
FROM `feed`
ORDER BY id
LIMIT 15;

-- =========================================================================================
-- 完成提示
-- =========================================================================================
SELECT 'Feed table extended successfully for Discover page!' AS message;
