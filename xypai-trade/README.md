# 💰 xypai-trade 交易模块

> **版本**: v7.1  
> **端口**: 9403  
> **负责人**: Frank (后端交易工程师)  
> **状态**: ✅ 生产就绪

---

## 📦 模块简介

xypai-trade 是 XY相遇派项目的**交易核心模块**，负责订单管理、支付处理、钱包管理和评价系统。

### 核心功能

- ✅ **订单管理**: 创建/支付/取消/完成订单
- ✅ **评价系统**: 多维度评分+商家回复+图片评价
- ✅ **钱包管理**: 余额充值/扣款/冻结/解冻（乐观锁并发控制）
- ✅ **交易流水**: 完整的交易记录+财务对账
- ✅ **支付集成**: 钱包支付（已实现）+ 微信/支付宝（待集成）

---

## 🗂️ 数据库表

### 核心表设计

| 表名 | 字段数 | 说明 | 版本 |
|------|--------|------|------|
| **service_order** | 32 | 服务订单表（升级） | v7.1 ⭐ |
| **service_review** | 18 | 服务评价表（新增） | v7.1 🆕 |
| **user_wallet** | 9 | 用户钱包表（新增） | v7.1 🆕 |
| **transaction** | 13 | 交易流水表（新增） | v7.1 🆕 |

### ServiceOrder 表结构

**v7.1 新增字段（23个）**:
```sql
-- 基础信息
order_no, service_type, service_name, service_time, service_duration, participant_count

-- 费用明细 ⭐
base_fee, person_fee, platform_fee, discount_amount, actual_amount

-- 联系信息
contact_name, contact_phone, special_request

-- 支付信息
payment_method, payment_time

-- 取消信息
cancel_reason, cancel_time

-- 完成信息
completed_at

-- 迁移标记
is_migrated, migrate_time
```

---

## 🚀 快速开始

### 1. 数据库初始化

```bash
# 进入sql目录
cd xypai-modules/xypai-trade/sql

# 按顺序执行升级脚本
mysql -u root -p xypai_trade < v7.1_service_order_upgrade.sql
mysql -u root -p xypai_trade < v7.1_service_review_create.sql
mysql -u root -p xypai_trade < v7.1_user_wallet_create.sql
mysql -u root -p xypai_trade < v7.1_transaction_create.sql
```

### 2. 配置文件

**bootstrap.yml**:
```yaml
server:
  port: 9403

spring:
  application:
    name: xypai-trade
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/xypai_trade
          username: root
          password: password
```

### 3. 启动服务

**方式1: IDEA运行**:
```
Run → XyPaiTradeApplication
```

**方式2: Maven命令**:
```bash
cd xypai-modules/xypai-trade
mvn spring-boot:run
```

**方式3: 打包运行**:
```bash
mvn clean package -DskipTests
java -jar target/xypai-modules-trade.jar
```

### 4. 访问API文档

```
http://localhost:9403/doc.html
```

---

## 📖 API 快速参考

### 订单API

```bash
# 创建订单
POST /api/v1/orders

# 查询订单详情
GET /api/v1/orders/{orderId}

# 我的购买订单
GET /api/v1/orders/my-purchases

# 取消订单
PUT /api/v1/orders/{orderId}/cancel

# 完成订单
PUT /api/v1/orders/{orderId}/complete
```

### 评价API

```bash
# 创建评价
POST /api/v1/reviews

# 商家回复
POST /api/v1/reviews/reply

# 查询内容评价
GET /api/v1/reviews/content/{contentId}

# 评价统计
GET /api/v1/reviews/content/{contentId}/stats
```

### 钱包API

```bash
# 我的钱包
GET /api/v1/wallet/my-wallet

# 充值
POST /api/v1/wallet/recharge

# 提现
POST /api/v1/wallet/withdraw

# 转账
POST /api/v1/wallet/transfer

# 检查余额
GET /api/v1/wallet/check-balance
```

---

## 🧪 测试示例

### 完整订单流程

