# 📖 用户模块API文档 v7.1

> **模块名称**: xypai-user  
> **端口**: 9401  
> **版本**: v7.1  
> **更新日期**: 2025-01-14

---

## 🎯 API概览

### 用户管理API
- `GET /api/v1/users/list` - 查询用户列表
- `GET /api/v1/users/{userId}` - 获取用户详情
- `POST /api/v1/users` - 新增用户
- `PUT /api/v1/users` - 修改用户
- `DELETE /api/v1/users/{userIds}` - 删除用户
- `GET /api/v1/users/profile` - 获取当前用户信息
- `PUT /api/v1/users/profile` - 更新当前用户信息

### 用户统计API ⭐ 新增
- `GET /api/v1/users/stats/{userId}` - 获取用户统计
- `GET /api/v1/users/stats/current` - 获取当前用户统计
- `POST /api/v1/users/stats/batch` - 批量查询统计
- `POST /api/v1/users/stats/init` - 初始化统计
- `POST /api/v1/users/stats/{userId}/refresh` - 刷新缓存
- `GET /api/v1/users/stats/popular` - 人气用户排行
- `GET /api/v1/users/stats/quality-organizers` - 优质组局者排行

### 职业标签API ⭐ 新增
- `GET /api/v1/occupation/list` - 查询所有职业
- `GET /api/v1/occupation/categories` - 查询职业分类
- `GET /api/v1/occupation/category/{category}` - 按分类查询
- `GET /api/v1/occupation/user/{userId}` - 查询用户职业
- `GET /api/v1/occupation/current` - 查询当前用户职业
- `PUT /api/v1/occupation/user/{userId}` - 更新用户职业
- `PUT /api/v1/occupation/current` - 更新当前用户职业
- `POST /api/v1/occupation/user/{userId}/add` - 添加职业
- `DELETE /api/v1/occupation/user/{userId}/remove` - 删除职业
- `DELETE /api/v1/occupation/user/{userId}/clear` - 清空职业

### 用户关系API
- `GET /api/v1/users/relations/list` - 查询关系列表
- `POST /api/v1/users/relations/follow` - 关注用户
- `DELETE /api/v1/users/relations/unfollow` - 取消关注

### 钱包管理API
- `GET /api/v1/users/wallet/{userId}` - 查询钱包
- `POST /api/v1/users/wallet/recharge` - 充值
- `POST /api/v1/users/wallet/transfer` - 转账

---

## 📝 API详细文档

### 1. 获取用户统计 ⭐

**接口**: `GET /api/v1/users/stats/{userId}`

**描述**: 查询用户的统计数据（优先从Redis读取，性能提升10倍）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "followerCount": 1520,
    "followingCount": 380,
    "contentCount": 45,
    "totalLikeCount": 8900,
    "totalCollectCount": 1200,
    "activityOrganizerCount": 12,
    "activityParticipantCount": 28,
    "activitySuccessCount": 10,
    "activityCancelCount": 2,
    "activityOrganizerScore": 4.65,
    "activitySuccessRate": 83.33,
    "isActive": true,
    "isPopular": true,
    "isQualityOrganizer": true,
    "followerFollowingRatio": 4.00,
    "lastSyncTime": "2025-01-14 10:30:00"
  }
}
```

**性能指标**:
- 首次查询（回源MySQL）: ~50ms
- 缓存命中（Redis）: ~5ms ✅
- 缓存时长: 1小时

---

### 2. 查询所有职业列表 ⭐

**接口**: `GET /api/v1/occupation/list`

**描述**: 获取所有启用的职业列表（按排序）

**无需参数**

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "code": "model",
      "name": "模特",
      "category": "艺术",
      "iconUrl": "https://cdn.example.com/icon/model.png",
      "sortOrder": 1,
      "status": 1,
      "statusDesc": "启用",
      "hasIcon": true
    },
    {
      "code": "student",
      "name": "学生",
      "category": "教育",
      "iconUrl": null,
      "sortOrder": 2,
      "status": 1,
      "statusDesc": "启用",
      "hasIcon": false
    }
    // ... 共20个职业
  ]
}
```

---

