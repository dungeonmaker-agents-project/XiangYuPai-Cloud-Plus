# XyPai-Trade 数据库初始化指南

> **⚠️ 重要提示**: 主数据库脚本已迁移到 `dev_workspace/team/frank/sql/`  
> **推荐使用**: Frank的标准工作区脚本（更完整，68条测试数据）

---

## 📋 快速开始（推荐方式）⭐

### 使用Frank标准脚本（推荐）

```bash
# 进入Frank的工作目录
cd dev_workspace\team\frank

# 执行一键初始化
init_frank_database.bat

# 自动完成：
# ✅ 创建数据库 xypai_trade
# ✅ 创建5张表（81字段）
# ✅ 创建29个索引
# ✅ 插入68条测试数据
```

**优势**:
- ✅ 测试数据更丰富（68条 vs 16条）
- ✅ 真实业务场景完整覆盖
- ✅ CREATE TABLE方式更清晰
- ✅ 符合团队标准规范

---

## 🔄 手动执行（备选方式）

### 方式1: 使用dev_workspace脚本（推荐）

```bash
# 1. 创建数据库
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS xypai_trade DEFAULT CHARACTER SET utf8mb4;"

# 2. 执行Frank的标准脚本
cd dev_workspace\team\frank\sql
mysql -u root -proot xypai_trade < 02_create_tables.sql
mysql -u root -proot xypai_trade < 03_create_indexes.sql
mysql -u root -proot xypai_trade < 04_init_test_data.sql
```

### 方式2: 使用本地脚本（临时）

```bash
# 1. 创建数据库
mysql -u root -proot < 00_create_database.sql

# 注意：v7.1_脚本已废弃，请使用dev_workspace脚本
```

## 📊 数据库信息

- **数据库名**: `xypai_trade`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`

## 📁 表结构

| 表名 | 字段数 | 说明 | 主要特性 |
|------|--------|------|---------|
| `service_order` | 32 | 服务订单表 | 订单ID、买家、卖家、金额、状态 |
| `service_review` | 18 | 服务评价表 | 评价ID、订单ID、评分、评论 |
| `user_wallet` | 9 | 用户钱包表 | 用户ID、余额、冻结金额、**版本号(乐观锁)** ⭐ |
| `transaction` | 13 | 交易流水表 | 流水ID、用户ID、金额、类型 |
| `service_stats` | 9 | 服务统计表 | 服务ID、服务次数、评分、好评率 |

**总计**: 5张表，81个字段，符合PL.md v7.1规范 ✅

## ⚙️ 配置说明

确保 Nacos 配置中心的 `xypai-trade.yml` 包含正确的数据库配置：

```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://localhost:3306/xypai_trade?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
      username: root
      password: root
```

## 🔧 常见问题

### 1. `Unknown database 'xypai_trade'`
**原因**: 数据库未创建  
**解决**: 执行 `init_database.bat` 或手动创建数据库

### 2. `Access denied for user 'root'@'localhost'`
**原因**: 数据库密码错误  
**解决**: 修改 `init_database.bat` 中的 `MYSQL_PASSWORD` 变量

### 3. `Can't connect to MySQL server`
**原因**: MySQL服务未启动  
**解决**: 启动MySQL服务
```bash
# Windows
net start MySQL80

# 或使用服务管理器启动
```

## 📝 特性说明

### 乐观锁机制
`user_wallet` 表使用 `version` 字段实现乐观锁，防止并发扣款问题：

```java
// 自动重试3次机制
boolean success = walletService.deductBalance(userId, amount, ...);
```

### 索引优化
所有表都包含必要的索引以优化查询性能：
- 主键索引
- 外键索引
- 组合索引（状态+时间）
- 唯一索引（订单号等）

## 🚀 下一步

数据库初始化完成后，启动 `xypai-trade` 服务：

```bash
# 在IDEA中直接运行
XyPaiTradeApplication.java

# 或使用Maven
mvn spring-boot:run
```

服务启动后访问 API文档：
- **Knife4j**: http://localhost:9300/doc.html
- **Swagger**: http://localhost:9300/swagger-ui.html

## 📞 支持

如有问题，请联系：
- **负责人**: Frank (后端交易工程师)
- **模块**: xypai-trade

