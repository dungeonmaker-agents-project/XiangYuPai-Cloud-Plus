# ⚡ Token机制快速参考

> **一页纸快速了解Token的创建、存储和使用**

---

## 🔑 核心概念

| 概念 | 说明 |
|------|------|
| **Token** | JWT格式的访问凭证，存储在Redis中 |
| **LoginHelper** | 统一的Token操作工具类 |
| **Sa-Token** | 底层认证框架，提供Token生命周期管理 |
| **多级缓存** | Caffeine（5秒） + Redis（1800秒） |
| **Token Extra** | 轻量级元数据（userId、tenantId等） |
| **Token Session** | 完整用户对象（LoginUser） |

---

## 📝 三步创建Token

### **Step 1: 用户登录**
```java
// 文件: TokenController.java
POST /login
{
  "username": "admin",
  "password": "admin123",
  "clientId": "e5cd7e48...",
  "grantType": "password"
}
```

### **Step 2: 验证并生成Token**
```java
// 文件: PasswordAuthStrategy.java
LoginUser loginUser = remoteUserService.getUserInfo(username, tenantId);
loginService.checkLogin(LoginType.PASSWORD, tenantId, username, 
    () -> !BCrypt.checkpw(password, user.getPassword()));
    
SaLoginParameter model = new SaLoginParameter();
model.setDeviceType(client.getDeviceType());
model.setTimeout(client.getTimeout());

LoginHelper.login(loginUser, model);  // 🔥 生成Token
```

### **Step 3: 返回Token给客户端**
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 1800,
    "clientId": "e5cd7e48..."
  }
}
```

---

## 🗄️ Redis存储结构

```
Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Redis Keys:
├─ satoken:login:token:{token} = "1"                    // Token → UserId
├─ satoken:login:session:{token} = {LoginUser对象}       // 完整用户信息
├─ satoken:login:extra:{token}:userId = "1"             // 用户ID（快速访问）
├─ satoken:login:extra:{token}:tenantId = "000000"      // 租户ID
├─ satoken:login:extra:{token}:userName = "admin"       // 用户名
├─ satoken:login:extra:{token}:clientid = "e5cd7e48..." // 客户端ID
├─ satoken:login:id:1 = ["token1", "token2"]            // 用户的所有Token
└─ satoken:login:last-active:{token} = 1699999999999    // 最后活跃时间
```

---

## 🚀 三种使用方式

### **方式1: 获取用户ID（推荐）**
```java
Long userId = LoginHelper.getUserId();
// ✅ 速度快（~0.5ms）
// ✅ 使用Extra，无需反序列化
```

### **方式2: 获取完整用户对象**
```java
LoginUser loginUser = LoginHelper.getLoginUser();
Set<String> roles = loginUser.getRoles();
Set<String> permissions = loginUser.getMenuPermission();
// ⚠️ 速度慢（~5ms）
// ⚠️ 需要反序列化完整对象
```

### **方式3: 权限检查（注解）**
```java
@SaCheckPermission("system:user:add")
@PostMapping("/add")
public R<Void> add(@RequestBody User user) {
    return userService.save(user);
}
// ✅ 声明式，易读
// ✅ 自动验证权限
```

---

## 🔄 完整请求流程

```
客户端 ──▶ Gateway ──▶ Auth Service ──▶ Redis
  │         │            │                │
  ├─ 1️⃣ POST /login     │                │
  │         │            │                │
  │         ├─ 2️⃣ 转发    │                │
  │         │            │                │
  │         │            ├─ 3️⃣ 验证密码    │
  │         │            │                │
  │         │            ├─ 4️⃣ 生成Token   ▶ 存储
  │         │            │                │
  │         ├─ 5️⃣ 返回Token              │
  │         │            │                │
  ├─ 6️⃣ 保存Token         │                │
  │         │            │                │
  ├─ 7️⃣ GET /api/user/profile            │
  │         │            │                │
  │         ├─ 8️⃣ 验证Token ──────────────▶ 查询
  │         │            │                │
  │         ├─ 9️⃣ 转发到后端服务           │
  │         │            │                │
  │         │            ├─ 10 获取userId ──▶ 查询
  │         │            │                │
  │         │            ├─ 11 查询数据库  │
  │         │            │                │
  │         ├─ 12 返回结果                │
  │         │            │                │
  ├─ 13 显示数据          │                │
