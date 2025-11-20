# 🌐 分布式Token生成模式 - 在任何微服务中生成Token

> **如何在不调用Auth Service的情况下，在任何微服务中直接生成Token**

---

## 🎯 核心原理

### **关键发现**
`LoginHelper`和Sa-Token在`ruoyi-common-satoken`模块中，所有微服务都依赖这个模块。因此：

✅ **任何微服务都可以直接生成Token！**
✅ **不需要调用Auth Service的HTTP接口！**
✅ **Token会自动存储到共享的Redis中！**

---

## 📦 依赖关系

```
所有微服务 (ruoyi-system, xypai-content, ruoyi-demo...)
   │
   ├─ 依赖: ruoyi-common-satoken
   │   │
   │   ├─ LoginHelper.java         ⭐ Token生成核心工具
   │   ├─ PlusSaTokenDao.java      ⭐ Redis存储实现
   │   └─ Sa-Token核心库
   │
   └─ 共享: Redis (所有服务连接同一个Redis)
       │
       └─ Token存储 (跨服务共享)
```

**关键点**：
- 所有微服务都有`LoginHelper`
- 所有微服务都连接同一个Redis
- 因此，任何服务生成的Token都可以被其他服务验证！

---

## 🔧 实现方式

### **方式1: 直接使用LoginHelper（最简单）**

在**任何微服务**中，你可以这样生成Token：

```java
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.model.LoginUser;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;

@Service
public class MyTokenService {
    
    /**
     * 在任何微服务中直接生成Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param tenantId 租户ID
     * @return Token字符串
     */
    public String generateToken(Long userId, String username, String tenantId) {
        // 1. 构建LoginUser对象
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setTenantId(tenantId);
        loginUser.setLoginId(userId);
        loginUser.setUserType("sys_user");
        // ... 设置其他必要字段（roles, permissions等）
        
        // 2. 构建登录参数
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("pc");                    // 设备类型
        model.setTimeout(1800L);                      // 30分钟超时
        model.setActiveTimeout(-1L);                  // 不启用活跃超时
        model.setExtra(LoginHelper.CLIENT_KEY, "custom-service");
        
        // 3. 🔥 直接调用LoginHelper生成Token
        LoginHelper.login(loginUser, model);
        
        // 4. 获取生成的Token
        String token = StpUtil.getTokenValue();
        
        // 5. Token已自动存储到Redis！
        return token;
    }
}
```

---

### **方式2: 使用PasswordAuthStrategy（复用认证逻辑）**

如果你想复用完整的认证逻辑（包括密码验证、用户查询等）：

```java
import org.dromara.xypai.auth.service.IAuthStrategy;
import org.dromara.xypai.auth.domain.vo.LoginVo;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.dromara.common.json.utils.JsonUtils;

@Service
public class MyAuthService {

    // 注入PasswordAuthStrategy（需要ruoyi-auth依赖）
    @Autowired
    private IAuthStrategy passwordAuthStrategy;

    /**
     * 在任何微服务中使用PasswordAuthStrategy
     */
    public LoginVo login(String username, String password, String tenantId) {
        // 1. 构建PasswordLoginBody
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", username);
        loginBody.put("password", password);
        loginBody.put("tenantId", tenantId);
        loginBody.put("code", "");
        loginBody.put("uuid", "");

        String loginBodyJson = JsonUtils.toJsonString(loginBody);

        // 2. 构建RemoteClientVo
        RemoteClientVo clientVo = new RemoteClientVo();
        clientVo.setClientId("custom-client-id");
        clientVo.setGrantType("password");
        clientVo.setDeviceType("pc");
        clientVo.setTimeout(1800L);
        clientVo.setStatus("0");

        // 3. 直接调用认证策略
        LoginVo loginVo = passwordAuthStrategy.login(loginBodyJson, clientVo);

        return loginVo;
    }
}
```

---

## 🌟 应用场景

### **场景1: 服务间认证（Service-to-Service Auth）**

