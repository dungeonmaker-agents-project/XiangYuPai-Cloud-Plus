# XyPai-Order 订单服务 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-28
>
> **服务端口**: 9405
>
> **接口前缀**: `/api/order/`

---

## 目录

1. [通用说明](#通用说明)
2. [订单预览接口](#订单预览接口)
3. [订单管理接口](#订单管理接口)
4. [错误码说明](#错误码说明)
5. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（直连服务）
http://localhost:9405/api/order/

# 生产环境（通过网关）
http://gateway:8080/xypai-order/api/order/
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

### 订单状态码

| 状态值 | 状态名 | 说明 |
|--------|--------|------|
| 10 | pending | 待支付/待接单 |
| 20 | accepted | 待服务/已接单 |
| 30 | in_progress | 进行中 |
| 40 | to_evaluate | 待评价 |
| 50 | completed | 已完成 |
| 90 | cancelled | 已取消 |
| 91 | refunded | 已退款 |

### 订单类型

| 类型 | 说明 |
|------|------|
| service | 技能服务订单（如游戏陪玩） |
| activity | 活动订单（如组局活动） |

### 订单编号格式

```
ORDER{yyyyMMddHHmmss}{6位随机数}
```
示例：`ORDER20251128153045123456`

---

## 订单预览接口

### 1. 获取订单预览

进入确认订单页面时，获取订单预览信息（包含价格计算和用户余额）。

**请求**

```http
GET /api/order/preview?serviceId={serviceId}&quantity={quantity}
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| serviceId | long | 是 | - | 服务ID |
| quantity | integer | 否 | 1 | 数量，最小1 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "provider": {
      "userId": 2001,
      "avatar": "https://cdn.example.com/avatar/2001.jpg",
      "nickname": "游戏达人",
      "gender": "male",
      "age": 25,
      "tags": ["王者50星", "声音好听"],
      "skillInfo": {
        "gameArea": "王者荣耀",
        "rank": "王者",
        "rankDisplay": "王者50星"
      }
    },
    "service": {
      "serviceId": 101,
      "name": "王者荣耀陪玩",
      "icon": "https://cdn.example.com/icons/wzry.png"
    },
    "price": {
      "unitPrice": 10.00,
      "unit": "局",
      "displayText": "10金币/局"
    },
    "quantityOptions": {
      "min": 1,
      "max": 10,
      "defaultValue": 1
    },
    "preview": {
      "quantity": 1,
      "subtotal": 10.00,
      "serviceFee": 0.50,
      "total": 10.50
    },
    "userBalance": 100.00
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| provider | object | 服务提供者信息 |
| provider.userId | long | 提供者用户ID |
| provider.avatar | string | 头像URL |
| provider.nickname | string | 昵称 |
| provider.gender | string | 性别 |
| provider.age | integer | 年龄 |
| provider.tags | array | 标签列表 |
| provider.skillInfo | object | 技能信息（游戏类） |
| service | object | 服务信息 |
| service.serviceId | long | 服务ID |
| service.name | string | 服务名称 |
| service.icon | string | 服务图标URL |
| price | object | 价格信息 |
| price.unitPrice | decimal | 单价 |
| price.unit | string | 单位（局/小时/次） |
| price.displayText | string | 展示文本 |
| quantityOptions | object | 数量选项 |
| quantityOptions.min | integer | 最小数量 |
| quantityOptions.max | integer | 最大数量 |
| quantityOptions.defaultValue | integer | 默认数量 |
| preview | object | 价格预览 |
| preview.quantity | integer | 当前数量 |
| preview.subtotal | decimal | 小计 |
| preview.serviceFee | decimal | 服务费（5%） |
| preview.total | decimal | 总金额 |
| userBalance | decimal | 用户余额 |

---

### 2. 更新订单预览

用户调整数量时，实时重新计算价格。

**请求**

```http
POST /api/order/preview/update
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "serviceId": 101,
  "quantity": 3
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| serviceId | long | 是 | 服务ID |
| quantity | integer | 是 | 数量，最小1 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "quantity": 3,
    "subtotal": 30.00,
    "serviceFee": 1.50,
    "total": 31.50
  }
}
```

**价格计算规则**

```
subtotal = unitPrice × quantity
serviceFee = subtotal × 5%
total = subtotal + serviceFee
```

---

## 订单管理接口

### 1. 创建订单

用户点击"立即支付"创建订单。

**请求**

```http
POST /api/order/create
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "serviceId": 101,
  "quantity": 1,
  "totalAmount": 10.50
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| serviceId | long | 是 | 服务ID |
| quantity | integer | 是 | 数量，最小1 |
| totalAmount | decimal | 是 | 总金额（用于后端验证，防篡改） |

**响应示例**

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "amount": 10.50,
    "needPayment": true,
    "paymentInfo": {
      "amount": 10.50,
      "currency": "coin",
      "userBalance": 100.00,
      "sufficientBalance": true
    }
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | string | 订单ID |
| orderNo | string | 订单编号 |
| amount | decimal | 订单金额 |
| needPayment | boolean | 是否需要支付 |
| paymentInfo | object | 支付信息 |
| paymentInfo.amount | decimal | 支付金额 |
| paymentInfo.currency | string | 币种（coin=金币） |
| paymentInfo.userBalance | decimal | 用户余额 |
| paymentInfo.sufficientBalance | boolean | 余额是否充足 |

**错误响应 - 金额不匹配**

```json
{
  "code": 400,
  "msg": "金额验证失败，请刷新页面重试",
  "data": null
}
```

---

### 2. 获取订单详情

查询订单详细信息。

**请求**

```http
GET /api/order/detail?orderId={orderId}
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "status": "pending",
    "amount": 10.50,
    "createdAt": "2025-11-28T15:30:45",
    "autoCancelTime": "2025-11-28T15:40:45",
    "provider": {
      "userId": 2001,
      "nickname": "游戏达人",
      "avatar": "https://cdn.example.com/avatar/2001.jpg"
    },
    "service": {
      "name": "王者荣耀陪玩",
      "quantity": 1
    }
  }
}
```

---

### 3. 获取订单状态

查询订单状态和可用操作（用于订单详情页轮询）。

**请求**

```http
GET /api/order/status?orderId={orderId}
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单ID |

**响应示例 - 待接单订单**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "status": "pending",
    "statusLabel": "等待服务者接单",
    "provider": {
      "userId": 2001,
      "nickname": "游戏达人",
      "avatar": "https://cdn.example.com/avatar/2001.jpg",
      "isOnline": true
    },
    "service": {
      "name": "王者荣耀陪玩",
      "quantity": 1,
      "unitPrice": 10.00
    },
    "amount": 10.50,
    "createdAt": "2025-11-28T15:30:45",
    "acceptedAt": null,
    "completedAt": null,
    "cancelledAt": null,
    "autoCancel": {
      "enabled": true,
      "cancelAt": "2025-11-28T15:40:45",
      "remainingSeconds": 580
    },
    "actions": [
      {
        "action": "cancel",
        "label": "取消订单",
        "enabled": true
      }
    ]
  }
}
```

**响应示例 - 已接单订单**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "status": "accepted",
    "statusLabel": "服务者已接单",
    "autoCancel": {
      "enabled": false,
      "cancelAt": null,
      "remainingSeconds": 0
    },
    "actions": [
      {
        "action": "contact",
        "label": "联系服务者",
        "enabled": true
      }
    ]
  }
}
```

**状态标签对照**

| 状态 | statusLabel |
|------|-------------|
| pending | 等待服务者接单 |
| accepted | 服务者已接单 |
| in_progress | 服务进行中 |
| to_evaluate | 待评价 |
| completed | 服务已完成 |
| cancelled | 已取消 |
| refunded | 已退款 |

**可用操作对照**

| 状态 | 可用操作 |
|------|----------|
| pending | cancel（取消订单） |
| accepted | contact（联系服务者） |
| in_progress | contact（联系服务者） |
| to_evaluate | rate（评价） |
| completed | - |
| cancelled | - |

---

### 4. 取消订单

取消订单并处理退款。

**请求**

```http
POST /api/order/cancel
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "orderId": "1234567890",
  "reason": "临时有事"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单ID |
