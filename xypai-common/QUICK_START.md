# XiangYuPai Common Services - Quick Start Guide

## 快速开始指南

本文档提供快速启动和使用 xypai-common 服务的步骤说明。

---

## 🚀 启动服务

### 1. 环境准备

确保以下服务已启动:
- ✅ MySQL 8.0+ (端口: 3306)
- ✅ Redis 7.0+ (端口: 6379)
- ✅ Nacos 2.x (端口: 8848)

### 2. 数据库初始化

```bash
# 连接MySQL
mysql -u root -p

# 执行初始化脚本
source script/sql/xypai_common.sql

# 验证表创建
USE xypai_common;
SHOW TABLES;
# 应显示: location, city, media_file, notification, report, punishment
```

### 3. Nacos配置

在Nacos配置中心添加以下配置:

**Data ID**: `xypai-common-dev.yml`
**Group**: `DEFAULT_GROUP`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_common?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0

# 业务配置
location:
  nearby-search-max-radius: 20  # 附近搜索最大半径(km)

media:
  max-image-size: 10485760      # 图片最大10MB
  max-video-size: 104857600     # 视频最大100MB

notification:
  batch-send-limit: 1000        # 批量发送限制

report:
  auto-ban-threshold: 10        # 自动封禁阈值(举报次数)
```

### 4. 启动服务

```bash
cd xypai-common
mvn clean package -DskipTests
java -jar target/xypai-common.jar --spring.profiles.active=dev
```

### 5. 验证启动成功

检查控制台输出:
```
========================================
XiangYuPai Common Services Started
享遇派通用服务已启动
========================================
Services included:
✓ Location Service      - 位置服务
✓ Media Upload Service  - 媒体上传服务
✓ Notification Service  - 通知服务
✓ Report Service        - 举报审核服务
========================================
```

检查Dubbo服务注册:
```bash
# 访问Nacos控制台
http://localhost:8848/nacos

# 检查服务列表，应该看到:
# - xypai-common (HTTP服务 - 端口9407)
# - Dubbo服务 (端口20807)
```

---

## 📡 使用RPC服务

### 在其他微服务中集成

#### 1. 添加依赖

在调用方微服务的 `pom.xml` 中添加:

```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>xypai-api-common</artifactId>
</dependency>

<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-common-dubbo</artifactId>
</dependency>
```

#### 2. 配置Dubbo

在调用方微服务的 `application.yml` 中配置:

```yaml
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: nacos://${nacos.server-addr}
    group: ${nacos.discovery.group:DUBBO_GROUP}
  consumer:
    check: false
    timeout: 3000
```

#### 3. 注入并使用RPC服务

```java
package org.dromara.user.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.api.location.RemoteLocationService;
import org.dromara.common.api.location.domain.DistanceVo;
import org.dromara.common.api.media.RemoteMediaService;
import org.dromara.common.api.notification.RemoteNotificationService;
import org.dromara.common.api.report.RemoteReportService;
import org.dromara.common.core.domain.R;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl {

    // 注入位置服务
    @DubboReference
    private RemoteLocationService remoteLocationService;

    // 注入媒体服务
    @DubboReference
    private RemoteMediaService remoteMediaService;

    // 注入通知服务
    @DubboReference
    private RemoteNotificationService remoteNotificationService;

    // 注入举报服务
    @DubboReference
    private RemoteReportService remoteReportService;

    /**
     * 示例1: 计算用户距离
     */
    public void calculateUserDistance(Long userId, Long targetUserId) {
        // 获取用户位置
        BigDecimal userLat = BigDecimal.valueOf(39.9);
        BigDecimal userLng = BigDecimal.valueOf(116.4);

        // 获取目标用户位置
        BigDecimal targetLat = BigDecimal.valueOf(31.2);
        BigDecimal targetLng = BigDecimal.valueOf(121.5);

        // 调用RPC接口
        R<DistanceVo> result = remoteLocationService.calculateDistance(
            userLat, userLng, targetLat, targetLng
        );

        if (result.isSuccess()) {
            DistanceVo distance = result.getData();
            System.out.println("距离: " + distance.getDisplayText());
        }
    }

    /**
     * 示例2: 发送点赞通知
     */
    public void likePost(Long userId, Long postId, Long postAuthorId) {
        // 1. 执行点赞逻辑
        // ...

        // 2. 发送通知
        if (!userId.equals(postAuthorId)) {
            R<Boolean> result = remoteNotificationService.sendLikeNotification(
                postAuthorId,  // 接收通知的用户
                userId,        // 点赞者
                "post",        // 内容类型
                postId         // 帖子ID
            );

            if (result.isSuccess()) {
                System.out.println("通知发送成功");
            }
        }
    }

    /**
     * 示例3: 检查用户是否可以发布
     */
    public R<Void> createPost(Long userId, String content) {
        // 1. 检查用户状态
        R<Boolean> canPost = remoteReportService.canUserPost(userId);
        if (!canPost.isSuccess() || !canPost.getData()) {
            return R.fail("您的账号已被封禁或禁言，无法发布内容");
        }

        // 2. 执行发布逻辑
        // ...

        return R.ok();
    }

    /**
     * 示例4: 关联媒体文件
     */
    public void publishPostWithImages(Long postId, Long[] imageIds) {
        // 关联图片到帖子
        for (Long imageId : imageIds) {
            R<Boolean> result = remoteMediaService.bindFileToBiz(
                imageId,
                "post",   // 业务类型
                postId    // 业务ID
            );

            if (!result.isSuccess()) {
                System.err.println("关联图片失败: " + imageId);
            }
        }
    }
}
```

---

## 🧪 测试验证

### 1. 测试位置服务

```bash
# 测试附近地点查询
curl -X GET "http://localhost:9407/api/location/nearby?latitude=39.9&longitude=116.4&radius=5" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试城市列表
curl -X GET "http://localhost:9407/api/city/list"
```

### 2. 测试媒体服务

```bash
# 测试图片上传
curl -X POST "http://localhost:9407/api/media/upload" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.jpg" \
  -F "bizType=post"
