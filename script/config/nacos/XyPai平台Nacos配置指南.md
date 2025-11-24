# XyPai 平台 Nacos 配置指南

## 📌 概述

本文档说明如何在 Nacos 配置中心配置 XyPai 平台的各个微服务。

## 🗂️ 配置文件列表

### 公共配置
| 配置文件 | Data ID | Group | 说明 |
|---------|---------|-------|------|
| 通用配置 | `application-common.yml` | DEFAULT_GROUP | 所有服务共享的配置 |
| Gateway 配置 | `ruoyi-gateway.yml` | DEFAULT_GROUP | API 网关配置 |

### XyPai 业务服务配置
| 配置文件 | Data ID | Group | 说明 | 端口 |
|---------|---------|-------|------|------|
| **BFF 聚合层** | `xypai-app-bff.yml` | DEFAULT_GROUP | App 端业务聚合服务 | 9400 |
| 认证服务 | `xypai-auth.yml` | DEFAULT_GROUP | 认证授权服务 | 8200 |
| 用户服务 | `xypai-user.yml` | DEFAULT_GROUP | 用户领域服务 | 9401 |
| 内容服务 | `xypai-content.yml` | DEFAULT_GROUP | 内容领域服务 | 9403 |
| 聊天服务 | `xypai-chat.yml` | DEFAULT_GROUP | 聊天服务 | 9404 |
| 订单服务 | `xypai-order.yml` | DEFAULT_GROUP | 订单服务 | 9405 |
| 支付服务 | `xypai-payment.yml` | DEFAULT_GROUP | 支付服务 | 9406 |
| 通用服务 | `xypai-common.yml` | DEFAULT_GROUP | 通用服务 | 9407 |

## 📋 配置步骤

### 1. 启动 Nacos Server

```bash
# Windows
cd nacos/bin
startup.cmd -m standalone

# Linux/Mac
cd nacos/bin
sh startup.sh -m standalone
```

访问: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

### 2. 创建命名空间

在 Nacos 控制台 → 命名空间 → 新建命名空间：

| 命名空间ID | 命名空间名称 | 说明 |
|-----------|------------|------|
| `dev` | 开发环境 | Development |
| `test` | 测试环境 | Testing |
| `prod` | 生产环境 | Production |

### 3. 上传配置文件

#### 方式一：通过 Nacos 控制台（推荐）

1. 登录 Nacos 控制台
2. 选择命名空间（如 `dev`）
3. 配置管理 → 配置列表 → 点击 "+" 号
4. 填写配置信息：
   - Data ID: `xypai-app-bff.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制 `xypai-app-bff.yml` 的内容
5. 点击"发布"

#### 方式二：通过脚本批量导入

```bash
# 进入配置文件目录
cd script/config/nacos

# 使用 Nacos 提供的导入工具
# 或使用 curl 命令上传
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -d "dataId=xypai-app-bff.yml" \
  -d "group=DEFAULT_GROUP" \
  -d "tenant=dev" \
  -d "content=$(cat xypai-app-bff.yml)"
```

### 4. 配置 Gateway 路由

将 `ruoyi-gateway-routes-xypai.yml` 中的路由配置添加到 Nacos 的 `ruoyi-gateway.yml` 中。

**步骤**：
1. 在 Nacos 中找到 `ruoyi-gateway.yml`
2. 编辑配置
3. 将 XyPai 路由配置追加到 `spring.cloud.gateway.routes` 下
4. 保存并发布

## 🔑 关键配置说明

### xypai-app-bff.yml 关键配置

#### 1. 无数据库配置
```yaml
# BFF 层不连接数据库
# 不需要配置 spring.datasource
```

#### 2. Dubbo Consumer 配置
```yaml
dubbo:
  consumer:
    timeout: 5000      # RPC 超时时间
    retries: 0         # 失败重试次数（建议不重试）
    check: false       # 启动时不检查 Provider
```

#### 3. 推荐算法配置
```yaml
recommendation:
  cache-duration: 5    # 缓存时长（分钟）
  max-distance: 50000  # 最大推荐距离（米）
  weights:
    distance: 0.3      # 距离权重
    activity: 0.3      # 活跃度权重
    skill-match: 0.2   # 技能匹配权重
    fans-count: 0.2    # 粉丝数权重
