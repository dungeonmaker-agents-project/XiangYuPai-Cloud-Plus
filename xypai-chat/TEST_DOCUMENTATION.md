# XiangYuPai Chat Service - Test Documentation

> **Version:** 1.0
> **Date:** 2025-01-14
> **Status:** Ready for Testing
> **Service:** xypai-chat (Port 9404)

---

## 📋 Table of Contents

1. [Test Overview](#test-overview)
2. [Test Environment Setup](#test-environment-setup)
3. [Unit Tests](#unit-tests)
4. [Integration Tests](#integration-tests)
5. [API Test Cases](#api-test-cases)
6. [WebSocket Test Cases](#websocket-test-cases)
7. [Performance Tests](#performance-tests)
8. [Test Data](#test-data)
9. [Test Execution](#test-execution)
10. [Bug Reporting](#bug-reporting)

---

## 🎯 Test Overview

### Test Scope

**In Scope:**
- ✅ All 10 REST API endpoints
- ✅ All 5 WebSocket events
- ✅ All 3 RPC methods
- ✅ Business logic validation
- ✅ Data persistence
- ✅ Cache behavior
- ✅ Security/authorization
- ✅ Error handling
- ✅ Performance benchmarks

**Out of Scope:**
- ❌ User Service integration (mocked)
- ❌ Notification Service integration (mocked)
- ❌ File storage (OSS) - use test bucket
- ❌ APNs/FCM push notifications

### Test Strategy

| Test Type | Coverage Target | Priority |
|-----------|-----------------|----------|
| **Unit Tests** | 70%+ | High |
| **Integration Tests** | All endpoints | High |
| **API Tests** | All happy/error paths | Critical |
| **WebSocket Tests** | All events | High |
| **Performance Tests** | P95 < 300ms | Medium |
| **Security Tests** | Auth/authz | Critical |

### Success Criteria

- ✅ All API endpoints return correct responses
- ✅ All WebSocket events delivered successfully
- ✅ Database transactions maintain consistency
- ✅ Cache hit rate > 60%
- ✅ No security vulnerabilities
- ✅ P95 response time < 300ms
- ✅ Handles 100 concurrent users

---

## 🛠️ Test Environment Setup

### Prerequisites

```bash
# Required software
- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Nacos 2.x
- Postman or curl
- WebSocket client (wscat or browser)
```

### Environment Setup

#### 1. Database Setup

```sql
-- Create database
mysql -u root -p

CREATE DATABASE IF NOT EXISTS xypai_chat_test
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE xypai_chat_test;

-- Run schema
SOURCE E:/Users/Administrator/Documents/GitHub/RuoYi-Cloud-Plus/xypai-chat/sql/xypai_chat.sql;

-- Verify tables
SHOW TABLES;
-- Expected: conversation, message
```

#### 2. Redis Setup

```bash
# Start Redis (Docker)
docker run -d \
  --name redis-test \
  -p 6379:6379 \
  redis:7-alpine

# Verify
redis-cli ping
# Expected: PONG
```

#### 3. Nacos Setup

```bash
# Start Nacos (Docker)
docker run -d \
  --name nacos-test \
  -e MODE=standalone \
  -p 8848:8848 \
  nacos/nacos-server:v2.3.0

# Access: http://localhost:8848/nacos
# Username: nacos
# Password: nacos
```

#### 4. Application Configuration

```yaml
# Update application-test.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_chat_test
    username: root
    password: your_password

  redis:
    host: localhost
    port: 6379
    database: 1  # Use separate DB for testing

server:
  port: 9404

# Enable test profile
spring:
  profiles:
    active: test
```

#### 5. Start Application

```bash
cd E:/Users/Administrator/Documents/GitHub/RuoYi-Cloud-Plus/xypai-chat

# Run tests with Maven
mvn clean test

# Or start service for manual testing
mvn spring-boot:run -Dspring.profiles.active=test
```

#### 6. Verify Health

```bash
# Health check
curl http://localhost:9404/actuator/health

# Expected response:
{
  "status": "UP"
}

# API documentation
open http://localhost:9404/doc.html
```

---

## 🧪 Unit Tests

### Test Structure

```
src/test/java/org/dromara/chat/
├── service/
│   └── MessageServiceImplTest.java
├── controller/
│   └── MessageControllerTest.java
├── mapper/
│   ├── ConversationMapperTest.java
│   └── MessageMapperTest.java
└── websocket/
    └── MessageWebSocketHandlerTest.java
```

### Service Layer Tests

#### MessageServiceImplTest.java

```java
@SpringBootTest
@Transactional
class MessageServiceImplTest {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    // Test data
    private Long testUserId = 1L;
    private Long testOtherUserId = 2L;

    @Test
    void testGetUnreadCount_Success() {
        // Arrange: Create test conversation with unread messages
        Conversation conversation = createTestConversation(testUserId, testOtherUserId, 5);
        conversationMapper.insert(conversation);

        // Act
        UnreadCountVO result = messageService.getUnreadCount(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getTotal());
        assertTrue(result.getTotal() >= result.getLikes());
    }

    @Test
    void testGetConversations_WithPagination() {
        // Arrange: Create 25 conversations
        for (int i = 0; i < 25; i++) {
            Conversation conv = createTestConversation(testUserId, 100L + i, 0);
            conversationMapper.insert(conv);
        }

        // Act
        ConversationQueryDTO queryDTO = ConversationQueryDTO.builder()
            .page(1)
            .pageSize(20)
            .build();
        Page<ConversationVO> result = messageService.getConversations(testUserId, queryDTO);

        // Assert
        assertEquals(20, result.getRecords().size());
        assertEquals(25, result.getTotal());
        assertTrue(result.getPages() == 2);
    }

    @Test
    void testSendMessage_Text_Success() {
        // Arrange
        MessageSendDTO sendDTO = MessageSendDTO.builder()
            .conversationId(null)  // New conversation
            .receiverId(testOtherUserId)
            .messageType("text")
            .content("Test message")
            .build();

        // Act
        MessageVO result = messageService.sendMessage(testUserId, sendDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessageId());
        assertEquals("text", result.getMessageType());
        assertEquals("Test message", result.getContent());
        assertEquals(1, result.getStatus());  // Delivered

        // Verify conversation created
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, testUserId)
               .eq(Conversation::getOtherUserId, testOtherUserId);
        Conversation conversation = conversationMapper.selectOne(wrapper);
        assertNotNull(conversation);
    }

    @Test
    void testSendMessage_TextTooLong_ThrowsException() {
        // Arrange
        String longContent = "a".repeat(501);  // Max is 500
        MessageSendDTO sendDTO = MessageSendDTO.builder()
            .receiverId(testOtherUserId)
            .messageType("text")
            .content(longContent)
            .build();

        // Act & Assert
        assertThrows(ServiceException.class, () -> {
            messageService.sendMessage(testUserId, sendDTO);
        });
    }

    @Test
    void testMarkMessagesAsRead_Success() {
        // Arrange: Create conversation with unread messages
        Conversation conversation = createTestConversation(testUserId, testOtherUserId, 3);
        conversationMapper.insert(conversation);

        Message msg1 = createTestMessage(conversation.getId(), testOtherUserId, testUserId, 1);
        Message msg2 = createTestMessage(conversation.getId(), testOtherUserId, testUserId, 1);
        Message msg3 = createTestMessage(conversation.getId(), testOtherUserId, testUserId, 1);
        messageMapper.insert(msg1);
        messageMapper.insert(msg2);
        messageMapper.insert(msg3);

        // Act
        Integer readCount = messageService.markMessagesAsRead(testUserId, conversation.getId());

        // Assert
        assertEquals(3, readCount);

        // Verify messages marked as read
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversation.getId())
               .eq(Message::getStatus, 2);  // Read
        long count = messageMapper.selectCount(wrapper);
        assertEquals(3, count);

        // Verify unread count cleared
        Conversation updated = conversationMapper.selectById(conversation.getId());
        assertEquals(0, updated.getUnreadCount());
    }

    @Test
    void testRecallMessage_WithinTimeLimit_Success() {
        // Arrange: Create message (just created, within 2-minute window)
        Conversation conversation = createTestConversation(testUserId, testOtherUserId, 0);
        conversationMapper.insert(conversation);

        Message message = createTestMessage(conversation.getId(), testUserId, testOtherUserId, 1);
        messageMapper.insert(message);

        // Act
        assertDoesNotThrow(() -> {
            messageService.recallMessage(testUserId, message.getId());
        });

        // Assert: Message marked as recalled
        Message recalled = messageMapper.selectById(message.getId());
        assertTrue(recalled.getIsRecalled());
        assertNotNull(recalled.getRecalledAt());
    }

    @Test
    void testRecallMessage_AfterTimeLimit_ThrowsException() {
        // Arrange: Create old message (simulate 3 minutes ago)
        Conversation conversation = createTestConversation(testUserId, testOtherUserId, 0);
        conversationMapper.insert(conversation);

        Message message = createTestMessage(conversation.getId(), testUserId, testOtherUserId, 1);
        message.setCreateTime(LocalDateTime.now().minusMinutes(3));
        messageMapper.insert(message);

        // Act & Assert
        assertThrows(ServiceException.class, () -> {
            messageService.recallMessage(testUserId, message.getId());
        });
    }

    @Test
    void testDeleteConversation_Success() {
        // Arrange
        Conversation conversation = createTestConversation(testUserId, testOtherUserId, 0);
        conversationMapper.insert(conversation);

        // Act
        messageService.deleteConversation(testUserId, conversation.getId());

        // Assert: Soft delete
        Conversation deleted = conversationMapper.selectById(conversation.getId());
        assertNull(deleted);  // @TableLogic filters deleted records

        // Direct query to verify soft delete flag
        Conversation raw = conversationMapper.selectOne(
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getId, conversation.getId())
                .eq(Conversation::getDeleted, 1)
        );
        assertNotNull(raw);
        assertNotNull(raw.getDeletedAt());
    }

    // Helper methods
    private Conversation createTestConversation(Long userId, Long otherUserId, int unreadCount) {
        return Conversation.builder()
            .userId(userId)
            .otherUserId(otherUserId)
            .lastMessage("Test message")
            .lastMessageTime(LocalDateTime.now())
            .unreadCount(unreadCount)
            .build();
    }

    private Message createTestMessage(Long conversationId, Long senderId, Long receiverId, int status) {
        return Message.builder()
            .conversationId(conversationId)
            .senderId(senderId)
            .receiverId(receiverId)
            .messageType("text")
            .content("Test content")
            .status(status)
            .isRecalled(false)
            .build();
    }
}
```

**Test Coverage:**
- ✅ Get unread count
- ✅ Get conversations with pagination
- ✅ Send text message (new conversation)
- ✅ Send message with validation errors
- ✅ Mark messages as read
- ✅ Recall message (within time limit)
- ✅ Recall message (after time limit - error)
- ✅ Delete conversation

---

## 🔗 Integration Tests

### API Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken = "test-token-123";  // Mock Sa-Token

    @Test
    void testGetUnreadCount_Authenticated() throws Exception {
        mockMvc.perform(get("/api/message/unread-count")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").exists())
            .andExpect(jsonPath("$.data.likes").exists());
    }

    @Test
    void testSendMessage_ValidRequest() throws Exception {
        MessageSendDTO request = MessageSendDTO.builder()
            .receiverId(2L)
            .messageType("text")
            .content("Integration test message")
            .build();

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.messageId").exists())
            .andExpect(jsonPath("$.data.messageType").value("text"));
    }

    @Test
    void testUploadFile_Image() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/message/upload")
                .file(file)
                .param("fileType", "image")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.mediaUrl").exists());
    }
}
```

---

## 📡 API Test Cases

### Test Case Template

```
TC-{Module}-{Number}: {Test Name}
Priority: High/Medium/Low
Type: Positive/Negative/Edge Case

Preconditions:
- Service running on port 9404
- Test user authenticated
- Database clean/seeded

Steps:
1. Step 1
2. Step 2
3. Step 3

Expected Result:
- Result 1
- Result 2

Actual Result:
[To be filled during execution]

Status: Pass/Fail
```

### Test Cases

#### TC-MSG-001: Get Unread Count - Success

**Priority:** High
**Type:** Positive

**Preconditions:**
- User ID 1 is authenticated
- User has 5 unread messages in conversation

**Steps:**
```bash
curl -X GET "http://localhost:9404/api/message/unread-count" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "likes": 0,
    "comments": 0,
    "followers": 0,
    "system": 0,
    "total": 5
  }
}
```

**Success Criteria:**
- HTTP 200
- Total count matches database
- Response time < 200ms

---

#### TC-MSG-002: Send Text Message - New Conversation

**Priority:** High
**Type:** Positive

**Preconditions:**
- User ID 1 is authenticated
- No existing conversation with User ID 2

**Steps:**
```bash
curl -X POST "http://localhost:9404/api/message/send" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": 2,
    "messageType": "text",
    "content": "Hello, this is a test message!"
  }'
```

**Expected Result:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "messageId": 123456789,
    "conversationId": 987654321,
    "senderId": 1,
    "receiverId": 2,
    "messageType": "text",
    "content": "Hello, this is a test message!",
    "status": 1,
    "isRecalled": false,
    "createdAt": "2025-01-14T10:30:00"
  }
}
```

**Verification:**
```sql
-- Check conversation created (bidirectional)
SELECT COUNT(*) FROM conversation
WHERE (user_id = 1 AND other_user_id = 2)
   OR (user_id = 2 AND other_user_id = 1);
-- Expected: 2

-- Check message saved
SELECT * FROM message WHERE id = {messageId};
-- Expected: 1 row, status=1
```

---

#### TC-MSG-003: Send Text Message - Content Too Long

**Priority:** High
**Type:** Negative

**Steps:**
```bash
curl -X POST "http://localhost:9404/api/message/send" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": 2,
    "messageType": "text",
    "content": "{'a' * 501}"  # 501 characters
  }'
```

**Expected Result:**
```json
{
  "code": 400,
  "message": "文字消息不能超过500字符",
  "data": null
}
```

---

#### TC-MSG-004: Get Chat History - With Pagination

**Priority:** High
**Type:** Positive

**Preconditions:**
- Conversation ID 123 exists
- Conversation has 50 messages
- User has access to conversation

**Steps:**
```bash
# Page 1
curl -X GET "http://localhost:9404/api/message/chat/123?page=1&pageSize=20" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:**
```json
{
  "code": 200,
  "data": {
    "records": [  // Array of 20 messages
      {
        "messageId": 999,
        "messageType": "text",
        "content": "Most recent message",
        ...
      },
      ...
    ],
    "total": 50,
    "size": 20,
    "current": 1,
    "pages": 3
  }
}
```

---

#### TC-MSG-005: Recall Message - Within Time Limit

**Priority:** High
**Type:** Positive

**Preconditions:**
- Message ID 456 was sent < 2 minutes ago
- User is the sender

**Steps:**
```bash
curl -X POST "http://localhost:9404/api/message/recall/456" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**Verification:**
```sql
SELECT is_recalled, recalled_at FROM message WHERE id = 456;
-- Expected: is_recalled=1, recalled_at IS NOT NULL
```

---

#### TC-MSG-006: Recall Message - After Time Limit

**Priority:** High
**Type:** Negative

**Preconditions:**
- Message ID 789 was sent > 2 minutes ago
- User is the sender

**Steps:**
```bash
curl -X POST "http://localhost:9404/api/message/recall/789" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:**
```json
{
  "code": 400,
  "message": "超过2分钟,无法撤回",
  "data": null
}
```

---

#### TC-MSG-007: Mark Messages As Read

**Priority:** High
**Type:** Positive

**Preconditions:**
- Conversation ID 123 exists
- Conversation has 5 unread messages (status=1)

**Steps:**
```bash
curl -X PUT "http://localhost:9404/api/message/read/123" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 5
}
```

**Verification:**
```sql
-- Check messages marked as read
SELECT COUNT(*) FROM message
WHERE conversation_id = 123 AND status = 2;
-- Expected: 5

-- Check unread count cleared
SELECT unread_count FROM conversation WHERE id = 123;
-- Expected: 0
```

---

#### TC-MSG-008: Upload Image File

**Priority:** High
**Type:** Positive

**Steps:**
```bash
curl -X POST "http://localhost:9404/api/message/upload" \
  -H "Authorization: Bearer {token}" \
  -F "file=@test-image.jpg" \
  -F "fileType=image"
```

**Expected Result:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "mediaUrl": "https://oss.example.com/chat/images/xxx-xxx-xxx.jpg",
    "fileType": "image"
  }
}
```

**Verification:**
- File exists in OSS
- File size < 10MB
- File type is jpg/jpeg/png/gif/webp

---

#### TC-MSG-009: Upload File - Size Exceeds Limit

**Priority:** Medium
**Type:** Negative

**Steps:**
```bash
curl -X POST "http://localhost:9404/api/message/upload" \
  -H "Authorization: Bearer {token}" \
  -F "file=@large-image.jpg" \  # 15MB file
  -F "fileType=image"
```

**Expected Result:**
```json
{
  "code": 400,
  "message": "图片文件大小不能超过 10MB",
  "data": null
}
```

---

#### TC-MSG-010: Delete Conversation

**Priority:** High
**Type:** Positive

**Preconditions:**
- Conversation ID 123 exists
- User owns the conversation

**Steps:**
```bash
curl -X DELETE "http://localhost:9404/api/message/conversation/123" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**Verification:**
```sql
-- Conversation soft deleted
SELECT deleted, deleted_at FROM conversation WHERE id = 123;
-- Expected: deleted=1, deleted_at IS NOT NULL
```

---

### Complete Test Matrix

| TC ID | Test Case | Priority | Type | Status |
|-------|-----------|----------|------|--------|
| TC-MSG-001 | Get Unread Count | High | Positive | ⏳ |
| TC-MSG-002 | Send Text Message (New) | High | Positive | ⏳ |
| TC-MSG-003 | Send Text (Too Long) | High | Negative | ⏳ |
| TC-MSG-004 | Get Chat History | High | Positive | ⏳ |
| TC-MSG-005 | Recall (Within Limit) | High | Positive | ⏳ |
| TC-MSG-006 | Recall (After Limit) | High | Negative | ⏳ |
| TC-MSG-007 | Mark As Read | High | Positive | ⏳ |
| TC-MSG-008 | Upload Image | High | Positive | ⏳ |
| TC-MSG-009 | Upload (Size Limit) | Medium | Negative | ⏳ |
| TC-MSG-010 | Delete Conversation | High | Positive | ⏳ |
| TC-MSG-011 | Send Image Message | High | Positive | ⏳ |
| TC-MSG-012 | Send Voice Message | High | Positive | ⏳ |
| TC-MSG-013 | Send Video Message | High | Positive | ⏳ |
| TC-MSG-014 | Delete Message | High | Positive | ⏳ |
| TC-MSG-015 | Clear All Messages | High | Positive | ⏳ |
| TC-MSG-016 | Unauthorized Access | Critical | Security | ⏳ |
| TC-MSG-017 | Invalid Token | Critical | Security | ⏳ |
| TC-MSG-018 | Delete Other's Message | Critical | Security | ⏳ |

---

## 🌐 WebSocket Test Cases

### WebSocket Test Setup

**Install wscat:**
```bash
npm install -g wscat
```

**Or use JavaScript:**
```html
<!DOCTYPE html>
<html>
<head><title>WebSocket Tester</title></head>
<body>
  <script>
    const ws = new WebSocket('ws://localhost:9404/ws?userId=1&token=test-token');

    ws.onopen = () => {
      console.log('Connected');
      // Send heartbeat
      setInterval(() => {
        ws.send(JSON.stringify({ type: 'heartbeat' }));
      }, 30000);
    };

    ws.onmessage = (event) => {
      console.log('Received:', JSON.parse(event.data));
    };

    ws.onerror = (error) => {
      console.error('Error:', error);
    };

    ws.onclose = () => {
      console.log('Disconnected');
    };
  </script>
</body>
</html>
```

### TC-WS-001: WebSocket Connection

**Priority:** Critical
**Type:** Positive

**Steps:**
```bash
wscat -c "ws://localhost:9404/ws?userId=1&token=test-token"
```

**Expected Result:**
```json
// Connection established
Connected

// Receive connection success message
{
  "type": "connection_success",
  "data": {
    "userId": 1,
    "message": "WebSocket连接成功"
  },
  "timestamp": 1705219800000
}
```

**Verification:**
```bash
# Check Redis
redis-cli GET "chat:online:1"
# Expected: "1"
```

---

### TC-WS-002: Heartbeat Mechanism

**Priority:** High
**Type:** Positive

**Steps:**
```javascript
// Send heartbeat
ws.send(JSON.stringify({
  type: 'heartbeat'
}));
```

**Expected Result:**
```json
{
  "type": "heartbeat_ack",
  "data": {
    "timestamp": 1705219800000
  },
  "timestamp": 1705219800000
}
```

---

### TC-WS-003: New Message Event

**Priority:** Critical
**Type:** Positive

**Preconditions:**
- User 1 connected via WebSocket
- User 2 sends message to User 1

**Steps:**
```bash
# User 2 sends message via REST API
curl -X POST "http://localhost:9404/api/message/send" \
  -H "Authorization: Bearer {user2-token}" \
  -d '{
    "receiverId": 1,
    "messageType": "text",
    "content": "Hello User 1!"
  }'
```

**Expected Result (User 1's WebSocket):**
```json
{
  "type": "new_message",
  "data": {
    "messageId": 123456,
    "senderId": 2,
    "messageType": "text",
    "content": "Hello User 1!",
    "mediaUrl": "",
    "timestamp": "2025-01-14T10:30:00"
  },
  "timestamp": 1705219800000
}
```

---

### TC-WS-004: Message Recalled Event

**Priority:** High
**Type:** Positive

**Preconditions:**
- User 1 connected via WebSocket
- User 2 recalls a message sent to User 1

**Steps:**
```bash
# User 2 recalls message
curl -X POST "http://localhost:9404/api/message/recall/456" \
  -H "Authorization: Bearer {user2-token}"
```

**Expected Result (User 1's WebSocket):**
```json
{
  "type": "message_recalled",
  "data": {
    "messageId": 456,
    "senderId": 2,
    "timestamp": 1705219800000
  },
  "timestamp": 1705219800000
}
```

---

### TC-WS-005: Typing Indicator

**Priority:** Medium
**Type:** Positive

**Steps:**
```javascript
// User 1 sends typing event
ws.send(JSON.stringify({
  type: 'typing',
  targetUserId: 2
}));
```

**Expected Result (User 2's WebSocket):**
```json
{
  "type": "typing",
  "data": {
    "userId": 1,
    "isTyping": true
  },
  "timestamp": 1705219800000
}
```

---

### TC-WS-006: Disconnect and Cleanup

**Priority:** High
**Type:** Positive

**Steps:**
```javascript
// Close connection
ws.close();
```

**Expected Result:**
- WebSocket closed gracefully
- Redis online status cleared

**Verification:**
```bash
# Check Redis
redis-cli GET "chat:online:1"
# Expected: (nil)
```

---

## ⚡ Performance Tests

### Load Test Scenarios

#### Scenario 1: Concurrent Message Sending

**Tool:** JMeter or Apache Bench

**Configuration:**
- Concurrent users: 100
- Requests per user: 10
- Total requests: 1,000

**Test Script:**
```bash
# Using Apache Bench
ab -n 1000 -c 100 \
   -p message.json \
   -T application/json \
   -H "Authorization: Bearer test-token" \
   http://localhost:9404/api/message/send
```

**Success Criteria:**
- P50 < 100ms
- P95 < 300ms
- P99 < 500ms
- 0% error rate

---

#### Scenario 2: Conversation List Query

**Configuration:**
- Concurrent users: 200
- Requests per user: 5
- Total requests: 1,000

**Success Criteria:**
- P95 < 200ms
- Cache hit rate > 60%
- Database query count < 400 (due to caching)

---

#### Scenario 3: WebSocket Connections

**Tool:** websocket-bench

**Configuration:**
```bash
npm install -g websocket-bench

websocket-bench \
  -a 1000 \
  -c 100 \
  -g 10 \
  ws://localhost:9404/ws?userId={userId}&token=test-token
```

**Success Criteria:**
- 1,000 concurrent connections supported
- Message delivery < 500ms
- Connection success rate > 99%

---

## 📊 Test Data

### Test Users

```sql
-- Insert test users (assuming user table exists)
INSERT INTO user (id, username, nickname, status) VALUES
(1, 'test_user_1', 'Test User 1', 1),
(2, 'test_user_2', 'Test User 2', 1),
(3, 'test_user_3', 'Test User 3', 1),
(4, 'test_user_4', 'Test User 4', 1),
(5, 'test_user_5', 'Test User 5', 1);
```

### Test Conversations

```sql
-- Create test conversations
INSERT INTO conversation (id, user_id, other_user_id, last_message, last_message_time, unread_count) VALUES
(101, 1, 2, 'Hello from User 2', NOW(), 3),
(102, 2, 1, 'Hello from User 1', NOW(), 0),
(103, 1, 3, 'Test message', NOW() - INTERVAL 1 HOUR, 5),
(104, 3, 1, 'Test message', NOW() - INTERVAL 1 HOUR, 0);
```

### Test Messages

```sql
-- Create test messages
INSERT INTO message (id, conversation_id, sender_id, receiver_id, message_type, content, status, is_recalled, create_time) VALUES
(201, 101, 2, 1, 'text', 'Recent message 1', 1, 0, NOW()),
(202, 101, 2, 1, 'text', 'Recent message 2', 1, 0, NOW() - INTERVAL 1 MINUTE),
(203, 101, 2, 1, 'text', 'Recent message 3', 1, 0, NOW() - INTERVAL 2 MINUTE),
(204, 103, 3, 1, 'text', 'Old message', 1, 0, NOW() - INTERVAL 3 MINUTE),
(205, 103, 1, 3, 'text', 'Recalled message', 1, 1, NOW() - INTERVAL 1 HOUR);
```

### Clean Test Data

```sql
-- Clean up test data
DELETE FROM message WHERE id >= 201;
DELETE FROM conversation WHERE id >= 101;
DELETE FROM user WHERE id <= 5;
```

---

## 🚀 Test Execution

### Manual Testing Checklist

#### Pre-Test Setup
- [ ] Start MySQL database
- [ ] Start Redis server
- [ ] Start Nacos server
- [ ] Run database migrations
- [ ] Insert test data
- [ ] Start xypai-chat service
- [ ] Verify health endpoint

#### During Testing
- [ ] Execute test cases in order
- [ ] Document actual results
- [ ] Take screenshots for failures
- [ ] Record response times
- [ ] Check logs for errors

#### Post-Test
- [ ] Generate test report
- [ ] Clean up test data
- [ ] Archive test results
- [ ] File bug reports

### Automated Testing

```bash
# Run all unit tests
cd E:/Users/Administrator/Documents/GitHub/RuoYi-Cloud-Plus/xypai-chat
mvn clean test

# Run specific test class
mvn test -Dtest=MessageServiceImplTest

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Postman Collection

**Import collection:**
```json
{
  "info": {
    "name": "XiangYuPai Chat API Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Unread Count",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/message/unread-count",
          "host": ["{{baseUrl}}"],
          "path": ["api", "message", "unread-count"]
        }
      },
      "response": []
    }
    // ... more test cases
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:9404"
    },
    {
      "key": "token",
      "value": "test-token-123"
    }
  ]
}
```

---

## 🐛 Bug Reporting

### Bug Report Template

```markdown
## Bug ID: BUG-{Module}-{Number}

### Title
[Brief description of the bug]

### Severity
- [ ] Critical (System down)
- [ ] High (Major feature broken)
- [ ] Medium (Feature partially working)
- [ ] Low (Minor issue)

### Environment
- Service: xypai-chat
- Version: 1.0.0
- Port: 9404
- Database: MySQL 8.0.35
- Redis: 7.0.15

### Test Case
TC-MSG-XXX: [Test case name]

### Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

### Expected Behavior
[What should happen]

### Actual Behavior
[What actually happened]

### Screenshots
[Attach screenshots if applicable]

### Logs
```
[Paste relevant logs]
```

### Possible Cause
[Your analysis of the root cause]

### Suggested Fix
[Optional: Suggest how to fix]
```

---

## 📈 Test Report Template

```markdown
# Test Execution Report

**Date:** 2025-01-14
**Tester:** [Name]
**Version:** 1.0.0

## Summary

- Total Test Cases: 30
- Executed: 30
- Passed: 28
- Failed: 2
- Blocked: 0
- Pass Rate: 93.3%

## Test Results by Module

| Module | Total | Passed | Failed | Pass Rate |
|--------|-------|--------|--------|-----------|
| REST APIs | 15 | 14 | 1 | 93.3% |
| WebSocket | 6 | 6 | 0 | 100% |
| RPC | 3 | 3 | 0 | 100% |
| Performance | 6 | 5 | 1 | 83.3% |

## Failed Test Cases

### TC-MSG-003: Send Text (Too Long)
- **Status:** Failed
- **Reason:** Error message incorrect
- **Bug:** BUG-MSG-001

### TC-PERF-002: Load Test
- **Status:** Failed
- **Reason:** P95 exceeded 300ms (actual: 350ms)
- **Bug:** BUG-PERF-001

## Performance Metrics

- Average Response Time: 120ms
- P95 Response Time: 280ms
- P99 Response Time: 450ms
- Cache Hit Rate: 65%
- Concurrent Users Supported: 100

## Recommendations

1. Optimize query for conversation list
2. Fix error message validation
3. Add index on message.status

## Conclusion

Overall test execution was successful with 93.3% pass rate.
Two non-critical issues identified and documented.
System ready for UAT.
```

---

**Test Documentation Version:** 1.0
**Last Updated:** 2025-01-14
**Status:** ✅ Ready for Test Execution
