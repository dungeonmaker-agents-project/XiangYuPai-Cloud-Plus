# 快速回答：APP用户架构

## ✅ 简短回答

### Q: ruoyi-auth 需要改吗？
**A: ❌ 完全不需要改！**

### Q: LoginUser 从哪里来？
**A: ✅ `org.dromara.system.api.model.LoginUser` (ruoyi-api-system)**

### Q: 用户业务数据放哪里？
**A: ⭐ 新建 `xypai-user` 微服务**

---

## 🎯 架构图（3层）

```
┌─────────────────────────────────────────────────┐
│  1. ruoyi-auth (认证层) - 不需要改              │
│     • 登录、Token生成                           │
│     • POST /auth/login                          │
│     • ✅ 已支持APP (clientId)                   │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  2. ruoyi-system (基础层) - 不需要改            │
│     • sys_user 表                               │
│     • 用户名、手机号、邮箱                      │
│     • 提供 Dubbo 接口                           │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  3. xypai-user (业务层) - ⭐ 你需要开发这个     │
│     • user_profile 表                           │
│     • health_record 表                          │
│     • 健康数据、个人资料                        │
│     • GET /xypai-user/api/v1/user/profile       │
└─────────────────────────────────────────────────┘
```

---

## 💻 代码示例

### xypai-user 的 Controller（你需要写的）

```java
@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {
    
    /**
     * 获取个人资料
     * 
     * ⭐ 关键：LoginHelper.getUserId() 自动从 Token 获取用户ID
     * ❌ 不需要：前端传递 userId
     * ✅ 安全性：用户只能访问自己的数据
     */
    @GetMapping("/profile")
    public R<UserProfileVo> getProfile() {
        // 从 Token 自动获取当前用户ID
        Long userId = LoginHelper.getUserId();
        
        // 查询业务数据
        UserProfile profile = userProfileService.getByUserId(userId);
        
        return R.ok(profile);
    }
    
    /**
     * 记录健康数据
     */
    @PostMapping("/health/record")
    public R<Void> addHealthRecord(@RequestBody HealthRecordDto dto) {
        // 自动获取用户ID
        Long userId = LoginHelper.getUserId();
        
        // 保存健康记录
        healthRecordService.addRecord(userId, dto);
        
        return R.ok();
    }
}
```

---

## 🔄 完整流程

### 1. APP 登录
```
APP → Gateway → ruoyi-auth
         ↓
    生成 Token
         ↓
    返回给 APP
```

### 2. APP 访问业务数据
```
APP (带 Token)
    ↓
Gateway (验证 Token)
    ↓
xypai-user
    ↓
LoginHelper.getUserId()  ← 从 Token 自动获取
    ↓
查询数据库
    ↓
返回数据
```

---

## 📋 需要开发什么？

### ❌ 不需要改的
- ruoyi-auth（已支持APP）
- ruoyi-system（提供基础服务）
- ruoyi-api-system（提供模型定义）

### ⭐ 需要开发的
- **xypai-user 微服务**（参考 ruoyi-demo）
  - Controller: 处理业务请求
  - Service: 业务逻辑
  - Mapper: 数据访问
  - Database: 业务数据表

---

## 🎯 核心要点

### 1. Token 是通用的
```java
// 所有微服务都可以这样获取当前用户
Long userId = LoginHelper.getUserId();
String userName = LoginHelper.getUsername();
```

### 2. 不需要传递 userId
```javascript
// ❌ 错误（不安全）
GET /api/user/profile?userId=1001

// ✅ 正确（安全）
GET /api/user/profile
Headers: { "Authorization": "Bearer token" }
// userId 自动从 Token 获取
```

### 3. 数据分层存储
```
sys_user (ruoyi-system)       ← 基础信息
├── user_id
├── user_name
├── phonenumber
└── email

user_profile (xypai-user)     ← 业务信息
├── user_id (关联)
├── real_name
├── height
└── weight

health_record (xypai-user)    ← 业务数据
├── user_id (关联)
├── record_type
└── record_value
```

---

## ⚡ 快速开始

### 步骤1: 确认 ruoyi-auth 已配置 APP 客户端
```sql
-- 检查 sys_client 表
SELECT * FROM sys_client WHERE client_id = 'app-xypai-client-id';

-- 如果没有，执行 APP_CLIENT_SETUP.sql
```

### 步骤2: 创建 xypai-user 微服务
```bash
# 复制模板
cp -r ruoyi-example/ruoyi-demo xypai-modules/xypai-user

# 修改配置
# 1. pom.xml: 改 artifactId
# 2. application.yml: 改端口和数据库
```

### 步骤3: 开发业务功能
```java
// 所有 Controller 都使用这个模式
@GetMapping("/xxx")
public R<XxxVo> getXxx() {
    Long userId = LoginHelper.getUserId();
    // ... 业务逻辑
    return R.ok(data);
}
```

### 步骤4: APP 调用
```javascript
// 1. 登录
const token = await login();

// 2. 访问业务接口（自动带 userId）
const profile = await fetch('/xypai-user/api/v1/user/profile', {
    headers: {
        'Authorization': 'Bearer ' + token
    }
});
```

---

## 🎉 总结

> **你的理解完全正确！**
> 
> **"登录用 ruoyi-auth，业务数据用 xypai-user"**

### 核心优势:
1. ✅ 不改现有代码（稳定）
2. ✅ 架构清晰（分层）
3. ✅ 安全性高（Token自动获取userId）
4. ✅ 易于扩展（新增微服务）

---

**详细文档**: `APP_USER_ARCHITECTURE.md`

**需要帮你创建 xypai-user 微服务代码吗？**

