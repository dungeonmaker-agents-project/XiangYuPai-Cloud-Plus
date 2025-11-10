# xypai-user 数据库脚本使用指南

## 📋 脚本清单

| 脚本文件 | 说明 | 执行顺序 |
|---------|------|---------|
| `01_create_database.sql` | 创建数据库 | 1️⃣ |
| `02_create_tables.sql` | 创建8张表 | 2️⃣ |
| `03_create_indexes.sql` | 创建索引 | 3️⃣ |
| `04_init_test_data.sql` | 初始化测试数据 | 4️⃣ |
| `99_verify.sql` | 验证数据库 | 5️⃣ |

---

## 🚀 快速开始

### 方法1：命令行执行（推荐）

```bash
# 进入SQL脚本目录
cd xypai-user/sql

# 执行所有脚本（按顺序）
mysql -u root -p < 01_create_database.sql
mysql -u root -p < 02_create_tables.sql
mysql -u root -p < 03_create_indexes.sql
mysql -u root -p < 04_init_test_data.sql
mysql -u root -p < 99_verify.sql
```

### 方法2：MySQL客户端执行

```bash
# 连接MySQL
mysql -u root -p

# 在MySQL命令行中执行
mysql> source C:/Users/Admin/Documents/GitHub/RuoYi-Cloud-Plus/xypai-user/sql/01_create_database.sql;
mysql> source C:/Users/Admin/Documents/GitHub/RuoYi-Cloud-Plus/xypai-user/sql/02_create_tables.sql;
mysql> source C:/Users/Admin/Documents/GitHub/RuoYi-Cloud-Plus/xypai-user/sql/03_create_indexes.sql;
mysql> source C:/Users/Admin/Documents/GitHub/RuoYi-Cloud-Plus/xypai-user/sql/04_init_test_data.sql;
mysql> source C:/Users/Admin/Documents/GitHub/RuoYi-Cloud-Plus/xypai-user/sql/99_verify.sql;
```

### 方法3：Navicat/DBeaver执行

1. 打开工具连接到MySQL
2. 依次打开SQL文件
3. 点击运行按钮执行

---

## 📊 数据库结构

### 核心表（8张）

```
xypai_user
├── user                 (19字段) - 用户基础表
├── user_profile         (34字段) - 用户资料表 ⭐
├── user_stats           (14字段) - 用户统计表 ⭐
├── occupation_dict      (7字段)  - 职业字典表 ⭐
├── user_occupation      (5字段)  - 用户职业关联表 ⭐
├── user_wallet          (9字段)  - 用户钱包表
├── transaction          (11字段) - 交易流水表
└── user_relation        (7字段)  - 用户关系表
```

### 测试数据

- ✅ **10个测试用户** (ID: 1-10)
- ✅ **20种职业类型** (艺术/教育/技术/医疗等)
- ✅ **22个职业关联** (用户多职业标签)
- ✅ **10个用户钱包** (余额/金币数据)
- ✅ **15个关注关系** (互相关注网络)
- ✅ **12条交易流水** (充值/消费/退款)

---

## 🔍 验证结果解读

执行 `99_verify.sql` 后，您应该看到：

### ✅ 正常输出

```
✅ 8张表创建成功
✅ 20+索引创建成功
✅ 7个外键约束正常
✅ 测试数据导入完成
✅ 数据完整性验证通过
```

### 📊 表数据统计

| 表名 | 行数 | 数据大小 | 说明 |
|------|------|---------|------|
| user | 10 | ~0.01 MB | 用户基础信息 |
| user_profile | 10 | ~0.02 MB | 用户详细资料 |
| user_stats | 10 | ~0.01 MB | 用户统计数据 |
| occupation_dict | 20 | ~0.01 MB | 职业字典 |
| user_occupation | 22 | ~0.01 MB | 职业关联 |
| user_wallet | 10 | ~0.01 MB | 用户钱包 |
| transaction | 12 | ~0.01 MB | 交易流水 |
| user_relation | 15 | ~0.01 MB | 用户关系 |

---

## ✅ 修复清单

### 相比原始脚本的改进

1. ✅ **补充 AUTO_INCREMENT**
   - `user`, `transaction`, `user_relation` 表的 `id` 字段

2. ✅ **补充 created_at 字段**
   - `user_wallet` 表

3. ✅ **优化外键约束**
   - 添加完整的外键命名
   - 统一使用 ON DELETE CASCADE

4. ✅ **优化索引**
   - 补充 `idx_last_edit`, `idx_deleted_at`, `idx_sync_time`
   - 在建表时直接创建关键索引

5. ✅ **完善注释**
   - 每个字段的详细说明
   - 业务规则注释

---

## 🎯 下一步操作

### 1. 配置应用连接

修改 Nacos 配置或本地配置文件：

```yaml
# script/config/nacos/xypai-user.yml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/xypai_user?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true&nullCatalogMeansCurrent=true
          username: root
          password: your_password_here
```

### 2. 启动应用

```bash
# 启动 xypai-user 服务
cd xypai-user
mvn spring-boot:run
```

### 3. 验证服务

访问 Knife4j 文档：
```
http://localhost:9401/doc.html
```

---

## 🔧 故障排查

### 问题1：找不到数据库

**错误信息**:
```
dynamic-datasource can not find primary datasource
```

**解决方案**:
```bash
# 检查数据库是否存在
mysql -u root -p -e "SHOW DATABASES LIKE 'xypai_user';"

# 如果不存在，执行
mysql -u root -p < 01_create_database.sql
```

### 问题2：外键约束失败

**错误信息**:
```
Cannot add foreign key constraint
```

**解决方案**:
1. 检查父表是否已创建
2. 检查字段类型是否一致
3. 按顺序执行脚本：`01 → 02 → 03 → 04`

### 问题3：唯一键冲突

**错误信息**:
```
Duplicate entry 'xxx' for key 'uk_xxx'
```

**解决方案**:
```sql
-- 清空数据库重新导入
DROP DATABASE IF EXISTS `xypai_user`;
-- 然后重新执行所有脚本
```

---

## 📚 相关文档

- `00_database_analysis.md` - 数据库分析报告
- `DATABASE_SETUP.md` - 数据库配置详细指南
- `DEPLOYMENT_GUIDE.md` - 完整部署文档
- `CODE_EXAMPLES.md` - 代码示例

---

## 🎯 测试用户账号

| 用户名 | 手机号 | 邮箱 | 昵称 | 角色 |
|--------|--------|------|------|------|
| alice_dev | 13800138001 | alice@xypai.com | Alice·全栈开发 | 开发者 |
| bob_designer | 13800138002 | bob@xypai.com | Bob·UI设计师 | 设计师 |
| diana_teacher | 13800138004 | diana@xypai.com | Diana·讲师 | VIP用户 |

**默认密码**: `123456` (BCrypt加密)

---

**状态**: ✅ 数据库脚本已修复，可直接使用  
**版本**: v7.1  
**最后更新**: 2025-10-20

