# XiangYuPai Common Services - RPC API Documentation

## 模块概述

**xypai-api-common** 是享遇派通用服务的Dubbo RPC接口模块,定义了四个服务的远程调用接口。

### 模块结构

```
xypai-api-common/
├── pom.xml
└── src/main/java/org/dromara/common/api/
    ├── location/                           # 位置服务API
    │   ├── RemoteLocationService.java      # 位置服务远程接口
    │   └── domain/
    │       ├── LocationPointDto.java       # 地理位置点DTO
    │       ├── DistanceVo.java             # 距离信息VO
    │       └── CityInfoVo.java             # 城市信息VO
    ├── media/                              # 媒体服务API
    │   └── RemoteMediaService.java         # 媒体服务远程接口
    ├── notification/                       # 通知服务API
    │   └── RemoteNotificationService.java  # 通知服务远程接口
    └── report/                             # 举报服务API
        └── RemoteReportService.java        # 举报服务远程接口
```

---

## 1. Location Service API (位置服务)

### RemoteLocationService

**接口**: `org.dromara.common.api.location.RemoteLocationService`

#### 距离计算

##### calculateDistance
```java
R<DistanceVo> calculateDistance(BigDecimal fromLat, BigDecimal fromLng,
                                BigDecimal toLat, BigDecimal toLng);
```
**功能**: 计算两点之间的距离（Haversine公式）
**参数**:
- `fromLat`: 起点纬度
- `fromLng`: 起点经度
- `toLat`: 终点纬度
- `toLng`: 终点经度

**返回**: `DistanceVo`
```json
{
  "id": null,
  "distance": 15.32,
  "unit": "km",
  "displayText": "15.32km"
}
```

**使用场景**:
- 计算用户与活动地点的距离
- 计算两个用户之间的距离
- 距离排序

---

##### calculateBatchDistance
```java
R<List<DistanceVo>> calculateBatchDistance(BigDecimal fromLat, BigDecimal fromLng,
                                           List<LocationPointDto> targets);
```
**功能**: 批量计算从一个起点到多个目标点的距离
**参数**:
- `fromLat`: 起点纬度
- `fromLng`: 起点经度
- `targets`: 目标点列表

**返回**: `List<DistanceVo>`
```json
[
  {
    "id": 1001,
    "distance": 2.5,
    "unit": "km",
    "displayText": "2.5km"
  },
  {
    "id": 1002,
    "distance": 0.8,
    "unit": "km",
    "displayText": "800m"
  }
]
```

**使用场景**:
- 活动列表按距离排序
- 附近用户距离展示

---

#### 坐标验证

##### validateCoordinates
```java
R<Boolean> validateCoordinates(BigDecimal latitude, BigDecimal longitude);
```
**功能**: 验证GPS坐标有效性
**参数**:
- `latitude`: 纬度 (-90 ~ 90)
- `longitude`: 经度 (-180 ~ 180)

**返回**: `true`=有效, `false`=无效

**使用场景**:
- 用户位置更新前验证
- 活动地点创建时验证

---

#### 城市查询

##### getCityInfo
```java
R<CityInfoVo> getCityInfo(String cityCode);
```
**功能**: 根据城市代码获取城市信息
**参数**:
- `cityCode`: 城市代码 (如: "110100"=北京)

**返回**: `CityInfoVo`
```json
{
  "id": 1,
  "cityCode": "110100",
  "cityName": "北京",
  "province": "北京市",
  "pinyin": "beijing",
  "firstLetter": "B",
  "centerLat": 39.904989,
  "centerLng": 116.405285,
  "isHot": 1
}
```

---

## 2. Media Service API (媒体服务)

### RemoteMediaService

**接口**: `org.dromara.common.api.media.RemoteMediaService`

#### 文件查询

##### getFileUrl
```java
R<String> getFileUrl(Long fileId);
```
**功能**: 根据文件ID获取文件URL
**返回**: OSS文件访问URL

**使用场景**:
- 展示用户头像
- 加载帖子图片/视频

---

##### findFileByMd5
```java
R<String> findFileByMd5(String md5);
```
**功能**: 根据文件MD5查找已存在的文件（秒传）
**返回**: 文件URL（如果存在）

**使用场景**:
- 文件上传前检查是否已存在
- 实现秒传功能

---

#### 文件删除

##### deleteFile
```java
R<Boolean> deleteFile(Long fileId, Long userId);
```
**功能**: 删除媒体文件（带权限校验）
**参数**:
- `fileId`: 文件ID
- `userId`: 用户ID（只能删除自己的文件）

**使用场景**:
- 用户删除自己的照片
- 帖子删除时清理媒体文件

---

#### 业务关联

