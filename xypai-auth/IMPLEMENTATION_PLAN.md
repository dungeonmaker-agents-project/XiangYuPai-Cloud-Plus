# XiangYuPai App Authentication Implementation Plan

## 📋 **Summary of Completed Work**

### ✅ **1. Database Schema**
Created `app_user` table at:
- **File:** `xypai-user/src/main/resources/db/migration/V1.0.0__Create_App_User_Table.sql`
- **Features:**
  - SMS-based auto-registration support
  - Optional password (user sets after first login)
  - No multi-tenancy fields
  - Soft delete support
  - Unique constraint: `(mobile, country_code, deleted_at)`

**Key Fields:**
```sql
user_id          -- Auto-increment primary key
mobile           -- Phone number (login credential)
country_code     -- Default '+86'
password         -- BCrypt encrypted (NULL initially)
nickname         -- Auto-generated: mobile[0:3]****mobile[-4:]
status           -- 0=disabled, 1=normal, 2=locked
is_new_user      -- 1=new (needs profile completion), 0=old
last_login_time  -- Updated on each login
login_count      -- Incremented on each login
```

### ✅ **2. API Module Created**
Created `xypai-api-appuser` module:
- **Location:** `ruoyi-api/xypai-api-appuser/`
- **Purpose:** Remote service interfaces for xypai-auth to call

**Files Created:**
1. `pom.xml` - Module configuration
2. `AppLoginUser.java` - Login user model (NO tenant/dept/role fields)
3. `RemoteAppUserService.java` - Remote service interface

**Key Methods in RemoteAppUserService:**
```java
// Login
AppLoginUser getUserByMobile(mobile, countryCode)
AppLoginUser getUserById(userId)

// Registration (auto-register on SMS login)
AppLoginUser registerOrGetByMobile(mobile, countryCode)  // ⭐ Key method

// Password
boolean checkPassword(userId, rawPassword)
boolean setPassword(mobile, countryCode, newPassword)
boolean resetPassword(mobile, countryCode, newPassword)

// Login tracking
boolean updateLastLoginInfo(userId, loginIp)
boolean markAsOldUser(userId)  // After profile completion

// Account status
boolean existsByMobile(mobile, countryCode)
Integer getUserStatus(userId)
boolean disableUser(userId, reason)
boolean enableUser(userId)
```

---

## 📋 **Next Steps - Implementation Roadmap**

### **Phase 1: xypai-user Module Implementation** (30-40 min)

#### **1.1 Create AppUser Entity**
```java
// Location: xypai-user/src/main/java/org/dromara/user/domain/entity/AppUser.java
@Data
@TableName("app_user")
public class AppUser extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String mobile;
    private String countryCode;
    private String password;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer status;

    @TableLogic
    private LocalDateTime deletedAt;

    @Version
    private Integer version;

    // ... other fields
}
```

#### **1.2 Create AppUserMapper**
```java
// Interface + MyBatis Plus BaseMapper
public interface AppUserMapper extends BaseMapper<AppUser> {
    // Custom queries if needed
}
```

#### **1.3 Implement RemoteAppUserService**
```java
@DubboService
public class RemoteAppUserServiceImpl implements RemoteAppUserService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Override
    public AppLoginUser registerOrGetByMobile(String mobile, String countryCode) {
        // 1. Query existing user
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getMobile, mobile)
               .eq(AppUser::getCountryCode, countryCode);

        AppUser user = appUserMapper.selectOne(wrapper);

        // 2. If not exists, create new user
        if (user == null) {
            user = AppUser.builder()
                .mobile(mobile)
                .countryCode(countryCode)
                .nickname(generateNickname(mobile))  // "131****6323"
                .status(1)  // Normal
                .isNewUser(true)
                .loginCount(0)
                .build();

            appUserMapper.insert(user);
        }

        // 3. Convert to AppLoginUser
        return convertToLoginUser(user);
    }

    // ... other methods
}
```

---

### **Phase 2: xypai-auth Strategy Refactoring** (20-30 min)

#### **2.1 Create App SMS Login Strategy (NO Tenant)**

**File:** `xypai-auth/src/main/java/org/dromara/auth/service/impl/AppSmsAuthStrategy.java`

