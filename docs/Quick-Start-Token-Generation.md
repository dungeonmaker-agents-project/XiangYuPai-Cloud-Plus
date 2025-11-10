# ⚡ Quick Start: Generate Tokens in Any Microservice

> **Copy-paste ready examples for generating tokens in your microservices**

---

## 🚀 Quick Answer

**YES!** You can generate tokens in **any microservice** without calling the Auth Service!

```java
// In ANY microservice, you can do this:
@Autowired
private UniversalTokenService tokenService;

String token = tokenService.generateQuickToken(userId);
// Token is ready! It's stored in Redis and works across all services!
```

---

## 📦 Step 1: Copy the Service Class

Copy [`UniversalTokenService.java`](./UniversalTokenService-Example.java) to your microservice:

```
your-microservice/
  └─ src/main/java/org/dromara/common/service/
      └─ UniversalTokenService.java  ← Copy this file here
```

---

## 🎯 Step 2: Use in Your Controllers

### **Example 1: Service-to-Service Call**

```java
@Service
public class OrderService {
    
    @Autowired
    private UniversalTokenService tokenService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Order Service calls User Service
     */
    public UserInfo getUserInfo(Long userId) {
        // 1. Generate token for service call
        String token = tokenService.generateServiceToken("order-service");
        
        // 2. Call User Service with token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        ResponseEntity<UserInfo> response = restTemplate.exchange(
            "http://user-service/api/user/" + userId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            UserInfo.class
        );
        
        return response.getBody();
    }
}
```

---

### **Example 2: Admin Creates Token for User**

```java
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UniversalTokenService tokenService;
    
    /**
     * Admin generates token for a user (no password needed)
     */
    @PostMapping("/generate-token/{userId}")
    @SaCheckPermission("admin:token:generate")
    public R<String> generateTokenForUser(@PathVariable Long userId) {
        // Get current admin's ID
        Long adminId = LoginHelper.getUserId();
        
        // Generate token for target user
        String token = tokenService.generateAdminProxyToken(userId, adminId);
        
        return R.ok(token);
    }
}
```

---

### **Example 3: API Key Authentication**

```java
@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    
    @Autowired
    private UniversalTokenService tokenService;
    
    @Autowired
    private IApiKeyService apiKeyService;
    
    /**
     * Exchange API Key for Token
     */
    @PostMapping("/token")
    public R<TokenResponse> getToken(@RequestBody ApiKeyRequest request) {
        // 1. Validate API Key
        if (!apiKeyService.isValid(request.getApiKey())) {
            return R.fail("Invalid API Key");
        }
        
        // 2. Generate token
        String token = tokenService.generateApiToken(
            request.getApiKey(), 
            request.getAppName()
        );
        
        // 3. Return token
        TokenResponse response = new TokenResponse();
        response.setAccessToken(token);
        response.setExpireIn(7200L);  // 2 hours
        
        return R.ok(response);
    }
}
```

---

### **Example 4: Temporary Token for Email Verification**

```java
@Service
public class EmailService {
    
    @Autowired
    private UniversalTokenService tokenService;
    
    /**
     * Send email verification link
     */
    public void sendVerificationEmail(Long userId, String email) {
        // 1. Generate temporary token (10 minutes)
        String token = tokenService.generateTemporaryToken(userId, "email-verify");
        
        // 2. Build verification link
        String link = "https://your-site.com/verify?token=" + token;
        
        // 3. Send email
        sendEmail(email, "Verify Your Email", link);
    }
}
```

---

## 🔧 Step 3: Configuration Check

Make sure all your microservices have the **same configuration**:

### **application.yml (Must be identical across all services)**

```yaml
# Sa-Token Configuration (MUST BE IDENTICAL!)
sa-token:
  token-name: Authorization
  token-prefix: "Bearer"
  timeout: 1800           # ⚠️ Must be same
  active-timeout: -1      # ⚠️ Must be same
  is-concurrent: true
  is-share: false

# Redis Configuration (MUST USE SAME REDIS!)
spring:
  redis:
    host: 127.0.0.1       # ⚠️ Must be same
    port: 6379
    database: 0           # ⚠️ Must be same (very important!)
    password: yourpassword
```

---

## ✅ Verification

### **Test 1: Generate Token**

```java
@Test
public void testGenerateToken() {
    String token = tokenService.generateQuickToken(1L);
    
    assertNotNull(token);
    assertTrue(token.split("\\.").length == 3);  // JWT format
    
    System.out.println("✅ Token: " + token);
}
```

### **Test 2: Verify Token Works Across Services**

