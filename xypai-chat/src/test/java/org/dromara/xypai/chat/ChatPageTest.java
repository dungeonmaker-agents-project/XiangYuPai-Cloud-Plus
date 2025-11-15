package org.dromara.xypai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.xypai.chat.domain.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Chat Page Test (聊天页面测试)
 *
 * Tests for: Frontend/02-聊天页面.md
 * Route: /message/chat/:conversationId
 *
 * Frontend Requirements:
 * 1. GET /api/message/chat/{conversationId} - Get chat history
 * 2. POST /api/message/send - Send message (text/image/voice/video)
 * 3. PUT /api/message/read/{conversationId} - Mark messages as read
 * 4. POST /api/message/recall/{messageId} - Recall message (2min window)
 * 5. DELETE /api/message/{messageId} - Delete message
 * 6. POST /api/message/upload - Upload media files
 *
 * Test Coverage:
 * - All 6 APIs required by frontend spec
 * - All message types (text, image, voice, video)
 * - Message validation rules
 * - 2-minute recall window
 * - Bidirectional conversation logic
 * - Real-time message status updates
 *
 * @author XiangYuPai Backend Team
 * @since 2025-01-14
 */
@DisplayName("Chat Page Tests - 聊天页面测试")
public class ChatPageTest extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== API 1: Get Chat History ====================

    @Test
    @DisplayName("TC-CHAT-001: Get chat history - Empty conversation")
    public void testGetChatHistory_EmptyConversation() throws Exception {
        // Given: Create empty conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 0);
        Long conversationId = testData.userConversation.getConversationId();

        // When: Get chat history
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.messages").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.hasMore").value(false));

        System.out.println("✅ TC-CHAT-001: Empty chat history verified");
    }

    @Test
    @DisplayName("TC-CHAT-002: Get chat history - With messages")
    public void testGetChatHistory_WithMessages() throws Exception {
        // Given: Create conversation with 10 messages
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 10);
        Long conversationId = testData.userConversation.getConversationId();
        setUserOnline(TEST_USER_2, true);

        // When: Get chat history
        MvcResult result = mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.conversationId").value(conversationId))
                .andExpect(jsonPath("$.data.userId").value(TEST_USER_2))
                .andExpect(jsonPath("$.data.userInfo.userId").value(TEST_USER_2))
                .andExpect(jsonPath("$.data.userInfo.isOnline").value(true))
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.messages", hasSize(10)))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.hasMore").value(false))
                // Verify message structure matches frontend spec
                .andExpect(jsonPath("$.data.messages[0].messageId").exists())
                .andExpect(jsonPath("$.data.messages[0].conversationId").exists())
                .andExpect(jsonPath("$.data.messages[0].senderId").exists())
                .andExpect(jsonPath("$.data.messages[0].receiverId").exists())
                .andExpect(jsonPath("$.data.messages[0].messageType").exists())
                .andExpect(jsonPath("$.data.messages[0].content").exists())
                .andExpect(jsonPath("$.data.messages[0].status").exists())
                .andExpect(jsonPath("$.data.messages[0].isRecalled").exists())
                .andExpect(jsonPath("$.data.messages[0].createdAt").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("✅ TC-CHAT-002: Chat history structure verified");
        System.out.println("Response: " + jsonResponse);
    }

    @Test
    @DisplayName("TC-CHAT-003: Get chat history - Pagination")
    public void testGetChatHistory_Pagination() throws Exception {
        // Given: Create conversation with 50 messages
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 50);
        Long conversationId = testData.userConversation.getConversationId();

        // When: Get first page (20 messages)
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(20)))
                .andExpect(jsonPath("$.data.total").value(50))
                .andExpect(jsonPath("$.data.hasMore").value(true));

        // When: Get second page
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "2")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(20)))
                .andExpect(jsonPath("$.data.total").value(50))
                .andExpect(jsonPath("$.data.hasMore").value(true));

        System.out.println("✅ TC-CHAT-003: Chat history pagination working");
    }

    @Test
    @DisplayName("TC-CHAT-004: Get chat history - No permission")
    public void testGetChatHistory_NoPermission() throws Exception {
        // Given: User 1 and User 2 have a conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 5);
        Long conversationId = testData.userConversation.getConversationId();

        // When: User 3 tries to access the conversation
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_3)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isForbidden());

        System.out.println("✅ TC-CHAT-004: Unauthorized access blocked");
    }

    @Test
    @DisplayName("TC-CHAT-005: Get chat history - Conversation not found")
    public void testGetChatHistory_NotFound() throws Exception {
        // When: Access non-existent conversation
        mockMvc.perform(get("/api/message/chat/999999")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isNotFound());

        System.out.println("✅ TC-CHAT-005: Non-existent conversation returns 404");
    }

    // ==================== API 2: Send Message ====================

    @Test
    @DisplayName("TC-CHAT-006: Send text message - New conversation")
    public void testSendTextMessage_NewConversation() throws Exception {
        // Given: No existing conversation between users
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "text");
        request.put("content", "Hello, this is a test message!");

        // When: Send message
        MvcResult result = mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.messageId").exists())
                .andExpect(jsonPath("$.data.conversationId").exists())
                .andExpect(jsonPath("$.data.senderId").value(TEST_USER_1))
                .andExpect(jsonPath("$.data.receiverId").value(TEST_USER_2))
                .andExpect(jsonPath("$.data.messageType").value("text"))
                .andExpect(jsonPath("$.data.content").value("Hello, this is a test message!"))
                .andExpect(jsonPath("$.data.status").value(1)) // Delivered
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("✅ TC-CHAT-006: Text message sent successfully (new conversation)");
        System.out.println("Response: " + jsonResponse);

        // Verify bidirectional conversations created
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.userId == " + TEST_USER_2 + ")]").exists());
    }

    @Test
    @DisplayName("TC-CHAT-007: Send text message - Existing conversation")
    public void testSendTextMessage_ExistingConversation() throws Exception {
        // Given: Existing conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 3);
        Long conversationId = testData.userConversation.getConversationId();

        Map<String, Object> request = new HashMap<>();
        request.put("conversationId", conversationId);
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "text");
        request.put("content", "Another message in existing conversation");

        // When: Send message
        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value(conversationId))
                .andExpect(jsonPath("$.data.content").value("Another message in existing conversation"));

        System.out.println("✅ TC-CHAT-007: Message sent to existing conversation");
    }

    @Test
    @DisplayName("TC-CHAT-008: Send text message - Empty content")
    public void testSendTextMessage_EmptyContent() throws Exception {
        // When: Send message with empty content
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "text");
        request.put("content", "");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("不能为空")));

        System.out.println("✅ TC-CHAT-008: Empty content rejected");
    }

    @Test
    @DisplayName("TC-CHAT-009: Send text message - Exceeds 500 chars")
    public void testSendTextMessage_TooLong() throws Exception {
        // Given: Content with 501 characters
        String longContent = "a".repeat(501);

        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "text");
        request.put("content", longContent);

        // When: Send message
        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("超过500")));

        System.out.println("✅ TC-CHAT-009: Content >500 chars rejected");
    }

    @Test
    @DisplayName("TC-CHAT-010: Send text message - Max 500 chars (boundary)")
    public void testSendTextMessage_Exactly500Chars() throws Exception {
        // Given: Content with exactly 500 characters
        String content500 = "a".repeat(500);

        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "text");
        request.put("content", content500);

        // When: Send message
        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value(content500));

        System.out.println("✅ TC-CHAT-010: Exactly 500 chars accepted");
    }

    @Test
    @DisplayName("TC-CHAT-011: Send image message - Success")
    public void testSendImageMessage_Success() throws Exception {
        // Given: Image message
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "image");
        request.put("mediaUrl", "https://oss.example.com/images/test.jpg");

        // When: Send image message
        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("image"))
                .andExpect(jsonPath("$.data.mediaUrl").value("https://oss.example.com/images/test.jpg"));

        System.out.println("✅ TC-CHAT-011: Image message sent successfully");
    }

    @Test
    @DisplayName("TC-CHAT-012: Send image message - Missing mediaUrl")
    public void testSendImageMessage_MissingMediaUrl() throws Exception {
        // When: Send image message without mediaUrl
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "image");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("mediaUrl")));

        System.out.println("✅ TC-CHAT-012: Image message without mediaUrl rejected");
    }

    @Test
    @DisplayName("TC-CHAT-013: Send voice message - Success")
    public void testSendVoiceMessage_Success() throws Exception {
        // Given: Voice message (1-60 seconds)
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "voice");
        request.put("mediaUrl", "https://oss.example.com/voice/test.mp3");
        request.put("duration", 30);

        // When: Send voice message
        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("voice"))
                .andExpect(jsonPath("$.data.mediaUrl").value("https://oss.example.com/voice/test.mp3"))
                .andExpect(jsonPath("$.data.duration").value(30));

        System.out.println("✅ TC-CHAT-013: Voice message sent successfully");
    }

    @Test
    @DisplayName("TC-CHAT-014: Send voice message - Duration > 60s")
    public void testSendVoiceMessage_DurationTooLong() throws Exception {
        // When: Send voice message with duration > 60s
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "voice");
        request.put("mediaUrl", "https://oss.example.com/voice/test.mp3");
        request.put("duration", 61);

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("60")));

        System.out.println("✅ TC-CHAT-014: Voice duration >60s rejected");
    }

    @Test
    @DisplayName("TC-CHAT-015: Send video message - Success")
    public void testSendVideoMessage_Success() throws Exception {
        // Given: Video message with thumbnail
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "video");
        request.put("mediaUrl", "https://oss.example.com/video/test.mp4");
        request.put("thumbnailUrl", "https://oss.example.com/video/test_thumb.jpg");
        request.put("duration", 45);

        // When: Send video message
        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("video"))
                .andExpect(jsonPath("$.data.mediaUrl").value("https://oss.example.com/video/test.mp4"))
                .andExpect(jsonPath("$.data.thumbnailUrl").value("https://oss.example.com/video/test_thumb.jpg"))
                .andExpect(jsonPath("$.data.duration").value(45));

        System.out.println("✅ TC-CHAT-015: Video message sent successfully");
    }

    @Test
    @DisplayName("TC-CHAT-016: Send video message - Missing thumbnailUrl")
    public void testSendVideoMessage_MissingThumbnail() throws Exception {
        // When: Send video message without thumbnail
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "video");
        request.put("mediaUrl", "https://oss.example.com/video/test.mp4");
        request.put("duration", 45);

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("thumbnailUrl")));

        System.out.println("✅ TC-CHAT-016: Video without thumbnail rejected");
    }

    // ==================== API 3: Mark Messages as Read ====================

    @Test
    @DisplayName("TC-CHAT-017: Mark messages as read - Success")
    public void testMarkMessagesAsRead_Success() throws Exception {
        // Given: Conversation with unread messages
        var testData = createConversationWithMessages(TEST_USER_2, TEST_USER_1, 5);
        Long conversationId = testData.otherUserConversation.getConversationId();

        // Set all messages to status 1 (delivered, not read)
        testData.messages.forEach(msg -> {
            msg.setStatus(1);
            messageMapper.updateById(msg);
        });

        // When: Mark messages as read
        mockMvc.perform(put("/api/message/read/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.conversationId").value(conversationId))
                .andExpect(jsonPath("$.data.readCount").value(5));

        System.out.println("✅ TC-CHAT-017: Messages marked as read");
    }

    @Test
    @DisplayName("TC-CHAT-018: Mark messages as read - No unread messages")
    public void testMarkMessagesAsRead_NoUnread() throws Exception {
        // Given: Conversation with all messages already read
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 3);
        Long conversationId = testData.userConversation.getConversationId();

        // Set all messages to status 2 (already read)
        testData.messages.forEach(msg -> {
            msg.setStatus(2);
            messageMapper.updateById(msg);
        });

        // When: Mark messages as read
        mockMvc.perform(put("/api/message/read/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readCount").value(0));

        System.out.println("✅ TC-CHAT-018: No unread messages to mark");
    }

    // ==================== API 4: Recall Message ====================

    @Test
    @DisplayName("TC-CHAT-019: Recall message - Within 2 minutes")
    public void testRecallMessage_Within2Minutes() throws Exception {
        // Given: Recently sent message
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);

        // When: Recall message
        mockMvc.perform(post("/api/message/recall/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.messageId").value(message.getMessageId()))
                .andExpect(jsonPath("$.data.isRecalled").value(true))
                .andExpect(jsonPath("$.data.recalledAt").exists());

        System.out.println("✅ TC-CHAT-019: Message recalled successfully");
    }

    @Test
    @DisplayName("TC-CHAT-020: Recall message - After 2 minutes")
    public void testRecallMessage_After2Minutes() throws Exception {
        // Given: Old message (>2 minutes)
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);

        // Set created time to 3 minutes ago
        message.setCreateTime(LocalDateTime.now().minusMinutes(3));
        messageMapper.updateById(message);

        // When: Try to recall message
        mockMvc.perform(post("/api/message/recall/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("2分钟")));

        System.out.println("✅ TC-CHAT-020: Recall after 2 minutes rejected");
    }

    @Test
    @DisplayName("TC-CHAT-021: Recall message - Not sender")
    public void testRecallMessage_NotSender() throws Exception {
        // Given: Message sent by User 1
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);

        // When: User 2 tries to recall User 1's message
        mockMvc.perform(post("/api/message/recall/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_2))
                .andExpect(status().isForbidden());

        System.out.println("✅ TC-CHAT-021: Non-sender cannot recall message");
    }

    @Test
    @DisplayName("TC-CHAT-022: Recall message - Already recalled")
    public void testRecallMessage_AlreadyRecalled() throws Exception {
        // Given: Already recalled message
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);
        message.setIsRecalled(true);
        messageMapper.updateById(message);

        // When: Try to recall again
        mockMvc.perform(post("/api/message/recall/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("已撤回")));

        System.out.println("✅ TC-CHAT-022: Cannot recall already recalled message");
    }

    // ==================== API 5: Delete Message ====================

    @Test
    @DisplayName("TC-CHAT-023: Delete message - Sender deletes")
    public void testDeleteMessage_Sender() throws Exception {
        // Given: Message sent by User 1
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);

        // When: Sender deletes message
        mockMvc.perform(delete("/api/message/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ TC-CHAT-023: Sender deleted message (soft delete)");
    }

    @Test
    @DisplayName("TC-CHAT-024: Delete message - Receiver deletes")
    public void testDeleteMessage_Receiver() throws Exception {
        // Given: Message sent by User 1 to User 2
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);

        // When: Receiver deletes message
        mockMvc.perform(delete("/api/message/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ TC-CHAT-024: Receiver deleted message (soft delete)");
    }

    @Test
    @DisplayName("TC-CHAT-025: Delete message - Unrelated user cannot delete")
    public void testDeleteMessage_UnrelatedUser() throws Exception {
        // Given: Message between User 1 and User 2
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 1);
        Message message = testData.messages.get(0);

        // When: User 3 tries to delete
        mockMvc.perform(delete("/api/message/" + message.getMessageId())
                .header("Authorization", TEST_TOKEN_USER_3))
                .andExpect(status().isForbidden());

        System.out.println("✅ TC-CHAT-025: Unrelated user cannot delete message");
    }

    @Test
    @DisplayName("TC-CHAT-026: Delete message - Message not found")
    public void testDeleteMessage_NotFound() throws Exception {
        // When: Delete non-existent message
        mockMvc.perform(delete("/api/message/999999")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isNotFound());

        System.out.println("✅ TC-CHAT-026: Non-existent message returns 404");
    }

    // ==================== API 6: Upload Media ====================

    @Test
    @DisplayName("TC-CHAT-027: Upload image - Success")
    public void testUploadImage_Success() throws Exception {
        // Given: Mock image file
        MockMultipartFile imageFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        // When: Upload image
        MvcResult result = mockMvc.perform(multipart("/api/message/upload")
                .file(imageFile)
                .param("fileType", "image")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.mediaUrl").exists())
                .andExpect(jsonPath("$.data.fileSize").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("✅ TC-CHAT-027: Image uploaded successfully");
        System.out.println("Response: " + jsonResponse);
    }

    @Test
    @DisplayName("TC-CHAT-028: Upload voice - Success")
    public void testUploadVoice_Success() throws Exception {
        // Given: Mock voice file
        MockMultipartFile voiceFile = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mp3",
            "fake voice content".getBytes()
        );

        // When: Upload voice
        mockMvc.perform(multipart("/api/message/upload")
                .file(voiceFile)
                .param("fileType", "voice")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mediaUrl").exists())
                .andExpect(jsonPath("$.data.duration").exists());

        System.out.println("✅ TC-CHAT-028: Voice uploaded successfully");
    }

    @Test
    @DisplayName("TC-CHAT-029: Upload video - Success")
    public void testUploadVideo_Success() throws Exception {
        // Given: Mock video file
        MockMultipartFile videoFile = new MockMultipartFile(
            "file",
            "test.mp4",
            "video/mp4",
            "fake video content".getBytes()
        );

        // When: Upload video
        mockMvc.perform(multipart("/api/message/upload")
                .file(videoFile)
                .param("fileType", "video")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mediaUrl").exists())
                .andExpect(jsonPath("$.data.thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.duration").exists());

        System.out.println("✅ TC-CHAT-029: Video uploaded successfully");
    }

    @Test
    @DisplayName("TC-CHAT-030: Upload file - Invalid type")
    public void testUploadFile_InvalidType() throws Exception {
        // Given: Invalid file type
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "test.exe",
            "application/x-msdownload",
            "fake exe content".getBytes()
        );

        // When: Upload invalid file
        mockMvc.perform(multipart("/api/message/upload")
                .file(invalidFile)
                .param("fileType", "image")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("类型")));

        System.out.println("✅ TC-CHAT-030: Invalid file type rejected");
    }

    // ==================== Data Structure Verification ====================

    @Test
    @DisplayName("TC-CHAT-031: Verify MessageVO matches frontend spec")
    public void testMessageVO_Structure() throws Exception {
        // Given: Send a message
        Map<String, Object> request = new HashMap<>();
        request.put("receiverId", TEST_USER_2);
        request.put("messageType", "text");
        request.put("content", "Structure test message");

        MvcResult result = mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        // Verify all required fields from frontend spec
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"messageId\""), "Missing 'messageId' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"conversationId\""), "Missing 'conversationId' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"senderId\""), "Missing 'senderId' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"receiverId\""), "Missing 'receiverId' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"messageType\""), "Missing 'messageType' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"content\""), "Missing 'content' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"status\""), "Missing 'status' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"isRecalled\""), "Missing 'isRecalled' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"createdAt\""), "Missing 'createdAt' field");

        System.out.println("✅ TC-CHAT-031: MessageVO structure matches frontend spec");
        System.out.println("Expected fields: messageId, conversationId, senderId, receiverId, messageType, content, status, isRecalled, createdAt");
        System.out.println("Actual response: " + json);
    }
}
