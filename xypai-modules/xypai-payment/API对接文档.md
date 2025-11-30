# XyPai-Payment 支付服务 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-28
>
> **服务端口**: 9406
>
> **接口前缀**: `/api/payment/`

---

## 目录

1. [通用说明](#通用说明)
2. [支付执行接口](#支付执行接口)
3. [余额查询接口](#余额查询接口)
4. [支付方式接口](#支付方式接口)
5. [错误码说明](#错误码说明)
6. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（直连服务）
http://localhost:9406/api/payment/

# 生产环境（通过网关）
http://gateway:8080/xypai-payment/api/payment/
```

### 认证方式

所有接口必须在请求头中携带 Token：

```http
Authorization: Bearer <access_token>
```

### 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 支付方式

| 类型 | 说明 |
|------|------|
| balance | 余额支付（需要支付密码） |
| alipay | 支付宝（跳转支付宝支付） |
| wechat | 微信支付（调起微信支付） |

### 支付状态

| 状态值 | 说明 |
|--------|------|
| success | 支付成功 |
| pending | 待支付 |
| require_password | 需要输入支付密码 |
| failed | 支付失败 |

### 支付流程

```
1. 用户点击"立即支付" → POST /api/payment/pay（不带密码）
2. 后端返回 requirePassword=true → 前端显示密码输入弹窗
3. 用户输入6位密码 → POST /api/payment/verify
4. 支付成功 → 跳转支付成功页面
```

---

## 支付执行接口

### 1. 执行支付

创建支付请求，执行订单支付。

**请求**

```http
POST /api/payment/pay
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "orderId": "1234567890",
  "orderNo": "ORDER20251128153045123456",
  "paymentMethod": "balance",
  "amount": 10.50,
  "paymentPassword": null
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单ID |
| orderNo | string | 是 | 订单编号 |
| paymentMethod | string | 是 | 支付方式: `balance`, `alipay`, `wechat` |
| amount | decimal | 是 | 支付金额，必须>0 |
| paymentPassword | string | 否 | 支付密码（余额支付时可选，不传则返回需要密码） |

**响应示例 - 需要密码**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "require_password",
    "requirePassword": true,
    "balance": null,
    "failureReason": null
  }
}
```

**响应示例 - 支付成功（带密码）**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "success",
    "requirePassword": false,
    "balance": 89.50,
    "failureReason": null
  }
}
```

**响应示例 - 支付失败**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "failed",
    "requirePassword": false,
    "balance": null,
    "failureReason": "余额不足"
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | string | 订单ID |
| orderNo | string | 订单编号 |
| paymentStatus | string | 支付状态: `success`, `pending`, `require_password`, `failed` |
| requirePassword | boolean | 是否需要输入支付密码 |
| balance | decimal | 支付后用户余额（仅支付成功时返回） |
| failureReason | string | 失败原因（仅支付失败时返回） |

**常见失败原因**

| failureReason | 说明 |
|---------------|------|
| 余额不足 | 用户余额小于支付金额 |
| 支付密码错误 | 支付密码验证失败 |
| 订单已失效 | 订单超时自动取消 |
| 订单已支付 | 订单已被支付，防止重复支付 |

---

### 2. 验证支付密码

验证支付密码并完成支付（两步支付流程的第二步）。

**请求**

```http
POST /api/payment/verify
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "orderId": "1234567890",
  "orderNo": "ORDER20251128153045123456",
  "paymentPassword": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单ID |
| orderNo | string | 是 | 订单编号 |
| paymentPassword | string | 是 | 支付密码（6位数字） |

**响应示例 - 验证成功**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "success",
    "requirePassword": false,
    "balance": 89.50,
    "failureReason": null
  }
}
```

**响应示例 - 密码错误**

```json
{
  "code": 500,
  "msg": "支付密码错误，还剩4次机会",
  "data": null
}
```

**密码安全机制**

| 机制 | 说明 |
|------|------|
| BCrypt加密 | 密码存储使用 BCrypt 加密 |
| 错误次数限制 | 连续错误5次锁定账户 |
| 锁定时长 | 锁定30分钟后自动解锁 |
| Redis计数 | 错误次数使用 Redis 记录 |

---

## 余额查询接口

### 查询用户余额

获取当前用户的账户余额信息。

**请求**

```http
GET /api/payment/balance
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "balance": 100.00,
    "frozenBalance": 0.00,
    "availableBalance": 100.00
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| balance | decimal | 总余额 |
| frozenBalance | decimal | 冻结金额（订单创建时冻结） |
| availableBalance | decimal | 可用余额（balance - frozenBalance） |

**余额冻结机制**

```
订单创建 → 冻结订单金额（availableBalance 减少，frozenBalance 增加）
支付成功 → 扣除冻结金额（frozenBalance 减少）
订单取消 → 解冻金额（frozenBalance 减少，availableBalance 增加）
```

---

## 支付方式接口

### 获取支付方式列表

获取当前用户可用的支付方式。

**请求**

```http
GET /api/payment/methods
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "methods": [
      {
        "type": "balance",
        "name": "余额支付",
        "icon": "https://cdn.example.com/icons/balance.png",
        "enabled": true,
        "requirePassword": true,
        "balance": 100.00
      },
      {
        "type": "alipay",
        "name": "支付宝",
        "icon": "https://cdn.example.com/icons/alipay.png",
        "enabled": true,
        "requirePassword": false,
        "balance": null
      },
      {
        "type": "wechat",
        "name": "微信支付",
        "icon": "https://cdn.example.com/icons/wechat.png",
        "enabled": true,
        "requirePassword": false,
        "balance": null
      }
    ]
  }
}
```

**支付方式字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| type | string | 支付方式类型 |
| name | string | 显示名称 |
| icon | string | 图标URL |
| enabled | boolean | 是否可用 |
| requirePassword | boolean | 是否需要密码（余额支付为true） |
| balance | decimal | 余额（仅余额支付显示） |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 无权限操作 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 业务错误信息

| 错误信息 | 说明 |
|----------|------|
| 支付密码错误 | 密码验证失败 |
| 支付密码错误，还剩N次机会 | 密码错误，提示剩余次数 |
| 账户已锁定，请30分钟后重试 | 密码错误5次后锁定 |
| 余额不足 | 可用余额小于支付金额 |
| 订单不存在 | 订单ID无效 |
| 订单已失效 | 订单超时取消 |
| 订单已支付 | 重复支付 |
| 支付金额不匹配 | 金额篡改检测 |

---

## 集成测试用例

### 测试环境配置

```
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:9211 (认证服务)
xypai-order:   http://localhost:9405 (订单服务)
xypai-payment: http://localhost:9406 (支付服务)
```

**依赖服务**: Nacos, Redis, MySQL, Seata

---

### 测试场景1: 支付弹窗页面 (Page14_PaymentModalFlowTest)

测试支付弹窗页面的完整流程。

#### 1.1 两步支付流程 - 第一步（需要密码）

```java
// 接口: POST /xypai-payment/api/payment/pay
// 请求头: Authorization: Bearer {token}
Map<String, Object> paymentRequest = new HashMap<>();
paymentRequest.put("orderId", "1234567890");
paymentRequest.put("orderNo", "ORDER20251128153045123456");
paymentRequest.put("paymentMethod", "balance");
paymentRequest.put("amount", new BigDecimal("10.50"));
paymentRequest.put("paymentPassword", null); // 不传密码

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "require_password",
    "requirePassword": true
  }
}
// → 前端显示密码输入弹窗
```

#### 1.2 两步支付流程 - 第二步（验证密码）

```java
// 接口: POST /xypai-payment/api/payment/verify
Map<String, Object> verifyRequest = new HashMap<>();
verifyRequest.put("orderId", "1234567890");
verifyRequest.put("orderNo", "ORDER20251128153045123456");
verifyRequest.put("paymentPassword", "123456"); // 6位数字密码

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "success",
    "requirePassword": false,
    "balance": 89.50
  }
}
// → 前端跳转支付成功页面
```

#### 1.3 一步支付流程（带密码）

```java
// 接口: POST /xypai-payment/api/payment/pay
Map<String, Object> paymentRequest = new HashMap<>();
paymentRequest.put("orderId", "1234567890");
paymentRequest.put("orderNo", "ORDER20251128153045123456");
paymentRequest.put("paymentMethod", "balance");
paymentRequest.put("amount", new BigDecimal("10.50"));
paymentRequest.put("paymentPassword", "123456"); // 直接传密码

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "success",
    "requirePassword": false,
    "balance": 89.50
  }
}
```

#### 1.4 密码错误

```java
// 接口: POST /xypai-payment/api/payment/verify
Map<String, Object> verifyRequest = new HashMap<>();
verifyRequest.put("orderId", "1234567890");
verifyRequest.put("orderNo", "ORDER20251128153045123456");
verifyRequest.put("paymentPassword", "wrong123"); // 错误密码

