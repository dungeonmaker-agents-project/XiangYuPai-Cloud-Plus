# Same-Token 调试指南

## 📋 问题描述

**现象：**
- ✅ Gateway成功验证JWT Token
- ✅ Gateway日志显示转发请求到Content Service
- ❌ Content Service没有任何日志输出
- ❌ 返回401错误："认证失败，无法访问系统资源"

**原因：**
Content Service的`SecurityConfiguration`过滤器在检查`Same-Token`（同源令牌），这是一个用于确保请求必须经过Gateway的安全机制。

---

## 🏗️ 架构说明

### Same-Token工作流程：

```
1️⃣ 用户请求 + JWT Token
    ↓
2️⃣ Gateway (端口 8080)
    ├─ AuthFilter: 验证JWT Token ✅
    ├─ ForwardAuthFilter: 添加 Same-Token header (如果check-same-token=true)
    ↓
3️⃣ Content Service (端口 9403)
    ├─ SecurityConfiguration: 检查 Same-Token
    │  ├─ 如果有Same-Token且正确 ✅ → 放行到Controller
    │  └─ 如果没有或错误 ❌ → 返回401
    ↓
4️⃣ HomepageController
    ├─ 使用LoginHelper获取用户信息
    └─ 处理业务逻辑
```

---

## 🔍 当前配置

### 全局配置 (`application-common.yml`):
```yaml
sa-token:
  check-same-token: true  # 启用Same-Token检查
  jwt-secret-key: abcdefghijklmnopqrstuvwxyz
```

### Gateway行为:
- **文件**: `ruoyi-gateway/src/main/java/org/dromara/gateway/filter/ForwardAuthFilter.java`
- **作用**: 如果`check-same-token=true`，自动为所有转发请求添加`Same-Token` header
- **已添加日志**: 
  ```
  🔑 [SAME-TOKEN] 为请求添加 Same-Token: /xypai-content/api/v1/homepage/users/list
     Same-Token值: xxx...
  ```

### Content Service行为:
- **文件**: `ruoyi-common-security/src/main/java/org/dromara/common/security/config/SecurityConfiguration.java`
- **作用**: 如果`check-same-token=true`，验证所有请求的`Same-Token`
- **已添加日志**:
  ```
  🔐 [SAME-TOKEN CHECK] 开始验证请求是否来自Gateway
     ✅ Same-Token验证通过
  ```
  或
  ```
  🔐 [SAME-TOKEN CHECK] 开始验证请求是否来自Gateway
     ❌ Same-Token验证失败: xxx
  ```

---

## 🧪 调试步骤

### 第1步：重启Gateway
```bash
# 停止旧的Gateway进程
# 在IDEA中找到 ruoyi-gateway 项目
# 点击 Run → Restart 'GatewayApplication'
```

**预期日志（Gateway启动后）：**
```
Started GatewayApplication in X.XXX seconds
nacos registry, DEFAULT_GROUP ruoyi-gateway xxx:8080 register finished
```

---

### 第2步：重启Content Service
```bash
# 停止旧的Content Service进程
taskkill /PID 30224 /F

# 在IDEA中找到 xypai-content 项目
# 点击 Run → Run 'XyPaiContentApplication'
```

**预期日志（Content Service启动后）：**
```
Started XyPaiContentApplication in X.XXX seconds
nacos registry, DEFAULT_GROUP xypai-content 198.18.0.1:9403 register finished
```

---

### 第3步：运行测试
```bash
# 在 xypai-security/security-oauth 项目中
# 右键 SimpleSaTokenTest.java → Run Test
```

---

### 第4步：查看日志分析

#### ✅ **成功情况** - 日志应该显示：

**Gateway日志：**
```
🔐 [GATEWAY AUTH] 开始认证: /xypai-content/api/v1/homepage/users/list
   ✅ StpUtil.checkLogin() 通过
   ✅ ClientId匹配通过
   ✅ [GATEWAY AUTH] 认证成功

🔑 [SAME-TOKEN] 为请求添加 Same-Token: /xypai-content/api/v1/homepage/users/list
   Same-Token值: xxx...

[PLUS]开始请求 => URL[GET /xypai-content/api/v1/homepage/users/list]
[PLUS]结束请求 => 耗时:[XX]毫秒
```

**Content Service日志：**
```
🔐 [SAME-TOKEN CHECK] 开始验证请求是否来自Gateway
   ✅ Same-Token验证通过

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎯 [HOMEPAGE CONTROLLER] ✅ 请求成功到达Controller！
📋 [HOMEPAGE] 首页用户列表接口被调用
   ✅ 认证成功: userId=2000, username=app_tester, clientId=app
   📊 查询参数: filterTab=all, region=null, page=1, limit=10
   ✅ 返回用户数量: 10
```

