# 🚀 Sa-Token 开发者快速上手指南

> **适用人群**: 新加入项目的后端开发者  
> **预计时间**: 30分钟  
> **最后更新**: 2025-11-08

---

## 📋 目录

1. [5分钟快速体验](#1-5分钟快速体验)
2. [开发必备知识](#2-开发必备知识)
3. [常见开发场景](#3-常见开发场景)
4. [调试技巧](#4-调试技巧)
5. [常见错误](#5-常见错误)

---

## 1. 5分钟快速体验

### Step 1: 登录获取Token (1分钟)

```bash
# 使用curl或Postman发送登录请求
curl -X POST http://localhost:8080/xypai-auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_dev",
    "password": "123456",
    "clientType": "app",
    "deviceId": "test-device-001"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 2000,
    "username": "alice_dev"
  }
}
```

### Step 2: 使用Token访问接口 (2分钟)

```bash
# 复制上面获取的accessToken，访问需要登录的接口
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  # 替换为你的token

curl -H "Authorization: Bearer $TOKEN" \
     -H "clientid: app" \
     http://localhost:8080/xypai-user/api/v2/users/profile
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 2000,
    "username": "alice_dev",
    "nickname": "Alice",
    "avatar": "https://...",
    ...
  }
}
```

### Step 3: 编写你的第一个需要登录的接口 (2分钟)

```java
@RestController
@RequestMapping("/api/v2/hello")
public class HelloController {
    
    /**
     * 🎯 只需要添加 @SaCheckLogin 注解即可！
     */
    @SaCheckLogin
    @GetMapping
    public R<String> hello() {
        // 获取当前登录用户ID
        Long userId = LoginHelper.getUserId();
        String username = LoginHelper.getUsername();
        
        return R.ok("Hello, " + username + " (userId: " + userId + ")");
    }
}
```

测试接口:
```bash
curl -H "Authorization: Bearer $TOKEN" \
     -H "clientid: app" \
     http://localhost:8080/xypai-xxx/api/v2/hello
```

✅ **恭喜！你已经掌握了Sa-Token的基本用法！**

---

## 2. 开发必备知识

### 2.1 核心概念（必须掌握）

| 概念 | 说明 | 示例 |
|-----|------|------|
| **Token** | 用户登录后的身份凭证 | `eyJhbGci...` |
| **LoginId** | 用户唯一标识 | `app_user:2000` |
| **ClientId** | 客户端类型 | `app`, `pc`, `ios` |
| **Permission** | 权限码 | `user:add`, `user:delete` |
| **Role** | 角色 | `admin`, `editor`, `viewer` |
| **Same-Token** | 服务间通信凭证（固定） | 自动处理，开发者无需关心 |

### 2.2 核心工具类（必须掌握）

#### LoginHelper - 获取用户信息

```java
// ✅ 最常用的方法
Long userId = LoginHelper.getUserId();          // 获取用户ID
String username = LoginHelper.getUsername();    // 获取用户名
String client = LoginHelper.getClient();        // 获取ClientId

// ✅ 获取完整用户对象
LoginUser loginUser = LoginHelper.getLoginUser();
```

#### StpUtil - Sa-Token核心工具类

```java
// ✅ 登录/注销
StpUtil.login(loginId);           // 登录
StpUtil.logout();                 // 注销当前用户

// ✅ 判断登录状态
boolean isLogin = StpUtil.isLogin();              // 是否登录
StpUtil.checkLogin();                             // 校验登录（未登录抛异常）

// ✅ 获取Token信息
String token = StpUtil.getTokenValue();           // 获取Token
long timeout = StpUtil.getTokenTimeout();         // 获取剩余有效期

// ✅ 权限校验
boolean hasPerm = StpUtil.hasPermission("user:add");        // 判断权限
StpUtil.checkPermission("user:add");                        // 校验权限（无权限抛异常）

// ✅ 角色校验
boolean hasRole = StpUtil.hasRole("admin");                 // 判断角色
StpUtil.checkRole("admin");                                 // 校验角色（无角色抛异常）
```

### 2.3 常用注解（必须掌握）

```java
@SaCheckLogin                    // ✅ 登录校验
@SaCheckPermission("user:add")   // ✅ 权限校验
@SaCheckRole("admin")            // ✅ 角色校验
@SaIgnore                        // ✅ 忽略校验（公开接口）
```

---

## 3. 常见开发场景

### 场景1: 创建一个需要登录的接口

```java
@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    
    /**
     * 方式1: 使用注解（推荐）
     */
    @SaCheckLogin
    @GetMapping("/profile")
    public R<UserProfileVO> getProfile() {
        Long userId = LoginHelper.getUserId();
        return R.ok(userService.getProfile(userId));
    }
    
    /**
     * 方式2: 编程式校验
     */
    @GetMapping("/info")
    public R<UserInfoVO> getInfo() {
        // 手动校验登录
        StpUtil.checkLogin();
        
        Long userId = LoginHelper.getUserId();
        return R.ok(userService.getInfo(userId));
    }
}
```

### 场景2: 创建一个需要特定权限的接口

```java
@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    
    /**
     * 方式1: 单个权限校验
     */
    @SaCheckPermission("user:add")
    @PostMapping
    public R<Void> addUser(@RequestBody UserDTO dto) {
        return userService.addUser(dto);
    }
    
    /**
     * 方式2: 多个权限校验（必须全部拥有）
     */
    @SaCheckPermission(value = {"user:update", "user:get"}, mode = SaMode.AND)
    @PutMapping("/{id}")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        return userService.updateUser(id, dto);
    }
    
    /**
     * 方式3: 多个权限校验（拥有其一即可）
     */
    @SaCheckPermission(value = {"user:list", "user:get"}, mode = SaMode.OR)
    @GetMapping
    public R<List<UserVO>> listUsers() {
        return userService.listUsers();
    }
    
    /**
     * 方式4: 权限或角色（任意一个满足即可）
     */
    @SaCheckPermission(value = "user:delete", orRole = "admin")
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
```

### 场景3: 创建一个公开接口（无需登录）

```java
@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    
    /**
     * 方式1: 使用@SaIgnore注解
     */
    @SaIgnore  // ✅ 忽略登录校验
    @GetMapping("/public/list")
    public R<List<UserVO>> getPublicList() {
        return R.ok(userService.getPublicList());
    }
    
    /**
     * 方式2: 配置白名单（在网关配置）
     * ruoyi-gateway/application.yml:
     * security:
     *   ignore:
     *     whites:
     *       - /api/v2/users/public/**
     */
}
```

### 场景4: 实现登录接口

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @PostMapping("/login")
    public R<LoginResultVO> login(@RequestBody LoginDTO loginDTO) {
        // 1️⃣ 验证用户名密码（从数据库查询）
        AuthUserDTO user = userService.authenticateUser(
            loginDTO.getUsername(), 
            loginDTO.getPassword()
        );
        
        // 2️⃣ 构建LoginUser对象
        LoginUser loginUser = LoginUser.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .userType(UserType.APP_USER.getUserType())
            .loginId(UserType.APP_USER.getLoginId(user.getId()))
            .menuPermission(Set.of("app:*"))  // 用户权限
            .rolePermission(Set.of("app_user"))  // 用户角色
            .build();
        
        // 3️⃣ 构建登录参数
        SaLoginParameter loginModel = new SaLoginParameter();
        loginModel.setDeviceType(loginDTO.getClientType());  // ✅ 设备类型
        loginModel.setTimeout(86400L);  // ✅ 24小时有效期
        loginModel.setExtra("clientType", loginDTO.getClientType());  // ✅ 关键！
        
        // 4️⃣ 执行登录
        LoginHelper.login(loginUser, loginModel);
        
        // 5️⃣ 获取Token
        String token = StpUtil.getTokenValue();
        
        // 6️⃣ 返回结果
        return R.ok(LoginResultVO.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(86400L)
            .userId(user.getId())
            .username(user.getUsername())
            .build());
    }
}
```

### 场景5: 跨服务调用（自动携带Token）

```java
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements IContentService {
    
    private final RestTemplate restTemplate;  // ✅ 使用配置好的RestTemplate
    
    /**
     * 调用用户服务（自动携带Token）
     */
    @Override
    public ContentDetailVO getContentDetail(Long contentId) {
        // 1️⃣ 获取内容信息
        Content content = contentMapper.selectById(contentId);
        
        // 2️⃣ 调用用户服务（✅ RestTemplate会自动携带Token）
        String url = "http://localhost:9401/api/v2/users/" + content.getAuthorId();
        UserProfileVO author = restTemplate.getForObject(url, UserProfileVO.class);
        
        // 3️⃣ 组装返回数据
        return ContentDetailVO.builder()
            .contentId(content.getId())
            .title(content.getTitle())
            .author(author)  // ✅ 成功获取作者信息
            .build();
    }
}
```

**⚠️ 重要提示**:
- ✅ 使用 `@Autowired` 注入的 `RestTemplate`
- ❌ 不要使用 `new RestTemplate()`（不会自动携带Token）

### 场景6: Service层业务权限校验

```java
@Service
public class UserServiceImpl implements IUserService {
    
    @Override
    public R<Void> updateUserProfile(Long targetUserId, UserProfileDTO dto) {
        Long currentUserId = LoginHelper.getUserId();
        
        // ✅ 业务权限校验1: 只能修改自己的资料
        if (!targetUserId.equals(currentUserId)) {
            // 除非是管理员
            if (!StpUtil.hasRole("admin")) {
                throw new ServiceException("只能修改自己的资料");
            }
        }
        
        // ✅ 业务权限校验2: 敏感字段需要特殊权限
        if (dto.getSensitiveField() != null) {
            StpUtil.checkPermission("user:update:sensitive");
        }
        
        // 执行更新...
        return R.ok();
    }
}
```

---

## 4. 调试技巧

### 技巧1: 打印Token信息（推荐）

```java
@GetMapping("/debug/token")
public R<Map<String, Object>> debugToken() {
    Map<String, Object> info = new HashMap<>();
    
    // 基础信息
    info.put("isLogin", StpUtil.isLogin());
    info.put("token", StpUtil.getTokenValue());
    info.put("loginId", StpUtil.getLoginId());
    
    // 用户信息
    info.put("userId", LoginHelper.getUserId());
    info.put("username", LoginHelper.getUsername());
    info.put("client", LoginHelper.getClient());
    
    // Token详情
    info.put("timeout", StpUtil.getTokenTimeout());
    info.put("tokenInfo", StpUtil.getTokenInfo());
    
    // 权限信息
    info.put("permissions", StpUtil.getPermissionList());
    info.put("roles", StpUtil.getRoleList());
    
    return R.ok(info);
}
```

### 技巧2: 使用测试接口

```bash
# xypai-security模块提供了完整的测试接口

# 1️⃣ 检查Token有效性
GET http://localhost:9405/api/v1/test/token/check
Authorization: Bearer YOUR_TOKEN
clientid: app

# 2️⃣ 获取Token详细信息
GET http://localhost:9405/api/v1/test/token/info
Authorization: Bearer YOUR_TOKEN
clientid: app

# 3️⃣ 获取标准请求头
GET http://localhost:9405/api/v1/test/token/headers
Authorization: Bearer YOUR_TOKEN
clientid: app

# 4️⃣ 测试跨服务调用
GET http://localhost:9405/api/v1/test/token/call-other-service?targetUrl=http://localhost:9401/api/v2/users/profile
Authorization: Bearer YOUR_TOKEN
clientid: app
```

### 技巧3: 查看Redis中的Token

```bash
# 连接Redis
redis-cli

# 查看所有Token相关的Key
KEYS satoken:*

# 查看具体的Token信息
GET satoken:login:token:eyJhbGci...

# 查看Same-Token
GET satoken:var:same-token
```

### 技巧4: 开启DEBUG日志

```yaml
# application.yml
logging:
  level:
    com.xypai: DEBUG
    org.dromara: DEBUG
    cn.dev33.satoken: DEBUG
```

查看日志输出:
```
🔐 [GATEWAY AUTH] 开始认证: /api/v2/users/profile
   📋 Token值: eyJhbGci...
   ✅ Token验证通过
   ✅ ClientId匹配通过
   ✅ [GATEWAY AUTH] 认证成功
```

---

## 5. 常见错误

### 错误1: 401 Unauthorized

**现象**:
```json
{
  "code": 401,
  "msg": "认证失败，无法访问系统资源"
}
```

**可能原因**:
1. ❌ Token格式错误（缺少`Bearer`前缀）
2. ❌ Token已过期
3. ❌ Token无效或被篡改
4. ❌ ClientId不匹配

**解决方法**:

```java
// ✅ 正确的Token格式
curl -H "Authorization: Bearer eyJhbGci..." \
     -H "clientid: app" \
     http://localhost:8080/api/v2/users/profile

// ❌ 错误的格式（缺少Bearer前缀）
curl -H "Authorization: eyJhbGci..." \  // ❌ 错误
     http://localhost:8080/api/v2/users/profile
```

### 错误2: 403 Forbidden (没有访问权限)

**现象**:
```json
{
  "code": 403,
  "msg": "没有访问权限，请联系管理员授权"
}
```

**可能原因**:
1. ❌ 用户没有所需的权限
2. ❌ 用户没有所需的角色

**解决方法**:

```java
// 1️⃣ 检查用户是否有权限
StpUtil.getPermissionList();  // 查看当前用户的所有权限

// 2️⃣ 检查用户是否有角色
StpUtil.getRoleList();  // 查看当前用户的所有角色

// 3️⃣ 在数据库中为用户添加权限/角色
```

### 错误3: ClientId不匹配

**现象**:
```
客户端ID与Token不匹配
```

**原因**: 登录时使用的ClientId和请求时的ClientId不一致

**解决方法**:

```java
// ✅ 确保一致
// 登录时
SaLoginParameter loginModel = new SaLoginParameter();
loginModel.setDeviceType("app");  // ✅
loginModel.setExtra("clientType", "app");  // ✅

// 请求时
headers.set("clientid", "app");  // ✅ 必须一致
```

### 错误4: 跨服务调用失败

**现象**: Gateway认证通过，但调用其他微服务返回401

**可能原因**:
1. ❌ 没有配置`RestTemplateConfig`
2. ❌ 使用了`new RestTemplate()`而不是注入的Bean
3. ❌ Same-Token验证失败

**解决方法**:

```java
// ✅ 正确做法
@Service
public class MyService {
    
    @Autowired  // ✅ 使用注入的RestTemplate
    private RestTemplate restTemplate;
    
    public void callOtherService() {
        // ✅ 自动携带Token
        restTemplate.getForObject("http://...", String.class);
    }
}

// ❌ 错误做法
public void callOtherService() {
    RestTemplate restTemplate = new RestTemplate();  // ❌ 不会自动携带Token
    restTemplate.getForObject("http://...", String.class);
}
```

### 错误5: @SaCheckLogin不生效

**可能原因**: 没有注册Sa-Token拦截器

**解决方法**:

```java
// 检查是否有以下配置类
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ✅ 必须注册拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/**").check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
```

---

## 6. 开发检查清单

在提交代码前，请确认以下事项：

### Controller层
- [ ] ✅ 需要登录的接口添加了`@SaCheckLogin`
- [ ] ✅ 需要权限的接口添加了`@SaCheckPermission`
- [ ] ✅ 公开接口添加了`@SaIgnore`
- [ ] ✅ 使用`LoginHelper`获取用户信息

### Service层
- [ ] ✅ 跨服务调用使用注入的`RestTemplate`
- [ ] ✅ 添加了必要的业务权限校验
- [ ] ✅ 异常处理完善

### 配置
- [ ] ✅ 微服务添加了`RestTemplateConfig`
- [ ] ✅ Redis配置使用`database: 0`
- [ ] ✅ 启用了`check-same-token: true`

### 测试
- [ ] ✅ 登录接口测试通过
- [ ] ✅ 权限校验测试通过
- [ ] ✅ 跨服务调用测试通过

---

## 7. 快速参考卡片

### 常用代码片段

```java
// ==================== Controller ====================
// 登录校验
@SaCheckLogin
@GetMapping("/api")
public R<String> api() { ... }

// 权限校验
@SaCheckPermission("user:add")
@PostMapping("/api")
public R<Void> add() { ... }

// 角色校验
@SaCheckRole("admin")
@DeleteMapping("/api")
public R<Void> delete() { ... }

// 公开接口
@SaIgnore
@GetMapping("/public/api")
public R<String> publicApi() { ... }

// ==================== Service ====================
// 获取当前用户
Long userId = LoginHelper.getUserId();
String username = LoginHelper.getUsername();
LoginUser user = LoginHelper.getLoginUser();

// 判断权限
if (StpUtil.hasPermission("user:add")) { ... }

// 判断角色
if (StpUtil.hasRole("admin")) { ... }

// 跨服务调用（自动携带Token）
@Autowired
private RestTemplate restTemplate;
UserVO user = restTemplate.getForObject(url, UserVO.class);
```

### 常用curl命令

```bash
# 登录
curl -X POST http://localhost:8080/xypai-auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice_dev","password":"123456","clientType":"app"}'

# 访问需要登录的接口
curl -H "Authorization: Bearer TOKEN" \
     -H "clientid: app" \
     http://localhost:8080/xypai-user/api/v2/users/profile

# 测试Token
curl -H "Authorization: Bearer TOKEN" \
     -H "clientid: app" \
     http://localhost:9405/api/v1/test/token/check
```

---

## 🎉 总结

### 三步开始开发

1. **登录获取Token** - 调用登录接口
2. **在Controller添加注解** - `@SaCheckLogin`、`@SaCheckPermission`
3. **使用LoginHelper获取用户信息** - `LoginHelper.getUserId()`

### 记住这3个核心

1. **LoginHelper** - 获取用户信息
2. **StpUtil** - Sa-Token核心工具类
3. **@SaCheckLogin** - 最常用的注解

### 遇到问题？

1. 查看[完整技术架构文档](./Sa-Token完整技术架构文档.md)
2. 使用测试接口调试
3. 查看Gateway和微服务日志
4. 联系团队技术负责人

---

**祝你开发愉快！** 🚀

**文档版本**: v1.0  
**最后更新**: 2025-11-08  
**维护团队**: DevTeam

