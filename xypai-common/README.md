# XiangYuPai Common Services - Implementation Complete

## 项目概述

**xypai-common** 是享遇派平台的统一通用服务模块,整合了四个核心业务服务到单个Spring Boot微服务中。

### 架构设计

- **单一微服务架构**: 所有服务共享一个启动类 `CommonApplication.java`
- **统一数据库**: 使用 `xypai_common` 数据库存储所有服务数据
- **服务端口**: 9407
- **技术栈**: Spring Boot 3.2.0 + MyBatis-Plus 3.5.7 + Dubbo 3.x + Redis + Nacos

---

## 项目结构

```
xypai-common/
├── pom.xml                                      # ✅ Maven配置(JAR打包)
├── src/main/
│   ├── java/org/dromara/common/
│   │   ├── CommonApplication.java               # ✅ 统一启动类
│   │   │
│   │   ├── location/                            # ✅ 位置服务
│   │   │   ├── controller/app/
│   │   │   │   ├── LocationController.java      # ✅ 附近地点API
│   │   │   │   └── CityController.java          # ✅ 城市列表API
│   │   │   ├── service/
│   │   │   │   ├── ILocationService.java        # ✅ 位置服务接口
│   │   │   │   ├── ICityService.java            # ✅ 城市服务接口
│   │   │   │   └── impl/
│   │   │   │       ├── LocationServiceImpl.java # ✅ Haversine距离计算
│   │   │   │       └── CityServiceImpl.java     # ✅ 城市列表+缓存
│   │   │   ├── mapper/
│   │   │   │   ├── LocationMapper.java          # ✅ 地点数据访问
│   │   │   │   └── CityMapper.java              # ✅ 城市数据访问
│   │   │   └── domain/
│   │   │       ├── entity/
│   │   │       │   ├── Location.java            # ✅ 地点实体(支持空间索引)
│   │   │       │   └── City.java                # ✅ 城市实体
│   │   │       ├── vo/
│   │   │       │   ├── LocationListVo.java      # ✅ 地点列表视图
│   │   │       │   ├── CityInfoVo.java          # ✅ 城市信息视图
│   │   │       │   ├── CityGroupVo.java         # ✅ 城市分组视图
│   │   │       │   └── CityListResultVo.java    # ✅ 城市列表结果
│   │   │       └── bo/
│   │   │           ├── NearbyLocationQueryBo.java  # ✅ 附近查询参数
│   │   │           └── CitySelectBo.java           # ✅ 城市选择参数
│   │   │
│   │   ├── media/                               # ✅ 媒体上传服务
│   │   │   ├── controller/app/
│   │   │   │   └── MediaController.java         # ✅ 媒体上传API
│   │   │   ├── service/
│   │   │   │   ├── IMediaService.java           # ✅ 媒体服务接口
│   │   │   │   └── impl/
│   │   │   │       └── MediaServiceImpl.java    # ✅ OSS上传+MD5秒传
│   │   │   ├── mapper/
│   │   │   │   └── MediaFileMapper.java         # ✅ 媒体文件数据访问
│   │   │   └── domain/
│   │   │       ├── entity/
│   │   │       │   └── MediaFile.java           # ✅ 媒体文件实体
│   │   │       ├── vo/
│   │   │       │   ├── MediaFileVo.java         # ✅ 媒体文件视图
│   │   │       │   └── MediaUploadResultVo.java # ✅ 上传结果视图
│   │   │       └── bo/
│   │   │           └── MediaUploadBo.java       # ✅ 媒体上传参数
│   │   │
│   │   ├── notification/                        # ✅ 通知服务
│   │   │   ├── controller/app/
│   │   │   │   └── NotificationController.java  # ✅ 通知API
│   │   │   ├── service/
│   │   │   │   ├── INotificationService.java    # ✅ 通知服务接口
│   │   │   │   └── impl/
│   │   │   │       └── NotificationServiceImpl.java  # ✅ 通知CRUD+未读统计
│   │   │   ├── mapper/
│   │   │   │   └── NotificationMapper.java      # ✅ 通知数据访问
│   │   │   └── domain/
│   │   │       ├── entity/
│   │   │       │   └── Notification.java        # ✅ 通知实体
│   │   │       ├── vo/
│   │   │       │   ├── NotificationVo.java      # ✅ 通知视图
│   │   │       │   └── UnreadCountVo.java       # ✅ 未读数统计视图
│   │   │       └── bo/
│   │   │           ├── NotificationCreateBo.java     # ✅ 通知创建参数
│   │   │           └── NotificationQueryBo.java      # ✅ 通知查询参数
│   │   │
│   │   └── report/                              # ✅ 举报审核服务
│   │       ├── controller/
│   │       │   ├── app/
│   │       │   │   └── ReportController.java    # ✅ C端举报API
│   │       │   └── admin/
│   │       │       └── ReportManageController.java  # ✅ B端审核API
│   │       ├── service/
│   │       │   ├── IReportService.java          # ✅ 举报服务接口
│   │       │   └── impl/
│   │       │       └── ReportServiceImpl.java   # ✅ 举报提交+审核+处罚
│   │       ├── mapper/
│   │       │   ├── ReportMapper.java            # ✅ 举报数据访问
│   │       │   └── PunishmentMapper.java        # ✅ 处罚数据访问
│   │       └── domain/
│   │           ├── entity/
│   │           │   ├── Report.java              # ✅ 举报实体
│   │           │   └── Punishment.java          # ✅ 处罚实体
│   │           ├── vo/
│   │           │   └── ReportVo.java            # ✅ 举报视图
│   │           └── bo/
│   │               ├── ReportSubmitBo.java      # ✅ 举报提交参数
│   │               └── ReportReviewBo.java      # ✅ 举报审核参数
│   │
│   └── resources/
│       ├── application.yml                      # ✅ 应用配置
│       ├── bootstrap.yml                        # ✅ Nacos配置
│       └── logback-plus.xml                     # ✅ 日志配置
│
└── script/sql/
    └── xypai_common.sql                         # ✅ 数据库初始化脚本
```

