# 🚀 用户模块v7.1部署实施指南

> **负责人**: Bob + 运维团队  
> **部署时间**: 2025-01-20 凌晨2:00-5:00  
> **部署方式**: 灰度发布  
> **回滚方案**: 已准备

---

## 📋 部署前检查清单

### 环境检查
- [ ] MySQL 8.0.35+ 已安装
- [ ] Redis 7.0.12+ 已安装
- [ ] JDK 21.0.1+ 已安装
- [ ] Maven 3.8.6+ 已安装
- [ ] Nacos 2.2.0+ 已配置

### 数据备份
- [ ] 备份user表
- [ ] 备份user_profile表
- [ ] 备份user_wallet表
- [ ] 备份transaction表
- [ ] 备份user_relation表

### 代码准备
- [ ] 代码已提交Git
- [ ] 代码已通过Code Review
- [ ] 单元测试已通过（35/35）
- [ ] 集成测试已通过
- [ ] 性能测试已通过

---

## 🔧 部署步骤

### Step 1: 数据库升级（凌晨2:00-2:30）

#### 1.1 备份数据库
```bash
# 备份完整数据库
mysqldump -u root -p xypai_user > backup/xypai_user_20250120_backup.sql

# 验证备份文件
ls -lh backup/xypai_user_20250120_backup.sql
```

#### 1.2 执行升级脚本
```bash
# 连接数据库
mysql -u root -p

# 执行升级
USE xypai_user;
SOURCE /path/to/sql/user_module_upgrade_v7.1.sql;

# 预计时间：5-10分钟
```

#### 1.3 验证升级结果
```sql
-- 检查User表字段
DESCRIBE user;
-- 应该有19个字段（包含email, login_fail_count等）

-- 检查UserProfile表字段
DESCRIBE user_profile;
-- 应该有42个字段（包含gender, birthday, bio等）

-- 检查新表
SHOW TABLES LIKE 'user_%';
-- 应该包含：user, user_profile, user_stats, user_occupation, user_wallet, user_relation

-- 检查职业字典数据
SELECT COUNT(*) FROM occupation_dict;
-- 应该返回：20

-- 检查UserStats数据
SELECT COUNT(*) FROM user_stats;
-- 应该 > 0（已初始化）

-- 检查索引
SHOW INDEX FROM user_profile;
-- 应该包含：idx_city_online, idx_vip, idx_completeness等
```

---

### Step 2: 配置Redis（凌晨2:30-2:40）

#### 2.1 检查Redis服务
```bash
systemctl status redis
# 应该显示：active (running)

redis-cli PING
# 应该返回：PONG
```

#### 2.2 配置Redis（如需要）
```bash
# 编辑Redis配置
vi /etc/redis/redis.conf

# 关键配置
maxmemory 2gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10

# 重启Redis
systemctl restart redis
```

#### 2.3 验证Redis连接
```bash
redis-cli
> SELECT 0
> INFO stats
> exit
```

---

### Step 3: 编译部署服务（凌晨2:40-3:30）

#### 3.1 编译项目
```bash
cd /path/to/xypai-modules/xypai-user

# 清理编译
mvn clean compile -DskipTests

# 打包
mvn package -DskipTests

# 验证jar包
ls -lh target/xypai-modules-user-3.6.6.jar
```

#### 3.2 运行测试
```bash
# 运行单元测试
mvn test

# 预期结果：35/35 passed ✅
```

#### 3.3 部署服务
```bash
# 停止旧服务
systemctl stop xypai-user

# 备份旧jar
cp /app/xypai-user.jar /app/xypai-user.jar.backup.20250120

# 部署新jar
cp target/xypai-modules-user-3.6.6.jar /app/xypai-user.jar

# 启动服务
systemctl start xypai-user

# 查看日志
tail -f /app/logs/xypai-user.log
```

#### 3.4 等待服务启动（约30秒）
```bash
# 检查服务状态
systemctl status xypai-user

# 检查端口
netstat -tunlp | grep 9401
```

---

