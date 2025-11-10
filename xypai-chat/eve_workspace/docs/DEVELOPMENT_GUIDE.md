# 🛠️ Eve的聊天模块开发指南

> **负责人**: Eve  
> **技术栈**: Spring Boot 3.2 + WebSocket + MyBatis Plus + Redis  
> **开发周期**: Week 7-12

---

## 🏗️ 技术架构

### 后端架构
```
xypai-chat (端口9404)
├─ Controller层  → 接收HTTP请求/WebSocket连接
├─ Service层     → 业务逻辑（消息去重/有序/权限）
├─ Mapper层      → 数据访问（MyBatis Plus）
├─ WebSocket层   → 实时推送
└─ Task层        → 定时任务（归档/清理）
```

### 数据层架构
```
MySQL 8.0  → 持久化存储（5张表，78字段）
Redis 7.0  → 缓存/队列/在线状态
```

---

## 🚀 开发环境搭建

### 1. 启动Docker环境

```bash
cd eve_workspace/docker
docker-compose up -d

# 验证
docker-compose ps
```

### 2. 初始化数据库

```bash
# Docker会自动执行init.sql
# 或手动执行：
cd ../sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password < 01_create_database.sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < 02_create_tables_v7.0.sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < 03_upgrade_to_v7.1.sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < 04_create_indexes.sql
mysql -h 127.0.0.1 -P 3307 -u eve_user -peve_password xypai_chat < 05_init_test_data.sql
```

### 3. 配置应用

**bootstrap-dev.yml**:
```yaml
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

### 4. 启动服务

```bash
cd ../../
mvn spring-boot:run -Dspring.profiles.active=dev
```

---

## 💻 核心功能开发

### 1. 消息去重机制

**原理**: 使用client_id（UUID）唯一索引

**实现**: `ChatMessageServiceImpl.java`

```java
// 前端生成UUID
const clientId = 'uuid-' + Date.now() + '-' + Math.random();

// 后端检查重复
if (sendDTO.getClientId() != null) {
    ChatMessage existMessage = chatMessageMapper.selectOne(
        Wrappers.lambdaQuery(ChatMessage.class)
            .eq(ChatMessage::getClientId, sendDTO.getClientId())
    );
    if (existMessage != null) {
        return existMessage.getId(); // 返回已存在消息
    }
}
```

**数据库支持**:
```sql
ALTER TABLE chat_message ADD COLUMN client_id VARCHAR(100) UNIQUE;
CREATE UNIQUE INDEX uk_client_id ON chat_message(client_id);
```

---

### 2. 消息有序性保证

**原理**: 使用sequence_id全局递增

**实现**: `ChatMessageServiceImpl.java`

```java
private Long generateSequenceId(Long conversationId) {
    // v7.1: 使用Redis INCR
    String key = "chat:sequence:" + conversationId;
    return redisTemplate.opsForValue().increment(key);
}
```

**查询优化**:
```sql
-- 严格按sequence_id排序（不再按时间）
ORDER BY sequence_id DESC
```

---

### 3. 冗余字段自动更新

**场景**: 发送消息时，自动更新会话的last_message_id/time

**实现**: `ChatMessageMapper.xml`

```xml
<update id="updateConversationLastMessage">
    UPDATE chat_conversation
    SET last_message_id = #{messageId},
        last_message_time = #{messageTime},
        total_message_count = total_message_count + 1
    WHERE id = #{conversationId}
</update>
```

**调用**: 
```java
updateConversationLastMessage(conversationId, messageId, now);
```

---

### 4. 精确已读定位

**场景**: 标记已读时，同时记录消息ID和时间

**实现**: `ChatParticipantMapper.xml`

```xml
<update id="updateReadPosition">
    UPDATE chat_participant
    SET last_read_message_id = #{messageId},
        last_read_time = #{readTime},
        unread_count = 0
    WHERE conversation_id = #{conversationId}
      AND user_id = #{userId}
</update>
```

---

### 5. WebSocket实时推送

**端点**: `ws://localhost:9404/ws/chat/{userId}/{token}`

**实现**: `ChatWebSocketServer.java`

```java
@ServerEndpoint("/ws/chat/{userId}/{token}")
public class ChatWebSocketServer {
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        // 验证Token
        // 保存连接
        // 更新在线状态
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // 解析消息类型（chat/typing/read/heartbeat）
        // 处理业务逻辑
        // 推送给其他成员
    }
    
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        // 移除连接
        // 更新离线状态
    }
}
```

**消息格式**:
```json
{
  "type": "chat",
  "data": {
    "conversationId": 123,
    "messageType": 1,
    "content": "你好！",
    "clientId": "uuid-xxx"
  }
}
```

---

## 🧪 开发测试

### 单元测试

