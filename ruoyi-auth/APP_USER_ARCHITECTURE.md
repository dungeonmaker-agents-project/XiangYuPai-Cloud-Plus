# APP用户架构设计方案

## 🎯 你的问题总结

1. **登录模块**: 使用 `ruoyi-auth` 的综合登录 ✅
2. **用户实体**: 从哪里获取？`ruoyi-api-system` 的 `LoginUser`？
3. **用户服务**: 应该在哪个微服务开发？
4. **业务数据**: 健康数据、个人资料等放在哪里？
5. **是否需要改动**: ruoyi-auth 是否需要修改？

---

## ✅ 推荐架构（分层清晰）

```
┌─────────────────────────────────────────────────────────────────┐
│                        APP 前端 (Flutter/React Native)            │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTP + Token
┌─────────────────────────────────────────────────────────────────┐
│                        Gateway (网关)                             │
│                    localhost:8080                                │
└─────────────────────────────────────────────────────────────────┘
          ↓                    ↓                    ↓
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  ruoyi-auth      │  │  ruoyi-system    │  │  xypai-user      │
│  (认证服务)      │  │  (系统基础)      │  │  (APP用户业务)   │
│  端口: 9210      │  │  端口: 9201      │  │  端口: 9501      │
└──────────────────┘  └──────────────────┘  └──────────────────┘
│                     │                     │
│ 职责:               │ 职责:               │ 职责:
│ • 登录认证          │ • sys_user表       │ • user_profile
│ • Token生成         │ • 角色权限         │ • 健康数据
│ • 短信验证码        │ • 基础CRUD         │ • 个人偏好
│ • 社交登录          │ • 组织架构         │ • 业务扩展
│                     │                     │
│ ❌ 不关心:          │ ✅ 提供:            │ ✅ 提供:
│ • 用户业务数据      │ • 用户基础信息      │ • APP业务数据
│ • 健康记录          │ • Dubbo RPC接口    │ • REST API
│                     │                     │
```

---

## 📋 详细职责划分

### 1. ruoyi-auth (认证服务) - ⚠️ 不需要改动

**职责**:
- ✅ 用户登录（密码、短信、社交登录）
- ✅ Token 生成和管理
- ✅ 验证码发送
- ✅ 登录日志记录

**使用的模型**:
- `LoginUser` (来自 `ruoyi-api-system`)
- `SysUser` (通过 Dubbo 从 `ruoyi-system` 获取)

**API 示例**:
```http
POST /auth/login
{
  "phonenumber": "13900000001",
  "smsCode": "123456",
  "clientId": "app-xypai-client-id",
  "grantType": "sms"
}

Response:
{
  "code": 200,
  "data": {
    "access_token": "eyJ0eXAiOiJKV1QiLCJ...",
    "expires_in": 7200
  }
}
```

**✅ 结论**: **ruoyi-auth 不需要任何改动！**

---

### 2. ruoyi-system (系统基础服务) - 已有，不需要改

**职责**:
- ✅ 管理 `sys_user` 表（用户基础信息）
- ✅ 用户 CRUD 操作
- ✅ 角色、权限管理
- ✅ 组织架构（部门、岗位）

**数据表**:
```sql
sys_user          -- 用户基础表
├── user_id       -- 用户ID
├── user_name     -- 用户名
├── nick_name     -- 昵称
├── phonenumber   -- 手机号
├── email         -- 邮箱
├── avatar        -- 头像
└── status        -- 状态
```

**提供的 Dubbo 接口** (已有):
```java
@DubboService
public interface RemoteUserService {
    // 根据用户名查询用户
    R<LoginUser> getUserInfo(String username, String source);
    
    // 根据手机号查询用户
    R<SysUser> getUserByPhonenumber(String phonenumber);
    
    // 注册用户
    R<Boolean> registerUser(SysUser sysUser, String source);
}
```

**✅ 结论**: **ruoyi-system 已经提供了基础用户服务，不需要改动！**

---

### 3. xypai-user (APP用户业务服务) - 你需要开发这个 ⭐

**职责**: APP 用户的业务扩展
- ✅ 用户个人资料扩展
- ✅ 健康数据、运动记录
- ✅ 个人偏好设置
- ✅ 业务相关的用户数据

