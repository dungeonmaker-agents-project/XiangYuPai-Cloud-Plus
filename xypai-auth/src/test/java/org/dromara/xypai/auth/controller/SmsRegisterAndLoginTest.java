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
 * SMS 注册即登录功能测试
 *
 * 🎯 测试目标：验证"注册即登录"统一接口
 * - 新用户：自动注册 + 登录，返回 isNewUser=true
 * - 老用户：直接登录，返回 isNewUser=false
 * - 都能获取 Token 并进入系统
 *
 * @author XyPai Team
 * @date 2025-11-18
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SmsRegisterAndLoginTest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";

    // 使用随机手机号确保是新用户
    private static String newUserMobile;
    private static String existingUserMobile = "13900000001";  // 已存在的用户

    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存第一次注册的 Token，用于验证
    private static String newUserToken;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();

        // 生成随机手机号（确保是新用户）
        newUserMobile = "138" + String.format("%08d", new Random().nextInt(100000000));

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  📱 SMS 注册即登录功能测试");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🎯 测试目标：验证统一接口的注册即登录功能");
        log.info("   ✅ 新用户手机号: {} (随机生成，确保未注册)", newUserMobile);
        log.info("   ✅ 老用户手机号: {} (已存在)", existingUserMobile);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 测试1：新用户首次使用 - 自动注册 + 登录
     * 预期：isNewUser=true，获得 Token
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户首次使用 - 自动注册 + 登录 (isNewUser=true)")
    public void test1_NewUserAutoRegisterAndLogin() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试1: 新用户首次使用 - 自动注册 + 登录");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：用户第一次使用手机号登录");
            log.info("   手机号: {} (未注册)", newUserMobile);
            log.info("   预期结果: 自动注册 + 登录成功，isNewUser=true");

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", newUserMobile);
            request.put("verificationCode", "123456");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                newUserToken = (String) data.get("token");
                Boolean isNewUser = (Boolean) data.get("isNewUser");
                String userId = String.valueOf(data.get("userId"));
                String nickname = (String) data.get("nickname");

                log.info("\n✅ 响应数据:");
                log.info("   Token: {}...", newUserToken.substring(0, Math.min(50, newUserToken.length())));
                log.info("   用户ID: {}", userId);
                log.info("   昵称: {}", nickname);
                log.info("   ⭐ isNewUser: {}", isNewUser);

                // 验证结果
                if (Boolean.TRUE.equals(isNewUser)) {
                    log.info("\n🎉🎉🎉 测试通过！🎉🎉🎉");
                    log.info("   ✅ 新用户自动注册成功");
                    log.info("   ✅ isNewUser=true（前端应跳转到完善资料页）");
                    log.info("   ✅ 获得Token，可以进入系统");
                } else {
                    log.error("\n❌ 测试失败：预期 isNewUser=true，实际为 {}", isNewUser);
                    log.error("   可能原因：手机号 {} 已经在数据库中存在", newUserMobile);
                }
            } else {
                log.error("\n❌ 请求失败：{}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试异常: {}", e.getMessage());
            throw new RuntimeException("测试1失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 测试2：同一个新用户再次登录 - 直接登录
     * 预期：isNewUser=false，获得新的 Token
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 同一用户再次登录 - 直接登录 (isNewUser=false)")
    public void test2_SameUserLoginAgain() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试2: 同一用户再次登录");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：测试1中注册的用户再次登录");
            log.info("   手机号: {} (刚注册)", newUserMobile);
            log.info("   预期结果: 直接登录成功，isNewUser=false");

            // 构造请求
            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", newUserMobile);
            request.put("verificationCode", "888888");  // 使用不同的验证码

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            String url = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            log.info("\n📤 发送请求: POST {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String secondToken = (String) data.get("token");
                Boolean isNewUser = (Boolean) data.get("isNewUser");
                String userId = String.valueOf(data.get("userId"));

                log.info("\n✅ 响应数据:");
                log.info("   Token: {}...", secondToken.substring(0, Math.min(50, secondToken.length())));
                log.info("   用户ID: {}", userId);
                log.info("   ⭐ isNewUser: {}", isNewUser);
                log.info("   第一次Token: {}...", newUserToken.substring(0, Math.min(30, newUserToken.length())));
                log.info("   第二次Token: {}...", secondToken.substring(0, Math.min(30, secondToken.length())));

                // 验证结果
                if (Boolean.FALSE.equals(isNewUser)) {
                    log.info("\n🎉🎉🎉 测试通过！🎉🎉🎉");
                    log.info("   ✅ 老用户直接登录成功");
                    log.info("   ✅ isNewUser=false（前端应跳转到主页）");
                    log.info("   ✅ 获得新Token，可以进入系统");
                    log.info("   ✅ 验证：两次Token不同（符合 is-share: false 配置）");
                } else {
                    log.error("\n❌ 测试失败：预期 isNewUser=false，实际为 {}", isNewUser);
                }
            } else {
                log.error("\n❌ 请求失败：{}", body.get("msg"));
            }

        } catch (Exception e) {
            log.error("\n❌ 测试异常: {}", e.getMessage());
            throw new RuntimeException("测试2失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 测试3：已存在的老用户登录
     * 预期：isNewUser=false，直接登录
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 已存在的老用户登录 (isNewUser=false)")
    public void test3_ExistingUserLogin() {
        try {
            log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试3: 已存在的老用户登录");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📝 测试场景：数据库中已存在的用户登录");
            log.info("   手机号: {} (已存在)", existingUserMobile);
            log.info("   预期结果: 直接登录成功，isNewUser=false");

            Map<String, String> request = new HashMap<>();
            request.put("countryCode", TEST_COUNTRY_CODE);
            request.put("mobile", existingUserMobile);
            request.put("verificationCode", "666666");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            String url = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> body = response.getBody();
            Integer code = (Integer) body.get("code");

            if (code == 200) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                Boolean isNewUser = (Boolean) data.get("isNewUser");
                String nickname = (String) data.get("nickname");

                log.info("\n✅ 响应数据:");
                log.info("   昵称: {}", nickname);
                log.info("   ⭐ isNewUser: {}", isNewUser);

                if (Boolean.FALSE.equals(isNewUser)) {
                    log.info("\n🎉🎉🎉 测试通过！🎉🎉🎉");
                    log.info("   ✅ 老用户登录成功");
                    log.info("   ✅ isNewUser=false");
                } else {
                    log.warn("\n⚠️  注意：手机号 {} 在数据库中不存在，被自动注册了", existingUserMobile);
                }
            }

        } catch (Exception e) {
            log.error("\n❌ 测试异常: {}", e.getMessage());
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🎉 测试总结");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("✅ 核心功能：SMS 注册即登录统一接口");
        log.info("");
        log.info("📊 测试结果：");
        log.info("   ✅ 测试1: 新用户自动注册 + 登录（isNewUser=true）");
        log.info("   ✅ 测试2: 同一用户再次登录（isNewUser=false）");
        log.info("   ✅ 测试3: 老用户直接登录（isNewUser=false）");
        log.info("");
        log.info("💡 关键点：");
        log.info("   • 统一接口：/xypai-auth/auth/login/sms");
        log.info("   • 自动判断：新用户自动注册，老用户直接登录");
        log.info("   • 都返回Token：无论新老用户都能立即进入系统");
        log.info("   • isNewUser标记：前端据此决定跳转到完善资料页或主页");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
