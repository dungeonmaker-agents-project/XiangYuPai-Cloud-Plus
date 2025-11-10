# 🧪 完整认证流程手动测试指南

**目的**: 测试完整的 Token 认证流程（Gateway + Auth + Content Service）  
**测试类型**: 集成测试（手动）  
**测试工具**: Postman / cURL

---

## 📋 前置条件检查清单

在开始测试前，请确保以下服务已启动：

```bash
# 1. 基础设施
✅ Nacos   (8848)    - 配置中心 + 注册中心
✅ Redis   (6379)    - Token存储
✅ MySQL   (3306)    - 数据库

# 2. 微服务
✅ Gateway (8080)    - API网关（必须！负责Token验证）
✅ Auth    (9401)    - 认证服务（xypai-auth）
✅ Content (9403)    - 内容服务（xypai-content）
```

**验证方法**:
```bash
# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=ruoyi-gateway
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-auth
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-content
```

---

## 🎯 测试步骤

### 步骤1：登录获取Token

**请求**:
```http
POST http://localhost:9401/api/v1/auth/login
Content-Type: application/json

{
  "username": "13900000001",
  "password": "Test@123456",
  "clientType": "app",
  "deviceId": "test-device-001"
}
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "userId": 2000,
      "username": "app_tester",
      "mobile": "13900000001",
      ...
    }
  }
}
```

**✅ 验证点**:
- [ ] HTTP状态码 200
- [ ] 业务code 200
- [ ] accessToken不为空
- [ ] Token格式为JWT（3段用.分隔）
- [ ] userInfo包含userId、username等信息

**🎯 复制accessToken备用**！

---

### 步骤2：通过Gateway访问公开接口（无需Token）

**目的**: 验证Gateway路由正常

**请求**:
```http
GET http://localhost:8080/xypai-content/api/v2/test/public
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "message": "公开测试接口访问成功",
    "service": "xypai-content",
    "timestamp": "2025-11-08T15:30:00"
  }
}
```

**✅ 验证点**:
- [ ] HTTP状态码 200
- [ ] 业务code 200
- [ ] Gateway → Content Service 路由成功

---

### 步骤3：通过Gateway访问认证接口（需要Token）

**目的**: 验证完整的Token认证流程

**请求**:
```http
GET http://localhost:8080/xypai-content/api/v2/test/auth
Authorization: Bearer <YOUR_ACCESS_TOKEN>
clientid: app
```

**⚠️ 注意事项**:
1. `Authorization` header 格式：`Bearer` + 空格 + Token
2. `clientid` header 必须与登录时的 `clientType` 一致
3. 必须通过Gateway（8080），不要直接访问Content Service（9403）

**预期响应（成功）**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "message": "认证测试接口访问成功",
    "service": "xypai-content",
    "userId": 2000,
    "username": "app_tester",
    "clientId": "app",
    "timestamp": "2025-11-08T15:30:00"
  }
}
```

**✅ 验证点**:
- [ ] HTTP状态码 200
- [ ] 业务code 200
- [ ] 返回了正确的用户信息（userId, username）
- [ ] 完整的认证流程正常工作

---

### 步骤4：验证Token信息

**请求**:
```http
GET http://localhost:8080/xypai-content/api/v2/test/token-info
Authorization: Bearer <YOUR_ACCESS_TOKEN>
clientid: app
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "authenticated": true,
    "userId": 2000,
    "username": "app_tester",
    "clientId": "app",
    "loginId": "app_user:2000",
    "service": "xypai-content",
    "timestamp": "2025-11-08T15:30:00"
  }
}
```

---

## ❌ 常见错误与排查

### 错误1：连接被拒绝

```
Connection refused: localhost:8080
```

**原因**: Gateway未启动

**解决**:
```bash
# 启动Gateway
cd ruoyi-gateway
mvn spring-boot:run

