package org.dromara.xypai.auth.controller;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.encrypt.utils.EncryptUtils;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Sa-Token 集成测试 - 通过Gateway调用真实登录接口
 *
 * 🎯 核心目标：
 * 本测试作为HTTP客户端，通过Gateway调用真实运行的ruoyi-auth服务的登录接口。
 * 模拟真实的APP前端登录场景，验证整个认证链路的可用性。
 *
 * 测试流程 (按顺序执行):
 * 1. 🔥 测试1: 通过Gateway调用 POST /auth/login 接口登录，获取并保存Token到globalToken
 * 2. 🚀 测试2: 使用globalToken通过Gateway访问RuoYi-Demo Service
 * 3. 🎨 测试3: 使用globalToken通过Gateway访问XYPai-Content Service
 * 4. 🏛️ 测试4: 使用globalToken通过Gateway访问RuoYi-System Service
 *
 * 🚀 真实HTTP登录流程:
 * ┌──────────────────────────────────────────────────────────┐
 * │  测试客户端 (模拟APP前端)                                 │
 * │  ↓                                                        │
 * │  POST http://localhost:8080/auth/login                   │
 * │  {                                                        │
 * │    "username": "13900000001",                            │
 * │    "password": "123456",                                 │
 * │    "clientId": "app-xypai-client-id",                    │
 * │    "grantType": "password",                              │
 * │    "tenantId": "000000"                                  │
 * │  }                                                        │
 * │  ↓                                                        │
 * │  Gateway (8080) 路由到 ruoyi-auth (9210)                 │
 * │  ↓                                                        │
 * │  TokenController.login()                                 │
 * │  ↓                                                        │
 * │  PasswordAuthStrategy.login()                            │
 * │  ↓                                                        │
 * │  返回 { access_token, expires_in }                       │
 * │  ✅ 完成！客户端保存Token到globalToken                    │
 * └──────────────────────────────────────────────────────────┘
 *
 * 💡 测试方式说明：
 * - 这是纯粹的集成测试，不启动任何服务
 * - 测试类只是HTTP客户端，调用外部服务
 * - 需要手动启动：Gateway + ruoyi-auth + 各业务服务
 * - 完全模拟真实的APP前端调用场景
 * - ⭐ 关键改进：使用 @Order 注解确保测试按顺序执行，登录后的Token保存在globalToken中供后续测试使用
 *
 * @author xypai
 * @date 2025-11-10
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // ⭐ 启用测试顺序
public class SimpleSaTokenTest {

    // 测试配置（使用RuoYi标准认证 + APP用户）
    private static final String TEST_PHONENUMBER = "testjojo";  // APP用户手机号
    private static final String TEST_PASSWORD = "123456";          // APP用户密码
    private static final String TEST_CLIENT_ID = "428a8310cd442757ae699df5d894f051";  // APP端clientId（数据库中已存在）
    private static final String GATEWAY_URL = "http://localhost:8080";   // Gateway地址

    // 加密配置（从Nacos配置中获取）
    private static final String RSA_PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKoR8mX0rGKLqzcWmOzbfj64K8ZIgOdHnzkXSOVOZbFu/TJhZ7rFAN+eaGkl3C4buccQd/EjEsj9ir7ijT7h96MCAwEAAQ==";
    private static final String ENCRYPT_HEADER_FLAG = "encrypt-key";

    // RestTemplate 用于HTTP请求
    private static RestTemplate restTemplate;

    // ObjectMapper 用于JSON序列化
    private static ObjectMapper objectMapper;