---

## 已实现功能清单

### 1. Location Service (位置服务)

#### 核心功能
- ✅ **附近地点查询**: 基于经纬度 + 半径搜索附近POI
- ✅ **距离计算**: Haversine公式精确计算两点GPS距离
- ✅ **城市列表**: 热门城市 + 全部城市按首字母分组
- ✅ **Redis缓存**: 城市列表缓存24小时

#### API接口
| 端点 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/location/nearby` | GET | 获取附近地点 | user |
| `/api/location/search` | GET | 搜索地点 | 公开 |
| `/api/city/list` | GET | 获取城市列表 | 公开 |

#### 数据库表
- `location`: 地点表 (支持SPATIAL INDEX空间索引)
- `city`: 城市表 (10个热门城市初始化数据)

---

### 2. Media Upload Service (媒体上传服务)

#### 核心功能
- ✅ **OSS文件上传**: 集成阿里云/腾讯云对象存储
- ✅ **MD5秒传**: 相同文件自动秒传,节省存储
- ✅ **文件类型验证**: 支持image/video/audio
- ✅ **大小限制**: 图片10MB, 视频100MB
- ✅ **业务分类**: 按bizType分类存储 (avatar/post/moment/chat)

#### API接口
| 端点 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/media/upload` | POST | 上传媒体文件 | user |
| `/api/media/{id}` | DELETE | 删除媒体文件 | user |

#### 数据库表
- `media_file`: 媒体文件表 (记录URL、大小、MD5等)

---

### 3. Notification Service (通知服务)

#### 核心功能
- ✅ **通知CRUD**: 创建、查询、标记已读、删除
- ✅ **未读数统计**: 分类型统计未读数 (like/comment/follow/system/activity)
- ✅ **批量操作**: 批量标记已读、清空已读通知
- ✅ **全部已读**: 一键全部标记为已读

#### API接口
| 端点 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/notification/list` | GET | 查询通知列表 | user |
| `/api/notification/unread-count` | GET | 获取未读数统计 | user |
| `/api/notification/{id}/read` | PUT | 标记为已读 | user |
| `/api/notification/batch-read` | PUT | 批量标记已读 | user |
| `/api/notification/read-all` | PUT | 全部标记已读 | user |
| `/api/notification/{id}` | DELETE | 删除通知 | user |
| `/api/notification/clear-read` | DELETE | 清空已读通知 | user |

#### 数据库表
- `notification`: 通知表 (支持类型筛选、已读状态)

---

### 4. Report Service (举报审核服务)

#### 核心功能
- ✅ **举报提交**: 用户举报内容/用户 (spam/porn/violence/fraud/other)
- ✅ **重复举报检测**: 防止用户重复举报同一内容
- ✅ **举报审核**: B端管理员审核举报 (无效/警告/删除内容/封禁用户)
- ✅ **自动处罚**: 审核通过自动创建处罚记录
- ✅ **封禁管理**: 支持临时/永久封禁, 自动过期解除

#### API接口
| 端点 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/report/submit` | POST | 提交举报 | user |
| `/admin/report/review` | POST | 审核举报 | report:review |

#### 数据库表
- `report`: 举报表 (记录举报原因、审核结果)
- `punishment`: 处罚表 (记录封禁/禁言, 支持自动过期)

---

## 技术实现亮点

### 1. 统一微服务架构
- **单一启动类**: 所有服务共享 `CommonApplication.java`
- **包隔离设计**: location/media/notification/report独立包结构
- **依赖整合**: 一个POM管理所有依赖

