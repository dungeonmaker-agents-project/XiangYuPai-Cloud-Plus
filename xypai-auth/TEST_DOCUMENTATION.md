# XiangYuPai Auth Service - 测试文档 (Test Documentation)

## 📋 测试概览 (Test Overview)

**服务名称**: xypai-auth
**测试版本**: v1.0
**测试环境**: Development
**更新日期**: 2025-11-14

---

## 🎯 测试准备 (Test Preparation)

### 1. 环境要求 (Prerequisites)

```bash
# 1. 启动基础服务
docker-compose up -d mysql redis nacos

# 2. 启动xypai-user服务 (必须先启动!)
cd xypai-user && mvn spring-boot:run

# 3. 启动xypai-auth服务
cd xypai-auth && mvn spring-boot:run

# 4. 验证服务启动
curl http://localhost:9211/actuator/health
```

### 2. 测试工具 (Test Tools)

- **Postman**: API测试
- **Knife4j**: 在线API文档 `http://localhost:9211/doc.html`
- **curl**: 命令行测试
- **JMeter**: 性能测试 (可选)

### 3. 测试数据 (Test Data)

```json
{
  "testUsers": [
    {
      "mobile": "13800138000",
      "countryCode": "+86",
      "password": "password123",
      "nickname": "测试用户1"
    },
    {
      "mobile": "13800138001",
      "countryCode": "+86",
      "password": "test456789",
      "nickname": "测试用户2"
    }
  ]
}
```

---

## 📝 测试用例 (Test Cases)

## 一、登录相关测试 (Login Tests)

### Test Case 1.1: 密码登录 - 成功场景 (Password Login - Success)

**接口**: `POST /auth/login/password`

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/login/password \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000",
    "password": "password123",
    "agreeToTerms": true,
    "clientId": "app",
    "grantType": "app_password"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 7200,
    "userId": "1001",
    "nickname": "测试用户1",
    "avatar": null,
    "isNewUser": false
  }
}
```

**验证点**:
- [ ] HTTP状态码为200
- [ ] code字段为200
- [ ] data.accessToken不为空
- [ ] data.userId正确
- [ ] data.isNewUser为false (老用户)

---

### Test Case 1.2: 密码登录 - 密码错误 (Password Login - Wrong Password)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/login/password \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000",
    "password": "wrongpassword",
    "agreeToTerms": true
  }'
```

**预期响应**:
```json
{
  "code": 401,
  "message": "手机号或密码错误"
}
```

**验证点**:
- [ ] HTTP状态码为200 (业务错误)
- [ ] code字段为401
- [ ] message提示密码错误
- [ ] data为null

---

### Test Case 1.3: 密码登录 - 参数验证失败 (Password Login - Validation Error)

**测试场景**:

| 场景 | 参数 | 错误值 | 预期message |
|------|------|--------|------------|
| 手机号为空 | mobile | "" | "手机号不能为空" |
| 手机号格式错误 | mobile | "1234567" | "手机号格式不正确" |
| 密码为空 | password | "" | "密码不能为空" |
| 密码过短 | password | "12345" | "密码长度必须在6-20位之间" |
| 未同意协议 | agreeToTerms | false | "必须同意用户协议" |

---

### Test Case 1.4: SMS登录 - 新用户自动注册 (SMS Login - Auto Registration)

**步骤1: 发送验证码**
```bash
curl -X POST http://localhost:9211/sms/send \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "13900139000",
    "type": "login",
    "region": "+86"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "codeId": "abc123def456",
    "expiresIn": 300,
    "nextSendTime": 60,
    "mobile": "13900139000",
    "code": "123456"  // 仅开发环境返回
  }
}
```

**步骤2: SMS登录**
```bash
curl -X POST http://localhost:9211/auth/login/sms \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13900139000",
    "verificationCode": "123456",
    "agreeToTerms": true,
    "clientId": "app",
    "grantType": "app_sms"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "accessToken": "...",
    "userId": "1002",
    "nickname": "139****9000",  // 自动生成昵称
    "isNewUser": true  // ⭐ 新用户标记
  }
}
```

