# 🧪 App Authentication Testing Guide

## 📋 **Testing Environment Setup**

### **1. Start Required Services**
```bash
# Start MySQL
mysql -u root -p

# Start Redis
redis-server

# Start Nacos
cd nacos/bin
./startup.sh -m standalone  # Linux/Mac
startup.cmd -m standalone   # Windows

# Start xypai-user service (Port 940x)
cd xypai-user
mvn spring-boot:run

# Start xypai-auth service (Port 9211)
cd xypai-auth
mvn spring-boot:run
```

### **2. Create Database and Table**
```sql
-- Create database
CREATE DATABASE IF NOT EXISTS xypai_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use database
USE xypai_user;

-- Run migration script
SOURCE /path/to/V1.0.0__Create_App_User_Table.sql;

-- Verify table created
SHOW TABLES;
DESC app_user;
```

---

## 🚀 **API Testing (Using Postman/Curl)**

### **Test Case 1: SMS Login (Auto-Registration) - New User**

#### **Step 1.1: Send SMS Verification Code**
```http
POST http://localhost:9211/auth/sms/send
Content-Type: application/json

{
  "mobile": "13800138000",
  "countryCode": "+86",
  "type": "login",
  "region": "+86"
}
```

**Expected Response:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "codeId": "abc123",
    "expiresIn": 300,
    "nextSendTime": 60,
    "mobile": "13800138000",
    "code": "123456"  // ⚠️ Only in dev mode
  }
}
```

#### **Step 1.2: SMS Login (Auto-Register)**
```http
POST http://localhost:9211/auth/login/sms
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "verificationCode": "123456",
  "agreeToTerms": true,
  "clientId": "app",
  "grantType": "app_sms"
}
```

**Expected Response (NEW User):**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireIn": 7200,
    "userId": "1",
    "nickname": "138****8000",
    "avatar": null,
    "isNewUser": true  // ⭐ Frontend should redirect to profile completion page
  }
}
```

**Database Verification:**
```sql
SELECT * FROM app_user WHERE mobile = '13800138000';

-- Expected result:
-- user_id: 1
-- mobile: 13800138000
-- country_code: +86
-- nickname: 138****8000
-- password: NULL (no password set yet)
-- status: 1 (normal)
-- is_new_user: 1 (true)
-- login_count: 1
```

---

### **Test Case 2: SMS Login - Existing User**

#### **Step 2.1: Send SMS Again**
```http
POST http://localhost:9211/auth/sms/send
Content-Type: application/json

{
  "mobile": "13800138000",
  "countryCode": "+86",
  "type": "login"
}
```

#### **Step 2.2: Login Again**
```http
POST http://localhost:9211/auth/login/sms
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "verificationCode": "654321",
  "agreeToTerms": true
}
```

**Expected Response (EXISTING User):**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "...",
    "expireIn": 7200,
    "userId": "1",
    "nickname": "138****8000",
    "isNewUser": false  // ⭐ Now false (user already completed profile)
  }
}
```

**Database Verification:**
```sql
SELECT login_count, last_login_time FROM app_user WHERE mobile = '13800138000';

-- Expected:
-- login_count: 2 (incremented)
-- last_login_time: <current timestamp>
```

---

### **Test Case 3: Password Login (Error - No Password Set)**

```http
POST http://localhost:9211/auth/login/password
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "password": "password123",
  "agreeToTerms": true
}
```

**Expected Response (ERROR):**
```json
{
  "code": 401,
  "message": "手机号或密码错误"
}
```

---

### **Test Case 4: Set Password (First Time)**

#### **Step 4.1: User Calls Profile Update API**
```http
POST http://localhost:9211/api/v1/user/password/set
Authorization: Bearer {token}
Content-Type: application/json

{
  "newPassword": "password123"
}
```

**⚠️ Note:** This endpoint needs to be created in xypai-user module (user profile management).

**Alternative: Direct Service Call (For Testing)**
```java
// In RemoteAppUserServiceImpl
remoteAppUserService.setPassword("13800138000", "+86", "password123");
```

**Database Verification:**
```sql
SELECT password FROM app_user WHERE mobile = '13800138000';

-- Expected:
-- password: $2a$10$... (BCrypt encrypted hash)
```

---

### **Test Case 5: Password Login (Success)**

```http
POST http://localhost:9211/auth/login/password
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "password": "password123",
  "agreeToTerms": true
}
```

**Expected Response (SUCCESS):**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "...",
    "expireIn": 7200,
    "userId": "1",
    "nickname": "138****8000",
    "isNewUser": false
  }
}
```

---

### **Test Case 6: Forgot Password Flow**

#### **Step 6.1: Send SMS (Reset Password)**
```http
POST http://localhost:9211/auth/sms/send
Content-Type: application/json

{
  "mobile": "13800138000",
  "countryCode": "+86",
  "type": "reset"
}
```

#### **Step 6.2: Verify SMS Code**
```http
POST http://localhost:9211/auth/password/reset/verify
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "verificationCode": "789012"
}
```

**Expected Response:**
```json
{
  "code": 200,
  "message": "验证成功"
}
```

#### **Step 6.3: Set New Password**
```http
POST http://localhost:9211/auth/password/reset/confirm
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "verificationCode": "789012",
  "newPassword": "newpassword456"
}
```

**Expected Response:**
```json
{
  "code": 200,
  "message": "密码重置成功"
}
```

