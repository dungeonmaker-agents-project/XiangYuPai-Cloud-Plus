# 📝 XyPai-Auth Nacos Configuration Import Guide

## 🎯 Quick Steps

### 1. Access Nacos Console
```
URL: http://localhost:8848/nacos
Username: nacos
Password: nacos
```

### 2. Import Configuration File

1. Click **"配置管理"** → **"配置列表"**
2. Click **"导入配置"** (Import Configuration) button
3. Select file: `script/config/nacos/xypai-auth.yml`
4. Confirm import

### 3. Verify Import

You should see a new configuration:
- **Data ID**: `xypai-auth.yml`
- **Group**: `DEFAULT_GROUP`
- **Format**: `YAML`

### 4. **IMPORTANT: Update Database Credentials**

Click **"编辑"** (Edit) on the `xypai-auth.yml` configuration and update:

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          # ⚠️ Change these values to match your MySQL setup
          url: jdbc:mysql://YOUR_MYSQL_HOST:3306/xypai_user?...
          username: YOUR_MYSQL_USERNAME
          password: YOUR_MYSQL_PASSWORD
```

**Common configurations:**
- Local development: `localhost:3306`, `root`, `root`
- Docker: `mysql:3306`, `root`, `yourpassword`
- Remote: `192.168.1.100:3306`, `xypai`, `xypai123`

### 5. Publish Changes

Click **"发布"** (Publish) to save the configuration.

---

## 🔍 Configuration Details

### Database Connection String Explained

```
jdbc:mysql://localhost:3306/xypai_user?
  useUnicode=true                   # Enable Unicode support
  &characterEncoding=utf8           # Use UTF-8 encoding
  &zeroDateTimeBehavior=convertToNull  # Handle zero dates
  &useSSL=true                      # Use SSL (set to false if not needed)
  &serverTimezone=GMT%2B8           # Timezone (GMT+8)
  &autoReconnect=true               # Auto reconnect on connection loss
  &rewriteBatchedStatements=true    # Optimize batch inserts
  &allowPublicKeyRetrieval=true     # Allow public key retrieval
```

### HikariCP Connection Pool Settings

```yaml
hikari:
  maxPoolSize: 20        # Maximum 20 connections
  minIdle: 10           # Minimum 10 idle connections
  connectionTimeout: 30000   # 30 seconds timeout
  idleTimeout: 600000       # 10 minutes idle timeout
  maxLifetime: 1800000      # 30 minutes max lifetime
```

---

## ✅ Testing the Configuration

After importing and updating the configuration:

### Test 1: Start XyPai-Auth

```bash
cd xypai-auth
mvn spring-boot:run
```

**Expected output:**
```
(♥◠‿◠)ﾉﾞ  XyPai认证授权中心启动成功 - 端口9211  ლ(´ڡ`ლ)ﾞ
```

### Test 2: Check Database Connection

Look for this log message:
```
HikariDataSource - HikariPool-1 - Start completed.
```

### Test 3: Verify MyBatis Mapper Loading

Look for:
```
Mapped "{[/auth/login]}" onto ...
```

---

## ⚠️ Common Issues & Solutions

### Issue 1: "Communications link failure"
**Cause:** MySQL is not running or wrong host/port  
**Solution:** 
```bash
# Check MySQL is running
mysql -u root -p -e "SELECT 1"

# Or start MySQL service
# Windows: net start MySQL80
# Linux: sudo systemctl start mysql
```

### Issue 2: "Access denied for user"
**Cause:** Wrong username/password  
**Solution:** Update credentials in Nacos config

### Issue 3: "Unknown database 'xypai_user'"
**Cause:** Database doesn't exist  
**Solution:**
```sql
CREATE DATABASE xypai_user 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

### Issue 4: "Table 'xypai_user.xy_user' doesn't exist"
**Cause:** Table not created  
**Solution:** Run the SQL schema:
```bash
mysql -u root -p xypai_user < xypai-user/sql/00_SINGLE_TABLE_SIMPLIFIED.sql
```

---

## 🎓 Advanced Configuration

### Production Settings

For production, adjust these values:

```yaml
spring:
  datasource:
    dynamic:
      p6spy: false  # Disable SQL logging in production
      hikari:
        maxPoolSize: 50  # Increase for high traffic
        minIdle: 20

logging:
  level:
    org.dromara.auth: INFO  # Less verbose logging
```

### SSL Connection

If using SSL for MySQL:

```yaml
url: jdbc:mysql://localhost:3306/xypai_user?useSSL=true&requireSSL=true&verifyServerCertificate=true
```

---

## 📚 Related Files

- `xypai-auth/src/main/resources/application.yml` - Base config
- `xypai-auth/DEPLOYMENT_CHECKLIST.md` - Full deployment guide
- `xypai-auth/README.md` - Module overview

---

**Once configuration is imported and database credentials updated, restart xypai-auth service!** 🚀