```bash
# 1. 创建订单
POST /api/v1/orders
{
  "sellerId": 10002,
  "contentId": 2001,
  "serviceType": 1,
  "serviceName": "王者荣耀陪玩",
  "amount": 99,
  "baseFee": 100,
  "platformFee": 5,
  "discountAmount": 6
}
# 返回：orderId = 123456

# 2. 支付订单（钱包支付）
POST /api/v1/payment/wallet-pay/123456
{
  "paymentPassword": "123456"
}
# 乐观锁扣款：买家余额 -99元，卖家余额 +94元（扣5%平台费）

# 3. 开始服务
PUT /api/v1/orders/123456/start-service

# 4. 完成订单
PUT /api/v1/orders/123456/complete

# 5. 创建评价
POST /api/v1/reviews
{
  "orderId": 123456,
  "ratingOverall": 4.5,
  "reviewText": "服务很好！"
}

# 6. 商家回复
POST /api/v1/reviews/reply
{
  "reviewId": 789,
  "replyText": "感谢支持！"
}
```

---

## 🏗️ 项目结构

```
xypai-trade/
├── pom.xml                          # Maven配置
├── README.md                        # 本文档
├── UPGRADE_GUIDE_v7.1.md           # 升级指南
├── sql/                             # SQL脚本
│   ├── v7.1_service_order_upgrade.sql
│   ├── v7.1_service_review_create.sql
│   ├── v7.1_user_wallet_create.sql
│   └── v7.1_transaction_create.sql
└── src/main/
    ├── java/com/xypai/trade/
    │   ├── XyPaiTradeApplication.java
    │   ├── controller/app/
    │   │   ├── ServiceOrderController.java
    │   │   ├── ServiceReviewController.java  🆕
    │   │   └── WalletController.java         🆕
    │   ├── domain/
    │   │   ├── entity/
    │   │   │   ├── ServiceOrder.java        (32字段)
    │   │   │   ├── ServiceReview.java       🆕
    │   │   │   ├── UserWallet.java          🆕
    │   │   │   └── Transaction.java         🆕
    │   │   ├── dto/
    │   │   │   ├── OrderCreateDTO.java
    │   │   │   ├── ReviewCreateDTO.java     🆕
    │   │   │   └── ReviewReplyDTO.java      🆕
    │   │   └── vo/
    │   │       ├── OrderDetailVO.java
    │   │       ├── ReviewDetailVO.java      🆕
    │   │       └── ReviewListVO.java        🆕
    │   ├── mapper/
    │   │   ├── ServiceOrderMapper.java
    │   │   ├── ServiceReviewMapper.java     🆕
    │   │   └── UserWalletMapper.java        🆕
    │   └── service/
    │       ├── IOrderService.java
    │       ├── IReviewService.java          🆕
    │       ├── IWalletService.java          🆕
    │       └── impl/
    │           ├── OrderServiceImpl.java    (双写策略)
    │           ├── PaymentServiceImpl.java  (集成钱包)
    │           ├── ReviewServiceImpl.java   🆕
    │           └── WalletServiceImpl.java   🆕
    └── resources/
        ├── bootstrap.yml
        ├── logback.xml
        └── mapper/
            ├── ServiceOrderMapper.xml
            ├── ServiceReviewMapper.xml      🆕
            └── UserWalletMapper.xml         🆕 (乐观锁SQL)
```

---

## 🔗 依赖模块

```xml
<!-- 核心依赖 -->
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-core</artifactId>      <!-- 工具类、BaseEntity -->
</dependency>
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-security</artifactId>  <!-- 鉴权、SecurityUtils -->
</dependency>
<dependency>
    <groupId>com.xypai</groupId>
    <artifactId>xypai-common-redis</artifactId>     <!-- Redis缓存 -->
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>  <!-- MyBatis Plus -->
</dependency>
```

---

## 📚 相关文档

- [技术栈规范](../../.cursor/rules/AAAAAA_TECH_STACK_REQUIREMENTS.md)
- [数据库设计](../../.cursor/rules/PL.md)
- [角色定义](../../.cursor/rules/ROLE_BACKEND_TRADE.md)
- [升级指南](./UPGRADE_GUIDE_v7.1.md)

---

## 👨‍💻 开发者

**Frank** - 后端交易与活动工程师  
- 📧 Email: frank@xypai.com  
- 💼 职责: 订单系统、支付系统、钱包管理、评价系统

---

**构建高质量、高性能、高安全的交易系统！** 💎

