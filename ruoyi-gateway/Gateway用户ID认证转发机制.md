# Gateway 用户 ID 认证转发机制

## 📌 核心功能
Gateway 在认证用户后，将用户 ID 从 Token 中提取并转发给下游微服务，解决微服务无法获取用户身份的问题。

## 🏗️ 技术架构
```
用户请求 (携带 Token)
    ↓
【Gateway】
    ├─ AuthFilter (认证过滤器, Order: -200)
    │   ├─ 验证 Token 有效性
    │   ├─ 提取 userId 和 clientKey
    │   └─ 存储到 exchange.attributes
    │
    └─ ForwardAuthFilter (转发过滤器, Order: -100)
        ├─ 从 exchange.attributes 读取 userId
        ├─ 添加到请求 Header: X-User-Id, X-Login-Id
        └─ 添加 Same-Token 用于内部服务验证
    ↓
下游微服务 (xypai-user, xypai-auth 等)
    └─ ProfileController.getCurrentUserId()
        ├─ 优先从 LoginHelper.getUserId() 获取
        └─ Fallback: 从 Header X-User-Id 读取
```

## 🔑 关键实现

### 1. AuthFilter - 提取并存储用户信息
**文件**: `ruoyi-gateway/src/main/java/org/dromara/gateway/filter/AuthFilter.java`

```java
// 在 Sa-Token 认证成功后，提取用户 ID
Object loginIdObj = StpUtil.getLoginId();
String userId = loginIdObj != null ? loginIdObj.toString() : null;

// 存储到 exchange attributes，供 ForwardAuthFilter 使用
if (userId != null) {
    SaReactorSyncHolder.getExchange()
        .getAttributes()
        .put("X-User-Id", userId);
}

// 同样存储 clientKey
if (clientId != null) {
    SaReactorSyncHolder.getExchange()
        .getAttributes()
        .put("X-Client-Key", clientId);
}
```

**为什么这样设计？**
- AuthFilter 在 Sa-Token 认证流程内，`StpUtil.getLoginId()` 可正常使用
- Exchange attributes 是 Spring WebFlux 在过滤器间传递数据的标准方式

### 2. ForwardAuthFilter - 读取并转发
**文件**: `ruoyi-gateway/src/main/java/org/dromara/gateway/filter/ForwardAuthFilter.java`

```java
// 从 exchange attributes 读取 userId（避免 SaTokenContext 未初始化的问题）
Object userIdAttr = exchange.getAttributes().get("X-User-Id");
String userId = userIdAttr != null ? userIdAttr.toString() : null;

Object clientKeyAttr = exchange.getAttributes().get("X-Client-Key");
String clientKey = clientKeyAttr != null ? clientKeyAttr.toString() : null;

// 添加到请求头
ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
    .header(SaSameUtil.SAME_TOKEN, sameToken);

if (userId != null) {
    requestBuilder.header("X-User-Id", userId);       // 主要 Header
    requestBuilder.header("X-Login-Id", userId);      // 兼容性 Header
}

if (clientKey != null) {
    requestBuilder.header("X-Client-Key", clientKey);
}
```

**为什么不直接调用 `StpUtil.getLoginId()`？**
- ForwardAuthFilter 在认证流程外运行，会抛出异常：`SaTokenContext 上下文尚未初始化`
- 使用 exchange attributes 传递数据，避免了 WebFlux 环境的上下文问题

### 3. 微服务接收 - ProfileController
**文件**: `xypai-user/src/main/java/org/dromara/user/controller/app/ProfileController.java`

```java
private Long getCurrentUserId(HttpServletRequest request) {
    // 优先从 Sa-Token 获取
    Long userId = LoginHelper.getUserId();

    // Fallback: 从 Gateway 传递的 Header 读取
    if (userId == null) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            userId = Long.parseLong(userIdHeader);
        }
    }

    return userId;
}
```

**双重获取策略**：
- 优先使用 `LoginHelper.getUserId()`（适用于已登录会话）
- Fallback 使用 Header（适用于 Gateway 转发的请求）

## 🔥 技术亮点

1. **WebFlux 响应式适配**：使用 exchange attributes 在过滤器间传递数据，避免 Servlet API 的限制
2. **双重 Fallback 机制**：微服务优先使用 Sa-Token 会话，失败时从 Header 读取
3. **过滤器顺序控制**：AuthFilter (Order: -200) → ForwardAuthFilter (Order: -100)，确保先认证再转发
4. **兼容性设计**：同时添加 `X-User-Id` 和 `X-Login-Id` 两个 Header，适配不同微服务

## 🐛 曾遇到的问题

### 问题 1：微服务接收到 userId=null
**原因**：ForwardAuthFilter 直接调用 `StpUtil.getLoginId()` 失败
**解决**：通过 exchange attributes 从 AuthFilter 传递数据

### 问题 2：SaTokenContext 上下文未初始化
**原因**：ForwardAuthFilter 在 Sa-Token 认证流程外运行
**解决**：在 AuthFilter 中提取数据并存储，ForwardAuthFilter 只负责读取和转发

## 📌 注意事项

- **过滤器顺序很重要**：AuthFilter 必须先于 ForwardAuthFilter 执行
- **Header 名称规范**：使用 `X-User-Id` 作为主要 Header，`X-Login-Id` 用于兼容性
- **微服务需适配**：所有需要用户 ID 的 Controller 都应使用 `getCurrentUserId()` 方法
- **安全性**：Same-Token 机制确保只有内部服务能调用（防止外网绕过 Gateway）

## 🚀 验证方法

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:8080/xypai-auth/api/v1/auth/login/sms \
  -H "Content-Type: application/json" \
  -d '{"mobile":"13800000001","countryCode":"+86","verificationCode":"123456"}'

# 2. 使用 Token 访问需要认证的接口
curl -X GET http://localhost:8080/xypai-user/api/user/profile/header \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 3. 检查 Gateway 日志
# 应该看到：
#   [AUTH-FILTER] 用户ID: 1763537406687
#   [FORWARD-AUTH-FILTER] ✅ 从 exchange attributes 读取 userId: 1763537406687
#   [FORWARD-AUTH-FILTER] ✅ 已添加用户ID到请求头: X-User-Id = 1763537406687

# 4. 检查微服务日志
# ProfileController 应该成功获取 userId
```

## 📚 相关文件

- `ruoyi-gateway/src/main/java/org/dromara/gateway/filter/AuthFilter.java` - Token 认证和 userId 提取
- `ruoyi-gateway/src/main/java/org/dromara/gateway/filter/ForwardAuthFilter.java` - userId 转发到下游服务
- `xypai-user/src/main/java/org/dromara/user/controller/app/ProfileController.java` - 微服务接收 userId 示例
- `xypai-auth/src/main/java/org/dromara/xypai/auth/service/impl/AppSmsAuthStrategy.java` - Token 生成逻辑