```java
@Service("app_sms" + IAuthStrategy.BASE_NAME)
public class AppSmsAuthStrategy implements IAuthStrategy {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @Override
    public LoginVo login(String body, RemoteClientVo client) {
        AppSmsLoginBody loginBody = JsonUtils.parseObject(body, AppSmsLoginBody.class);

        String mobile = loginBody.getMobile();
        String countryCode = loginBody.getCountryCode();
        String smsCode = loginBody.getSmsCode();

        // 1. Validate SMS code
        if (!validateSmsCode(mobile, smsCode)) {
            throw new CaptchaException("验证码错误");
        }

        // 2. Register or get user (auto-registration)
        AppLoginUser loginUser = remoteAppUserService.registerOrGetByMobile(mobile, countryCode);

        // 3. Check account status
        if (!loginUser.isAccountNonLocked()) {
            throw new UserException("账号已被禁用");
        }

        // 4. Update login info
        remoteAppUserService.updateLastLoginInfo(loginUser.getUserId(), getClientIp());

        // 5. Generate token (Sa-Token)
        LoginHelper.login(loginUser, buildSaLoginParameter(client));

        // 6. Build response
        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setUserId(String.valueOf(loginUser.getUserId()));
        loginVo.setNickname(loginUser.getNickname());
        loginVo.setAvatar(loginUser.getAvatar());
        loginVo.setIsNewUser(loginUser.getIsNewUser());  // ⭐ Important for frontend routing

        return loginVo;
    }
}
```

**Key Differences from Admin Strategy:**
- ❌ No `TenantHelper.dynamic(tenantId, () -> {...})`
- ❌ No `tenantId` parameter in remote calls
- ✅ Auto-registration via `registerOrGetByMobile()`
- ✅ Returns `isNewUser` flag for frontend routing

#### **2.2 Create App Password Login Strategy**

**File:** `xypai-auth/src/main/java/org/dromara/auth/service/impl/AppPasswordAuthStrategy.java`

```java
@Service("app_password" + IAuthStrategy.BASE_NAME)
public class AppPasswordAuthStrategy implements IAuthStrategy {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @Override
    public LoginVo login(String body, RemoteClientVo client) {
        AppPasswordLoginBody loginBody = JsonUtils.parseObject(body, AppPasswordLoginBody.class);

        String mobile = loginBody.getMobile();
        String countryCode = loginBody.getCountryCode();
        String password = loginBody.getPassword();

        // 1. Get user by mobile
        AppLoginUser loginUser = remoteAppUserService.getUserByMobile(mobile, countryCode);

        // 2. Check password
        if (!remoteAppUserService.checkPassword(loginUser.getUserId(), password)) {
            throw new UserException("手机号或密码错误");
        }

        // 3. Check account status
        if (!loginUser.isAccountNonLocked()) {
            throw new UserException("账号已被禁用");
        }

        // 4. Generate token and return
        // ... same as SMS login
    }
}
```

---

### **Phase 3: Forgot Password Flow** (15-20 min)

#### **3.1 Create ForgotPasswordController**

**File:** `xypai-auth/src/main/java/org/dromara/auth/controller/ForgotPasswordController.java`

```java
@RestController
@RequestMapping("/auth/password")
public class ForgotPasswordController {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    /**
     * Step 1: Send SMS verification code
     * POST /auth/sms/send (reuse existing SmsController)
     */

    /**
     * Step 2: Verify SMS code
     * POST /auth/password/reset/verify
     */
    @PostMapping("/reset/verify")
    public R<Void> verifyCode(@RequestBody @Validated VerifyCodeRequest request) {
        String mobile = request.getMobile();
        String code = request.getVerificationCode();

        // 1. Check if user exists
        if (!remoteAppUserService.existsByMobile(mobile, request.getCountryCode())) {
            return R.fail("该手机号未注册");
        }

        // 2. Validate SMS code
        String cachedCode = RedisUtils.getCacheObject(SMS_CODE_KEY + mobile);
        if (!StringUtils.equals(code, cachedCode)) {
            return R.fail("验证码错误");
        }

        // 3. Mark code as verified (store in Redis with 10-minute expiry)
        RedisUtils.setCacheObject(
            VERIFIED_CODE_KEY + mobile,
            code,
            Duration.ofMinutes(10)
        );

        return R.ok();
    }

    /**
     * Step 3: Set new password
     * POST /auth/password/reset/confirm
     */
    @PostMapping("/reset/confirm")
    public R<Void> resetPassword(@RequestBody @Validated ResetPasswordRequest request) {
        String mobile = request.getMobile();
        String code = request.getVerificationCode();
        String newPassword = request.getNewPassword();

        // 1. Verify code was previously validated
        String verifiedCode = RedisUtils.getCacheObject(VERIFIED_CODE_KEY + mobile);
        if (!StringUtils.equals(code, verifiedCode)) {
            return R.fail("验证码已过期，请重新获取");
        }

        // 2. Validate password format
        if (!isValidPassword(newPassword)) {
            return R.fail("密码格式不正确（6-20位，不可纯数字）");
        }

        // 3. Reset password
        boolean success = remoteAppUserService.resetPassword(
            mobile,
            request.getCountryCode(),
            newPassword
        );

        if (success) {
            // Clear verification code
            RedisUtils.deleteObject(VERIFIED_CODE_KEY + mobile);
            return R.ok();
        } else {
            return R.fail("密码重置失败");
        }
    }
}
```

