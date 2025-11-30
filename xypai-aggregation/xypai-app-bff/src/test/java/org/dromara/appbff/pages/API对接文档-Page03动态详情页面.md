# Page03 动态详情页面 API 对接文档

> 对应前端页面: 03-动态详情页面
> 对应测试文件: Page03_FeedDetailTest.java
> 最后更新: 2025-11-29

## 一、通用说明

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 服务名 | xypai-content |
| 网关地址 | http://localhost:8080 |
| 服务端口 | 9403 |
| 接口前缀 | /xypai-content/api/v1 |

### 1.2 认证方式

所有接口需要在请求头中携带认证Token：

```
Authorization: Bearer {token}
```

Token通过 `/xypai-auth/api/auth/login/sms` 接口获取。

### 1.3 通用响应格式

```json
{
    "code": 200,
    "msg": "success",
    "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功 |
| msg | String | 响应消息 |
| data | Object | 响应数据 |

---

## 二、动态详情接口

### 2.1 获取动态详情

获取指定动态的完整信息，包括作者信息、互动数据等。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/detail/{feedId}` |
| Method | GET |
| 认证 | 需要 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |

**响应示例**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "feedId": 1001,
        "feedType": "image",
        "title": "今日王者荣耀五杀",
        "content": "终于拿到五杀了！太开心了！",
        "images": [
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        ],
        "videoUrl": null,
        "videoCover": null,
        "videoDuration": null,
        "author": {
            "userId": 10001,
            "nickname": "游戏达人",
            "avatar": "https://example.com/avatar.jpg",
            "gender": "male",
            "isVerified": true,
            "isFollowed": false
        },
        "topic": {
            "topicId": 1,
            "topicName": "王者荣耀",
            "icon": "https://example.com/topic-icon.png"
        },
        "stats": {
            "likeCount": 128,
            "commentCount": 32,
            "shareCount": 15,
            "collectCount": 8
        },
        "interaction": {
            "isLiked": false,
            "isCollected": false
        },
        "location": {
            "address": "上海市浦东新区",
            "latitude": 31.2304,
            "longitude": 121.4737
        },
        "createTime": "2025-11-29 10:30:00",
        "updateTime": "2025-11-29 10:30:00"
    }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| feedId | Long | 动态ID |
| feedType | String | 动态类型: image/video/text |
| title | String | 动态标题 |
| content | String | 动态内容 |
| images | List | 图片URL列表 |
| videoUrl | String | 视频URL (视频类型时) |
| videoCover | String | 视频封面 |
| videoDuration | Integer | 视频时长(秒) |
| author | Object | 作者信息 |
| topic | Object | 关联话题 |
| stats | Object | 统计数据 |
| interaction | Object | 当前用户互动状态 |
| location | Object | 位置信息 |
| createTime | String | 创建时间 |

---

### 2.2 发布动态

