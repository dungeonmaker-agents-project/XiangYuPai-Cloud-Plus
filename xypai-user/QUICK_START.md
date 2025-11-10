# 🚀 用户模块v7.1快速开始指南

## 📋 前置条件

- ✅ MySQL 8.0+
- ✅ Redis 7.0+
- ✅ JDK 21
- ✅ Maven 3.8+

---

## 1️⃣ 数据库升级（5分钟）

### Step 1: 连接数据库
```bash
mysql -u root -p
```

### Step 2: 执行升级脚本
```sql
USE xypai_user;
SOURCE /path/to/sql/user_module_upgrade_v7.1.sql;
```

### Step 3: 验证升级结果
```sql
-- 检查User表字段
DESCRIBE user;
-- 应该看到新字段: email, region_code, login_fail_count...

-- 检查UserProfile表字段
DESCRIBE user_profile;
-- 应该看到42个字段: gender, birthday, bio, height...

-- 检查新表是否创建
SHOW TABLES LIKE 'user_%';
-- 应该看到: user_stats, user_occupation

-- 检查职业字典数据
SELECT COUNT(*) FROM occupation_dict;
-- 应该返回: 20
```

---

## 2️⃣ 配置Redis（2分钟）

### 确认Redis连接
```yaml
# src/main/resources/application.yml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    password: # 如有密码请填写
```

### 测试Redis连接
```bash
redis-cli PING
# 应返回: PONG
```

---

## 3️⃣ 编译运行（3分钟）

### Step 1: 编译项目
```bash
cd xypai-modules/xypai-user
mvn clean compile
```

### Step 2: 运行测试
```bash
mvn test
```

### Step 3: 启动服务
```bash
mvn spring-boot:run
```

### Step 4: 验证服务
```bash
# 健康检查
curl http://localhost:9401/actuator/health

# 应返回: {"status":"UP"}
```

---

## 4️⃣ API测试（10分钟）

### 使用Knife4j（推荐）
```
访问: http://localhost:9401/doc.html
```

### 测试用户统计API

#### 1. 初始化用户统计
```bash
POST http://localhost:9401/api/v1/users/stats/init
Content-Type: application/json

{
  "userId": 1
}
```

#### 2. 查询用户统计
```bash
GET http://localhost:9401/api/v1/users/stats/1
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "followerCount": 0,
    "followingCount": 0,
    "contentCount": 0,
    "totalLikeCount": 0,
    "isActive": false,
    "isPopular": false
  }
}
```

#### 3. 增加粉丝数
```bash
PUT http://localhost:9401/api/v1/users/stats/1/follower/increment
```

#### 4. 查询人气用户TOP10
```bash
GET http://localhost:9401/api/v1/users/stats/popular?limit=10
```

---

## 5️⃣ Redis缓存验证（5分钟）

### 查看缓存数据
```bash
redis-cli

# 查看用户统计缓存
HGETALL user:stats:1

# 应返回:
# 1) "followerCount"
# 2) "1"
# 3) "followingCount"
# 4) "0"
# ...

# 查看缓存过期时间
TTL user:stats:1
# 应返回: 3600 (1小时)

# 查看所有用户统计缓存Key
KEYS user:stats:*
```

### 测试缓存命中
```bash
# 第一次查询（回源MySQL）
curl http://localhost:9401/api/v1/users/stats/1
# 响应时间: ~50ms

# 第二次查询（命中Redis）
curl http://localhost:9401/api/v1/users/stats/1
# 响应时间: ~5ms ✅ 提升10倍！
```

---

## 6️⃣ 职业标签测试

### 1. 查询所有职业
```bash
GET http://localhost:9401/api/v1/occupation/list
```

**预期响应**:
```json
{
  "code": 200,
  "data": [
    {
      "code": "model",
      "name": "模特",
      "category": "艺术",
      "sortOrder": 1
    },
    {
      "code": "student",
      "name": "学生",
      "category": "教育",
      "sortOrder": 2
    }
    // ... 共20个职业
  ]
}
```

### 2. 更新用户职业
```bash
PUT http://localhost:9401/api/v1/users/1/occupations
Content-Type: application/json

{
  "occupationCodes": ["model", "student", "designer"]
}
```

