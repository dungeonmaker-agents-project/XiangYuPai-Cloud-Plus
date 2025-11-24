# Page 05 限时专享 - RPC集成完成说明

## 一、实现概述

已完成将 Page 05 (限时专享) 从 Mock 数据切换到真实数据库查询的 RPC 集成。

### 架构变更
- **之前**: BFF 层使用 Mock 数据生成用户列表
- **现在**: BFF 通过 Dubbo RPC 调用 xypai-user 服务，从数据库查询真实用户+技能+统计数据

---

## 二、修改的文件清单

### 1. RPC 接口定义 (ruoyi-api/xypai-api-appuser)

#### 新增文件:
- `LimitedTimeUserVo.java` - RPC 传输对象 (包含用户+技能+统计信息)
- `LimitedTimePageResult.java` - 分页结果包装类

#### 修改文件:
- `RemoteAppUserService.java` - 添加新方法:
  ```java
  LimitedTimePageResult queryLimitedTimeUsers(
      String gender, String cityCode, String districtCode,
      Double latitude, Double longitude,
      Integer pageNum, Integer pageSize
  );
  ```

### 2. RPC Provider (xypai-modules/xypai-user)

#### 修改文件:
- `RemoteAppUserServiceImpl.java` - 实现新 RPC 方法
- `UserMapper.java` - 添加两个新查询方法:
  - `queryLimitedTimeUsers()` - 复杂 JOIN 查询 (users + skills + user_stats)
  - `countLimitedTimeUsers()` - 统计总数

#### SQL 查询特点:
- INNER JOIN `skills` 表 (只返回有上架技能的用户)
- LEFT JOIN `user_stats` 表 (统计信息)
- 使用 MySQL 空间函数 `ST_Distance_Sphere` 计算距离
- 支持动态筛选: 性别、城市、区县
- 按在线状态和用户ID排序

### 3. BFF 聚合层 (xypai-aggregation/xypai-app-bff)

#### 修改文件:
- `pom.xml` - 添加依赖:
  ```xml
  <dependency>
      <groupId>org.dromara</groupId>
      <artifactId>xypai-api-appuser</artifactId>
  </dependency>
  ```

- `HomeLimitedTimeServiceImpl.java` - **完全重写**:
  - 删除所有 Mock 用户生成代码 (200+ 行)
  - 添加 `@DubboReference` 注入远程服务
  - 实现 RPC 调用 → VO 转换 → 排序 → 返回
  - 保留临时 Mock: 促销标签、促销价格计算 (等促销服务就绪后替换)

---

## 三、数据流程

```
1. APP 请求 → BFF Controller (HomeController)
                ↓
2. BFF Service (HomeLimitedTimeServiceImpl)
   - 调用 RPC: remoteAppUserService.queryLimitedTimeUsers()
                ↓
3. Dubbo RPC 跨服务调用
                ↓
4. User Service Provider (RemoteAppUserServiceImpl)
   - 调用 UserMapper.queryLimitedTimeUsers()
   - 查询数据库 (users + skills + user_stats)
                ↓
5. 返回 RPC VO (LimitedTimePageResult)
                ↓
6. BFF 接收数据
   - 转换 RPC VO → BFF VO
   - 应用排序 (智能推荐/价格/距离)
   - 添加临时促销信息
                ↓
7. 返回给 APP 客户端
```

---

## 四、测试步骤

### 前置准备

1. **执行 SQL 脚本 (插入测试数据)**:
   ```bash
   mysql -u root -p xypai_user < E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\sql\xypai_user_test_data.sql
   ```

   脚本会插入:
   - 25 个用户 (user_id: 1001-1025)
   - 男性 13 人, 女性 12 人
   - 覆盖北京、上海、广州、成都、杭州等城市
   - 每个用户都有上架技能
   - 约 18 人在线 (is_online = 1)

2. **启动 xypai-user 服务**:
   ```bash
   # 确保数据库连接正常
   # 确保 Dubbo 配置正确
   # 启动服务
   ```

3. **启动 xypai-app-bff 服务**:
   ```bash
   # 确保 Dubbo 能连接到 xypai-user
   # 启动服务
   ```

### 运行测试

1. **执行集成测试**:
   ```bash
   cd E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\xypai-aggregation\xypai-app-bff
   mvn test -Dtest=Page05_LimitedTimeTest
   ```