**测试输出：**
```
✅ 阶段3成功 - 完整业务流程通过！
📊 完整业务验证结果:
   ✅ Gateway路由成功
   ✅ Sa-Token认证通过
   ✅ Content Service响应正常
   ✅ 真实业务接口工作正常
```

---

#### ❌ **失败情况1** - Gateway没有添加Same-Token:

**Gateway日志：**
```
🔓 [SAME-TOKEN] 未启用 check-same-token，跳过: /xypai-content/api/v1/homepage/users/list
```

**原因**: Gateway的`check-same-token`配置为false或未生效

**解决方案**: 
1. 检查`ruoyi-gateway.yml`是否覆盖了`check-same-token`
2. 确认Gateway使用的是`application-common.yml`的全局配置(`check-same-token: true`)
3. 重启Gateway确保配置生效

---

#### ❌ **失败情况2** - Content Service验证Same-Token失败:

**Content Service日志：**
```
🔐 [SAME-TOKEN CHECK] 开始验证请求是否来自Gateway
   ❌ Same-Token验证失败: Same-Token 无效
🚫 [SECURITY FILTER] 认证失败: Same-Token 无效
```

**可能原因**:
1. Gateway和Content Service的`same-token`密钥不一致
2. Same-Token过期
3. 网络问题导致header丢失

**解决方案**:
1. 确认两个服务都使用相同的Nacos配置
2. 检查Nacos中`application-common.yml`的配置是否正确加载
3. 重启两个服务确保配置同步

---

#### ❌ **失败情况3** - Content Service没有任何日志:

**Content Service日志：**
```
(空)
```

**原因**: 请求根本没有到达Content Service

**可能原因**:
1. Content Service未启动或未注册到Nacos
2. Gateway路由配置错误
3. 网络问题

**解决方案**:
1. 检查Content Service是否在运行：`netstat -ano | findstr :9403`
2. 检查Nacos控制台中`xypai-content`服务是否注册（健康实例数应为1）
3. 检查Gateway路由配置(`ruoyi-gateway.yml`):
   ```yaml
   - id: xypai-content
     uri: lb://xypai-content
     predicates:
       - Path=/xypai-content/**
   ```

---

## 🔧 故障排除

### 问题：Same-Token一直验证失败

**临时解决方案（仅用于开发测试）：**

修改 `xypai-content.yml`，禁用Same-Token检查：

```yaml
# Sa-Token 配置（覆盖全局配置）
sa-token:
  check-same-token: false  # 临时禁用，仅用于调试
```

⚠️ **警告**: 这会降低安全性，允许直接访问Content Service，绕过Gateway。生产环境必须启用！

---

### 问题：生产环境是否能正常工作？

**回答**: ✅ **可以！**

当用户在前端登录并携带Token访问时，完整流程如下：

1. 前端发送请求：`GET /xypai-content/api/v1/homepage/users/list`
2. 携带Header：`Authorization: Bearer <JWT Token>`
3. Gateway验证JWT Token ✅
4. Gateway添加Same-Token header
5. Gateway转发到Content Service
6. Content Service验证Same-Token ✅
7. Content Service放行到Controller
8. Controller使用`LoginHelper`获取用户信息（从JWT中提取）
9. 返回数据给前端

整个过程中：
- ✅ Gateway负责JWT验证
- ✅ Content Service不需要验证JWT（JWT Simple Mode）
- ✅ Content Service只验证Same-Token（确保请求来自Gateway）
- ✅ Controller可以直接使用`LoginHelper`获取用户信息

---

## 📊 配置汇总

### 需要check-same-token: false的服务
- `xypai-auth` (认证服务，需要被外部直接访问)

### 需要check-same-token: true的服务（或使用全局配置）
- `xypai-content` (内容服务)
- `xypai-user` (用户服务)
- `xypai-chat` (聊天服务)
- `xypai-trade` (交易服务)
- `ruoyi-system` (系统服务)
- 其他所有业务服务

---

## 🎯 下一步

1. **重启两个服务** (Gateway + Content Service)
2. **运行测试** (SimpleSaTokenTest)
3. **检查所有日志**：
   - Gateway日志：是否添加了Same-Token？
   - Content Service日志：是否验证了Same-Token？
   - Content Service日志：是否到达了Controller？
4. **根据日志结果**，参考上面的故障排除部分

---

## 📝 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2025-11-08 | 初始版本，添加详细的Same-Token调试指南 |

---

**如有问题，请提供以下信息：**
1. Gateway完整日志（从启动到测试结束）
2. Content Service完整日志（从启动到测试结束）
3. 测试输出
4. Nacos控制台截图（显示服务注册情况）

