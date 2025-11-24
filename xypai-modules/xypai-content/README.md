# XiangYuPai Content Service (xypai-content)

## 📋 项目简介

向娱拍内容服务模块,提供动态Feed流、评论系统、话题管理、互动功能等核心业务能力。

**服务端口**: 9403
**数据库**: xypai_content
**版本**: 1.0.0

## 🚀 快速开始

### 1. 前置要求

- Java 21 (LTS)
- MySQL 8.0+
- Redis 7.0+
- Nacos 2.x (服务注册与配置中心)
- Maven 3.8+

### 2. 数据库初始化

```bash
# 1. 连接MySQL
mysql -u root -p

# 2. 执行建表脚本
source ./sql/xypai_content.sql

# 3. 验证数据库
USE xypai_content;
SHOW TABLES;
```

### 3. 配置Nacos

在Nacos控制台创建以下配置文件:

**配置文件**: `xypai-content-dev.yml`
**Group**: `XYPAI_GROUP`
**内容**:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_content?useSSL=false
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
      database: 1
```

### 4. 启动服务

```bash
# 方式1: Maven
mvn clean install
mvn spring-boot:run

# 方式2: JAR
java -jar target/xypai-content.jar

# 方式3: IDEA
直接运行 XyPaiContentApplication.main()
```

### 5. 验证服务

```bash
# 检查服务健康
curl http://localhost:9403/actuator/health

# 查看API文档
open http://localhost:9403/content/doc.html
```

## 📊 核心功能

### 1. 动态Feed流 (Feed Management)

- ✅ **关注Tab**: 查看关注用户的动态
- ✅ **热门Tab**: 基于热度算法推荐
- ✅ **同城Tab**: 基于地理位置推荐(Spatial查询)
- ✅ 支持文字、图片(最多9张)、视频(1个)
- ✅ 话题标签(最多5个)
- ✅ 地理位置标注
- ✅ 可见范围控制(公开/仅好友/仅自己)

### 2. 评论系统 (Comment System)

- ✅ **一级评论**: 支持置顶、多种排序方式
- ✅ **二级回复**: 嵌套结构,前3条自动展开
- ✅ 评论点赞
- ✅ 评论删除(级联删除二级回复)

### 3. 互动功能 (Interaction)

- ✅ **点赞**: 支持动态和评论点赞
- ✅ **收藏**: 收藏动态
- ✅ **分享**: 多渠道分享(微信/朋友圈/QQ/微博)
- ✅ 乐观更新UI(前端配合)

### 4. 话题管理 (Topic)

- ✅ 热门话题推荐
- ✅ 话题搜索
- ✅ 话题统计(参与人数、帖子数)

## 🔌 API接口列表

### Feed Management (4个接口)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取动态列表 | GET | `/api/v1/content/feed/{tabType}` | tabType: follow/hot/local |
| 获取动态详情 | GET | `/api/v1/content/detail/{feedId}` | 包含权限判断 |
| 发布动态 | POST | `/api/v1/content/publish` | 需要登录 |
| 删除动态 | DELETE | `/api/v1/content/{feedId}` | 仅作者可删除 |

### Comment Management (3个接口)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取评论列表 | GET | `/api/v1/content/comments/{feedId}` | 支持分页和排序 |
| 发布评论 | POST | `/api/v1/content/comment` | 支持一级和二级回复 |
| 删除评论 | DELETE | `/api/v1/content/comment/{commentId}` | 级联删除二级回复 |

### Interaction (3个接口)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 点赞/取消点赞 | POST | `/api/v1/interaction/like` | action: like/unlike |
| 收藏/取消收藏 | POST | `/api/v1/interaction/collect` | action: collect/uncollect |
| 分享 | POST | `/api/v1/interaction/share` | 记录分享渠道 |

## 🏗️ 项目结构

```
xypai-content/
├── src/main/java/org/dromara/content/
│   ├── XyPaiContentApplication.java          # 启动类
│   ├── controller/                            # 控制器层
│   │   ├── FeedController.java               # 动态管理
│   │   ├── CommentController.java            # 评论管理
│   │   └── InteractionController.java        # 互动管理
│   ├── service/                               # 服务层
│   │   ├── IFeedService.java                 # 接口定义
│   │   └── impl/
│   │       ├── FeedServiceImpl.java          # 实现类
│   │       ├── CommentServiceImpl.java
│   │       └── InteractionServiceImpl.java
│   ├── mapper/                                # 数据访问层
│   │   ├── FeedMapper.java
│   │   ├── CommentMapper.java
│   │   └── ...
│   └── domain/                                # 数据模型
│       ├── entity/                            # 数据库实体(8个)
│       ├── dto/                               # 请求对象(5个)
│       └── vo/                                # 响应对象(5个)
├── src/main/resources/
│   ├── application.yml                        # 主配置
│   ├── application-dev.yml                    # 开发环境配置
│   └── mapper/                                # MyBatis XML
├── sql/
│   └── xypai_content.sql                      # 数据库脚本
└── pom.xml                                    # Maven配置
```

## 💾 数据库设计

### 核心表结构

1. **feed** - 动态表 (包含空间索引)
2. **comment** - 评论表 (支持二级回复)
3. **topic** - 话题表
4. **feed_topic** - 动态话题关联表
5. **feed_media** - 动态媒体关联表
6. **like** - 点赞表 (联合唯一索引)
7. **collection** - 收藏表
8. **share** - 分享表

### 关键索引

- `feed.idx_location` - 空间索引,用于附近动态查询
- `like.uk_user_target` - 唯一索引,防止重复点赞
- `comment.idx_feed_id` - 复合索引,优化评论列表查询

## 🔧 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 应用框架 |
| MyBatis Plus | 3.5.7 | ORM框架 |
| Redis | 7.0+ | 缓存 |
| MySQL | 8.0+ | 数据库 |
| Sa-Token | Latest | 认证授权 |
| Dubbo | 3.x | RPC通信 |
| Nacos | 2.x | 服务注册与配置 |
| Knife4j | Latest | API文档 |

## 📈 缓存策略

### Redis缓存设计

```java
// 1. 动态详情缓存
Key: feed:detail:{feedId}
TTL: 10分钟
更新策略: 动态编辑/删除时清除

