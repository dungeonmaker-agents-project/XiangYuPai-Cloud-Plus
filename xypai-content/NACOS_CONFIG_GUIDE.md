# XyPai-Content Nacos 配置指南

## 📋 问题说明

在本地 `application.yml` 中使用占位符（如 `${spring.datasource.type}`）会导致配置解析失败，因为：
1. **配置加载时机**：本地配置在 Nacos 配置之前被解析
2. **占位符解析**：Spring Boot 在绑定属性时，本地配置中的占位符无法引用 Nacos 的配置

## ✅ 解决方案：将服务专属配置放到 Nacos

### 步骤1：在 Nacos 中创建 `xypai-content.yml`

**访问 Nacos 控制台**：http://localhost:8848/nacos

**创建配置**：
- **Data ID**: `xypai-content.yml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**:

```yaml
# xypai-content 微服务专属配置
spring:
  datasource:
    dynamic:
      datasource:
        # 主库数据源（使用 xypai_content 数据库）
        master:
          type: ${spring.datasource.type}
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: ${datasource.xypai-content-master.url}
          username: ${datasource.xypai-content-master.username}
          password: ${datasource.xypai-content-master.password}

# WebSocket 配置
websocket:
  enabled: true
  path: /ws
  allowed-origins: "*"

# Dubbo配置
dubbo:
  protocol:
    name: dubbo
    port: -1
```

### 步骤2：验证 Nacos 公共配置

确认 `application-common.yml` 中包含以下配置（您已经有了）：

```yaml
datasource:
  xypai-content-master:
    url: jdbc:mysql://localhost:3306/xypai_content?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true
    username: root
    password: ruoyi123

spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    dynamic:
      p6spy: true
      seata: ${seata.enabled}
      strict: true
      hikari:
        maxPoolSize: 20
        minIdle: 10
        connectionTimeout: 30000
        validationTimeout: 5000
        idleTimeout: 600000
        maxLifetime: 1800000
        keepaliveTime: 30000
```

### 步骤3：重启服务

```bash
# 重启 xypai-content 服务
```

## 📊 配置加载顺序

```
1. application.yml (本地 - 基础配置)
   ├── 服务端口
   ├── 应用名称
   └── Nacos 连接信息

2. application-common.yml (Nacos - 公共配置)
   ├── 数据源连接信息 (datasource.*)
   ├── Redis 配置
   ├── 全局 Hikari 配置
   └── 其他公共配置

3. xypai-content.yml (Nacos - 服务专属配置)
   ├── 数据源配置 (引用 common 中的变量)
   ├── WebSocket 配置
   └── Dubbo 配置
```

## 🎯 优势

✅ **集中管理**：所有业务配置在 Nacos 统一管理  
✅ **动态刷新**：Nacos 配置变更无需重启服务  
✅ **环境隔离**：通过 namespace 实现 dev/test/prod 环境隔离  
✅ **占位符正确解析**：Nacos 内部配置可以互相引用  
✅ **符合微服务最佳实践**：配置中心统一管理

## 🔍 验证配置是否生效

启动服务后，查看日志：

```log
2025-11-10 12:44:13 [main] INFO  c.a.c.n.c.NacosConfigDataLoader
 - [Nacos Config] Load config[dataId=application-common.yml, group=DEFAULT_GROUP] success

2025-11-10 12:44:13 [main] INFO  c.a.c.n.c.NacosConfigDataLoader
 - [Nacos Config] Load config[dataId=xypai-content.yml, group=DEFAULT_GROUP] success
```

如果看到 `is empty` 警告，说明 Nacos 中还没有创建该配置文件。

## 📝 注意事项

1. **Nacos 配置优先级更高**：Nacos 配置会覆盖本地配置
2. **数据库密码**：确认 Nacos 中的数据库密码正确（当前为 `ruoyi123`）
3. **namespace**：确保 Nacos 配置和服务在同一 namespace（`dev`）
4. **group**：确保配置组一致（`DEFAULT_GROUP`）

## 🆚 对比：本地配置 vs Nacos 配置

| 项目 | 本地配置 | Nacos 配置 |
|------|---------|-----------|
| 占位符解析 | ❌ 无法引用 Nacos 变量 | ✅ 可以互相引用 |
| 动态刷新 | ❌ 需要重启 | ✅ 支持热更新 |
| 集中管理 | ❌ 分散在各服务 | ✅ 统一管理 |
| 环境隔离 | ❌ 需要多套配置文件 | ✅ 通过 namespace |
| 版本管理 | ❌ 依赖 Git | ✅ Nacos 自带版本管理 |

## 🚀 其他 XyPai 模块

同样的配置方式适用于其他模块：
- `xypai-user.yml`
- `xypai-trade.yml`
- `xypai-chat.yml`
- `xypai-security.yml`

每个模块都应该：
1. 本地只保留基础配置
2. 业务配置放到 Nacos 对应的配置文件中
3. 引用 `application-common.yml` 中的公共变量