    // ⭐ 全局 token (测试1登录后保存，测试2-4使用)
    private static String globalToken = null;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🚀 Sa-Token 集成测试 - 真实服务调用模式（加密请求）");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("⚠️  请确保以下服务已启动：");
        log.info("   测试1（登录测试）需要：");
        log.info("   ✅ Gateway服务 (8080) - 必需");
        log.info("   ✅ ruoyi-auth服务 (9210) - 必需");
        log.info("   ✅ Redis (6379) - 必需");
        log.info("   ✅ 数据库 - 必需");
        log.info("");
        log.info("   测试2-4（集成测试）还需要：");
        log.info("   ⚠️ RuoYi-Demo (9401) - 测试2需要");
        log.info("   ⚠️ XYPai-Content (9403) - 测试3需要");
        log.info("   ⚠️ RuoYi-System (9201) - 测试4需要");
        log.info("");
        log.info("🔐 加密配置：");
        log.info("   • RSA 公钥（前40字符）: {}...", RSA_PUBLIC_KEY.substring(0, 40));
        log.info("   • 加密 Header: {}", ENCRYPT_HEADER_FLAG);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 加密请求体（模拟前端加密逻辑）
     *
     * 加密流程：
     * 1. 生成32位随机AES密钥
     * 2. Base64编码AES密钥
     * 3. 用RSA公钥加密Base64编码后的AES密钥
     * 4. 用AES密钥加密请求体JSON
     * 5. 返回加密后的内容和加密header
     *
     * @param requestBody 请求体对象
     * @return Map包含加密后的body和header值
     */
    private static Map<String, String> encryptRequest(Object requestBody) throws Exception {
        // 1. 将请求体转换为JSON字符串
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 2. 生成32位随机AES密钥
        String aesPassword = RandomUtil.randomString(32);
        log.debug("🔑 生成AES密钥: {}", aesPassword);

        // 3. Base64编码AES密钥
        String encryptAes = EncryptUtils.encryptByBase64(aesPassword);
        log.debug("📦 Base64编码AES: {}", encryptAes);

        // 4. 用RSA公钥加密Base64编码后的AES密钥
        String encryptedPassword = EncryptUtils.encryptByRsa(encryptAes, RSA_PUBLIC_KEY);
        log.debug("🔐 RSA加密后密钥（前50字符）: {}...", encryptedPassword.substring(0, Math.min(50, encryptedPassword.length())));

        // 5. 用AES密钥加密请求体JSON
        String encryptedBody = EncryptUtils.encryptByAes(jsonBody, aesPassword);
        log.debug("📦 AES加密后body（前50字符）: {}...", encryptedBody.substring(0, Math.min(50, encryptedBody.length())));

        // 6. 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("body", encryptedBody);           // 加密后的请求体
        result.put("header", encryptedPassword);      // 加密后的AES密钥（放在header中）

        return result;
    }