**验证点**:
- [ ] 验证码发送成功
- [ ] 登录成功返回token
- [ ] **isNewUser为true** (核心验证点)
- [ ] nickname格式为 "138****8000"
- [ ] 数据库中创建了新用户记录

---

### Test Case 1.5: SMS登录 - 老用户登录 (SMS Login - Existing User)

**请求示例**: (使用已注册的手机号)
```bash
curl -X POST http://localhost:9211/auth/login/sms \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000",  // 已注册用户
    "verificationCode": "123456",
    "agreeToTerms": true
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "accessToken": "...",
    "userId": "1001",
    "nickname": "测试用户1",  // 保留原昵称
    "avatar": "https://...",
    "isNewUser": false  // ⭐ 老用户标记
  }
}
```

**验证点**:
- [ ] **isNewUser为false**
- [ ] 返回原有用户信息
- [ ] 不创建新用户记录

---

## 二、短信验证码测试 (SMS Verification Tests)

### Test Case 2.1: 发送验证码 - 频率限制 (Send SMS - Rate Limit)

**测试步骤**:
1. 发送第一次验证码 → 成功
2. 立即发送第二次 → 失败(60秒限制)
3. 等待61秒后发送 → 成功

**第二次请求预期响应**:
```json
{
  "code": 429,
  "message": "验证码发送过于频繁，请59秒后再试"
}
```

**验证点**:
- [ ] 第一次发送成功
- [ ] 60秒内重复发送被拒绝
- [ ] 错误码为429
- [ ] 提示剩余等待时间

---

### Test Case 2.2: 发送验证码 - 每日限制 (Send SMS - Daily Limit)

**测试步骤**:
1. 连续发送11次验证码(每次间隔61秒)
2. 第11次应该被拒绝

**第11次请求预期响应**:
```json
{
  "code": 429,
  "message": "今日发送次数已达上限，请明天再试"
}
```

**验证点**:
- [ ] 前10次成功
- [ ] 第11次被拒绝
- [ ] Redis中记录发送次数
- [ ] 24小时后计数器重置

---

### Test Case 2.3: 验证码过期 (SMS Code Expiration)

**测试步骤**:
1. 发送验证码
2. 等待6分钟
3. 使用该验证码登录

**预期响应**:
```json
{
  "code": 401,
  "message": "验证码已过期，请重新获取"
}
```

**验证点**:
- [ ] 5分钟内验证码有效
- [ ] 5分钟后验证码失效
- [ ] Redis中验证码被自动清除

---

## 三、忘记密码流程测试 (Forgot Password Tests)

### Test Case 3.1: 完整忘记密码流程 (Complete Forgot Password Flow)

**步骤1: 发送重置验证码**
```bash
curl -X POST http://localhost:9211/sms/send \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "13800138000",
    "type": "reset"
  }'
```

**预期响应**: (成功发送)
```json
{
  "code": 200,
  "data": {
    "codeId": "...",
    "code": "123456"  // 开发环境
  }
}
```

---

**步骤2: 验证验证码**
```bash
curl -X POST http://localhost:9211/auth/password/reset/verify \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000",
    "verificationCode": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "验证成功"
}
```

**验证点**:
- [ ] 验证码校验通过
- [ ] Redis中保存验证通过标记(10分钟)
- [ ] 原验证码被删除(一次性使用)

---

**步骤3: 设置新密码**
```bash
curl -X POST http://localhost:9211/auth/password/reset/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000",
    "verificationCode": "123456",
    "newPassword": "newpassword456"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "密码重置成功"
}
```

**验证点**:
- [ ] 密码重置成功
- [ ] Redis验证标记被清除
- [ ] 可以用新密码登录
- [ ] 旧密码无法登录