```

---

## 📊 性能对比

| 操作 | 响应时间 | QPS | 推荐度 |
|------|----------|-----|--------|
| `LoginHelper.getUserId()` | ~0.5ms | 20,000 | ⭐⭐⭐⭐⭐ |
| `LoginHelper.getUsername()` | ~0.5ms | 20,000 | ⭐⭐⭐⭐⭐ |
| `LoginHelper.getTenantId()` | ~0.5ms | 20,000 | ⭐⭐⭐⭐⭐ |
| `LoginHelper.getLoginUser()` | ~5ms | 2,000 | ⭐⭐⭐ |
| `StpUtil.checkLogin()` | ~0.5ms | 20,000 | ⭐⭐⭐⭐⭐ |

**优化建议**:
- ✅ **优先使用轻量级方法** (`getUserId()`, `getUsername()`)
- ✅ **避免频繁调用** `getLoginUser()`
- ✅ **使用注解** 代替编程式权限检查

---

## 🔒 安全特性

| 特性 | 说明 | 配置 |
|------|------|------|
| **CSRF防护** | 禁止从Cookie读取Token | `is-read-cookie: false` |
| **Token过期** | 30分钟无操作自动失效 | `timeout: 1800` |
| **活跃续期** | 每次请求自动刷新过期时间 | `dynamic-active-timeout: true` |
| **多设备管理** | 支持查看/踢出在线设备 | `is-concurrent: true` |
| **JWT签名** | 防止Token篡改 | `jwt-secret-key: xxx` |

---

## 🛠️ 常用API

### **LoginHelper方法**

```java
// 用户信息
LoginHelper.getUserId()          // Long
LoginHelper.getUsername()        // String
LoginHelper.getLoginUser()       // LoginUser

// 租户信息
LoginHelper.getTenantId()        // String

// 部门信息
LoginHelper.getDeptId()          // Long
LoginHelper.getDeptName()        // String

// 权限检查
LoginHelper.isSuperAdmin()       // boolean
LoginHelper.isTenantAdmin()      // boolean
LoginHelper.isLogin()            // boolean
```

### **StpUtil方法**

```java
// Token操作
StpUtil.getTokenValue()          // 获取Token字符串
StpUtil.getTokenTimeout()        // 获取剩余有效时间
StpUtil.checkLogin()             // 验证是否登录（抛异常）
StpUtil.isLogin()                // 验证是否登录（返回boolean）

// 登出操作
StpUtil.logout()                 // 当前设备登出
StpUtil.logout(userId)           // 踢出指定用户所有设备
StpUtil.kickout(userId, device)  // 踢出指定设备

// 权限操作
StpUtil.checkPermission("perm")  // 验证权限
StpUtil.hasPermission("perm")    // 检查权限
StpUtil.checkRole("role")        // 验证角色
```

---

## 🎯 最佳实践

### ✅ **DO - 推荐做法**

```java
// 1. 使用LoginHelper获取用户信息
Long userId = LoginHelper.getUserId();

// 2. 使用注解做权限控制
@SaCheckPermission("system:user:add")
public R<Void> add(@RequestBody User user) { ... }

// 3. 只在需要时获取完整对象
if (needFullInfo) {
    LoginUser loginUser = LoginHelper.getLoginUser();
}

