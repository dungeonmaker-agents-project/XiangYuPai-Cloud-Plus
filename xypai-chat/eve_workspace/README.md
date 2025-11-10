# 💬 Eve的聊天模块工作空间

> **负责人**: Eve  
> **角色**: 后端聊天服务工程师  
> **模块**: xypai-chat  
> **数据库**: xypai_chat  
> **端口**: 9404  
> **版本**: v7.1

---

## 📦 工作空间结构

```
eve_workspace/
├─ README.md                      # 本文档
├─ WORKSPACE_MANIFEST.md          # 工作清单
├─ EVE_WORKSPACE_COMPLETE.md      # 完成报告
├─ QUICK_START.md                 # 快速启动
│
├─ docker/                        # Docker环境
│  ├─ docker-compose.yml          # 一键启动MySQL+Redis
│  ├─ mysql/
│  │  ├─ init.sql                 # 自动初始化脚本
│  │  └─ my.cnf                   # MySQL配置
│  └─ redis/
│     └─ redis.conf               # Redis配置
│
├─ sql/                           # SQL脚本
│  ├─ 01_create_database.sql      # 创建数据库
│  ├─ 02_create_tables_v7.0.sql   # 创建表（v7.0基础版）
│  ├─ 03_upgrade_to_v7.1.sql      # 升级到v7.1
│  ├─ 04_create_indexes.sql       # 创建索引
│  ├─ 05_init_test_data.sql       # 测试数据
│  └─ 99_reset_all.sql            # 重置脚本
│
└─ docs/                          # 文档
   ├─ DATABASE_DESIGN.md          # 数据库设计文档
   └─ DEVELOPMENT_GUIDE.md        # 开发指南
```

---

## 🚀 快速开始

### 1. 启动Docker环境

```bash
cd eve_workspace/docker
docker-compose up -d

# 等待MySQL和Redis启动（约30秒）
docker-compose ps
```

### 2. 验证环境

```bash
# 测试MySQL连接
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password

# 测试Redis连接
redis-cli -p 6380 ping
```

### 3. 初始化数据库

```bash
# MySQL会自动执行init.sql
# 或手动执行：
cd ../sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password < 01_create_database.sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < 02_create_tables_v7.0.sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < 03_upgrade_to_v7.1.sql
```

### 4. 启动聊天服务

```bash
cd ../../
mvn spring-boot:run -Dspring.profiles.active=dev
```

---

## 🔧 环境配置

### Docker服务

| 服务 | 端口 | 用户名 | 密码 | 说明 |
|------|------|--------|------|------|
| MySQL | 3307 | eve_user | eve_password | 聊天数据库 |
| Redis | 6380 | - | eve_redis | 缓存/队列 |

### 应用配置

```yaml
# bootstrap-dev.yml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3307/xypai_chat
    username: eve_user
    password: eve_password
  
  redis:
    host: 127.0.0.1
    port: 6380
    password: eve_redis
```

---

## 📊 数据库概览

### 表结构（v7.1）

| 表名 | 字段数 | 说明 |
|------|--------|------|
| chat_conversation | 15 | 会话表 |
| chat_message | 23 | 消息表 |
| chat_participant | 13 | 参与者表 |
| message_settings | 20 | 消息设置表 |
| typing_status | 7 | 输入状态表 |

### 关键字段（v7.1新增）

**消息去重**: `chat_message.client_id`  
**消息有序**: `chat_message.sequence_id`  
**投递状态**: `chat_message.delivery_status`  
**精确已读**: `chat_participant.last_read_message_id`  
**置顶功能**: `chat_participant.is_pinned`  
**免打扰**: `chat_participant.is_muted`

---

## 📚 文档索引

### 开发文档
- [数据库设计文档](docs/DATABASE_DESIGN.md)
- [开发指南](docs/DEVELOPMENT_GUIDE.md)
- [快速启动](QUICK_START.md)

### 项目文档
- [升级指南](../UPGRADE_GUIDE_v7.1.md)
- [API文档](../API_DOCUMENTATION_v7.1.md)
- [完成报告](../UPGRADE_COMPLETE_REPORT.md)

---

## 🎯 工作清单

详见 [WORKSPACE_MANIFEST.md](WORKSPACE_MANIFEST.md)

---

## 📞 联系方式

**负责人**: Eve  
**团队**: 后端开发组  
**协作**: Bob(用户), Alice(认证), Jack(DBA), Ivy(前端)

---

**🚀 开始开发聊天模块吧！**