### Step 4: 功能验证（凌晨3:30-4:30）

#### 4.1 健康检查
```bash
# 服务健康检查
curl http://localhost:9401/actuator/health

# 预期响应：
# {"status":"UP"}

# Nacos注册检查
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-user

# 应该看到实例信息
```

#### 4.2 Swagger文档验证
```bash
# 浏览器访问
http://localhost:9401/doc.html

# 检查新增API：
# - /api/v1/users/stats/**  （10个接口）
# - /api/v1/occupation/**    （11个接口）
# - /api/v2/user/profile/**  （10个接口）
```

#### 4.3 数据库功能验证
```sql
-- 1. 查询用户统计
SELECT * FROM user_stats WHERE user_id = 1;

-- 2. 查询职业字典
SELECT * FROM occupation_dict ORDER BY sort_order LIMIT 5;

-- 3. 查询用户资料（新字段）
SELECT user_id, nickname, gender, birthday, bio, profile_completeness 
FROM user_profile WHERE user_id = 1;
```

#### 4.4 Redis缓存验证
```bash
redis-cli

# 查看缓存Key
> KEYS user:stats:*
> KEYS user:profile:*

# 查看统计缓存
> HGETALL user:stats:1

# 查看缓存过期时间
> TTL user:stats:1
# 应该返回：3600（1小时）
```

#### 4.5 API功能测试
```bash
# 测试1: 查询用户统计
curl http://localhost:9401/api/v1/users/stats/1

# 预期：返回完整统计数据

# 测试2: 查询职业列表
curl http://localhost:9401/api/v1/occupation/list

# 预期：返回20个职业

# 测试3: 更新用户职业
curl -X PUT http://localhost:9401/api/v1/occupation/current \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"occupationCodes": ["model", "student"]}'

# 预期：返回成功

# 测试4: 查询资料完整度
curl http://localhost:9401/api/v2/user/profile/current/completeness \
  -H "Authorization: Bearer YOUR_TOKEN"

# 预期：返回完整度信息
```

---

### Step 5: 性能监控（凌晨4:30-5:00）

#### 5.1 查看应用日志
```bash
# 实时日志
tail -f /app/logs/xypai-user.log

# 过滤统计相关日志
tail -f /app/logs/xypai-user.log | grep UserStats

# 检查错误日志
grep ERROR /app/logs/xypai-user.log
```

#### 5.2 监控Redis性能
```bash
redis-cli INFO stats

# 关键指标：
# keyspace_hits: 缓存命中次数
# keyspace_misses: 缓存未命中次数
# 命中率 = hits / (hits + misses)
# 目标：> 90%
```

#### 5.3 监控MySQL性能
```sql
-- 查看慢查询
SELECT * FROM mysql.slow_log 
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY query_time DESC;

-- 检查索引使用
EXPLAIN SELECT * FROM user_profile WHERE city_id = 440300;
-- 应该使用索引：idx_city_online

-- 检查表大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.TABLES
WHERE table_schema = 'xypai_user'
ORDER BY size_mb DESC;
```

#### 5.4 监控JVM性能
```bash
# 查看JVM状态
jstat -gcutil <PID> 1000 10

# 查看内存使用
jmap -heap <PID>

# Actuator监控
curl http://localhost:9401/actuator/metrics/jvm.memory.used
```

---

### Step 6: 灰度发布（上午9:00-12:00）

#### 阶段1: 5%流量（9:00-9:30）
```bash
# Nginx配置（5%流量到新服务）
upstream xypai_user {
    server 192.168.1.101:9401 weight=1;  # 新服务
    server 192.168.1.102:9401 weight=19; # 旧服务
}

# 重启Nginx
nginx -s reload

# 监控错误率
tail -f /app/logs/xypai-user.log | grep ERROR
```

#### 阶段2: 20%流量（9:30-10:00）
```bash
# 调整权重
weight=1  # 新服务（20%）
weight=4  # 旧服务（80%）
```

#### 阶段3: 50%流量（10:00-11:00）
```bash
# 调整权重
weight=1  # 新服务（50%）
weight=1  # 旧服务（50%）
```

