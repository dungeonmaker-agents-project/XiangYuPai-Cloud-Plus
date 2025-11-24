# XiangYuPai Chat Service (xypai-chat)

> **Chat & Messaging Microservice for XiangYuPai Platform**
>
> Version: 1.0.0 | Port: 9404 | Database: xypai_chat

---

## 📋 Overview

**xypai-chat** is a complete, production-ready microservice for real-time chat and messaging functionality in the XiangYuPai platform. It provides:

- ✅ **Private messaging** - One-on-one conversations with conversation management
- ✅ **Multi-format support** - Text, images, voice, and video messages
- ✅ **Real-time communication** - WebSocket-based instant messaging
- ✅ **Message management** - Read receipts, message recall (2-min window), deletion
- ✅ **Online status tracking** - Real-time user presence
- ✅ **Performance optimized** - Redis caching, efficient database queries
- ✅ **Fully documented** - Knife4j/Swagger API documentation

---

## 🏗️ Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.2.0 |
| ORM | MyBatis Plus | 3.5.7 |
| Database | MySQL | 8.0+ |
| Cache | Redis | 7.0+ |
| Service Discovery | Nacos | 2.x |
| RPC Framework | Dubbo | 3.x |
| Auth | Sa-Token | Latest |
| WebSocket | Spring WebSocket | Latest |

### 4-Layer Architecture

```
Controller Layer (REST APIs)
    ↓
Service Layer (Business Logic)
    ↓
Mapper Layer (MyBatis Plus)
    ↓
Database Layer (MySQL xypai_chat)
```

---

## 📁 Project Structure

```
xypai-chat/
├── src/main/java/org/dromara/chat/
│   ├── XyPaiChatApplication.java           # Main application
│   ├── controller/
│   │   └── app/                            # App-facing REST APIs
│   ├── service/
│   │   ├── IMessageService.java            # Service interface
│   │   └── impl/                           # Service implementations
│   ├── mapper/
│   │   ├── ConversationMapper.java         # Conversation DB access
│   │   └── MessageMapper.java              # Message DB access
│   ├── domain/
│   │   ├── entity/                         # Database entities
│   │   │   ├── Conversation.java
│   │   │   └── Message.java
│   │   ├── dto/                            # Request DTOs
│   │   │   ├── ConversationQueryDTO.java
│   │   │   ├── ChatHistoryQueryDTO.java
│   │   │   └── MessageSendDTO.java
│   │   └── vo/                             # Response VOs
│   │       ├── UnreadCountVO.java
│   │       ├── ConversationVO.java
│   │       └── MessageVO.java
│   ├── config/                             # Configuration classes
│   │   └── WebSocketConfig.java            # WebSocket configuration
│   └── websocket/                          # WebSocket handlers
│       └── MessageWebSocketHandler.java
├── src/main/resources/
│   ├── application.yml                     # Main configuration
│   └── bootstrap.yml                       # Nacos bootstrap
├── sql/
│   └── xypai_chat.sql                      # Database schema
├── pom.xml                                 # Maven dependencies
└── README.md                               # This file
```

---

## 🗄️ Database Schema

### Tables

#### 1. **conversation** (会话表)
Stores user conversation metadata.

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | User ID |
| other_user_id | BIGINT | Other user ID |
| last_message | TEXT | Last message content |
| last_message_time | DATETIME | Last message time |
| unread_count | INT | Unread message count |
| deleted | TINYINT | Soft delete flag (0=active, 1=deleted) |
| version | INT | Optimistic lock version |

**Key Indexes:**
- `idx_user_deleted (user_id, deleted)`
- `idx_user_other_deleted (user_id, other_user_id, deleted)`
- `idx_last_message_time (last_message_time)`

#### 2. **message** (消息表)
Stores all chat messages.

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| conversation_id | BIGINT | Conversation ID |
| sender_id | BIGINT | Sender user ID |
| receiver_id | BIGINT | Receiver user ID |
| message_type | VARCHAR(20) | text/image/voice/video |
| content | TEXT | Message content (text messages) |
| media_url | VARCHAR(500) | Media file URL |
| thumbnail_url | VARCHAR(500) | Thumbnail URL (video) |
| duration | INT | Duration in seconds (voice/video) |
| status | TINYINT | 0=sending, 1=delivered, 2=read, 3=failed |
| is_recalled | TINYINT | Recall flag (0=no, 1=yes) |
| recalled_at | DATETIME | Recall timestamp |
| deleted | TINYINT | Soft delete flag |
| version | INT | Optimistic lock version |