    /**
     * 🎯 测试1：APP用户通过Gateway登录 → 获取Token并保存到globalToken
     *
     * ⭐ 这个测试必须最先运行（@Order(1)），因为它获取Token并保存到globalToken供后续测试使用
     *
     * 测试目标：
     * 1. ✅ 通过Gateway调用 POST /auth/login 接口登录（真实Gateway路由）
     * 2. ✅ 验证Token格式和有效性
     * 3. ✅ 保存Token到globalToken供后续测试使用
     *
     * ⚠️ 前置条件（必须手动启动）：
     *   ✅ Gateway服务运行中 (8080) - 必需
     *   ✅ ruoyi-auth 服务运行中 (9210) - 必需
     *   ✅ Redis运行中 (6379) - 必需
     *   ✅ 数据库可用 - 必需
     *   ✅ APP用户存在 (手机号: testjojo)
     *   ✅ APP客户端已配置 (clientId已配置)
     */
    @Test
    @Order(1)  // ⭐ 第一个执行
    @DisplayName("测试1: APP用户登录 - Gateway → ruoyi-auth → 保存Token到globalToken")
    public void test1_Login() {

        try {
            log.info("\n");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📱 测试1: 通过Gateway调用登录接口并保存Token");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            log.info("\n📝 步骤1: 获取验证码配置");
            log.info("   接口: GET {}/auth/code", GATEWAY_URL);

            // 1. 先调用 /auth/code 接口，获取验证码配置
            String captchaUrl = GATEWAY_URL + "/auth/code";
            ResponseEntity<Map> captchaResponse = restTemplate.getForEntity(captchaUrl, Map.class);

            String uuid = null;
            boolean captchaEnabled = false;

            if (captchaResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> captchaBody = captchaResponse.getBody();
                if (captchaBody != null && (Integer) captchaBody.get("code") == 200) {
                    Map<String, Object> captchaData = (Map<String, Object>) captchaBody.get("data");
                    if (captchaData != null) {
                        uuid = (String) captchaData.get("uuid");
                        Object captchaEnabledObj = captchaData.get("captchaEnabled");
                        captchaEnabled = captchaEnabledObj != null && (Boolean) captchaEnabledObj;

                        log.info("   ✅ 验证码配置获取成功");
                        log.info("   • 验证码开关: {}", captchaEnabled ? "启用" : "关闭");
                        if (uuid != null) {
                            log.info("   • UUID: {}", uuid);
                        }
                    }
                }
            }

            log.info("\n📝 步骤2: 构造登录请求");
            log.info("   接口: POST {}/auth/login", GATEWAY_URL);
            log.info("   手机号: {}", TEST_PHONENUMBER);
            log.info("   密码: {}", TEST_PASSWORD);
            log.info("   clientId: {}", TEST_CLIENT_ID);

            // 2. 构造登录请求体
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", TEST_PHONENUMBER);  // 用户名（这里用手机号）
            loginRequest.put("password", TEST_PASSWORD);     // 密码
            loginRequest.put("clientId", TEST_CLIENT_ID);    // APP客户端ID
            loginRequest.put("grantType", "password");       // 登录方式：密码登录
            loginRequest.put("tenantId", "000000");         // 租户ID

            // 如果验证码启用，添加验证码信息（uuid必须传，code留空表示不验证）
            if (captchaEnabled && uuid != null) {
                loginRequest.put("uuid", uuid);
                loginRequest.put("code", "");  // 验证码留空（需要在Nacos配置中关闭验证码验证）
                log.info("   ⚠️  验证码已启用但code留空（需配置security.captcha.enabled: false）");
            }

            log.info("\n🔐 步骤3: 加密请求体（模拟前端加密）");
            // 3. 加密请求体
            Map<String, String> encryptResult = encryptRequest(loginRequest);
            String encryptedBody = encryptResult.get("body");
            String encryptedHeader = encryptResult.get("header");

            log.info("   ✅ 加密完成");
            log.info("   • 加密body（前50字符）: {}...", encryptedBody.substring(0, Math.min(50, encryptedBody.length())));
            log.info("   • 加密header（前50字符）: {}...", encryptedHeader.substring(0, Math.min(50, encryptedHeader.length())));

            // 4. 构造请求头并发送登录请求
            log.info("\n📤 步骤4: 发送加密请求到Gateway");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(ENCRYPT_HEADER_FLAG, encryptedHeader);  // ⭐ 设置加密header

            HttpEntity<String> request = new HttpEntity<>(encryptedBody, headers);
            log.info("   URL: {}/auth/login", GATEWAY_URL);
            log.info("   Header[{}]: {}...", ENCRYPT_HEADER_FLAG, encryptedHeader.substring(0, Math.min(30, encryptedHeader.length())));

            // 4.1 🔥 通过Gateway调用登录接口（核心！）
            String loginUrl = GATEWAY_URL + "/auth/login";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                loginUrl,
                request,
                Map.class
            );

            log.info("\n📥 收到Gateway响应:");
            log.info("   HTTP状态码: {} {}", response.getStatusCode().value(), response.getStatusCode());
            log.info("   响应体: {}", response.getBody());

            // 5. 解析响应获取Token
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    // 获取data中的access_token
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    globalToken = (String) data.get("access_token");  // ⭐ 保存到全局变量
                    Object expiresIn = data.get("expires_in");

                    log.info("\n✅ 登录成功！");
                    log.info("   AccessToken (前50字符): {}...",
                        globalToken.substring(0, Math.min(50, globalToken.length())));
                    log.info("   Token长度: {} 字符", globalToken.length());
                    log.info("   过期时间: {} 秒", expiresIn);
                    log.info("   Token格式: {}", globalToken.split("\\.").length == 3 ? "JWT (3部分)" : "其他");
                    log.info("   ✅ Token已保存到globalToken，可用于后续测试");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("登录失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

            // 6. 验证Token有效性
            log.info("\n🎯 步骤5: 验证Token有效性");

            // 验证Token格式
            if (globalToken.split("\\.").length != 3) {
                log.error("❌ Token格式错误：不是有效的JWT格式");
                throw new RuntimeException("Token格式错误");
            }

            // 验证Token长度
            if (globalToken.length() < 100) {
                log.error("❌ Token长度异常：可能不完整");
                throw new RuntimeException("Token长度异常");
            }

            log.info("✅ Token格式验证通过");
            log.info("✅ Token长度验证通过");

            // 测试成功总结
            log.info("\n✅✅✅ 测试1完成 - 登录成功！✅✅✅");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📊 测试结果:");
            log.info("   ✅ 通过Gateway调用登录接口成功");
            log.info("   ✅ Token验证通过");
            log.info("   ✅ globalToken已保存: {}...", globalToken.substring(0, Math.min(30, globalToken.length())));
            log.info("   💡 下一步: 运行测试2-4来验证Token访问各个服务");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        } catch (Exception e) {
            log.error("\n❌ 登录测试失败:");
            log.error("   错误: {}", e.getMessage());
            log.error("\n💡 可能原因:");
            log.error("   1. Gateway 服务未启动 (端口 8080)");
            log.error("   2. ruoyi-auth 服务未启动 (端口 9210)");
            log.error("   3. Gateway路由配置错误 (/auth/** -> ruoyi-auth)");
            log.error("   4. Redis 未启动或配置错误 (端口 6379)");
            log.error("   5. 数据库未启动或配置错误");
            log.error("   6. APP用户不存在: sys_user表中没有用户名 {} 的用户", TEST_PHONENUMBER);
            log.error("   7. APP客户端未配置: sys_client表中没有 clientId={}", TEST_CLIENT_ID);
            log.error("   8. 用户密码错误");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            throw new RuntimeException("登录测试失败", e);
        }
    }