---

### **Phase 4: Update xypai-auth Dependencies** (5 min)

#### **4.1 Update pom.xml**

Add dependency on our new API module:

```xml
<!-- xypai-api-appuser -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>xypai-api-appuser</artifactId>
    <version>${revision}</version>
</dependency>
```

Remove (if exists):
```xml
<!-- REMOVE: tenant dependency -->
<!-- <dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-tenant</artifactId>
</dependency> -->

<!-- REMOVE: mybatis dependency (no direct DB access) -->
<!-- <dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-mybatis</artifactId>
</dependency> -->
```

---

## 🔄 **API Endpoint Summary**

### **Implemented Endpoints**

| Endpoint | Method | Purpose | Request Body | Response |
|----------|--------|---------|--------------|----------|
| `/auth/sms/send` | POST | Send SMS code | `{mobile, countryCode, purpose}` | `{code, message}` |
| `/auth/login/sms` | POST | SMS login (auto-reg) | `{mobile, countryCode, verificationCode, agreeToTerms}` | `{token, userId, nickname, avatar, isNewUser}` |
| `/auth/login/password` | POST | Password login | `{mobile, countryCode, password, agreeToTerms}` | `{token, userId, nickname, avatar}` |
| `/auth/password/reset/verify` | POST | Verify SMS for reset | `{mobile, countryCode, verificationCode}` | `{code, message}` |
| `/auth/password/reset/confirm` | POST | Confirm new password | `{mobile, countryCode, verificationCode, newPassword}` | `{code, message}` |

---

## 🧪 **Testing Checklist**

### **SMS Login Flow (Auto-Registration)**
- [ ] New user: Send SMS → Login → Creates account → Returns `isNewUser=true`
- [ ] Existing user: Send SMS → Login → Returns `isNewUser=false`
- [ ] Wrong SMS code → Error: "验证码错误"
- [ ] Expired SMS code → Error: "验证码已过期"
- [ ] Account disabled → Error: "账号已被禁用"

### **Password Login Flow**
- [ ] Correct password → Login success
- [ ] Wrong password → Error: "手机号或密码错误"
- [ ] No password set → Error: "请先设置密码"
- [ ] Account disabled → Error: "账号已被禁用"

### **Forgot Password Flow**
- [ ] Unregistered phone → Error: "该手机号未注册"
- [ ] Wrong SMS code → Error: "验证码错误"
- [ ] Expired verification → Error: "验证码已过期"
- [ ] Invalid password format → Error: "密码格式不正确"
- [ ] Success → Password reset → Can login with new password

---

## 📝 **Questions for Confirmation**

1. **Database Schema:**
   - ✅ Approve `app_user` table structure?
   - ✅ Auto-generate nickname as `mobile[0:3]****mobile[-4:]`?

2. **Registration Flow:**
   - ✅ Auto-register on SMS login (no separate registration endpoint)?
   - ✅ New users marked as `is_new_user=1` until profile completion?

3. **Password Policy:**
   - ✅ Password optional (user sets after first login)?
   - ✅ Password rules: 6-20 characters, not pure digits?

4. **Multi-Tenancy:**
   - ✅ Remove all tenant-related code from xypai-auth?
   - ✅ Use single app user pool (no tenant isolation)?

5. **Token Configuration:**
   - ❓ Access token expiry: 2 hours? 24 hours?
   - ❓ Refresh token expiry: 7 days? 30 days?

---

## 🚀 **Ready to Proceed?**

Please confirm the following before I start implementation:

1. ✅ Database schema approved
2. ✅ API interface design approved
3. ✅ Auto-registration flow approved
4. ❓ Any changes needed to the plan above?

Once confirmed, I'll implement in this order:
1. xypai-user module (AppUser entity, mapper, service implementation)
2. xypai-auth strategies (app SMS + password login)
3. Forgot password endpoints
4. Testing and bug fixes

**Estimated Total Time:** 1.5-2 hours
