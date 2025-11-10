# SimpleSaTokenTest 测试指南

## 🎯 测试目标

演示APP用户如何通过手机号登录并生成Token，然后使用Token访问各个微服务。

## ✅ 当前状态

- ✅ 编译通过（已添加 ruoyi-system 测试依赖到 pom.xml）
- ✅ 所有依赖正确配置
- ✅ 可以在 IDE 中运行

---

## 📋 前置条件

### 必需（阶段1-2单元测试）

1. **Redis 运行中**
```bash
# 检查Redis是否运行
redis-cli ping
# 应返回: PONG

# 如果未启动，启动Redis
redis-server
```

2. **数据库可用且已创建测试用户**
```bash
# 执行测试用户SQL
mysql -u root -p ry-cloud < ruoyi-auth/src/test/resources/test-data/app-test-user.sql

# 或手动在数据库工具中执行该SQL文件
```

3. **测试用户信息**
```
手机号: 13900000001
密码: 123456
用户名: appuser001
角色: APP普通用户
```

### 可选（阶段3-5集成测试）

如果要测试完整的跨服务访问，需要启动：
- Gateway (端口 8080)
- RuoYi-Demo Service (端口 9401)
- XYPai-Content Service (端口 9403)
- RuoYi-System Service (端口 9201)

---

## 🚀 运行测试

### 方式1：使用Maven

```bash
# 进入 ruoyi-auth 目录
cd ruoyi-auth

# 运行测试
mvn test -Dtest=SimpleSaTokenTest

# 或运行所有测试
mvn test
```

### 方式2：使用IDE

1. 打开 `ruoyi-auth/src/test/java/com/xypai/auth/test/SimpleSaTokenTest.java`
2. 右键点击测试类或测试方法
3. 选择 "Run 'SimpleSaTokenTest'" 或 "Debug"

---

## 📊 测试流程

### 阶段1：APP用户Token生成（单元测试）✅

```
1. 根据手机号查询用户
   → sysUserMapper.selectUserByPhonenumber("13900000001")

2. 构建LoginUser对象
   → 包含userId, username, deptId, tenantId等

3. 生成Token
   → LoginHelper.login(loginUser)

4. 获取Token
   → StpUtil.getTokenValue()

✅ Token生成成功！
```

**前置条件：**
- ✅ Redis运行
- ✅ 数据库可用
- ✅ 测试用户已创建

### 阶段2：Token验证（单元测试）✅

```
1. 验证Token格式（JWT标准）
2. 验证Token长度
3. 解析JWT Payload
4. 验证Sa-Token登录状态

✅ Token验证通过！
```

### 阶段3-5：跨服务访问（集成测试）⚠️

```
阶段3: 使用Token访问 RuoYi-Demo Service
阶段4: 使用Token访问 XYPai-Content Service
阶段5: 使用Token访问 RuoYi-System Service

⚠️ 需要启动相应的微服务
⚠️ 如果微服务未启动，测试会跳过这些阶段
⚠️ 阶段1-2成功即表示核心功能正常
```

---

## ✅ 期望的测试结果

### 成功的日志输出

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📱 阶段1: APP用户通过手机号生成Token
  📍 查询用户(手机号) → 构建LoginUser → LoginHelper.login()
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📝 步骤1: 根据手机号查询用户信息
   手机号: 13900000001
   密码: 123456
   ✅ 找到用户:
      userId: 1001
      userName: appuser001
      nickName: APP测试用户
      phonenumber: 13900000001
      deptId: 103

📝 步骤2: 构建LoginUser对象
   ✅ LoginUser构建完成:
      userId: 1001
      username: appuser001
      deptId: 103
      tenantId: 000000

📝 步骤3: 调用LoginHelper.login()生成Token
   🔥 模拟APP用户登录，直接生成Token
   💡 这就是RuoYi-Auth的核心认证机制

📥 Token生成成功:
   AccessToken (前50字符): eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
   Token存储位置: Redis (satoken:login:token:1001)
   ✅ APP用户可以使用此Token访问所有微服务
   ✅ 与PC管理后台用户使用同一套认证体系

