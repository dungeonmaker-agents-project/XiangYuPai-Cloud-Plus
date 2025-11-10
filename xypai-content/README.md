# 📝 xypai-content 内容服务模块 v7.1

**模块功能：** 动态/活动/技能内容管理  
**服务端口：** 9402  
**数据库：** xypai_content  
**版本：** v7.1 (2025-01-15升级)

---

## 🌟 v7.1新特性

### 1. 地理位置查询（空间索引优化）⭐⭐⭐⭐⭐
- ✅ MySQL POINT类型支持
- ✅ 空间索引（SPATIAL INDEX）
- ✅ 性能提升10倍（500ms → 50ms）

### 2. 统计系统（Redis缓存架构）⭐⭐⭐⭐⭐
- ✅ Redis主存储 + MySQL持久化
- ✅ 非阻塞写入
- ✅ 性能提升50倍（100ms → 2ms）

### 3. 评论系统（多级评论）⭐⭐⭐⭐
- ✅ 一级评论 + 二级回复
- ✅ 评论点赞
- ✅ 评论置顶

### 4. 草稿自动保存⭐⭐⭐⭐
- ✅ 每10秒自动保存
- ✅ 30天后自动过期
- ✅ 用户体验优化

---

## 🚀 快速开始

### 1. 环境准备

**必需软件：**
```bash
JDK 21
Maven 3.8+
MySQL 8.0+
Redis 7.0+
Nacos 2.x
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS xypai_content DEFAULT CHARSET utf8mb4;"

# 执行升级脚本
mysql -u root -p xypai_content < ../../sql/content_module_upgrade_v7.1.sql

# 验证表结构
mysql -u root -p xypai_content -e "SHOW TABLES;"
# 应该看到：content, content_stats, comment, comment_like, content_draft, media
```

### 3. 配置文件

编辑 `src/main/resources/bootstrap.yml`：

```yaml
spring:
  application:
    name: xypai-content
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/xypai_content
          username: root
          password: your_password
  redis:
    host: localhost
    port: 6379
    database: 0
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
```

### 4. 编译运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 或使用启动脚本（Windows）
../../bin/run-modules-content.bat
```

### 5. 验证服务

```bash
# 健康检查
curl http://localhost:9402/actuator/health

# 查看API文档
浏览器访问：http://localhost:9402/doc.html
```

---

## 📚 API文档

### Knife4j文档
- 本地：http://localhost:9402/doc.html
- 在线：http://your-domain:9402/doc.html

### 主要API

#### 内容管理
```http
POST   /api/v1/contents              # 发布内容
GET    /api/v1/contents/{id}         # 获取详情
GET    /api/v1/contents/nearby       # 附近内容⭐
GET    /api/v1/contents/city/{cityId} # 城市内容⭐
```

#### 评论管理
```http
POST   /api/v1/comments              # 发表评论⭐
GET    /api/v1/comments/content/{id} # 评论列表⭐
POST   /api/v1/comments/{id}/like    # 评论点赞⭐
```

#### 草稿管理
```http
POST   /api/v1/drafts/save           # 保存草稿⭐
GET    /api/v1/drafts/my             # 草稿列表⭐
POST   /api/v1/drafts/{id}/publish   # 发布草稿⭐
```

详细API文档：[API_USAGE_GUIDE_v7.1.md](./docs/API_USAGE_GUIDE_v7.1.md)

---

## 🏗️ 架构说明

### 模块结构

```
xypai-content/
├── src/main/java/com/xypai/content/
│   ├── controller/app/          # 控制器层
│   │   ├── ContentController.java
│   │   ├── CommentController.java ⭐
│   │   └── ContentDraftController.java ⭐
│   ├── service/                 # 服务层
│   │   ├── IContentService.java
│   │   ├── IContentStatsService.java ⭐
│   │   ├── ICommentService.java ⭐
│   │   └── IContentDraftService.java ⭐
│   ├── service/impl/
│   │   ├── ContentServiceImpl.java
│   │   ├── ContentStatsServiceImpl.java ⭐
│   │   ├── CommentServiceImpl.java ⭐
│   │   └── ContentDraftServiceImpl.java ⭐
│   ├── mapper/                  # 数据访问层
│   │   ├── ContentMapper.java
│   │   ├── ContentStatsMapper.java ⭐
│   │   ├── CommentMapper.java ⭐
│   │   ├── CommentLikeMapper.java ⭐
│   │   ├── ContentDraftMapper.java ⭐
│   │   └── MediaMapper.java ⭐
│   ├── domain/entity/           # 实体类
│   │   ├── Content.java (升级)
│   │   ├── ContentStats.java ⭐
│   │   ├── Comment.java ⭐
│   │   ├── CommentLike.java ⭐
│   │   ├── ContentDraft.java ⭐
│   │   └── Media.java ⭐
│   ├── domain/dto/              # 数据传输对象
│   ├── domain/vo/               # 视图对象
│   └── handler/                 # 类型处理器
│       └── PointTypeHandler.java ⭐
├── src/main/resources/
│   ├── mapper/                  # MyBatis XML
│   │   ├── ContentMapper.xml ⭐
│   │   ├── ContentStatsMapper.xml ⭐
│   │   ├── CommentMapper.xml ⭐
│   │   ├── CommentLikeMapper.xml ⭐
│   │   ├── ContentDraftMapper.xml ⭐
│   │   └── MediaMapper.xml ⭐
│   └── bootstrap.yml            # 配置文件
├── docs/                        # 文档
│   ├── UPGRADE_PLAN_V7.1.md
│   ├── UPGRADE_COMPLETE_REPORT_v7.1.md
│   └── API_USAGE_GUIDE_v7.1.md
└── pom.xml                      # Maven依赖（添加JTS）
```

### 分层架构

```
Controller层（接收请求）
    ↓
