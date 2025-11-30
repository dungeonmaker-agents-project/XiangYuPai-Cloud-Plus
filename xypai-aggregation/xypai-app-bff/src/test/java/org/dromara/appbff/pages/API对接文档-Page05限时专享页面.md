# Page05 限时专享页面 API 对接文档

> 对应前端页面: 05-限时专享页面
> 对应测试文件: Page05_LimitedTimeTest.java
> 最后更新: 2025-11-29

## 一、通用说明

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 服务名 | xypai-app-bff (BFF聚合层) |
| 网关地址 | http://localhost:8080 |
| 服务端口 | 9400 |
| 接口前缀 | /xypai-app-bff/api/home |
| 后端依赖 | xypai-user (通过Dubbo RPC) |

### 1.2 架构说明

```
前端 → Gateway(8080) → xypai-app-bff(9400) → [Dubbo RPC] → xypai-user(9401)
```

BFF层通过 `@DubboReference RemoteAppUserService` 调用用户服务获取限时专享数据。

### 1.3 认证方式

所有接口需要在请求头中携带认证Token：

```
Authorization: Bearer {token}
```

Token通过 `/xypai-auth/api/auth/login/sms` 接口获取。

### 1.4 通用响应格式

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

## 二、限时专享接口

### 2.1 获取限时专享列表

获取限时优惠的服务提供者列表，支持排序和筛选。

**请求信息**

| 项目 | 说明 |
|------|------|
| URL | `/xypai-app-bff/api/home/limited-time/list` |
| Method | GET |
| 认证 | 需要 |

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码，从1开始 |
| pageSize | Integer | 否 | 10 | 每页条数，最大50 |
| sortBy | String | 否 | smart_recommend | 排序方式 |
| gender | String | 否 | all | 性别筛选 |

**排序方式 (sortBy)**

| 值 | 说明 |
|----|------|
| smart_recommend | 智能推荐 (默认) |
| price_asc | 价格升序 |
| price_desc | 价格降序 |
| distance_asc | 距离升序 (由近到远) |

**性别筛选 (gender)**

| 值 | 说明 |
|----|------|
| all | 不限 (默认) |
| male | 仅男生 |
| female | 仅女生 |

**请求示例**

```
GET /xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10&sortBy=price_asc&gender=female
```

**响应示例**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "total": 156,
        "pageNum": 1,
        "pageSize": 10,
        "pages": 16,
        "hasMore": true,
        "endTime": "2025-11-29 23:59:59",
        "remainingSeconds": 43200,
        "list": [
            {
                "userId": 10001,
                "nickname": "甜美小姐姐",
                "avatar": "https://example.com/avatar1.jpg",
                "gender": "female",
                "age": 22,
                "isOnline": true,
                "isVerified": true,
                "verifyType": "real_person",
                "level": 5,
                "skillType": "王者荣耀",
                "skillLabel": "游戏陪玩",
                "description": "王者荣耀陪玩，5年经验，快速上分",
                "tags": [
                    {
                        "text": "声音甜美",
                        "type": "voice",
                        "color": "#FF6B6B"
                    },
                    {
                        "text": "技术过硬",
                        "type": "skill",
                        "color": "#4ECDC4"
                    }
                ],
                "originalPrice": {
                    "amount": 20,
                    "unit": "局",
                    "displayText": "20金币/局"
                },
                "discountPrice": {
                    "amount": 15,
                    "unit": "局",
                    "displayText": "15金币/局"
                },
                "discount": "7.5折",
                "discountRate": 0.75,
                "stats": {
                    "orders": 1280,
                    "rating": 4.9,
                    "reviewCount": 856
                },
                "distance": 1200,
                "distanceDisplay": "1.2km"
            },
            {
                "userId": 10002,
                "nickname": "游戏大神",
                "avatar": "https://example.com/avatar2.jpg",
                "gender": "male",
                "age": 25,
                "isOnline": true,
                "isVerified": true,
                "verifyType": "pro_gamer",
                "level": 8,
                "skillType": "英雄联盟",
                "skillLabel": "代练上分",
                "description": "钻石段位，带你飞",
                "tags": [
                    {
                        "text": "王者段位",
                        "type": "rank",
                        "color": "#FFD700"
                    }
                ],
                "originalPrice": {
                    "amount": 30,
                    "unit": "局",
                    "displayText": "30金币/局"
                },
                "discountPrice": {
                    "amount": 20,
                    "unit": "局",
                    "displayText": "20金币/局"
                },
                "discount": "6.7折",
                "discountRate": 0.67,
                "stats": {
                    "orders": 2150,
                    "rating": 4.8,
                    "reviewCount": 1234
                },
                "distance": 3500,
                "distanceDisplay": "3.5km"
            }
        ]
    }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| total | Long | 总记录数 |
