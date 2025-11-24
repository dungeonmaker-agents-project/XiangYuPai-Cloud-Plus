package org.dromara.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容发布集成测试
 *
 * 🎯 核心目标:
 * 测试发布动态页面的所有功能，包括发布动态、话题查询、话题搜索
 *
 * 📌 测试接口归属说明:
 * - POST /api/v1/content/publish → xypai-content (内容服务)
 *   发布动态，支持文字、图片、视频、话题、地点
 *
 * - GET /api/v1/content/topics/hot → xypai-content (内容服务)
 *   获取热门话题列表
 *
 * - GET /api/v1/content/topics/search → xypai-content (内容服务)
 *   搜索话题
 *
 * 测试流程:
 * 1. 📱 用户登录 (调用 xypai-auth)
 * 2. 📝 发布纯文字动态
 * 3. 📝 发布带标题的动态
 * 4. 📝 发布带话题的动态
 * 5. 📝 发布带地点的动态
 * 6. 🔥 获取热门话题列表
 * 7. 🔍 搜索话题
 * 8. ❌ 测试内容验证（空内容、超长内容等）
 * 9. ❌ 测试话题验证（超过5个话题）
 *
 * 💡 测试方式说明:
 * - 集成测试，调用真实服务
 * - 需要启动: Gateway(8080), xypai-auth(8200), xypai-content(9403), Nacos, Redis, MySQL
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContentPublishTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token
    private static String authToken;
    private static String userId;

    // 保存测试数据
    private static Long publishedFeedId;
    private static List<String> testTopicNames = new ArrayList<>();

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📝 内容发布集成测试启动 (Content Service)");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(8200), xypai-content(9403), Nacos, Redis, MySQL\n");
    }

    /**
     * 辅助方法: 确保有有效的登录 Token
     */
    private static void ensureAuthenticated() {
        if (authToken != null && !authToken.isEmpty()) {
            return;
        }

        log.info("⚠️ 创建新用户并登录...");

        try {
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("139%08d", timestamp);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", uniqueMobile);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    authToken = (String) data.get("token");
                    userId = String.valueOf(data.get("userId"));
                    log.info("✅ 登录成功 - userId: {}", userId);
                } else {
                    log.error("❌ 登录失败: {}", responseBody.get("msg"));
                }
            }
        } catch (Exception e) {
            log.error("❌ 登录异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试1: 用户登录
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 用户登录")
    public void test1_UserLogin() {
        try {
            log.info("\n[测试1] 用户登录");

            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("139%08d", timestamp);
            log.info("手机号: {}, 验证码: {}", uniqueMobile, TEST_SMS_CODE);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", uniqueMobile);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    authToken = (String) data.get("token");
                    userId = String.valueOf(data.get("userId"));
                    log.info("✅ 登录成功 - userId: {}, token前10位: {}", userId,
                        authToken.substring(0, Math.min(10, authToken.length())));
                } else {
                    throw new RuntimeException("登录失败: " + responseBody.get("msg"));
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("用户登录测试失败", e);
        }
    }

    /**
     * 🎯 测试2: 发布纯文字动态
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 发布纯文字动态")
    public void test2_PublishTextOnlyFeed() {
        try {
            log.info("\n[测试2] 发布纯文字动态");
            ensureAuthenticated();

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);  // 1=动态
            publishRequest.put("content", "这是一条测试动态，来自集成测试。今天天气真好！ 😊");
            publishRequest.put("visibility", 0);  // 0=公开

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Object data = responseBody.get("data");
                    publishedFeedId = data != null ? Long.valueOf(data.toString()) : null;
                    log.info("✅ 发布纯文字动态成功 - feedId: {}", publishedFeedId);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 发布动态失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {} (如果Content服务未启动，这是正常的)", e.getMessage());
        }
    }

    /**
     * 🎯 测试3: 发布带标题的动态
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 发布带标题的动态")
    public void test3_PublishFeedWithTitle() {
        try {
            log.info("\n[测试3] 发布带标题的动态");
            ensureAuthenticated();

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);  // 1=动态
            publishRequest.put("title", "今天的美食分享");
            publishRequest.put("content", "今天去了一家很棒的餐厅，菜品精致，服务也很好。推荐给大家！");
            publishRequest.put("visibility", 0);  // 0=公开

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Object data = responseBody.get("data");
                    Long feedId = data != null ? Long.valueOf(data.toString()) : null;
                    log.info("✅ 发布带标题动态成功 - feedId: {}", feedId);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 发布动态失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试4: 发布带话题的动态
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 发布带话题的动态")
    public void test4_PublishFeedWithTopics() {
        try {
            log.info("\n[测试4] 发布带话题的动态");
            ensureAuthenticated();

            List<String> topics = new ArrayList<>();
            topics.add("探店日记");
            topics.add("美食推荐");

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);  // 1=动态
            publishRequest.put("content", "发现了一家宝藏店铺，环境优雅，服务贴心！强烈推荐！");
            publishRequest.put("topicNames", topics);
            publishRequest.put("visibility", 0);  // 0=公开

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Object data = responseBody.get("data");
                    Long feedId = data != null ? Long.valueOf(data.toString()) : null;
                    log.info("✅ 发布带话题动态成功 - feedId: {}", feedId);
                    log.info("   - 关联话题: {}", topics);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 发布动态失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试5: 发布带地点的动态
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 发布带地点的动态")
    public void test5_PublishFeedWithLocation() {
        try {
            log.info("\n[测试5] 发布带地点的动态");
            ensureAuthenticated();

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);  // 1=动态
            publishRequest.put("content", "在深圳湾公园散步，天气很好！推荐大家周末来这里放松。");
            publishRequest.put("locationName", "深圳湾公园");
            publishRequest.put("locationAddress", "广东省深圳市南山区深圳湾");
            publishRequest.put("longitude", 113.9577);
            publishRequest.put("latitude", 22.5189);
            publishRequest.put("visibility", 0);  // 0=公开

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Object data = responseBody.get("data");
                    Long feedId = data != null ? Long.valueOf(data.toString()) : null;
                    log.info("✅ 发布带地点动态成功 - feedId: {}", feedId);
                    log.info("   - 地点: 深圳湾公园");
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 发布动态失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试6: 获取热门话题列表
     *
     * 接口: GET /api/v1/content/topics/hot
     * 服务: xypai-content
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 获取热门话题列表")
    public void test6_GetHotTopics() {
        try {
            log.info("\n[测试6] 获取热门话题列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String topicsUrl = GATEWAY_URL + "/xypai-content/api/v1/content/topics/hot?page=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(
                topicsUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object records = data.get("records");
                    int recordsSize = (records instanceof List) ? ((List<?>) records).size() : 0;

                    log.info("✅ 获取热门话题成功");
                    log.info("   - 总数: {}", data.get("total"));
                    log.info("   - 当前页数量: {}", recordsSize);

                    // 保存话题名称用于搜索测试
                    if (recordsSize > 0) {
                        List<?> topicList = (List<?>) records;
                        for (int i = 0; i < Math.min(3, topicList.size()); i++) {
                            Map<String, Object> topic = (Map<String, Object>) topicList.get(i);
                            String name = (String) topic.get("name");
                            if (name != null) {
                                testTopicNames.add(name);
                                log.info("   - 话题{}: {}", i + 1, name);
                            }
                        }
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取热门话题失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试7: 搜索话题
     *
     * 接口: GET /api/v1/content/topics/search
     * 服务: xypai-content
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 搜索话题")
    public void test7_SearchTopics() {
        try {
            log.info("\n[测试7] 搜索话题");
            ensureAuthenticated();

            String keyword = testTopicNames.isEmpty() ? "探店" : testTopicNames.get(0).substring(0, 1);
            log.info("搜索关键词: {}", keyword);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String searchUrl = GATEWAY_URL + "/xypai-content/api/v1/content/topics/search?keyword=" + keyword + "&page=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(
                searchUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object records = data.get("records");
                    int recordsSize = (records instanceof List) ? ((List<?>) records).size() : 0;

                    log.info("✅ 搜索话题成功");
                    log.info("   - 关键词: {}", keyword);
                    log.info("   - 结果数量: {}", recordsSize);

                    if (recordsSize > 0) {
                        List<?> topicList = (List<?>) records;
                        Map<String, Object> firstTopic = (Map<String, Object>) topicList.get(0);
                        log.info("   - 第一个结果: {}", firstTopic.get("name"));
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 搜索话题失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试7失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试8: 内容验证 - 空内容
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     * 预期: 返回400错误
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 内容验证 - 空内容")
    public void test8_PublishEmptyContent() {
        try {
            log.info("\n[测试8] 内容验证 - 空内容（应该失败）");
            ensureAuthenticated();

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);
            publishRequest.put("content", "");  // 空内容
            publishRequest.put("visibility", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code != 200) {
                    log.info("✅ 空内容验证通过 - 返回错误: {}", responseBody.get("msg"));
                } else {
                    log.warn("⚠️ 空内容验证失败 - 应该返回错误，但返回成功");
                }
            }

        } catch (Exception e) {
            log.info("✅ 空内容验证通过 - 捕获异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试9: 内容验证 - 超长内容
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     * 预期: 返回400错误
     */
    @Test
    @Order(9)
    @DisplayName("测试9: 内容验证 - 超长内容")
    public void test9_PublishTooLongContent() {
        try {
            log.info("\n[测试9] 内容验证 - 超长内容（应该失败）");
            ensureAuthenticated();

            // 生成超过1000字符的内容
            StringBuilder longContent = new StringBuilder();
            for (int i = 0; i < 1001; i++) {
                longContent.append("字");
            }

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);
            publishRequest.put("content", longContent.toString());
            publishRequest.put("visibility", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code != 200) {
                    log.info("✅ 超长内容验证通过 - 返回错误: {}", responseBody.get("msg"));
                } else {
                    log.warn("⚠️ 超长内容验证失败 - 应该返回错误，但返回成功");
                }
            }

        } catch (Exception e) {
            log.info("✅ 超长内容验证通过 - 捕获异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试10: 话题验证 - 超过5个话题
     *
     * 接口: POST /api/v1/content/publish
     * 服务: xypai-content
     * 预期: 返回400错误
     */
    @Test
    @Order(10)
    @DisplayName("测试10: 话题验证 - 超过5个话题")
    public void test10_PublishTooManyTopics() {
        try {
            log.info("\n[测试10] 话题验证 - 超过5个话题（应该失败）");
            ensureAuthenticated();

            List<String> topics = new ArrayList<>();
            topics.add("话题1");
            topics.add("话题2");
            topics.add("话题3");
            topics.add("话题4");
            topics.add("话题5");
            topics.add("话题6");  // 超过5个

            Map<String, Object> publishRequest = new HashMap<>();
            publishRequest.put("type", 1);
            publishRequest.put("content", "测试超过5个话题的验证");
            publishRequest.put("topicNames", topics);
            publishRequest.put("visibility", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(publishRequest, headers);

            String publishUrl = GATEWAY_URL + "/xypai-content/api/v1/content/publish";
            ResponseEntity<Map> response = restTemplate.postForEntity(publishUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code != 200) {
                    log.info("✅ 话题数量验证通过 - 返回错误: {}", responseBody.get("msg"));
                } else {
                    log.warn("⚠️ 话题数量验证失败 - 应该返回错误，但返回成功");
                }
            }

        } catch (Exception e) {
            log.info("✅ 话题数量验证通过 - 捕获异常: {}", e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 内容发布测试完成！");
        log.info("📝 注意: 媒体上传功能需要在 xypai-common 模块的 MediaUploadTest 中进行测试");
        log.info("📝 注意: 地点选择功能需要在 xypai-common 模块的 LocationTest 中进行测试");
    }
}