// 2. 评论列表缓存
Key: comment:list:{feedId}:page:{page}
TTL: 5分钟
更新策略: 新评论发布时清除

// 3. 浏览数计数器
Key: feed:view:count:{feedId}
TTL: 1天
更新策略: 定时同步到MySQL
```

## 🔐 安全机制

### 1. 认证授权

- Sa-Token JWT认证
- Gateway统一鉴权(Same-Token验证)
- 接口级权限控制

### 2. 数据安全

- 软删除机制(`deleted`字段)
- 乐观锁(`version`字段)
- 敏感词检测(TODO)

### 3. 频率限制

```java
@RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
```

- 发布动态: 10次/分钟
- 评论: 20次/分钟
- 点赞/收藏: 50次/分钟

## 📝 开发规范

### 1. 命名规范

- **实体类**: `Feed.java` (单数名词)
- **Mapper**: `FeedMapper.java`
- **Service接口**: `IFeedService.java`
- **Service实现**: `FeedServiceImpl.java`
- **Controller**: `FeedController.java`
- **DTO**: `FeedPublishDTO.java` (动词+名词+DTO)
- **VO**: `FeedListVO.java` (名词+VO)

### 2. 代码规范

- 使用Lombok简化代码
- 所有实体类使用`@Builder`
- 统一异常处理(`ServiceException`)
- 日志规范(使用SLF4J)

### 3. 数据库规范

- 表名小写,使用下划线分隔
- 必须有主键`id`
- 必须有`created_at`和`updated_at`
- 逻辑删除字段`deleted`
- 乐观锁字段`version`

## 🧪 测试

### API测试

访问Knife4j文档:
```
http://localhost:9403/content/doc.html
```

### 测试数据

数据库脚本已包含测试话题数据,可直接使用。

### 单元测试

```bash
mvn test
```

## 📦 部署

### Docker部署

```bash
# 构建镜像
docker build -t xypai-content:1.0.0 .

# 运行容器
docker run -d \
  -p 9403:9403 \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  -e NACOS_HOST=nacos \
  --name xypai-content \
  xypai-content:1.0.0
```

### K8s部署

```yaml
# TODO: 添加K8s deployment配置
```

## 🐛 常见问题

### 1. 服务无法启动

**问题**: 数据库连接失败
**解决**: 检查`application-dev.yml`中的数据库配置

### 2. Nacos注册失败

**问题**: 服务未在Nacos注册
**解决**:
1. 确认Nacos服务运行正常
2. 检查`bootstrap.yml`中的Nacos地址
3. 确认Group和Namespace配置正确

### 3. Same-Token验证失败

**问题**: 网关调用服务失败
**解决**:
1. 确保Gateway先启动(生成Same-Token)
2. 所有服务共用同一个Redis
3. 检查依赖中没有重复引入`ruoyi-common-satoken`

## 📚 相关文档

- [后端技术栈指南](../启动/BACKEND_TECH_STACK_GUIDE.md)
- [前端接口文档](../XiangYuPai-Doc/Action-API/模块化架构/03-content模块/)
- [RuoYi-Cloud-Plus官方文档](https://plus-doc.dromara.org/)

## 👥 维护者

- XiangYuPai Team
- Email: support@xiangyupai.com

## 📄 License

Copyright © 2025 XiangYuPai

---

**最后更新**: 2025-11-14
**文档版本**: v1.0.0