| pageNum | Integer | 当前页码 |
| pageSize | Integer | 每页条数 |
| pages | Integer | 总页数 |
| hasMore | Boolean | 是否有更多数据 |
| endTime | String | 限时活动结束时间 |
| remainingSeconds | Integer | 剩余秒数 |
| list | List | 服务提供者列表 |

**列表项字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| nickname | String | 昵称 |
| avatar | String | 头像URL |
| gender | String | 性别: male/female |
| age | Integer | 年龄 |
| isOnline | Boolean | 是否在线 |
| isVerified | Boolean | 是否认证 |
| verifyType | String | 认证类型 |
| level | Integer | 等级 (1-10) |
| skillType | String | 技能类型 |
| skillLabel | String | 技能标签 |
| description | String | 个人简介 |
| tags | List | 标签列表 |
| originalPrice | Object | 原价信息 |
| discountPrice | Object | 折扣价信息 |
| discount | String | 折扣显示文本 |
| discountRate | Float | 折扣率 (0-1) |
| stats | Object | 统计数据 |
| distance | Integer | 距离 (米) |
| distanceDisplay | String | 距离显示文本 |

**价格对象 (originalPrice/discountPrice)**

| 字段 | 类型 | 说明 |
|------|------|------|
| amount | Integer | 金额 (金币) |
| unit | String | 单位: 局/小时/次 |
| displayText | String | 显示文本 |

**标签对象 (tags)**

| 字段 | 类型 | 说明 |
|------|------|------|
| text | String | 标签文本 |
| type | String | 标签类型: voice/skill/rank/service |
| color | String | 标签颜色 (十六进制) |

**统计对象 (stats)**

| 字段 | 类型 | 说明 |
|------|------|------|
| orders | Integer | 订单数 |
| rating | Float | 评分 (0-5) |
| reviewCount | Integer | 评价数 |

---

## 三、排序与筛选说明

### 3.1 智能推荐排序 (smart_recommend)

系统根据多维度因素进行综合排序：
- 用户偏好匹配度
- 服务提供者评分
- 在线状态
- 距离因素
- 活跃度

### 3.2 价格排序

- **price_asc**: 按折扣价从低到高排序
- **price_desc**: 按折扣价从高到低排序

### 3.3 距离排序

- **distance_asc**: 按距离从近到远排序
- 需要用户授权位置信息
- 未授权时返回默认排序

### 3.4 性别筛选

- 可与排序方式组合使用
- 筛选结果数量可能影响分页

---

## 四、错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查参数格式和取值范围 |
| 401 | 未授权 | Token无效或过期，重新登录 |
| 500 | 服务器内部错误 | 联系技术支持 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 20001 | 页码超出范围 |
| 20002 | 每页条数超出限制 |
| 20003 | 排序方式不支持 |
| 20004 | 性别筛选值无效 |
| 20005 | 限时活动已结束 |

---

## 五、集成测试用例

### 测试文件
`Page05_LimitedTimeTest.java`

### 测试覆盖场景

| 序号 | 测试方法 | 测试场景 | 预期结果 |
|------|----------|----------|----------|
| 1 | testGetLimitedTimeList_Default | 默认参数获取列表 | 返回智能推荐排序的数据 |
| 2 | testGetLimitedTimeList_PriceAsc | 按价格升序排序 | 列表按折扣价从低到高排列 |
| 3 | testGetLimitedTimeList_PriceDesc | 按价格降序排序 | 列表按折扣价从高到低排列 |
| 4 | testGetLimitedTimeList_DistanceAsc | 按距离排序 | 列表按距离从近到远排列 |
| 5 | testGetLimitedTimeList_FilterMale | 筛选男性服务者 | 仅返回gender=male的数据 |
| 6 | testGetLimitedTimeList_FilterFemale | 筛选女性服务者 | 仅返回gender=female的数据 |
| 7 | testGetLimitedTimeList_CombinedFilter | 组合筛选 | 同时应用排序和性别筛选 |
| 8 | testGetLimitedTimeList_Pagination | 分页测试 | 正确返回第2页数据 |

