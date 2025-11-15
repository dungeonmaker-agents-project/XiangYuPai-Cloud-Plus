# Backend Testing Organization Plan

## 📋 Overview

This document organizes backend API tests according to **frontend pages and user flows**, ensuring comprehensive coverage of all user interactions described in the frontend documentation.

**Documentation References:**
- Frontend Page Docs: `e:\Users\Administrator\Documents\GitHub\XiangYuPai-Doc\Action-API\模块化架构\01-auth模块\Frontend\`
- Backend API Docs: `e:\Users\Administrator\Documents\GitHub\XiangYuPai-Doc\Action-API\模块化架构\01-auth模块\Backend\认证服务接口文档.md`
- Interface Corrections: `Frontend/INTERFACE_CORRECTIONS.md`

---

## 🎯 Test Organization Strategy

### Principle: Test by User Journey, Not by Technical Layer

Instead of organizing tests by controller/service, we organize by **frontend pages and user flows** to ensure:
1. ✅ Complete user journey coverage
2. ✅ Frontend-backend integration validation
3. ✅ Real-world scenario testing
4. ✅ Easy mapping to frontend requirements

---

## 📱 Test File Organization

### Directory Structure

```
xypai-auth/src/test/java/org/dromara/auth/
├── page/                                    # Tests organized by frontend pages
│   ├── Page01_PasswordLoginTest.java        # 01-密码登录页面.md
│   ├── Page02_SmsLoginTest.java             # 02-验证码登录页面.md
│   ├── Page03_ForgotPasswordFlowTest.java   # 03-忘记密码页面.md (3 steps)
│   └── Page04_PaymentPasswordTest.java      # 04-设置支付密码页面.md
├── flow/                                    # Tests for complete user flows
│   ├── NewUserRegistrationFlowTest.java     # New user: SMS login → Profile
│   ├── ExistingUserLoginFlowTest.java       # Existing user: Login → Home
│   ├── PasswordResetFlowTest.java           # Complete password reset
│   └── PaymentPasswordManagementFlowTest.java
├── api/                                     # Individual API endpoint tests
│   ├── TokenManagementApiTest.java          # Refresh, Logout
│   └── UtilityApiTest.java                  # Check phone
└── integration/                             # Cross-module integration
    └── AuthUserServiceIntegrationTest.java  # Auth + User service RPC
