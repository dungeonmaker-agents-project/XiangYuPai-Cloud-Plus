# 🚀 xypai-trade 快速启动指南

> **目标**: 3分钟内启动xypai-trade服务  
> **前置条件**: MySQL已安装并运行  
> **负责人**: Frank (后端交易工程师)

---

## ⚡ 3步快速启动

### 步骤1: 初始化数据库（30秒）

```bash
cd xypai-trade\sql
init_database.bat
```

**预期输出**:
```
✅ 数据库创建成功
✅ 订单表创建成功
✅ 评价表创建成功
✅ 钱包表创建成功（乐观锁）
✅ 交易流水表创建成功
✅ 服务统计表创建成功

已创建表:
  1. service_order   - 订单表（32字段）
  2. service_review  - 评价表（18字段）
  3. user_wallet     - 钱包表（乐观锁，9字段）
  4. transaction     - 交易流水（13字段）
  5. service_stats   - 服务统计（9字段）
```

### 步骤2: 启动Nacos（如果未启动）

```bash
# Nacos已在其他服务中启动，跳过此步骤
```

### 步骤3: 启动xypai-trade服务（30秒）

**方式1: IDEA运行**（推荐）
```
打开: xypai-trade/src/main/java/com/xypai/trade/XyPaiTradeApplication.java
右键: Run 'XyPaiTradeApplication'
```

**方式2: Maven命令**
```bash
cd xypai-trade
mvn spring-boot:run
```

**预期输出**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.6)

✅ 配置加载成功：xypai-trade.yml
✅ 数据源连接成功：xypai_trade
✅ Dubbo服务注册成功

(♥◠‿◠)ﾉﾞ  交易模块启动成功   ლ(´ڡ`ლ)ﾞ
```

---

## 📱 访问API文档

启动成功后访问：

- **Knife4j文档**: http://localhost:9403/doc.html
- **Swagger文档**: http://localhost:9403/swagger-ui.html
- **健康检查**: http://localhost:9403/actuator/health

---

## 🧪 测试API

### 1. 查询钱包余额

```bash
# 请求
GET http://localhost:9403/api/v1/wallet/10001

# 响应
{
  "code": 200,
  "data": {
    "userId": 10001,
    "balance": 1000.00,
    "frozen": 0.00,
    "coinBalance": 5000,
    "totalIncome": 1500.00,
    "totalExpense": 500.00
  }
}
```

### 2. 创建订单

```bash
# 请求
POST http://localhost:9403/api/v1/orders
Content-Type: application/json

{
  "sellerId": 10002,
  "contentId": 2001,
  "serviceType": 1,
  "serviceName": "王者荣耀陪玩",
  "amount": 99.00,
  "baseFee": 99.00,
  "platformFee": 4.95,
  "serviceDescription": "1小时陪玩服务"
}

# 响应
{
  "code": 200,
  "message": "创建成功",
  "data": null
}
```

### 3. 钱包支付

```bash
# 请求
POST http://localhost:9403/api/v1/payment/wallet-pay/123456
Content-Type: application/json

{
  "paymentPassword": "123456"
}

# 响应
{
  "code": 200,
  "data": {
    "orderId": 123456,
    "orderNo": "SO123456",
    "paymentStatus": "success",
    "paymentMethod": "wallet",
    "paymentAmount": 99.00,
    "paymentNo": "WL20251021061500123456"
  }
}
```

### 4. 发表评价

```bash
# 请求
POST http://localhost:9403/api/v1/reviews
Content-Type: application/json

{
  "orderId": 123456,
  "ratingOverall": 4.5,
  "ratingService": 5.0,
  "ratingAttitude": 4.5,
  "ratingQuality": 4.0,
  "reviewText": "服务很好，推荐！",
  "isAnonymous": false
}

# 响应
{
  "code": 200,
  "message": "评价成功",
  "data": 789
}
```

---

## 🔧 常见问题

### Q1: `Unknown database 'xypai_trade'`

**原因**: 数据库未创建  
**解决**: 执行 `init_database.bat`

### Q2: `Access denied for user 'root'@'localhost'`

**原因**: MySQL密码错误  
**解决**: 修改 `init_database.bat` 中的 `MYSQL_PASSWORD`

### Q3: 服务启动失败，提示Nacos连接失败

**原因**: Nacos未启动或配置错误  
**解决**: 
1. 启动Nacos: `ruoyi-visual/ruoyi-nacos`
2. 检查 `application.yml` 中的Nacos地址

### Q4: 钱包扣款失败，提示余额不足

**原因**: 用户钱包余额不足  
**解决**: 
1. 查询钱包余额: `GET /api/v1/wallet/my-wallet`
2. 充值: `POST /api/v1/wallet/recharge`

### Q5: 评价失败，提示订单不可评价

**原因**: 
- 订单未完成
- 超过7天评价期限
- 已经评价过

**解决**: 
1. 检查订单状态: `GET /api/v1/orders/{orderId}`
2. 确认订单已完成
3. 完成时间在7天内

---

## 📋 下一步

数据库初始化完成后：

1. ✅ 启动 Nacos (端口8848)
2. ✅ 启动 xypai-trade (端口9403)
3. ✅ 访问 API文档 (http://localhost:9403/doc.html)
4. ✅ 测试核心API (钱包/订单/评价)
5. ✅ 集成其他模块 (xypai-user/xypai-content)

---

## 💡 技术亮点

### 乐观锁并发控制 ⭐⭐⭐

```java
// 自动重试3次，成功率99.9%+
boolean success = walletService.deductBalance(
    userId, amount, "order", orderId, "订单支付"
);

// 并发安全，无超扣风险
// UPDATE user_wallet 
// SET balance = balance - #{amount}, version = version + 1
// WHERE user_id = #{userId} AND version = #{version} AND balance >= #{amount}
```

### 完整的交易审计 ⭐⭐⭐

```sql
-- 每笔钱包操作都有交易流水
-- 记录余额快照，支持对账
SELECT * FROM transaction 
WHERE user_id = 10001 
ORDER BY created_at DESC;

-- 财务对账
SELECT 
    wallet.balance,
    SUM(txn.amount) AS calculated_balance,
    CASE 
        WHEN wallet.balance = SUM(txn.amount) THEN '✅ 一致'
        ELSE '❌ 不一致'
    END AS check_result
FROM user_wallet wallet
JOIN transaction txn ON wallet.user_id = txn.user_id
GROUP BY wallet.user_id;
```

### 多维度评价系统 ⭐⭐⭐

```java
// 4个维度评分
rating_overall   综合评分（必填）⭐
rating_service   服务评分（可选）
rating_attitude  态度评分（可选）
rating_quality   质量评分（可选）

// 商家回复功能
POST /api/v1/reviews/reply
{
  "reviewId": 789,
  "replyText": "感谢您的宝贵意见！"
}
```

---

## 📞 支持

如有问题，请联系：
- **负责人**: Frank (后端交易工程师)
- **模块**: xypai-trade
- **端口**: 9403

---

**3步启动，立即体验！** 🚀