### 测试执行

```bash
# 运行单个测试文件
mvn test -Dtest=Page05_LimitedTimeTest -pl xypai-aggregation/xypai-app-bff

# 运行指定测试方法
mvn test -Dtest=Page05_LimitedTimeTest#testGetLimitedTimeList_Default -pl xypai-aggregation/xypai-app-bff
```

### 测试依赖服务

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API网关 |
| xypai-auth | 9211 | 认证服务 |
| xypai-app-bff | 9400 | BFF聚合服务 |
| xypai-user | 9401 | 用户服务 (RPC) |
| Nacos | 8848 | 服务注册中心 |
| Redis | 6379 | 缓存服务 |
| MySQL | 3306 | 数据库 |

---

## 六、前端对接指南

### 6.1 页面加载流程

```
1. 进入限时专享页面
   ↓
2. 调用 GET /api/home/limited-time/list 获取默认列表
   ↓
3. 解析 endTime 和 remainingSeconds，启动倒计时
   ↓
4. 渲染服务提供者卡片列表
   ↓
5. 用户下拉刷新或切换筛选条件时重新请求
```

### 6.2 倒计时处理

```javascript
// 前端倒计时逻辑示例
let remainingSeconds = response.data.remainingSeconds;

const timer = setInterval(() => {
    remainingSeconds--;
    if (remainingSeconds <= 0) {
        clearInterval(timer);
        // 活动结束处理
        showActivityEndedMessage();
    }
    updateCountdownDisplay(remainingSeconds);
}, 1000);
```

### 6.3 筛选与排序

```javascript
// 切换排序方式
function changeSort(sortBy) {
    fetchList({
        pageNum: 1,
        pageSize: 10,
        sortBy: sortBy,
        gender: currentGender
    });
}

// 切换性别筛选
function changeGender(gender) {
    fetchList({
        pageNum: 1,
        pageSize: 10,
        sortBy: currentSort,
        gender: gender
    });
}
```

### 6.4 无限滚动加载

```javascript
// 滚动加载更多
function loadMore() {
    if (!hasMore || isLoading) return;

    isLoading = true;
    fetchList({
        pageNum: currentPage + 1,
        pageSize: 10,
        sortBy: currentSort,
        gender: currentGender
    }).then(response => {
        list = [...list, ...response.data.list];
        currentPage++;
        hasMore = response.data.hasMore;
        isLoading = false;
    });
}
```

### 6.5 注意事项

1. **倒计时同步**: 服务端返回 `remainingSeconds`，前端本地倒计时，但需要定期与服务端同步
2. **缓存策略**: 列表数据可适当缓存，但倒计时信息需实时更新
3. **价格显示**: 同时显示原价(划线)和折扣价，突出优惠力度
4. **在线状态**: 优先展示在线的服务提供者
5. **距离权限**: 距离排序需要用户授权位置，未授权时降级为默认排序
6. **空状态处理**: 筛选结果为空时显示友好提示

### 6.6 卡片展示规范

```
┌─────────────────────────────────────┐
│  [头像]  昵称  ♀ 22岁  ●在线        │
│          ⭐ 4.9  接单1280           │
├─────────────────────────────────────┤
│  [声音甜美] [技术过硬]              │
│  王者荣耀陪玩，5年经验              │
├─────────────────────────────────────┤
│  ¥20/局  →  ¥15/局   7.5折         │
│                        距离 1.2km   │
└─────────────────────────────────────┘
```

---

## 七、后端实现说明

### 7.1 BFF层实现

```java
@RestController
@RequestMapping("/api/home/limited-time")
public class LimitedTimeController {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @GetMapping("/list")
    public R<LimitedTimeListVO> getLimitedTimeList(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(defaultValue = "smart_recommend") String sortBy,
        @RequestParam(defaultValue = "all") String gender
    ) {
        // 调用RPC获取数据
        // 转换VO返回给前端
    }
}
```

### 7.2 RPC接口

```java
public interface RemoteAppUserService {
    /**
     * 获取限时专享列表
     */
    LimitedTimePageResult getLimitedTimeList(
        Integer pageNum,
        Integer pageSize,
        String sortBy,
        String gender,
        Long currentUserId
    );
}
```

---

*文档版本: v1.0*
*最后更新: 2025-11-29*