#### 阶段4: 100%流量（11:00-12:00）
```bash
# 全部切换到新服务
weight=1  # 新服务（100%）
weight=0  # 旧服务（关闭）
```

---

## 🔄 回滚方案

### 触发条件
- [ ] 错误率 > 5%
- [ ] API响应时间 P95 > 1秒
- [ ] Redis缓存命中率 < 50%
- [ ] 数据一致性错误 > 1%
- [ ] 严重Bug

### 回滚步骤（5分钟完成）

#### Step 1: 立即切换流量
```bash
# Nginx切回旧服务
upstream xypai_user {
    server 192.168.1.102:9401 weight=1;  # 旧服务
    server 192.168.1.101:9401 weight=0;  # 新服务
}

nginx -s reload
```

#### Step 2: 停止新服务
```bash
systemctl stop xypai-user
```

#### Step 3: 回滚数据库（如需要）
```bash
# 恢复备份
mysql -u root -p xypai_user < backup/xypai_user_20250120_backup.sql

# 验证数据
mysql -u root -p -e "USE xypai_user; SELECT COUNT(*) FROM user;"
```

#### Step 4: 清理Redis缓存
```bash
redis-cli FLUSHDB

# 或删除特定Key
redis-cli --scan --pattern "user:stats:*" | xargs redis-cli DEL
```

---

## 📊 监控指标

### 关键指标阈值

| 指标 | 正常值 | 警告阈值 | 报警阈值 |
|------|--------|----------|----------|
| **API响应时间（P95）** | < 100ms | > 300ms | > 500ms |
| **错误率** | < 0.1% | > 1% | > 5% |
| **Redis命中率** | > 95% | < 80% | < 50% |
| **CPU使用率** | < 50% | > 70% | > 90% |
| **内存使用率** | < 60% | > 80% | > 95% |
| **数据库连接数** | < 20 | > 50 | > 80 |

### 监控命令

#### 实时监控API响应时间
```bash
tail -f /app/logs/xypai-user.log | grep "UserStats"
```

#### 实时监控错误日志
```bash
tail -f /app/logs/xypai-user.log | grep ERROR
```

#### 监控Redis性能
```bash
redis-cli --stat
# 每秒刷新统计信息
```

#### 监控MySQL性能
```bash
mysqladmin -u root -p -i 1 status
# 每秒刷新状态
```

---

## 🧪 功能测试脚本

### 自动化测试脚本
```bash
#!/bin/bash

echo "========== 用户模块v7.1功能测试 =========="

# 基础URL
BASE_URL="http://localhost:9401"

# 测试1: 健康检查
echo "测试1: 健康检查"
curl -s ${BASE_URL}/actuator/health | jq '.status'

# 测试2: 查询职业列表
echo "测试2: 查询职业列表"
curl -s ${BASE_URL}/api/v1/occupation/list | jq '.data | length'

# 测试3: 查询用户统计
echo "测试3: 查询用户统计"
curl -s ${BASE_URL}/api/v1/users/stats/1 | jq '.data.followerCount'

# 测试4: 人气排行榜
echo "测试4: 人气排行榜"
curl -s "${BASE_URL}/api/v1/users/stats/popular?limit=10" | jq '.data | length'

# 测试5: Redis缓存验证
echo "测试5: Redis缓存验证"
redis-cli EXISTS user:stats:1

echo "========== 测试完成 =========="
```

### 执行测试
```bash
chmod +x test_deployment.sh
./test_deployment.sh
```

---

## 📈 性能压测

### JMeter压测脚本

#### 测试用户统计API
```xml
<!-- thread_group.jmx -->
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">100</stringProp>
  <stringProp name="ThreadGroup.ramp_time">10</stringProp>
  <stringProp name="ThreadGroup.duration">60</stringProp>
</ThreadGroup>

<HTTPSampler>
  <stringProp name="HTTPSampler.domain">localhost</stringProp>
  <stringProp name="HTTPSampler.port">9401</stringProp>
  <stringProp name="HTTPSampler.path">/api/v1/users/stats/1</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
</HTTPSampler>
```

