# SimpleSaTokenTest 升级对比图

## 📊 测试流程对比

### 旧版本（仅支持IAuthService）

```
┌──────────────────────────────────────────────────────────────┐
│                    SimpleSaTokenTest                          │
│                    （单一模式）                                │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │  阶段1: IAuthService登录获取Token     │
        │  authService.loginWithPassword()      │
        │  • 用户名密码验证                      │
        │  • 返回LoginResultVO                  │
        └───────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │  阶段2: 验证Token格式                  │
        │  • JWT标准验证                         │
        │  • Sa-Token状态检查                    │
        └───────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │  阶段3-5: Gateway访问各服务           │
        │  • RuoYi-Demo                         │
        │  • XYPai-Content                      │
        │  • RuoYi-System                       │
        └───────────────────────────────────────┘

❌ 局限性:
   • 只能演示标准登录流程
   • 无法展示分布式Token生成能力
   • 不够灵活
```

---

### 新版本（支持双模式）⭐

```
┌──────────────────────────────────────────────────────────────┐
│                    SimpleSaTokenTest                          │
│                   （双模式可切换）                             │
│                                                                │
│   USE_AUTH_SERVICE = true/false  👈 一键切换                  │
└──────────────────────────────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
   ┌─────────────────┐           ┌─────────────────────┐
   │   方式A: 标准    │           │   方式B: 分布式 ⭐   │
   │  IAuthService   │           │   LoginHelper       │
   └─────────────────┘           └─────────────────────┘
            │                               │
            │                               │
            ▼                               ▼
┌──────────────────────┐       ┌──────────────────────────┐
│ authService          │       │ 1. sysUserMapper         │
│ .loginWithPassword() │       │    .selectUserByUserName │
│                      │       │                          │
│ • 密码验证           │       │ 2. 构建LoginUser对象     │
│ • 查询用户信息       │       │                          │
│ • 调用LoginHelper    │       │ 3. LoginHelper           │
│ • 返回LoginResultVO  │       │    .login(loginUser)     │
│                      │       │                          │
│                      │       │ 4. StpUtil               │
│                      │       │    .getTokenValue()      │
└──────────────────────┘       └──────────────────────────┘
            │                               │
            └───────────────┬───────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  获取Token     │
                    │  (完全等价)    │
                    └───────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │  阶段2: 验证Token格式                  │
        │  • JWT标准验证                         │
        │  • Sa-Token状态检查                    │
        │  • 解析JWT Payload                    │
        └───────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │  阶段3-5: 使用自生成Token访问各服务    │
        │  ✅ RuoYi-Demo (Redis缓存测试)        │
        │  ✅ XYPai-Content (首页用户列表)       │
        │  ✅ RuoYi-System (菜单路由)            │
        │                                        │
        │  验证点:                                │
        │  • Token可用性                         │
        │  • 跨服务认证                          │
        │  • LoginHelper.getUserId()            │
        └───────────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  ✅ 测试成功   │
                    │  所有验证通过  │
                    └───────────────┘

✅ 优势:
   • 演示分布式Token生成能力
   • 展示Token的通用性
   • 验证任意微服务都能独立生成Token
   • 灵活切换模式
   • 更接近真实使用场景
```

---

## 🔄 两种模式详细对比

### 方式A: IAuthService（标准业务流程）

```
┌─────────────────────────────────────────────────────┐
│                IAuthService模式                      │
│                                                      │
│  使用场景: 用户登录、需要密码验证                     │
└─────────────────────────────────────────────────────┘

Step 1: 构建LoginDTO
┌──────────────────────────┐
│ LoginDTO                 │
│ • username: "admin"      │
│ • password: "admin123"   │
│ • clientType: "web"      │
│ • deviceId: "xxx"        │
│ • rememberMe: false      │
└──────────────────────────┘
            │
            ▼
Step 2: 调用认证服务
┌──────────────────────────┐
│ IAuthService             │
│ .loginWithPassword()     │
│                          │
│ 内部流程:                │
│ 1. BCrypt密码验证        │
│ 2. 查询用户信息          │
│ 3. 构建LoginUser         │
│ 4. LoginHelper.login()   │
│ 5. 构建LoginResultVO     │
└──────────────────────────┘
            │
            ▼
Step 3: 获取结果
┌──────────────────────────┐
│ LoginResultVO            │
│ • accessToken: "..."     │
│ • refreshToken: "..."    │
│ • tokenType: "Bearer"    │
│ • expiresIn: 1800        │
│ • userInfo: {...}        │
└──────────────────────────┘
            │
            ▼
        提取Token
┌──────────────────────────┐
│ String token =           │
│   result.getAccessToken()│
└──────────────────────────┘

优点:
  ✅ 完整的业务验证
  ✅ 密码校验
  ✅ 返回完整用户信息
  ✅ 符合标准登录流程

缺点:
  ❌ 需要密码
  ❌ 只能在Auth Service使用
  ❌ 不够灵活
```

---

### 方式B: LoginHelper（分布式模式）⭐ 推荐

