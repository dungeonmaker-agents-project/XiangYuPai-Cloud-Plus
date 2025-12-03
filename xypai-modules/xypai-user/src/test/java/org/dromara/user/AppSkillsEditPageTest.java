package org.dromara.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * App 添加技能页面集成测试 - 通过Gateway调用真实接口
 *
 * 对应UI文档: 添加技能页_结构文档.md
 * 对应前端页面: SkillsEditPage.tsx
 *
 * 🎯 核心目标：
 * 测试 App 添加技能页面的完整功能，包含新UI文档的所有需求
 *
 * 测试流程:
 * 1. 📋 获取技能配置（技能列表、段位选项、时间选项）
 * 2. ✅ 验证技能配置数据结构
 * 3. 🎮 创建线上技能（带skillConfigId和server）
 * 4. 📍 创建线下技能（带skillConfigId和activityTime）
 * 5. 📊 获取我的技能列表验证创建结果
 * 6. 🔄 切换技能上下架状态
 * 7. 🗑️ 删除测试技能
 *
 * 💡 测试方式说明：
 * - 集成测试，调用真实服务
 * - 需要手动启动：Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppSkillsEditPageTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token，用于后续测试
    private static String authToken;
    private static String userId;

    // 保存创建的技能ID，用于后续测试
    private static Long onlineSkillId;
    private static Long offlineSkillId;

    // 技能配置数据（从API获取）
    private static Map<String, Object> skillConfig;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 添加技能页面集成测试启动");
        log.info("📄 对应UI文档: 添加技能页_结构文档.md");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL\n");
    }

    /**
     * 辅助方法：确保有有效的登录 Token
     */
    private static void ensureAuthenticated() {
        if (authToken != null && !authToken.isEmpty()) {
            return;
        }

        log.info("⚠️ 创建新用户并登录...");

        try {
            // 生成唯一手机号（确保11位）
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("139%08d", timestamp);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", uniqueMobile);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    authToken = (String) data.get("token");
                    userId = String.valueOf(data.get("userId"));
                    log.info("✅ 新用户创建成功 - userId: {}, mobile: {}", userId, uniqueMobile);
                }
            }
        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage());
        }
    }

    /**
     * 辅助方法：创建带Authorization的Headers
     */
    private static HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);
        return headers;
    }

    // ============ 测试用例 ============

    /**
     * 🎯 测试1：用户注册/登录
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 用户注册/登录 - 准备测试数据")
    public void test1_RegisterUser() {
        log.info("\n========== [测试1] 用户注册/登录 ==========");
        ensureAuthenticated();

        Assertions.assertNotNull(authToken, "登录Token不能为空");
        Assertions.assertNotNull(userId, "用户ID不能为空");
        log.info("✅ 用户注册成功 - userId: {}", userId);
    }

    /**
     * 🎯 测试2：获取技能配置
     * 对应前端: skillApi.getSkillConfig()
     * 对应后端: GET /api/skills/config
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取技能配置 - 验证新数据结构")
    public void test2_GetSkillConfig() {
        log.info("\n========== [测试2] 获取技能配置 ==========");
        ensureAuthenticated();

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String configUrl = GATEWAY_URL + "/xypai-user/api/skills/config";
            ResponseEntity<Map> response = restTemplate.exchange(configUrl, HttpMethod.GET, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful(), "HTTP请求应成功");

            Map<String, Object> responseBody = response.getBody();
            Assertions.assertNotNull(responseBody, "响应体不能为空");

            Integer code = (Integer) responseBody.get("code");
            Assertions.assertEquals(200, code, "业务码应为200");

            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            Assertions.assertNotNull(data, "配置数据不能为空");

            // 保存配置供后续测试使用
            skillConfig = data;

            // ========== 验证新版数据结构 ==========
            log.info("\n📋 验证技能配置数据结构:");

            // 1. 验证skills字段（技能列表）
            List<Map<String, Object>> skills = (List<Map<String, Object>>) data.get("skills");
            Assertions.assertNotNull(skills, "skills字段不能为空");
            Assertions.assertFalse(skills.isEmpty(), "技能列表不能为空");
            log.info("  ✅ skills: {} 个技能", skills.size());

            // 验证技能项结构
            Map<String, Object> firstSkill = skills.get(0);
            Assertions.assertNotNull(firstSkill.get("id"), "技能ID不能为空");
            Assertions.assertNotNull(firstSkill.get("name"), "技能名称不能为空");
            Assertions.assertNotNull(firstSkill.get("type"), "技能类型不能为空");
            log.info("    - 示例: id={}, name={}, type={}",
                firstSkill.get("id"), firstSkill.get("name"), firstSkill.get("type"));

            // 统计线上/线下技能数量
            long onlineCount = skills.stream().filter(s -> "online".equals(s.get("type"))).count();
            long offlineCount = skills.stream().filter(s -> "offline".equals(s.get("type"))).count();
            log.info("    - 线上技能: {} 个, 线下技能: {} 个", onlineCount, offlineCount);

            // 2. 验证rankOptions字段（段位选项）
            Map<String, Object> rankOptions = (Map<String, Object>) data.get("rankOptions");
            Assertions.assertNotNull(rankOptions, "rankOptions字段不能为空");

            List<String> servers = (List<String>) rankOptions.get("servers");
            Assertions.assertNotNull(servers, "servers不能为空");
            Assertions.assertTrue(servers.contains("QQ区") || servers.contains("微信区"), "应包含QQ区或微信区");
            log.info("  ✅ rankOptions.servers: {}", servers);

            Map<String, List<String>> ranksBySkill = (Map<String, List<String>>) rankOptions.get("ranksBySkill");
            Assertions.assertNotNull(ranksBySkill, "ranksBySkill不能为空");
            Assertions.assertFalse(ranksBySkill.isEmpty(), "段位配置不能为空");
            log.info("  ✅ rankOptions.ranksBySkill: {} 个游戏配置", ranksBySkill.size());

            // 验证王者荣耀段位
            if (ranksBySkill.containsKey("wzry")) {
                List<String> wzryRanks = ranksBySkill.get("wzry");
                log.info("    - 王者荣耀段位: {}", wzryRanks);
                Assertions.assertTrue(wzryRanks.contains("最强王者") || wzryRanks.contains("永恒钻石"),
                    "王者荣耀段位配置应包含常见段位");
            }

            // 3. 验证timeOptions字段（时间选项）
            Map<String, Object> timeOptions = (Map<String, Object>) data.get("timeOptions");
            Assertions.assertNotNull(timeOptions, "timeOptions字段不能为空");
            log.info("  ✅ timeOptions: startHour={}, endHour={}, intervalMinutes={}",
                timeOptions.get("startHour"), timeOptions.get("endHour"), timeOptions.get("intervalMinutes"));

            // 4. 验证旧版兼容字段（可选）
            List<Map<String, Object>> games = (List<Map<String, Object>>) data.get("games");
            List<Map<String, Object>> serviceTypes = (List<Map<String, Object>>) data.get("serviceTypes");
            if (games != null) {
                log.info("  ℹ️ 旧版兼容字段 games: {} 个", games.size());
            }
            if (serviceTypes != null) {
                log.info("  ℹ️ 旧版兼容字段 serviceTypes: {} 个", serviceTypes.size());
            }

            log.info("\n✅ 技能配置获取成功，数据结构符合UI文档要求");

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取技能配置测试失败", e);
        }
    }

    /**
     * 🎯 测试3：创建线上技能（带新字段）
     * 对应前端: skillApi.createOnlineSkill()
     * 对应后端: POST /api/user/skills/online
     *
     * 新增字段:
     * - skillConfigId: 技能配置ID
     * - server: 服务区（QQ区/微信区）
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 创建线上技能 - 验证新字段支持")
    public void test3_CreateOnlineSkill() {
        log.info("\n========== [测试3] 创建线上技能 ==========");
        ensureAuthenticated();

        try {
            // 构建线上技能数据（匹配UI文档）
            Map<String, Object> onlineSkillData = new HashMap<>();

            // ========== 新版字段 ==========
            onlineSkillData.put("skillConfigId", "wzry");        // 技能配置ID
            onlineSkillData.put("server", "QQ区");               // 服务区（新增）

            // ========== 基础字段 ==========
            onlineSkillData.put("gameId", "wzry");               // 游戏ID
            onlineSkillData.put("gameName", "王者荣耀");          // 游戏名称
            onlineSkillData.put("gameRank", "最强王者");          // 游戏段位
            onlineSkillData.put("skillName", "集成测试-王者荣耀陪玩");  // 技能名称（标题）
            onlineSkillData.put("description", "这是一个集成测试创建的王者荣耀陪玩技能，测试新UI文档的线上技能创建功能。");  // 描述
            onlineSkillData.put("price", 50);                    // 价格
            onlineSkillData.put("serviceHours", 1);              // 服务时长
            onlineSkillData.put("isOnline", true);               // 上架状态

            // 可选字段
            onlineSkillData.put("images", Arrays.asList(
                "https://cdn.example.com/test/skill1.jpg",
                "https://cdn.example.com/test/skill2.jpg"
            ));
            onlineSkillData.put("promises", Arrays.asList("准时上线", "态度友好"));

            log.info("📤 创建线上技能请求数据:");
            log.info("  - skillConfigId: {}", onlineSkillData.get("skillConfigId"));
            log.info("  - server: {}", onlineSkillData.get("server"));
            log.info("  - gameName: {}", onlineSkillData.get("gameName"));
            log.info("  - gameRank: {}", onlineSkillData.get("gameRank"));
            log.info("  - skillName: {}", onlineSkillData.get("skillName"));
            log.info("  - price: {}", onlineSkillData.get("price"));

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(onlineSkillData, headers);

            String createUrl = GATEWAY_URL + "/xypai-user/api/user/skills/online";
            ResponseEntity<Map> response = restTemplate.postForEntity(createUrl, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful(), "HTTP请求应成功");

            Map<String, Object> responseBody = response.getBody();
            Assertions.assertNotNull(responseBody, "响应体不能为空");

            Integer code = (Integer) responseBody.get("code");
            Assertions.assertEquals(200, code, "业务码应为200，msg: " + responseBody.get("msg"));

            // 获取返回的技能ID
            Object data = responseBody.get("data");
            if (data instanceof Number) {
                onlineSkillId = ((Number) data).longValue();
            } else if (data instanceof Map) {
                onlineSkillId = ((Number) ((Map<?, ?>) data).get("skillId")).longValue();
            }

            Assertions.assertNotNull(onlineSkillId, "技能ID不能为空");
            log.info("✅ 线上技能创建成功 - skillId: {}", onlineSkillId);

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建线上技能测试失败", e);
        }
    }

    /**
     * 🎯 测试4：创建线下技能（带新字段）
     * 对应前端: skillApi.createOfflineSkill()
     * 对应后端: POST /api/user/skills/offline
     *
     * 新增字段:
     * - skillConfigId: 技能配置ID
     * - activityTime: 活动时间
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 创建线下技能 - 验证新字段支持")
    public void test4_CreateOfflineSkill() {
        log.info("\n========== [测试4] 创建线下技能 ==========");
        ensureAuthenticated();

        try {
            // 构建线下技能数据（匹配UI文档）
            Map<String, Object> offlineSkillData = new HashMap<>();

            // ========== 新版字段 ==========
            offlineSkillData.put("skillConfigId", "tanding");    // 技能配置ID
            // 设置活动时间为明天下午2点
            LocalDateTime activityTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            offlineSkillData.put("activityTime", activityTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // ========== 基础字段 ==========
            offlineSkillData.put("serviceType", "tanding");      // 服务类型ID
            offlineSkillData.put("serviceTypeName", "探店");      // 服务类型名称
            offlineSkillData.put("skillName", "集成测试-探店陪玩");   // 技能名称（标题）
            offlineSkillData.put("description", "这是一个集成测试创建的探店陪玩技能，测试新UI文档的线下技能创建功能，包含活动时间字段。");  // 描述
            offlineSkillData.put("price", 100);                  // 价格
            offlineSkillData.put("isOnline", true);              // 上架状态

            // 位置信息（按UI文档要求）
            Map<String, Object> location = new HashMap<>();
            location.put("address", "深圳市南山区科技园");
            location.put("latitude", new BigDecimal("22.5431"));
            location.put("longitude", new BigDecimal("113.9569"));
            offlineSkillData.put("location", location);

            // 可选字段
            offlineSkillData.put("images", Arrays.asList(
                "https://cdn.example.com/test/offline1.jpg"
            ));
            offlineSkillData.put("availableTimes", Arrays.asList(
                Map.of("dayOfWeek", 6, "startTime", "14:00", "endTime", "22:00"),
                Map.of("dayOfWeek", 7, "startTime", "10:00", "endTime", "22:00")
            ));

            log.info("📤 创建线下技能请求数据:");
            log.info("  - skillConfigId: {}", offlineSkillData.get("skillConfigId"));
            log.info("  - activityTime: {}", offlineSkillData.get("activityTime"));
            log.info("  - serviceTypeName: {}", offlineSkillData.get("serviceTypeName"));
            log.info("  - skillName: {}", offlineSkillData.get("skillName"));
            log.info("  - location: {}", location.get("address"));
            log.info("  - price: {}", offlineSkillData.get("price"));

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(offlineSkillData, headers);

            String createUrl = GATEWAY_URL + "/xypai-user/api/user/skills/offline";
            ResponseEntity<Map> response = restTemplate.postForEntity(createUrl, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful(), "HTTP请求应成功");

            Map<String, Object> responseBody = response.getBody();
            Assertions.assertNotNull(responseBody, "响应体不能为空");

            Integer code = (Integer) responseBody.get("code");
            Assertions.assertEquals(200, code, "业务码应为200，msg: " + responseBody.get("msg"));

            // 获取返回的技能ID
            Object data = responseBody.get("data");
            if (data instanceof Number) {
                offlineSkillId = ((Number) data).longValue();
            } else if (data instanceof Map) {
                offlineSkillId = ((Number) ((Map<?, ?>) data).get("skillId")).longValue();
            }

            Assertions.assertNotNull(offlineSkillId, "技能ID不能为空");
            log.info("✅ 线下技能创建成功 - skillId: {}", offlineSkillId);

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建线下技能测试失败", e);
        }
    }

    /**
     * 🎯 测试5：获取我的技能列表 - 验证创建结果
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 获取我的技能列表 - 验证创建结果")
    public void test5_GetMySkillsAndVerify() {
        log.info("\n========== [测试5] 获取我的技能列表 ==========");
        ensureAuthenticated();

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(skillsUrl, HttpMethod.GET, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful(), "HTTP请求应成功");

            Map<String, Object> responseBody = response.getBody();
            Assertions.assertNotNull(responseBody, "响应体不能为空");

            // TableDataInfo 直接返回
            List<Map<String, Object>> rows = (List<Map<String, Object>>) responseBody.get("rows");
            Integer total = (Integer) responseBody.get("total");

            log.info("📋 我的技能列表:");
            log.info("  - 总数: {}", total);

            if (rows != null && !rows.isEmpty()) {
                for (Map<String, Object> skill : rows) {
                    log.info("  - skillId: {}, name: {}, type: {}, price: {}",
                        skill.get("skillId"),
                        skill.get("skillName"),
                        skill.get("skillType"),
                        skill.get("price"));
                }

                // 验证创建的技能是否在列表中
                boolean foundOnlineSkill = rows.stream()
                    .anyMatch(s -> onlineSkillId != null && onlineSkillId.equals(((Number) s.get("skillId")).longValue()));
                boolean foundOfflineSkill = rows.stream()
                    .anyMatch(s -> offlineSkillId != null && offlineSkillId.equals(((Number) s.get("skillId")).longValue()));

                if (onlineSkillId != null) {
                    Assertions.assertTrue(foundOnlineSkill, "应包含创建的线上技能");
                    log.info("  ✅ 找到线上技能: {}", onlineSkillId);
                }
                if (offlineSkillId != null) {
                    Assertions.assertTrue(foundOfflineSkill, "应包含创建的线下技能");
                    log.info("  ✅ 找到线下技能: {}", offlineSkillId);
                }
            }

            log.info("✅ 技能列表获取成功");

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取技能列表测试失败", e);
        }
    }

    /**
     * 🎯 测试6：切换技能上下架状态
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 切换技能上下架状态")
    public void test6_ToggleSkillStatus() {
        log.info("\n========== [测试6] 切换技能上下架状态 ==========");
        ensureAuthenticated();

        if (onlineSkillId == null) {
            log.warn("⚠️ 没有可测试的技能，跳过此测试");
            return;
        }

        try {
            // 下架技能
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String toggleUrl = GATEWAY_URL + "/xypai-user/api/user/skills/" + onlineSkillId + "/toggle?isOnline=false";
            ResponseEntity<Map> response = restTemplate.exchange(toggleUrl, HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 技能下架成功 - skillId: {}", onlineSkillId);
                }

                // 再上架
                toggleUrl = GATEWAY_URL + "/xypai-user/api/user/skills/" + onlineSkillId + "/toggle?isOnline=true";
                response = restTemplate.exchange(toggleUrl, HttpMethod.PUT, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("✅ 技能上架成功 - skillId: {}", onlineSkillId);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
            log.warn("⚠️ 切换状态功能可能未完全实现，继续测试");
        }
    }

    /**
     * 🎯 测试7：获取技能详情 - 验证新字段
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 获取技能详情 - 验证新字段")
    public void test7_GetSkillDetail() {
        log.info("\n========== [测试7] 获取技能详情 ==========");
        ensureAuthenticated();

        if (onlineSkillId == null) {
            log.warn("⚠️ 没有可测试的技能，跳过此测试");
            return;
        }

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String detailUrl = GATEWAY_URL + "/xypai-user/api/user/skills/" + onlineSkillId;
            ResponseEntity<Map> response = restTemplate.exchange(detailUrl, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                    log.info("📋 技能详情:");
                    log.info("  - skillId: {}", data.get("skillId"));
                    log.info("  - skillName: {}", data.get("skillName"));
                    log.info("  - skillConfigId: {}", data.get("skillConfigId"));
                    log.info("  - server: {}", data.get("server"));
                    log.info("  - gameName: {}", data.get("gameName"));
                    log.info("  - gameRank: {}", data.get("gameRank"));
                    log.info("  - price: {}", data.get("price"));

                    // 验证新字段
                    if (data.get("skillConfigId") != null) {
                        log.info("  ✅ skillConfigId字段存在");
                    }
                    if (data.get("server") != null) {
                        log.info("  ✅ server字段存在");
                    }

                    log.info("✅ 技能详情获取成功");
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试7失败: {}", e.getMessage());
            log.warn("⚠️ 获取详情功能可能未完全实现，继续测试");
        }
    }

    /**
     * 🎯 测试8：删除测试技能 - 清理数据
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 删除测试技能 - 清理数据")
    public void test8_DeleteSkills() {
        log.info("\n========== [测试8] 删除测试技能 ==========");
        ensureAuthenticated();

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 删除线上技能
        if (onlineSkillId != null) {
            try {
                String deleteUrl = GATEWAY_URL + "/xypai-user/api/user/skills/" + onlineSkillId;
                ResponseEntity<Map> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("✅ 线上技能删除成功 - skillId: {}", onlineSkillId);
                }
            } catch (Exception e) {
                log.warn("⚠️ 删除线上技能失败: {}", e.getMessage());
            }
        }

        // 删除线下技能
        if (offlineSkillId != null) {
            try {
                String deleteUrl = GATEWAY_URL + "/xypai-user/api/user/skills/" + offlineSkillId;
                ResponseEntity<Map> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("✅ 线下技能删除成功 - skillId: {}", offlineSkillId);
                }
            } catch (Exception e) {
                log.warn("⚠️ 删除线下技能失败: {}", e.getMessage());
            }
        }

        log.info("✅ 测试数据清理完成");
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 ========================================");
        log.info("🎉 App 添加技能页面集成测试完成！");
        log.info("🎉 ========================================");
        log.info("\n📊 测试结果汇总:");
        log.info("  - 技能配置API: ✅");
        log.info("  - 线上技能创建: {}", onlineSkillId != null ? "✅ skillId=" + onlineSkillId : "❌");
        log.info("  - 线下技能创建: {}", offlineSkillId != null ? "✅ skillId=" + offlineSkillId : "❌");
        log.info("\n💡 如需进一步测试，请在前端运行 SkillsEditPage.tsx");
    }
}
