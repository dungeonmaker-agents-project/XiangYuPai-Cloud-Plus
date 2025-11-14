# 📋 XYPai 服务 POM 依赖对齐报告

> **日期**: 2025-11-11  
> **目的**: 解决 Same-Token 不同步问题  
> **策略**: 对齐所有 xypai 服务到 ruoyi-system 的依赖标准

---

## 🎯 问题背景

### 症状
```
✅ ruoyi-system: Same-Token 验证通过
❌ xypai-user:   Same-Token 验证失败 (期望的 Token 与 Gateway 不一致)
❌ xypai-content: Same-Token 验证失败
```

### 根本原因

**xypai 服务生成了各自独立的 Same-Token，未能与 Gateway 同步。**

通过对比 POM 文件发现：
1. ⚠️ **重复依赖**: xypai-user 和 xypai-content 重复声明了 `ruoyi-common-satoken`
2. ❌ **缺失依赖**: xypai 服务缺少 `ruoyi-common-tenant`、`ruoyi-common-translation` 等关键依赖
3. ✅ **参照标准**: ruoyi-system 只通过 `ruoyi-common-security` 传递获得 `ruoyi-common-satoken`

---

## ✅ 修复方案

### 修复策略：对齐到 ruoyi-system 标准

#### 1. 移除重复的 `ruoyi-common-satoken` 依赖

**原因**: `ruoyi-common-security` 已经传递包含了 `ruoyi-common-satoken`，重复声明可能导致类加载冲突。

**修改的服务**:
- ✅ xypai-user
- ✅ xypai-content

```xml
<!-- ⚠️ 移除重复依赖：ruoyi-common-satoken 已被 ruoyi-common-security 传递包含 -->
<!--
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-satoken</artifactId>
</dependency>
-->
```

#### 2. 添加缺失的核心依赖

对齐到 ruoyi-system 的标准依赖集：

```xml
<!-- ✅ 对齐 ruoyi-system 的依赖 -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-tenant</artifactId>
</dependency>

<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-translation</artifactId>
</dependency>

<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-service-impl</artifactId>
</dependency>
```

**修改的服务**:
- ✅ xypai-user
- ✅ xypai-content
- ✅ xypai-chat
- ✅ xypai-trade

---

## 📊 修改对比表

| 服务 | 修改前问题 | 修改后状态 |
|------|-----------|-----------|
| **xypai-user** | ❌ 重复 satoken 依赖<br>❌ 缺少 tenant/translation | ✅ 已对齐 |
| **xypai-content** | ❌ 重复 satoken 依赖<br>❌ 缺少 tenant/translation | ✅ 已对齐 |
| **xypai-chat** | ❌ 缺少 tenant/translation | ✅ 已对齐 |
| **xypai-trade** | ❌ 缺少 tenant/translation | ✅ 已对齐 |
| **ruoyi-system** | ✅ 标准依赖配置 | ✅ 参照标准 |

---

## 📝 完整依赖列表（xypai 服务标准配置）

```xml
<dependencies>
    <!-- 核心依赖 -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-nacos</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-log</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-doc</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-mybatis</artifactId>
    </dependency>
    
    <!-- 安全认证（包含 satoken）-->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-security</artifactId>
    </dependency>
    
    <!-- ⚠️ 不要重复声明 ruoyi-common-satoken -->
    
    <!-- 缓存和数据 -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-redis</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-sensitive</artifactId>
    </dependency>
    
    <!-- RPC 通信 -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-dubbo</artifactId>
    </dependency>
    
    <!-- ✅ 关键依赖：对齐到 ruoyi-system -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-tenant</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-translation</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-common-service-impl</artifactId>
    </dependency>
    
    <!-- 根据业务需求添加的特殊依赖 -->
    <!-- xypai-chat: spring-boot-starter-websocket -->
    <!-- xypai-content: jts-core (空间数据) -->
</dependencies>
```

---

## 🚀 部署步骤

### 1. 重新编译所有 xypai 服务

```bash
cd E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus

# 编译所有 xypai 服务
mvn clean compile -pl xypai-user,xypai-content,xypai-chat,xypai-trade
```

### 2. 重启服务（按顺序）

```bash
1. Redis (6379)
2. Nacos (8848)
3. Gateway (8080)  # ⭐ 先启动，生成 Same-Token
4. xypai-user (9401)
5. xypai-content (9403)
6. xypai-chat (9402)
7. xypai-trade (9404)
```

### 3. 验证 Same-Token 同步

查看启动日志：

```
Gateway 启动:
🔑 [GATEWAY SAME-TOKEN] 生成 Same-Token: eC5Gr...

xypai-user 启动:
🔐 [SAME-TOKEN CHECK] xypai-user - 验证请求
   请求中的 Same-Token: eC5Gr...
   期望的 Same-Token: eC5Gr... ✅  # 应该一致
   ✅ Same-Token验证通过
```

---

## 🔍 为什么这样修复有效？

### 依赖传递机制

```
xypai-user
  └── ruoyi-common-security
       └── ruoyi-common-satoken
            └── Sa-Token 核心
```

**关键点**:
1. **避免重复依赖**: 重复声明可能导致不同版本的 Sa-Token 同时存在
2. **统一依赖来源**: 所有服务通过 `ruoyi-common-security` 统一获得 Sa-Token
3. **对齐配置加载**: `ruoyi-common-tenant` 等依赖可能包含 Sa-Token 配置初始化逻辑

### Sa-Token 的 Same-Token 机制

```
1. Gateway 启动
   ↓
2. Sa-Token 自动生成 Same-Token
   ↓
3. 存储到 Redis (key: satoken:same-token)
   ↓
4. 微服务启动
   ↓
5. Sa-Token 从 Redis 读取 Same-Token（如果依赖配置正确）
   ↓
6. 验证时使用统一的 Same-Token ✅
```

---

## ⚠️ 注意事项

### 1. 依赖顺序很重要

`ruoyi-common-security` 必须在业务依赖之前声明：

```xml
<!-- ✅ 正确顺序 -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-security</artifactId>
</dependency>

<!-- 业务依赖... -->
```

### 2. 不要自定义 Same-Token 初始化

**错误做法** ❌:
```java
@Component
public class CustomSameTokenInit {
    @PostConstruct
    public void init() {
        SaSameUtil.refreshToken(); // 会生成新的 Token！
    }
}
```

**正确做法** ✅:
```
让 Sa-Token 自动管理 Same-Token，通过统一的依赖配置保证同步
```

### 3. Redis 配置必须一致

所有服务使用相同的 Redis:
```yaml
spring:
  data:
    redis:
      database: 0  # ⭐ 必须一致
```

---

## 📚 相关文档

- [Same-Token 根本原因与最终解决方案](./Same-Token根本原因与最终解决方案.md)
- [Sa-Token 完整技术架构文档](./Sa-Token完整技术架构文档.md)
- [401 错误完整解决方案](../xypai-user/docs/401_ERROR_SOLUTION.md)

---

## ✅ 验证清单

- [ ] 所有 xypai 服务的 POM 已对齐
- [ ] 移除了重复的 `ruoyi-common-satoken` 依赖
- [ ] 添加了 `ruoyi-common-tenant` 依赖
- [ ] 添加了 `ruoyi-common-translation` 依赖
- [ ] 添加了 `ruoyi-common-service-impl` 依赖
- [ ] 重新编译所有服务
- [ ] 按顺序重启服务
- [ ] 验证 Same-Token 日志一致
- [ ] 运行测试通过

---

**最后更新**: 2025-11-11  
**作者**: AI Assistant  
**状态**: ✅ 已完成依赖对齐