// 4. 使用try-catch处理未登录
try {
    Long userId = LoginHelper.getUserId();
} catch (NotLoginException e) {
    return R.fail("请先登录");
}
```

### ❌ **DON'T - 不推荐做法**

```java
// ❌ 1. 频繁获取完整对象
for (int i = 0; i < 100; i++) {
    LoginUser user = LoginHelper.getLoginUser();  // 太慢！
}

// ❌ 2. 在Token中存储敏感信息
loginUser.setPassword("123456");  // 危险！

// ❌ 3. 直接操作StpUtil（除非必要）
Object userId = StpUtil.getExtra("userId");  // 使用LoginHelper代替

// ❌ 4. 在Gateway中操作业务
LoginUser user = LoginHelper.getLoginUser();
userMapper.update(user);  // 业务应该在服务层！
```

---

## 🐛 常见问题

### **Q1: 401 Unauthorized错误**
```
原因: Token无效、过期或未传递
解决:
  1. 检查Header: Authorization: Bearer {token}
  2. 检查Token是否过期（expireIn）
  3. 检查clientid是否传递
  4. 查看Redis是否有对应Token
```

### **Q2: LoginHelper.getUserId()返回null**
```
原因: Token验证未通过或Token Session不存在
解决:
  1. 确保已调用StpUtil.checkLogin()
  2. 检查Redis中是否有Token Extra数据
  3. 查看Gateway是否正确转发Token
```

### **Q3: 多个服务Token不一致**
```
原因: Redis配置不同（database不同）
解决:
  1. 统一Redis配置（host、port、database）
  2. 检查application.yml中的redis.database
  3. 确保所有服务使用相同的Redis实例
```

### **Q4: Token无法跨域**
```
原因: CORS配置问题
解决:
  1. Gateway配置CORS允许Authorization头
  2. 前端设置withCredentials（如果需要）
  3. 确保OPTIONS预检请求通过
```

---

## 📚 文档导航

| 文档 | 说明 | 链接 |
|------|------|------|
| **创建与存储详解** | 深入了解Token的技术实现 | [查看](./Sa-Token创建与存储机制详解.md) |
| **流程可视化** | 通过图表快速理解流程 | [查看](./Token创建流程可视化.md) |
| **开发者指南** | 快速上手开发 | [查看](./Sa-Token开发者快速上手指南.md) |
| **架构文档** | 了解整体架构设计 | [查看](./Sa-Token完整技术架构文档.md) |

---

## 🔗 核心文件位置

```
ruoyi-auth/
  └─ src/main/java/org/dromara/auth/
      ├─ controller/TokenController.java          # 登录入口
      └─ service/impl/PasswordAuthStrategy.java   # 密码认证

ruoyi-common/ruoyi-common-satoken/
  └─ src/main/java/org/dromara/common/satoken/
      ├─ utils/LoginHelper.java                   # Token工具类 ⭐
      ├─ core/dao/PlusSaTokenDao.java            # 多级缓存 ⭐
      └─ config/SaTokenConfiguration.java         # Sa-Token配置

ruoyi-gateway/
  └─ src/main/java/org/dromara/gateway/
      └─ filter/AuthFilter.java                   # Token验证拦截器
```

---

## ⚡ 快速诊断命令

```bash
# 1. 查看Redis中的Token
redis-cli --scan --pattern "satoken:login:*"

# 2. 查看指定Token的信息
redis-cli GET "satoken:login:token:{tokenValue}"

# 3. 查看Token Extra
redis-cli GET "satoken:login:extra:{tokenValue}:userId"

# 4. 查看用户的所有Token
redis-cli GET "satoken:login:id:1"

# 5. 查看Token剩余时间
redis-cli TTL "satoken:login:token:{tokenValue}"

# 6. 手动删除Token（强制下线）
redis-cli DEL "satoken:login:token:{tokenValue}"
```

---

**本快速参考提供Token机制的核心知识，帮助开发者快速查找和使用。**

**更多详细信息请参考 [Sa-Token创建与存储机制详解](./Sa-Token创建与存储机制详解.md)**

