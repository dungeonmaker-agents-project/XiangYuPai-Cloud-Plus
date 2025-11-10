# Nacos 配置上传指南

## 🎯 问题分析

**错误信息**:
```
[Nacos Config] config[dataId=xypai-user.yml, group=DEFAULT_GROUP] is empty
dynamic-datasource can not find primary datasource
```

**根本原因**: Nacos配置中心没有 `xypai-user.yml` 配置

---

## 🚀 解决方案

### 步骤1：登录 Nacos 控制台

访问: http://localhost:8848/nacos

**默认账号**:
- 用户名: `nacos`
- 密码: `nacos`

### 步骤2：创建配置

1. 点击左侧菜单 **配置管理** → **配置列表**
2. 点击右上角 **+** 按钮（创建配置）

**配置信息**:
```
Data ID:    xypai-user.yml
Group:      DEFAULT_GROUP
配置格式:    YAML
```

**配置内容**（直接复制粘贴）:
```yaml
# XY相遇派用户模块配置

# Sa-Token 配置
sa-token:
  check-same-token: false

# 数据源配置
spring:
  datasource:
    dynamic:
      primary: master
      strict: true
      datasource:
        master:
          type: com.zaxxer.hikari.HikariDataSource
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/xypai_user?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true
          username: root
          password: password

# Dubbo配置
dubbo:
  application:
    name: ${spring.application.name}
  protocol:
    name: dubbo
    port: -1
  registry:
    address: nacos://${spring.cloud.nacos.server-addr}
    group: ${spring.cloud.nacos.discovery.group}
    parameters:
      namespace: ${spring.cloud.nacos.discovery.namespace}
```

### 步骤3：修改数据库密码

⚠️ **重要**: 将上面配置中的 `password: password` 改成您实际的MySQL密码！

### 步骤4：发布配置

点击页面底部的 **发布** 按钮

---

## 📝 完整的Nacos配置清单

xypai-user服务需要以下3个配置文件：

| Data ID | Group | 说明 | 状态 |
|---------|-------|------|------|
| `xypai-user.yml` | DEFAULT_GROUP | 用户服务专属配置 | ⚠️ 需创建 |
| `datasource.yml` | DEFAULT_GROUP | 通用数据源配置 | ✅ 已存在 |
| `application-common.yml` | DEFAULT_GROUP | 通用应用配置 | ✅ 已存在 |

---

## 🔍 验证配置是否生效

### 1. 查看Nacos日志

重启 xypai-user 服务，应该看到：

```
[Nacos Config] Load config[dataId=xypai-user.yml, group=DEFAULT_GROUP] success  ✅
[Nacos Config] Load config[dataId=datasource.yml, group=DEFAULT_GROUP] success
[Nacos Config] Load config[dataId=application-common.yml, group=DEFAULT_GROUP] success
```

### 2. 查看数据源初始化日志

```
dynamic-datasource initial loaded [1] datasource  ✅
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

### 3. 查看服务启动成功

```
Started XyPaiUserApplication in X.XXX seconds  ✅
```

---

## 🛠️ 其他可能的问题

### 问题1：数据库不存在

**检查**:
```sql
SHOW DATABASES LIKE 'xypai_user';
```

**解决**:
```bash
cd xypai-user/sql
mysql -u root -p < 01_create_database.sql
mysql -u root -p < 02_create_tables.sql
mysql -u root -p < 03_create_indexes.sql
mysql -u root -p < 04_init_test_data.sql
```

### 问题2：MySQL密码错误

**检查**:
```bash
mysql -u root -p
# 输入密码测试连接
```

**解决**: 修改Nacos配置中的 `password` 字段

### 问题3：Nacos未启动

**检查**:
```bash
# 访问 http://localhost:8848/nacos
# 能打开则正常
```

**解决**:
```bash
cd ruoyi-visual/ruoyi-nacos
java -jar nacos-server.jar
```

---

## 📋 快速执行清单

- [ ] 1. 启动Nacos服务
- [ ] 2. 登录Nacos控制台 (http://localhost:8848/nacos)
- [ ] 3. 创建 `xypai-user.yml` 配置（复制上面的内容）
- [ ] 4. 修改数据库密码为实际密码
- [ ] 5. 发布配置
- [ ] 6. 执行SQL脚本创建数据库
- [ ] 7. 重启 xypai-user 服务
- [ ] 8. 验证启动成功

---

## 🎯 预期结果

配置正确后，启动日志应该显示：

```
✅ Nacos配置加载成功
✅ 数据源初始化成功  
✅ Dubbo服务注册成功
✅ Sa-Token配置加载成功
✅ 服务启动成功（端口9401）
✅ Knife4j文档可访问 (http://localhost:9401/doc.html)
```

---

**下一步**: 按照上述步骤上传配置到Nacos即可！

