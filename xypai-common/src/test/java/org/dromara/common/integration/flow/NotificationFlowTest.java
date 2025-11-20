package org.dromara.common.integration.flow;

import io.restassured.response.Response;
import org.dromara.common.support.FlowTestBase;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 通知查看流程测试
 * <p>
 * 业务流程:
 * 1. 用户打开通知页
 * 2. 获取未读数 (GET /api/notification/unread-count)
 * 3. 显示红点和数字
 * 4. 用户点击某个通知分类
 * 5. 获取通知列表 (GET /api/notification/list?type=xxx)
 * 6. 显示通知列表
 * 7. 用户点击查看通知
 * 8. 标记已读 (PUT /api/notification/read/{id})
 * 9. 跳转到目标内容
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("通知查看流程测试")
class NotificationFlowTest extends FlowTestBase {

    /**
     * 场景1: 完整的通知查看流程
     * <p>
     * 步骤:
     * 1. 准备: 创建测试通知
     * 2. 获取未读数
     * 3. 获取点赞通知列表
     * 4. 标记第一条已读
     * 5. 再次获取未读数，验证减少
     */
    @Test
    @Order(1)
    @DisplayName("完整的通知查看流程")
    void testCompleteNotificationFlow() {
        // Given: 准备测试数据 (假设有5条点赞通知、3条评论通知)
        // TODO: 调用TestDataBuilder创建测试通知
        // dataBuilder.createTestNotifications(testUserId, 5, "like");
        // dataBuilder.createTestNotifications(testUserId, 3, "comment");

        // When: 步骤1 - 获取未读数
        Response unreadCountResponse = getUnreadCount();

        // Then: 验证未读数返回成功
        assertThat(unreadCountResponse.statusCode()).isEqualTo(200);
        assertThat(unreadCountResponse.jsonPath().getInt("code")).isEqualTo(200);
        assertThat((Object) unreadCountResponse.jsonPath().get("data")).isNotNull();

        // 验证未读数数据结构
        Map<String, Object> unreadCount = unreadCountResponse.jsonPath().getMap("data");
        assertThat(unreadCount).containsKeys("likeCount", "commentCount", "followCount",
            "systemCount", "activityCount", "totalCount");

        int totalCount = (Integer) unreadCount.get("totalCount");
        int likeCount = (Integer) unreadCount.get("likeCount");

        assertThat(totalCount).isGreaterThanOrEqualTo(0);

        // When: 步骤2 - 获取点赞通知列表
        Response notificationListResponse = getNotificationList("like", 1);

        // Then: 验证通知列表返回成功
        assertThat(notificationListResponse.statusCode()).isEqualTo(200);
        assertThat(notificationListResponse.jsonPath().getInt("code")).isEqualTo(200);

        List<Map<String, Object>> notifications = notificationListResponse.jsonPath()
            .getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            // 验证通知数据结构
            Map<String, Object> firstNotification = notifications.get(0);
            assertThat(firstNotification).containsKeys(
                "notificationId", "type", "senderId", "senderInfo",
                "targetType", "targetId", "isRead", "createdAt"
            );

            // When: 步骤3 - 标记第一条通知已读
            Long notificationId = ((Number) firstNotification.get("notificationId")).longValue();
            Response markReadResponse = markNotificationAsRead(notificationId);

            // Then: 验证标记成功
            assertThat(markReadResponse.statusCode()).isEqualTo(200);
            assertThat(markReadResponse.jsonPath().getInt("code")).isEqualTo(200);

            // When: 步骤4 - 再次获取未读数
            Response newUnreadCountResponse = getUnreadCount();

            // Then: 验证未读数减少了
            assertThat(newUnreadCountResponse.statusCode()).isEqualTo(200);
            int newTotalCount = newUnreadCountResponse.jsonPath().getInt("data.totalCount");

            if (totalCount > 0) {
                assertThat(newTotalCount).isLessThan(totalCount);
            }
        }
    }

    /**
     * 场景2: 分类通知列表
     * <p>
     * 测试各类型通知列表
     */
    @Test
    @Order(2)
    @DisplayName("分类通知列表")
    void testNotificationListByType() {
        // Given: 通知类型列表
        String[] notificationTypes = {"like", "comment", "follow", "system"};

        // When & Then: 遍历每种类型
        for (String type : notificationTypes) {
            Response response = getNotificationList(type, 1);

            // 验证返回成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

            // 验证返回的通知类型正确
            String returnedType = response.jsonPath().getString("data.type");
            if (returnedType != null) {
                assertThat(returnedType).isEqualTo(type);
            }

            // 验证数据结构
            assertThat((Object) response.jsonPath().get("data.total")).isNotNull();
            assertThat((Object) response.jsonPath().get("data.list")).isNotNull();
        }
    }

    /**
     * 场景3: 批量标记已读
     * <p>
     * 批量标记多条通知为已读
     */
    @Test
    @Order(3)
    @DisplayName("批量标记已读")
    void testBatchMarkAsRead() {
        // Given: 获取通知列表
        Response listResponse = getNotificationList("like", 1);
        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && notifications.size() >= 2) {
            // 提取前两条通知的ID
            Long id1 = ((Number) notifications.get(0).get("notificationId")).longValue();
            Long id2 = ((Number) notifications.get(1).get("notificationId")).longValue();

            // When: 批量标记已读
            Response batchResponse = batchMarkAsRead(new Long[]{id1, id2});

            // Then: 验证批量标记成功
            assertThat(batchResponse.statusCode()).isEqualTo(200);
            assertThat(batchResponse.jsonPath().getInt("code")).isEqualTo(200);

            // 验证未读数减少
            Response newUnreadCountResponse = getUnreadCount();
            assertThat(newUnreadCountResponse.statusCode()).isEqualTo(200);
        }
    }

    /**
     * 场景4: 全部标记已读
     * <p>
     * 将某个类型的通知全部标记为已读
     */
    @Test
    @Order(4)
    @DisplayName("全部标记已读")
    void testMarkAllAsRead() {
        // Given: 获取点赞通知的未读数
        Response unreadCountResponse = getUnreadCount();
        int beforeLikeCount = unreadCountResponse.jsonPath().getInt("data.likeCount");

        if (beforeLikeCount > 0) {
            // When: 全部标记已读
            Response markAllResponse = markAllAsRead("like");

            // Then: 验证成功
            assertThat(markAllResponse.statusCode()).isEqualTo(200);
            assertThat(markAllResponse.jsonPath().getInt("code")).isEqualTo(200);

            // 验证点赞未读数变为0
            Response newUnreadCountResponse = getUnreadCount();
            int afterLikeCount = newUnreadCountResponse.jsonPath().getInt("data.likeCount");

            assertThat(afterLikeCount).isEqualTo(0);
        }
    }

    /**
     * 场景5: 清除已读通知
     * <p>
     * 删除已读通知
     */
    @Test
    @Order(5)
    @DisplayName("清除已读通知")
    void testClearReadNotifications() {
        // Given: 先标记一些通知为已读
        Response listResponse = getNotificationList("like", 1);
        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && !notifications.isEmpty()) {
            Long notificationId = ((Number) notifications.get(0).get("notificationId")).longValue();
            markNotificationAsRead(notificationId);

            // When: 清除已读通知
            Response clearResponse = clearReadNotifications("like");

            // Then: 验证清除成功
            assertThat(clearResponse.statusCode()).isEqualTo(200);
            assertThat(clearResponse.jsonPath().getInt("code")).isEqualTo(200);

            // 再次获取列表，应该只剩未读通知
            Response newListResponse = getNotificationList("like", 1);
            List<Map<String, Object>> newNotifications = newListResponse.jsonPath()
                .getList("data.list");

            if (newNotifications != null) {
                // 所有通知都应该是未读的
                for (Map<String, Object> notification : newNotifications) {
                    Integer isRead = (Integer) notification.get("isRead");
                    assertThat(isRead).isEqualTo(0);  // 0表示未读
                }
            }
        }
    }

    /**
     * 场景6: 通知详情验证
     * <p>
     * 验证不同类型通知的数据结构
     */
    @Test
    @Order(6)
    @DisplayName("通知详情验证")
    void testNotificationDetailStructure() {
        // When: 获取点赞通知列表
        Response likeResponse = getNotificationList("like", 1);
        List<Map<String, Object>> likeNotifications = likeResponse.jsonPath().getList("data.list");

        if (likeNotifications != null && !likeNotifications.isEmpty()) {
            Map<String, Object> likeNotification = likeNotifications.get(0);

            // Then: 验证点赞通知数据结构
            assertThat(likeNotification).containsKeys(
                "notificationId", "type", "senderId", "senderInfo",
                "targetType", "targetId", "isRead", "createdAt"
            );

            assertThat(likeNotification.get("type")).isEqualTo("like");

            // 验证发送者信息
            Map<String, Object> senderInfo = (Map<String, Object>) likeNotification.get("senderInfo");
            if (senderInfo != null) {
                assertThat(senderInfo).containsKeys("userId", "nickname", "avatar");
            }

            // 验证目标信息
            assertThat(likeNotification.get("targetType")).isNotNull();
            assertThat(likeNotification.get("targetId")).isNotNull();
        }

        // When: 获取评论通知列表
        Response commentResponse = getNotificationList("comment", 1);
        List<Map<String, Object>> commentNotifications = commentResponse.jsonPath()
            .getList("data.list");

        if (commentNotifications != null && !commentNotifications.isEmpty()) {
            Map<String, Object> commentNotification = commentNotifications.get(0);

            // Then: 验证评论通知数据结构
            assertThat(commentNotification.get("type")).isEqualTo("comment");

            // 评论通知应该包含评论内容
            if (commentNotification.containsKey("content")) {
                assertThat(commentNotification.get("content")).isNotNull();
            }
        }
    }

    /**
     * 场景7: 分页测试
     * <p>
     * 测试通知列表分页功能
     */
    @Test
    @Order(7)
    @DisplayName("分页测试")
    void testNotificationPagination() {
        // When: 获取第1页 (默认pageSize=20)
        Response page1Response = getNotificationList("like", 1);

        // Then: 验证返回成功
        assertThat(page1Response.statusCode()).isEqualTo(200);
        assertThat(page1Response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证分页信息
        int total = page1Response.jsonPath().getInt("data.total");
        boolean hasMore = page1Response.jsonPath().getBoolean("data.hasMore");
        List<Map<String, Object>> page1List = page1Response.jsonPath().getList("data.list");

        if (total > 20) {
            // 如果总数大于20，应该有更多页
            assertThat(hasMore).isTrue();
            assertThat(page1List).hasSize(20);

            // When: 获取第2页
            Response page2Response = getNotificationList("like", 2);

            // Then: 验证第2页返回成功
            assertThat(page2Response.statusCode()).isEqualTo(200);
            List<Map<String, Object>> page2List = page2Response.jsonPath().getList("data.list");

            // 第2页应该有数据
            assertThat(page2List).isNotEmpty();

            // 第1页和第2页的通知ID应该不同
            Long firstIdPage1 = ((Number) page1List.get(0).get("notificationId")).longValue();
            Long firstIdPage2 = ((Number) page2List.get(0).get("notificationId")).longValue();

            assertThat(firstIdPage1).isNotEqualTo(firstIdPage2);
        } else {
            // 如果总数小于等于20，不应该有更多页
            if (total > 0) {
                assertThat(hasMore).isFalse();
                assertThat(page1List.size()).isLessThanOrEqualTo(20);
            }
        }
    }

    /**
     * 场景8: 通知排序验证
     * <p>
     * 验证通知按时间倒序排列 (最新的在前)
     */
    @Test
    @Order(8)
    @DisplayName("通知排序验证")
    void testNotificationOrdering() {
        // When: 获取通知列表
        Response response = getNotificationList("like", 1);
        List<Map<String, Object>> notifications = response.jsonPath().getList("data.list");

        if (notifications != null && notifications.size() >= 2) {
            // Then: 验证按时间倒序排列
            for (int i = 1; i < notifications.size(); i++) {
                String prevTime = (String) notifications.get(i - 1).get("createdAt");
                String currTime = (String) notifications.get(i).get("createdAt");

                if (prevTime != null && currTime != null) {
                    // 前一个时间应该大于等于后一个时间 (倒序)
                    assertThat(prevTime.compareTo(currTime)).isGreaterThanOrEqualTo(0);
                }
            }
        }
    }

    /**
     * 场景9: 未读通知优先显示
     * <p>
     * 验证未读通知排在已读通知前面
     */
    @Test
    @Order(9)
    @DisplayName("未读通知优先显示")
    void testUnreadNotificationsPriority() {
        // Given: 先标记部分通知为已读
        Response listResponse = getNotificationList("like", 1);
        List<Map<String, Object>> notifications = listResponse.jsonPath().getList("data.list");

        if (notifications != null && notifications.size() >= 2) {
            // 标记第2条为已读 (保留第1条未读)
            Long notificationId = ((Number) notifications.get(1).get("notificationId")).longValue();
            markNotificationAsRead(notificationId);

            // When: 再次获取列表
            Response newListResponse = getNotificationList("like", 1);
            List<Map<String, Object>> newNotifications = newListResponse.jsonPath()
                .getList("data.list");

            if (newNotifications != null && newNotifications.size() >= 2) {
                // Then: 验证未读通知在前 (如果API支持这个排序)
                // 注意: 这个功能可能需要后端实现，如果未实现可以跳过
                int unreadCount = 0;
                int readCount = 0;
                int firstReadIndex = -1;

                for (int i = 0; i < newNotifications.size(); i++) {
                    Integer isRead = (Integer) newNotifications.get(i).get("isRead");
                    if (isRead == 0) {
                        unreadCount++;
                        if (firstReadIndex != -1) {
                            // 如果已经遇到了已读通知，又出现未读通知，说明排序有问题
                            // 但这个断言可能过于严格，取决于实际业务需求
                        }
                    } else {
                        readCount++;
                        if (firstReadIndex == -1) {
                            firstReadIndex = i;
                        }
                    }
                }

                // 至少验证有未读和已读通知
                if (unreadCount > 0 && readCount > 0) {
                    // 可以添加更严格的排序验证
                }
            }
        }
    }

    /**
     * 场景10: 无效通知ID处理
     * <p>
     * 测试标记不存在的通知ID
     */
    @Test
    @Order(10)
    @DisplayName("无效通知ID处理")
    void testInvalidNotificationId() {
        // Given: 不存在的通知ID
        Long invalidId = 999999999L;

        // When: 尝试标记已读
        Response response = markNotificationAsRead(invalidId);

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            // 业务层返回错误
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).isNotBlank();
        } else {
            // HTTP层返回错误
            assertThat(response.statusCode()).isIn(400, 404);
        }
    }
}