##### bindFileToBiz
```java
R<Boolean> bindFileToBiz(Long fileId, String bizType, Long bizId);
```
**功能**: 关联文件到业务对象
**参数**:
- `fileId`: 文件ID
- `bizType`: 业务类型 (post/moment/comment)
- `bizId`: 业务ID

**使用场景**:
- 发布帖子后关联图片
- 发布动态后关联视频

---

##### getFilesByBiz
```java
R<String[]> getFilesByBiz(String bizType, Long bizId);
```
**功能**: 查询业务对象关联的所有文件
**返回**: 文件URL数组

**使用场景**:
- 加载帖子的所有图片
- 展示动态的视频列表

---

## 3. Notification Service API (通知服务)

### RemoteNotificationService

**接口**: `org.dromara.common.api.notification.RemoteNotificationService`

#### 发送通知

##### sendLikeNotification
```java
R<Boolean> sendLikeNotification(Long userId, Long fromUserId,
                                String contentType, Long contentId);
```
**功能**: 发送点赞通知
**参数**:
- `userId`: 接收通知的用户ID
- `fromUserId`: 点赞者用户ID
- `contentType`: 被点赞内容类型 (post/moment/comment)
- `contentId`: 被点赞内容ID

**使用场景**:
- 用户A点赞用户B的帖子
- 用户A点赞用户B的评论

---

##### sendCommentNotification
```java
R<Boolean> sendCommentNotification(Long userId, Long fromUserId,
                                   String contentType, Long contentId, String commentText);
```
**功能**: 发送评论通知
**参数**:
- `commentText`: 评论内容摘要（用于通知预览）

**使用场景**:
- 用户A评论用户B的帖子
- 用户A回复用户B的评论

---

##### sendFollowNotification
```java
R<Boolean> sendFollowNotification(Long userId, Long fromUserId);
```
**功能**: 发送关注通知

**使用场景**:
- 用户A关注用户B

---

##### sendSystemNotification
```java
R<Boolean> sendSystemNotification(Long userId, String title, String content);
```
**功能**: 发送系统通知
**参数**:
- `title`: 通知标题
- `content`: 通知内容

**使用场景**:
- 系统维护通知
- 新功能上线通知
- 账号安全提醒

---

##### batchSendSystemNotification
```java
R<Boolean> batchSendSystemNotification(Long[] userIds, String title, String content);
```
**功能**: 批量发送系统通知

**使用场景**:
- 全员通知
- 批量消息推送

---

##### sendActivityNotification
```java
R<Boolean> sendActivityNotification(Long userId, Long activityId,
                                    String title, String content);
```
**功能**: 发送活动通知

**使用场景**:
- 活动即将开始提醒
- 活动报名成功通知

---

#### 查询未读数

##### getUnreadCount
```java
R<Long> getUnreadCount(Long userId);
```
**功能**: 获取用户未读通知总数

**使用场景**:
- 显示App角标数字
- 通知中心红点提示

---

##### getUnreadCountByType
```java
R<Long> getUnreadCountByType(Long userId, String type);
```
**功能**: 获取指定类型的未读数
**参数**:
- `type`: 通知类型 (like/comment/follow/system/activity)

**使用场景**:
- 分类显示未读数
- 点赞、评论、关注独立计数

---

## 4. Report Service API (举报服务)

### RemoteReportService

**接口**: `org.dromara.common.api.report.RemoteReportService`

#### 用户状态检查

##### isUserBanned
```java
R<Boolean> isUserBanned(Long userId);
```
**功能**: 检查用户是否被封禁
**返回**: `true`=已封禁, `false`=未封禁

**使用场景**:
- 用户登录时检查
- 发布内容前检查

---

##### isUserMuted
```java
R<Boolean> isUserMuted(Long userId);
```
**功能**: 检查用户是否被禁言
**返回**: `true`=已禁言, `false`=未禁言

**使用场景**:
- 发布评论前检查
- 发送消息前检查

---

##### canUserPost
```java
R<Boolean> canUserPost(Long userId);
```
**功能**: 检查用户是否可以发布内容（未封禁且未禁言）
**返回**: `true`=可以发布, `false`=不可以

**使用场景**:
- 发布帖子前统一检查
- 前端按钮权限控制

---

#### 内容审核

##### isContentReported
```java
R<Boolean> isContentReported(String contentType, Long contentId);
```
**功能**: 检查内容是否被举报过
**参数**:
- `contentType`: 内容类型 (post/moment/comment)
- `contentId`: 内容ID

**使用场景**:
- 内容展示时标记"已被举报"
- 审核工作流触发

---

##### getReportCount
```java
R<Integer> getReportCount(String contentType, Long contentId);
```
**功能**: 获取内容被举报次数

**使用场景**:
- 自动下架超过N次举报的内容
- 审核优先级排序

