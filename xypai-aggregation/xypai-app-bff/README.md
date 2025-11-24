# XyPai App BFF (Backend For Frontend) 聚合服务

## 📌 项目概述

`xypai-app-bff` 是向娱拍平台的 **App 端业务聚合服务**，采用 BFF（Backend For Frontend）架构模式。

### 核心特点

- ✅ **无数据库**: 纯业务编排层，不直接访问数据库
- ✅ **Dubbo RPC**: 通过 Dubbo 调用领域服务获取数据
- ✅ **数据聚合**: 一次请求聚合多个服务数据（n → 1）
- ✅ **推荐算法**: 实现用户推荐、内容推荐等复杂算法
- ✅ **Redis 缓存**: 缓存聚合结果，提升性能

## 🏗️ 服务信息

| 属性 | 值 |
|------|-----|
| **服务名称** | xypai-app-bff |
| **端口** | 9400 |
| **架构层次** | 聚合服务层（BFF） |
| **数据库** | ❌ 无（不直接访问数据库） |
| **依赖服务** | xypai-user (9401), xypai-content (9403) |

## 🔑 核心接口

### 1. 首页用户推荐流

**接口**: `GET /api/home/feed`

**功能**: 返回用户推荐列表（线上/线下用户）

**参数**:
- `type`: online-线上, offline-线下
- `pageNum`: 页码（默认 1）
- `pageSize`: 每页数量（默认 10）
- `cityCode`: 城市代码（可选）
- `latitude`: 纬度（可选）
- `longitude`: 经度（可选）

**当前状态**: ✅ Mock 实现（返回模拟数据）

**通过 Gateway 访问**:
```bash
curl -X GET "http://localhost:8080/xypai-app-bff/api/home/feed?type=online&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🚀 快速开始

### 前置条件

1. **基础设施服务**（必须）:
   - Nacos: http://localhost:8848/nacos
   - Redis: localhost:6379
   - Gateway: localhost:8080

2. **领域服务**（可选，Mock 模式不需要）:
   - xypai-user: localhost:9401
   - xypai-content: localhost:9403

### 启动服务

```bash
# 方式一：Maven 启动
cd xypai-aggregation/xypai-app-bff
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式二：JAR 包启动
mvn clean package -DskipTests
java -jar target/xypai-app-bff.jar --spring.profiles.active=dev

# 方式三：IDE 启动
# 直接运行 XyPaiAppBffApplication.java
```

### 验证服务

```bash
# 1. 检查控制台输出
(♥◠‿◠)ノ゙  XyPai App BFF聚合服务启动成功   ლ(´ڡ`ლ)゙

# 2. 检查 Nacos 注册
访问: http://localhost:8848/nacos
查看服务列表中是否有 xypai-app-bff

# 3. 访问 API 文档
http://localhost:9400/doc.html
```

### 运行测试

```bash
# 确保服务已启动
mvn test -Dtest=AppHomeFeedTest
```

## 🏛️ BFF 架构模式

```
┌─────────────┐
│ App 客户端   │
└──────┬──────┘
       │ REST API
       ↓
┌─────────────┐
│xypai-app-bff│ ← 本服务（聚合层）
│   (9400)    │   - 无数据库
└──────┬──────┘   - 业务编排
       │          - 数据聚合
       │ Dubbo RPC
   ┌───┴────┬─────────┐
   ↓        ↓         ↓
┌──────┐┌──────┐┌──────┐
│user  ││content││order │ ← 领域服务层
│(9401)││(9403) ││(9405)│   - 有数据库
└──┬───┘└──┬───┘└──┬───┘   - 领域逻辑
   │       │       │
   ↓       ↓       ↓
 ┌────────────────────┐
 │      MySQL         │
 └────────────────────┘
```

## 🔄 实现进度

### ✅ 已完成

- [x] Spring Boot 应用启动类
- [x] 服务配置（application.yml）
- [x] Maven 依赖配置
- [x] HomeFeedController（Mock 数据）
- [x] DTO/VO 定义
- [x] 集成测试用例
- [x] 文档完善

### ⏳ 待实现

- [ ] Dubbo RPC 客户端
  - [ ] UserServiceClient
  - [ ] ContentServiceClient
- [ ] 业务编排服务
  - [ ] HomeFeedBizService
  - [ ] RecommendationEngine
- [ ] Redis 缓存集成
- [ ] 其他聚合接口

详见: [实现进度.md](./实现进度.md)

## 📚 相关文档

- [启动说明](./启动说明.md) - 详细的服务启动指南
- [实现进度](./实现进度.md) - 功能实现进度追踪
- [接口映射](./01-首页Feed流页面-接口映射.md) - API 服务映射关系
- [快速理解](../快速理解.md) - 聚合层整体架构说明

## 🐛 常见问题

### Q1: 为什么服务没有数据库？
**A**: BFF 是业务聚合层，职责是编排业务流程和聚合数据，不应该直接访问数据库。所有数据通过 Dubbo RPC 调用领域服务获取。

### Q2: 当前为什么返回 Mock 数据？
**A**: 因为 Dubbo RPC 接口还未实现。当前可以启动服务并测试接口，但返回的是模拟数据。

### Q3: 测试用例为什么失败？
**A**: 如果看到 404 错误，说明服务未启动。请先启动 `xypai-app-bff` 服务，然后再运行测试。

---

**版本**: v1.0.0
**最后更新**: 2025-11-23
**维护团队**: XyPai 后端团队