### 3. 更新用户职业标签 ⭐

**接口**: `PUT /api/v1/occupation/user/{userId}`

**描述**: 批量更新用户的职业标签（最多5个）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

**请求体**:
```json
{
  "occupationCodes": ["model", "student", "designer"],
  "keepSortOrder": false
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| occupationCodes | List<String> | 是 | 职业编码列表（最多5个） |
| keepSortOrder | Boolean | 否 | 是否保留原排序（默认false） |

**响应示例**:
```json
{
  "code": 200,
  "message": "更新职业标签成功"
}
```

**业务规则**:
- ✅ 最多选择5个职业
- ✅ 职业编码必须存在于职业字典
- ✅ 自动去重
- ✅ 先删除原有职业，再插入新职业（事务保证）

---

### 4. 查询用户职业标签 ⭐

**接口**: `GET /api/v1/occupation/user/{userId}`

**描述**: 获取指定用户的所有职业标签（按排序）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1001,
      "userId": 1,
      "occupationCode": "model",
      "occupationName": "模特",
      "category": "艺术",
      "iconUrl": "https://cdn.example.com/icon/model.png",
      "sortOrder": 0,
      "isPrimary": true,
      "createdAt": "2025-01-14 10:00:00"
    },
    {
      "id": 1002,
      "userId": 1,
      "occupationCode": "student",
      "occupationName": "学生",
      "category": "教育",
      "iconUrl": null,
      "sortOrder": 1,
      "isPrimary": false,
      "createdAt": "2025-01-14 10:00:00"
    }
  ]
}
```

---

### 5. 人气用户排行榜 ⭐

**接口**: `GET /api/v1/users/stats/popular`

**描述**: 查询粉丝数最多的用户（TOP 10）

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 10 | 数量限制（1-100） |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "userId": 123,
      "followerCount": 15200,
      "followingCount": 500,
      "isPopular": true,
      "followerFollowingRatio": 30.40
    },
    {
      "userId": 456,
      "followerCount": 12800,
      "followingCount": 300,
      "isPopular": true,
      "followerFollowingRatio": 42.67
    }
    // ... TOP 10
  ]
}
```

---

### 6. 优质组局者排行榜 ⭐

**接口**: `GET /api/v1/users/stats/quality-organizers`

**描述**: 查询评分和成功率最高的组局者（TOP 10）

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 10 | 数量限制（1-100） |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "userId": 789,
      "activityOrganizerCount": 25,
      "activitySuccessCount": 23,
      "activityOrganizerScore": 4.85,
      "activitySuccessRate": 92.00,
      "isQualityOrganizer": true
    }
    // ... TOP 10
  ]
}
```

**筛选条件**:
- 评分 > 4.5分
- 成功率 > 80%

---

## 🔐 权限要求

| API路径 | 权限标识 | 说明 |
|---------|----------|------|
| `/api/v1/users/**` | `user:user:query` | 用户查询权限 |
| `/api/v1/users/stats/**` | `user:stats:query` | 统计查询权限 |
| `/api/v1/occupation/**` | `user:occupation:query` | 职业查询权限 |
| PUT/POST/DELETE | 对应的 `:edit` 权限 | 修改权限 |

---

## 🚀 性能优化

### Redis缓存策略
```
Key格式: user:stats:{userId}
数据结构: Hash
过期时间: 1小时
更新策略: 双写（先Redis后MySQL）
```

### 索引优化
```sql
-- UserProfile查询优化
EXPLAIN SELECT * FROM user_profile 
WHERE city_id = 440300 AND is_vip = TRUE;
-- 使用索引: idx_city_online

-- UserStats排序优化
EXPLAIN SELECT * FROM user_stats 
ORDER BY follower_count DESC LIMIT 10;
-- 使用索引: idx_follower
```

---

## 🧪 测试用例

### cURL测试命令

#### 1. 查询用户统计
```bash
curl -X GET "http://localhost:9401/api/v1/users/stats/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 2. 初始化统计
```bash
curl -X POST "http://localhost:9401/api/v1/users/stats/init?userId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 3. 查询所有职业
```bash
curl -X GET "http://localhost:9401/api/v1/occupation/list"
```

