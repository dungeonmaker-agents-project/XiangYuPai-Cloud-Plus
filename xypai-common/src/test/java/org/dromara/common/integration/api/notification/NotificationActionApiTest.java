package org.dromara.common.integration.api.notification;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 通知操作API测试
 * <p>
 * 测试API:
 * - PUT /api/notification/read/{id} - 标记已读
 * - PUT /api/notification/batch-read - 批量标记已读
 * - PUT /api/notification/read-all - 全部标记已读
 * - DELETE /api/notification/clear - 清除已读通知
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("通知操作API测试")
class NotificationActionApiTest extends ApiTestBase {

    /**
     * 测试用例1: 标记单条通知已读
     */
    @Test
    @DisplayName("标记单条通知已读")
    void testMarkAsRead_Success() {
        // Given: 获取一条未读通知
        Response listResponse = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            Long notificationId = ((Number) notifications.get(0).get("notificationId")).longValue();

            // When: 标记已读
            Response response = authenticatedRequest()
                .when()
                .put("/api/notification/read/" + notificationId);

            // Then: 验证标记成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        }
    }

    /**
     * 测试用例2: 标记不存在的通知
     */
    @Test
    @DisplayName("标记不存在的通知")
    void testMarkAsRead_NotFound() {
        // Given: 不存在的通知ID
        Long nonExistentId = 999999999L;

        // When: 尝试标记已读
        Response response = authenticatedRequest()
            .when()
            .put("/api/notification/read/" + nonExistentId);

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(response.statusCode()).isIn(400, 404);
        }
    }

    /**
     * 测试用例3: 批量标记已读
     */
    @Test
    @DisplayName("批量标记已读")
    void testBatchMarkAsRead_Success() {
        // Given: 获取多条通知
        Response listResponse = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && notifications.size() >= 2) {
            Long id1 = ((Number) notifications.get(0).get("notificationId")).longValue();
            Long id2 = ((Number) notifications.get(1).get("notificationId")).longValue();

            // When: 批量标记已读
            String requestBody = String.format("{\"ids\":[%d,%d]}", id1, id2);

            Response response = authenticatedRequest()
                .body(requestBody)
                .when()
                .put("/api/notification/batch-read");

            // Then: 验证批量标记成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        }
    }

    /**
     * 测试用例4: 批量标记空列表
     */
    @Test
    @DisplayName("批量标记空列表")
    void testBatchMarkAsRead_EmptyList() {
        // Given: 空的ID列表
        String requestBody = "{\"ids\":[]}";

        // When: 尝试批量标记
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .put("/api/notification/batch-read");

        // Then: 可能返回成功(无操作)或错误
        if (response.statusCode() == 200) {
            // 允许返回成功
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例5: 全部标记已读
     */
    @Test
    @DisplayName("全部标记已读")
    void testMarkAllAsRead_Success() {
        // Given: 获取某类型的未读数
        Response unreadResponse = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");

        int beforeLikeCount = unreadResponse.jsonPath().getInt("data.likeCount");

        if (beforeLikeCount > 0) {
            // When: 全部标记已读
            Response response = authenticatedRequest()
                .param("type", "like")
                .when()
                .put("/api/notification/read-all");

            // Then: 验证标记成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

            // 验证未读数变为0
            Response newUnreadResponse = authenticatedRequest()
                .when()
                .get("/api/notification/unread-count");

            int afterLikeCount = newUnreadResponse.jsonPath().getInt("data.likeCount");
            assertThat(afterLikeCount).isEqualTo(0);
        }
    }

    /**
     * 测试用例6: 清除已读通知
     */
    @Test
    @DisplayName("清除已读通知")
    void testClearReadNotifications_Success() {
        // Given: 先标记一些通知为已读
        Response listResponse = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            Long notificationId = ((Number) notifications.get(0).get("notificationId")).longValue();

            // 标记已读
            authenticatedRequest()
                .when()
                .put("/api/notification/read/" + notificationId);

            // When: 清除已读通知
            Response response = authenticatedRequest()
                .param("type", "like")
                .when()
                .delete("/api/notification/clear");

            // Then: 验证清除成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

            // 再次获取列表，已读的应该被删除
            Response newListResponse = authenticatedRequest()
                .param("type", "like")
                .param("pageNum", 1)
                .when()
                .get("/api/notification/list");

            List<Map<String, Object>> newNotifications = newListResponse.jsonPath()
                .getList("data.list");

            if (newNotifications != null) {
                // 所有通知都应该是未读的
                for (Map<String, Object> notification : newNotifications) {
                    Integer isRead = (Integer) notification.get("isRead");
                    assertThat(isRead).isEqualTo(0);
                }
            }
        }
    }

    /**
     * 测试用例7: 重复标记已读
     */
    @Test
    @DisplayName("重复标记已读")
    void testMarkAsRead_Idempotent() {
        // Given: 获取一条通知
        Response listResponse = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            Long notificationId = ((Number) notifications.get(0).get("notificationId")).longValue();

            // When: 第一次标记已读
            Response firstResponse = authenticatedRequest()
                .when()
                .put("/api/notification/read/" + notificationId);

            assertThat(firstResponse.statusCode()).isEqualTo(200);

            // When: 第二次标记已读 (幂等操作)
            Response secondResponse = authenticatedRequest()
                .when()
                .put("/api/notification/read/" + notificationId);

            // Then: 第二次也应该成功
            assertThat(secondResponse.statusCode()).isEqualTo(200);
            assertThat(secondResponse.jsonPath().getInt("code")).isEqualTo(200);
        }
    }

    /**
     * 测试用例8: 批量标记包含无效ID
     */
    @Test
    @DisplayName("批量标记包含无效ID")
    void testBatchMarkAsRead_WithInvalidIds() {
        // Given: 混合有效和无效ID
        String requestBody = "{\"ids\":[1,999999999]}";

        // When: 尝试批量标记
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .put("/api/notification/batch-read");

        // Then: 可能部分成功或全部失败
        // 根据实际业务逻辑验证
        assertThat(response.statusCode()).isIn(200, 400);
    }
}