| reason | string | 否 | 取消原因 |

**响应示例**

```json
{
  "code": 200,
  "msg": "取消成功",
  "data": {
    "orderId": "1234567890",
    "status": "cancelled",
    "refundAmount": 10.50,
    "refundTime": "2025-11-28T15:35:00",
    "balance": 100.00
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | string | 订单ID |
| status | string | 订单状态（cancelled） |
| refundAmount | decimal | 退款金额 |
| refundTime | datetime | 退款时间 |
| balance | decimal | 退款后用户余额 |

**错误响应 - 订单状态不允许取消**

```json
{
  "code": 400,
  "msg": "订单状态不允许取消",
  "data": null
}
```

**取消规则**
- 仅 `pending`（待接单）状态的订单可以取消
- 取消后自动触发退款（通过 PaymentService RPC）
- 退款金额返还至用户余额

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 无权限操作 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误信息 | 说明 |
|----------|------|
| 服务不存在或已下架 | 服务ID无效或已下架 |
| 金额验证失败 | 前端传入金额与后端计算不一致（防篡改） |
| 订单状态不允许取消 | 非待接单状态的订单不能取消 |
| 订单不存在 | 订单ID无效 |
| 数量超出限制 | 订单数量超出最大限制 |

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

### 测试场景1: 确认订单页面 (Page13_OrderConfirmationFlowTest)

测试确认订单页面的完整流程。

#### 1.1 页面加载 - 获取订单预览

```java
// 接口: GET /xypai-order/api/order/preview
// 请求头: Authorization: Bearer {token}
String previewUrl = GATEWAY_URL + "/xypai-order/api/order/preview?serviceId=101&quantity=1";