#### 运行压测
```bash
jmeter -n -t user_stats_test.jmx -l results.jtl

# 分析结果
jmeter -g results.jtl -o report/
```

#### 预期结果
```
并发用户: 100
测试时长: 60秒
总请求数: > 10000
成功率: > 99.9%
平均响应时间: < 50ms
P95响应时间: < 100ms
P99响应时间: < 200ms
```

---

## 🚨 告警配置

### Prometheus配置
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'xypai-user'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:9401']
```

### Grafana Dashboard
```
1. 导入Dashboard模板
2. 配置数据源（Prometheus）
3. 配置告警规则
```

### 告警规则
```yaml
groups:
  - name: xypai_user_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        annotations:
          summary: "用户模块错误率过高"

      - alert: SlowAPI
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 0.5
        annotations:
          summary: "API响应时间过慢"

      - alert: LowCacheHitRate
        expr: redis_keyspace_hits_total / (redis_keyspace_hits_total + redis_keyspace_misses_total) < 0.8
        annotations:
          summary: "Redis缓存命中率过低"
```

---

## ✅ 部署后验证

### 验证清单
- [ ] 服务启动成功
- [ ] Nacos注册成功
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] Swagger文档可访问
- [ ] 用户统计API可用
- [ ] 职业标签API可用
- [ ] 资料完整度API可用
- [ ] Redis缓存生效（命中率>90%）
- [ ] 统计数据准确
- [ ] 日志正常输出
- [ ] 无ERROR日志

### 业务验证
- [ ] 用户注册流程正常
- [ ] 用户登录流程正常
- [ ] 关注操作正常（统计更新）
- [ ] 资料编辑正常（完整度计算）
- [ ] 职业标签更新正常
- [ ] 在线状态更新正常

---

## 📞 应急联系

### 技术支持
- **Bob（开发）**: 138****5678
- **Jack（DBA）**: 139****6789
- **运维**: 137****1234

### 值班安排
```
凌晨2:00-5:00: Bob + Jack + 运维
上午9:00-12:00: Bob + 运维（灰度发布）
下午15:00-18:00: Bob（全量监控）
```

---

## 📝 部署日志

### 部署记录模板
```
部署时间：2025-01-20 02:00:00
执行人：Bob + 运维
部署内容：用户模块v7.1升级

【步骤1】数据库升级
- 备份完成：02:05
- 脚本执行：02:10-02:20
- 验证通过：02:25

【步骤2】Redis配置
- 连接验证：02:30
- 配置完成：02:35

【步骤3】服务部署
- 编译打包：02:40-03:00
- 服务部署：03:05-03:10
- 启动成功：03:15

【步骤4】功能验证
- API测试：03:20-03:40
- 缓存验证：03:45
- 全部通过：03:50

【步骤5】灰度发布
- 5%流量：09:00 ✅
- 20%流量：09:30 ✅
- 50%流量：10:00 ✅
- 100%流量：11:00 ✅

【结果】部署成功 ✅
```

---

## 🎯 成功标准

### 部署成功标准
- ✅ 所有服务正常启动
- ✅ 所有API测试通过
- ✅ Redis缓存命中率 > 90%
- ✅ 数据库查询正常
- ✅ 无ERROR日志
- ✅ 灰度发布平稳

### 运行成功标准（24小时）
- ✅ 服务可用性 > 99.9%
- ✅ API响应时间 P95 < 300ms
- ✅ 错误率 < 0.1%
- ✅ Redis缓存命中率 > 90%
- ✅ 无严重Bug

---

## 📚 相关文档

- [QUICK_START.md](QUICK_START.md) - 快速开始
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API文档
- [USER_MODULE_UPGRADE_SUMMARY.md](USER_MODULE_UPGRADE_SUMMARY.md) - 升级总结

---

**部署指南完整，按步骤执行即可！** 🚀

**祝部署顺利！** 🎉