---

### Test Case 3.2: 忘记密码 - 未注册手机号 (Forgot Password - Unregistered Phone)

**步骤1: 发送验证码 (未注册手机号)**
```bash
curl -X POST http://localhost:9211/sms/send \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "19999999999",  // 未注册
    "type": "reset"
  }'
```

**预期响应**:
```json
{
  "code": 404,
  "message": "该手机号未注册"
}
```

**验证点**:
- [ ] 发送被拒绝
- [ ] 错误码为404
- [ ] 不发送短信

⚠️ **注意**: 此功能需要在SmsController中增强实现

---

### Test Case 3.3: 忘记密码 - 验证码错误 (Forgot Password - Wrong Code)

**步骤2: 验证错误的验证码**
```bash
curl -X POST http://localhost:9211/auth/password/reset/verify \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000",
    "verificationCode": "000000"  // 错误验证码
  }'
```

**预期响应**:
```json
{
  "code": 401,
  "message": "验证码错误，请重新输入"
}
```

---

## 四、支付密码测试 (Payment Password Tests)

### Test Case 4.1: 设置支付密码 - 首次设置 (Set Payment Password - First Time)

**前置条件**: 用户已登录，未设置过支付密码

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/payment-password/set \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5..." \
  -d '{
    "paymentPassword": "123456",
    "confirmPassword": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "支付密码设置成功"
}
```

**验证点**:
- [ ] 设置成功
- [ ] 密码被BCrypt加密存储
- [ ] 两次密码一致性校验通过
- [ ] 后续可以用于支付验证

---

### Test Case 4.2: 设置支付密码 - 两次密码不一致 (Set Payment Password - Mismatch)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/payment-password/set \
  -H "Authorization: Bearer ..." \
  -H "Content-Type: application/json" \
  -d '{
    "paymentPassword": "123456",
    "confirmPassword": "654321"  // 不一致
  }'
```

**预期响应**:
```json
{
  "code": 400,
  "message": "两次输入的密码不一致"
}
```

---

### Test Case 4.3: 修改支付密码 (Update Payment Password)

**前置条件**: 用户已设置支付密码

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/payment-password/update \
  -H "Authorization: Bearer ..." \
  -H "Content-Type: application/json" \
  -d '{
    "oldPaymentPassword": "123456",
    "newPaymentPassword": "654321",
    "confirmPassword": "654321"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "支付密码修改成功"
}
```

**验证点**:
- [ ] 原密码验证通过
- [ ] 新密码设置成功
- [ ] 旧密码无法验证
- [ ] 新密码可以验证通过

---

### Test Case 4.4: 验证支付密码 - 正确密码 (Verify Payment Password - Correct)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/payment-password/verify \
  -H "Authorization: Bearer ..." \
  -H "Content-Type: application/json" \
  -d '{
    "paymentPassword": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "verified": true
  }
}
```

---

### Test Case 4.5: 验证支付密码 - 错误次数锁定 (Verify Payment Password - Lockout)

**测试步骤**:
1. 连续5次输入错误密码
2. 第5次后账号被锁定30分钟

**第1-4次请求预期响应**:
```json
{
  "code": 200,
  "data": {
    "verified": false
  }
}
```

**第5次请求预期响应**:
```json
{
  "code": 429,
  "message": "密码错误次数过多，已锁定30分钟"
}
```

**验证点**:
- [ ] 前4次返回错误但不锁定
- [ ] 第5次触发锁定
- [ ] 锁定期间无法验证
- [ ] 30分钟后自动解锁
- [ ] Redis中记录错误次数

⚠️ **注意**: 此功能需要在xypai-user服务中实现

---

## 五、Token管理测试 (Token Management Tests)