#### 4. 更新用户职业
```bash
curl -X PUT "http://localhost:9401/api/v1/occupation/user/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "occupationCodes": ["model", "student", "designer"]
  }'
```

#### 5. 人气用户排行
```bash
curl -X GET "http://localhost:9401/api/v1/users/stats/popular?limit=10"
```

---

## 📊 数据模型

### UserStatsVO
```typescript
interface UserStatsVO {
  userId: number;
  followerCount: number;         // 粉丝数
  followingCount: number;        // 关注数
  contentCount: number;          // 内容数
  totalLikeCount: number;        // 获赞总数
  totalCollectCount: number;     // 被收藏总数
  activityOrganizerCount: number;    // 发起组局数
  activityParticipantCount: number;  // 参与组局数
  activitySuccessCount: number;      // 成功组局数
  activityCancelCount: number;       // 取消组局数
  activityOrganizerScore: number;    // 组局评分（5分制）
  activitySuccessRate: number;       // 组局成功率（%）
  isActive: boolean;             // 是否活跃（内容>10）
  isPopular: boolean;            // 是否人气（粉丝>1000）
  isQualityOrganizer: boolean;   // 优质组局者
  followerFollowingRatio: number;    // 粉丝关注比
  lastSyncTime: string;          // 最后同步时间
}
```

### OccupationDictVO
```typescript
interface OccupationDictVO {
  code: string;          // 职业编码
  name: string;          // 职业名称
  category: string;      // 职业分类
  iconUrl: string;       // 图标URL
  sortOrder: number;     // 排序
  status: number;        // 状态
  statusDesc: string;    // 状态描述
  hasIcon: boolean;      // 是否有图标
  createdAt: string;     // 创建时间
}
```

### UserOccupationVO
```typescript
interface UserOccupationVO {
  id: number;
  userId: number;
  occupationCode: string;
  occupationName: string;
  category: string;
  iconUrl: string;
  sortOrder: number;
  isPrimary: boolean;    // 是否主要职业
  createdAt: string;
}
```

---

## 🔧 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 用户不存在 |
| 500 | 服务器错误 |
| 10001 | 用户统计不存在 |
| 10002 | 职业编码不存在 |
| 10003 | 职业数量超过限制（最多5个） |
| 10004 | 职业已存在 |

---

## 🌐 Swagger文档

访问地址: http://localhost:9401/doc.html

**优势**:
- ✅ 可视化API文档
- ✅ 在线测试接口
- ✅ 自动生成请求示例
- ✅ 参数校验说明

---

## 📈 监控指标

### 性能指标（SLA）
```
P95响应时间:
- 用户统计查询: < 50ms（Redis缓存）
- 职业列表查询: < 100ms
- 批量统计查询: < 200ms

并发能力:
- 统计查询: 2000 QPS
- 职业查询: 3000 QPS
```

### Redis缓存命中率
```bash
# 查看缓存命中率
redis-cli INFO stats | grep keyspace

# 目标: > 90%
```

---

## 🔍 调试技巧

### 查看Redis缓存
```bash
# 查看用户统计缓存
redis-cli> HGETALL user:stats:1

# 查看所有统计缓存Key
redis-cli> KEYS user:stats:*

# 查看缓存过期时间
redis-cli> TTL user:stats:1
```

### 查看MySQL数据
```sql
-- 查看用户统计
SELECT * FROM user_stats WHERE user_id = 1;

-- 查看用户职业
SELECT uo.*, od.name 
FROM user_occupation uo
LEFT JOIN occupation_dict od ON uo.occupation_code = od.code
WHERE uo.user_id = 1
ORDER BY uo.sort_order;
```

### 日志查看
```bash
# 查看服务日志
tail -f logs/xypai-user.log | grep UserStats

# 查看SQL日志
tail -f logs/xypai-user.log | grep "==> Preparing"
```

---

## 📚 相关文档

- [快速开始指南](QUICK_START.md)
- [升级总结](USER_MODULE_UPGRADE_SUMMARY.md)
- [数据库设计](../../PL.md)

---

**API文档完整，接口规范统一！** 🎉

