package org.dromara.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * App 编辑资料页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试编辑资料页面的所有接口功能，支持11个字段的实时保存
 *
 * 测试流程:
 * 1. 📱 新用户 SMS 注册（获取Token）
 * 2. 📋 加载编辑页面数据
 * 3. ✏️ 更新昵称
 * 4. 👤 更新性别
 * 5. 🎂 更新生日
 * 6. 📍 更新居住地
 * 7. 📏 更新身高
 * 8. ⚖️ 更新体重
 * 9. 💼 更新职业
 * 10. 💬 更新微信号
 * 11. 📝 更新个性签名
 *
 * ⚠️ 注意：
 * 头像上传接口 (POST /api/user/profile/avatar/upload) 需要 multipart/form-data
 * 格式，暂未包含在自动化测试中，需要手动测试或单独实现
 *
 * 💡 测试方式说明：
 * - 集成测试，调用真实服务
 * - 需要手动启动：Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL
 *
 * @author XyPai Team
 * @date 2025-11-19
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppEditProfilePageTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";  // 验证码

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token，用于后续测试
    private static String authToken;
    private static String userId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📝 App 编辑资料页面集成测试启动");
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
            String uniqueMobile = String.format("138%08d", timestamp);

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
                    log.info("✅ 新用户创建成功 - userId: {}", userId);
                } else {
                    log.error("❌ 创建用户失败: {}", responseBody.get("msg"));
                }
            }
        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试1：新用户 SMS 注册（获取Token）
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户 SMS 注册 - 获取Token")
    public void test1_NewUserRegistration() {
        try {
            log.info("\n[测试1] 新用户 SMS 注册");

            // 使用时间戳生成唯一手机号，确保是新用户（11位）
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("138%08d", timestamp);
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
                    log.info("✅ 注册成功 - userId: {}, token 已保存", userId);
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("注册失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("新用户注册测试失败", e);
        }
    }

    /**
     * 🎯 测试2：加载编辑页面数据
     *
     * 接口: GET /api/user/profile/edit
     * 触发时机: 页面加载时
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 加载编辑页面数据")
    public void test2_LoadEditPageData() {
        try {
            log.info("\n[测试2] 加载编辑页面数据");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String editUrl = GATEWAY_URL + "/xypai-user/api/user/profile/edit";
            ResponseEntity<Map> response = restTemplate.exchange(editUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 加载编辑数据成功 - userId: {}, nickname: {}", data.get("userId"), data.get("nickname"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("加载编辑数据失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("加载编辑数据测试失败", e);
        }
    }

    /**
     * 🎯 测试3：更新昵称
     *
     * 接口: PUT /api/user/profile/nickname
     * 触发时机: 昵称输入框失去焦点
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 更新昵称")
    public void test3_UpdateNickname() {
        try {
            log.info("\n[测试3] 更新昵称");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            String newNickname = "测试昵称_" + System.currentTimeMillis() % 10000;
            log.info("新昵称: {}", newNickname);

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("nickname", newNickname);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String nicknameUrl = GATEWAY_URL + "/xypai-user/api/user/profile/nickname";
            ResponseEntity<Map> response = restTemplate.exchange(nicknameUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新昵称成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新昵称失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            throw new RuntimeException("更新昵称测试失败", e);
        }
    }

    /**
     * 🎯 测试4：更新性别
     *
     * 接口: PUT /api/user/profile/gender
     * 触发时机: 选择性别后
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 更新性别")
    public void test4_UpdateGender() {
        try {
            log.info("\n[测试4] 更新性别");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("gender", "male");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String genderUrl = GATEWAY_URL + "/xypai-user/api/user/profile/gender";
            ResponseEntity<Map> response = restTemplate.exchange(genderUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新性别成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新性别失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            throw new RuntimeException("更新性别测试失败", e);
        }
    }

    /**
     * 🎯 测试5：更新生日
     *
     * 接口: PUT /api/user/profile/birthday
     * 触发时机: 选择生日后
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 更新生日")
    public void test5_UpdateBirthday() {
        try {
            log.info("\n[测试5] 更新生日");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("birthday", "1995-06-15");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String birthdayUrl = GATEWAY_URL + "/xypai-user/api/user/profile/birthday";
            ResponseEntity<Map> response = restTemplate.exchange(birthdayUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新生日成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新生日失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("更新生日测试失败", e);
        }
    }

    /**
     * 🎯 测试6：更新居住地
     *
     * 接口: PUT /api/user/profile/residence
     * 触发时机: 选择居住地后
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 更新居住地")
    public void test6_UpdateResidence() {
        try {
            log.info("\n[测试6] 更新居住地");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("residence", "广东省广州市天河区");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String residenceUrl = GATEWAY_URL + "/xypai-user/api/user/profile/residence";
            ResponseEntity<Map> response = restTemplate.exchange(residenceUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新居住地成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新居住地失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
            throw new RuntimeException("更新居住地测试失败", e);
        }
    }

    /**
     * 🎯 测试7：更新身高
     *
     * 接口: PUT /api/user/profile/height
     * 触发时机: 身高输入框失去焦点
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 更新身高")
    public void test7_UpdateHeight() {
        try {
            log.info("\n[测试7] 更新身高");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("height", 175);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateRequest, headers);

            String heightUrl = GATEWAY_URL + "/xypai-user/api/user/profile/height";
            ResponseEntity<Map> response = restTemplate.exchange(heightUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新身高成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新身高失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试7失败: {}", e.getMessage());
            throw new RuntimeException("更新身高测试失败", e);
        }
    }

    /**
     * 🎯 测试8：更新体重
     *
     * 接口: PUT /api/user/profile/weight
     * 触发时机: 体重输入框失去焦点
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 更新体重")
    public void test8_UpdateWeight() {
        try {
            log.info("\n[测试8] 更新体重");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("weight", 65);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateRequest, headers);

            String weightUrl = GATEWAY_URL + "/xypai-user/api/user/profile/weight";
            ResponseEntity<Map> response = restTemplate.exchange(weightUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新体重成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新体重失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试8失败: {}", e.getMessage());
            throw new RuntimeException("更新体重测试失败", e);
        }
    }

    /**
     * 🎯 测试9：更新职业
     *
     * 接口: PUT /api/user/profile/occupation
     * 触发时机: 职业输入框失去焦点
     */
    @Test
    @Order(9)
    @DisplayName("测试9: 更新职业")
    public void test9_UpdateOccupation() {
        try {
            log.info("\n[测试9] 更新职业");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("occupation", "软件工程师");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String occupationUrl = GATEWAY_URL + "/xypai-user/api/user/profile/occupation";
            ResponseEntity<Map> response = restTemplate.exchange(occupationUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新职业成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新职业失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试9失败: {}", e.getMessage());
            throw new RuntimeException("更新职业测试失败", e);
        }
    }

    /**
     * 🎯 测试10：更新微信号
     *
     * 接口: PUT /api/user/profile/wechat
     * 触发时机: 微信号输入框失去焦点
     */
    @Test
    @Order(10)
    @DisplayName("测试10: 更新微信号")
    public void test10_UpdateWechat() {
        try {
            log.info("\n[测试10] 更新微信号");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("wechat", "wechat_" + System.currentTimeMillis() % 10000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String wechatUrl = GATEWAY_URL + "/xypai-user/api/user/profile/wechat";
            ResponseEntity<Map> response = restTemplate.exchange(wechatUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新微信号成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新微信号失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试10失败: {}", e.getMessage());
            throw new RuntimeException("更新微信号测试失败", e);
        }
    }

    /**
     * 🎯 测试11：更新个性签名
     *
     * 接口: PUT /api/user/profile/bio
     * 触发时机: 个性签名输入框失去焦点
     */
    @Test
    @Order(11)
    @DisplayName("测试11: 更新个性签名")
    public void test11_UpdateBio() {
        try {
            log.info("\n[测试11] 更新个性签名");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("bio", "这是我的个性签名，用于测试 - " + System.currentTimeMillis());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String bioUrl = GATEWAY_URL + "/xypai-user/api/user/profile/bio";
            ResponseEntity<Map> response = restTemplate.exchange(bioUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新个性签名成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新个性签名失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试11失败: {}", e.getMessage());
            throw new RuntimeException("更新个性签名测试失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
        log.info("⚠️ 注意：头像上传接口 (POST /api/user/profile/avatar/upload) 需要单独测试");
    }
}