### 2. 距离计算算法
```java
// Haversine公式实现GPS距离计算
public BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1,
                                   BigDecimal lat2, BigDecimal lon2) {
    final double R = 6371; // 地球半径(km)
    double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
    double dLon = Math.toRadians(lon2.subtract(lon1).doubleValue());

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
               Math.cos(Math.toRadians(lat1.doubleValue())) *
               Math.cos(Math.toRadians(lat2.doubleValue())) *
               Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return BigDecimal.valueOf(R * c).setScale(2, RoundingMode.HALF_UP);
}
```

### 3. MD5秒传实现
```java
// 计算文件MD5
String md5 = DigestUtil.md5Hex(file.getInputStream());

// 查找已存在文件
MediaUploadResultVo existingFile = findByMd5(md5);
if (existingFile != null) {
    return R.ok(existingFile);  // 秒传
}
```

### 4. Redis缓存优化
```java
// 城市列表缓存24小时
String cacheKey = "location:cities:all";
CityListResultVo cached = RedisUtils.getCacheObject(cacheKey);
if (cached != null) {
    return R.ok(cached);
}
// ... 查询数据库 ...
RedisUtils.setCacheObject(cacheKey, result, Duration.ofHours(24));
```

### 5. 自动过期处罚
```java
// 检查封禁是否过期
if (punishment.getEndTime() != null && punishment.getEndTime().before(new Date())) {
    punishment.setStatus(2);  // 2=已过期
    punishmentMapper.updateById(punishment);
    return false;
}
```

---

## 数据库设计

### 表结构概览
| 表名 | 记录数 | 索引类型 | 描述 |
|------|--------|----------|------|
| `location` | ~50K+ | SPATIAL INDEX | 地点表(支持空间索引) |
| `city` | ~400 | B-Tree | 城市表(热门城市+全部城市) |
| `media_file` | ~1M+ | B-Tree | 媒体文件表(OSS URL记录) |
| `notification` | ~10M+ | B-Tree | 通知表(分类型索引) |
| `report` | ~100K+ | B-Tree | 举报表(内容索引) |
| `punishment` | ~10K+ | B-Tree | 处罚表(用户+类型+状态联合索引) |

### 初始化数据
- **10个热门城市**: 北京、上海、广州、深圳、杭州、成都、武汉、南京、西安、重庆

---

## 部署说明

### 1. 数据库初始化
```bash
mysql -u root -p < script/sql/xypai_common.sql
```

### 2. 配置Nacos
在Nacos配置中心添加以下配置文件:
- `application-common.yml` - 公共配置
- `datasource.yml` - 数据源配置
- `xypai-common-dev.yml` - 开发环境配置

### 3. 启动服务
```bash
cd xypai-common
mvn clean package
java -jar target/xypai-common.jar --spring.profiles.active=dev
```

### 4. 验证启动
```bash
# 检查服务注册
curl http://localhost:9407/actuator/health

# 测试城市列表API
curl http://localhost:9407/api/city/list
```

---

## 性能优化建议

### 1. Location Service
- ✅ 已实现: Redis缓存城市列表
- TODO: 实现Geohash编码加速附近搜索
- TODO: 添加ElasticSearch全文搜索

### 2. Media Service
- ✅ 已实现: MD5秒传
- TODO: 实现CDN加速
- TODO: 添加图片压缩功能

### 3. Notification Service
- TODO: 实现WebSocket实时推送
- TODO: 添加消息队列异步处理

### 4. Report Service
- TODO: 添加机器学习自动审核
- TODO: 实现审核工作流

---

## API文档

### Swagger地址
启动后访问: `http://localhost:9407/doc.html`

---

## 总结

### 已完成 ✅
1. ✅ 统一微服务架构设计
2. ✅ Location Service完整实现
3. ✅ Media Service完整实现
4. ✅ Notification Service完整实现
5. ✅ Report Service完整实现
6. ✅ 配置文件创建
7. ✅ 数据库脚本
8. ✅ 所有Controller/Service/Mapper/Entity/VO/BO

### 待完成 ⏳
1. ⏳ Dubbo RPC接口定义 (ruoyi-api-common模块)
2. ⏳ MyBatis XML映射文件 (目前使用MyBatis-Plus自动生成)
3. ⏳ 单元测试
4. ⏳ 集成测试

### 预估工作量
- **已完成**: 核心业务逻辑 100%
- **待完成**: RPC接口定义 + 测试 (约4-6小时)

---

**文档版本**: v2.0 (Unified Microservice Architecture)
**完成日期**: 2025-11-14
**作者**: XiangYuPai Team
**总代码量**: 约3000行Java代码 + 400行SQL