```java
@Test
public void testMessageDeduplication() {
    String clientId = MessageUtils.generateClientId();
    
    MessageSendDTO dto = MessageSendDTO.builder()
        .conversationId(123L)
        .content("测试消息")
        .clientId(clientId)
        .build();
    
    Long id1 = chatMessageService.sendMessage(dto);
    Long id2 = chatMessageService.sendMessage(dto);  // 重复发送
    
    assertEquals(id1, id2);  // 消息去重成功
}
```

### API测试（Postman）

```bash
# 发送文本消息
POST http://localhost:9404/api/v1/messages/text
Authorization: Bearer {token}
{
  "conversationId": 5001,
  "content": "测试消息",
  "clientId": "uuid-test-001"
}

# 置顶会话
PUT http://localhost:9404/api/v1/conversations/5001/pin?isPinned=true

# 查询未读数
GET http://localhost:9404/api/v1/messages/conversation/5001/unread-count
```

### WebSocket测试

```bash
# 安装wscat
npm install -g wscat

# 连接
wscat -c ws://localhost:9404/ws/chat/1001/test_token

# 发送心跳
> {"type":"heartbeat","data":{}}

# 预期响应
< {"type":"heartbeat","data":{"pong":true,...},...}
```

---

## 📊 性能优化

### 1. 查询优化

**使用冗余字段**:
```java
// ✅ v7.1优化
SELECT c.*, c.last_message_time
FROM chat_conversation c

// ❌ v7.0（慢）
SELECT c.*, 
  (SELECT created_at FROM chat_message ...) as last_time
FROM chat_conversation c
```

**使用索引**:
```java
// ✅ 使用索引字段查询
WHERE conversation_id = 5001  // 使用idx_conversation_id

// ❌ 避免函数操作字段
WHERE DATE(created_at) = '2024-01-14'  // 不走索引
```

### 2. 批量操作

```java
// ✅ 批量插入参与者
chatParticipantMapper.batchInsertParticipants(participants);

// ❌ 避免循环单条插入
for (ChatParticipant p : participants) {
    chatParticipantMapper.insert(p);  // 慢
}
```

### 3. Redis缓存

```java
// 消息设置缓存（1小时）
String key = "chat:settings:" + userId;
redisTemplate.opsForValue().set(key, JSON.toJSONString(settings), 1, TimeUnit.HOURS);

// 在线状态（5分钟）
String key = "chat:online:" + userId;
redisTemplate.opsForValue().set(key, "1", 5, TimeUnit.MINUTES);
```

---

## 🔒 安全规范

### 1. 权限验证

```java
// 发送消息前验证权限
if (!chatConversationService.isParticipant(conversationId, currentUserId)) {
    throw new ServiceException("无权限访问该会话");
}
```

### 2. 内容审核

```java
// 检查敏感词
if (MessageUtils.containsSensitiveWords(content)) {
    throw new ServiceException("消息包含敏感词");
}
```

### 3. 频率限制

```java
// TODO: 使用Redis滑动窗口限流
// 每用户每分钟最多发送20条消息
```

---

## 📚 工具类使用

### MessageUtils

```java
// 生成客户端ID
String clientId = MessageUtils.generateClientId();

// 生成消息预览
String preview = MessageUtils.generatePreview(messageType, content, mediaData, 50);

// 验证内容长度
boolean valid = MessageUtils.validateContentLength(messageType, content);

// 格式化文件大小
String size = MessageUtils.formatFileSize(1024000L);  // "1.0MB"
```

### WebSocketUtils

```java
// 发送JSON消息
WebSocketUtils.sendJsonMessage(session, message);

// 构建系统消息
Map<String, Object> msg = WebSocketUtils.buildSystemMessage("连接成功");

// 安全关闭连接
WebSocketUtils.closeSession(session, "Token过期");
```

---

## 🐛 常见问题

### Q1: 消息重复怎么办？
```
A: 前端必须带上clientId（UUID），后端自动去重。
```

### Q2: 消息乱序怎么办？
```
A: v7.1使用sequence_id全局递增，保证100%有序。
查询时：ORDER BY sequence_id DESC
```

### Q3: 未读数不准确？
```
A: v7.1使用unread_count冗余字段，自动增减。
发送时：+1（其他成员）
标记已读：清零
```

### Q4: WebSocket连接失败？
```
A: 检查：
1. 端口9404是否开放
2. Token是否有效
3. WebSocketConfig是否配置
```

---

## 📞 协作对接

### 依赖模块

**上游**:
- xypai-user（用户服务）- 查询用户信息
- xypai-file（文件服务）- 文件上传

**下游**:
- 前端Ivy - WebSocket对接

---

## ✅ 开发检查清单

### 提交代码前
- [ ] 编译无错误
- [ ] 消息去重测试通过
- [ ] 消息有序性验证
- [ ] 冗余字段自动更新
- [ ] WebSocket连接测试
- [ ] API文档更新
- [ ] 日志完整

### 功能完成前
- [ ] 单元测试覆盖率>80%
- [ ] 性能测试通过
- [ ] WebSocket并发测试>1000
- [ ] Code Review通过

---

**开始开发吧，Eve！** 🚀