// 响应
{
  "code": 200,
  "data": {
    "provider": {
      "userId": 2001,
      "avatar": "https://cdn.example.com/avatar/2001.jpg",
      "nickname": "游戏达人"
    },
    "service": {
      "serviceId": 101,
      "name": "王者荣耀陪玩"
    },
    "price": {
      "unitPrice": 10.00,
      "unit": "局",
      "displayText": "10金币/局"
    },
    "preview": {
      "quantity": 1,
      "subtotal": 10.00,
      "serviceFee": 0.50,
      "total": 10.50
    },
    "userBalance": 100.00
  }
}
```

#### 1.2 调整数量 - 更新预览

```java
// 接口: POST /xypai-order/api/order/preview/update
Map<String, Object> updateRequest = new HashMap<>();
updateRequest.put("serviceId", 101);
updateRequest.put("quantity", 3);

// 响应
{
  "code": 200,
  "data": {
    "quantity": 3,
    "subtotal": 30.00,
    "serviceFee": 1.50,
    "total": 31.50
  }
}
```

#### 1.3 点击"立即支付" - 创建订单

```java
// 接口: POST /xypai-order/api/order/create
Map<String, Object> createRequest = new HashMap<>();
createRequest.put("serviceId", 101);
createRequest.put("quantity", 1);
createRequest.put("totalAmount", new BigDecimal("10.50"));

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "amount": 10.50,
    "needPayment": true,
    "paymentInfo": {
      "amount": 10.50,
      "currency": "coin",
      "userBalance": 100.00,
      "sufficientBalance": true
    }
  }
}
```

#### 1.4 金额篡改检测（预期失败）

```java
// 接口: POST /xypai-order/api/order/create
// 用户修改前端代码，将金额改为 5.00
Map<String, Object> tamperedRequest = new HashMap<>();
tamperedRequest.put("serviceId", 101);
tamperedRequest.put("quantity", 1);
tamperedRequest.put("totalAmount", new BigDecimal("5.00")); // 应为 10.50

// 响应 (预期错误)
{
  "code": 400,
  "msg": "金额验证失败，请刷新页面重试"
}
```

---

### 测试场景2: 订单详情页面 (Page16_OrderDetailFlowTest)

测试订单详情页面的状态轮询和取消功能。

#### 2.1 获取订单状态（待接单）

```java
// 接口: GET /xypai-order/api/order/status
String statusUrl = GATEWAY_URL + "/xypai-order/api/order/status?orderId=1234567890";

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "ORDER20251128153045123456",
    "status": "pending",
    "statusLabel": "等待服务者接单",
    "autoCancel": {
      "enabled": true,
      "cancelAt": "2025-11-28T15:40:45",
      "remainingSeconds": 580
    },
    "actions": [
      {
        "action": "cancel",
        "label": "取消订单",
        "enabled": true
      }
    ]
  }
}
```

#### 2.2 状态轮询（前端每5秒轮询）

```java
// 前端每5秒调用一次 GET /api/order/status
// 当状态从 pending 变为 accepted 时停止轮询

// Poll 1 (T+0s)
{
  "data": { "status": "pending", "autoCancel": { "remainingSeconds": 580 } }
}

// Poll 2 (T+5s)
{
  "data": { "status": "pending", "autoCancel": { "remainingSeconds": 575 } }
}

// Poll 3 (T+10s) - 服务者接单
{
  "data": { "status": "accepted", "statusLabel": "服务者已接单" }
}
// → 停止轮询
```

#### 2.3 取消订单

```java
// 接口: POST /xypai-order/api/order/cancel
Map<String, Object> cancelRequest = new HashMap<>();
cancelRequest.put("orderId", "1234567890");
cancelRequest.put("reason", "临时有事");

// 响应
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "status": "cancelled",
    "refundAmount": 10.50,
    "refundTime": "2025-11-28T15:35:00",
    "balance": 100.00
  }
}
```

#### 2.4 取消已接单订单（预期失败）

```java
// 接口: POST /xypai-order/api/order/cancel
// 订单已被接单，状态为 accepted

// 响应 (预期错误)
{
  "code": 400,
  "msg": "订单状态不允许取消"
}
```

---

### 测试场景3: 服务费计算测试

验证5%服务费计算的正确性。

```java
// 数量1：10.00 + 0.50 = 10.50
verifyServiceFee(1, 10.00, 0.50, 10.50);

// 数量2：20.00 + 1.00 = 21.00
verifyServiceFee(2, 20.00, 1.00, 21.00);

// 数量5：50.00 + 2.50 = 52.50
verifyServiceFee(5, 50.00, 2.50, 52.50);
```

---

### 运行测试

```bash
# 进入订单服务目录
cd xypai-modules/xypai-order

# 运行确认订单页面测试
mvn test -Dtest=Page13_OrderConfirmationFlowTest

# 运行订单详情页面测试
mvn test -Dtest=Page16_OrderDetailFlowTest

# 运行支付成功流程测试
mvn test -Dtest=Page15_PaymentSuccessFlowTest

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