### 3. 查询用户职业
```bash
GET http://localhost:9401/api/v1/users/1/occupations
```

---

## 7️⃣ 资料完整度测试

### 1. 更新用户资料
```bash
PUT http://localhost:9401/api/v1/users/profile
Content-Type: application/json

{
  "userId": 1,
  "nickname": "张三",
  "avatar": "https://cdn.example.com/avatar.jpg",
  "gender": 1,
  "birthday": "1995-06-15",
  "cityId": 440300,
  "bio": "热爱生活，喜欢交友",
  "height": 175,
  "weight": 65
}
```

### 2. 查询资料完整度
```bash
GET http://localhost:9401/api/v1/users/profile/1
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "nickname": "张三",
    "profileCompleteness": 85,  // 自动计算
    "isProfileComplete": true   // >= 80%
  }
}
```

---

## 8️⃣ 性能监控

### 查看SQL执行计划
```sql
-- 检查索引使用情况
EXPLAIN SELECT * FROM user_profile 
WHERE city_id = 440300 AND is_vip = TRUE;

-- 应使用索引: idx_city_online 或 idx_vip

EXPLAIN SELECT * FROM user_stats 
WHERE follower_count > 1000 
ORDER BY follower_count DESC;

-- 应使用索引: idx_follower
```

### 监控Redis性能
```bash
redis-cli INFO stats

# 关键指标:
# keyspace_hits:1000    # 缓存命中
# keyspace_misses:100   # 缓存未命中
# 命中率 = 1000/(1000+100) = 90.9% ✅
```

### 查看慢查询日志
```sql
-- MySQL慢查询
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- 目标: P95 < 100ms
```

---

## 9️⃣ 常见问题

### Q1: 升级脚本执行失败？
```bash
# 检查数据库版本
SELECT VERSION();
# 需要 MySQL 8.0+

# 检查表是否存在
SHOW TABLES LIKE 'user';

# 如果metadata字段不存在，跳过数据迁移
# 直接执行 ALTER TABLE ADD COLUMN 部分
```

### Q2: Redis连接失败？
```bash
# 检查Redis服务
systemctl status redis

# 检查端口
netstat -an | grep 6379

# 检查配置
cat src/main/resources/application.yml | grep redis
```

### Q3: 缓存未命中？
```bash
# 检查Redis Key
redis-cli KEYS user:stats:*

# 手动刷新缓存
curl -X POST http://localhost:9401/api/v1/users/stats/1/refresh
```

### Q4: 统计数据不一致？
```sql
-- 手动同步MySQL
SELECT * FROM user_stats WHERE user_id = 1;

-- 对比Redis
redis-cli HGETALL user:stats:1

-- 强制刷新
redis-cli DEL user:stats:1
```

---

## 🎯 下一步

### 1. 实施单元测试
```bash
cd src/test/java
# 创建测试类
- UserStatsServiceTest.java
- OccupationServiceTest.java
```

### 2. 补充Controller接口
```java
// 新增接口
- GET /api/v2/user/stats/{userId}
- GET /api/v2/occupation/list
- PUT /api/v2/user/occupations
```

### 3. 前端集成
```javascript
// 调用统计API
axios.get('/api/v1/users/stats/1')
  .then(res => {
    console.log('粉丝数:', res.data.followerCount);
  });
```

### 4. 性能优化
- [ ] 配置Redis连接池
- [ ] 配置MySQL连接池
- [ ] 开启SQL日志监控
- [ ] 配置Sentinel限流

---

## 📚 参考文档

- [升级总结](USER_MODULE_UPGRADE_SUMMARY.md)
- [数据库设计](../../PL.md)
- [技术栈规范](../../.cursor/rules/AAAAAA_TECH_STACK_REQUIREMENTS.md)
- [Bob角色文档](../../.cursor/rules/ROLE_BACKEND_USER.md)

---

## 📞 技术支持

- **负责人**: Bob (后端用户服务专家)
- **邮件**: bob@xypai.com
- **文档**: 查看 `USER_MODULE_UPGRADE_SUMMARY.md`

---

**恭喜！您已成功部署用户模块v7.1！** 🎉