### Test Case 5.1: 刷新Token (Refresh Token)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // 新Token
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 7200
  }
}
```

**验证点**:
- [ ] 返回新的Access Token
- [ ] 新Token可以正常使用
- [ ] Token有效期为2小时

---

### Test Case 5.2: 刷新Token - Token已过期 (Refresh Token - Expired)

**请求示例**: (使用过期的refresh token)
```bash
curl -X POST http://localhost:9211/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "expired_token_here"
  }'
```

**预期响应**:
```json
{
  "code": 401,
  "message": "Refresh Token无效或已过期"
}
```

---

### Test Case 5.3: 登出 (Logout)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**预期响应**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

**验证点**:
- [ ] 登出成功
- [ ] Token被加入黑名单
- [ ] 旧Token无法继续使用
- [ ] 再次请求返回401

---

### Test Case 5.4: 登出后使用Token (Use Token After Logout)

**请求示例**: (使用已登出的token访问需要认证的接口)
```bash
curl -X POST http://localhost:9211/auth/payment-password/set \
  -H "Authorization: Bearer <已登出的token>" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentPassword": "123456",
    "confirmPassword": "123456"
  }'
```

**预期响应**:
```json
{
  "code": 401,
  "message": "Token无效或已过期"
}
```

---

## 六、工具接口测试 (Utility Tests)

### Test Case 6.1: 检查手机号 - 已注册 (Check Phone - Registered)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/check/phone \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "phoneNumber": "13800138000"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "isRegistered": true
  }
}
```

---

### Test Case 6.2: 检查手机号 - 未注册 (Check Phone - Not Registered)

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/check/phone \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "phoneNumber": "19999999999"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "isRegistered": false
  }
}
```

---

## 七、性能测试 (Performance Tests)

### Test Case 7.1: 登录接口压力测试 (Login Stress Test)

**测试工具**: JMeter
**测试参数**:
- 并发用户: 100
- 测试时间: 60秒
- 目标QPS: 500

**验证指标**:
- [ ] 响应时间P95 < 200ms
- [ ] 响应时间P99 < 500ms
- [ ] 错误率 < 1%
- [ ] TPS >= 500

---

### Test Case 7.2: Token验证性能 (Token Validation Performance)

**目标**: 验证Token响应时间 < 50ms

**测试方法**:
```bash
# 使用ab工具
ab -n 1000 -c 10 \
  -H "Authorization: Bearer <token>" \
  http://localhost:9211/auth/payment-password/verify
```

**验证指标**:
- [ ] 平均响应时间 < 50ms
- [ ] 99%请求 < 100ms

---

## 八、安全测试 (Security Tests)

### Test Case 8.1: SQL注入测试 (SQL Injection Test)

**测试场景**: 尝试在手机号字段注入SQL

**请求示例**:
```bash
curl -X POST http://localhost:9211/auth/login/password \
  -H "Content-Type: application/json" \
  -d '{
    "countryCode": "+86",
    "mobile": "13800138000 OR 1=1--",
    "password": "password123",
    "agreeToTerms": true
  }'