```

### 3. 测试通知服务

```bash
# 测试获取未读数
curl -X GET "http://localhost:9407/api/notification/unread-count" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试查询通知列表
curl -X GET "http://localhost:9407/api/notification/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 测试举报服务

```bash
# 测试提交举报
curl -X POST "http://localhost:9407/api/report/submit" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportedUserId": 1001,
    "contentType": "post",
    "contentId": 2001,
    "reasonType": "spam",
    "reasonDetail": "垃圾广告内容"
  }'
```

---

## 📊 监控和健康检查

### 1. 服务健康检查

```bash
curl http://localhost:9407/actuator/health
```

预期响应:
```json
{
  "status": "UP"
}
```

### 2. Dubbo服务检查

访问Nacos控制台:
```
http://localhost:8848/nacos
```

在"服务管理 > 服务列表"中查看:
- **xypai-common**: HTTP服务 (9407)
- **Dubbo服务**: 应该看到4个Dubbo接口注册

### 3. 日志检查

```bash
# 查看实时日志
tail -f logs/xypai-common.log

# 查看Dubbo调用日志
grep "RPC调用" logs/xypai-common.log
```

---

## 🐛 常见问题

### 问题1: 服务启动失败

**原因**: 数据库连接失败

**解决**:
```bash
# 检查MySQL是否启动
systemctl status mysql

# 检查数据库是否存在
mysql -u root -p -e "SHOW DATABASES LIKE 'xypai_common';"

# 检查Nacos配置是否正确
```

### 问题2: Dubbo服务未注册

**原因**: Nacos连接失败

**解决**:
```bash
# 检查Nacos是否启动
curl http://localhost:8848/nacos/

# 检查application.yml中的Nacos地址配置
# 检查dubbo.registry.address配置
```

### 问题3: RPC调用超时

**原因**: 网络延迟或服务处理慢

**解决**:
```yaml
# 在调用方增加超时时间
dubbo:
  consumer:
    timeout: 5000  # 改为5秒
```

### 问题4: 找不到RPC服务

**错误信息**: `No provider available for the service`

**解决**:
1. 检查 xypai-common 服务是否启动
2. 检查 Dubbo 服务是否注册到 Nacos
3. 检查调用方和提供方的 Nacos 配置是否一致
4. 检查 `dubbo.scan.base-packages` 配置是否正确

---

## 📚 更多文档

- **架构设计**: [DUBBO_IMPLEMENTATION_PLAN.md](./DUBBO_IMPLEMENTATION_PLAN.md)
- **完成报告**: [DUBBO_IMPLEMENTATION_COMPLETION.md](./DUBBO_IMPLEMENTATION_COMPLETION.md)
- **RPC API文档**: [API_DOCUMENTATION.md](../ruoyi-api/xypai-api-common/API_DOCUMENTATION.md)
- **接口测试**: [INTERFACE_COMPLIANCE_TEST.md](./INTERFACE_COMPLIANCE_TEST.md)

---

## 🆘 获取帮助

如遇到问题，请:
1. 查看日志文件: `logs/xypai-common.log`
2. 检查Nacos服务列表
3. 查阅上述文档
4. 联系技术支持团队

---

**文档版本**: v1.0
**创建日期**: 2025-11-14
**作者**: XiangYuPai Team
