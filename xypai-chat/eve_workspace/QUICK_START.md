# 🚀 Eve的聊天模块快速启动指南

> 5分钟启动完整开发环境

---

## 🎯 前置要求

### 必需软件
- [x] Docker Desktop（或Docker + Docker Compose）
- [x] JDK 21
- [x] Maven 3.8+
- [x] IDE（IntelliJ IDEA推荐）

### 可选软件
- [ ] MySQL客户端（DataGrip/Navicat）
- [ ] Redis客户端（RedisInsight）
- [ ] wscat（WebSocket测试工具）

---

## ⚡ 一键启动（5分钟）

### Step 1: 启动Docker环境（1分钟）

```bash
cd eve_workspace/docker
docker-compose up -d

# 等待服务启动
sleep 30

# 验证服务状态
docker-compose ps
```

**预期输出**:
```
NAME                 STATUS         PORTS
eve-mysql-chat       Up 30 seconds  0.0.0.0:3307->3306/tcp
eve-redis-chat       Up 30 seconds  0.0.0.0:6380->6379/tcp
```

---

### Step 2: 验证数据库（1分钟）

```bash
# 连接MySQL
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password

# 查看数据库
mysql> SHOW DATABASES;
mysql> USE xypai_chat;
mysql> SHOW TABLES;

# 预期：5张表
# chat_conversation
# chat_message
# chat_participant
# message_settings
# typing_status

# 查看测试数据
mysql> SELECT COUNT(*) FROM chat_conversation;  -- 预期：10
mysql> SELECT COUNT(*) FROM chat_message;       -- 预期：35
mysql> SELECT COUNT(*) FROM chat_participant;   -- 预期：40
```

---

### Step 3: 启动应用（2分钟）

```bash
cd ../../  # 回到xypai-chat目录

# 编译
mvn clean package -DskipTests

# 启动
mvn spring-boot:run -Dspring.profiles.active=dev

# 或直接运行JAR
java -jar target/xypai-modules-chat-3.6.6.jar
```

**启动成功标志**:
```
Started XyPaiChatApplication in 5.123 seconds
Tomcat started on port(s): 9404 (http)
```

---

### Step 4: 验证功能（1分钟）

#### 4.1 健康检查
```bash
curl http://localhost:9404/api/v1/health

# 预期响应
{
  "code": 200,
  "data": {
    "status": "UP",
    "service": "xypai-chat",
    "version": "v7.1"
  }
}
```

#### 4.2 Swagger文档
```
浏览器访问：http://localhost:9404/doc.html

# 预期：看到34个API接口
```

#### 4.3 WebSocket连接
```bash
# 安装wscat
npm install -g wscat

# 连接测试
wscat -c ws://localhost:9404/ws/chat/1001/test_token

# 发送心跳
> {"type":"heartbeat","data":{}}

# 预期响应
< {"type":"heartbeat","data":{"pong":true,"serverTime":1705201800000},"timestamp":1705201800000}
```

---

## 🧪 功能测试

### 1. 测试消息发送

```bash
curl -X POST http://localhost:9404/api/v1/messages/text \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "conversationId": 5001,
    "content": "测试消息v7.1",
    "clientId": "uuid-test-'$(date +%s)'"
  }'

# 预期响应
{
  "code": 200,
  "message": "消息发送成功",
  "data": 6038
}
```

### 2. 测试消息去重

```bash
# 发送相同clientId的消息2次
CLIENT_ID="uuid-dedup-test"

curl -X POST http://localhost:9404/api/v1/messages/text \
  -H "Content-Type: application/json" \
  -d '{"conversationId":5001,"content":"去重测试","clientId":"'$CLIENT_ID'"}'

curl -X POST http://localhost:9404/api/v1/messages/text \
  -H "Content-Type: application/json" \
  -d '{"conversationId":5001,"content":"去重测试","clientId":"'$CLIENT_ID'"}'

# 预期：两次返回相同的messageId（消息去重成功）
```

### 3. 测试置顶功能

```bash
curl -X PUT "http://localhost:9404/api/v1/conversations/5001/pin?isPinned=true" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 预期：{"code":200,"message":"操作成功"}
```