```

#### 4. Mock 数据开关
```yaml
app:
  bff:
    enable-mock: true  # 开发阶段启用 Mock 数据
```

### Gateway 路由配置

#### xypai-app-bff 路由
```yaml
- id: xypai-app-bff
  uri: lb://xypai-app-bff       # 负载均衡到 xypai-app-bff 服务
  predicates:
    - Path=/xypai-app-bff/**    # 匹配路径
  filters:
    - StripPrefix=1             # 去除第一级前缀
    - RequestRateLimiter        # 限流配置
```

**访问示例**：
```
客户端请求: http://localhost:8080/xypai-app-bff/api/home/feed
Gateway 转发: http://xypai-app-bff:9400/api/home/feed
```

## 📊 配置验证

### 1. 验证配置是否生效

启动服务后查看日志：

```bash
# 查看服务日志
tail -f logs/xypai-app-bff/console.log

# 应该看到：
# - Nacos 配置加载成功
# - Dubbo Consumer 初始化成功
# - 服务注册到 Nacos 成功
```

### 2. 在 Nacos 控制台验证

1. **服务列表**：服务管理 → 服务列表
   - 应该看到 `xypai-app-bff` 服务已注册
   - 实例数 > 0

2. **配置监听**：配置管理 → 监听查询
   - 输入 `xypai-app-bff.yml`
   - 应该看到服务正在监听此配置

### 3. 测试 API 访问

```bash
# 通过 Gateway 访问
curl -X GET "http://localhost:8080/xypai-app-bff/api/home/feed?type=online" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 直接访问服务（开发调试）
curl -X GET "http://localhost:9400/api/home/feed?type=online" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🔧 常见问题

### Q1: 服务启动后无法连接 Nacos
**原因**: Nacos 配置错误或 Nacos 服务未启动
**解决**:
```bash
# 1. 检查 Nacos 是否启动
netstat -ano | findstr 8848

# 2. 检查配置
spring.cloud.nacos.server-addr=localhost:8848
spring.cloud.nacos.username=nacos
spring.cloud.nacos.password=nacos
```

### Q2: 配置更新不生效
**原因**: 配置缓存未刷新
**解决**:
1. 在 Nacos 控制台重新发布配置
2. 重启服务
3. 或使用 `@RefreshScope` 注解动态刷新

### Q3: Gateway 路由 404
**原因**: Gateway 路由配置错误或服务未注册
**解决**:
1. 检查 Nacos 服务列表中是否有 `xypai-app-bff`
2. 检查 Gateway 路由配置
3. 查看 Gateway 日志

### Q4: Dubbo RPC 调用失败
**原因**: Provider 服务未启动或网络不通
**解决**:
```bash
# 1. 检查 Provider 服务是否启动
# 在 Nacos 服务列表中查看 xypai-user、xypai-content 等服务

# 2. 检查 Dubbo 配置
dubbo.registry.address=nacos://localhost:8848

# 3. 查看日志
tail -f logs/xypai-app-bff/console.log | grep -i dubbo
```

## 📝 配置模板

### 新增服务配置模板

创建新的 XyPai 服务时，参考以下模板：

```yaml
# ${service-name}.yml
spring:
  application:
    name: ${service-name}

server:
  port: ${port}

# 如果需要数据库
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/${database}?useSSL=false
          username: root
          password: password

# 如果提供 Dubbo 服务
dubbo:
  protocol:
    name: dubbo
    port: -1
  provider:
    timeout: 3000

# 如果消费 Dubbo 服务
dubbo:
  consumer:
    timeout: 5000
    retries: 0
```

## 🔗 相关文档

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Gateway 文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Apache Dubbo 文档](https://dubbo.apache.org/zh/)
- [向娱拍平台架构总览](../../../向娱拍平台架构总览.md)
- [xypai-app-bff 快速理解](../../../xypai-aggregation/xypai-app-bff/快速理解.md)

---

**最后更新**: 2025-11-23
**维护者**: XyPai 运维团队