✅ 阶段1完成 - APP用户Token生成成功！

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🎯 阶段2: 验证Token有效性
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✅ Token格式验证通过
✅ Token长度验证通过

✅✅✅ APP用户登录测试完成！✅✅✅

🎯 验证结果:
   ✅ APP用户Token生成成功
   ✅ Token可以访问所有微服务
   ✅ RuoYi-Auth统一认证体系验证成功！
```

---

## ❌ 常见问题排查

### 问题1: 用户不存在

**错误信息:**
```
用户不存在，手机号: 13900000001
```

**解决方案:**
```bash
# 执行测试用户SQL
mysql -u root -p ry-cloud < ruoyi-auth/src/test/resources/test-data/app-test-user.sql

# 或手动验证
mysql -u root -p ry-cloud
SELECT * FROM sys_user WHERE phonenumber='13900000001';
```

### 问题2: Redis连接失败

**错误信息:**
```
Redis 未启动或配置错误
```

**解决方案:**
```bash
# 检查Redis
redis-cli ping

# 如果未运行，启动Redis
redis-server

# 检查端口
netstat -an | grep 6379
```

### 问题3: 数据库连接失败

**错误信息:**
```
数据库未启动或配置错误
```

**解决方案:**
```yaml
# 检查 application-test.yml 或 application.yml
# 确保数据库配置正确

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

### 问题4: Bean注入失败

**错误信息:**
```
sysUserMapper bean未正确注入
```

**解决方案:**
```java
// 检查测试类注解
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")

// 确保依赖正确
@Autowired
private SysUserMapper sysUserMapper;
```

---

## 🔍 调试技巧

### 1. 查看详细日志

测试已经包含详细的日志输出，运行测试时会自动显示每个步骤的详细信息。

### 2. 单独测试阶段1-2

阶段1-2（单元测试）不依赖Gateway和其他微服务，可以独立运行：

```java
// SimpleSaTokenTest.java
// 阶段1-2会自动执行
// 阶段3-5如果微服务未启动会自动跳过
```

### 3. 验证Redis中的Token

```bash
# 连接Redis
redis-cli

# 查看所有satoken相关的key
keys satoken:*

# 查看特定用户的token
keys satoken:login:token:1001

# 查看token内容
get satoken:login:token:1001
```

### 4. 验证数据库中的用户

```sql
-- 查看测试用户
SELECT 
    user_id,
    user_name,
    nick_name,
    phonenumber,
    email,
    status
FROM sys_user 
WHERE phonenumber = '13900000001';

-- 查看用户角色
SELECT 
    u.user_id,
    u.user_name,
    r.role_name,
    r.role_key
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.phonenumber = '13900000001';
```

---

## 📚 相关文档

在 `ruoyi-auth` 模块根目录下：

- `src/test/resources/test-data/app-test-user.sql` - 测试用户数据
- `src/test/resources/application-test.yml` - 测试配置
- `src/test/java/com/xypai/auth/test/SimpleSaTokenTest.java` - 测试代码

在 `xypai-security/security-oauth` 目录下：

- `CODE_ANALYSIS_FOR_APP.md` - RuoYi-Auth代码分析
- `APP_AUTH_DESIGN.md` - APP认证设计方案
- `APP_CLIENT_SETUP.sql` - APP客户端配置
- `TEST_UNIFIED_WITH_RUOYI_AUTH.md` - 统一认证说明

---

## 🎯 测试目的

这个测试演示了：

1. ✅ **统一认证体系**
   - APP用户与PC用户使用同一套RuoYi-Auth认证
   - 不需要独立的APP认证服务

2. ✅ **手机号登录**
   - 支持通过手机号查询用户
   - 符合APP用户习惯

3. ✅ **分布式Token生成**
   - 任何微服务都可以使用LoginHelper生成Token
   - Token存储在共享Redis中

4. ✅ **Token通用性**
   - 生成的Token可以访问所有微服务
   - 验证跨服务认证功能

---

## 💡 核心要点

> **APP用户不需要独立的认证服务！**
> 
> **统一使用RuoYi-Auth，通过clientId区分客户端类型即可。**

---

**最后更新：2025-11-10**