// 响应 (预期错误)
{
  "code": 500,
  "msg": "支付密码错误，还剩4次机会"
}
// → 前端清空密码输入框，显示错误提示
```

#### 1.5 余额不足

```java
// 接口: POST /xypai-payment/api/payment/pay
Map<String, Object> paymentRequest = new HashMap<>();
paymentRequest.put("orderId", "1234567890");
paymentRequest.put("orderNo", "ORDER20251128153045123456");
paymentRequest.put("paymentMethod", "balance");
paymentRequest.put("amount", new BigDecimal("5000.00")); // 超过余额
paymentRequest.put("paymentPassword", "123456");

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "paymentStatus": "failed",
    "requirePassword": false,
    "failureReason": "余额不足"
  }
}
// → 前端显示"余额不足，请先充值"
```

---

### 测试场景2: 查询接口测试 (PaymentServiceIntegrationTest)

#### 2.1 获取支付方式列表

```java
// 接口: GET /xypai-payment/api/payment/methods
String methodsUrl = GATEWAY_URL + "/xypai-payment/api/payment/methods";

// 响应
{
  "code": 200,
  "data": {
    "methods": [
      {
        "type": "balance",
        "name": "余额支付",
        "enabled": true,
        "requirePassword": true,
        "balance": 100.00
      },
      {
        "type": "alipay",
        "name": "支付宝",
        "enabled": true,
        "requirePassword": false,
        "balance": null
      },
      {
        "type": "wechat",
        "name": "微信支付",
        "enabled": true,
        "requirePassword": false,
        "balance": null
      }
    ]
  }
}
```

#### 2.2 查询用户余额

```java
// 接口: GET /xypai-payment/api/payment/balance
String balanceUrl = GATEWAY_URL + "/xypai-payment/api/payment/balance";

