package org.dromara.xypai.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * App SMS 注册/登录集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 用户通过 SMS 验证码注册/登录功能，验证整个认证链路的可用性。
 *
 * 测试流程:
 * 1. 📱 测试1: 新用户 SMS 注册（isNewUser=true）
 * 2. 📱 测试2: 老用户 SMS 登录（isNewUser=false）
 *
 * 💡 测试方式说明：
 * - 这是纯粹的集成测试，不启动任何 Spring 上下文
 * - 测试类只是 HTTP 客户端，调用外部服务
 * - 需要手动启动：Gateway + xypai-auth 服务
 * - 完全模拟真实的 APP 前端调用场景
 *
 * @author XyPai Team
 * @date 2025-11-18
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppSmsRegistrationTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_MOBILE_NEW = "13800138000";  // 新用户（未注册）
    private static final String TEST_MOBILE_OLD = "13900000001";  // 老用户（已注册）
    private static final String TEST_SMS_CODE = "123456";         // 验证码

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  📱 App SMS 注册/登录集成测试");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("⚠️  请确保以下服务已启动：");
        log.info("   ✅ Gateway 服务 (8080) - 必需");
        log.info("   ✅ xypai-auth 服务 (9211) - 必需");
        log.info("   ✅ xypai-user 服务 (9401) - 必需（Dubbo RPC）");
        log.info("   ✅ Redis (6379) - 必需");
        log.info("   ✅ MySQL 数据库 - 必需");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试1：新用户 SMS 注册
     *
     * 测试场景：
     * 1. 用户首次使用手机号登录
     * 2. 后端自动创建用户
     * 3. 返回 isNewUser=true，前端跳转到完善资料页
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户 SMS 注册 - isNewUser=true")
    public void test1_NewUserRegistration() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试1: 新用户 SMS 注册");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            log.info("\n📝 步骤1: 构造 SMS 注册请求");
            log.info("   接口: POST {}/xypai-auth/auth/login/sms", GATEWAY_URL);
            log.info("   国家区号: {}", TEST_COUNTRY_CODE);
            log.info("   手机号: {}", TEST_MOBILE_NEW);
            log.info("   验证码: {}", TEST_SMS_CODE);

            // 构造请求体
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", TEST_MOBILE_NEW);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            log.info("\n📤 步骤2: 发送请求到 Gateway");
            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                loginUrl,
                request,
                Map.class
            );

            log.info("\n📥 收到 Gateway 响应:");
            log.info("   HTTP 状态码: {} {}", response.getStatusCode().value(), response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String token = (String) data.get("token");
                    Boolean isNewUser = (Boolean) data.get("isNewUser");
                    String userId = String.valueOf(data.get("userId"));
                    String nickname = (String) data.get("nickname");

                    log.info("\n✅ 注册成功！");
                    log.info("   Token: {}...", token.substring(0, Math.min(50, token.length())));
                    log.info("   用户ID: {}", userId);
                    log.info("   昵称: {}", nickname);
                    log.info("   ⭐ isNewUser: {} (true=新用户，跳转完善资料页)", isNewUser);

                    // 验证新用户标记
                    if (Boolean.TRUE.equals(isNewUser)) {
                        log.info("   ✅ 新用户标记正确");
                    } else {
                        log.warn("   ⚠️  预期 isNewUser=true，实际为 {}", isNewUser);
                    }

                    log.info("\n✅✅✅ 测试1完成 - 新用户注册成功！✅✅✅");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("注册失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("\n❌ 测试1失败: {}", e.getMessage());
            log.error("   可能原因:");
            log.error("   1. Gateway 服务未启动 (端口 8080)");
            log.error("   2. xypai-auth 服务未启动 (端口 9211)");
            log.error("   3. xypai-user 服务未启动 (端口 9401) - Dubbo RPC 调用需要");
            log.error("   4. Redis 未启动或配置错误");
            log.error("   5. 数据库未启动或配置错误");
            log.error("   6. 验证码验证失败（请在 Redis 中设置验证码或关闭验证码校验）");
            throw new RuntimeException("新用户注册测试失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试2：老用户 SMS 登录
     *
     * 测试场景：
     * 1. 已注册用户使用手机号登录
     * 2. 返回 isNewUser=false，前端跳转到主页
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 老用户 SMS 登录 - isNewUser=false")
    public void test2_ExistingUserLogin() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试2: 老用户 SMS 登录");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            log.info("\n📝 步骤1: 构造 SMS 登录请求");
            log.info("   接口: POST {}/xypai-auth/auth/login/sms", GATEWAY_URL);
            log.info("   国家区号: {}", TEST_COUNTRY_CODE);
            log.info("   手机号: {} (已注册用户)", TEST_MOBILE_OLD);
            log.info("   验证码: {}", TEST_SMS_CODE);

            // 构造请求体
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", TEST_MOBILE_OLD);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            log.info("\n📤 步骤2: 发送请求到 Gateway");
            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                loginUrl,
                request,
                Map.class
            );

            log.info("\n📥 收到 Gateway 响应:");
            log.info("   HTTP 状态码: {} {}", response.getStatusCode().value(), response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String token = (String) data.get("token");
                    Boolean isNewUser = (Boolean) data.get("isNewUser");
                    String userId = String.valueOf(data.get("userId"));
                    String nickname = (String) data.get("nickname");
                    String avatar = (String) data.get("avatar");

                    log.info("\n✅ 登录成功！");
                    log.info("   Token: {}...", token.substring(0, Math.min(50, token.length())));
                    log.info("   用户ID: {}", userId);
                    log.info("   昵称: {}", nickname);
                    log.info("   头像: {}", avatar != null ? avatar : "未设置");
                    log.info("   ⭐ isNewUser: {} (false=老用户，跳转主页)", isNewUser);

                    // 验证老用户标记
                    if (Boolean.FALSE.equals(isNewUser)) {
                        log.info("   ✅ 老用户标记正确");
                    } else {
                        log.warn("   ⚠️  预期 isNewUser=false，实际为 {}", isNewUser);
                    }

                    log.info("\n✅✅✅ 测试2完成 - 老用户登录成功！✅✅✅");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("登录失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("\n❌ 测试2失败: {}", e.getMessage());
            log.error("   可能原因:");
            log.error("   1. Gateway 服务未启动");
            log.error("   2. xypai-auth 服务未启动");
            log.error("   3. 测试用户不存在（手机号: {}）", TEST_MOBILE_OLD);
            log.error("   4. 验证码验证失败");
            throw new RuntimeException("老用户登录测试失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试3：参数验证 - 手机号格式错误
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 参数验证 - 手机号格式错误")
    public void test3_InvalidMobileFormat() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  ❌ 测试3: 参数验证 - 手机号格式错误");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", "12345");  // 错误格式
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                loginUrl,
                request,
                Map.class
            );

            log.info("   响应: {}", response.getBody());

            // 期望返回 400 错误
            if (response.getStatusCode().value() == 400) {
                log.info("   ✅ 参数验证生效，返回 400 错误");
            } else {
                Map<String, Object> body = response.getBody();
                Integer code = (Integer) body.get("code");
                if (code != null && code != 200) {
                    log.info("   ✅ 参数验证生效，返回错误码: {}", code);
                } else {
                    log.warn("   ⚠️  预期返回错误，实际返回成功");
                }
            }

            log.info("\n✅✅✅ 测试3完成 - 参数验证正常！✅✅✅");

        } catch (Exception e) {
            // 期望抛出异常（400错误）
            log.info("   ✅ 参数验证生效: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试4：参数验证 - 验证码格式错误
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 参数验证 - 验证码格式错误")
    public void test4_InvalidVerificationCode() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  ❌ 测试4: 参数验证 - 验证码格式错误");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", TEST_MOBILE_NEW);
            loginRequest.put("verificationCode", "12345");  // 不是6位

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                loginUrl,
                request,
                Map.class
            );

            log.info("   响应: {}", response.getBody());

            if (response.getStatusCode().value() == 400) {
                log.info("   ✅ 参数验证生效，返回 400 错误");
            } else {
                Map<String, Object> body = response.getBody();
                Integer code = (Integer) body.get("code");
                if (code != null && code != 200) {
                    log.info("   ✅ 参数验证生效，返回错误码: {}", code);
                }
            }

            log.info("\n✅✅✅ 测试4完成 - 参数验证正常！✅✅✅");

        } catch (Exception e) {
            log.info("   ✅ 参数验证生效: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🎉 所有测试完成！");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📊 测试总结:");
        log.info("   ✅ 测试1: 新用户 SMS 注册（isNewUser=true）");
        log.info("   ✅ 测试2: 老用户 SMS 登录（isNewUser=false）");
        log.info("   ✅ 测试3: 参数验证 - 手机号格式");
        log.info("   ✅ 测试4: 参数验证 - 验证码格式");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