发布新的动态内容。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/publish` |
| Method | POST |
| Content-Type | application/json |
| 认证 | 需要 |

**请求参数**

```json
{
    "feedType": "image",
    "title": "今日王者荣耀五杀",
    "content": "终于拿到五杀了！太开心了！",
    "images": [
        "https://example.com/image1.jpg"
    ],
    "topicId": 1,
    "location": {
        "address": "上海市浦东新区",
        "latitude": 31.2304,
        "longitude": 121.4737
    }
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedType | String | 是 | 动态类型: image/video/text |
| title | String | 否 | 动态标题 |
| content | String | 是 | 动态内容 |
| images | List | 否 | 图片URL列表 (图片类型必填) |
| videoUrl | String | 否 | 视频URL (视频类型必填) |
| topicId | Long | 否 | 关联话题ID |
| location | Object | 否 | 位置信息 |

**响应示例**

```json
{
    "code": 200,
    "msg": "发布成功",
    "data": {
        "feedId": 1002
    }
}
```

---

### 2.3 删除动态

删除自己发布的动态。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/{feedId}` |
| Method | DELETE |
| 认证 | 需要 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |

**响应示例**

```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

**错误码**

| 错误码 | 说明 |
|--------|------|
| 404 | 动态不存在 |
| 403 | 无权删除他人动态 |

---

## 三、评论接口

### 3.1 获取评论列表

获取动态的评论列表，支持分页。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/comments/{feedId}` |
| Method | GET |
| 认证 | 需要 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |
| sortBy | String | 否 | hot | 排序方式: hot/new |

**响应示例**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "total": 32,
        "pageNum": 1,
        "pageSize": 10,
        "pages": 4,
        "hasNext": true,
        "list": [
            {
                "commentId": 2001,
                "content": "太厉害了！",
                "user": {
                    "userId": 10002,
                    "nickname": "路人甲",
                    "avatar": "https://example.com/avatar2.jpg"
                },
                "likeCount": 15,
                "isLiked": false,
                "replyCount": 3,
                "replies": [
                    {
                        "commentId": 2002,
                        "content": "确实厉害",
                        "user": {
                            "userId": 10003,
                            "nickname": "路人乙",
                            "avatar": "https://example.com/avatar3.jpg"
                        },
                        "replyTo": {
                            "userId": 10002,
                            "nickname": "路人甲"
                        },
                        "likeCount": 2,
                        "isLiked": false,
                        "createTime": "2025-11-29 11:00:00"
                    }
                ],
                "createTime": "2025-11-29 10:45:00"
            }
        ]
    }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| total | Long | 总评论数 |
| pageNum | Integer | 当前页码 |
| pageSize | Integer | 每页条数 |
| pages | Integer | 总页数 |
| hasNext | Boolean | 是否有下一页 |
| list | List | 评论列表 |
| list[].commentId | Long | 评论ID |
| list[].content | String | 评论内容 |
| list[].user | Object | 评论用户信息 |
| list[].likeCount | Integer | 点赞数 |
| list[].isLiked | Boolean | 当前用户是否已点赞 |
| list[].replyCount | Integer | 回复数 |
| list[].replies | List | 回复列表 (默认显示前3条) |

---

### 3.2 发表评论

对动态发表评论。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/comment` |
| Method | POST |
| Content-Type | application/json |
| 认证 | 需要 |

**请求参数**

```json
{
    "feedId": 1001,
    "content": "太厉害了！"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |
| content | String | 是 | 评论内容 (1-500字符) |

**响应示例**

```json
{
    "code": 200,
    "msg": "评论成功",
    "data": {
        "commentId": 2003,
        "content": "太厉害了！",
        "createTime": "2025-11-29 12:00:00"
    }
}
```

---

### 3.3 回复评论

回复指定的评论。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/comment` |
| Method | POST |
| Content-Type | application/json |
| 认证 | 需要 |

**请求参数**

```json
{
    "feedId": 1001,
    "parentId": 2001,
    "replyToUserId": 10002,
    "content": "同意！"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |
| parentId | Long | 是 | 父评论ID |
| replyToUserId | Long | 否 | 被回复用户ID |
| content | String | 是 | 回复内容 (1-500字符) |

**响应示例**

```json
{
    "code": 200,
    "msg": "回复成功",
    "data": {
        "commentId": 2004,
        "parentId": 2001,
        "content": "同意！",
        "createTime": "2025-11-29 12:05:00"
    }
}
```

---

### 3.4 删除评论

删除自己发表的评论。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/content/comment/{commentId}` |
| Method | DELETE |
| 认证 | 需要 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 评论ID |

**响应示例**

```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

## 四、互动接口

### 4.1 点赞/取消点赞

对动态进行点赞或取消点赞操作。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/interaction/like` |
| Method | POST |
| Content-Type | application/json |
| 认证 | 需要 |

**请求参数**

```json
{
    "targetId": 1001,
    "targetType": "feed",
    "action": "like"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | 是 | 目标ID (动态ID或评论ID) |
| targetType | String | 是 | 目标类型: feed/comment |
| action | String | 是 | 操作类型: like/unlike |

**响应示例**

```json
{
    "code": 200,
    "msg": "点赞成功",
    "data": {
        "isLiked": true,
        "likeCount": 129
    }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| isLiked | Boolean | 当前点赞状态 |
| likeCount | Integer | 最新点赞数 |

---

### 4.2 收藏/取消收藏

对动态进行收藏或取消收藏操作。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/interaction/collect` |
| Method | POST |
| Content-Type | application/json |
| 认证 | 需要 |

**请求参数**

```json
{
    "feedId": 1001,
    "action": "collect"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |
| action | String | 是 | 操作类型: collect/uncollect |

**响应示例**

```json
{
    "code": 200,
    "msg": "收藏成功",
    "data": {
        "isCollected": true,
        "collectCount": 9
    }
}
```

---

### 4.3 分享

记录动态分享行为。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-content/api/v1/interaction/share` |
| Method | POST |
| Content-Type | application/json |
| 认证 | 需要 |

**请求参数**

```json
{
    "feedId": 1001,
    "platform": "wechat"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | Long | 是 | 动态ID |
| platform | String | 否 | 分享平台: wechat/weibo/qq/link |

**响应示例**

```json
{
    "code": 200,
    "msg": "分享成功",
    "data": {
        "shareCount": 16,
        "shareUrl": "https://xypai.com/feed/1001"
    }
}
```

---

## 五、错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未授权 | 检查Token是否有效 |
| 403 | 禁止访问 | 无权限执行此操作 |
| 404 | 资源不存在 | 动态/评论可能已删除 |
| 500 | 服务器内部错误 | 联系技术支持 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 10001 | 动态不存在 |
| 10002 | 评论不存在 |
| 10003 | 无权删除他人内容 |
| 10004 | 评论内容不能为空 |
| 10005 | 评论内容超过长度限制 |
| 10006 | 已点赞，请勿重复操作 |
| 10007 | 已收藏，请勿重复操作 |

---

## 六、集成测试用例

### 测试文件
`Page03_FeedDetailTest.java`

### 测试覆盖场景

| 序号 | 测试方法 | 测试场景 | 预期结果 |
|------|----------|----------|----------|
| 1 | testGetFeedDetail_Success | 获取动态详情成功 | 返回完整动态信息 |
| 2 | testGetFeedDetail_NotFound | 查询不存在的动态 | 返回404错误码 |
| 3 | testLikeFeed_Success | 点赞动态 | 点赞成功，likeCount+1 |
| 4 | testUnlikeFeed_Success | 取消点赞 | 取消成功，likeCount-1 |
| 5 | testCollectFeed_Success | 收藏动态 | 收藏成功 |
| 6 | testUncollectFeed_Success | 取消收藏 | 取消成功 |
| 7 | testGetComments_Success | 获取评论列表 | 返回分页评论数据 |
| 8 | testPostComment_Success | 发表评论 | 评论成功，返回commentId |
| 9 | testReplyComment_Success | 回复评论 | 回复成功，关联parentId |
| 10 | testDeleteComment_Success | 删除自己的评论 | 删除成功 |
| 11 | testShareFeed_Success | 分享动态 | 分享成功，shareCount+1 |
| 12 | testPublishFeed_Success | 发布动态 | 发布成功，返回feedId |
| 13 | testDeleteFeed_Success | 删除自己的动态 | 删除成功 |
| 14 | testDeleteFeed_Forbidden | 删除他人动态 | 返回403错误码 |

### 测试执行

```bash
# 运行单个测试文件
mvn test -Dtest=Page03_FeedDetailTest -pl xypai-aggregation/xypai-app-bff

# 运行指定测试方法
mvn test -Dtest=Page03_FeedDetailTest#testGetFeedDetail_Success -pl xypai-aggregation/xypai-app-bff
```

### 测试依赖服务

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API网关 |
| xypai-auth | 9211 | 认证服务 |
| xypai-content | 9403 | 内容服务 |
| Nacos | 8848 | 服务注册中心 |
| Redis | 6379 | 缓存服务 |
| MySQL | 3306 | 数据库 |

---

## 七、前端对接指南

### 7.1 页面加载流程

```
1. 进入动态详情页面
   ↓
2. 调用 GET /content/detail/{feedId} 获取动态详情
   ↓
3. 渲染动态内容、作者信息、互动数据
   ↓
4. 调用 GET /content/comments/{feedId} 获取评论列表
   ↓
5. 渲染评论区域
```

### 7.2 互动操作流程

```
点赞操作:
1. 用户点击点赞按钮
2. 调用 POST /interaction/like (action=like/unlike)
3. 根据返回的 isLiked 更新UI状态
4. 更新点赞数显示

收藏操作:
1. 用户点击收藏按钮
2. 调用 POST /interaction/collect (action=collect/uncollect)
3. 根据返回的 isCollected 更新UI状态

评论操作:
1. 用户输入评论内容
2. 调用 POST /content/comment
3. 成功后将新评论插入列表顶部
4. 更新评论数统计
```

### 7.3 注意事项

1. **Token过期处理**: 接口返回401时，需跳转登录页面重新获取Token
2. **乐观更新**: 点赞/收藏等操作可先更新UI，失败时回滚
3. **评论分页**: 下拉加载更多评论时，注意pageNum递增
4. **图片加载**: 动态图片建议使用懒加载和缩略图
5. **视频播放**: 视频类型动态需处理自动播放策略

---

*文档版本: v1.0*
*最后更新: 2025-11-29*
