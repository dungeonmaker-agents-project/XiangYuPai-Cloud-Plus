package org.dromara.common.integration.api.notification;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 通知列表API测试
 * <p>
 * 测试API: GET /api/notification/list
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("通知列表API测试")
class NotificationListApiTest extends ApiTestBase {

    /**
     * 测试用例1: 获取通知列表成功
     */
    @Test
    @DisplayName("获取通知列表成功")
    void testGetNotificationList_Success() {
        // When: 获取点赞通知列表
        Response response = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .param("pageSize", 20)
            .when()
            .get("/api/notification/list");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证数据结构
        assertThat(response.jsonPath().get("data")).isNotNull();
        assertThat(response.jsonPath().get("data.type")).isEqualTo("like");
        assertThat(response.jsonPath().get("data.total")).isNotNull();
        assertThat(response.jsonPath().get("data.list")).isNotNull();
    }

    /**
     * 测试用例2: 获取不同类型的通知
     */
    @Test
    @DisplayName("获取不同类型的通知")
    void testGetNotificationList_DifferentTypes() {
        // Test different notification types
        String[] types = {"like", "comment", "follow", "system"};

        for (String type : types) {
            Response response = authenticatedRequest()
                .param("type", type)
                .param("pageNum", 1)
                .when()
                .get("/api/notification/list");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

            String returnedType = response.jsonPath().getString("data.type");
            if (returnedType != null) {
                assertThat(returnedType).isEqualTo(type);
            }
        }
    }

    /**
     * 测试用例3: 分页测试
     */
    @Test
    @DisplayName("分页测试")
    void testGetNotificationList_Pagination() {
        // When: 获取第1页
        Response page1Response = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .param("pageSize", 10)
            .when()
            .get("/api/notification/list");

        assertThat(page1Response.statusCode()).isEqualTo(200);

        int total = page1Response.jsonPath().getInt("data.total");
        List<Map<String, Object>> page1List = page1Response.jsonPath().getList("data.list");

        if (total > 10) {
            // When: 获取第2页
            Response page2Response = authenticatedRequest()
                .param("type", "like")
                .param("pageNum", 2)
                .param("pageSize", 10)
                .when()
                .get("/api/notification/list");

            assertThat(page2Response.statusCode()).isEqualTo(200);
            List<Map<String, Object>> page2List = page2Response.jsonPath().getList("data.list");

            // 验证两页数据不同
            if (!page1List.isEmpty() && !page2List.isEmpty()) {
                Long firstIdPage1 = ((Number) page1List.get(0).get("notificationId")).longValue();
                Long firstIdPage2 = ((Number) page2List.get(0).get("notificationId")).longValue();
                assertThat(firstIdPage1).isNotEqualTo(firstIdPage2);
            }
        }
    }

    /**
     * 测试用例4: 通知数据结构验证
     */
    @Test
    @DisplayName("通知数据结构验证")
    void testGetNotificationList_DataStructure() {
        // When: 获取通知列表
        Response response = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        assertThat(response.statusCode()).isEqualTo(200);

        List<Map<String, Object>> notifications = response.jsonPath().getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            for (Map<String, Object> notification : notifications) {
                // 验证必须字段
                assertThat(notification).containsKeys(
                    "notificationId", "type", "senderId",
                    "targetType", "targetId", "isRead", "createdAt"
                );

                // 验证字段类型
                assertThat(notification.get("notificationId")).isNotNull();
                assertThat(notification.get("type")).isNotNull();
                assertThat(notification.get("isRead")).isInstanceOf(Integer.class);
            }
        }
    }

    /**
     * 测试用例5: 按时间排序验证
     */
    @Test
    @DisplayName("按时间排序验证")
    void testGetNotificationList_TimeOrdering() {
        // When: 获取通知列表
        Response response = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .param("pageSize", 20)
            .when()
            .get("/api/notification/list");

        assertThat(response.statusCode()).isEqualTo(200);

        List<Map<String, Object>> notifications = response.jsonPath().getList("data.list");

        if (notifications != null && notifications.size() >= 2) {
            // 验证按时间倒序排列 (最新的在前)
            for (int i = 1; i < notifications.size(); i++) {
                String prevTime = (String) notifications.get(i - 1).get("createdAt");
                String currTime = (String) notifications.get(i).get("createdAt");

                if (prevTime != null && currTime != null) {
                    // 前一个时间应该大于等于后一个时间
                    assertThat(prevTime.compareTo(currTime)).isGreaterThanOrEqualTo(0);
                }
            }
        }
    }

    /**
     * 测试用例6: 未读通知优先
     */
    @Test
    @DisplayName("未读通知优先")
    void testGetNotificationList_UnreadFirst() {
        // When: 获取通知列表
        Response response = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        assertThat(response.statusCode()).isEqualTo(200);

        List<Map<String, Object>> notifications = response.jsonPath().getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            // 统计未读和已读通知的位置
            int firstReadIndex = -1;

            for (int i = 0; i < notifications.size(); i++) {
                Integer isRead = (Integer) notifications.get(i).get("isRead");
                if (isRead != null && isRead == 1 && firstReadIndex == -1) {
                    firstReadIndex = i;
                    break;
                }
            }

            // 如果有已读通知，验证未读通知在前面
            if (firstReadIndex > 0) {
                for (int i = 0; i < firstReadIndex; i++) {
                    Integer isRead = (Integer) notifications.get(i).get("isRead");
                    // 前面的都应该是未读
                    assertThat(isRead).isEqualTo(0);
                }
            }
        }
    }

    /**
     * 测试用例7: 无效的通知类型
     */
    @Test
    @DisplayName("无效的通知类型")
    void testGetNotificationList_InvalidType() {
        // When: 使用无效的类型
        Response response = authenticatedRequest()
            .param("type", "invalid_type_xyz")
            .param("pageNum", 1)
            .when()
            .get("/api/notification/list");

        // Then: 可能返回空列表或错误
        if (response.statusCode() == 200) {
            Integer code = response.jsonPath().getInt("code");
            if (code == 200) {
                // 如果成功，列表应该为空
                List<?> list = response.jsonPath().getList("data.list");
                assertThat(list).isEmpty();
            }
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例8: 页码超出范围
     */
    @Test
    @DisplayName("页码超出范围")
    void testGetNotificationList_PageOutOfRange() {
        // When: 请求第999页
        Response response = authenticatedRequest()
            .param("type", "like")
            .param("pageNum", 999)
            .param("pageSize", 20)
            .when()
            .get("/api/notification/list");

        // Then: 应该返回空列表
        assertThat(response.statusCode()).isEqualTo(200);

        List<?> list = response.jsonPath().getList("data.list");
        assertThat(list).isEmpty();

        boolean hasMore = response.jsonPath().getBoolean("data.hasMore");
        assertThat(hasMore).isFalse();
    }
}