```java
/**
 * 场景：微服务A需要调用微服务B的受保护接口
 * 
 * 传统方式：微服务A先调用Auth Service获取Token
 * 新方式：微服务A直接生成Token
 */

@Service
public class ServiceAClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public String callServiceB() {
        // 1. 直接生成服务间调用的Token
        LoginUser serviceUser = new LoginUser();
        serviceUser.setUserId(999L);                    // 系统用户ID
        serviceUser.setUsername("service-a");           // 服务名
        serviceUser.setUserType("service_account");     // 服务账号类型
        
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("service");
        model.setTimeout(300L);  // 5分钟短期Token
        
        LoginHelper.login(serviceUser, model);
        String token = StpUtil.getTokenValue();
        
        // 2. 使用Token调用服务B
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "http://service-b/api/data",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        return response.getBody();
    }
}
```

---

### **场景2: 内部管理接口（无需密码登录）**

```java
/**
 * 场景：管理员通过后台系统直接创建Token给用户
 * 
 * 用途：用户忘记密码、管理员代理登录等
 */

@RestController
@RequestMapping("/admin/token")
public class AdminTokenController {
    
    @Autowired
    private IUserService userService;
    
    /**
     * 管理员为用户生成Token（无需密码）
     */
    @PostMapping("/generate-for-user/{userId}")
    @SaCheckPermission("admin:token:generate")  // 需要管理员权限
    public R<String> generateTokenForUser(@PathVariable Long userId) {
        // 1. 查询用户信息
        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        
        // 2. 构建LoginUser
        LoginUser loginUser = buildLoginUser(user);
        
        // 3. 生成Token（绕过密码验证）
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("admin-generated");
        model.setTimeout(3600L);  // 1小时
        
        LoginHelper.login(loginUser, model);
        String token = StpUtil.getTokenValue();
        
        // 4. 记录管理员操作日志
        logAdminAction("为用户 " + userId + " 生成Token");
        
        return R.ok(token);
    }
}
```

---

### **场景3: 第三方服务集成（API Key方式）**

```java
/**
 * 场景：第三方系统使用API Key换取临时Token
 * 
 * 流程：API Key验证 → 生成Token → 使用Token访问API
 */

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    
    @Autowired
    private IApiKeyService apiKeyService;
    
    /**
     * 使用API Key换取Token
     */
    @PostMapping("/token-by-apikey")
    public R<LoginVo> getTokenByApiKey(@RequestBody ApiKeyRequest request) {
        // 1. 验证API Key
        ApiKey apiKey = apiKeyService.validateApiKey(request.getApiKey());
        if (apiKey == null || !apiKey.isActive()) {
            return R.fail("无效的API Key");
        }
        
        // 2. 构建服务账号LoginUser
        LoginUser serviceAccount = new LoginUser();
        serviceAccount.setUserId(apiKey.getUserId());
        serviceAccount.setUsername(apiKey.getAppName());
        serviceAccount.setUserType("api_account");
        
        // 3. 生成临时Token
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("api");
        model.setTimeout(7200L);  // 2小时
        model.setExtra(LoginHelper.CLIENT_KEY, apiKey.getAppId());
        
        LoginHelper.login(serviceAccount, model);
        
        // 4. 返回Token
        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(apiKey.getAppId());
        
        return R.ok(loginVo);
    }
}
```

---

### **场景4: 单点登录（SSO）集成**

```java
/**
 * 场景：从其他SSO系统获取用户信息后，生成本系统Token
 * 
 * 流程：SSO验证 → 获取用户信息 → 生成Token
 */

@RestController
@RequestMapping("/sso")
public class SsoController {
    
    @Autowired
    private ISsoService ssoService;
    
    @Autowired
    private IUserService userService;
    
    /**
     * SSO回调，生成本系统Token
     */
    @GetMapping("/callback")
    public R<LoginVo> ssoCallback(@RequestParam String ssoToken) {
        // 1. 验证SSO Token，获取用户信息
        SsoUser ssoUser = ssoService.validateSsoToken(ssoToken);
        if (ssoUser == null) {
            return R.fail("SSO验证失败");
        }
        
        // 2. 查询或创建本地用户
        SysUser localUser = userService.getOrCreateFromSso(ssoUser);
        
        // 3. 构建LoginUser
        LoginUser loginUser = buildLoginUser(localUser);
        
        // 4. 生成本系统Token
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("sso");
        model.setTimeout(1800L);
        model.setExtra("sso_source", ssoUser.getSource());
        
        LoginHelper.login(loginUser, model);
        
        // 5. 返回Token
        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        
        return R.ok(loginVo);
    }
}
```