#### **Step 6.4: Login with New Password**
```http
POST http://localhost:9211/auth/login/password
Content-Type: application/json

{
  "countryCode": "+86",
  "mobile": "13800138000",
  "password": "newpassword456",
  "agreeToTerms": true
}
```

**Expected Response (SUCCESS):**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "...",
    "userId": "1"
  }
}
```

---

## 🔍 **Error Cases Testing**

### **Error 1: Invalid Phone Number**
```http
POST http://localhost:9211/auth/login/sms
Content-Type: application/json

{
  "mobile": "12345",  // Invalid
  "verificationCode": "123456"
}
```

**Expected Response:**
```json
{
  "code": 400,
  "message": "手机号格式不正确"
}
```

### **Error 2: Wrong Verification Code**
```http
POST http://localhost:9211/auth/login/sms
Content-Type: application/json

{
  "mobile": "13800138000",
  "verificationCode": "999999"  // Wrong code
}
```

**Expected Response:**
```json
{
  "code": 401,
  "message": "验证码错误，请重新输入"
}
```

### **Error 3: Expired Verification Code**
```http
# Wait 6 minutes (verification code expires after 5 minutes)
POST http://localhost:9211/auth/login/sms
Content-Type: application/json

{
  "mobile": "13800138000",
  "verificationCode": "123456"
}
```

**Expected Response:**
```json
{
  "code": 401,
  "message": "验证码已过期，请重新获取"
}
```

### **Error 4: Password Too Short**
```http
POST http://localhost:9211/auth/password/reset/confirm
Content-Type: application/json

{
  "mobile": "13800138000",
  "newPassword": "12345"  // Only 5 chars
}
```

**Expected Response:**
```json
{
  "code": 400,
  "message": "密码长度必须在6-20位之间"
}
```

### **Error 5: Password Pure Digits**
```http
POST http://localhost:9211/auth/password/reset/confirm
Content-Type: application/json

{
  "mobile": "13800138000",
  "newPassword": "123456789"  // Pure digits
}
```

**Expected Response:**
```json
{
  "code": 400,
  "message": "密码格式不正确（6-20位，不可纯数字）"
}
```

### **Error 6: User Not Exists (Password Login)**
```http
POST http://localhost:9211/auth/login/password
Content-Type: application/json

{
  "mobile": "19999999999",  // Not registered
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "code": 401,
  "message": "手机号或密码错误"
}
```

---

## 📊 **Database Queries for Verification**

### **Check User Registration**
```sql
SELECT
    user_id,
    mobile,
    country_code,
    nickname,
    password IS NOT NULL AS has_password,
    status,
    is_new_user,
    login_count,
    last_login_time,
    created_at
FROM app_user
WHERE mobile = '13800138000';
```

### **Check Login History**
```sql
SELECT
    user_id,
    mobile,
    login_count,
    last_login_ip,
    last_login_time,
    TIMESTAMPDIFF(SECOND, last_login_time, NOW()) AS seconds_since_last_login
FROM app_user
ORDER BY last_login_time DESC;
```

### **Check Soft Delete**
```sql
SELECT * FROM app_user WHERE deleted_at IS NOT NULL;
```

---

## 🛠️ **Manual Testing Checklist**

### **SMS Login Flow**
- [ ] New user registration via SMS login
- [ ] Existing user login via SMS
- [ ] `isNewUser` flag returns correctly
- [ ] Login count increments
- [ ] Last login time updates
- [ ] Token generated successfully
- [ ] Verification code expires after 5 minutes
- [ ] Verification code deleted after successful login

### **Password Login Flow**
- [ ] Cannot login without password set
- [ ] Can login after password set
- [ ] Wrong password returns error
- [ ] BCrypt password verification works

### **Forgot Password Flow**
- [ ] SMS code sent successfully
- [ ] SMS code verification works
- [ ] New password set successfully
- [ ] Can login with new password
- [ ] Old password no longer works

### **Error Handling**
- [ ] Invalid phone number format
- [ ] Wrong verification code
- [ ] Expired verification code
- [ ] Password too short
- [ ] Password pure digits
- [ ] User not exists

### **Security**
- [ ] Password stored as BCrypt hash
- [ ] Verification codes are one-time use
- [ ] Account locked after 5 failed attempts (if implemented)
- [ ] No tenant ID in queries (app-only)

---

## 🔧 **Troubleshooting**

### **Problem: Verification code not found in Redis**
```bash
# Check Redis
redis-cli
> KEYS *captcha_code:*
> GET captcha_code:13800138000
```

### **Problem: User not created in database**
```sql
-- Check if table exists
SHOW TABLES LIKE 'app_user';

-- Check table structure
DESC app_user;

-- Check all users
SELECT * FROM app_user;
```

### **Problem: Dubbo service not found**
```bash
# Check Nacos registry
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-user

# Check Dubbo logs
tail -f logs/xypai-user.log | grep Dubbo
```

---

## ✅ **Success Criteria**

All tests pass when:
1. ✅ New user can register via SMS login
2. ✅ `isNewUser=true` returned for first login
3. ✅ `isNewUser=false` returned for subsequent logins
4. ✅ User can set password
5. ✅ User can login with password
6. ✅ User can reset password via SMS
7. ✅ All error cases handled correctly
8. ✅ Token generated and valid
9. ✅ No database queries contain `tenant_id`
10. ✅ No `TenantHelper` calls in code