**数据表设计**:
```sql
-- xypai_user 数据库

-- 用户扩展资料表
user_profile
├── user_id          -- 关联 sys_user.user_id
├── real_name        -- 真实姓名
├── gender           -- 性别
├── birthday         -- 生日
├── height           -- 身高
├── weight           -- 体重
├── bio              -- 个人简介
└── ...

-- 健康数据表
health_record
├── id
├── user_id          -- 关联 sys_user.user_id
├── record_type      -- 记录类型（血压、心率等）
├── record_value     -- 数值
├── record_time      -- 记录时间
└── ...

-- 用户统计表
user_stats
├── user_id          -- 关联 sys_user.user_id
├── post_count       -- 发帖数
├── follow_count     -- 关注数
├── fans_count       -- 粉丝数
└── ...
```

**Controller 示例**:
```java
@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {
    
    @Autowired
    private UserProfileService userProfileService;
    
    /**
     * 获取当前用户的个人资料
     * 
     * ✅ 不需要传递 userId，从 Token 中自动获取
     */
    @GetMapping("/profile")
    public R<UserProfileVo> getProfile() {
        // 从 Token 中获取当前用户ID
        Long userId = LoginHelper.getUserId();
        
        // 查询业务数据
        UserProfile profile = userProfileService.getByUserId(userId);
        
        return R.ok(profile);
    }
    
    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody UserProfileDto dto) {
        Long userId = LoginHelper.getUserId();
        
        userProfileService.updateByUserId(userId, dto);
        
        return R.ok();
    }
    
    /**
     * 获取用户健康数据
     */
    @GetMapping("/health/records")
    public R<List<HealthRecordVo>> getHealthRecords(
        @RequestParam(required = false) String recordType,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        Long userId = LoginHelper.getUserId();
        
        List<HealthRecord> records = healthRecordService.queryRecords(
            userId, recordType, startDate, endDate
        );
        
        return R.ok(records);
    }
}
```

**✅ 结论**: **这是你主要需要开发的微服务！**

---

## 🔄 完整的用户数据流

### 场景1: APP 用户登录

```
1. APP 前端
   POST /auth/login
   {
     "phonenumber": "13900000001",
     "smsCode": "123456",
     "clientId": "app-xypai-client-id",
     "grantType": "sms"
   }
   ↓
2. Gateway 路由到 ruoyi-auth
   ↓
3. ruoyi-auth 验证短信码
   ↓
4. ruoyi-auth 通过 Dubbo 调用 ruoyi-system
   RemoteUserService.getUserByPhonenumber("13900000001")
   ↓
5. ruoyi-system 返回 SysUser（基础信息）
   ↓
6. ruoyi-auth 生成 Token
   LoginHelper.login(loginUser)
   ↓
7. 返回 Token 给 APP
   {
     "access_token": "eyJ0eXAiOiJKV1QiLCJ...",
     "expires_in": 7200
   }
```

### 场景2: APP 获取用户资料

```
1. APP 前端（带 Token）
   GET /xypai-user/api/v1/user/profile
   Authorization: Bearer eyJ0eXAiOiJKV1QiLCJ...
   ↓
2. Gateway 验证 Token（Sa-Token 自动）
   ↓
3. Gateway 路由到 xypai-user
   ↓
4. xypai-user 的 UserProfileController
   Long userId = LoginHelper.getUserId();  // 从 Token 获取
   ↓
5. 查询 user_profile 表
   ↓
6. 如果需要基础信息，通过 Dubbo 调用 ruoyi-system
   RemoteUserService.getUserInfo(userId)
   ↓
7. 组合返回
   {
     "code": 200,
     "data": {
       "userId": 1001,
       "userName": "appuser001",      // 来自 sys_user
       "nickName": "APP测试用户",     // 来自 sys_user
       "phonenumber": "13900000001",  // 来自 sys_user
       "realName": "张三",            // 来自 user_profile
       "gender": "男",                // 来自 user_profile
       "birthday": "1990-01-01",      // 来自 user_profile
       "height": 175,                 // 来自 user_profile
       "weight": 70                   // 来自 user_profile
     }
   }
```