```
┌─────────────────────────────────────────────────────┐
│             LoginHelper分布式模式 ⭐                 │
│                                                      │
│  使用场景: 内部调用、定时任务、测试、第三方集成       │
│                                                      │
│  🔥 任何微服务都可以使用此模式！                      │
└─────────────────────────────────────────────────────┘

Step 1: 查询用户信息
┌──────────────────────────┐
│ SysUserMapper            │
│ .selectUserByUserName()  │
│                          │
│ 输入: "admin"            │
│ 输出: SysUser对象        │
└──────────────────────────┘
            │
            ▼
Step 2: 构建LoginUser
┌──────────────────────────┐
│ LoginUser loginUser      │
│ • userId: 1              │
│ • username: "admin"      │
│ • userType: "sys_user"   │
│ • deptId: 103            │
│ • tenantId: "000000"     │
└──────────────────────────┘
            │
            ▼
Step 3: 生成Token 🔥
┌──────────────────────────┐
│ LoginHelper              │
│ .login(loginUser)        │
│                          │
│ 内部流程:                │
│ 1. StpUtil.login()       │
│ 2. 存储用户信息到Redis   │
│ 3. 生成JWT Token         │
└──────────────────────────┘
            │
            ▼
Step 4: 获取Token
┌──────────────────────────┐
│ String token =           │
│   StpUtil.getTokenValue()│
└──────────────────────────┘
            │
            ▼
        ✅ 完成！
┌──────────────────────────┐
│ Token可用于访问所有服务   │
│ • RuoYi-Demo             │
│ • XYPai-Content          │
│ • RuoYi-System           │
│ • 任何其他微服务...       │
└──────────────────────────┘

优点:
  ✅ 无需密码验证
  ✅ 任何微服务都可使用
  ✅ 更灵活
  ✅ 适合内部调用
  ✅ 简单直接
  ✅ 性能更好（无HTTP调用）

适用场景:
  ✅ 定时任务生成Token
  ✅ 内部服务间调用
  ✅ 测试环境
  ✅ 第三方集成
  ✅ 微服务独立部署
```

---

## 🌐 分布式Token生成的可移植性

```
┌──────────────────────────────────────────────────────────┐
│              分布式Token生成模式的可移植性                │
│                                                           │
│  核心理念: 任何微服务都可以独立生成Token                  │
└──────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │   共享Redis     │
                    │  (Token存储)    │
                    └─────────────────┘
                            ▲
            ┌───────────────┼───────────────┐
            │               │               │
            │               │               │
    ┌───────┴─────┐  ┌──────┴──────┐  ┌────┴────────┐
    │ Auth Service│  │Content Svc  │  │ User Service│
    │             │  │             │  │             │
    │ LoginHelper │  │ LoginHelper │  │ LoginHelper │
    │   .login()  │  │   .login()  │  │   .login()  │
    └─────────────┘  └─────────────┘  └─────────────┘
            │               │               │
            └───────────────┼───────────────┘
                            │
                    生成的Token完全等价
                            │
                            ▼
            ┌───────────────────────────────┐
            │  Token可用于访问任何微服务     │
            └───────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
    ┌───▼────┐       ┌──────▼──────┐      ┌────▼────┐
    │ Demo   │       │  Content    │      │ System  │
    │ Service│       │  Service    │      │ Service │
    └────────┘       └─────────────┘      └─────────┘
    
    所有服务都认可此Token，因为:
    • Token存储在共享Redis中
    • 所有服务都使用相同的Sa-Token配置
    • Token验证逻辑一致
```

---

## 📋 实际应用场景对比

### 场景1: 用户登录

**适合方式A（IAuthService）**

```
用户界面
    │
    ▼ 输入用户名密码
┌──────────────┐
│ 登录表单      │
│ username     │
│ password     │
└──────────────┘
    │
    ▼ POST /auth/login
┌──────────────┐
│ Auth Service │
│              │
│ IAuthService │
└──────────────┘
    │
    ▼ 返回LoginResultVO
┌──────────────┐
│ 前端接收Token │
│ 保存到localStorage
└──────────────┘

✅ 使用IAuthService: 完整的业务流程，包含密码验证
```

---

### 场景2: 定时任务调用API

**适合方式B（LoginHelper）**

```
定时任务启动
    │
    ▼
┌──────────────────────────┐
│ @Scheduled(...)          │
│ public void syncData() { │
│                          │
│   // 生成Token           │
│   LoginHelper.login(     │
│     buildSystemUser()    │
│   );                     │
│                          │
│   String token =         │
│     StpUtil.getTokenValue();
│                          │
│   // 调用其他服务         │
│   callApiWithToken(token);
│ }                        │
└──────────────────────────┘

✅ 使用LoginHelper: 无需密码，直接生成Token
❌ 不用IAuthService: 定时任务没有密码输入
```

---

### 场景3: 内部服务间调用

**适合方式B（LoginHelper）**