```java
// Service A generates token
String token = tokenService.generateServiceToken("service-a");

// Service B can validate this token
@GetMapping("/protected")
public R<String> protectedEndpoint() {
    // Gateway will validate the token automatically!
    Long userId = LoginHelper.getUserId();  // Works!
    return R.ok("User ID: " + userId);
}
```

---

## 🎯 Use Cases Summary

| Use Case | Method | Token Expiry |
|----------|--------|--------------|
| **Service-to-Service** | `generateServiceToken()` | 5 minutes |
| **Regular User** | `generateQuickToken()` | 30 minutes |
| **With Permissions** | `generateFullToken()` | 30 minutes |
| **API Integration** | `generateApiToken()` | 2 hours |
| **Email Verification** | `generateTemporaryToken()` | 10 minutes |
| **Admin Proxy** | `generateAdminProxyToken()` | 30 minutes |

---

## ⚠️ Security Notes

### **✅ DO**
```java
// ✅ Protect token generation endpoints
@SaCheckPermission("admin:token:generate")
public R<String> generateToken() { ... }

// ✅ Use short-lived tokens for service calls
model.setTimeout(300L);  // 5 minutes

// ✅ Log token generation for audit
log.info("Token generated by admin: {}", adminId);
```

### **❌ DON'T**
```java
// ❌ DON'T expose public token generation
@GetMapping("/public/generate-token")  // Anyone can call!
public String getToken() { ... }

// ❌ DON'T use long-lived tokens for services
model.setTimeout(86400L);  // 24 hours - too long!

// ❌ DON'T forget to validate in production
if (production && !hasPermission()) {
    throw new SecurityException();
}
```

---

## 📊 Performance Comparison

| Method | Network Calls | Response Time | Recommended |
|--------|---------------|---------------|-------------|
| **HTTP to Auth Service** | 2 (Gateway + Auth) | ~50ms | User Login |
| **Direct LoginHelper** | 0 (local call) | ~5ms | Service Calls |

**Result**: **10x faster!**

---

## 🆚 When to Use What?

### **Use Auth Service (HTTP) when:**
- ✅ User login from web/app
- ✅ Password verification needed
- ✅ Need centralized audit logs
- ✅ Need security policies enforcement

### **Use Direct Token Generation when:**
- ✅ Service-to-service calls
- ✅ Internal admin operations
- ✅ API key authentication
- ✅ SSO integration
- ✅ Temporary tokens (email verify, etc.)

---

## 🎉 Complete Example: Order Service

```java
package com.example.order;

import org.dromara.common.service.UniversalTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {
    
    @Autowired
    private UniversalTokenService tokenService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Place order - needs to call User Service and Product Service
     */
    public OrderResult placeOrder(PlaceOrderRequest request) {
        // 1. Generate service token
        String token = tokenService.generateServiceToken("order-service");
        
        // 2. Call User Service to verify user
        UserInfo user = callUserService(token, request.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }
        
        // 3. Call Product Service to check stock
        ProductInfo product = callProductService(token, request.getProductId());
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("Out of stock");
        }
        
        // 4. Create order
        Order order = createOrder(request, user, product);
        
        return new OrderResult(order.getId(), "Success");
    }
    
    private UserInfo callUserService(String token, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("clientid", "order-service");
        
        ResponseEntity<UserInfo> response = restTemplate.exchange(
            "http://user-service/api/user/" + userId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            UserInfo.class
        );
        
        return response.getBody();
    }
    
    private ProductInfo callProductService(String token, Long productId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("clientid", "order-service");
        
        ResponseEntity<ProductInfo> response = restTemplate.exchange(
            "http://product-service/api/product/" + productId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ProductInfo.class
        );
        
        return response.getBody();
    }
}
```

---

## ✅ Checklist

Before deploying to production:

- [ ] All services use same `sa-token` configuration
- [ ] All services connect to same Redis (same database!)
- [ ] Token generation endpoints are protected
- [ ] Short-lived tokens for service calls (5 minutes)
- [ ] Logging enabled for audit trail
- [ ] Error handling implemented
- [ ] Tests written and passing

---

## 📚 Related Documents

- [分布式Token生成模式](./分布式Token生成模式.md) - Full documentation (Chinese)
- [UniversalTokenService-Example.java](./UniversalTokenService-Example.java) - Complete service class
- [Token创建与存储机制详解](./Sa-Token创建与存储机制详解.md) - Technical deep dive

---

**You're now ready to generate tokens in any microservice!** 🚀

**Key takeaway**: With `LoginHelper` available in all services + shared Redis, you can generate tokens **anywhere, anytime**!

