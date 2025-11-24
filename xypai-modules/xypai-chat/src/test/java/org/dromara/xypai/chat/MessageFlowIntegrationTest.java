package org.dromara.xypai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Message Flow Integration Test (消息流程集成测试)
 *
 * Tests for: Complete user flows as described in frontend documentation
 *
 * Test Scenarios:
 * 1. New conversation flow - User sends first message to another user
 * 2. Ongoing conversation flow - Users exchange multiple messages
 * 3. Message recall flow - User recalls a message within 2 minutes
 * 4. Read receipt flow - Messages marked as read when viewed
 * 5. Multi-user conversation flow - Multiple conversations for one user
 * 6. Media message flow - Complete flow for sending images/voice/video
 *
 * These tests simulate real user behavior patterns to ensure all
 * components work together correctly.
 *
 * @author XiangYuPai Backend Team
 * @since 2025-01-14
 */
@DisplayName("Message Flow Integration Tests - 消息流程集成测试")
public class MessageFlowIntegrationTest extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== Flow 1: New Conversation ====================

    @Test
    @DisplayName("FLOW-001: New conversation complete flow")
    public void testNewConversationFlow() throws Exception {
        System.out.println("\n=== FLOW-001: New Conversation Flow ===");

        // Step 1: User 1 checks their conversation list (should be empty)
        System.out.println("Step 1: User 1 checks conversation list");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        // Step 2: User 1 sends first message to User 2
        System.out.println("Step 2: User 1 sends first message to User 2");
        Map<String, Object> sendRequest = new HashMap<>();
        sendRequest.put("receiverId", TEST_USER_2);
        sendRequest.put("messageType", "text");
        sendRequest.put("content", "Hello User 2! This is our first message.");

        MvcResult sendResult = mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").exists())
                .andExpect(jsonPath("$.data.conversationId").exists())
                .andExpect(jsonPath("$.data.status").value(1)) // Delivered
                .andReturn();

        String sendResponse = sendResult.getResponse().getContentAsString();
        Map<String, Object> sendData = objectMapper.readValue(sendResponse, Map.class);
        Map<String, Object> messageData = (Map<String, Object>) sendData.get("data");
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        System.out.println("  → Message sent, conversation ID: " + conversationId);

        // Step 3: User 1 checks conversation list (should now have 1 conversation)
        System.out.println("Step 3: User 1 checks conversation list (should show new conversation)");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].conversationId").value(conversationId))
                .andExpect(jsonPath("$.data.records[0].userId").value(TEST_USER_2))
                .andExpect(jsonPath("$.data.records[0].lastMessage").value("Hello User 2! This is our first message."));
        System.out.println("  → Conversation appears in User 1's list");

        // Step 4: User 2 checks their conversation list (should also have the conversation)
        System.out.println("Step 4: User 2 checks conversation list (bidirectional)");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_2)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].userId").value(TEST_USER_1));
        System.out.println("  → Conversation also appears in User 2's list (bidirectional)");

        // Step 5: User 2 opens chat and views the message
        System.out.println("Step 5: User 2 opens chat and views messages");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_2)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(1)))
                .andExpect(jsonPath("$.data.messages[0].content").value("Hello User 2! This is our first message."));
        System.out.println("  → User 2 can view the message");

        System.out.println("✅ FLOW-001 COMPLETE: New conversation flow successful\n");
    }

    // ==================== Flow 2: Ongoing Conversation ====================

    @Test
    @DisplayName("FLOW-002: Ongoing conversation with multiple messages")
    public void testOngoingConversationFlow() throws Exception {
        System.out.println("\n=== FLOW-002: Ongoing Conversation Flow ===");

        // Setup: Create initial conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 2);
        Long conversationId = testData.userConversation.getId();
        System.out.println("Setup: Initial conversation created with 2 messages");

        // Step 1: User 2 replies to the conversation
        System.out.println("Step 1: User 2 replies to the conversation");
        Map<String, Object> replyRequest = new HashMap<>();
        replyRequest.put("conversationId", conversationId);
        replyRequest.put("receiverId", TEST_USER_1);
        replyRequest.put("messageType", "text");
        replyRequest.put("content", "Thanks for the message!");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value(conversationId));
        System.out.println("  → User 2 sent reply");

        // Step 2: User 1 views chat history (should see 3 messages now)
        System.out.println("Step 2: User 1 views chat history");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(3)))
                .andExpect(jsonPath("$.data.total").value(3));
        System.out.println("  → User 1 sees all 3 messages");

        // Step 3: User 1 sends another message
        System.out.println("Step 3: User 1 sends another message");
        Map<String, Object> anotherMessage = new HashMap<>();
        anotherMessage.put("conversationId", conversationId);
        anotherMessage.put("receiverId", TEST_USER_2);
        anotherMessage.put("messageType", "text");
        anotherMessage.put("content", "Great! Let's keep chatting.");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(anotherMessage)))
                .andExpect(status().isOk());
        System.out.println("  → User 1 sent another message");

        // Step 4: Verify conversation has 4 messages total
        System.out.println("Step 4: Verify conversation now has 4 messages");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(4)))
                .andExpect(jsonPath("$.data.total").value(4));
        System.out.println("  → Confirmed: 4 messages in conversation");

        System.out.println("✅ FLOW-002 COMPLETE: Ongoing conversation flow successful\n");
    }

    // ==================== Flow 3: Message Recall ====================

    @Test
    @DisplayName("FLOW-003: Message recall flow (within 2 minutes)")
    public void testMessageRecallFlow() throws Exception {
        System.out.println("\n=== FLOW-003: Message Recall Flow ===");

        // Step 1: User 1 sends a message
        System.out.println("Step 1: User 1 sends a message");
        Map<String, Object> sendRequest = new HashMap<>();
        sendRequest.put("receiverId", TEST_USER_2);
        sendRequest.put("messageType", "text");
        sendRequest.put("content", "Oops, I made a typo!");

        MvcResult sendResult = mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String sendResponse = sendResult.getResponse().getContentAsString();
        Map<String, Object> sendData = objectMapper.readValue(sendResponse, Map.class);
        Map<String, Object> messageData = (Map<String, Object>) sendData.get("data");
        Long messageId = Long.valueOf(messageData.get("messageId").toString());
        System.out.println("  → Message sent with ID: " + messageId);

        // Step 2: User 1 immediately recalls the message (within 2 minutes)
        System.out.println("Step 2: User 1 recalls the message (within 2 minutes)");
        mockMvc.perform(post("/api/message/recall/" + messageId)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRecalled").value(true))
                .andExpect(jsonPath("$.data.recalledAt").exists());
        System.out.println("  → Message recalled successfully");

        // Step 3: Verify message shows as recalled
        System.out.println("Step 3: Verify message shows as recalled");
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].isRecalled").value(true));
        System.out.println("  → Message appears as recalled in chat history");

        // Step 4: Verify cannot recall again
        System.out.println("Step 4: Verify cannot recall the same message again");
        mockMvc.perform(post("/api/message/recall/" + messageId)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isBadRequest());
        System.out.println("  → Double-recall properly rejected");

        System.out.println("✅ FLOW-003 COMPLETE: Message recall flow successful\n");
    }

    // ==================== Flow 4: Read Receipt ====================

    @Test
    @DisplayName("FLOW-004: Read receipt flow")
    public void testReadReceiptFlow() throws Exception {
        System.out.println("\n=== FLOW-004: Read Receipt Flow ===");

        // Setup: User 2 sends message to User 1
        System.out.println("Setup: User 2 sends message to User 1");
        Map<String, Object> sendRequest = new HashMap<>();
        sendRequest.put("receiverId", TEST_USER_1);
        sendRequest.put("messageType", "text");
        sendRequest.put("content", "Hi User 1, please read this!");

        MvcResult sendResult = mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1)) // Delivered (not read yet)
                .andReturn();

        String sendResponse = sendResult.getResponse().getContentAsString();
        Map<String, Object> sendData = objectMapper.readValue(sendResponse, Map.class);
        Map<String, Object> messageData = (Map<String, Object>) sendData.get("data");
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        System.out.println("  → Message sent with status 1 (Delivered)");

        // Step 1: User 1 opens the chat (views the message)
        System.out.println("Step 1: User 1 opens chat to view message");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(greaterThanOrEqualTo(1))));
        System.out.println("  → User 1 viewed the message");

        // Step 2: User 1 marks messages as read
        System.out.println("Step 2: User 1 marks messages as read");
        mockMvc.perform(put("/api/message/read/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readCount", greaterThanOrEqualTo(1)));
        System.out.println("  → Messages marked as read");

        // Step 3: Verify message status changed to read (status 2)
        System.out.println("Step 3: Verify message status changed to read");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].status").value(2)); // Read
        System.out.println("  → Message status updated to 2 (Read)");

        System.out.println("✅ FLOW-004 COMPLETE: Read receipt flow successful\n");
    }

    // ==================== Flow 5: Multi-User Conversations ====================

    @Test
    @DisplayName("FLOW-005: User managing multiple conversations")
    public void testMultiUserConversationsFlow() throws Exception {
        System.out.println("\n=== FLOW-005: Multi-User Conversations Flow ===");

        // Step 1: User 1 starts conversation with User 2
        System.out.println("Step 1: User 1 starts conversation with User 2");
        Map<String, Object> msg1 = new HashMap<>();
        msg1.put("receiverId", TEST_USER_2);
        msg1.put("messageType", "text");
        msg1.put("content", "Hello User 2!");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg1)))
                .andExpect(status().isOk());
        System.out.println("  → Conversation 1 created with User 2");

        // Step 2: User 1 starts conversation with User 3
        System.out.println("Step 2: User 1 starts conversation with User 3");
        Map<String, Object> msg2 = new HashMap<>();
        msg2.put("receiverId", TEST_USER_3);
        msg2.put("messageType", "text");
        msg2.put("content", "Hello User 3!");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg2)))
                .andExpect(status().isOk());
        System.out.println("  → Conversation 2 created with User 3");

        // Step 3: User 1 checks conversation list (should have 2 conversations)
        System.out.println("Step 3: User 1 checks conversation list");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records", hasSize(2)))
                .andExpect(jsonPath("$.data.total").value(2));
        System.out.println("  → User 1 has 2 active conversations");

        // Step 4: User 2 sends reply
        System.out.println("Step 4: User 2 sends reply to User 1");
        Map<String, Object> reply = new HashMap<>();
        reply.put("receiverId", TEST_USER_1);
        reply.put("messageType", "text");
        reply.put("content", "Hi User 1!");

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reply)))
                .andExpect(status().isOk());
        System.out.println("  → User 2 replied");

        // Step 5: User 1's conversation list should show User 2's conversation at top (most recent)
        System.out.println("Step 5: Verify conversations sorted by most recent message");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].lastMessage").value("Hi User 1!"));
        System.out.println("  → Conversations sorted correctly (most recent first)");

        System.out.println("✅ FLOW-005 COMPLETE: Multi-user conversations flow successful\n");
    }

    // ==================== Flow 6: Media Message Complete Flow ====================

    @Test
    @DisplayName("FLOW-006: Complete media message flow (upload + send)")
    public void testMediaMessageFlow() throws Exception {
        System.out.println("\n=== FLOW-006: Media Message Flow ===");

        // Step 1: User 1 wants to send an image
        System.out.println("Step 1: User 1 prepares to send image message");

        // Step 2: Upload image first (simulated - in real scenario, frontend uploads before sending)
        System.out.println("Step 2: Image uploaded to OSS (simulated)");
        String mockImageUrl = "https://oss.example.com/images/test_" + System.currentTimeMillis() + ".jpg";
        System.out.println("  → Image URL: " + mockImageUrl);

        // Step 3: Send image message with the uploaded URL
        System.out.println("Step 3: Send image message");
        Map<String, Object> imageMsg = new HashMap<>();
        imageMsg.put("receiverId", TEST_USER_2);
        imageMsg.put("messageType", "image");
        imageMsg.put("mediaUrl", mockImageUrl);

        MvcResult result = mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(imageMsg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("image"))
                .andExpect(jsonPath("$.data.mediaUrl").value(mockImageUrl))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> data = objectMapper.readValue(response, Map.class);
        Map<String, Object> messageData = (Map<String, Object>) data.get("data");
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        System.out.println("  → Image message sent successfully");

        // Step 4: User 2 views the image message
        System.out.println("Step 4: User 2 views the image message");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_2)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].messageType").value("image"))
                .andExpect(jsonPath("$.data.messages[0].mediaUrl").value(mockImageUrl));
        System.out.println("  → User 2 can view the image message");

        // Step 5: Test voice message flow
        System.out.println("\nStep 5: Send voice message");
        String mockVoiceUrl = "https://oss.example.com/voice/test_" + System.currentTimeMillis() + ".mp3";
        Map<String, Object> voiceMsg = new HashMap<>();
        voiceMsg.put("conversationId", conversationId);
        voiceMsg.put("receiverId", TEST_USER_2);
        voiceMsg.put("messageType", "voice");
        voiceMsg.put("mediaUrl", mockVoiceUrl);
        voiceMsg.put("duration", 15);

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voiceMsg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("voice"))
                .andExpect(jsonPath("$.data.mediaUrl").value(mockVoiceUrl))
                .andExpect(jsonPath("$.data.duration").value(15));
        System.out.println("  → Voice message sent successfully");

        // Step 6: Test video message flow
        System.out.println("\nStep 6: Send video message");
        String mockVideoUrl = "https://oss.example.com/video/test_" + System.currentTimeMillis() + ".mp4";
        String mockThumbUrl = "https://oss.example.com/video/test_thumb.jpg";
        Map<String, Object> videoMsg = new HashMap<>();
        videoMsg.put("conversationId", conversationId);
        videoMsg.put("receiverId", TEST_USER_2);
        videoMsg.put("messageType", "video");
        videoMsg.put("mediaUrl", mockVideoUrl);
        videoMsg.put("thumbnailUrl", mockThumbUrl);
        videoMsg.put("duration", 30);

        mockMvc.perform(post("/api/message/send")
                .header("Authorization", TEST_TOKEN_USER_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(videoMsg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("video"))
                .andExpect(jsonPath("$.data.mediaUrl").value(mockVideoUrl))
                .andExpect(jsonPath("$.data.thumbnailUrl").value(mockThumbUrl))
                .andExpect(jsonPath("$.data.duration").value(30));
        System.out.println("  → Video message sent successfully");

        // Step 7: Verify conversation now has 3 media messages
        System.out.println("\nStep 7: Verify all 3 media messages in conversation");
        mockMvc.perform(get("/api/message/chat/" + conversationId)
                .header("Authorization", TEST_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages", hasSize(3)))
                .andExpect(jsonPath("$.data.total").value(3));
        System.out.println("  → All 3 media messages confirmed in chat history");

        System.out.println("\n✅ FLOW-006 COMPLETE: Media message flow successful\n");
    }

    // ==================== Flow 7: Conversation Deletion ====================

    @Test
    @DisplayName("FLOW-007: Complete conversation deletion flow")
    public void testConversationDeletionFlow() throws Exception {
        System.out.println("\n=== FLOW-007: Conversation Deletion Flow ===");

        // Setup: Create conversation with messages
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 5);
        Long conv1Id = testData.userConversation.getId();
        Long conv2Id = testData.otherUserConversation.getId();
        System.out.println("Setup: Created conversation with 5 messages");

        // Step 1: User 1 checks conversation list
        System.out.println("Step 1: User 1 has 1 conversation");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        // Step 2: User 1 deletes the conversation
        System.out.println("Step 2: User 1 deletes the conversation");
        mockMvc.perform(delete("/api/message/conversation/" + conv1Id)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk());
        System.out.println("  → Conversation deleted for User 1");

        // Step 3: User 1's conversation list should be empty now
        System.out.println("Step 3: User 1's conversation list now empty");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        System.out.println("  → Confirmed: User 1's list is empty");

        // Step 4: User 2's conversation should still exist (bidirectional independence)
        System.out.println("Step 4: User 2's conversation still exists");
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_2)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].conversationId").value(conv2Id));
        System.out.println("  → Confirmed: User 2's conversation unaffected");

        System.out.println("✅ FLOW-007 COMPLETE: Conversation deletion flow successful\n");
    }
}
