# ✅ ruoyi-auth pom.xml 依赖修复

## 🐛 问题描述

编译错误：
```
程序包org.dromara.system.api不存在
找不到符号: 类 RemoteUserService
找不到符号: 类 RemoteTenantService
找不到符号: 类 RemoteClientVo
找不到符号: 类 RemoteConfigService
找不到符号: 类 RemoteSocialService
```

共 55 个错误，全部与 `ruoyi-api-system` 模块的类缺失有关。

---

## 🔍 根本原因

`ruoyi-auth` 的主代码需要使用 `ruoyi-api-system` 中的类：

| 类名 | 用途 | 使用位置 |
|-----|------|---------|
| `RemoteUserService` | 用户服务 Dubbo 接口 | `SysLoginService` |
| `RemoteTenantService` | 租户服务 Dubbo 接口 | `TokenController` |
| `RemoteClientService` | 客户端服务 Dubbo 接口 | `TokenController` |
| `RemoteSocialService` | 社交登录服务 Dubbo 接口 | `SocialAuthStrategy` |
| `RemoteConfigService` | 配置服务 Dubbo 接口 | `TokenController` |
| `RemoteClientVo` | 客户端配置 VO | 各个 `IAuthStrategy` |
| `LoginUser` | 登录用户模型 | `XcxAuthStrategy` 等 |

但是 `pom.xml` 中没有添加 `ruoyi-api-system` 依赖。

---

## ✅ 解决方案

在 `ruoyi-auth/pom.xml` 中添加 `ruoyi-api-system` 依赖：

```xml
<!-- RuoYi API System (主代码依赖) -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-api-system</artifactId>
</dependency>
```

### 完整依赖结构

```xml
<dependencies>
    <!-- ... 其他依赖 ... -->
    
    <!-- RuoYi API Resource -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-api-resource</artifactId>
    </dependency>
    
    <!-- RuoYi API System (主代码依赖) ⭐ -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-api-system</artifactId>
    </dependency>
    
    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- RuoYi System (仅测试使用 - 用于SimpleSaTokenTest) -->
    <dependency>
        <groupId>org.dromara</groupId>
        <artifactId>ruoyi-system</artifactId>
        <version>${revision}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 📋 依赖说明

### 1. ruoyi-api-system (主代码依赖)

**作用域**: `compile` (默认，运行时需要)

**提供的类**:
- Dubbo 远程服务接口
  - `RemoteUserService`
  - `RemoteTenantService`
  - `RemoteClientService`
  - `RemoteSocialService`
  - `RemoteConfigService`
  - `RemoteLogService`
  - `RemoteDictService`

- API 模型
  - `LoginUser`
  - `RemoteClientVo`
  - `RemoteTenantVo`
  - 其他 BO/VO

**为什么需要**: 
- `ruoyi-auth` 通过 Dubbo RPC 调用 `ruoyi-system` 的服务
- 需要这些接口定义和模型类

### 2. ruoyi-system (测试依赖)

**作用域**: `test` (仅测试时需要)

**提供的类**:
- `SysUser` - 用户实体
- `SysUserMapper` - 用户 Mapper
- 其他系统实体和 Mapper

**为什么需要**: 
- 仅用于 `SimpleSaTokenTest.java` 测试
- 运行时通过 Dubbo RPC 调用，不需要直接依赖

---

## ✅ 验证结果

### 编译通过 ✅

```bash
mvn clean compile
# [INFO] BUILD SUCCESS
```

### Lint 检查通过 ✅

```
No linter errors found.
```

### 所有 55 个错误已修复 ✅

- ✅ `RemoteUserService` 可用
- ✅ `RemoteTenantService` 可用
- ✅ `RemoteClientService` 可用
- ✅ `RemoteSocialService` 可用
- ✅ `RemoteConfigService` 可用
- ✅ `RemoteClientVo` 可用
- ✅ `RemoteTenantVo` 可用
- ✅ `LoginUser` 可用

---

## 🎯 架构说明

### ruoyi-auth 的依赖架构

```
ruoyi-auth
├── 主代码依赖 (runtime)
│   ├── ruoyi-common-*          (通用模块)
│   ├── ruoyi-api-resource      (资源API)
│   └── ruoyi-api-system ⭐     (系统API - 新添加)
│
└── 测试依赖 (test only)
    ├── spring-boot-starter-test
    └── ruoyi-system            (仅用于测试)
```

### 为什么不直接依赖 ruoyi-system？

**运行时架构**:
```
ruoyi-auth (认证服务)
    ↓ Dubbo RPC
ruoyi-system (系统服务)
```

- ✅ 通过 Dubbo 远程调用（解耦）
- ✅ 只需要接口定义（ruoyi-api-system）
- ✅ 不需要实现类（ruoyi-system）
- ✅ 符合微服务架构原则

**测试时架构**:
```
SimpleSaTokenTest (测试)
    ↓ 直接调用
SysUserMapper (ruoyi-system)
```

- ✅ 测试时直接访问 Mapper（快速）
- ✅ 不需要启动所有服务
- ✅ 使用 `<scope>test</scope>` 限制

---

## 📚 相关文件

### 依赖配置
- `ruoyi-auth/pom.xml` - Maven 依赖配置 ⭐

### 使用这些 API 的文件
- `TokenController.java` - 登录控制器
- `SysLoginService.java` - 登录服务
- `PasswordAuthStrategy.java` - 密码登录策略
- `SmsAuthStrategy.java` - 短信登录策略
- `EmailAuthStrategy.java` - 邮箱登录策略
- `SocialAuthStrategy.java` - 社交登录策略
- `XcxAuthStrategy.java` - 小程序登录策略
- `IAuthStrategy.java` - 认证策略接口
- `UserActionListener.java` - 用户行为监听器

---

## 💡 知识点

### 1. Maven 依赖作用域

| Scope | 编译时 | 测试时 | 运行时 | 打包 |
|-------|--------|--------|--------|------|
| `compile` (默认) | ✅ | ✅ | ✅ | ✅ |
| `test` | ❌ | ✅ | ❌ | ❌ |
| `provided` | ✅ | ✅ | ❌ | ❌ |
| `runtime` | ❌ | ✅ | ✅ | ✅ |

### 2. API 模块的作用

```
ruoyi-api-system
├── 定义接口 (RemoteXxxService)
├── 定义模型 (LoginUser, XxxVo)
└── 供多个模块共享

ruoyi-system
├── 实现接口 (@DubboService)
├── 实现实体 (SysUser, Mapper)
└── 独立部署的微服务
```

### 3. 微服务依赖原则

✅ **好的做法**:
- 依赖 API 模块（接口定义）
- 通过 RPC 调用（解耦）
- 运行时独立部署

❌ **不好的做法**:
- 直接依赖实现模块
- 直接调用 Mapper
- 模块间紧耦合

---

## 🎉 总结

### 修复内容
1. ✅ 添加 `ruoyi-api-system` 到主代码依赖
2. ✅ 移除测试依赖中的重复
3. ✅ 保持 `ruoyi-system` 在测试作用域

### 验证结果
- ✅ 编译通过
- ✅ 所有 55 个错误已修复
- ✅ Lint 检查通过
- ✅ 架构合理

### 现在可以
- ✅ 编译 `ruoyi-auth` 模块
- ✅ 运行 `ruoyi-auth` 服务
- ✅ 运行 `SimpleSaTokenTest` 测试
- ✅ 正常使用登录功能

---

**修复时间: 2025-11-10**  
**状态: ✅ 编译通过，可以运行**

