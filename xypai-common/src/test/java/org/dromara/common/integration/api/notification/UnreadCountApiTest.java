package org.dromara.common.integration.api.notification;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 未读数API测试
 * <p>
 * 测试API: GET /api/notification/unread-count
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("未读数API测试")
class UnreadCountApiTest extends ApiTestBase {

    /**
     * 测试用例1: 获取未读数成功
     */
    @Test
    @DisplayName("获取未读数成功")
    void testGetUnreadCount_Success() {
        // When: 获取未读数
        Response response = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证数据结构
        Map<String, Object> data = response.jsonPath().getMap("data");
        assertThat(data).containsKeys(
            "likeCount", "commentCount", "followCount",
            "systemCount", "activityCount", "totalCount"
        );

        // 验证所有计数都是非负数
        assertThat((Integer) data.get("likeCount")).isGreaterThanOrEqualTo(0);
        assertThat((Integer) data.get("commentCount")).isGreaterThanOrEqualTo(0);
        assertThat((Integer) data.get("followCount")).isGreaterThanOrEqualTo(0);
        assertThat((Integer) data.get("systemCount")).isGreaterThanOrEqualTo(0);
        assertThat((Integer) data.get("activityCount")).isGreaterThanOrEqualTo(0);
        assertThat((Integer) data.get("totalCount")).isGreaterThanOrEqualTo(0);
    }

    /**
     * 测试用例2: totalCount等于各类型之和
     */
    @Test
    @DisplayName("totalCount等于各类型之和")
    void testGetUnreadCount_TotalEqualsSum() {
        // When: 获取未读数
        Response response = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");

        assertThat(response.statusCode()).isEqualTo(200);

        Map<String, Object> data = response.jsonPath().getMap("data");

        int likeCount = (Integer) data.get("likeCount");
        int commentCount = (Integer) data.get("commentCount");
        int followCount = (Integer) data.get("followCount");
        int systemCount = (Integer) data.get("systemCount");
        int activityCount = (Integer) data.get("activityCount");
        int totalCount = (Integer) data.get("totalCount");

        // Then: 验证总数等于各类型之和
        int sum = likeCount + commentCount + followCount + systemCount + activityCount;
        assertThat(totalCount).isEqualTo(sum);
    }

    /**
     * 测试用例3: 标记已读后未读数减少
     */
    @Test
    @DisplayName("标记已读后未读数减少")
    void testGetUnreadCount_DecreasesAfterMarkRead() {
        // Given: 获取初始未读数
        Response initialResponse = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");

        assertThat(initialResponse.statusCode()).isEqualTo(200);
        int initialTotal = initialResponse.jsonPath().getInt("data.totalCount");

        if (initialTotal == 0) {
            // 如果没有未读通知，跳过测试
            return;
        }

        // When: 获取一条通知并标记已读
        Response listResponse = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        if (listResponse.jsonPath().getList("data.list") != null &&
            !listResponse.jsonPath().getList("data.list").isEmpty()) {

            Long notificationId = listResponse.jsonPath().getLong("data.list[0].notificationId");

            // 标记已读
            authenticatedRequest()
                .when()
                .put("/api/notification/read/" + notificationId);

            // Then: 再次获取未读数
            Response newResponse = authenticatedRequest()
                .when()
                .get("/api/notification/unread-count");

            int newTotal = newResponse.jsonPath().getInt("data.totalCount");

            // 未读数应该减少
            assertThat(newTotal).isLessThan(initialTotal);
        }
    }

    /**
     * 测试用例4: 未登录访问
     */
    @Test
    @DisplayName("未登录访问")
    void testGetUnreadCount_Unauthorized() {
        // When: 不带Token访问
        Response response = unauthenticatedRequest()
            .when()
            .get("/api/notification/unread-count");

        // Then: 应该返回401错误
        assertThat(response.statusCode()).isEqualTo(401);
    }

    /**
     * 测试用例5: 缓存测试
     */
    @Test
    @DisplayName("缓存测试")
    void testGetUnreadCount_Cache() {
        // When: 第一次请求
        long start1 = System.currentTimeMillis();
        Response response1 = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");
        long time1 = System.currentTimeMillis() - start1;

        assertThat(response1.statusCode()).isEqualTo(200);
        int total1 = response1.jsonPath().getInt("data.totalCount");

        // When: 第二次请求 (可能走缓存)
        long start2 = System.currentTimeMillis();
        Response response2 = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");
        long time2 = System.currentTimeMillis() - start2;

        assertThat(response2.statusCode()).isEqualTo(200);
        int total2 = response2.jsonPath().getInt("data.totalCount");

        // Then: 验证数据一致
        assertThat(total1).isEqualTo(total2);

        // 第二次请求可能更快 (走缓存)
        // 注意: 这个断言可能不稳定
        if (time1 > 50) {
            assertThat(time2).isLessThanOrEqualTo(time1);
        }
    }

    /**
     * 测试用例6: 响应时间验证
     */
    @Test
    @DisplayName("响应时间验证")
    void testGetUnreadCount_ResponseTime() {
        // When: 获取未读数
        long start = System.currentTimeMillis();
        Response response = authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");
        long duration = System.currentTimeMillis() - start;

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);

        // 响应时间应该小于500ms
        assertThat(duration).isLessThan(500);
    }
}