```
Content Service 需要调用 User Service

┌─────────────────────────────┐
│ Content Service             │
│                             │
│ // 需要以用户身份调用        │
│ public void someMethod() {  │
│                             │
│   // 生成Token              │
│   SysUser user =            │
│     userMapper.selectById(1);
│                             │
│   LoginUser loginUser =     │
│     buildLoginUser(user);   │
│                             │
│   LoginHelper.login(        │
│     loginUser               │
│   );                        │
│                             │
│   String token =            │
│     StpUtil.getTokenValue();│
│                             │
│   // 调用User Service       │
│   userServiceClient         │
│     .getUserInfo(token, id);│
│ }                           │
└─────────────────────────────┘
            │
            ▼ 携带Token
┌─────────────────────────────┐
│ User Service                │
│                             │
│ // Sa-Token自动验证Token    │
│ @GetMapping("/user/info")   │
│ public UserInfo getUserInfo()│
│ {                           │
│   Long userId =             │
│     LoginHelper.getUserId();│
│   // 正常业务逻辑            │
│ }                           │
└─────────────────────────────┘

✅ 使用LoginHelper: 服务间可信调用
❌ 不用IAuthService: 内部调用不需要走HTTP
```

---

### 场景4: 测试环境

**适合方式B（LoginHelper）**

```
单元测试

┌──────────────────────────────┐
│ @BeforeEach                  │
│ public void setUp() {        │
│                              │
│   // 快速生成测试Token        │
│   SysUser testUser =         │
│     createTestUser();        │
│                              │
│   LoginUser loginUser =      │
│     buildLoginUser(testUser);│
│                              │
│   LoginHelper.login(         │
│     loginUser                │
│   );                         │
│                              │
│   testToken =                │
│     StpUtil.getTokenValue(); │
│ }                            │
│                              │
│ @Test                        │
│ public void testApi() {      │
│   // 使用testToken测试        │
│   testRestTemplate           │
│     .exchange(url,           │
│       HttpMethod.GET,        │
│       new HttpEntity<>(      │
│         createHeaders(       │
│           testToken)),       │
│       String.class);         │
│ }                            │
└──────────────────────────────┘

✅ 使用LoginHelper: 快速生成测试Token
✅ 优势: 无需启动Auth Service
✅ 优势: 测试速度更快
```

---

## 🎯 核心价值对比

### IAuthService模式

```
┌──────────────────────────────────────┐
│         IAuthService模式              │
├──────────────────────────────────────┤
│ 使用场景                              │
│  • 用户登录                           │
│  • 需要密码验证                        │
│  • 标准业务流程                        │
│  • 外部API调用                        │
├──────────────────────────────────────┤
│ 优势                                  │
│  ✅ 完整的业务验证                     │
│  ✅ 密码校验                          │
│  ✅ 返回完整用户信息                   │
│  ✅ 符合标准登录流程                   │
├──────────────────────────────────────┤
│ 局限性                                │
│  ❌ 需要密码                          │
│  ❌ 只能在Auth Service使用             │
│  ❌ 不够灵活                          │
│  ❌ 增加服务间依赖                     │
└──────────────────────────────────────┘
```

### LoginHelper模式 ⭐

```
┌──────────────────────────────────────┐
│        LoginHelper模式 ⭐              │
├──────────────────────────────────────┤
│ 使用场景                              │
│  • 定时任务生成Token                  │
│  • 内部服务间调用                      │
│  • 测试环境                           │
│  • 第三方集成                         │
│  • 微服务独立部署                      │
├──────────────────────────────────────┤
│ 优势                                  │
│  ✅ 无需密码验证                       │
│  ✅ 任何微服务都可使用                 │
│  ✅ 更灵活                            │
│  ✅ 适合内部调用                       │
│  ✅ 简单直接                          │
│  ✅ 性能更好（无HTTP调用）             │
│  ✅ 去中心化                          │
│  ✅ 减少服务间依赖                     │
├──────────────────────────────────────┤
│ 注意事项                              │
│  ⚠️ 仅用于内部可信调用                 │
│  ⚠️ 不要暴露给外部                     │
│  ⚠️ 需要记录审计日志                   │
└──────────────────────────────────────┘
```

---

## 🔧 快速切换模式

### 在SimpleSaTokenTest中切换

```java
// 找到这行配置
private static final boolean USE_AUTH_SERVICE = false;  // 👈 修改这里

// 设置为 true: 使用IAuthService
// 设置为 false: 使用LoginHelper（分布式模式）⭐
```

### 测试对比

```bash
# 测试方式A（IAuthService）
USE_AUTH_SERVICE = true
运行测试 → 查看标准流程日志

# 测试方式B（LoginHelper）⭐
USE_AUTH_SERVICE = false
运行测试 → 查看分布式模式日志

# 对比两种方式的日志输出
# 你会发现生成的Token完全等价！
```

---

## 📚 参考文档

- **[TEST_DISTRIBUTED_TOKEN_MODE.md](../xypai-security/security-oauth/TEST_DISTRIBUTED_TOKEN_MODE.md)** - 详细使用说明
- **[分布式Token生成模式.md](./分布式Token生成模式.md)** - 理论说明
- **[UniversalTokenService-Example.java](./UniversalTokenService-Example.java)** - 代码示例
- **[Quick-Start-Token-Generation.md](./Quick-Start-Token-Generation.md)** - 快速开始

---

**Made with ❤️ by XYPai Team**

*最后更新：2025-11-10*

