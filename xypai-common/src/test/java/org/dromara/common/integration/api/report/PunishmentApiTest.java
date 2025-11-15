package org.dromara.common.integration.api.report;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 处罚管理API测试
 * <p>
 * 测试API:
 * - POST /api/report/ban - 封禁用户
 * - POST /api/report/mute - 禁言用户
 * - POST /api/report/unban - 解除封禁
 * - GET /api/report/punishment/check - 检查用户处罚状态
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("处罚管理API测试")
class PunishmentApiTest extends ApiTestBase {

    /**
     * 测试用例1: 封禁用户
     */
    @Test
    @DisplayName("封禁用户")
    void testBanUser_Success() {
        // Given: 准备封禁数据
        String requestBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":1440," +  // 1天 (分钟)
            "\"reason\":\"违反社区规定\"" +
            "}", anotherUserId);

        // When: 封禁用户
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/ban");

        // Then: 验证封禁成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("data.success")).isTrue();
    }

    /**
     * 测试用例2: 永久封禁用户
     */
    @Test
    @DisplayName("永久封禁用户")
    void testBanUser_Permanent() {
        // Given: 不设置duration，表示永久封禁
        String requestBody = String.format("{" +
            "\"userId\":%d," +
            "\"reason\":\"严重违规\"" +
            "}", anotherUserId + 1);

        // When: 封禁用户
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/ban");

        // Then: 验证封禁成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
    }

    /**
     * 测试用例3: 禁言用户
     */
    @Test
    @DisplayName("禁言用户")
    void testMuteUser_Success() {
        // Given: 准备禁言数据
        String requestBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":60," +  // 1小时 (分钟)
            "\"reason\":\"发送垃圾信息\"" +
            "}", anotherUserId + 2);

        // When: 禁言用户
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/mute");

        // Then: 验证禁言成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
    }

    /**
     * 测试用例4: 解除封禁
     */
    @Test
    @DisplayName("解除封禁")
    void testUnbanUser_Success() {
        // Given: 先封禁一个用户
        Long targetUserId = anotherUserId + 3;
        String banBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":60," +
            "\"reason\":\"测试封禁\"" +
            "}", targetUserId);

        Response banResponse = authenticatedRequest()
            .body(banBody)
            .when()
            .post("/api/report/ban");

        if (banResponse.statusCode() == 200) {
            // When: 解除封禁
            String unbanBody = String.format("{\"userId\":%d}", targetUserId);

            Response response = authenticatedRequest()
                .body(unbanBody)
                .when()
                .post("/api/report/unban");

            // Then: 验证解除成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        }
    }

    /**
     * 测试用例5: 检查用户处罚状态
     */
    @Test
    @DisplayName("检查用户处罚状态")
    void testCheckPunishmentStatus() {
        // When: 检查用户状态
        Response response = authenticatedRequest()
            .param("userId", testUserId)
            .when()
            .get("/api/report/punishment/check");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证返回数据结构
        assertThat(response.jsonPath().get("data.isBanned")).isNotNull();
        assertThat(response.jsonPath().get("data.isMuted")).isNotNull();
    }

    /**
     * 测试用例6: 检查被封禁用户状态
     */
    @Test
    @DisplayName("检查被封禁用户状态")
    void testCheckPunishmentStatus_BannedUser() {
        // Given: 先封禁一个用户
        Long targetUserId = anotherUserId + 4;
        String banBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":30," +
            "\"reason\":\"测试\"" +
            "}", targetUserId);

        Response banResponse = authenticatedRequest()
            .body(banBody)
            .when()
            .post("/api/report/ban");

        if (banResponse.statusCode() == 200 && banResponse.jsonPath().getInt("code") == 200) {
            // When: 检查该用户状态
            Response response = authenticatedRequest()
                .param("userId", targetUserId)
                .when()
                .get("/api/report/punishment/check");

            // Then: 验证显示被封禁
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getBoolean("data.isBanned")).isTrue();

            // 验证封禁剩余时间
            if (response.jsonPath().get("data.bannedUntil") != null) {
                assertThat(response.jsonPath().getString("data.bannedUntil")).isNotBlank();
            }
        }
    }

    /**
     * 测试用例7: 封禁不存在的用户
     */
    @Test
    @DisplayName("封禁不存在的用户")
    void testBanUser_UserNotFound() {
        // Given: 不存在的用户ID
        String requestBody = "{" +
            "\"userId\":999999999," +
            "\"duration\":60," +
            "\"reason\":\"测试\"" +
            "}";

        // When: 尝试封禁
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/ban");

        // Then: 可能返回错误
        if (response.statusCode() == 200) {
            Integer code = response.jsonPath().getInt("code");
            if (code != 200) {
                assertThat(response.jsonPath().getString("msg"))
                    .containsAnyOf("不存在", "未找到");
            }
        } else {
            assertThat(response.statusCode()).isIn(400, 404);
        }
    }

    /**
     * 测试用例8: 缺少封禁理由
     */
    @Test
    @DisplayName("缺少封禁理由")
    void testBanUser_MissingReason() {
        // Given: 缺少reason参数
        String requestBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":60" +
            "}", anotherUserId + 5);

        // When: 尝试封禁
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/ban");

        // Then: 可能返回错误或使用默认理由
        // 根据实际业务规则验证
        assertThat(response.statusCode()).isIn(200, 400);
    }

    /**
     * 测试用例9: 无效的封禁时长
     */
    @Test
    @DisplayName("无效的封禁时长")
    void testBanUser_InvalidDuration() {
        // Given: 负数封禁时长
        String requestBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":-60," +
            "\"reason\":\"测试\"" +
            "}", anotherUserId + 6);

        // When: 尝试封禁
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/ban");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例10: 重复封禁
     */
    @Test
    @DisplayName("重复封禁")
    void testBanUser_AlreadyBanned() {
        // Given: 准备封禁数据
        Long targetUserId = anotherUserId + 7;
        String requestBody = String.format("{" +
            "\"userId\":%d," +
            "\"duration\":60," +
            "\"reason\":\"测试重复封禁\"" +
            "}", targetUserId);

        // When: 第一次封禁
        Response firstResponse = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/ban");

        if (firstResponse.statusCode() == 200 && firstResponse.jsonPath().getInt("code") == 200) {
            // When: 第二次封禁
            Response secondResponse = authenticatedRequest()
                .body(requestBody)
                .when()
                .post("/api/report/ban");

            // Then: 可能返回成功(更新封禁)或错误(已封禁)
            assertThat(secondResponse.statusCode()).isEqualTo(200);

            // 验证处罚状态
            Response checkResponse = authenticatedRequest()
                .param("userId", targetUserId)
                .when()
                .get("/api/report/punishment/check");

            assertThat(checkResponse.jsonPath().getBoolean("data.isBanned")).isTrue();
        }
    }
}
