# Same-Token 修复验证指南

> **版本**: v2.0 (Fail-Fast Edition)  
> **日期**: 2025-11-08  
> **验证时间**: 预计 5 分钟

---

## 🎯 修复内容总结

### 关键修改

| 文件 | 修改内容 | 修改原因 |
|-----|---------|---------|
| **ForwardAuthFilter.java** | ✅ 直接从Redis读取token<br>✅ token不存在时抛出异常 | ❌ 旧版本每次生成新token |
| **SameTokenInitializer.java** | ✅ 初始化失败时抛出异常 | ❌ 旧版本只记录日志 |

### Fail-Fast 原则

```
Redis正常 → Gateway启动成功 → 请求正常 ✅
Redis异常 → Gateway启动失败 → 立即暴露问题 ✅
```

---

## 🧪 验证步骤

### Step 1: 检查Redis状态

```bash
# 确保Redis正在运行
redis-cli ping
# 期望输出: PONG
```

如果Redis未启动：
```bash
# Windows
redis-server

# Linux/Mac
sudo systemctl start redis
```

---

### Step 2: 重启Gateway

#### 2.1 停止旧的Gateway进程

#### 2.2 清理并重新编译

```bash
cd RuoYi-Cloud-Plus
mvn clean compile -pl ruoyi-gateway
```

#### 2.3 启动Gateway

```bash
mvn spring-boot:run -pl ruoyi-gateway
```

---

### Step 3: 验证启动日志

#### ✅ 期望看到（正常情况）

```log
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔐 [SAME-TOKEN INIT] 开始初始化Same-Token
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ✅ 生成Same-Token: QROPDYZchpe...
   ✅ Same-Token已存储到Redis
   📋 Redis Key: satoken:var:same-token
   ⏰ 有效期: 7 天
   ✅ 验证成功：Same-Token已正确存储到Redis
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎉 [SAME-TOKEN INIT] Same-Token初始化完成
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🚀 Gateway启动成功！端口: 8080
```

#### ❌ 如果看到（Redis异常）

```log
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❌ [SAME-TOKEN INIT] Same-Token初始化失败
   错误信息: Unable to connect to Redis
   ⚠️  可能原因：
   1. Redis服务未启动或连接失败
   2. Redis配置错误（检查 application-common.yml）
   3. Redis权限不足，无法写入数据
   ⚠️  解决方案：
   1. 检查Redis服务状态: redis-cli ping
   2. 检查Redis配置: spring.data.redis.*
   3. 检查Redis日志，排查连接问题
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

***************************
APPLICATION FAILED TO START
***************************

Description:
Same-Token初始化失败，Gateway无法启动。

Action:
请检查Redis连接配置。
```

**这是正确的！Fail-Fast原则生效了！** ✅

---

### Step 4: 测试请求流程

#### 4.1 确保Content Service也在运行

```bash
# 端口: 9403
mvn spring-boot:run -pl xypai-content
```

#### 4.2 登录获取Token

```bash
curl -X POST http://localhost:8080/xypai-auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_dev",
    "password": "123456",
    "clientType": "app"
  }'
```

**期望响应**:
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "eyJ0eXAiOiJKV1Q...",
    "refreshToken": "...",
    "expiresIn": 604800
  }
}
```

保存 `accessToken` 的值。

#### 4.3 调用Content服务（通过Gateway）

```bash
# 替换 YOUR_TOKEN 为上一步获取的 accessToken
curl -H "Authorization: Bearer YOUR_TOKEN" \
     -H "clientid: app" \
     http://localhost:8080/xypai-content/api/v1/homepage/users/list
```

**期望响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "users": [
      {
        "userId": 1,
        "nickname": "Alice",
        "avatar": "https://..."
      }
    ],
    "total": 10,
    "hasMore": false
  }
}
```

---

### Step 5: 验证日志（重要！）

#### Gateway日志

```log
🔑 [SAME-TOKEN] 为请求添加 Same-Token: /xypai-content/api/v1/homepage/users/list
   Same-Token: QROPDYZchpe...
```

**关键验证点**:
- ✅ Same-Token的值应该与启动时生成的一致
- ✅ 不应该看到"从Redis读取失败"的错误

#### Content Service日志

```log
🔐 [SAME-TOKEN CHECK] 开始验证请求是否来自Gateway
   Redis中的Same-Token: QROPDYZchpe...
   请求中的Same-Token: QROPDYZchpe...
   两者是否一致: true
   ✅ Same-Token验证通过

🎯 [HOMEPAGE CONTROLLER] ✅ 请求成功到达Controller
   ✅ 认证成功: userId=2000, username=alice_dev, clientId=app
   ✅ 返回用户数量: 10
```

**关键验证点**:
- ✅ 两个token的值应该完全一致
- ✅ 应该看到"Same-Token验证通过"
- ✅ 应该看到"请求成功到达Controller"

---

## 🧪 故障模拟测试（可选）

### 测试Fail-Fast机制

#### Test 1: Redis未启动时启动Gateway