// 响应
{
  "code": 200,
  "data": {
    "balance": 100.00,
    "frozenBalance": 0.00,
    "availableBalance": 100.00
  }
}
```

---

### 测试场景3: 安全测试 (PaymentSecurityTest)

#### 3.1 未授权访问（预期失败）

```java
// 接口: POST /xypai-payment/api/payment/pay
// 不携带 Authorization 请求头

// 响应 (预期 401)
{
  "code": 401,
  "msg": "未授权，请先登录"
}
```

#### 3.2 支付防重测试

```java
// 同一订单连续发起两次支付请求
// 第一次应成功，第二次应返回"订单已支付"

// 第一次支付
// POST /api/payment/pay → success

// 第二次支付（重复）
// POST /api/payment/pay
{
  "code": 200,
  "data": {
    "paymentStatus": "failed",
    "failureReason": "订单已支付"
  }
}
```

#### 3.3 金额篡改检测

```java
// 用户篡改前端金额，从10.50改为0.01
Map<String, Object> tamperedRequest = new HashMap<>();
tamperedRequest.put("orderId", "1234567890");
tamperedRequest.put("orderNo", "ORDER20251128153045123456");
tamperedRequest.put("paymentMethod", "balance");
tamperedRequest.put("amount", new BigDecimal("0.01")); // 篡改金额
tamperedRequest.put("paymentPassword", "123456");

// 响应 (预期失败)
{
  "code": 400,
  "msg": "支付金额不匹配"
}
```

---

### 运行测试

```bash
# 进入支付服务目录
cd xypai-modules/xypai-payment

# 运行支付弹窗流程测试
mvn test -Dtest=Page14_PaymentModalFlowTest

# 运行支付服务集成测试
mvn test -Dtest=PaymentServiceIntegrationTest

# 运行安全测试
mvn test -Dtest=PaymentSecurityTest

# 运行所有测试
mvn test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL、Seata 已启动
2. 确保 xypai-auth (9211) 服务已启动
3. 确保 xypai-order (9405) 服务已启动
4. 确保 xypai-payment (9406) 服务已启动
5. 确保 Gateway (8080) 已启动

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-28