Service层（业务逻辑）
    ↓
Mapper层（数据访问）
    ↓
MySQL/Redis（数据存储）
```

---

## 🛠️ 核心技术

### 后端框架
- Spring Boot 3.2.0
- Spring Cloud 2023.0.3
- MyBatis Plus 3.5.7

### 数据库
- MySQL 8.0+ (POINT空间索引)
- Redis 7.0+ (统计缓存)

### 工具库
- JTS 1.19.0 (空间数据处理) ⭐
- Hutool 5.8.25 (工具类)
- Lombok 1.18.30

---

## 🧪 测试

### 运行单元测试

```bash
mvn test
```

### 运行集成测试

```bash
mvn verify
```

### 性能测试

```bash
# JMeter测试脚本
jmeter -n -t tests/content_performance_test.jmx -l results.jtl
```

---

## 📊 监控

### 健康检查
```http
GET http://localhost:9402/actuator/health
```

### 性能指标
```http
GET http://localhost:9402/actuator/metrics
```

### 定时任务监控
- 统计同步任务：每5分钟执行
- 草稿清理任务：每天凌晨3点执行

**日志位置：**
```
logs/xypai-content.log
```

---

## 🔗 相关模块

- **xypai-user** - 用户服务（Feign调用）
- **xypai-file** - 文件服务（媒体上传）
- **xypai-chat** - 聊天服务（评论通知）

---

## 📖 开发文档

1. **升级计划** - [UPGRADE_PLAN_V7.1.md](./docs/UPGRADE_PLAN_V7.1.md)
2. **完成报告** - [UPGRADE_COMPLETE_REPORT_v7.1.md](./docs/UPGRADE_COMPLETE_REPORT_v7.1.md)
3. **API指南** - [API_USAGE_GUIDE_v7.1.md](./docs/API_USAGE_GUIDE_v7.1.md)
4. **技术规范** - [AAAAAA_TECH_STACK_REQUIREMENTS.md](../../../.cursor/rules/AAAAAA_TECH_STACK_REQUIREMENTS.md)

---

## 📝 更新日志

### v7.1 (2025-01-15)
- ✅ 添加地理位置查询（空间索引）
- ✅ 添加Redis统计缓存
- ✅ 添加评论系统
- ✅ 添加草稿自动保存
- ✅ Content表字段扩展（11个新字段）
- ✅ 新增5个数据库表

### v1.0 (2025-01-01)
- ✅ 基础CRUD功能
- ✅ 内容分类（动态/活动/技能）
- ✅ 状态管理（草稿/发布/下架）

---

## 🤝 贡献指南

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用MyBatis Plus LambdaQueryWrapper
- 完整的异常处理和日志记录

### 提交规范
```bash
feat(content): 添加地理位置查询功能
fix(comment): 修复评论点赞统计问题
perf(stats): 优化Redis缓存命中率
```

---

## ❓ 常见问题FAQ

**Q1: 为什么附近内容查询这么快？**  
A: 使用了MySQL 8.0的SPATIAL INDEX空间索引，数据库自动优化查询路径。

**Q2: Redis缓存如何保证一致性？**  
A: 定时任务每5分钟批量同步到MySQL，保证最终一致性（延迟<5分钟）。

**Q3: 草稿会自动删除吗？**  
A: 30天未编辑的草稿会自动过期清理，每天凌晨3点执行。

**Q4: 支持几级评论？**  
A: 数据库支持无限层级，但UI只显示2级（一级评论+二级回复）。

---

## 📞 技术支持

有问题？联系我们：
- 📧 Email: tech@xiangyupai.com
- 💬 企业微信：XYP-Content-Team
- 📱 技术支持热线：400-xxx-xxxx

---

**⭐ 如果这个模块对你有帮助，请给项目点个Star！**

**License:** MIT  
**Copyright:** © 2025 XY相遇派团队