```bash
# 1. 停止Redis
redis-cli shutdown

# 2. 尝试启动Gateway
mvn spring-boot:run -pl ruoyi-gateway
```

**期望结果**: ❌ Gateway启动失败，并显示明确的错误信息。

```log
***************************
APPLICATION FAILED TO START
***************************

Description:
Same-Token初始化失败，Gateway无法启动。

Reason:
Unable to connect to Redis; nested exception is 
io.lettuce.core.RedisConnectionException: 
Unable to connect to localhost:6379
```

✅ **这是正确的行为！** Fail-Fast原则生效。

#### Test 2: 运行时Redis中的Same-Token被删除

```bash
# 1. Gateway正常运行

# 2. 手动删除Redis中的Same-Token
redis-cli del satoken:var:same-token

# 3. 发起请求
curl -H "Authorization: Bearer YOUR_TOKEN" \
     -H "clientid: app" \
     http://localhost:8080/xypai-content/api/v1/homepage/users/list
```

**期望结果**: ❌ 请求失败，Gateway日志显示明确错误。

```log
❌ [SAME-TOKEN] Redis中没有Same-Token！
   Redis Key: satoken:var:same-token
   请求路径: /xypai-content/api/v1/homepage/users/list
   ⚠️  可能原因：
   1. SameTokenInitializer未执行（Gateway启动失败）
   2. Redis连接失败
   3. Same-Token已过期（检查过期时间配置）
   ⚠️  解决方案：重启Gateway服务
```

**HTTP响应**:
```json
{
  "code": 500,
  "msg": "Same-Token未初始化，请检查Gateway启动日志"
}
```

✅ **这是正确的行为！** 不会生成临时token，而是直接失败并给出明确原因。

**解决方法**: 重启Gateway，SameTokenInitializer会重新初始化。

---

## ✅ 验证检查清单

### 启动阶段

- [ ] Redis服务正常运行
- [ ] Gateway启动成功
- [ ] 启动日志显示"Same-Token初始化完成"
- [ ] Redis中存在key: `satoken:var:same-token`

### 运行阶段

- [ ] 登录成功，获取Token
- [ ] 通过Gateway访问Content服务成功
- [ ] Gateway日志显示"为请求添加 Same-Token"
- [ ] Content服务日志显示"Same-Token验证通过"
- [ ] HTTP响应码为200，业务code也为200

### Fail-Fast验证

- [ ] Redis停止时，Gateway启动失败（而不是启动成功）
- [ ] 启动日志显示明确的错误信息和解决方案
- [ ] 运行时删除Same-Token，请求立即失败（而不是降级）

---

## 🐛 常见问题

### Q1: Gateway启动失败，提示"Same-Token初始化失败"

**原因**: Redis连接问题。

**检查步骤**:
1. Redis是否启动？`redis-cli ping`
2. Redis配置是否正确？检查 `01A_xyp_doc/nacos/application-common.yml`
3. Redis端口是否被占用？`netstat -an | grep 6379`

### Q2: 请求返回401，日志显示"Redis中没有Same-Token"

**原因**: Same-Token被意外删除或过期。

**解决方法**: 重启Gateway，会重新初始化。

### Q3: 启动成功，但请求还是返回401

**原因**: 可能是其他认证问题，与Same-Token无关。

**排查步骤**:
1. 检查Token是否有效
2. 检查clientId是否正确
3. 检查Gateway的AuthFilter日志

---

## 📊 性能验证（可选）

### 压力测试

```bash
# 使用 Apache Bench 测试
ab -n 1000 -c 10 \
   -H "Authorization: Bearer YOUR_TOKEN" \
   -H "clientid: app" \
   http://localhost:8080/xypai-content/api/v1/homepage/users/list
```

**期望结果**:
- ✅ 所有请求的Same-Token应该一致（从Redis读取）
- ✅ 不应该有"Same-Token验证失败"的错误
- ✅ 响应时间正常（无明显性能下降）

---

## ✅ 验证通过标准

当以下所有条件满足时，修复验证通过：

1. ✅ **启动阶段**: Gateway成功启动，日志显示Same-Token初始化完成
2. ✅ **运行阶段**: 请求能够正常通过Gateway到达Content服务
3. ✅ **日志验证**: Gateway和Content服务的日志都显示Same-Token验证通过
4. ✅ **Fail-Fast验证**: Redis异常时，Gateway启动失败（而不是启动成功后请求失败）
5. ✅ **一致性验证**: 启动时生成的Same-Token与运行时使用的Same-Token完全一致

---

## 🎉 验证完成

如果所有检查都通过，恭喜！修复已经成功！

### 下一步

1. **提交代码**: 将修复提交到版本控制
2. **部署测试环境**: 在测试环境验证
3. **监控生产环境**: 部署到生产环境后，监控Same-Token相关日志

---

**验证日期**: ___________  
**验证人**: ___________  
**验证结果**: [ ] 通过 / [ ] 未通过  
**备注**: ___________

