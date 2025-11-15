-- ========================================
-- XiangYuPai Common Services Database
-- 享遇派通用服务数据库
-- ========================================
-- 包含四个业务服务的数据表:
-- 1. Location Service - 位置服务
-- 2. Media Upload Service - 媒体上传服务
-- 3. Notification Service - 通知服务
-- 4. Report Service - 举报审核服务
-- ========================================

CREATE DATABASE IF NOT EXISTS `xypai_common` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `xypai_common`;

-- ========================================
-- Location Service - 位置服务
-- ========================================

-- 1. 地点表
CREATE TABLE `location` (
    `id`            BIGINT(20)      NOT NULL COMMENT '地点ID',
    `name`          VARCHAR(100)    NOT NULL COMMENT '地点名称',
    `address`       VARCHAR(255)    NOT NULL COMMENT '详细地址',
    `latitude`      DECIMAL(10,6)   NOT NULL COMMENT '纬度',
    `longitude`     DECIMAL(10,6)   NOT NULL COMMENT '经度',
    `location_point` POINT          NOT NULL SRID 4326 COMMENT '地理坐标点(空间索引)',
    `geohash`       VARCHAR(20)     COMMENT 'Geohash编码',

    `category`      VARCHAR(50)     COMMENT '分类',
    `province`      VARCHAR(50)     COMMENT '省份',
    `city_code`     VARCHAR(10)     COMMENT '城市代码',
    `city`          VARCHAR(50)     COMMENT '城市',
    `district`      VARCHAR(50)     COMMENT '区县',
    `street`        VARCHAR(50)     COMMENT '街道',

    `source`        VARCHAR(20)     COMMENT '数据来源',
    `extra_info`    TEXT            COMMENT '额外信息JSON',

    `status`        TINYINT(1)      DEFAULT 0 COMMENT '状态: 0=正常,1=禁用',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `create_by`     BIGINT(20)      COMMENT '创建人',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`     BIGINT(20)      COMMENT '更新人',
    `deleted`       BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`       INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    SPATIAL KEY `idx_location_point` (`location_point`),
    KEY `idx_geohash` (`geohash`),
    KEY `idx_city_code` (`city_code`),
    KEY `idx_category` (`category`),
    KEY `idx_deleted` (`deleted`),
    FULLTEXT KEY `idx_fulltext` (`name`, `address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点表';

-- 2. 城市表
CREATE TABLE `city` (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `city_code`         VARCHAR(10)     NOT NULL COMMENT '城市代码',
    `city_name`         VARCHAR(50)     NOT NULL COMMENT '城市名称',
    `province`          VARCHAR(50)     COMMENT '省份',
    `pinyin`            VARCHAR(50)     COMMENT '拼音',
    `first_letter`      CHAR(1)         COMMENT '首字母',

    `center_lat`        DECIMAL(10,6)   COMMENT '中心点纬度',
    `center_lng`        DECIMAL(10,6)   COMMENT '中心点经度',
    `center_point`      POINT           SRID 4326 COMMENT '中心点坐标',

    `is_hot`            TINYINT(1)      DEFAULT 0 COMMENT '是否热门: 0=否,1=是',
    `has_districts`     TINYINT(1)      DEFAULT 0 COMMENT '是否有区域: 0=否,1=是',
    `sort_order`        INT(11)         DEFAULT 0 COMMENT '排序',

    `status`            TINYINT(1)      DEFAULT 1 COMMENT '状态: 0=禁用,1=正常',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `create_by`         BIGINT(20),
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         BIGINT(20),
    `deleted`           BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`           INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_code` (`city_code`, `deleted`),
    SPATIAL KEY `idx_center_point` (`center_point`),
    KEY `idx_first_letter` (`first_letter`),
    KEY `idx_is_hot` (`is_hot`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市表';

-- 初始化热门城市数据
INSERT INTO `city` (`city_code`, `city_name`, `province`, `pinyin`, `first_letter`,
                    `center_lat`, `center_lng`, `is_hot`, `sort_order`, `status`)
VALUES
('110100', '北京', '北京市', 'beijing', 'B', 39.904989, 116.405285, 1, 1, 1),
('310100', '上海', '上海市', 'shanghai', 'S', 31.230416, 121.473701, 1, 2, 1),
('440100', '广州', '广东省', 'guangzhou', 'G', 23.129110, 113.264385, 1, 3, 1),
('440300', '深圳', '广东省', 'shenzhen', 'S', 22.543099, 114.057868, 1, 4, 1),
('330100', '杭州', '浙江省', 'hangzhou', 'H', 30.274084, 120.155070, 1, 5, 1),
('510100', '成都', '四川省', 'chengdu', 'C', 30.572815, 104.066801, 1, 6, 1),
('420100', '武汉', '湖北省', 'wuhan', 'W', 30.592849, 114.305539, 1, 7, 1),
('320100', '南京', '江苏省', 'nanjing', 'N', 32.041546, 118.767413, 1, 8, 1),
('610100', '西安', '陕西省', 'xian', 'X', 34.341568, 108.939774, 1, 9, 1),
('500100', '重庆', '重庆市', 'chongqing', 'C', 29.431586, 106.912251, 1, 10, 1);

-- ========================================
-- Media Upload Service - 媒体上传服务
-- ========================================

-- 3. 媒体文件表
CREATE TABLE `media_file` (
    `id`                BIGINT(20)      NOT NULL COMMENT '媒体文件ID',
    `user_id`           BIGINT(20)      NOT NULL COMMENT '用户ID',

    `file_type`         VARCHAR(20)     NOT NULL COMMENT '文件类型: image/video/audio',
    `original_name`     VARCHAR(255)    COMMENT '原始文件名',
    `stored_name`       VARCHAR(255)    COMMENT '存储文件名',
    `file_path`         VARCHAR(500)    COMMENT '文件路径(OSS相对路径)',
    `file_url`          VARCHAR(500)    COMMENT '文件URL(完整访问地址)',
    `thumbnail_url`     VARCHAR(500)    COMMENT '缩略图URL',

    `file_size`         BIGINT(20)      COMMENT '文件大小(字节)',
    `file_ext`          VARCHAR(20)     COMMENT '文件扩展名',
    `mime_type`         VARCHAR(100)    COMMENT 'MIME类型',

    `width`             INT(11)         COMMENT '图片宽度',
    `height`            INT(11)         COMMENT '图片高度',
    `duration`          INT(11)         COMMENT '视频时长(秒)',

    `storage`           VARCHAR(20)     COMMENT '存储平台: aliyun/tencent/local',
    `bucket_name`       VARCHAR(100)    COMMENT 'OSS Bucket名称',
    `md5`               VARCHAR(64)     COMMENT '文件MD5值(去重使用)',

    `biz_type`          VARCHAR(50)     COMMENT '业务类型: avatar/post/moment/chat等',
    `biz_id`            BIGINT(20)      COMMENT '关联业务ID',

    `status`            TINYINT(1)      DEFAULT 0 COMMENT '状态: 0=正常, 1=审核中, 2=违规',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `create_by`         BIGINT(20),
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         BIGINT(20),
    `deleted`           BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`           INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_file_type` (`file_type`),
    KEY `idx_biz_type_id` (`biz_type`, `biz_id`),
    KEY `idx_md5` (`md5`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件表';

-- ========================================
-- Notification Service - 通知服务
-- ========================================

-- 4. 通知表
CREATE TABLE `notification` (
    `id`                BIGINT(20)      NOT NULL COMMENT '通知ID',
    `user_id`           BIGINT(20)      NOT NULL COMMENT '接收用户ID',
    `from_user_id`      BIGINT(20)      COMMENT '发送用户ID(系统通知为null)',

    `type`              VARCHAR(20)     NOT NULL COMMENT '通知类型: like/comment/follow/system/activity',
    `title`             VARCHAR(100)    NOT NULL COMMENT '通知标题',
    `content`           VARCHAR(500)    NOT NULL COMMENT '通知内容',

    `biz_type`          VARCHAR(50)     COMMENT '关联业务类型: post/moment/comment等',
    `biz_id`            BIGINT(20)      COMMENT '关联业务ID',
    `extra_data`        TEXT            COMMENT '扩展数据(JSON格式)',

    `is_read`           TINYINT(1)      DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
    `read_at`           DATETIME        COMMENT '已读时间',

    `status`            TINYINT(1)      DEFAULT 0 COMMENT '状态: 0=正常, 1=已删除',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `create_by`         BIGINT(20),
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         BIGINT(20),
    `deleted`           BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`           INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_user_type_read` (`user_id`, `type`, `is_read`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ========================================
-- Report Service - 举报审核服务
-- ========================================

-- 5. 举报表
CREATE TABLE `report` (
    `id`                BIGINT(20)      NOT NULL COMMENT '举报ID',
    `reporter_id`       BIGINT(20)      NOT NULL COMMENT '举报用户ID',
    `reported_user_id`  BIGINT(20)      NOT NULL COMMENT '被举报用户ID',

    `content_type`      VARCHAR(20)     NOT NULL COMMENT '被举报内容类型: post/moment/comment/user',
    `content_id`        BIGINT(20)      NOT NULL COMMENT '被举报内容ID',

    `reason_type`       VARCHAR(50)     NOT NULL COMMENT '举报原因类型: spam/porn/violence/fraud/other',
    `reason_detail`     VARCHAR(500)    COMMENT '举报详细说明',
    `evidence_urls`     TEXT            COMMENT '证据截图URLs(JSON数组)',

    `status`            TINYINT(1)      DEFAULT 0 COMMENT '审核状态: 0=待审核, 1=审核中, 2=已处理, 3=已驳回',
    `reviewer_id`       BIGINT(20)      COMMENT '审核人ID',
    `review_result`     TINYINT(1)      COMMENT '审核结果: 0=无效举报, 1=警告, 2=删除内容, 3=封禁用户',
    `review_remark`     VARCHAR(500)    COMMENT '审核备注',
    `reviewed_at`       DATETIME        COMMENT '审核时间',

    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `create_by`         BIGINT(20),
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         BIGINT(20),
    `deleted`           BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`           INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_reported_user_id` (`reported_user_id`),
    KEY `idx_content` (`content_type`, `content_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 6. 处罚表
CREATE TABLE `punishment` (
    `id`                BIGINT(20)      NOT NULL COMMENT '处罚ID',
    `user_id`           BIGINT(20)      NOT NULL COMMENT '被处罚用户ID',
    `report_id`         BIGINT(20)      COMMENT '关联举报ID',

    `type`              VARCHAR(20)     NOT NULL COMMENT '处罚类型: warning/mute/ban',
    `reason`            VARCHAR(500)    NOT NULL COMMENT '处罚原因',
    `duration`          INT(11)         COMMENT '处罚时长(分钟, null表示永久)',

    `start_time`        DATETIME        NOT NULL COMMENT '处罚开始时间',
    `end_time`          DATETIME        COMMENT '处罚结束时间',

    `status`            TINYINT(1)      DEFAULT 0 COMMENT '处罚状态: 0=生效中, 1=已解除, 2=已过期',
    `operator_id`       BIGINT(20)      COMMENT '操作人ID',
    `operator_remark`   VARCHAR(500)    COMMENT '操作备注',

    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `create_by`         BIGINT(20),
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`         BIGINT(20),
    `deleted`           BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`           INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_user_type_status` (`user_id`, `type`, `status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处罚表';

-- ========================================
-- 初始化完成
-- ========================================

SELECT '数据库初始化完成' AS 'Status';
SELECT 'Location Service: location, city' AS 'Tables';
SELECT 'Media Service: media_file' AS 'Tables';
SELECT 'Notification Service: notification' AS 'Tables';
SELECT 'Report Service: report, punishment' AS 'Tables';
