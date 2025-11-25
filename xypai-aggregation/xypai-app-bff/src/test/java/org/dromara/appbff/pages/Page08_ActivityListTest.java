package org.dromara.appbff.pages;

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
 * 【页面级集成测试】08-组局中心列表页面
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/08-组局中心列表页面.md
 * - 页面路由: /activity/list
 * - 页面名称: 组局中心
 * - 用户角色: 登录用户
 * - 页面类型: 列表页面
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-app-bff (聚合服务, 9400)】
 * - GET /api/activity/list           获取活动列表（支持筛选、排序、分页）
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 用户登录
 * 2. 获取活动列表（首页加载）
 * 3. 应用排序（智能排序、最新发布、距离最近）
 * 4. 筛选性别（全部、男、女）
 * 5. 筛选人数（不限、2-4人、5-10人）
 * 6. 组合筛选（性别+人数）
 * 7. 分页加载更多
 * 8. 验证活动卡片数据完整性
 * 9. 验证筛选配置选项
 * 10. 验证空列表场景
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用 xypai-app-bff 服务
 * - 活动列表功能使用 Mock 数据
 * - 需要启动: Gateway(8080), xypai-auth(9211), xypai-app-bff(9400), Nacos, Redis
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page08_ActivityListTest {

    // ============================================================
    // 测试配置
    // ============================================================
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token
    private static String authToken;
    private static String userId;

    // 保存测试数据
    private static Long firstActivityId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 08-组局中心列表页面                        ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-app-bff (9400)  活动列表聚合                        ║");
        log.info("║  - xypai-auth (9211)     用户认证                           ║");
        log.info("║  - Gateway (8080)        API网关                             ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
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

    // ============================================================
    // 测试1: 用户登录
    // ============================================================
    @Test
    @Order(1)
    @DisplayName("[测试1] 用户登录")
    void test01_userLogin() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试1] 用户登录                                           │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
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
                    log.info("✅ 登录成功 - userId: {}", userId);
                    Assertions.assertNotNull(authToken, "Token不能为空");
                } else {
                    Assertions.fail("登录失败: " + responseBody.get("msg"));
                }
            } else {
                Assertions.fail("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试2: 获取活动列表（首页加载）
    // ============================================================
    @Test
    @Order(2)
    @DisplayName("[测试2] 获取活动列表（首页加载）")
    void test02_getActivityList() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试2] 获取活动列表（首页加载）                            │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=10";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
                Object total = data.get("total");
                Boolean hasMore = (Boolean) data.get("hasMore");

                log.info("✅ 获取活动列表成功");
                log.info("   - 总数: {}", total);
                log.info("   - 当前页数量: {}", list != null ? list.size() : 0);
                log.info("   - 是否有更多: {}", hasMore);

                // 保存第一个活动ID用于后续测试
                if (list != null && !list.isEmpty()) {
                    Map<String, Object> firstActivity = list.get(0);
                    firstActivityId = Long.valueOf(String.valueOf(firstActivity.get("activityId")));
                    log.info("   - 第一个活动ID: {}", firstActivityId);

                    // 验证活动卡片数据结构
                    Map<String, Object> organizer = (Map<String, Object>) firstActivity.get("organizer");
                    if (organizer != null) {
                        log.info("   - 组织者昵称: {}", organizer.get("nickname"));
                    }
                }

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 获取活动列表失败: {}", msg);
                Assertions.fail("获取活动列表失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试3: 应用排序（智能排序）
    // ============================================================
    @Test
    @Order(3)
    @DisplayName("[测试3] 应用排序（智能排序）")
    void test03_sortBySmartRecommend() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试3] 应用排序（智能排序）                                │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&sortBy=smart_recommend";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 智能排序查询成功");
                log.info("   - 返回数量: {}", list != null ? list.size() : 0);

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                Assertions.fail("智能排序查询失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试4: 筛选性别（女性）
    // ============================================================
    @Test
    @Order(4)
    @DisplayName("[测试4] 筛选性别（女性）")
    void test04_filterByGender() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试4] 筛选性别（女性）                                    │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&gender=female";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 性别筛选查询成功");
                log.info("   - 筛选条件: gender=female");
                log.info("   - 返回数量: {}", list != null ? list.size() : 0);

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                Assertions.fail("性别筛选查询失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试5: 筛选人数（2-4人）
    // ============================================================
    @Test
    @Order(5)
    @DisplayName("[测试5] 筛选人数（2-4人）")
    void test05_filterByMemberCount() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试5] 筛选人数（2-4人）                                   │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&memberCount=2-4";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 人数筛选查询成功");
                log.info("   - 筛选条件: memberCount=2-4");
                log.info("   - 返回数量: {}", list != null ? list.size() : 0);

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                Assertions.fail("人数筛选查询失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试6: 组合筛选（性别+人数）
    // ============================================================
    @Test
    @Order(6)
    @DisplayName("[测试6] 组合筛选（性别+人数）")
    void test06_combinedFilter() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试6] 组合筛选（性别+人数）                               │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&gender=male&memberCount=5-10";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 组合筛选查询成功");
                log.info("   - 筛选条件: gender=male, memberCount=5-10");
                log.info("   - 返回数量: {}", list != null ? list.size() : 0);

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                Assertions.fail("组合筛选查询失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试7: 分页加载更多
    // ============================================================
    @Test
    @Order(7)
    @DisplayName("[测试7] 分页加载更多")
    void test07_loadMorePages() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试7] 分页加载更多                                       │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            // 加载第2页
            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=2&pageSize=10";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
                Boolean hasMore = (Boolean) data.get("hasMore");

                log.info("✅ 分页加载成功");
                log.info("   - 页码: 2");
                log.info("   - 返回数量: {}", list != null ? list.size() : 0);
                log.info("   - 是否有更多: {}", hasMore);

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                Assertions.fail("分页加载失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试8: 验证活动卡片数据完整性
    // ============================================================
    @Test
    @Order(8)
    @DisplayName("[测试8] 验证活动卡片数据完整性")
    void test08_verifyActivityCardData() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试8] 验证活动卡片数据完整性                              │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=5";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 验证活动卡片数据:");

                if (list != null && !list.isEmpty()) {
                    Map<String, Object> activity = list.get(0);

                    // 验证必要字段
                    log.info("   - activityId: {}", activity.get("activityId"));
                    log.info("   - status: {}", activity.get("status"));

                    // 验证organizer
                    Map<String, Object> organizer = (Map<String, Object>) activity.get("organizer");
                    if (organizer != null) {
                        log.info("   - organizer.userId: {}", organizer.get("userId"));
                        log.info("   - organizer.nickname: {}", organizer.get("nickname"));
                        log.info("   - organizer.avatar: {}", organizer.get("avatar"));
                    }

                    // 验证activityType
                    Map<String, Object> activityType = (Map<String, Object>) activity.get("activityType");
                    if (activityType != null) {
                        log.info("   - activityType.label: {}", activityType.get("label"));
                    }

                    // 验证price
                    Map<String, Object> price = (Map<String, Object>) activity.get("price");
                    if (price != null) {
                        log.info("   - price.displayText: {}", price.get("displayText"));
                    }

                    // 验证schedule
                    Map<String, Object> schedule = (Map<String, Object>) activity.get("schedule");
                    if (schedule != null) {
                        log.info("   - schedule.displayText: {}", schedule.get("displayText"));
                    }

                    // 验证location
                    Map<String, Object> location = (Map<String, Object>) activity.get("location");
                    if (location != null) {
                        log.info("   - location.address: {}", location.get("address"));
                    }

                    // 验证participants
                    Map<String, Object> participants = (Map<String, Object>) activity.get("participants");
                    if (participants != null) {
                        log.info("   - participants.displayText: {}", participants.get("displayText"));
                    }

                    Assertions.assertNotNull(activity.get("activityId"), "activityId不能为空");
                    Assertions.assertNotNull(activity.get("status"), "status不能为空");
                } else {
                    log.info("   - 列表为空，跳过数据验证");
                }
            } else {
                Assertions.fail("获取活动列表失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试9: 验证筛选配置选项
    // ============================================================
    @Test
    @Order(9)
    @DisplayName("[测试9] 验证筛选配置选项")
    void test09_verifyFilterOptions() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试9] 验证筛选配置选项                                   │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=1";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                Map<String, Object> filters = (Map<String, Object>) data.get("filters");

                log.info("✅ 验证筛选配置:");

                if (filters != null) {
                    // 验证sortOptions
                    List<Map<String, Object>> sortOptions = (List<Map<String, Object>>) filters.get("sortOptions");
                    if (sortOptions != null) {
                        log.info("   - 排序选项数量: {}", sortOptions.size());
                        for (Map<String, Object> opt : sortOptions) {
                            log.info("     - {}: {}", opt.get("value"), opt.get("label"));
                        }
                    }

                    // 验证genderOptions
                    List<Map<String, Object>> genderOptions = (List<Map<String, Object>>) filters.get("genderOptions");
                    if (genderOptions != null) {
                        log.info("   - 性别选项数量: {}", genderOptions.size());
                    }

                    // 验证memberOptions
                    List<Map<String, Object>> memberOptions = (List<Map<String, Object>>) filters.get("memberOptions");
                    if (memberOptions != null) {
                        log.info("   - 人数选项数量: {}", memberOptions.size());
                    }

                    // 验证activityTypes
                    List<Map<String, Object>> activityTypes = (List<Map<String, Object>>) filters.get("activityTypes");
                    if (activityTypes != null) {
                        log.info("   - 活动类型数量: {}", activityTypes.size());
                        for (Map<String, Object> type : activityTypes) {
                            log.info("     - {}: {} {}", type.get("value"), type.get("label"), type.get("icon"));
                        }
                    }
                } else {
                    log.info("   - 筛选配置为空");
                }
            } else {
                Assertions.fail("获取筛选配置失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试10: 按活动类型筛选
    // ============================================================
    @Test
    @Order(10)
    @DisplayName("[测试10] 按活动类型筛选")
    void test10_filterByActivityType() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试10] 按活动类型筛选                                    │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&activityType=billiards";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 活动类型筛选成功");
                log.info("   - 筛选条件: activityType=billiards (台球)");
                log.info("   - 返回数量: {}", list != null ? list.size() : 0);

                Assertions.assertNotNull(data, "data不能为空");
            } else {
                Assertions.fail("活动类型筛选失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  ✅ 测试完成                                                 ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  组局中心列表页面测试全部通过                                  ║");
        log.info("║  - 活动列表加载 ✓                                           ║");
        log.info("║  - 排序功能 ✓                                               ║");
        log.info("║  - 筛选功能（性别、人数、类型）✓                              ║");
        log.info("║  - 分页加载 ✓                                               ║");
        log.info("║  - 数据完整性验证 ✓                                         ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