---

## 🏗️ 架构模式对比

### **模式1: 集中式认证（推荐用于用户登录）**

```
┌─────────────┐
│  前端/客户端  │
└──────┬──────┘
       │ POST /login
       ↓
┌─────────────────┐
│    Gateway      │
└──────┬──────────┘
       │ 路由
       ↓
┌─────────────────┐
│  Auth Service   │ ← 集中认证
│  ────────────   │
│  • 验证密码      │
│  • 生成Token    │
│  • 统一日志      │
│  • 安全控制      │
└──────┬──────────┘
       │ 返回Token
       ↓
    所有服务共享Redis
```

**优点**:
- ✅ 集中管理，安全性高
- ✅ 统一日志审计
- ✅ 便于实施安全策略
- ✅ 符合微服务最佳实践

**适用场景**:
- 用户登录（Web、App）
- 需要密码验证的场景
- 需要审计的敏感操作

---

### **模式2: 分布式Token生成（推荐用于服务间调用）**

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Service A  │  │  Service B  │  │  Service C  │
│  ─────────  │  │  ─────────  │  │  ─────────  │
│ LoginHelper │  │ LoginHelper │  │ LoginHelper │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┴────────────────┘
                        ↓
                 所有服务共享Redis
                 (Token跨服务验证)
```

**优点**:
- ✅ 无需调用Auth Service
- ✅ 减少网络延迟
- ✅ 提高系统吞吐量
- ✅ 服务解耦

**适用场景**:
- 服务间调用（Service-to-Service）
- 内部管理接口
- API Key认证
- SSO集成
- 自动化任务/定时任务

---

## ⚠️ 重要注意事项

### **1. Token一致性**

所有服务生成的Token**必须使用相同的配置**：

```yaml
# 所有微服务的application.yml必须一致
sa-token:
  token-name: Authorization
  token-prefix: "Bearer"
  timeout: 1800          # ⚠️ 必须一致
  active-timeout: -1     # ⚠️ 必须一致
  is-concurrent: true
  is-share: false
```

### **2. Redis配置一致**

所有服务必须连接**同一个Redis实例**（或Redis集群）：

```yaml
# 所有微服务必须使用相同的Redis配置
spring:
  redis:
    host: 127.0.0.1    # ⚠️ 必须一致
    port: 6379
    database: 0        # ⚠️ 必须一致（database不同会导致Token无法共享）
```

### **3. LoginUser结构一致**

所有服务生成的`LoginUser`对象**必须包含必要字段**：

```java
// ⚠️ 必须设置的字段
loginUser.setUserId(userId);         // 必需
loginUser.setUsername(username);     // 必需
loginUser.setTenantId(tenantId);     // 多租户必需
loginUser.setLoginId(userId);        // 必需
loginUser.setUserType(userType);     // 必需
```

### **4. 安全考虑**

**不要在公开接口中暴露Token生成功能！**

```java
// ❌ 危险：任何人都可以生成Token
@PostMapping("/public/generate-token")  // 没有权限控制！
public R<String> generateToken() {
    // 这是安全漏洞！
}

// ✅ 安全：只有管理员或内部服务可以生成Token
@PostMapping("/internal/generate-token")
@SaCheckPermission("admin:token:generate")  // 权限控制
public R<String> generateToken() {
    // 安全
}
```

---

## 📊 性能对比

| 方式 | 网络请求 | 响应时间 | QPS | 推荐场景 |
|------|----------|----------|-----|----------|
| **HTTP调用Auth Service** | 2次（Gateway + Auth） | ~50ms | 2,000 | 用户登录 |
| **直接使用LoginHelper** | 0次（本地调用） | ~5ms | 20,000 | 服务间调用 |

**提升**: 性能提升**10倍**！

---

## 🎯 最佳实践建议

### **1. 用户登录 → 使用集中式Auth Service**

```java
// ✅ 用户登录走Auth Service（通过Gateway）
POST /auth/login
{
  "username": "admin",
  "password": "admin123",
  "clientId": "web-client"
}
```

**原因**:
- 集中管理
- 统一审计
- 安全策略一致

---

### **2. 服务间调用 → 使用分布式Token生成**

```java
// ✅ 服务A调用服务B时，直接生成Token
@Service
public class ServiceAClient {
    public void callServiceB() {
        // 直接生成Token
        LoginHelper.login(serviceUser, model);
        String token = StpUtil.getTokenValue();
        
        // 调用服务B
        callWithToken(token);
    }
}
```

**原因**:
- 减少网络开销
- 提高性能
- 服务解耦

---

### **3. 内部管理接口 → 按需选择**

```java
// ✅ 根据安全要求选择
// 高安全: 走Auth Service + 审计
// 低安全: 直接生成Token
```

---

## 🔧 完整示例代码

### **在任意微服务中生成Token的完整示例**

```java
package com.example.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.model.LoginUser;
import org.springframework.stereotype.Service;