---

#### 处罚管理

##### banUser
```java
R<Boolean> banUser(Long userId, Integer duration, String reason);
```
**功能**: 封禁用户
**参数**:
- `duration`: 封禁时长（分钟，null=永久）
- `reason`: 封禁原因

**使用场景**:
- 管理员手动封禁
- 自动封禁违规用户

---

##### muteUser
```java
R<Boolean> muteUser(Long userId, Integer duration, String reason);
```
**功能**: 禁言用户
**参数**:
- `duration`: 禁言时长（分钟，null=永久）
- `reason`: 禁言原因

**使用场景**:
- 管理员手动禁言
- 自动禁言刷屏用户

---

##### unbanUser / unmuteUser
```java
R<Boolean> unbanUser(Long userId);
R<Boolean> unmuteUser(Long userId);
```
**功能**: 解除封禁/禁言

**使用场景**:
- 管理员手动解除
- 申诉成功后解除

---

#### 举报查询

##### getUserReportCount
```java
R<Integer> getUserReportCount(Long userId);
```
**功能**: 获取用户被举报次数

**使用场景**:
- 用户信用评分计算
- 风控规则判断

---

##### isDuplicateReport
```java
R<Boolean> isDuplicateReport(Long reporterId, String contentType, Long contentId);
```
**功能**: 检查用户是否重复举报
**返回**: `true`=已举报过, `false`=未举报过

**使用场景**:
- 举报提交前检查
- 防止重复举报

---

## 调用示例

### 示例1: 计算活动距离

```java
@DubboReference
private RemoteLocationService remoteLocationService;

public void calculateActivityDistance(Long userId, Long activityId) {
    // 获取用户位置
    BigDecimal userLat = userService.getUserLatitude(userId);
    BigDecimal userLng = userService.getUserLongitude(userId);

    // 获取活动位置
    Activity activity = activityService.getById(activityId);

    // 调用RPC接口计算距离
    R<DistanceVo> result = remoteLocationService.calculateDistance(
        userLat, userLng,
        activity.getLatitude(), activity.getLongitude()
    );

    if (result.isSuccess()) {
        DistanceVo distance = result.getData();
        System.out.println("距离: " + distance.getDisplayText());
    }
}
```

---

### 示例2: 发送点赞通知

```java
@DubboReference
private RemoteNotificationService remoteNotificationService;

public void likePost(Long userId, Long postId) {
    // 1. 执行点赞逻辑
    likeService.like(userId, postId);

    // 2. 获取帖子作者
    Post post = postService.getById(postId);
    Long authorId = post.getUserId();

    // 3. 发送通知（不给自己发）
    if (!userId.equals(authorId)) {
        remoteNotificationService.sendLikeNotification(
            authorId,      // 接收通知的用户
            userId,        // 点赞者
            "post",        // 内容类型
            postId         // 帖子ID
        );
    }
}
```

---

### 示例3: 发布前检查用户状态

```java
@DubboReference
private RemoteReportService remoteReportService;

public R<Void> createPost(Long userId, PostCreateBo bo) {
    // 1. 检查用户是否可以发布
    R<Boolean> canPost = remoteReportService.canUserPost(userId);
    if (!canPost.isSuccess() || !canPost.getData()) {
        return R.fail("您的账号已被封禁或禁言，无法发布内容");
    }

    // 2. 执行发布逻辑
    Post post = postService.create(bo);

    return R.ok();
}
```

---

### 示例4: 关联媒体文件

```java
@DubboReference
private RemoteMediaService remoteMediaService;

public void publishPost(Long userId, Long[] imageIds, String content) {
    // 1. 创建帖子
    Post post = new Post();
    post.setUserId(userId);
    post.setContent(content);
    postMapper.insert(post);

    // 2. 关联图片到帖子
    for (Long imageId : imageIds) {
        remoteMediaService.bindFileToBiz(imageId, "post", post.getId());
    }
}
```

---

## 依赖配置

### 在其他微服务中使用

#### 1. 添加依赖
```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>xypai-api-common</artifactId>
</dependency>
```

#### 2. 注入服务
```java
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.api.location.RemoteLocationService;

@Service
public class YourService {

    @DubboReference
    private RemoteLocationService remoteLocationService;

    // 使用服务...
}
```

---

## 总结

### 接口统计
- **Location Service**: 5个方法
- **Media Service**: 8个方法
- **Notification Service**: 10个方法
- **Report Service**: 12个方法
- **Total**: 35个RPC接口

### 适用场景
- ✅ 微服务间调用
- ✅ 异步任务调用
- ✅ 定时任务调用
- ✅ 第三方系统集成

---

**文档版本**: v1.0
**创建日期**: 2025-11-14
**作者**: XiangYuPai Team