### 4. 测试消息设置

```bash
curl -X GET http://localhost:9404/api/v1/message-settings/my \
  -H "Authorization: Bearer YOUR_TOKEN"

# 预期：返回20个设置字段
```

---

## 🐛 故障排查

### 问题1：Docker启动失败

```bash
# 检查Docker是否运行
docker ps

# 查看日志
docker-compose logs -f

# 常见问题：
# - 端口冲突（3307/6380被占用）→ 修改docker-compose.yml端口
# - 内存不足 → 增加Docker内存限制
```

### 问题2：MySQL连接失败

```bash
# 检查MySQL容器状态
docker-compose ps eve-mysql

# 查看MySQL日志
docker-compose logs eve-mysql

# 测试连接
telnet 127.0.0.1 3307

# 常见问题：
# - 密码错误 → 检查docker-compose.yml密码配置
# - 数据库未创建 → 检查init.sql是否执行
```

### 问题3：应用启动失败

```bash
# 检查配置
cat src/main/resources/bootstrap-dev.yml

# 检查数据库连接
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat

# 常见问题：
# - 数据库连接失败 → 检查URL/用户名/密码
# - 表不存在 → 执行SQL脚本
# - 端口冲突 → 修改server.port
```

### 问题4：WebSocket连接失败

```bash
# 检查WebSocket配置
cat src/main/java/com/xypai/chat/config/WebSocketConfig.java

# 测试端口
telnet 127.0.0.1 9404

# 常见问题：
# - Token验证失败 → 检查开发模式是否跳过验证
# - 端点路径错误 → 检查@ServerEndpoint注解
```

---

## 📚 参考文档

### 数据库
- [数据库设计文档](docs/DATABASE_DESIGN.md) - 表结构详细说明
- [PL.md](../../../../.cursor/rules/PL.md) - v7.1设计标准

### 开发
- [开发指南](docs/DEVELOPMENT_GUIDE.md) - 开发规范和示例
- [API文档](../API_DOCUMENTATION_v7.1.md) - 完整API文档

### 升级
- [升级指南](../UPGRADE_GUIDE_v7.1.md) - v7.0→v7.1升级步骤
- [升级报告](../UPGRADE_COMPLETE_REPORT.md) - 升级成果

---

## 🎯 快速验证清单

### 环境验证
- [ ] Docker服务运行正常
- [ ] MySQL连接成功（端口3307）
- [ ] Redis连接成功（端口6380）
- [ ] 数据库xypai_chat存在
- [ ] 5张表全部创建
- [ ] 测试数据加载成功

### 应用验证
- [ ] 应用启动成功（端口9404）
- [ ] Swagger文档可访问
- [ ] 健康检查API正常
- [ ] WebSocket连接成功
- [ ] 消息发送API正常

### 功能验证
- [ ] 消息去重生效
- [ ] 消息有序性验证
- [ ] 置顶功能正常
- [ ] 免打扰功能正常
- [ ] 消息设置API正常

---

## 🔧 常用命令

### Docker管理
```bash
# 启动
docker-compose up -d

# 停止
docker-compose down

# 重启
docker-compose restart

# 查看日志
docker-compose logs -f eve-mysql
docker-compose logs -f eve-redis

# 清理数据（⚠️ 会删除所有数据）
docker-compose down -v
```

### 数据库管理
```bash
# 连接数据库
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat

# 导出数据
mysqldump -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat > backup.sql

# 导入数据
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < backup.sql

# 重置数据
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < sql/99_reset_all.sql
```

### Redis管理
```bash
# 连接Redis
redis-cli -p 6380 -a eve_redis

# 查看所有Key
KEYS chat:*

# 查看在线用户
KEYS chat:online:*

# 查看正在输入
KEYS chat:typing:*

# 清空所有数据
FLUSHALL
```

---

## 📞 获取帮助

### 遇到问题？

1. **查看文档**: [DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md)
2. **查看日志**: `docker-compose logs -f`
3. **重置环境**: 执行`99_reset_all.sql`
4. **联系Eve**: 后端聊天服务组负责人

---

**5分钟快速启动，立即开始开发！** 🚀

**下一步**: 查看 [DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) 开始编码