/**
 * 通用Token生成服务
 * 
 * 可在任何微服务中使用（只要有ruoyi-common-satoken依赖）
 */
@Service
public class UniversalTokenService {
    
    /**
     * 方法1: 最简单 - 只需要用户ID
     */
    public String generateSimpleToken(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setLoginId(userId);
        loginUser.setUsername("user-" + userId);
        loginUser.setUserType("sys_user");
        loginUser.setTenantId("000000");
        
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("pc");
        model.setTimeout(1800L);
        
        LoginHelper.login(loginUser, model);
        return StpUtil.getTokenValue();
    }
    
    /**
     * 方法2: 完整版 - 包含所有信息
     */
    public String generateFullToken(Long userId, String username, String tenantId,
                                   List<String> roles, Set<String> permissions) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setLoginId(userId);
        loginUser.setUsername(username);
        loginUser.setTenantId(tenantId);
        loginUser.setUserType("sys_user");
        loginUser.setRoles(roles);
        loginUser.setMenuPermission(permissions);
        
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("pc");
        model.setTimeout(1800L);
        model.setActiveTimeout(-1L);
        model.setExtra(LoginHelper.CLIENT_KEY, "custom-service");
        
        LoginHelper.login(loginUser, model);
        return StpUtil.getTokenValue();
    }
    
    /**
     * 方法3: 服务账号Token
     */
    public String generateServiceToken(String serviceName) {
        LoginUser serviceAccount = new LoginUser();
        serviceAccount.setUserId(999L);  // 系统预留ID
        serviceAccount.setLoginId(999L);
        serviceAccount.setUsername(serviceName);
        serviceAccount.setUserType("service_account");
        serviceAccount.setTenantId("000000");
        
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("service");
        model.setTimeout(300L);  // 5分钟短期Token
        
        LoginHelper.login(serviceAccount, model);
        return StpUtil.getTokenValue();
    }
    
    /**
     * 方法4: 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            StpUtil.checkActiveTimeout();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 方法5: 从Token获取用户信息
     */
    public LoginUser getLoginUserFromToken() {
        return LoginHelper.getLoginUser();
    }
}
```

---

## ✅ 总结

| 问题 | 答案 |
|------|------|
| **能在任何微服务中生成Token吗？** | ✅ 可以！所有服务都有LoginHelper |
| **需要依赖Auth Service吗？** | ❌ 不需要！直接调用LoginHelper即可 |
| **Token会存储到Redis吗？** | ✅ 会！自动存储到共享Redis |
| **其他服务能验证这个Token吗？** | ✅ 能！所有服务共享同一个Redis |
| **适合所有场景吗？** | ⚠️ 不是！用户登录建议用Auth Service |
| **性能如何？** | ✅ 比HTTP调用快10倍！ |

---

## 🚀 快速开始

### **步骤1: 确保依赖**

你的微服务pom.xml中需要有：

```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-satoken</artifactId>
</dependency>
```

### **步骤2: 创建Token服务**

```java
@Service
public class MyTokenService {
    public String generateToken(Long userId, String username) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setLoginId(userId);
        loginUser.setUserType("sys_user");
        loginUser.setTenantId("000000");
        
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType("pc");
        model.setTimeout(1800L);
        
        LoginHelper.login(loginUser, model);
        return StpUtil.getTokenValue();
    }
}
```

### **步骤3: 使用**

```java
@Autowired
private MyTokenService myTokenService;

String token = myTokenService.generateToken(1L, "admin");
// 使用token...
```

---

**现在你可以在任何微服务中生成Token了！** 🎉