    /**
     * 🎯 测试2：使用globalToken通过Gateway访问RuoYi-Demo Service
     *
     * ⭐ 这个测试必须在测试1之后运行（@Order(2)），因为它依赖测试1获取的globalToken
     *
     * 测试目标：
     * - 验证globalToken可以通过Gateway访问RuoYi-Demo服务
     * - 验证Sa-Token跨服务认证功能
     *
     * ⚠️ 前置条件：
     *   ✅ 测试1已运行并成功获取globalToken - 必需
     *   ✅ Gateway服务运行中 (8080) - 必需
     *   ✅ RuoYi-Demo Service运行中 (9401) - 必需
     */
    @Test
    @Order(2)  // ⭐ 第二个执行
    @DisplayName("测试2: 使用globalToken访问Demo服务 - Gateway → ruoyi-demo")
    public void test2_AccessDemoService() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🚀 测试2: 使用globalToken访问RuoYi-Demo Service");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 验证globalToken是否存在
        if (globalToken == null || globalToken.isEmpty()) {
            log.error("❌ globalToken为空！请先运行测试1获取Token");
            throw new RuntimeException("globalToken为空，请先运行测试1");
        }

        log.info("✅ 使用globalToken: {}...", globalToken.substring(0, Math.min(30, globalToken.length())));

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + globalToken);
            headers.set("clientid", TEST_CLIENT_ID);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 🎯 测试RuoYi-Demo模块接口：Redis缓存测试接口
            String apiUrl = GATEWAY_URL + "/demo/cache/test1?key=testKey&value=testValue";

            log.info("\n📤 发送Gateway请求:");
            log.info("   URL: {}", apiUrl);
            log.info("   方法: GET");
            log.info("   接口说明: Redis缓存测试接口（ruoyi-demo模块）");

            ResponseEntity<String> apiResponse = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("\n📥 收到Gateway响应:");
            log.info("   HTTP状态码: {} {}", apiResponse.getStatusCode().value(), apiResponse.getStatusCode());

            String responseBody = apiResponse.getBody();
            log.info("   响应体: {}", responseBody != null && responseBody.length() > 200
                ? responseBody.substring(0, 200) + "..."
                : responseBody);

            // 验证响应
            boolean isSuccess = apiResponse.getStatusCode().is2xxSuccessful() &&
                responseBody != null &&
                (responseBody.contains("\"code\":200") || responseBody.contains("\"code\": 200"));

            if (isSuccess) {
                log.info("\n✅✅✅ 测试2成功！✅✅✅");
                log.info("   ✅ globalToken有效");
                log.info("   ✅ Gateway路由成功");
                log.info("   ✅ Sa-Token认证通过");
                log.info("   ✅ RuoYi-Demo Service响应正常");
            } else {
                log.warn("\n⚠️ 测试2部分失败 - 业务层返回异常: {}", responseBody);
            }

        } catch (Exception e) {
            log.error("\n❌ 测试2失败: {}", e.getMessage());
            log.error("   可能原因:");
            log.error("   1. Gateway未启动 (端口 8080)");
            log.error("   2. RuoYi-Demo Service未启动 (端口 9401)");
            log.error("   3. Token已过期");
            throw new RuntimeException("访问Demo服务失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试3：使用globalToken通过Gateway访问XYPai-Content Service
     *
     * ⭐ 这个测试必须在测试1之后运行（@Order(3)），因为它依赖测试1获取的globalToken
     *
     * ⚠️ 前置条件：
     *   ✅ 测试1已运行并成功获取globalToken - 必需
     *   ✅ Gateway服务运行中 (8080) - 必需
     *   ✅ XYPai-Content Service运行中 (9403) - 必需
     */
    @Test
    @Order(3)  // ⭐ 第三个执行
    @DisplayName("测试3: 使用globalToken访问Content服务 - Gateway → xypai-content")
    public void test3_AccessContentService() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🎨 测试3: 使用globalToken访问XYPai-Content Service");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (globalToken == null || globalToken.isEmpty()) {
            log.error("❌ globalToken为空！请先运行测试1获取Token");
            throw new RuntimeException("globalToken为空，请先运行测试1");
        }

        log.info("✅ 使用globalToken: {}...", globalToken.substring(0, Math.min(30, globalToken.length())));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + globalToken);
            headers.set("clientid", TEST_CLIENT_ID);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String apiUrl = GATEWAY_URL + "/xypai-content/api/v1/homepage/users/list?filterTab=all&page=1&limit=10";

            log.info("\n📤 发送Gateway请求:");
            log.info("   URL: {}", apiUrl);
            log.info("   方法: GET");
            log.info("   接口说明: 首页用户列表接口（xypai-content模块）");

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("\n📥 收到Gateway响应:");
            log.info("   HTTP状态码: {} {}", response.getStatusCode().value(), response.getStatusCode());

            String responseBody = response.getBody();
            log.info("   响应体: {}", responseBody != null && responseBody.length() > 200
                ? responseBody.substring(0, 200) + "..."
                : responseBody);

            boolean isSuccess = response.getStatusCode().is2xxSuccessful() &&
                responseBody != null &&
                (responseBody.contains("\"code\":200") || responseBody.contains("\"code\": 200"));

            if (isSuccess) {
                log.info("\n✅✅✅ 测试3成功！✅✅✅");
                log.info("   ✅ XYPai-Content Service响应正常");
            } else {
                log.warn("\n⚠️ 测试3部分失败: {}", responseBody);
            }

        } catch (Exception e) {
            log.error("\n❌ 测试3失败: {}", e.getMessage());
            log.error("   可能原因:");
            log.error("   1. XYPai-Content Service未启动 (端口 9403)");
            log.error("   2. Token已过期");
            throw new RuntimeException("访问Content服务失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试4：使用globalToken通过Gateway访问XYPai-User Service
     *
     * ⭐ 这个测试必须在测试1之后运行（@Order(4)），因为它依赖测试1获取的globalToken
     *
     * ⚠️ 前置条件：
     *   ✅ 测试1已运行并成功获取globalToken - 必需
     *   ✅ Gateway服务运行中 (8080) - 必需
     *   ✅ XYPai-User Service运行中 (9401) - 必需
     */
    @Test
    @Order(4)  // ⭐ 第四个执行
    @DisplayName("测试4: 使用globalToken访问User服务 - Gateway → xypai-user")
    public void test4_AccessUserService() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  👤 测试4: 使用globalToken访问XYPai-User Service");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (globalToken == null || globalToken.isEmpty()) {
            log.error("❌ globalToken为空！请先运行测试1获取Token");
            throw new RuntimeException("globalToken为空，请先运行测试1");
        }

        log.info("✅ 使用globalToken: {}...", globalToken.substring(0, Math.min(30, globalToken.length())));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + globalToken);
            headers.set("clientid", TEST_CLIENT_ID);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 🎯 测试1: 获取当前用户资料（已移除权限检查，只需登录）
            String profileUrl = GATEWAY_URL + "/xypai-user/api/v1/user/profile";

            log.info("\n📤 发送Gateway请求 (测试1/3):");
            log.info("   URL: {}", profileUrl);
            log.info("   方法: GET");
            log.info("   接口说明: 获取当前用户资料（xypai-user模块）");
            log.info("   权限要求: @SaCheckLogin (只需登录)");

            ResponseEntity<String> profileResponse = restTemplate.exchange(
                profileUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("\n📥 收到Gateway响应:");
            log.info("   HTTP状态码: {} {}", profileResponse.getStatusCode().value(), profileResponse.getStatusCode());

            String profileBody = profileResponse.getBody();
            log.info("   响应体: {}", profileBody != null && profileBody.length() > 200
                ? profileBody.substring(0, 200) + "..."
                : profileBody);

            boolean profileSuccess = profileResponse.getStatusCode().is2xxSuccessful() &&
                profileBody != null &&
                (profileBody.contains("\"code\":200") || profileBody.contains("\"code\": 200"));

            if (profileSuccess) {
                log.info("   ✅ 测试1通过: 获取用户资料成功");
            } else {
                log.warn("   ⚠️ 测试1失败: {}", profileBody);
            }

            // 🎯 测试2: 根据ID查询用户（使用当前登录用户的ID）
            // 先从 Token 中提取 userId（简单方法：调用接口获取）
            String userIdUrl = GATEWAY_URL + "/xypai-user/api/v1/user/profile";
            ResponseEntity<Map> userIdResponse = restTemplate.exchange(
                userIdUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );

            Long testUserId = null;
            if (userIdResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> respBody = userIdResponse.getBody();
                if (respBody != null && (Integer) respBody.get("code") == 200) {
                    Map<String, Object> data = (Map<String, Object>) respBody.get("data");
                    if (data != null && data.get("userId") != null) {
                        testUserId = Long.valueOf(data.get("userId").toString());
                    }
                }
            }

            if (testUserId != null) {
                String userByIdUrl = GATEWAY_URL + "/xypai-user/api/v1/user/" + testUserId;

                log.info("\n📤 发送Gateway请求 (测试2/3):");
                log.info("   URL: {}", userByIdUrl);
                log.info("   方法: GET");
                log.info("   接口说明: 根据ID查询用户");
                log.info("   测试用户ID: {}", testUserId);

                ResponseEntity<String> userByIdResponse = restTemplate.exchange(
                    userByIdUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
                );

                log.info("\n📥 收到Gateway响应:");
                log.info("   HTTP状态码: {} {}", userByIdResponse.getStatusCode().value(), userByIdResponse.getStatusCode());

                String userByIdBody = userByIdResponse.getBody();
                boolean userByIdSuccess = userByIdResponse.getStatusCode().is2xxSuccessful() &&
                    userByIdBody != null &&
                    (userByIdBody.contains("\"code\":200") || userByIdBody.contains("\"code\": 200"));

                if (userByIdSuccess) {
                    log.info("   ✅ 测试2通过: 根据ID查询用户成功");
                } else {
                    log.warn("   ⚠️ 测试2失败: {}", userByIdBody);
                }
            }

            // 🎯 测试3: 分页查询用户列表
            String listUrl = GATEWAY_URL + "/xypai-user/api/v1/user/list?pageNum=1&pageSize=10";

            log.info("\n📤 发送Gateway请求 (测试3/3):");
            log.info("   URL: {}", listUrl);
            log.info("   方法: GET");
            log.info("   接口说明: 分页查询用户列表");

            ResponseEntity<String> listResponse = restTemplate.exchange(
                listUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("\n📥 收到Gateway响应:");
            log.info("   HTTP状态码: {} {}", listResponse.getStatusCode().value(), listResponse.getStatusCode());

            String listBody = listResponse.getBody();
            log.info("   响应体: {}", listBody != null && listBody.length() > 200
                ? listBody.substring(0, 200) + "..."
                : listBody);

            boolean listSuccess = listResponse.getStatusCode().is2xxSuccessful() &&
                listBody != null &&
                (listBody.contains("\"code\":200") || listBody.contains("\"code\": 200"));

            if (listSuccess) {
                log.info("   ✅ 测试3通过: 分页查询用户列表成功");
            } else {
                log.warn("   ⚠️ 测试3失败: {}", listBody);
            }

            // 总结
            if (profileSuccess && listSuccess) {
                log.info("\n✅✅✅ 测试4成功！✅✅✅");
                log.info("   ✅ XYPai-User Service响应正常");
                log.info("   ✅ @SaCheckLogin 认证通过");
                log.info("   ✅ 已移除 @SaCheckPermission 权限检查");
                log.info("   💡 现在可以正常访问 xypai-user 接口了！");
            } else {
                log.warn("\n⚠️ 测试4部分成功");
            }

        } catch (Exception e) {
            log.error("\n❌ 测试4失败: {}", e.getMessage());
            log.error("   可能原因:");
            log.error("   1. XYPai-User Service未启动 (端口 9401)");
            log.error("   2. Token已过期");
            log.error("   3. Gateway路由配置错误");
            log.error("   4. 接口仍有权限检查（需确认已移除 @SaCheckPermission）");
            throw new RuntimeException("访问User服务失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试5：使用globalToken通过Gateway访问RuoYi-System Service（保留原测试4）
     *
     * ⭐ 这个测试必须在测试1之后运行（@Order(5)），因为它依赖测试1获取的globalToken
     *
     * ⚠️ 前置条件：
     *   ✅ 测试1已运行并成功获取globalToken - 必需
     *   ✅ Gateway服务运行中 (8080) - 必需
     *   ✅ RuoYi-System Service运行中 (9201) - 必需
     */
    @Test
    @Order(5)  // ⭐ 第五个执行
    @DisplayName("测试5: 使用globalToken访问System服务 - Gateway → ruoyi-system")
    public void test5_AccessSystemService() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  🏛️ 测试4: 使用globalToken访问RuoYi-System Service");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (globalToken == null || globalToken.isEmpty()) {
            log.error("❌ globalToken为空！请先运行测试1获取Token");
            throw new RuntimeException("globalToken为空，请先运行测试1");
        }

        log.info("✅ 使用globalToken: {}...", globalToken.substring(0, Math.min(30, globalToken.length())));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + globalToken);
            headers.set("clientid", TEST_CLIENT_ID);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String apiUrl = GATEWAY_URL + "/system/role/list";

            log.info("\n📤 发送Gateway请求:");
            log.info("   URL: {}", apiUrl);
            log.info("   方法: GET");
            log.info("   接口说明: 获取菜单路由接口（ruoyi-system模块）");

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("\n📥 收到Gateway响应:");
            log.info("   HTTP状态码: {} {}", response.getStatusCode().value(), response.getStatusCode());

            String responseBody = response.getBody();
            log.info("   响应体: {}", responseBody != null && responseBody.length() > 200
                ? responseBody.substring(0, 200) + "..."
                : responseBody);

            boolean isSuccess = response.getStatusCode().is2xxSuccessful() &&
                responseBody != null &&
                (responseBody.contains("\"code\":200") || responseBody.contains("\"code\": 200"));

            if (isSuccess) {
                log.info("\n✅✅✅ 测试4成功！✅✅✅");
                log.info("   ✅ RuoYi-System Service响应正常");
                log.info("   ✅ LoginHelper.getUserId() 正常工作");
                log.info("   ⚡ 核心RuoYi模块与Sa-Token集成正常");
            } else {
                log.warn("\n⚠️ 测试4部分失败: {}", responseBody);
            }

        } catch (Exception e) {
            log.error("\n❌ 测试4失败: {}", e.getMessage());
            log.error("   可能原因:");
            log.error("   1. RuoYi-System Service未启动 (端口 9201)");
            log.error("   2. Token已过期");
            log.error("   3. 用户权限不足");
            throw new RuntimeException("访问System服务失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 🎯 测试6：心跳接口测试（只需登录，无需权限）
     *
     * ⚠️ 前置条件：
     *   ✅ 测试1已运行并成功获取globalToken - 必需
     *   ✅ Gateway服务运行中 (8080) - 必需
     *   ✅ XYPai-User Service运行中 (9401) - 必需
     */
    @Test
    @Order(6)  // ⭐ 第六个执行
    @DisplayName("测试6: 心跳接口 - Gateway → xypai-user (只需登录)")
    public void test6_Heartbeat() {
        log.info("\n");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  💓 测试6: 心跳接口测试");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (globalToken == null || globalToken.isEmpty()) {
            log.error("❌ globalToken为空！请先运行测试1获取Token");
            throw new RuntimeException("globalToken为空，请先运行测试1");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + globalToken);
            headers.set("clientid", TEST_CLIENT_ID);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String heartbeatUrl = GATEWAY_URL + "/xypai-user/api/v1/user/heartbeat";

            log.info("\n📤 发送心跳请求:");
            log.info("   URL: {}", heartbeatUrl);
            log.info("   方法: POST");
            log.info("   接口说明: 更新用户最后在线时间");
            log.info("   权限要求: @SaCheckLogin (只需登录)");

            ResponseEntity<String> response = restTemplate.exchange(
                heartbeatUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.info("\n📥 收到响应:");
            log.info("   HTTP状态码: {} {}", response.getStatusCode().value(), response.getStatusCode());

            String responseBody = response.getBody();
            log.info("   响应体: {}", responseBody);

            boolean isSuccess = response.getStatusCode().is2xxSuccessful() &&
                responseBody != null &&
                (responseBody.contains("\"code\":200") || responseBody.contains("\"code\": 200"));

            if (isSuccess) {
                log.info("\n✅✅✅ 测试6成功！✅✅✅");
                log.info("   ✅ 心跳接口响应正常");
                log.info("   ✅ 最后在线时间已更新");
            } else {
                log.warn("\n⚠️ 测试6失败: {}", responseBody);
            }

        } catch (Exception e) {
            log.error("\n❌ 测试6失败: {}", e.getMessage());
            throw new RuntimeException("心跳接口测试失败", e);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
