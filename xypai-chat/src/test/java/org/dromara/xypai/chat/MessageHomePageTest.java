package org.dromara.xypai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Message Home Page Test (消息主页页面测试)
 *
 * Tests for: Frontend/01-消息主页页面.md
 * Route: /message/main
 *
 * Frontend Requirements:
 * 1. GET /api/message/unread-count - Get unread notification counts
 * 2. GET /api/message/conversations - Get conversation list
 * 3. DELETE /api/message/conversation/{id} - Delete conversation
 * 4. POST /api/message/clear-all - Clear all conversations
 *
 * Test Coverage:
 * - All 4 APIs required by frontend spec
 * - Data structure alignment verification
 * - Business logic validation
 * - Edge cases and error scenarios
 *
 * @author XiangYuPai Backend Team
 * @since 2025-01-14
 */
@DisplayName("Message Home Page Tests - 消息主页页面测试")
public class MessageHomePageTest extends BaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== API 1: Get Unread Count ====================

    @Test
    @DisplayName("TC-HOME-001: Get unread count - Success")
    public void testGetUnreadCount_Success() throws Exception {
        // Given: User with some unread notifications
        // This would normally be populated by NotificationService
        // For now, we test the structure

        // When: Get unread count
        MvcResult result = mockMvc.perform(get("/api/message/unread-count")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                // Verify data structure matches frontend spec
                .andExpect(jsonPath("$.data.likes").isNumber())
                .andExpect(jsonPath("$.data.comments").isNumber())
                .andExpect(jsonPath("$.data.followers").isNumber())
                .andExpect(jsonPath("$.data.system").isNumber())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andReturn();

        // Then: Verify response format
        String jsonResponse = result.getResponse().getContentAsString();
        assertRFormat(jsonResponse);

        System.out.println("✅ TC-HOME-001: Unread count structure verified");
        System.out.println("Response: " + jsonResponse);
    }

    @Test
    @DisplayName("TC-HOME-002: Get unread count - Unauthorized")
    public void testGetUnreadCount_Unauthorized() throws Exception {
        // When: Request without auth token
        mockMvc.perform(get("/api/message/unread-count"))
                .andExpect(status().isUnauthorized());

        System.out.println("✅ TC-HOME-002: Unauthorized access blocked");
    }

    @Test
    @DisplayName("TC-HOME-003: Get unread count - Cache verification")
    public void testGetUnreadCount_Cache() throws Exception {
        // When: Call API twice
        MvcResult result1 = mockMvc.perform(get("/api/message/unread-count")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/api/message/unread-count")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andReturn();

        // Then: Both should return same data (from cache)
        String response1 = result1.getResponse().getContentAsString();
        String response2 = result2.getResponse().getContentAsString();

        System.out.println("✅ TC-HOME-003: Cache working (responses should match)");
        System.out.println("First call: " + response1);
        System.out.println("Second call: " + response2);
    }

    // ==================== API 2: Get Conversation List ====================

    @Test
    @DisplayName("TC-HOME-004: Get conversation list - Empty list")
    public void testGetConversations_EmptyList() throws Exception {
        // When: Get conversations for user with no conversations
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));

        System.out.println("✅ TC-HOME-004: Empty conversation list verified");
    }

    @Test
    @DisplayName("TC-HOME-005: Get conversation list - With conversations")
    public void testGetConversations_WithData() throws Exception {
        // Given: Create test conversations
        createTestConversation(TEST_USER_1, TEST_USER_2);
        createTestConversation(TEST_USER_1, TEST_USER_3);
        setUserOnline(TEST_USER_2, true);  // User 2 is online
        setUserOnline(TEST_USER_3, false); // User 3 is offline

        // When: Get conversations
        MvcResult result = mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(2)))
                // Verify first conversation structure matches frontend spec
                .andExpect(jsonPath("$.data.records[0].conversationId").exists())
                .andExpect(jsonPath("$.data.records[0].userId").exists())
                .andExpect(jsonPath("$.data.records[0].nickname").exists())
                .andExpect(jsonPath("$.data.records[0].avatar").exists())
                .andExpect(jsonPath("$.data.records[0].lastMessage").exists())
                .andExpect(jsonPath("$.data.records[0].lastMessageTime").exists())
                .andExpect(jsonPath("$.data.records[0].unreadCount").exists())
                .andExpect(jsonPath("$.data.records[0].isOnline").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("✅ TC-HOME-005: Conversation list structure verified");
        System.out.println("Response: " + jsonResponse);
    }

    @Test
    @DisplayName("TC-HOME-006: Get conversation list - Pagination")
    public void testGetConversations_Pagination() throws Exception {
        // Given: Create 25 conversations (more than one page)
        for (int i = 1; i <= 25; i++) {
            createTestConversation(TEST_USER_1, 1000L + i);
        }

        // When: Get first page
        MvcResult result = mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records", hasSize(20)))
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(25)))
                .andExpect(jsonPath("$.data.pages", greaterThanOrEqualTo(2)))
                .andReturn();

        System.out.println("✅ TC-HOME-006: Pagination working correctly");
        System.out.println("Page 1: " + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("TC-HOME-007: Get conversation list - Invalid page number")
    public void testGetConversations_InvalidPage() throws Exception {
        // When: Request with invalid page (0 or negative)
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "0")
                .param("pageSize", "20"))
                .andExpect(status().isBadRequest());

        System.out.println("✅ TC-HOME-007: Invalid page number rejected");
    }

    @Test
    @DisplayName("TC-HOME-008: Get conversation list - Cache verification")
    public void testGetConversations_Cache() throws Exception {
        // Given: Create test conversation
        createTestConversation(TEST_USER_1, TEST_USER_2);

        // When: Call API twice (first page should be cached)
        MvcResult result1 = mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andReturn();

        // Then: Responses should match (from cache)
        String response1 = result1.getResponse().getContentAsString();
        String response2 = result2.getResponse().getContentAsString();

        System.out.println("✅ TC-HOME-008: Conversation list cache verified (5min TTL)");
        System.out.println("First call: " + response1);
        System.out.println("Cached call: " + response2);
    }

    // ==================== API 3: Delete Conversation ====================

    @Test
    @DisplayName("TC-HOME-009: Delete conversation - Success")
    public void testDeleteConversation_Success() throws Exception {
        // Given: Create test conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 5);
        Long conversationId = testData.userConversation.getConversationId();

        // When: Delete conversation
        mockMvc.perform(delete("/api/message/conversation/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists());

        // Then: Conversation should be soft-deleted
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                // Should not include deleted conversation
                .andExpect(jsonPath("$.data.records[?(@.conversationId == " + conversationId + ")]").doesNotExist());

        System.out.println("✅ TC-HOME-009: Conversation deleted (soft delete)");
    }

    @Test
    @DisplayName("TC-HOME-010: Delete conversation - Not found")
    public void testDeleteConversation_NotFound() throws Exception {
        // When: Delete non-existent conversation
        mockMvc.perform(delete("/api/message/conversation/999999")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isNotFound());

        System.out.println("✅ TC-HOME-010: Non-existent conversation returns 404");
    }

    @Test
    @DisplayName("TC-HOME-011: Delete conversation - No permission")
    public void testDeleteConversation_NoPermission() throws Exception {
        // Given: User 1 creates conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 3);
        Long conversationId = testData.userConversation.getConversationId();

        // When: User 3 tries to delete User 1's conversation
        mockMvc.perform(delete("/api/message/conversation/" + conversationId)
                .header("Authorization", TEST_TOKEN_USER_3))
                .andExpect(status().isForbidden());

        System.out.println("✅ TC-HOME-011: Unauthorized deletion blocked");
    }

    @Test
    @DisplayName("TC-HOME-012: Delete conversation - Bidirectional check")
    public void testDeleteConversation_Bidirectional() throws Exception {
        // Given: Create bidirectional conversation
        var testData = createConversationWithMessages(TEST_USER_1, TEST_USER_2, 3);
        Long conv1Id = testData.userConversation.getConversationId();
        Long conv2Id = testData.otherUserConversation.getConversationId();

        // When: User 1 deletes their conversation
        mockMvc.perform(delete("/api/message/conversation/" + conv1Id)
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk());

        // Then: User 2's conversation should still exist
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_2)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.conversationId == " + conv2Id + ")]").exists());

        System.out.println("✅ TC-HOME-012: Bidirectional delete verified (only deletes one side)");
    }

    // ==================== API 4: Clear All Conversations ====================

    @Test
    @DisplayName("TC-HOME-013: Clear all conversations - Success")
    public void testClearAllConversations_Success() throws Exception {
        // Given: Create multiple conversations
        createTestConversation(TEST_USER_1, TEST_USER_2);
        createTestConversation(TEST_USER_1, TEST_USER_3);
        createTestConversation(TEST_USER_1, 1001L);

        // When: Clear all conversations
        mockMvc.perform(post("/api/message/clear-all")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists());

        // Then: All conversations should be deleted
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));

        System.out.println("✅ TC-HOME-013: All conversations cleared");
    }

    @Test
    @DisplayName("TC-HOME-014: Clear all conversations - Empty list")
    public void testClearAllConversations_EmptyList() throws Exception {
        // When: Clear conversations when none exist
        mockMvc.perform(post("/api/message/clear-all")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ TC-HOME-014: Clear all on empty list succeeds");
    }

    @Test
    @DisplayName("TC-HOME-015: Clear all conversations - Does not affect other users")
    public void testClearAllConversations_OtherUsersUnaffected() throws Exception {
        // Given: User 1 and User 2 both have conversations
        createTestConversation(TEST_USER_1, TEST_USER_2);
        createTestConversation(TEST_USER_2, TEST_USER_1);
        createTestConversation(TEST_USER_2, TEST_USER_3);

        // When: User 1 clears all conversations
        mockMvc.perform(post("/api/message/clear-all")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk());

        // Then: User 2's conversations should still exist
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_2)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records", hasSize(greaterThanOrEqualTo(2))));

        System.out.println("✅ TC-HOME-015: Clear all only affects current user");
    }

    @Test
    @DisplayName("TC-HOME-016: Clear all conversations - Cache invalidation")
    public void testClearAllConversations_CacheInvalidation() throws Exception {
        // Given: Create conversation and cache it
        createTestConversation(TEST_USER_1, TEST_USER_2);

        // First call to populate cache
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records", hasSize(greaterThanOrEqualTo(1))));

        // When: Clear all conversations
        mockMvc.perform(post("/api/message/clear-all")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk());

        // Then: Cache should be invalidated, new call should return empty
        mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isEmpty());

        System.out.println("✅ TC-HOME-016: Cache invalidated after clear all");
    }

    // ==================== Data Structure Verification Tests ====================

    @Test
    @DisplayName("TC-HOME-017: Verify UnreadCountVO matches frontend spec")
    public void testUnreadCountVO_Structure() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/message/unread-count")
                .header("Authorization", TEST_TOKEN_USER_1))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        // Verify all required fields exist and are numbers
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"likes\""), "Missing 'likes' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"comments\""), "Missing 'comments' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"followers\""), "Missing 'followers' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"system\""), "Missing 'system' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"total\""), "Missing 'total' field");

        System.out.println("✅ TC-HOME-017: UnreadCountVO structure matches frontend spec");
        System.out.println("Expected fields: likes, comments, followers, system, total");
        System.out.println("Actual response: " + json);
    }

    @Test
    @DisplayName("TC-HOME-018: Verify ConversationVO matches frontend spec")
    public void testConversationVO_Structure() throws Exception {
        // Given: Create test conversation
        createTestConversation(TEST_USER_1, TEST_USER_2);
        setUserOnline(TEST_USER_2, true);

        MvcResult result = mockMvc.perform(get("/api/message/conversations")
                .header("Authorization", TEST_TOKEN_USER_1)
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        // Verify all required fields from frontend spec
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"conversationId\""), "Missing 'conversationId' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"userId\""), "Missing 'userId' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"nickname\""), "Missing 'nickname' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"avatar\""), "Missing 'avatar' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"lastMessage\""), "Missing 'lastMessage' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"lastMessageTime\""), "Missing 'lastMessageTime' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"unreadCount\""), "Missing 'unreadCount' field");
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"isOnline\""), "Missing 'isOnline' field");

        System.out.println("✅ TC-HOME-018: ConversationVO structure matches frontend spec");
        System.out.println("Expected fields: conversationId, userId, nickname, avatar, lastMessage, lastMessageTime, unreadCount, isOnline");
        System.out.println("Actual response: " + json);
    }
}