```

---

## 📄 Page 1: Password Login (01-密码登录页面.md)

### Frontend Documentation Path
`Frontend/01-密码登录页面.md`

### Test File
`page/Page01_PasswordLoginTest.java`

### APIs Tested
| API | Method | Path |
|-----|--------|------|
| Password Login | POST | `/api/auth/login/password` |

### Test Scenarios (Based on Frontend Doc)

#### Success Scenarios
- ✅ TC-P1-01: Valid credentials login success
- ✅ TC-P1-02: Response contains all required fields (accessToken, userId, nickname, avatar, isNewUser)
- ✅ TC-P1-03: Token is valid and can access protected endpoints

#### Validation Tests (Frontend Form Validation)
- ✅ TC-P1-04: Empty mobile number → 400
- ✅ TC-P1-05: Invalid mobile format → 400
- ✅ TC-P1-06: Mobile number too short (< 11 digits for +86) → 400
- ✅ TC-P1-07: Empty password → 400
- ✅ TC-P1-08: Password too short (< 6 characters) → 400
- ✅ TC-P1-09: Password too long (> 20 characters) → 400
- ✅ TC-P1-10: Terms not agreed (agreeToTerms = false) → 400

#### Authentication Failure Tests
- ✅ TC-P1-11: Wrong password → 401
- ✅ TC-P1-12: Non-existent user → 401/404
- ✅ TC-P1-13: Disabled account → 403

#### Security Tests (Frontend Security Requirements)
- ✅ TC-P1-14: SQL injection in mobile field → Safely handled
- ✅ TC-P1-15: XSS attempt in password field → Safely handled
- ✅ TC-P1-16: Verify password is BCrypt hashed (not plain text)

#### Field Mapping Tests (Interface Corrections)
- ✅ TC-P1-17: Verify `mobile` field works (not `phoneNumber`)
- ✅ TC-P1-18: Verify response uses `accessToken` (not `token`)
- ✅ TC-P1-19: Verify `expireIn` field is present
- ✅ TC-P1-20: Verify `refreshToken` field is present (if implemented)

#### Country Code Tests
- ✅ TC-P1-21: Login with +86 (China)
- ✅ TC-P1-22: Login with +852 (Hong Kong)
- ✅ TC-P1-23: Login with other supported country codes

**Total: ~23 test cases**

---

## 📄 Page 2: SMS Login (02-验证码登录页面.md)

### Frontend Documentation Path
`Frontend/02-验证码登录页面.md`

### Test File
`page/Page02_SmsLoginTest.java`

### APIs Tested
| API | Method | Path |
|-----|--------|------|
| Send SMS Code | POST | `/api/sms/send` |
| SMS Login | POST | `/api/auth/login/sms` |

### Test Scenarios

#### Send SMS Code Tests
- ✅ TC-P2-01: Send SMS for login type → Success
- ✅ TC-P2-02: Response contains codeId, expiresIn, nextSendTime
- ✅ TC-P2-03: Rate limiting - 60 second interval → 429
- ✅ TC-P2-04: Daily limit - 10 codes per day → 429
- ✅ TC-P2-05: Invalid mobile format → 400
- ✅ TC-P2-06: Empty mobile → 400
- ✅ TC-P2-07: Invalid type value → 400
- ✅ TC-P2-08: Verify correct API path `/api/sms/send` (not `/api/auth/sms/send`)
- ✅ TC-P2-09: Verify field name `type: "login"` (not `purpose: "LOGIN"`)
- ✅ TC-P2-10: Verify field name `mobile` (not `phoneNumber`)
- ✅ TC-P2-11: Verify field name `region` (not `countryCode`)

#### SMS Login - Existing User Tests
- ✅ TC-P2-12: Valid code for existing user → Login success
- ✅ TC-P2-13: Response has `isNewUser: false`
- ✅ TC-P2-14: Response contains correct nickname
- ✅ TC-P2-15: Response contains user avatar

#### SMS Login - New User Auto-Registration Tests
- ✅ TC-P2-16: Valid code for unregistered mobile → Auto-register + login
- ✅ TC-P2-17: Response has `isNewUser: true` **← CRITICAL**
- ✅ TC-P2-18: Auto-generated nickname format: `138****8000`
- ✅ TC-P2-19: New user created in database
- ✅ TC-P2-20: New user can login again with password

#### SMS Login - Verification Code Validation
- ✅ TC-P2-21: Wrong verification code → 401
- ✅ TC-P2-22: Expired verification code (> 5 minutes) → 401
- ✅ TC-P2-23: Non-existent code → 401
- ✅ TC-P2-24: Empty verification code → 400
- ✅ TC-P2-25: Invalid code format (not 6 digits) → 400

#### SMS Login - Auto-complete Tests (Frontend Behavior)
- ✅ TC-P2-26: Verify 6th digit auto-submits (simulated)
- ✅ TC-P2-27: Verify response time < 2 seconds

**Total: ~27 test cases**

---

## 📄 Page 3: Forgot Password Flow (03-忘记密码页面.md)

### Frontend Documentation Path
`Frontend/03-忘记密码页面.md`

### Test File
`page/Page03_ForgotPasswordFlowTest.java`

### APIs Tested
| API | Method | Path |
|-----|--------|------|
| Send Reset SMS | POST | `/api/sms/send` (type: "reset") |
| Verify Code | POST | `/api/auth/password/reset/verify` |
| Confirm Reset | POST | `/api/auth/password/reset/confirm` |

### Test Scenarios

#### Step 1: Send Reset SMS Tests
- ✅ TC-P3-01: Send SMS for registered mobile → Success
- ✅ TC-P3-02: Send SMS for unregistered mobile → 404 (important!)
- ✅ TC-P3-03: Verify type field is `"reset"` (lowercase)
- ✅ TC-P3-04: Rate limiting applies
- ✅ TC-P3-05: Invalid mobile format → 400

#### Step 2: Verify Code Tests
- ✅ TC-P3-06: Valid code → Verification success
- ✅ TC-P3-07: Wrong code → 401
- ✅ TC-P3-08: Expired code → 401
- ✅ TC-P3-09: Code for unregistered user → 404
- ✅ TC-P3-10: Empty mobile → 400
- ✅ TC-P3-11: Empty code → 400
- ✅ TC-P3-12: Verify `mobile` field (not `phoneNumber`)

#### Step 3: Confirm Reset Password Tests
- ✅ TC-P3-13: Valid reset with verified code → Success
- ✅ TC-P3-14: Reset without prior verification → 401
- ✅ TC-P3-15: Reuse verification code → 401 (token should be cleared)
- ✅ TC-P3-16: Invalid new password (too short) → 400
- ✅ TC-P3-17: Invalid new password (too long) → 400
- ✅ TC-P3-18: Pure numeric password → 400
- ✅ TC-P3-19: Empty new password → 400

#### Complete Flow Tests
- ✅ TC-P3-20: Complete flow: Send → Verify → Reset → Login with new password
- ✅ TC-P3-21: Verify old password no longer works after reset
- ✅ TC-P3-22: Verify data persistence across 3 steps

#### Security Tests
- ✅ TC-P3-23: SQL injection in password → Safely handled
- ✅ TC-P3-24: Special characters in password → Accepted
- ✅ TC-P3-25: Multiple verification attempts → All tracked

**Total: ~25 test cases**

---

## 📄 Page 4: Payment Password (04-设置支付密码页面.md)

### Frontend Documentation Path
`Frontend/04-设置支付密码页面.md`

### Test File
`page/Page04_PaymentPasswordTest.java`

### APIs Tested
| API | Method | Path |
|-----|--------|------|
| Set Payment Password | POST | `/api/auth/payment-password/set` |
| Update Payment Password | POST | `/api/auth/payment-password/update` |
| Verify Payment Password | POST | `/api/auth/payment-password/verify` |

### Test Scenarios

#### Set Payment Password Tests (First Time)
- ✅ TC-P4-01: Valid 6-digit password → Success
- ✅ TC-P4-02: Passwords match → Success
- ✅ TC-P4-03: Passwords mismatch → 400
- ✅ TC-P4-04: Not 6 digits → 400
- ✅ TC-P4-05: Contains non-digits → 400
- ✅ TC-P4-06: Empty password → 400
- ✅ TC-P4-07: Without authentication token → 401
- ✅ TC-P4-08: Invalid token → 401
- ✅ TC-P4-09: Already set → 400/409

#### Update Payment Password Tests
- ✅ TC-P4-10: Valid update with correct old password → Success
- ✅ TC-P4-11: Wrong old password → 401
- ✅ TC-P4-12: New passwords mismatch → 400
- ✅ TC-P4-13: New password same as old → 400
- ✅ TC-P4-14: Invalid new password format → 400
- ✅ TC-P4-15: Without authentication → 401
- ✅ TC-P4-16: Payment password not set yet → 404

#### Verify Payment Password Tests
- ✅ TC-P4-17: Correct password → verified: true
- ✅ TC-P4-18: Wrong password → verified: false
- ✅ TC-P4-19: Multiple wrong attempts (< 5) → Still allows retry
- ✅ TC-P4-20: Account lockout after 5 failures → 429/423
- ✅ TC-P4-21: Empty password → 400
- ✅ TC-P4-22: Invalid format → 400
- ✅ TC-P4-23: Without authentication → 401
- ✅ TC-P4-24: Payment password not set → 404

#### Security Tests
- ✅ TC-P4-25: Password stored as BCrypt hash
- ✅ TC-P4-26: Timing attack resistance (constant time comparison)
- ✅ TC-P4-27: SQL injection attempt → Safely handled

#### Frontend Interaction Tests
- ✅ TC-P4-28: Auto-submit after 6th digit (simulated)
- ✅ TC-P4-29: Two-step input flow (password → confirm)

**Total: ~29 test cases**

---

## 🔄 Additional API Tests

### Test File: `api/TokenManagementApiTest.java`

#### Refresh Token Tests
- ✅ TC-API-01: Valid refresh token → New tokens generated
- ✅ TC-API-02: New tokens are different from original
- ✅ TC-API-03: New access token is valid
- ✅ TC-API-04: Old access token still valid (or invalidated, depends on design)
- ✅ TC-API-05: Invalid refresh token → 401
- ✅ TC-API-06: Expired refresh token → 401
- ✅ TC-API-07: Empty refresh token → 400
- ✅ TC-API-08: Malformed refresh token → 401

#### Logout Tests
- ✅ TC-API-09: Valid logout → Success
- ✅ TC-API-10: Token invalidated after logout
- ✅ TC-API-11: Cannot use token after logout → 401
- ✅ TC-API-12: Refresh token also invalidated
- ✅ TC-API-13: Logout without token → 401
- ✅ TC-API-14: Logout with invalid token → 401
- ✅ TC-API-15: Double logout → 401

**Total: ~15 test cases**

---

### Test File: `api/UtilityApiTest.java`

#### Check Phone Tests
- ✅ TC-UTIL-01: Registered phone → isRegistered: true
- ✅ TC-UTIL-02: Unregistered phone → isRegistered: false
- ✅ TC-UTIL-03: Invalid phone format → 400
- ✅ TC-UTIL-04: Empty phone → 400
- ✅ TC-UTIL-05: Different country codes

**Total: ~5 test cases**

---

## 🌊 Complete User Flow Tests

### Test File: `flow/NewUserRegistrationFlowTest.java`

**Complete Journey: Unregistered User → Registration → Profile Setup**

```
1. Check phone (unregistered) → isRegistered: false
2. Send SMS (login type) → Code sent
3. SMS Login → Auto-register, isNewUser: true
4. Set payment password → Success
5. Verify payment password → Success
6. Logout → Success
```

---

### Test File: `flow/ExistingUserLoginFlowTest.java`

**Complete Journey: Registered User → Login → Operations → Logout**

```
1. Check phone (registered) → isRegistered: true
2. Password login → Success, isNewUser: false
3. Verify payment password → Success
4. Update payment password → Success
5. Refresh token → New tokens
6. Use new token → Success
7. Logout → Success
8. Verify token invalid → 401
```

---

### Test File: `flow/PasswordResetFlowTest.java`

**Complete Journey: Forgot Password → Reset → Login**

```
1. Send reset SMS → Success
2. Verify code → Success
3. Reset password → Success
4. Login with new password → Success
5. Verify old password doesn't work → 401
```

---

## 📊 Test Coverage Summary

| Category | Test File | Test Cases | APIs Covered |
|----------|-----------|-----------|--------------|
| Page 1 | Page01_PasswordLoginTest | ~23 | Password Login |
| Page 2 | Page02_SmsLoginTest | ~27 | Send SMS, SMS Login |
| Page 3 | Page03_ForgotPasswordFlowTest | ~25 | Send SMS, Verify, Reset |
| Page 4 | Page04_PaymentPasswordTest | ~29 | Set, Update, Verify Payment Password |
| Token API | TokenManagementApiTest | ~15 | Refresh, Logout |
| Utility API | UtilityApiTest | ~5 | Check Phone |
| Flow 1 | NewUserRegistrationFlowTest | 1 flow | Multi-API integration |
| Flow 2 | ExistingUserLoginFlowTest | 1 flow | Multi-API integration |
| Flow 3 | PasswordResetFlowTest | 1 flow | Multi-API integration |

**Total Estimated Test Cases: ~130+**

**All 11 Backend APIs Covered**: ✅

---

## 🎯 Testing Priorities

### P0 (Critical - Must Pass Before Frontend Handover)
1. Page 1: Password Login (all scenarios)
2. Page 2: SMS Login auto-registration + `isNewUser` flag
3. Page 3: Complete forgot password flow
4. API field name corrections (`mobile`, `accessToken`, `type`)

### P1 (High - Before Integration Testing)
1. Page 4: Payment password management
2. Token refresh mechanism
3. Complete user flows

### P2 (Medium - Before Production)
1. Security tests
2. Performance tests
3. Edge cases

---

## ✅ Success Criteria

Before marking testing complete, ensure:

- [ ] All frontend page scenarios covered
- [ ] All interface corrections validated (`mobile` vs `phoneNumber`, etc.)
- [ ] `isNewUser` flag correctly returned for SMS login
- [ ] Complete user flows work end-to-end
- [ ] All 11 APIs tested
- [ ] Security validations passed
- [ ] Error handling verified
- [ ] Frontend team confirms test coverage matches their needs

---

**Document Version**: v1.0
**Last Updated**: 2025-11-14
**Maintained By**: XyPai Backend Team