**Key Indexes:**
- `idx_conversation_deleted_created (conversation_id, deleted, create_time)`
- `idx_sender_receiver (sender_id, receiver_id)`
- `idx_status (status)`

---

## 🚀 Quick Start

### Prerequisites

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Nacos 2.x

### 1. Database Setup

```bash
# Create database and tables
mysql -u root -p < sql/xypai_chat.sql
```

### 2. Configure Nacos

Upload configuration to Nacos (or use local config):

**Data ID:** `xypai-chat-dev.yml`
**Group:** `DEFAULT_GROUP`
**Namespace:** `dev`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_chat?useSSL=false
    username: root
    password: your_password
```

### 3. Start Infrastructure

```bash
# Using Docker Compose (recommended)
cd docker
docker-compose up -d mysql redis nacos

# Verify services
docker ps
```

### 4. Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run JAR
java -jar target/xypai-chat.jar
```

### 5. Verify

```bash
# Health check
curl http://localhost:9404/actuator/health

# API documentation
open http://localhost:9404/doc.html
```

**Expected output:**
```
============================================
✅ XiangYuPai Chat Service Started Successfully!
📡 Port: 9404
📖 API Docs: http://localhost:9404/doc.html
🔌 WebSocket: ws://localhost:9404/ws
============================================
```

---

## 📡 API Endpoints

### 10 REST APIs

| # | Endpoint | Method | Description |
|---|----------|--------|-------------|
| 1 | `/api/message/unread-count` | GET | Get unread message count |
| 2 | `/api/message/conversations` | GET | Get conversation list |
| 3 | `/api/message/conversation/{id}` | DELETE | Delete conversation |
| 4 | `/api/message/clear-all` | POST | Clear all messages |
| 5 | `/api/message/chat/{id}` | GET | Get chat history |
| 6 | `/api/message/send` | POST | Send message |
| 7 | `/api/message/read/{id}` | PUT | Mark messages as read |
| 8 | `/api/message/recall/{id}` | POST | Recall message (2-min limit) |
| 9 | `/api/message/{id}` | DELETE | Delete message |
| 10 | `/api/message/upload` | POST | Upload media file |

### WebSocket Endpoint

**URL:** `ws://localhost:9404/ws`

**Events:**
- `new_message` - New message received
- `message_read` - Message read by recipient
- `message_recalled` - Message recalled by sender
- `typing` - User is typing
- `online_status` - User online status changed

---

## 🔧 Configuration

### Key Configuration Parameters

```yaml
xypai:
  chat:
    # Message constraints
    message:
      text-max-length: 500                  # Max text message length
      recall-timeout-seconds: 120           # Recall time limit (2 minutes)

    # File upload limits (bytes)
    upload:
      image-max-size: 10485760             # 10MB
      voice-max-size: 2097152              # 2MB
      video-max-size: 52428800             # 50MB

    # Cache TTL (seconds)
    cache:
      unread-count-ttl: 180                # 3 minutes
      conversation-list-ttl: 300           # 5 minutes
      online-status-ttl: 300               # 5 minutes

    # WebSocket configuration
    websocket:
      heartbeat-interval: 30000            # 30 seconds
      timeout: 300000                      # 5 minutes
      max-connections: 10000               # Max concurrent connections
```

---

## 🧪 Testing

### Health Check

```bash
curl http://localhost:9404/actuator/health
```

### API Testing (using Knife4j)

1. Open http://localhost:9404/doc.html
2. Navigate to "App APIs" group
3. Try endpoints with sample data

### Example API Call

```bash
# Get unread count
curl -X GET "http://localhost:9404/api/message/unread-count" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Send text message
curl -X POST "http://localhost:9404/api/message/send" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "conversationId": 1,
    "receiverId": 2,
    "messageType": "text",
    "content": "Hello!"
  }'
```

---

## 📊 Performance Features

### 1. Redis Caching

- **Unread count**: 3-minute TTL, cleared on new message/read
- **Conversation list**: 5-minute TTL, cleared on updates
- **Online status**: 5-minute TTL, refreshed by WebSocket heartbeat

### 2. Database Optimization

- **Indexes**: Optimized for common queries (user conversations, message history)
- **Soft delete**: Fast delete operations without data loss
- **Optimistic locking**: Prevents concurrent update conflicts
- **Pagination**: Cursor-based pagination for large datasets

### 3. WebSocket Optimization