2. **预期结果**:
   - `testGetLimitedTimeList_Default()` - 返回所有在线技能用户
   - `testGetLimitedTimeList_GenderFilter()` - 筛选男性/女性用户
   - `testGetLimitedTimeList_LocationFilter()` - 筛选特定城市用户
   - `testGetLimitedTimeList_Sorting()` - 验证各种排序
   - `testGetLimitedTimeList_Pagination()` - 验证分页

---

## 五、验证要点

### 数据库验证
```sql
-- 1. 查看有上架技能的用户总数 (应该是 25)
SELECT COUNT(DISTINCT u.user_id)
FROM users u
INNER JOIN skills s ON u.user_id = s.user_id AND s.is_online = 1
WHERE u.deleted = 0;

-- 2. 查看在线用户数量 (约 18 人)
SELECT COUNT(*) FROM users WHERE is_online = 1 AND deleted = 0;

-- 3. 查看男性用户数量 (13 人)
SELECT COUNT(*) FROM users WHERE gender = 'male' AND deleted = 0;

-- 4. 查看女性用户数量 (12 人)
SELECT COUNT(*) FROM users WHERE gender = 'female' AND deleted = 0;
```

### API 响应验证
```json
// GET /api/v1/app/home/limited-time?pageNum=1&pageSize=10

// 期望响应:
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 25,          // 总用户数
    "hasMore": true,      // 是否有更多数据
    "list": [             // 用户列表 (每人包含技能和价格信息)
      {
        "userId": 1012,
        "nickname": "御姐射手小美",
        "avatar": "https://avatar.example.com/female2.jpg",
        "gender": "female",
        "age": 29,
        "distance": 1234,
        "distanceText": "1.2km",
        "tags": [
          { "text": "可线上", "type": "feature", "color": "#4CAF50" },
          { "text": "限时7折", "type": "price", "color": "#FF5722" },
          { "text": "王者荣耀 - 御姐射手", "type": "skill", "color": "#2196F3" }
        ],
        "description": "御姐音,稳定输出",
        "price": {
          "amount": 12,              // 促销价 (70% * 18 = 12.6 → 12)
          "unit": "per_order",
          "displayText": "12 金币/单",
          "originalPrice": 18        // 原价
        },
        "promotionTag": "今日优惠",  // 临时 Mock
        "isOnline": true,
        "skillLevel": "王者"
      }
      // ... 更多用户
    ],
    "filters": {
      "sortOptions": [...],
      "genderOptions": [...],
      "languageOptions": [...]
    }
  }
}
```

---

## 六、临时 Mock 说明

以下数据仍为临时 Mock (等促销服务开发后替换):

1. **促销标签**: `"限时特价"`, `"今日优惠"`, `"新人专享"`, `"热门推荐"`, `"超值特惠"`
   - 位置: `HomeLimitedTimeServiceImpl.java:33-35`

2. **促销价格计算**: 统一 7 折优惠
   - 位置: `HomeLimitedTimeServiceImpl.java:78-80`
   - 公式: `promotionPrice = (int) (originalPrice * 0.7)`

### 替换计划
当促销服务开发完成后:
1. 删除 `PROMOTION_TAGS` 常量
2. 通过 RPC 调用促销服务获取真实促销信息
3. 替换价格计算逻辑为促销服务返回的价格

---

## 七、编译状态

✅ **xypai-api-appuser**: 编译成功 + 安装到本地 Maven 仓库
✅ **xypai-user**: 编译成功
✅ **xypai-app-bff**: 编译成功

---

## 八、后续任务

1. ✅ RPC 接口定义
2. ✅ RPC Provider 实现
3. ✅ BFF 服务改造
4. ✅ SQL 测试数据脚本
5. ⏳ **执行 SQL 脚本 (您需要执行)**
6. ⏳ **启动服务并运行测试 (您需要执行)**
7. ⏳ 开发促销服务 (未来)
8. ⏳ 替换临时 Mock 数据 (未来)

---

## 九、注意事项

1. **数据库表结构**: 确保 `users`, `skills`, `user_stats` 三张表已创建
2. **空间索引**: `users.location` 字段需要空间索引才能正常查询距离
3. **Dubbo 配置**: 确保两个服务的 Dubbo 配置正确 (注册中心地址等)
4. **端口占用**: 确保服务端口未被占用

---

**完成时间**: 2025-11-24
**实现人员**: Claude Code
**状态**: ✅ RPC 集成完成, 等待测试验证
