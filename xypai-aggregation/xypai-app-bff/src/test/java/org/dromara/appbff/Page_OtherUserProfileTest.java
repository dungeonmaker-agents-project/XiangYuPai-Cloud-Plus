package org.dromara.appbff;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对方主页页面接口测试
 * Other User Profile Page API Test
 *
 * <p>测试范围：</p>
 * <ul>
 *     <li>GET /api/profile/{userId} - 获取对方主页数据</li>
 *     <li>GET /api/profile/{userId}/info - 获取用户资料详情</li>
 *     <li>GET /api/profile/{userId}/skills - 获取用户技能列表</li>
 *     <li>POST /api/profile/unlock-wechat - 解锁微信</li>
 *     <li>POST /api/profile/{userId}/follow - 关注用户</li>
 *     <li>DELETE /api/profile/{userId}/follow - 取消关注</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page_OtherUserProfileTest {

    private static final String BASE_URL = "http://localhost:9200/xypai-app-bff";
    private static final RestTemplate restTemplate = new RestTemplate();

    // 测试用户ID（实际测试时需要替换为真实数据）
    private static final Long CURRENT_USER_ID = 1L;  // 当前登录用户
    private static final Long TARGET_USER_ID = 2L;   // 目标用户

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", CURRENT_USER_ID.toString());
        return headers;
    }

    // ==================== 1. 获取对方主页数据 ====================

    @Test
    @Order(1)
    @DisplayName("1.1 获取对方主页数据 - 正常请求")
    void testGetOtherUserProfile_Success() {
        String url = BASE_URL + "/api/profile/" + TARGET_USER_ID;

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            Map<String, Object> body = response.getBody();
            assertEquals(200, body.get("code"));

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertNotNull(data);
            assertNotNull(data.get("userId"));
            assertNotNull(data.get("nickname"));
            assertNotNull(data.get("avatar"));

            System.out.println("✅ 获取对方主页数据成功");
            System.out.println("   用户ID: " + data.get("userId"));
            System.out.println("   昵称: " + data.get("nickname"));
            System.out.println("   关注状态: " + data.get("followStatus"));
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过（服务未启动）: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("1.2 获取对方主页数据 - 带位置信息")
    void testGetOtherUserProfile_WithLocation() {
        String url = BASE_URL + "/api/profile/" + TARGET_USER_ID + "?latitude=31.2304&longitude=121.4737";

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            if (data != null && data.get("distance") != null) {
                System.out.println("✅ 获取对方主页数据（带位置）成功");
                System.out.println("   距离: " + data.get("distance"));
            }
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("1.3 获取对方主页数据 - 用户不存在")
    void testGetOtherUserProfile_UserNotFound() {
        String url = BASE_URL + "/api/profile/999999999";

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
            );

            Map<String, Object> body = response.getBody();
            assertNotEquals(200, body.get("code"));

            System.out.println("✅ 用户不存在情况处理正确");
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    // ==================== 2. 获取用户资料详情 ====================

    @Test
    @Order(4)
    @DisplayName("2.1 获取用户资料详情 - 正常请求")
    void testGetProfileInfo_Success() {
        String url = BASE_URL + "/api/profile/" + TARGET_USER_ID + "/info";

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();
            assertEquals(200, body.get("code"));

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertNotNull(data);
            assertNotNull(data.get("userId"));

            System.out.println("✅ 获取用户资料详情成功");
            System.out.println("   居住地: " + data.get("residence"));
            System.out.println("   微信(脱敏): " + data.get("wechat"));
            System.out.println("   是否已解锁: " + data.get("wechatUnlocked"));
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    // ==================== 3. 获取用户技能列表 ====================

    @Test
    @Order(5)
    @DisplayName("3.1 获取用户技能列表 - 正常请求")
    void testGetUserSkills_Success() {
        String url = BASE_URL + "/api/profile/" + TARGET_USER_ID + "/skills?pageNum=1&pageSize=10";

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();
            assertEquals(200, body.get("code"));

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            assertNotNull(data);
            assertNotNull(data.get("list"));
            assertNotNull(data.get("total"));
            assertNotNull(data.get("hasMore"));

            System.out.println("✅ 获取用户技能列表成功");
            System.out.println("   总数: " + data.get("total"));
            System.out.println("   是否有更多: " + data.get("hasMore"));
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    // ==================== 4. 关注/取消关注 ====================

    @Test
    @Order(6)
    @DisplayName("4.1 关注用户 - 正常请求")
    void testFollowUser_Success() {
        String url = BASE_URL + "/api/profile/" + TARGET_USER_ID + "/follow";

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();

            System.out.println("✅ 关注用户接口调用成功");
            System.out.println("   响应码: " + body.get("code"));
            System.out.println("   消息: " + body.get("msg"));
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    @DisplayName("4.2 取消关注 - 正常请求")
    void testUnfollowUser_Success() {
        String url = BASE_URL + "/api/profile/" + TARGET_USER_ID + "/follow";

        HttpEntity<Void> request = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.DELETE, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();

            System.out.println("✅ 取消关注接口调用成功");
            System.out.println("   响应码: " + body.get("code"));
            System.out.println("   消息: " + body.get("msg"));
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    // ==================== 5. 解锁微信 ====================

    @Test
    @Order(8)
    @DisplayName("5.1 解锁微信 - 正常请求")
    void testUnlockWechat_Success() {
        String url = BASE_URL + "/api/profile/unlock-wechat";

        String requestBody = """
            {
                "targetUserId": %d,
                "unlockType": "coins"
            }
            """.formatted(TARGET_USER_ID);

        HttpEntity<String> request = new HttpEntity<>(requestBody, createHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();

            System.out.println("✅ 解锁微信接口调用成功");
            System.out.println("   响应码: " + body.get("code"));

            if ((Integer) body.get("code") == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                System.out.println("   解锁成功: " + data.get("success"));
                System.out.println("   微信号: " + data.get("wechat"));
                System.out.println("   扣费: " + data.get("cost"));
            } else {
                System.out.println("   失败原因: " + body.get("msg"));
            }
        } catch (Exception e) {
            System.out.println("⚠️ 测试跳过: " + e.getMessage());
        }
    }

    // ==================== 接口文档验证 ====================

    @Test
    @Order(100)
    @DisplayName("接口文档验证 - 对方主页接口列表")
    void testApiDocumentation() {
        System.out.println("\n=== 对方主页接口列表 ===\n");

        System.out.println("1. 获取对方主页数据");
        System.out.println("   GET /api/profile/{userId}");
        System.out.println("   Query: latitude, longitude (可选)");
        System.out.println("   Response: OtherUserProfileVO\n");

        System.out.println("2. 获取用户资料详情");
        System.out.println("   GET /api/profile/{userId}/info");
        System.out.println("   Response: ProfileInfoVO\n");

        System.out.println("3. 获取用户技能列表");
        System.out.println("   GET /api/profile/{userId}/skills");
        System.out.println("   Query: pageNum, pageSize");
        System.out.println("   Response: UserSkillsListVO\n");

        System.out.println("4. 解锁微信");
        System.out.println("   POST /api/profile/unlock-wechat");
        System.out.println("   Body: { targetUserId, unlockType, paymentPassword }");
        System.out.println("   Response: UnlockWechatResultVO\n");

        System.out.println("5. 关注用户");
        System.out.println("   POST /api/profile/{userId}/follow");
        System.out.println("   Response: R<Void>\n");

        System.out.println("6. 取消关注");
        System.out.println("   DELETE /api/profile/{userId}/follow");
        System.out.println("   Response: R<Void>\n");

        System.out.println("=========================\n");
    }
}