# 或在IDEA中启动 GatewayApplication.java
```

---

### 错误2：Gateway返回401

```json
{"code": 401, "msg": "认证失败，无法访问系统资源", "data": null}
```

**原因**: Token验证失败

**排查步骤**:

1. **检查Token格式**
   ```bash
   # Token应该是 "Bearer <token>"
   # ❌ 错误: Authorization: eyJ0eXAi...
   # ✅ 正确: Authorization: Bearer eyJ0eXAi...
   ```

2. **检查clientId**
   ```bash
   # clientid header 必须与登录时的 clientType 一致
   # 登录时使用: "clientType": "app"
   # API请求时: clientid: app
   ```

3. **检查Token是否过期**
   ```bash
   # Token有效期24小时
   # 如果超时，重新登录获取新Token
   ```

4. **检查Redis连接**
   ```bash
   redis-cli
   > AUTH your-password
   > KEYS satoken:*
   # 应该能看到Sa-Token的key
   ```

---

### 错误3：Content Service未注册到Nacos

```
No available server for client: xypai-content
```

**原因**: Content Service未启动或未注册到Nacos

**解决**:
```bash
# 1. 启动Content Service
cd xypai-content
mvn spring-boot:run

# 2. 检查Nacos注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-content
```

---

### 错误4：JWT解析失败

```
JWT invalid: signature verification failed
```

**原因**: Sa-Token密钥配置不一致

**检查**:
```yaml
# Auth Service 和 Content Service 的 sa-token.jwt-secret-key 必须一致
# 在Nacos配置中检查:
# - application-common.yml
# - xypai-auth.yml
# - xypai-content.yml
```

---

## 🔍 调试技巧

### 1. 使用Postman Collections

创建Postman Collection，保存以下请求：

```
XYPai Token Test
├─ 1. 登录获取Token
├─ 2. 测试公开接口
├─ 3. 测试认证接口
└─ 4. 查看Token信息
```

设置环境变量：
- `{{gateway_url}}` = `http://localhost:8080`
- `{{auth_url}}` = `http://localhost:9401`
- `{{access_token}}` = (从登录响应自动提取)

### 2. 查看Gateway日志

```bash
tail -f logs/ruoyi-gateway/sys-info.log

# 关注以下信息：
# - Sa-Token 验证日志
# - 路由转发日志
# - 错误堆栈
```

### 3. 解析JWT Token

在线工具：https://jwt.io/

将Token粘贴进去，查看Payload：
```json
{
  "loginType": "login",
  "loginId": "app_user:2000",
  "rnStr": "...",
  "clientid": "app",
  "userId": 2000,
  "userName": "app_tester"
}
```

验证：
- `clientid` 是否为 "app"
- `userId` 是否正确
- Token是否过期（查看 `exp` 字段）

---

## ✅ 测试通过标准

所有以下测试均通过，才算完整测试成功：

- [ ] **步骤1**: 登录成功，获取Token
- [ ] **步骤2**: 公开接口访问成功（无需Token）
- [ ] **步骤3**: 认证接口访问成功（需要Token）
- [ ] **步骤4**: Token信息正确
- [ ] **JWT解析**: Token payload包含正确的用户信息
- [ ] **Redis验证**: Token存在于Redis中

---

## 📊 性能测试（可选）

### 1. 并发测试

使用Apache Bench测试：
```bash
# 登录接口（100个请求，10个并发）
ab -n 100 -c 10 -p login.json -T application/json \
   http://localhost:9401/api/v1/auth/login

# 认证接口（使用Token）
ab -n 100 -c 10 -H "Authorization: Bearer <TOKEN>" \
   -H "clientid: app" \
   http://localhost:8080/xypai-content/api/v2/test/auth
```

### 2. 性能指标

- **登录接口**: < 200ms (P95)
- **认证接口**: < 100ms (P95)
- **并发能力**: > 100 QPS

---

## 🎯 总结

### 架构流程回顾

```
前端/Postman
    │
    │ 1. 登录请求
    ▼
Auth Service (9401)
    │
    │ 2. 生成JWT Token
    │ 3. 存储到Redis（可选）
    ▼
前端
    │
    │ 4. 使用Token访问API
    ▼
Gateway (8080)
    │
    │ 5. 验证JWT签名
    │ 6. 检查Token过期时间
    │ 7. 验证clientId
    ▼
Content Service (9403)
    │
    │ 8. 从Token提取用户信息（LoginHelper）
    │ 9. 执行业务逻辑
    ▼
前端
```

### 关键点

1. **所有API请求必须通过Gateway**（8080端口）
2. **Token传递方式**: `Authorization: Bearer <token>` + `clientid: app`
3. **Content Service信任Gateway**：不重复验证Token，直接使用LoginHelper提取信息
4. **JWT Simple Mode**：Token自包含，不依赖Redis验证（但可以存储用于刷新）

---

**测试完成！** 🎉

如有问题，请查看：
- Gateway日志
- Content Service日志
- Nacos服务列表
- Redis中的Token数据