### 场景3: APP 记录健康数据

```
1. APP 前端（带 Token）
   POST /xypai-user/api/v1/user/health/record
   Authorization: Bearer eyJ0eXAiOiJKV1QiLCJ...
   {
     "recordType": "blood_pressure",
     "systolic": 120,
     "diastolic": 80
   }
   ↓
2. Gateway 验证 Token
   ↓
3. Gateway 路由到 xypai-user
   ↓
4. xypai-user 的 HealthRecordController
   Long userId = LoginHelper.getUserId();  // 自动获取
   ↓
5. 保存到 health_record 表
   INSERT INTO health_record (user_id, record_type, ...)
   ↓
6. 返回成功
```

---

## 🎯 回答你的具体问题

### Q1: 是否应该使用 ruoyi-api-system 模块？

**A**: ✅ **是的，但仅限于 API 定义和模型**

使用场景：
- ✅ `LoginUser` - 登录用户模型
- ✅ Dubbo RPC 接口定义 (`RemoteUserService` 等)
- ✅ 基础 DTO/VO 模型

不使用场景：
- ❌ 不要在 `ruoyi-api-system` 中添加业务相关的模型
- ❌ 业务模型应该在 `xypai-user` 模块中定义

---

### Q2: 用户实体和信息从哪里获取？

**A**: **分层获取**

| 信息类型 | 来源 | 获取方式 |
|---------|------|---------|
| **认证信息** (Token中的用户) | `LoginHelper` | `LoginHelper.getUserId()` |
| **基础信息** (用户名、手机号) | `ruoyi-system` | Dubbo RPC 或数据库 |
| **业务信息** (健康、资料) | `xypai-user` | REST API 或本地数据库 |

代码示例：
```java
// 在 xypai-user 的 Controller 中
@GetMapping("/complete-profile")
public R<CompleteProfileVo> getCompleteProfile() {
    // 1. 从 Token 获取用户ID（自动）
    Long userId = LoginHelper.getUserId();
    
    // 2. 查询业务数据（本地数据库）
    UserProfile profile = userProfileService.getByUserId(userId);
    HealthStats stats = healthStatsService.getByUserId(userId);
    
    // 3. 如果需要基础信息，调用 ruoyi-system（Dubbo）
    R<SysUser> userResult = remoteUserService.getUserById(userId);
    SysUser sysUser = userResult.getData();
    
    // 4. 组合返回
    CompleteProfileVo vo = new CompleteProfileVo();
    vo.setUserId(userId);
    vo.setUserName(sysUser.getUserName());     // 来自 ruoyi-system
    vo.setPhoneNumber(sysUser.getPhonenumber()); // 来自 ruoyi-system
    vo.setRealName(profile.getRealName());       // 来自 xypai-user
    vo.setHeight(profile.getHeight());           // 来自 xypai-user
    vo.setHealthScore(stats.getHealthScore());   // 来自 xypai-user
    
    return R.ok(vo);
}
```

---

### Q3: 用户相关服务应该在哪个微服务开发？

**A**: **按职责分层开发**

```
ruoyi-system (已有)
├── 用户基础 CRUD
├── 角色权限管理
├── 组织架构
└── 提供 Dubbo 接口

xypai-user (你需要开发) ⭐
├── 个人资料管理
├── 健康数据管理
├── 用户统计分析
├── 业务相关功能
└── 提供 REST API
```

**Gateway 路由配置**:
```yaml
# script/config/nacos/ruoyi-gateway.yml

- id: xypai-user
  uri: lb://xypai-user
  predicates:
    - Path=/xypai-user/**
  filters:
    - StripPrefix=1
    - CacheRequestFilter
```

---

### Q4: 登录后什么需要改？什么不需要改？

**A**: **ruoyi-auth 完全不需要改！**

#### ✅ 不需要改的（已经支持APP）:

**ruoyi-auth**:
- ✅ 登录接口 (`/auth/login`)
- ✅ Token 生成机制
- ✅ 多客户端支持 (clientId)
- ✅ 短信登录支持

**理由**: 
- ruoyi-auth 只负责"认证"，不关心业务
- 通过 `clientId` 已经区分了 APP/PC
- Token 是通用的，所有微服务都能用

