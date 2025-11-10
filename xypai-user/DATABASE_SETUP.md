# xypai-user 数据库配置指南

## 🔴 当前错误

```
dynamic-datasource can not find primary datasource
```

**原因**: 数据库 `xypai_user` 不存在或连接失败

## 🛠️ 快速修复步骤

### 方案 A: 创建独立数据库（推荐）

#### 1. 创建数据库
在 MySQL 中执行：

```sql
-- 创建 xypai_user 数据库
CREATE DATABASE IF NOT EXISTS `xypai_user` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE `xypai_user`;

-- 创建用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(200) DEFAULT NULL COMMENT '密码',
  `status` int DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建用户资料表
CREATE TABLE `user_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `avatar_thumbnail` varchar(500) DEFAULT NULL COMMENT '头像缩略图',
  `background_image` varchar(500) DEFAULT NULL COMMENT '背景图',
  `gender` int DEFAULT '0' COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `age` int DEFAULT NULL COMMENT '年龄',
  `city_id` bigint DEFAULT NULL COMMENT '城市ID',
  `location` varchar(100) DEFAULT NULL COMMENT '位置',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `ip_location` varchar(100) DEFAULT NULL COMMENT 'IP归属地',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `height` int DEFAULT NULL COMMENT '身高',
  `weight` int DEFAULT NULL COMMENT '体重',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `online_status` int DEFAULT '0' COMMENT '在线状态',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 创建用户统计表
CREATE TABLE `user_stats` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `follower_count` int DEFAULT '0' COMMENT '粉丝数',
  `following_count` int DEFAULT '0' COMMENT '关注数',
  `content_count` int DEFAULT '0' COMMENT '内容数',
  `total_like_count` int DEFAULT '0' COMMENT '总点赞数',
  `total_collect_count` int DEFAULT '0' COMMENT '总收藏数',
  `activity_organizer_count` int DEFAULT '0' COMMENT '组局次数',
  `activity_participant_count` int DEFAULT '0' COMMENT '参与次数',
  `activity_success_count` int DEFAULT '0' COMMENT '成功次数',
  `activity_cancel_count` int DEFAULT '0' COMMENT '取消次数',
  `activity_organizer_score` decimal(3,2) DEFAULT '0.00' COMMENT '组局评分',
  `activity_success_rate` decimal(5,2) DEFAULT '0.00' COMMENT '成功率',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最后同步时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计表';

-- 创建用户关系表
CREATE TABLE `user_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `follower_id` bigint NOT NULL COMMENT '关注者ID',
  `followed_user_id` bigint NOT NULL COMMENT '被关注者ID',
  `relation_type` int DEFAULT '1' COMMENT '关系类型(1关注 4特别关注)',
  `status` int DEFAULT '1' COMMENT '状态(0取消 1正常)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_relation` (`follower_id`,`followed_user_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_followed` (`followed_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关系表';

-- 创建用户钱包表
CREATE TABLE `user_wallet` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `frozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结金额',
  `total_recharge` decimal(10,2) DEFAULT '0.00' COMMENT '总充值',
  `total_consume` decimal(10,2) DEFAULT '0.00' COMMENT '总消费',
  `status` int DEFAULT '0' COMMENT '状态(0正常 1冻结)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包表';

-- 创建交易记录表
CREATE TABLE `transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` int NOT NULL COMMENT '交易类型',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `balance_before` decimal(10,2) DEFAULT NULL COMMENT '交易前余额',
  `balance_after` decimal(10,2) DEFAULT NULL COMMENT '交易后余额',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- 创建职业字典表
CREATE TABLE `occupation_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` varchar(50) NOT NULL COMMENT '职业编码',
  `name` varchar(100) NOT NULL COMMENT '职业名称',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `icon` varchar(200) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` int DEFAULT '1' COMMENT '状态(0禁用 1启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业字典表';

-- 创建用户职业表
CREATE TABLE `user_occupation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `occupation_code` varchar(50) NOT NULL COMMENT '职业编码',
  `is_primary` int DEFAULT '0' COMMENT '是否主职业',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_occupation` (`user_id`,`occupation_code`),
  KEY `idx_user` (`user_id`),
  KEY `idx_occupation` (`occupation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户职业表';
```

#### 2. 更新密码
修改 Nacos 配置或本地配置中的数据库密码为您实际的 MySQL 密码。

### 方案 B: 使用统一数据库

如果您想使用 `ry_cloud` 统一数据库，修改配置：

```yaml
# script/config/nacos/xypai-user.yml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/ry_cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true&nullCatalogMeansCurrent=true
          username: root
          password: password
```

然后在 `ry_cloud` 数据库中创建上述表。

### 方案 C: 禁用健康检查（临时）

如果只是测试编译，可以临时禁用数据库健康检查：

```yaml
# xypai-user/src/main/resources/application.yml
management:
  health:
    db:
      enabled: false
  endpoint:
    health:
      show-details: always
```

## ⚡ 快速执行

### 1. 连接到 MySQL
```bash
mysql -u root -p
```

### 2. 执行上述 SQL 脚本

### 3. 重启应用

## 📝 验证

启动成功后，您应该看到：
```
Started XyPaiUserApplication in X.XXX seconds
```

---

**建议**: 使用方案 A 创建独立数据库，保持微服务的数据隔离性。