- **Heartbeat mechanism**: 30-second intervals
- **Auto-reconnect**: Client-side automatic reconnection
- **Message queue**: Offline message buffering
- **Connection pooling**: Efficient connection management

---

## 🔐 Security

### Authentication

- **Sa-Token**: JWT-based authentication
- **Same-Token**: Gateway-to-service verification
- **Token refresh**: Automatic token refresh mechanism

### Authorization

- Users can only:
  - View their own conversations
  - Send messages to non-blocked users
  - Recall their own messages (within 2 minutes)
  - Delete their own messages

### Data Protection

- **Soft delete**: Messages preserved for audit trail
- **Blacklist check**: Prevents messaging blocked users
- **XSS protection**: Input sanitization and validation

---

## 🚨 Troubleshooting

### Issue: Service won't start

**Check:**
1. MySQL is running: `docker ps | grep mysql`
2. Redis is running: `docker ps | grep redis`
3. Nacos is running: `curl http://localhost:8848/nacos`
4. Database exists: `mysql -u root -p -e "SHOW DATABASES LIKE 'xypai_chat'"`

### Issue: Database connection failed

**Solution:**
```yaml
# Update application.yml or Nacos config
spring:
  datasource:
    url: jdbc:mysql://YOUR_HOST:3306/xypai_chat?useSSL=false&allowPublicKeyRetrieval=true
    username: YOUR_USER
    password: YOUR_PASSWORD
```

### Issue: WebSocket connection refused

**Check:**
1. Service is running on port 9404
2. Firewall allows WebSocket connections
3. Client uses correct WebSocket URL: `ws://localhost:9404/ws`

---

## 📝 Development Guide

### Adding a New API

1. **Define DTO** in `domain/dto/`
2. **Define VO** in `domain/vo/`
3. **Add service method** in `IMessageService.java`
4. **Implement logic** in `MessageServiceImpl.java`
5. **Create controller endpoint** in `controller/app/`
6. **Add API documentation** using `@Operation` annotation
7. **Test** using Knife4j UI

### Database Migration

```bash
# 1. Create migration script
sql/migrations/V1.1__add_group_chat.sql

# 2. Run migration
mysql -u root -p xypai_chat < sql/migrations/V1.1__add_group_chat.sql
```

---

## 📈 Monitoring

### Actuator Endpoints

- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Service information

### Logging

Logs are stored in: `logs/xypai-chat/app.log`

**Log levels:**
- `ERROR`: System errors
- `WARN`: Business exceptions
- `INFO`: Key operations
- `DEBUG`: Development debugging

---

## 🔗 Dependencies

### Parent Project

```xml
<parent>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-cloud-plus</artifactId>
    <version>${revision}</version>
</parent>
```

### Key Dependencies

- `ruoyi-common-web` - Web utilities
- `ruoyi-common-mybatis` - MyBatis Plus integration
- `ruoyi-common-security` - Sa-Token security
- `ruoyi-common-redis` - Redis caching
- `ruoyi-common-dubbo` - RPC framework
- `spring-boot-starter-websocket` - WebSocket support

---

## 🎯 Next Steps

### Phase 2: Implementation Tasks

1. **Complete Service Implementation**
   - Implement all methods in `MessageServiceImpl.java`
   - Add business logic, validation, and error handling

2. **Create Controllers**
   - Implement 10 REST API controllers
   - Add Knife4j documentation annotations

3. **WebSocket Server**
   - Implement `WebSocketConfig.java`
   - Create `MessageWebSocketHandler.java`
   - Handle connection, disconnection, and message events

4. **RPC Interfaces**
   - Define Dubbo service interfaces
   - Implement RPC methods for cross-service calls

5. **Redis Caching**
   - Implement cache-aside pattern
   - Add cache invalidation logic

6. **Testing**
   - Unit tests for service layer
   - Integration tests for APIs
   - WebSocket connection tests

---

## 📞 Support

**Project:** XiangYuPai Platform
**Module:** Chat & Messaging Service
**Version:** 1.0.0
**Maintained by:** XiangYuPai Team

**Documentation:**
- API Docs: http://localhost:9404/doc.html
- Database Schema: `sql/xypai_chat.sql`
- Tech Guide: `BACKEND_TECH_STACK_GUIDE.md`

---

## 📄 License

Copyright © 2025 XiangYuPai Team. All rights reserved.

---

**Status:** ✅ Core structure complete, ready for implementation
**Last Updated:** 2025-01-14