```

**预期结果**:
- [ ] 请求被拒绝(参数验证失败)
- [ ] 不执行任何数据库操作
- [ ] 返回400错误

---

### Test Case 8.2: XSS攻击测试 (XSS Attack Test)

**测试场景**: 尝试在昵称字段注入XSS

**预期结果**:
- [ ] 特殊字符被转义
- [ ] 不执行JavaScript代码

---

### Test Case 8.3: 密码存储安全 (Password Storage Security)

**验证步骤**:
1. 创建用户并设置密码
2. 查看数据库中的密码字段

**验证点**:
- [ ] 密码使用BCrypt加密
- [ ] 密码哈希值无法反向解密
- [ ] 每个密码的salt不同

---

## 九、集成测试 (Integration Tests)

### Test Case 9.1: 完整用户流程 (Complete User Flow)

**测试流程**:
1. SMS登录(自动注册) → 获得token, isNewUser=true
2. 设置密码 → 成功
3. 登出 → 成功
4. 密码登录 → 成功, isNewUser=false
5. 设置支付密码 → 成功
6. 验证支付密码 → 成功
7. 修改支付密码 → 成功
8. 刷新Token → 成功
9. 登出 → 成功

**验证点**:
- [ ] 全流程无错误
- [ ] isNewUser状态正确转换
- [ ] Token在整个流程中正常工作
- [ ] 所有密码操作成功

---

### Test Case 9.2: 跨服务调用测试 (Cross-Service Call Test)

**测试场景**: 验证xypai-auth与xypai-user的Dubbo RPC调用

**验证点**:
- [ ] RemoteAppUserService调用成功
- [ ] 用户数据正确返回
- [ ] 密码验证正确
- [ ] 服务超时处理正确

---

## 十、异常测试 (Exception Tests)

### Test Case 10.1: 服务不可用 (Service Unavailable)

**测试步骤**:
1. 停止xypai-user服务
2. 调用登录接口

**预期结果**:
- [ ] 返回500错误
- [ ] 错误信息清晰
- [ ] 不泄露系统信息

---

### Test Case 10.2: Redis不可用 (Redis Unavailable)

**测试步骤**:
1. 停止Redis服务
2. 发送验证码

**预期结果**:
- [ ] 返回500错误
- [ ] 提示"服务暂时不可用"

---

### Test Case 10.3: 数据库不可用 (Database Unavailable)

**测试步骤**:
1. 停止MySQL服务
2. 调用登录接口

**预期结果**:
- [ ] 返回500错误
- [ ] 系统优雅降级
- [ ] 不影响其他服务

---

## 📊 测试报告模板 (Test Report Template)

### 测试摘要 (Test Summary)

| 项目 | 数量/结果 |
|------|----------|
| 测试用例总数 | XX |
| 通过用例数 | XX |
| 失败用例数 | XX |
| 跳过用例数 | XX |
| 通过率 | XX% |
| 覆盖率 | XX% |

### 失败用例分析 (Failed Cases Analysis)

| 用例ID | 用例名称 | 失败原因 | 优先级 | 状态 |
|--------|---------|---------|--------|------|
| TC-X.X | XXX | XXX | 高/中/低 | 待修复/已修复 |

### 性能测试结果 (Performance Test Results)

| 接口 | QPS | 平均响应时间 | P95 | P99 | 错误率 |
|------|-----|------------|-----|-----|--------|
| /auth/login/password | XXX | XXms | XXms | XXms | X% |

---

## ✅ 测试检查清单 (Test Checklist)

### 功能测试 (Functional Tests)
- [ ] 所有API端点测试通过
- [ ] 参数验证测试通过
- [ ] 错误处理测试通过
- [ ] 业务逻辑测试通过

### 安全测试 (Security Tests)
- [ ] 身份认证测试通过
- [ ] 权限控制测试通过
- [ ] SQL注入防护验证
- [ ] XSS攻击防护验证
- [ ] 密码加密验证

### 性能测试 (Performance Tests)
- [ ] 响应时间达标
- [ ] 吞吐量达标
- [ ] 并发测试通过
- [ ] 压力测试通过

### 集成测试 (Integration Tests)
- [ ] 跨服务调用测试通过
- [ ] 完整业务流程测试通过
- [ ] 异常场景测试通过

---

## 📝 测试执行记录 (Test Execution Record)

**测试人员**: _______________
**测试日期**: _______________
**测试环境**: Development / Staging / Production
**测试版本**: v1.0

**总结**:
```
测试执行情况：
- 计划测试用例：XX个
- 实际执行：XX个
- 通过：XX个
- 失败：XX个
- 阻塞：XX个

主要问题：
1. XXX
2. XXX

建议：
1. XXX
2. XXX
```

---

**文档维护**: Claude AI Assistant
**最后更新**: 2025-11-14
**文档版本**: v1.0