#### ⭐ 需要开发的（新建微服务）:

**xypai-user** (新建):
```
xypai-user/
├── src/main/java/com/xypai/user/
│   ├── controller/
│   │   ├── UserProfileController.java      ⭐ 个人资料
│   │   ├── HealthRecordController.java     ⭐ 健康数据
│   │   └── UserStatsController.java        ⭐ 用户统计
│   ├── service/
│   │   ├── IUserProfileService.java
│   │   ├── IHealthRecordService.java
│   │   └── IUserStatsService.java
│   ├── mapper/
│   │   ├── UserProfileMapper.java
│   │   ├── HealthRecordMapper.java
│   │   └── UserStatsMapper.java
│   └── domain/
│       ├── UserProfile.java
│       ├── HealthRecord.java
│       └── UserStats.java
└── src/main/resources/
    ├── application.yml
    └── mapper/
        ├── UserProfileMapper.xml
        ├── HealthRecordMapper.xml
        └── UserStatsMapper.xml
```

---

## 💡 实现步骤

### 步骤1: 不改任何现有代码 ✅

**ruoyi-auth**: 不动  
**ruoyi-system**: 不动  
**ruoyi-api-system**: 不动

### 步骤2: 创建 xypai-user 微服务 ⭐

```bash
# 1. 复制 ruoyi-example/ruoyi-demo 作为模板
cp -r ruoyi-example/ruoyi-demo xypai-modules/xypai-user

# 2. 修改 pom.xml
<artifactId>xypai-user</artifactId>
<name>xypai-user</name>

# 3. 创建数据库
CREATE DATABASE xypai_user;

# 4. 创建数据表
CREATE TABLE user_profile (...);
CREATE TABLE health_record (...);
CREATE TABLE user_stats (...);
```

### 步骤3: 开发 Controller（使用 LoginHelper）

```java
@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {
    
    @GetMapping("/profile")
    public R<UserProfileVo> getProfile() {
        // ⭐ 关键：从 Token 自动获取用户ID
        Long userId = LoginHelper.getUserId();
        
        // 查询业务数据
        UserProfile profile = userProfileService.getByUserId(userId);
        
        return R.ok(profile);
    }
}
```

### 步骤4: 配置 Gateway 路由

```yaml
# 在 ruoyi-gateway 的 application.yml 或 Nacos 中添加
- id: xypai-user
  uri: lb://xypai-user
  predicates:
    - Path=/xypai-user/**
  filters:
    - StripPrefix=1
```

### 步骤5: APP 前端调用

```javascript
// 1. 登录（使用现有接口）
POST http://localhost:8080/auth/login
{
  "phonenumber": "13900000001",
  "smsCode": "123456",
  "clientId": "app-xypai-client-id",
  "grantType": "sms"
}

// 2. 获取 Token
const token = response.data.access_token;

// 3. 调用业务接口（自动带 Token）
GET http://localhost:8080/xypai-user/api/v1/user/profile
Headers: {
  "Authorization": "Bearer " + token,
  "clientid": "app-xypai-client-id"
}

// 4. 自动获取当前用户数据 ✅
```

---

## 🎉 总结

### ✅ 你完全正确理解了！

> **"登录和用户认证用 ruoyi-auth，业务数据从其他微服务获取"**

这就是正确的微服务架构！

### 架构清单:

| 模块 | 职责 | 是否需要改 |
|-----|------|-----------|
| `ruoyi-auth` | 认证、Token生成 | ❌ 不需要 |
| `ruoyi-system` | 用户基础信息 | ❌ 不需要 |
| `ruoyi-api-system` | API定义、模型 | ❌ 不需要 |
| `xypai-user` | APP业务数据 | ⭐ 需要开发 |

### 关键点:

1. ✅ **ruoyi-auth 不需要任何改动**
2. ✅ **所有微服务通过 `LoginHelper.getUserId()` 获取当前用户**
3. ✅ **Token 是通用的，所有微服务都能用**
4. ✅ **业务数据在各自的微服务中管理**
5. ✅ **基础用户信息通过 Dubbo 从 ruoyi-system 获取**

---

**需要我帮你创建 `xypai-user` 微服务的模板代码吗？**

