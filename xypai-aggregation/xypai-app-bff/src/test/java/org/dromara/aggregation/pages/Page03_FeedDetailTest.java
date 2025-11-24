package org.dromara.aggregation.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【页面级集成测试】03-动态详情页面
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/03-动态详情页面.md
 * - 页面路由: /feed/detail/:feedId
 * - 页面名称: 动态详情页
 * - 用户角色: 已登录用户/游客(部分功能限制)
 * - 页面类型: 详情页
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-content (内容服务, 9403)】
 * - GET    /xypai-content/api/v1/content/detail/{feedId}       获取动态详情
 * - GET    /xypai-content/api/v1/content/comments/{feedId}     获取评论列表
 * - POST   /xypai-content/api/v1/content/comment                发布评论/回复
 * - DELETE /xypai-content/api/v1/content/comment/{commentId}   删除评论
 * - DELETE /xypai-content/api/v1/content/{feedId}               删除动态
 *
 * 【xypai-content (互动服务)】
 * - POST   /xypai-content/api/v1/interaction/like               点赞/取消点赞
 * - POST   /xypai-content/api/v1/interaction/collect            收藏/取消收藏
 * - POST   /xypai-content/api/v1/interaction/share              分享动态
 *
 * 【xypai-auth (认证服务, 8200)】
 * - POST   /xypai-auth/auth/login/sms                        用户登录
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 用户A登录 (xypai-auth)
 * 2. 用户A发布一条动态 (xypai-content)
 * 3. 用户A获取动态详情 (xypai-content)
 * 4. 用户A点赞动态 (xypai-content)
 * 5. 用户A取消点赞 (xypai-content)
 * 6. 用户A收藏动态 (xypai-content)
 * 7. 用户A发布一级评论 (xypai-content)
 * 8. 用户A获取评论列表 (xypai-content)
 * 9. 用户B登录 (xypai-auth)
 * 10. 用户B发布二级回复 (xypai-content)
 * 11. 用户B点赞评论 (xypai-content)
 * 12. 用户B分享动态 (xypai-content)
 * 13. 用户A删除自己的评论 (xypai-content)
 * 14. 用户A删除自己的动态 (xypai-content)
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用各个微服务
 * - 动态详情是核心社交功能，包含完整的CRUD和互动功能
 * - 需要启动: Gateway(8080), xypai-auth(8200), xypai-content(9403), Nacos, MySQL, Redis
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page03_FeedDetailTest {

    // ============================================================
    // 测试配置
    // ============================================================
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_PHONE_USER_A = "13800000002";
    private static final String TEST_PHONE_USER_B = "13800000003";
    private static final String TEST_SMS_CODE = "123456";

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token
    private static String authTokenUserA;
    private static String authTokenUserB;
    private static String userIdA;
    private static String userIdB;

    // 保存测试数据ID
    private static Long testFeedId;
    private static Long testCommentId;
    private static Long testReplyId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 03-动态详情页面                            ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-auth (8200)     用户认证                            ║");
        log.info("║  - xypai-content (9403)  动态、评论、互动                     ║");
        log.info("║  - Gateway (8080)        API网关                             ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }

    // ============================================================
    // 测试1: 用户A登录
    // ============================================================
    @Test
    @Order(1)
    @DisplayName("[测试1] 用户A登录")
    void test01_userALogin() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试1] 用户A登录                                         │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-auth/auth/login/sms";

            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", TEST_PHONE_USER_A);
            request.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                authTokenUserA = (String) data.get("token");
                userIdA = String.valueOf(data.get("userId"));

                log.info("✅ 用户A登录成功");
                log.info("   - Token: {}...", authTokenUserA.substring(0, Math.min(20, authTokenUserA.length())));
                log.info("   - 用户ID: {}", userIdA);

                Assertions.assertNotNull(authTokenUserA, "用户A Token不能为空");
                Assertions.assertNotNull(userIdA, "用户A ID不能为空");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 用户A登录失败: {}", msg);
                Assertions.fail("用户A登录失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 用户A登录异常", e);
            Assertions.fail("用户A登录异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试2: 用户A发布动态
    // ============================================================
    @Test
    @Order(2)
    @DisplayName("[测试2] 用户A发布动态")
    void test02_publishFeed() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试2] 用户A发布动态                                      │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/publish";

            Map<String, Object> request = new HashMap<>();
            request.put("type", 1); // 1=动态
            request.put("content", "这是一条测试动态，用于动态详情页测试。大家觉得怎么样？#动态测试 #集成测试");
            request.put("visibility", 0); // 0=公开

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Object dataObj = responseBody.get("data");
                if (dataObj != null) {
                    testFeedId = Long.valueOf(String.valueOf(dataObj));
                    log.info("✅ 发布动态成功");
                    log.info("   - 动态ID: {}", testFeedId);
                    Assertions.assertNotNull(testFeedId, "动态ID不能为空");
                } else {
                    log.error("❌ data字段为null");
                    Assertions.fail("发布动态失败: data字段为null");
                }
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 发布动态失败: {}", msg);
                Assertions.fail("发布动态失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 发布动态异常", e);
            Assertions.fail("发布动态异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试3: 用户A获取动态详情
    // ============================================================
    @Test
    @Order(3)
    @DisplayName("[测试3] 用户A获取动态详情")
    void test03_getFeedDetail() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试3] 用户A获取动态详情                                  │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/detail/" + testFeedId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                log.info("✅ 获取动态详情成功");
                log.info("   - 动态ID: {}", data.get("id"));
                log.info("   - 内容: {}", data.get("content"));
                log.info("   - 点赞数: {}", data.get("likeCount"));
                log.info("   - 评论数: {}", data.get("commentCount"));
                log.info("   - 收藏数: {}", data.get("collectCount"));
                log.info("   - 是否已点赞: {}", data.get("isLiked"));
                log.info("   - 是否已收藏: {}", data.get("isCollected"));

                Map<String, Object> userInfo = (Map<String, Object>) data.get("userInfo");
                if (userInfo != null) {
                    log.info("   - 作者昵称: {}", userInfo.get("nickname"));
                    log.info("   - 作者头像: {}", userInfo.get("avatar"));
                }

                Assertions.assertNotNull(data.get("id"), "动态ID不能为空");
                Assertions.assertNotNull(data.get("content"), "动态内容不能为空");
                Assertions.assertEquals(testFeedId, Long.valueOf(String.valueOf(data.get("id"))), "动态ID匹配");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 获取动态详情失败: {}", msg);
                Assertions.fail("获取动态详情失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 获取动态详情异常", e);
            Assertions.fail("获取动态详情异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试4: 用户A点赞动态
    // ============================================================
    @Test
    @Order(4)
    @DisplayName("[测试4] 用户A点赞动态")
    void test04_likeFeed() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试4] 用户A点赞动态                                      │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/interaction/like";

            Map<String, Object> request = new HashMap<>();
            request.put("targetType", "feed");
            request.put("targetId", testFeedId);
            request.put("action", "like");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                log.info("✅ 点赞动态成功");
                log.info("   - 操作成功: {}", data.get("success"));
                log.info("   - 点赞数: {}", data.get("count"));
                log.info("   - 当前状态: {}", data.get("isActive") + " (true=已点赞)");

                Assertions.assertTrue((Boolean) data.get("success"), "点赞操作应该成功");
                Assertions.assertTrue((Boolean) data.get("isActive"), "应该处于已点赞状态");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 点赞动态失败: {}", msg);
                Assertions.fail("点赞动态失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 点赞动态异常", e);
            Assertions.fail("点赞动态异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试5: 用户A取消点赞
    // ============================================================
    @Test
    @Order(5)
    @DisplayName("[测试5] 用户A取消点赞")
    void test05_unlikeFeed() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试5] 用户A取消点赞                                      │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/interaction/like";

            Map<String, Object> request = new HashMap<>();
            request.put("targetType", "feed");
            request.put("targetId", testFeedId);
            request.put("action", "unlike");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                log.info("✅ 取消点赞成功");
                log.info("   - 操作成功: {}", data.get("success"));
                log.info("   - 点赞数: {}", data.get("count"));
                log.info("   - 当前状态: {}", data.get("isActive") + " (false=未点赞)");

                Assertions.assertTrue((Boolean) data.get("success"), "取消点赞操作应该成功");
                Assertions.assertFalse((Boolean) data.get("isActive"), "应该处于未点赞状态");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 取消点赞失败: {}", msg);
                Assertions.fail("取消点赞失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 取消点赞异常", e);
            Assertions.fail("取消点赞异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试6: 用户A收藏动态
    // ============================================================
    @Test
    @Order(6)
    @DisplayName("[测试6] 用户A收藏动态")
    void test06_collectFeed() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试6] 用户A收藏动态                                      │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/interaction/collect";

            Map<String, Object> request = new HashMap<>();
            request.put("targetType", "feed");
            request.put("targetId", testFeedId);
            request.put("action", "collect");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                log.info("✅ 收藏动态成功");
                log.info("   - 操作成功: {}", data.get("success"));
                log.info("   - 收藏数: {}", data.get("count"));
                log.info("   - 当前状态: {}", data.get("isActive") + " (true=已收藏)");

                Assertions.assertTrue((Boolean) data.get("success"), "收藏操作应该成功");
                Assertions.assertTrue((Boolean) data.get("isActive"), "应该处于已收藏状态");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 收藏动态失败: {}", msg);
                Assertions.fail("收藏动态失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 收藏动态异常", e);
            Assertions.fail("收藏动态异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试7: 用户A发布一级评论
    // ============================================================
    @Test
    @Order(7)
    @DisplayName("[测试7] 用户A发布一级评论")
    void test07_publishComment() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试7] 用户A发布一级评论                                  │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/comment";

            Map<String, Object> request = new HashMap<>();
            request.put("feedId", testFeedId);
            request.put("content", "这是一条测试评论，测试评论功能是否正常！");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                testCommentId = Long.valueOf(String.valueOf(data.get("id")));
                log.info("✅ 发布评论成功");
                log.info("   - 评论ID: {}", testCommentId);
                log.info("   - 评论内容: {}", data.get("content"));
                log.info("   - 点赞数: {}", data.get("likeCount"));
                log.info("   - 回复数: {}", data.get("replyCount"));

                Assertions.assertNotNull(testCommentId, "评论ID不能为空");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 发布评论失败: {}", msg);
                Assertions.fail("发布评论失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 发布评论异常", e);
            Assertions.fail("发布评论异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试8: 用户A获取评论列表
    // ============================================================
    @Test
    @Order(8)
    @DisplayName("[测试8] 用户A获取评论列表")
    void test08_getCommentList() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试8] 用户A获取评论列表                                  │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/comments/" + testFeedId + "?pageNum=1&pageSize=10&sortType=hot";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");

                log.info("✅ 获取评论列表成功");
                log.info("   - 总评论数: {}", data.get("total"));
                log.info("   - 当前页评论数: {}", records.size());

                if (!records.isEmpty()) {
                    Map<String, Object> firstComment = records.get(0);
                    log.info("   - 第一条评论ID: {}", firstComment.get("id"));
                    log.info("   - 第一条评论内容: {}", firstComment.get("content"));

                    Map<String, Object> userInfo = (Map<String, Object>) firstComment.get("userInfo");
                    if (userInfo != null) {
                        log.info("   - 评论者昵称: {}", userInfo.get("nickname"));
                    }
                }

                Assertions.assertNotNull(records, "评论列表不能为空");
                Assertions.assertTrue(records.size() > 0, "应该至少有一条评论");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 获取评论列表失败: {}", msg);
                Assertions.fail("获取评论列表失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 获取评论列表异常", e);
            Assertions.fail("获取评论列表异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试9: 用户B登录
    // ============================================================
    @Test
    @Order(9)
    @DisplayName("[测试9] 用户B登录")
    void test09_userBLogin() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试9] 用户B登录                                         │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-auth/auth/login/sms";

            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", TEST_PHONE_USER_B);
            request.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                authTokenUserB = (String) data.get("token");
                userIdB = String.valueOf(data.get("userId"));

                log.info("✅ 用户B登录成功");
                log.info("   - Token: {}...", authTokenUserB.substring(0, Math.min(20, authTokenUserB.length())));
                log.info("   - 用户ID: {}", userIdB);

                Assertions.assertNotNull(authTokenUserB, "用户B Token不能为空");
                Assertions.assertNotNull(userIdB, "用户B ID不能为空");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 用户B登录失败: {}", msg);
                Assertions.fail("用户B登录失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 用户B登录异常", e);
            Assertions.fail("用户B登录异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试10: 用户B发布二级回复
    // ============================================================
    @Test
    @Order(10)
    @DisplayName("[测试10] 用户B发布二级回复")
    void test10_publishReply() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试10] 用户B发布二级回复                                 │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/comment";

            Map<String, Object> request = new HashMap<>();
            request.put("feedId", testFeedId);
            request.put("content", "这是对评论的回复，测试二级评论功能！");
            request.put("parentId", testCommentId);
            request.put("replyToUserId", Long.valueOf(userIdA));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserB);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                testReplyId = Long.valueOf(String.valueOf(data.get("id")));
                log.info("✅ 发布二级回复成功");
                log.info("   - 回复ID: {}", testReplyId);
                log.info("   - 回复内容: {}", data.get("content"));

                Assertions.assertNotNull(testReplyId, "回复ID不能为空");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 发布二级回复失败: {}", msg);
                Assertions.fail("发布二级回复失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 发布二级回复异常", e);
            Assertions.fail("发布二级回复异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试11: 用户B点赞评论
    // ============================================================
    @Test
    @Order(11)
    @DisplayName("[测试11] 用户B点赞评论")
    void test11_likeComment() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试11] 用户B点赞评论                                     │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/interaction/like";

            Map<String, Object> request = new HashMap<>();
            request.put("targetType", "comment");
            request.put("targetId", testCommentId);
            request.put("action", "like");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserB);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                log.info("✅ 点赞评论成功");
                log.info("   - 操作成功: {}", data.get("success"));
                log.info("   - 点赞数: {}", data.get("count"));
                log.info("   - 当前状态: {}", data.get("isActive") + " (true=已点赞)");

                Assertions.assertTrue((Boolean) data.get("success"), "点赞评论操作应该成功");
                Assertions.assertTrue((Boolean) data.get("isActive"), "应该处于已点赞状态");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 点赞评论失败: {}", msg);
                Assertions.fail("点赞评论失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 点赞评论异常", e);
            Assertions.fail("点赞评论异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试12: 用户B分享动态
    // ============================================================
    @Test
    @Order(12)
    @DisplayName("[测试12] 用户B分享动态")
    void test12_shareFeed() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试12] 用户B分享动态                                     │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/interaction/share";

            Map<String, Object> request = new HashMap<>();
            request.put("targetType", "feed");
            request.put("targetId", testFeedId);
            request.put("shareChannel", "wechat");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authTokenUserB);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                log.info("✅ 分享动态成功");
                log.info("   - 操作成功: {}", data.get("success"));

                Assertions.assertTrue((Boolean) data.get("success"), "分享操作应该成功");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 分享动态失败: {}", msg);
                Assertions.fail("分享动态失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 分享动态异常", e);
            Assertions.fail("分享动态异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试13: 用户A删除自己的评论
    // ============================================================
    @Test
    @Order(13)
    @DisplayName("[测试13] 用户A删除自己的评论")
    void test13_deleteComment() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试13] 用户A删除自己的评论                               │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/comment/" + testCommentId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                log.info("✅ 删除评论成功");
                log.info("   - 已删除评论ID: {}", testCommentId);
                Assertions.assertEquals(200, code, "删除操作应该成功");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 删除评论失败: {}", msg);
                Assertions.fail("删除评论失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 删除评论异常", e);
            Assertions.fail("删除评论异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试14: 用户A删除自己的动态
    // ============================================================
    @Test
    @Order(14)
    @DisplayName("[测试14] 用户A删除自己的动态")
    void test14_deleteFeed() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试14] 用户A删除自己的动态                               │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-content/api/v1/content/" + testFeedId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authTokenUserA);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                log.info("✅ 删除动态成功");
                log.info("   - 已删除动态ID: {}", testFeedId);
                Assertions.assertEquals(200, code, "删除操作应该成功");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 删除动态失败: {}", msg);
                Assertions.fail("删除动态失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 删除动态异常", e);
            Assertions.fail("删除动态异常: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  ✅ 测试完成                                                 ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  测试数据清理:                                               ║");
        log.info("║  - 动态ID: {} (已删除)                                       ║", testFeedId);
        log.info("║  - 评论ID: {} (已删除)                                       ║", testCommentId);
        log.info("║  - 回复ID: {} (已删除-级联)                                  ║", testReplyId);
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
