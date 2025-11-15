# Quick Start: Testing Guide

## 🚀 5-Minute Quick Start

### Step 1: Verify Prerequisites (30 seconds)

```bash
# Check services are running
docker ps | grep -E "mysql|redis|nacos"

# Check xypai-user service
curl http://localhost:9212/actuator/health

# Check xypai-auth service
curl http://localhost:9211/actuator/health
```

**All should return success/UP status** ✅

---

### Step 2: Run Tests (2 minutes)

#### Option A: Using Batch Script (Windows)
```bash
cd xypai-auth

# Run most critical tests first
run-tests.bat sms

# Then run all tests
run-tests.bat all
```

#### Option B: Using Maven Directly
```bash
cd xypai-auth

# Critical test: SMS login with auto-registration
mvn test -Dtest=Page02_SmsLoginTest

# All tests
mvn clean test
```

#### Option C: Using IntelliJ IDEA
1. Open `Page02_SmsLoginTest.java`
2. Click green arrow next to class name
3. Wait for results

---

### Step 3: Check Results (1 minute)

**Expected Output**:
```
Tests run: 27, Failures: 0-8, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**Some failures are expected** if:
- SMS codes are not mocked in Redis (5-8 tests)
- RefreshToken not fully implemented (3-5 tests)

---

### Step 4: View Detailed Results (1 minute)

```bash
# View test summary
cat target/surefire-reports/*.txt

# View detailed XML report
ls target/surefire-reports/*.xml

# Check application logs
tail -100 logs/sys-info.log
```

---

## 📋 Test Priority Order

Run tests in this order for best results:

### 1️⃣ High Priority (Must Pass)
```bash
# Utility tests (100% should pass)
mvn test -Dtest=UtilityApiTest

# Password login (90% should pass)
mvn test -Dtest=Page01_PasswordLoginTest

# SMS send (80% should pass)
mvn test -Dtest=Page02_SmsLoginTest#testSendSms*
```

### 2️⃣ Medium Priority (70-80% pass rate)
```bash
# SMS login tests
mvn test -Dtest=Page02_SmsLoginTest

# Password reset tests
mvn test -Dtest=Page03_ForgotPasswordFlowTest

# Payment password tests
mvn test -Dtest=Page04_PaymentPasswordTest
```

### 3️⃣ Integration Tests (Variable pass rate)
```bash
# Complete user flows
mvn test -Dtest=NewUserRegistrationFlowTest
mvn test -Dtest=ExistingUserLoginFlowTest
mvn test -Dtest=PasswordResetFlowTest
```

---

## ⚠️ Common Quick Fixes

### Problem: Tests fail with "SMS code invalid"

**Quick Fix**:
```bash
# Mock SMS codes in Redis
redis-cli

# For new user (13900139999)
SET "sms:login:+86:13900139999" "123456"
EXPIRE "sms:login:+86:13900139999" 300

# For existing user (13800138000)
SET "sms:reset:+86:13800138000" "123456"
EXPIRE "sms:reset:+86:13800138000" 300
```

---

### Problem: Tests fail with "User not found"

**Quick Fix**:
```sql
-- Connect to MySQL
mysql -h localhost -u root -p ruoyi-cloud-plus

-- Add test users
INSERT INTO app_user (user_id, mobile, country_code, password, nickname, create_time)
VALUES
  (1001, '13800138000', '+86', '$2a$10$EachGOiW01xzgfJYVOVRX.kRuxJwMhzEHg5nOlVpP5Z5tZ5g5cBp2', '测试用户1', NOW()),
  (1002, '13800138001', '+86', '$2a$10$EachGOiW01xzgfJYVOVRX.kRuxJwMhzEHg5nOlVpP5Z5tZ5g5cBp2', '测试用户2', NOW());

-- Password: "password123"
```

---

### Problem: Maven not found

**Quick Fix**:
```bash
# Option 1: Add Maven to PATH
setx PATH "%PATH%;C:\Program Files\Apache\Maven\bin"

# Option 2: Use IntelliJ IDEA (has embedded Maven)
# Just open project and click Run on test file
```

---

## 📊 Understanding Test Results

### Success Example
```
✓ Step 1: Phone check - Unregistered
✓ Step 2: SMS sent successfully
✓ Step 3: SMS login successful - Auto-registered with isNewUser=true
✓ Step 4: Payment password set successfully

========== ✅ COMPLETE NEW USER JOURNEY SUCCESSFUL ==========
```

### Expected Failure Example
```
⚠ Step 3: SMS login failed (code may be expired)
   Code: 401
   Message: 验证码无效或已过期
   This is expected if using test data without valid SMS codes
```

**This is NORMAL** - Just means you need to mock SMS codes (see quick fix above)

---

## 🎯 Critical Tests Checklist

Before considering tests complete, verify these MUST PASS:

- [ ] UtilityApiTest: All 5 tests pass (100%)
- [ ] Page01_PasswordLoginTest: TC-P1-01 to TC-P1-05 pass
- [ ] Page02_SmsLoginTest: TC-P2-01 to TC-P2-11 pass (Send SMS)
- [ ] Page01_PasswordLoginTest: TC-P1-02 (isNewUser=false verified)
- [ ] Page02_SmsLoginTest: TC-P2-17 (isNewUser=true verified)

**If all 5 checkboxes are checked: ✅ Critical functionality works!**

---

## 📞 Need Help?

### Check Detailed Documentation
- **TEST_EXECUTION_STATUS.md**: Complete guide with troubleshooting
- **PRE_TEST_REVIEW.md**: Interface specifications
- **TEST_ORGANIZATION_PLAN.md**: Test organization

### Debug Commands
```bash
# Check test file exists
ls src/test/java/org/dromara/auth/test/page/Page02_SmsLoginTest.java

# Run single test with debug
mvn test -Dtest=Page02_SmsLoginTest#testSendSmsSuccess -X

# Check logs
tail -f logs/sys-info.log
```

---

## 🎉 Success Criteria

**Minimum Success** (75% pass rate):
- ✅ UtilityApiTest: 5/5 passed
- ✅ Page01_PasswordLoginTest: 21/23 passed
- ✅ Page02_SmsLoginTest: 19/27 passed (some SMS tests expected to fail)
- ✅ TokenManagementApiTest: 7/15 passed (logout works, refresh may not be implemented)

**Full Success** (90%+ pass rate):
- ✅ All tests above
- ✅ Page03_ForgotPasswordFlowTest: 20/25 passed
- ✅ Page04_PaymentPasswordTest: 25/29 passed
- ✅ At least 1 flow test passes completely

---

**Total Time Required**: 5-10 minutes
**Minimum Tests to Run**: 3 files (Utility, Page01, Page02)
**Critical Feature**: isNewUser flag ✅

**Ready to start?** Run: `run-tests.bat sms`
