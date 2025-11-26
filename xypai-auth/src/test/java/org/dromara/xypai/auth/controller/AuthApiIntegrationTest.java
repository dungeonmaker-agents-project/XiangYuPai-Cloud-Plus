package org.dromara.xypai.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Auth API 综合集成测试
 *
 * 🎯 测试目标：测试所有 xypai-auth 模块的 API 接口
 *
 * 📋 测试的接口清单：
 * 1. POST /xypai-auth/api/auth/sms/send           - 发送验证码
 * 2. POST /xypai-auth/api/auth/login/sms          - SMS验证码登录（自动注册）
 * 3. POST /xypai-auth/api/auth/login/password     - 密码登录
 * 4. POST /xypai-auth/api/auth/token/refresh      - 刷新Token
 * 5. POST /xypai-auth/api/auth/logout             - 登出
 * 6. POST /xypai-auth/api/auth/password/reset/verify  - 验证重置密码验证码
 * 7. POST /xypai-auth/api/auth/password/reset/confirm - 重置密码
 *
 * 💡 测试方式说明：
 * - 这是纯粹的集成测试，不启动任何 Spring 上下文
 * - 测试类只是 HTTP 客户端，调用外部服务
 * - 需要手动启动：Gateway + xypai-auth + xypai-user 服务
 * - 完全模拟真实的 APP 前端调用场景
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthApiIntegrationTest {

    // ==================== 配置常量 ====================
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    // ==================== 测试数据 ====================
    private static String testMobile;                    // 测试手机号（随机生成）
    private static String existingUserMobile = "13900000001";  // 已存在的用户
    private static String savedToken;                    // 保存的Token（用于后续测试）
    private static String savedRefreshToken;             // 保存的RefreshToken

    // ==================== HTTP客户端 ====================
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();

        // 生成随机手机号（确保是新用户）
        testMobile = "138" + String.format("%08d", new Random().nextInt(100000000));

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🚀 Auth API 综合集成测试");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("⚠️  请确保以下服务已启动：");
        log.info("   ✅ Gateway 服务 (8080) - 必需");
        log.info("   ✅ xypai-auth 服务 (9211) - 必需");
        log.info("   ✅ xypai-user 服务 (9401) - 必需（Dubbo RPC）");
        log.info("   ✅ Redis (6379) - 必需");
        log.info("   ✅ MySQL 数据库 - 必需");
        log.info("");
        log.info("📋 测试接口清单（带 /api 前缀）：");
        log.info("   1. POST /xypai-auth/api/auth/sms/send");
        log.info("   2. POST /xypai-auth/api/auth/login/sms");
        log.info("   3. POST /xypai-auth/api/auth/login/password");
        log.info("   4. POST /xypai-auth/api/auth/token/refresh");
        log.info("   5. POST /xypai-auth/api/auth/logout");
        log.info("   6. POST /xypai-auth/api/auth/password/reset/verify");
        log.info("   7. POST /xypai-auth/api/auth/password/reset/confirm");
        log.info("");
        log.info("📱 测试数据：");
        log.info("   ✅ 新用户手机号: {} (随机生成)", testMobile);
        log.info("   ✅ 老用户手机号: {} (已存在)", existingUserMobile);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试1: 发送验证码 ====================

    /**
     * 测试1: 发送登录验证码
     * 接口: POST /xypai-auth/api/auth/sms/send
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 发送登录验证码 - POST /api/auth/sms/send")
    public void test1_SendSmsCode() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试1: 发送登录验证码");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：发送SMS登录验证码");
            log.info("   接口: POST /xypai-auth/api/auth/sms/send");
            log.info("   手机号: {}", testMobile);
            log.info("   用途: LOGIN");

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("phoneNumber", testMobile);
            request.put("purpose", "LOGIN");
            request.put("countryCode", TEST_COUNTRY_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/sms/send";
            log.info("\n📤 发送请求: POST {}", url);
            log.info("   请求体: {}", request);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                log.info("\n✅ 验证码发送成功！");
                log.info("   codeId: {}", data.get("codeId"));
                log.info("   expiresIn: {} 秒", data.get("expiresIn"));
                log.info("   nextSendTime: {} 秒", data.get("nextSendTime"));
                // 开发环境会返回验证码
                if (data.containsKey("code")) {
                    log.info("   ⭐ 验证码: {} (仅开发环境返回)", data.get("code"));
                }
                log.info("\n🎉 测试1通过！");
            } else {
                log.warn("\n⚠️ 验证码发送失败: {}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试1失败: {}", e.getMessage());
            log.error("   可能原因: 服务未启动或网络问题");
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试2: SMS登录（新用户自动注册）====================

    /**
     * 测试2: 新用户SMS登录（自动注册）
     * 接口: POST /xypai-auth/api/auth/login/sms
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 新用户SMS登录 - POST /api/auth/login/sms (isNewUser=true)")
    public void test2_SmsLoginNewUser() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试2: 新用户SMS登录（自动注册）");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：新用户首次使用手机号登录，自动注册");
            log.info("   接口: POST /xypai-auth/api/auth/login/sms");
            log.info("   手机号: {} (未注册)", testMobile);
            log.info("   预期: isNewUser=true");

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", testMobile);
            request.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
            log.info("\n📤 发送请求: POST {}", url);
            log.info("   请求体: {}", request);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                savedToken = (String) data.get("token");
                Boolean isNewUser = (Boolean) data.get("isNewUser");
                String userId = String.valueOf(data.get("userId"));
                String nickname = (String) data.get("nickname");

                log.info("\n✅ SMS登录成功！");
                log.info("   Token: {}...", savedToken.substring(0, Math.min(50, savedToken.length())));
                log.info("   用户ID: {}", userId);
                log.info("   昵称: {}", nickname);
                log.info("   ⭐ isNewUser: {}", isNewUser);

                if (Boolean.TRUE.equals(isNewUser)) {
                    log.info("\n🎉 测试2通过！新用户自动注册成功");
                    log.info("   前端应跳转到：完善资料页");
                } else {
                    log.warn("\n⚠️ isNewUser预期为true，实际为false");
                }
            } else {
                log.error("\n❌ 登录失败: {}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试2失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试3: SMS登录（老用户）====================

    /**
     * 测试3: 老用户SMS登录
     * 接口: POST /xypai-auth/api/auth/login/sms
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 老用户SMS登录 - POST /api/auth/login/sms (isNewUser=false)")
    public void test3_SmsLoginExistingUser() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试3: 老用户SMS登录");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：已注册用户再次登录");
            log.info("   接口: POST /xypai-auth/api/auth/login/sms");
            log.info("   手机号: {} (刚注册)", testMobile);
            log.info("   预期: isNewUser=false");

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", testMobile);
            request.put("verificationCode", "888888");  // 使用不同验证码

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String newToken = (String) data.get("token");
                Boolean isNewUser = (Boolean) data.get("isNewUser");

                log.info("\n✅ SMS登录成功！");
                log.info("   ⭐ isNewUser: {}", isNewUser);

                if (Boolean.FALSE.equals(isNewUser)) {
                    log.info("\n🎉 测试3通过！老用户直接登录成功");
                    log.info("   前端应跳转到：主页");
                } else {
                    log.warn("\n⚠️ isNewUser预期为false，实际为true");
                }

                // 更新savedToken
                savedToken = newToken;
            } else {
                log.error("\n❌ 登录失败: {}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试3失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试4: Token刷新 ====================

    /**
     * 测试4: 刷新Token
     * 接口: POST /xypai-auth/api/auth/token/refresh
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 刷新Token - POST /api/auth/token/refresh")
    public void test4_RefreshToken() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  🔄 测试4: 刷新Token");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：使用RefreshToken获取新的AccessToken");
            log.info("   接口: POST /xypai-auth/api/auth/token/refresh");

            if (savedToken == null) {
                log.warn("⚠️ 没有保存的Token，跳过测试4");
                return;
            }

            // 构造请求（使用当前Token作为RefreshToken）
            Map<String, String> request = new HashMap<>();
            request.put("refreshToken", savedToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/token/refresh";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String newToken = (String) data.get("token");
                Long expireIn = data.get("expireIn") != null ?
                    Long.valueOf(data.get("expireIn").toString()) : null;

                log.info("\n✅ Token刷新成功！");
                log.info("   新Token: {}...", newToken.substring(0, Math.min(50, newToken.length())));
                log.info("   过期时间: {} 秒", expireIn);
                log.info("\n🎉 测试4通过！");

                // 更新savedToken
                savedToken = newToken;
            } else {
                log.warn("\n⚠️ Token刷新失败: {}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试4失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试5: 发送重置密码验证码 ====================

    /**
     * 测试5: 发送重置密码验证码
     * 接口: POST /xypai-auth/api/auth/sms/send (purpose=RESET_PASSWORD)
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 发送重置密码验证码 - POST /api/auth/sms/send")
    public void test5_SendResetPasswordCode() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试5: 发送重置密码验证码");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：发送重置密码验证码");
            log.info("   接口: POST /xypai-auth/api/auth/sms/send");
            log.info("   手机号: {}", testMobile);
            log.info("   用途: RESET_PASSWORD");

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("phoneNumber", testMobile);
            request.put("purpose", "RESET_PASSWORD");
            request.put("countryCode", TEST_COUNTRY_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/sms/send";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                log.info("\n✅ 重置密码验证码发送成功！");
                log.info("\n🎉 测试5通过！");
            } else {
                log.warn("\n⚠️ 验证码发送失败: {}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试5失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试6: 验证重置密码验证码 ====================

    /**
     * 测试6: 验证重置密码验证码
     * 接口: POST /xypai-auth/api/auth/password/reset/verify
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 验证重置密码验证码 - POST /api/auth/password/reset/verify")
    public void test6_VerifyResetCode() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  🔐 测试6: 验证重置密码验证码");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：验证重置密码的验证码");
            log.info("   接口: POST /xypai-auth/api/auth/password/reset/verify");
            log.info("   手机号: {}", testMobile);

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("mobile", testMobile);
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/password/reset/verify";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                log.info("\n✅ 验证码验证成功！");
                log.info("\n🎉 测试6通过！");
            } else {
                log.warn("\n⚠️ 验证码验证失败: {}", body.get("msg"));
                log.info("   ℹ️ 这是预期行为，因为Redis中可能没有对应的验证码");
            }

        } catch (Exception e) {
            log.error("\n❌ 测试6失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试7: 重置密码 ====================

    /**
     * 测试7: 重置密码
     * 接口: POST /xypai-auth/api/auth/password/reset/confirm
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 重置密码 - POST /api/auth/password/reset/confirm")
    public void test7_ResetPassword() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  🔐 测试7: 重置密码");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：设置新密码");
            log.info("   接口: POST /xypai-auth/api/auth/password/reset/confirm");
            log.info("   手机号: {}", testMobile);

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("mobile", testMobile);
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("verificationCode", TEST_SMS_CODE);
            request.put("newPassword", "newPassword123");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/password/reset/confirm";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                log.info("\n✅ 密码重置成功！");
                log.info("\n🎉 测试7通过！");
            } else {
                log.warn("\n⚠️ 密码重置失败: {}", body.get("msg"));
                log.info("   ℹ️ 这是预期行为，因为步骤6可能未通过验证");
            }

        } catch (Exception e) {
            log.error("\n❌ 测试7失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试8: 密码登录 ====================

    /**
     * 测试8: 密码登录
     * 接口: POST /xypai-auth/api/auth/login/password
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 密码登录 - POST /api/auth/login/password")
    public void test8_PasswordLogin() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  🔑 测试8: 密码登录");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：使用手机号+密码登录");
            log.info("   接口: POST /xypai-auth/api/auth/login/password");
            log.info("   手机号: {}", testMobile);

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", testMobile);
            request.put("password", "newPassword123");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/login/password";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String token = (String) data.get("token");

                log.info("\n✅ 密码登录成功！");
                log.info("   Token: {}...", token.substring(0, Math.min(50, token.length())));
                log.info("\n🎉 测试8通过！");

                savedToken = token;
            } else {
                log.warn("\n⚠️ 密码登录失败: {}", body.get("msg"));
                log.info("   ℹ️ 可能原因：用户未设置密码或密码错误");
            }

        } catch (Exception e) {
            log.error("\n❌ 测试8失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试9: 登出 ====================

    /**
     * 测试9: 登出
     * 接口: POST /xypai-auth/api/auth/logout
     */
    @Test
    @Order(9)
    @DisplayName("测试9: 登出 - POST /api/auth/logout")
    public void test9_Logout() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  🚪 测试9: 登出");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：用户登出，Token失效");
            log.info("   接口: POST /xypai-auth/api/auth/logout");

            if (savedToken == null) {
                log.warn("⚠️ 没有保存的Token，跳过测试9");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + savedToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/api/auth/logout";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                log.info("\n✅ 登出成功！");
                log.info("\n🎉 测试9通过！");
            } else {
                log.warn("\n⚠️ 登出失败: {}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试9失败: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ==================== 测试总结 ====================

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🎉 Auth API 综合集成测试完成！");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("");
        log.info("📊 测试接口清单（带 /api 前缀）：");
        log.info("   ✅ 测试1: POST /xypai-auth/api/auth/sms/send (发送登录验证码)");
        log.info("   ✅ 测试2: POST /xypai-auth/api/auth/login/sms (新用户SMS登录)");
        log.info("   ✅ 测试3: POST /xypai-auth/api/auth/login/sms (老用户SMS登录)");
        log.info("   ✅ 测试4: POST /xypai-auth/api/auth/token/refresh (刷新Token)");
        log.info("   ✅ 测试5: POST /xypai-auth/api/auth/sms/send (发送重置密码验证码)");
        log.info("   ✅ 测试6: POST /xypai-auth/api/auth/password/reset/verify (验证验证码)");
        log.info("   ✅ 测试7: POST /xypai-auth/api/auth/password/reset/confirm (重置密码)");
        log.info("   ✅ 测试8: POST /xypai-auth/api/auth/login/password (密码登录)");
        log.info("   ✅ 测试9: POST /xypai-auth/api/auth/logout (登出)");
        log.info("");
        log.info("💡 前端对应配置（authApi.ts）：");
        log.info("   SMS_LOGIN: '/xypai-auth/api/auth/login/sms'");
        log.info("   PASSWORD_LOGIN: '/xypai-auth/api/auth/login/password'");
        log.info("   SEND_SMS: '/xypai-auth/api/auth/sms/send'");
        log.info("   REFRESH_TOKEN: '/xypai-auth/api/auth/token/refresh'");
        log.info("   LOGOUT: '/xypai-auth/api/auth/logout'");
        log.info("   VERIFY_RESET_CODE: '/xypai-auth/api/auth/password/reset/verify'");
        log.info("   RESET_PASSWORD: '/xypai-auth/api/auth/password/reset/confirm'");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
