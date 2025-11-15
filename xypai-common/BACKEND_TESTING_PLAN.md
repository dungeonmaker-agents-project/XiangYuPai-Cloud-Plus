# Backend Testing Plan - XiangYuPai Common Module

## 后端测试计划 - 享遇派通用模块

**文档类型**: 测试计划 (Test Plan Document)
**创建日期**: 2025-11-14
**版本**: v1.0
**状态**: 待执行

---

## 📋 目录

1. [测试概述](#测试概述)
2. [测试策略](#测试策略)
3. [测试文件结构](#测试文件结构)
4. [前端页面流程测试](#前端页面流程测试)
5. [API集成测试](#api集成测试)
6. [数据准备](#数据准备)
7. [测试用例设计](#测试用例设计)
8. [执行计划](#执行计划)

---

## 测试概述

### 测试目标

基于前端页面流程和实际业务场景，对xypai-common模块的后端API进行全面的集成测试，确保:

1. ✅ **功能完整性**: 所有API按照前端需求正确实现
2. ✅ **数据准确性**: 返回数据格式和内容符合前端期望
3. ✅ **业务流程**: 完整的页面流程能够顺利执行
4. ✅ **边界处理**: 异常情况和边界条件正确处理
5. ✅ **性能要求**: 响应时间满足性能标准

### 测试范围

**包含的服务**:
- ✅ LocationService (位置服务)
- ✅ MediaService (媒体服务)
- ✅ NotificationService (通知服务)
- ✅ ReportService (举报服务)

**包含的前端页面流程**:
- ✅ 03-区域选择页面流程
- ✅ 04-城市定位页面流程
- ✅ 媒体上传流程
- ✅ 通知查看流程

### 测试类型

1. **API集成测试** (Integration Tests)
   - 测试完整的HTTP请求-响应流程
   - 验证数据库交互
   - 验证Redis缓存
   - 验证OSS上传

2. **业务流程测试** (Business Flow Tests)
   - 模拟前端完整操作流程
   - 多个API的组合调用
   - 状态变化验证

3. **数据验证测试** (Data Validation Tests)
   - 请求参数验证
   - 响应数据格式验证
   - 业务规则验证

---

## 测试策略

### 测试环境

```yaml
测试环境配置:
  数据库: MySQL 8.0 (独立测试库)
  缓存: Redis 7.0 (测试实例)
  对象存储: OSS (测试bucket)
  应用端口: 9407
  测试框架: Spring Boot Test + RestAssured
  数据库初始化: 每次测试前清空并重新初始化
```

### 测试数据管理

**测试数据策略**:
1. **固定测试数据**: 城市、区域等基础数据
2. **动态生成数据**: 用户上传的图片、通知等
3. **清理策略**: 每个测试类执行后清理数据

### 测试执行顺序

```
Phase 1: 基础功能测试 (单个API)
  └─ 验证每个API基本功能

Phase 2: 页面流程测试 (API组合)
  └─ 验证前端页面完整流程

Phase 3: 边界和异常测试
  └─ 验证异常处理和边界条件

Phase 4: 性能测试
  └─ 验证响应时间和并发能力
```

---

## 测试文件结构

### 建议的测试文件组织

```
xypai-common/
└── src/test/java/org/dromara/common/
    ├── integration/                          # 集成测试
    │   ├── flow/                            # 页面流程测试
    │   │   ├── DistrictSelectionFlowTest.java      # 区域选择流程
    │   │   ├── CityLocationFlowTest.java           # 城市定位流程
    │   │   ├── MediaUploadFlowTest.java            # 媒体上传流程
    │   │   └── NotificationFlowTest.java           # 通知查看流程
    │   │
    │   ├── api/                             # API集成测试
    │   │   ├── location/
    │   │   │   ├── CityApiTest.java               # 城市API测试
    │   │   │   ├── DistrictApiTest.java           # 区域API测试
    │   │   │   ├── LocationApiTest.java           # 位置API测试
    │   │   │   └── GeocodeApiTest.java            # 地理编码API测试
    │   │   │
    │   │   ├── media/
    │   │   │   ├── ImageUploadApiTest.java        # 图片上传API测试
    │   │   │   ├── VideoUploadApiTest.java        # 视频上传API测试
    │   │   │   └── MediaQueryApiTest.java         # 媒体查询API测试
    │   │   │
    │   │   ├── notification/
    │   │   │   ├── NotificationListApiTest.java   # 通知列表API测试
    │   │   │   ├── UnreadCountApiTest.java        # 未读数API测试
    │   │   │   └── NotificationActionApiTest.java # 通知操作API测试
    │   │   │
    │   │   └── report/
    │   │       ├── ReportSubmitApiTest.java       # 举报提交API测试
    │   │       ├── ReportReviewApiTest.java       # 举报审核API测试
    │   │       └── PunishmentApiTest.java         # 处罚管理API测试
    │   │
    │   └── rpc/                             # RPC接口测试
    │       ├── RemoteLocationServiceTest.java
    │       ├── RemoteMediaServiceTest.java
    │       ├── RemoteNotificationServiceTest.java
    │       └── RemoteReportServiceTest.java
    │
    └── support/                             # 测试支持类
        ├── TestDataBuilder.java             # 测试数据构建器
        ├── ApiTestBase.java                 # API测试基类
        ├── FlowTestBase.java                # 流程测试基类
        └── assertions/                      # 自定义断言
            ├── LocationAssertions.java
            ├── MediaAssertions.java
            └── NotificationAssertions.java
```

### 测试文件数量统计

| 分类 | 文件数 | 说明 |
|------|--------|------|
| **页面流程测试** | 4个 | 对应4个前端页面流程 |
| **API集成测试** | 12个 | 覆盖所有HTTP API |
| **RPC接口测试** | 4个 | 覆盖所有Dubbo服务 |
| **测试支持类** | 6个 | 基类和工具类 |
| **总计** | 26个文件 | 完整测试覆盖 |

---

## 前端页面流程测试

### 流程测试 1: 区域选择页面流程

**测试文件**: `DistrictSelectionFlowTest.java`

**前端页面**: 03-区域选择页面.md

**业务流程**:
```
用户进入页面
  ↓
获取区域列表 (GET /api/location/districts?cityCode=xxx)
  ↓
显示区域选项 (全深圳、南山区、福田区等)
  ↓
用户点击选择区域
  ↓
调用选择接口 (POST /api/location/district/select)
  ↓
返回首页，刷新Feed流
```

**需要测试的场景**:

```java
@TestMethodOrder(OrderAnnotation.class)
class DistrictSelectionFlowTest {

    // 场景1: 完整的区域选择流程
    @Test
    @Order(1)
    void testCompleteDistrictSelectionFlow() {
        // 1. 准备: 用户登录北京
        String cityCode = "110100";

        // 2. 步骤1: 获取区域列表
        // GET /api/location/districts?cityCode=110100
        // 验证: 返回区域列表，包含"全北京"和各区

        // 3. 步骤2: 用户选择"朝阳区"
        // POST /api/location/district/select
        // Body: { cityCode: "110100", districtCode: "110105" }
        // 验证: 选择成功，hasDistricts=false，返回成功标志

        // 4. 验证: 用户位置信息已更新
        // 检查数据库或缓存中的用户位置
    }

    // 场景2: 选择"全城"
    @Test
    @Order(2)
    void testSelectAllDistrictFlow() {
        // 用户选择"全深圳"
        // districtCode = "all"
    }

    // 场景3: 城市无区域的情况
    @Test
    @Order(3)
    void testCityWithoutDistricts() {
        // 测试小城市，没有区域划分
        // 应该自动选择全城
    }

    // 场景4: 网络异常处理
    @Test
    @Order(4)
    void testNetworkErrorHandling() {
        // 模拟网络错误
        // 验证错误提示
    }

    // 场景5: 无效城市代码
    @Test
    @Order(5)
    void testInvalidCityCode() {
        // cityCode = "999999"
        // 验证: 返回错误信息"城市信息无效"
    }
}
```

**测试数据示例**:

```java
// 前端发送的数据
{
  "cityCode": "110100",
  "districtCode": "110105"
}

// 期望的响应数据
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "selectedDistrict": {
      "code": "110105",
      "name": "朝阳区"
    }
  }
}
```

---

### 流程测试 2: 城市定位页面流程

**测试文件**: `CityLocationFlowTest.java`

**前端页面**: 04-城市定位页面.md

**业务流程**:
```
用户打开城市选择页
  ↓
获取城市列表 (GET /api/location/cities)
  ↓
显示: 当前定位、最近访问、热门城市、全部城市(A-Z)
  ↓
用户选择方式:
  ├─ GPS定位 (POST /api/location/detect)
  ├─ 点击最近访问城市
  ├─ 点击热门城市
  └─ 浏览全部城市选择
  ↓
调用选择接口 (POST /api/location/city/select)
  ↓
判断: 是否有区域?
  ├─ 是 → 跳转区域选择页
  └─ 否 → 直接返回首页
```

**需要测试的场景**:

```java
@TestMethodOrder(OrderAnnotation.class)
class CityLocationFlowTest {

    // 场景1: 完整的城市选择流程
    @Test
    @Order(1)
    void testCompleteCitySelectionFlow() {
        // 1. 步骤1: 获取城市列表
        // GET /api/location/cities
        // 验证: 返回热门城市、全部城市(按字母分组)

        // 2. 步骤2: 用户选择"北京"
        // POST /api/location/city/select
        // Body: { cityCode: "110100", cityName: "北京", source: "hot" }
        // 验证: hasDistricts=true (北京有区域)

        // 3. 验证: 应该跳转到区域选择页面
    }

    // 场景2: GPS定位流程
    @Test
    @Order(2)
    void testGPSLocationFlow() {
        // 1. 步骤1: 用户触发GPS定位
        // 前端获取GPS坐标: lat=39.9, lng=116.4

        // 2. 步骤2: 调用定位解析
        // POST /api/location/detect
        // Body: { latitude: 39.9, longitude: 116.4 }
        // 验证: 返回城市信息 (北京)

        // 3. 步骤3: 显示定位结果给用户确认
    }

    // 场景3: 选择无区域的城市
    @Test
    @Order(3)
    void testSelectCityWithoutDistricts() {
        // 选择小城市，没有区域划分
        // 验证: hasDistricts=false
        // 应该直接返回首页
    }

    // 场景4: 城市列表数据验证
    @Test
    @Order(4)
    void testCityListDataStructure() {
        // 验证城市列表数据结构:
        // - hotCities: 热门城市列表
        // - allCities: 按字母分组
        // - 每个城市包含: cityCode, cityName, province
    }

    // 场景5: 字母分组正确性
    @Test
    @Order(5)
    void testCityAlphabetGrouping() {
        // 验证城市按首字母正确分组
        // A: 安庆
        // B: 北京、保定
        // ...
    }

    // 场景6: 热门城市排序
    @Test
    @Order(6)
    void testHotCitiesOrder() {
        // 验证热门城市的排序
        // 应该是: 北京、上海、深圳、广州...
    }
}
```

**测试数据示例**:

```java
// 前端GPS定位发送的数据
{
  "latitude": 39.904989,
  "longitude": 116.405285
}

// 期望的定位响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "cityCode": "110100",
    "cityName": "北京",
    "district": "东城区",
    "province": "北京市",
    "formattedAddress": "北京市东城区天安门广场"
  }
}

// 城市选择发送的数据
{
  "cityCode": "110100",
  "cityName": "北京",
  "source": "hot"  // manual/gps/recent/hot
}

// 期望的选择响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "selectedCity": {
      "cityCode": "110100",
      "cityName": "北京"
    },
    "hasDistricts": true  // 北京有区域，需要跳转区域选择页
  }
}
```

---

### 流程测试 3: 媒体上传流程

**测试文件**: `MediaUploadFlowTest.java`

**业务流程**:
```
用户选择文件
  ↓
前端验证文件类型和大小
  ↓
上传文件 (POST /api/media/upload)
  ↓
后端处理:
  ├─ 验证文件
  ├─ 计算MD5 (检查秒传)
  ├─ 压缩/处理
  └─ 上传OSS
  ↓
返回文件URL
  ↓
前端显示上传成功
```

**需要测试的场景**:

```java
@TestMethodOrder(OrderAnnotation.class)
class MediaUploadFlowTest {

    // 场景1: 图片上传完整流程
    @Test
    @Order(1)
    void testImageUploadFlow() {
        // 1. 准备: 创建测试图片文件
        byte[] imageData = createTestImage(1024 * 1024);  // 1MB

        // 2. 步骤1: 上传图片
        // POST /api/media/upload
        // Content-Type: multipart/form-data
        // file: (binary)
        // bizType: "post"

        // 3. 验证响应:
        // - fileId: 生成的文件ID
        // - fileUrl: OSS访问URL
        // - fileName: 文件名
        // - fileSize: 文件大小
        // - thumbnail: 缩略图URL

        // 4. 验证文件:
        // - OSS中文件存在
        // - 数据库记录存在
        // - 缩略图生成成功
    }

    // 场景2: 视频上传流程
    @Test
    @Order(2)
    void testVideoUploadFlow() {
        // 上传视频文件
        // 验证封面图生成
    }

    // 场景3: MD5秒传
    @Test
    @Order(3)
    void testInstantUploadWithMD5() {
        // 1. 上传文件A
        String fileUrl1 = uploadFile(fileA);

        // 2. 再次上传相同文件
        String fileUrl2 = uploadFile(fileA);

        // 3. 验证: 两次返回相同URL (秒传)
        assertEquals(fileUrl1, fileUrl2);
    }

    // 场景4: 文件大小限制
    @Test
    @Order(4)
    void testFileSizeLimit() {
        // 图片: 测试11MB (超过10MB限制)
        // 验证: 返回错误 "文件大小超过限制"

        // 视频: 测试101MB (超过100MB限制)
        // 验证: 返回错误
    }

    // 场景5: 文件类型验证
    @Test
    @Order(5)
    void testFileTypeValidation() {
        // 上传.exe文件
        // 验证: 返回错误 "不支持的文件类型"
    }

    // 场景6: 图片压缩
    @Test
    @Order(6)
    void testImageCompression() {
        // 上传5MB图片
        // 验证: 压缩后小于5MB
        // 验证: 图片质量可接受
    }

    // 场景7: 业务关联
    @Test
    @Order(7)
    void testFileBusinessBinding() {
        // 1. 上传图片
        String fileUrl = uploadImage();
        Long fileId = extractFileId(fileUrl);

        // 2. 关联到帖子
        // POST /api/media/bind
        // Body: { fileId, bizType: "post", bizId: 1001 }

        // 3. 验证: 关联成功
    }
}
```

**测试数据示例**:

```java
// 图片上传请求 (multipart/form-data)
POST /api/media/upload
Content-Type: multipart/form-data

file: (binary data)
bizType: "post"

// 期望响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "fileId": 1001,
    "fileUrl": "https://oss.example.com/uploads/2025/11/14/abc123.jpg",
    "fileName": "image.jpg",
    "fileSize": 1024000,
    "fileType": "image/jpeg",
    "md5": "abc123def456",
    "thumbnail": "https://oss.example.com/uploads/2025/11/14/abc123_thumb.jpg",
    "width": 1920,
    "height": 1080
  }
}
```

---

### 流程测试 4: 通知查看流程

**测试文件**: `NotificationFlowTest.java`

**业务流程**:
```
用户打开通知页
  ↓
获取未读数 (GET /api/notification/unread-count)
  ↓
显示红点和数字
  ↓
用户点击某个通知分类
  ↓
获取通知列表 (GET /api/notification/list?type=xxx)
  ↓
显示通知列表
  ↓
用户点击查看通知
  ↓
标记已读 (PUT /api/notification/read/{id})
  ↓
跳转到目标内容
```

**需要测试的场景**:

```java
@TestMethodOrder(OrderAnnotation.class)
class NotificationFlowTest {

    // 场景1: 完整的通知查看流程
    @Test
    @Order(1)
    void testCompleteNotificationFlow() {
        // 1. 准备: 创建测试通知
        createTestNotifications(userId, 5, "like");
        createTestNotifications(userId, 3, "comment");

        // 2. 步骤1: 获取未读数
        // GET /api/notification/unread-count
        // 验证: likeCount=5, commentCount=3, totalCount=8

        // 3. 步骤2: 获取点赞通知列表
        // GET /api/notification/list?type=like&pageNum=1
        // 验证: 返回5条点赞通知

        // 4. 步骤3: 标记第一条已读
        // PUT /api/notification/read/{notificationId}
        // 验证: 标记成功

        // 5. 步骤4: 再次获取未读数
        // 验证: likeCount=4, totalCount=7
    }

    // 场景2: 分类通知列表
    @Test
    @Order(2)
    void testNotificationListByType() {
        // 测试各类型通知列表:
        // - likes: 点赞通知
        // - comments: 评论通知
        // - followers: 粉丝通知
        // - system: 系统通知
    }

    // 场景3: 批量标记已读
    @Test
    @Order(3)
    void testBatchMarkAsRead() {
        // 1. 创建10条通知
        // 2. 批量标记前5条已读
        // PUT /api/notification/batch-read
        // Body: { ids: [1,2,3,4,5] }
        // 3. 验证: 未读数减少5
    }

    // 场景4: 全部标记已读
    @Test
    @Order(4)
    void testMarkAllAsRead() {
        // 1. 有10条未读通知
        // 2. 全部标记已读
        // PUT /api/notification/read-all?type=like
        // 3. 验证: likeCount=0
    }

    // 场景5: 清除已读通知
    @Test
    @Order(5)
    void testClearReadNotifications() {
        // 1. 有5条已读、5条未读
        // 2. 清除已读
        // DELETE /api/notification/clear?type=like
        // 3. 验证: 只剩5条未读
    }

    // 场景6: 通知详情验证
    @Test
    @Order(6)
    void testNotificationDetailStructure() {
        // 验证通知数据结构:
        // - 点赞通知: 包含点赞者信息、被点赞内容
        // - 评论通知: 包含评论者信息、评论内容摘要
        // - 粉丝通知: 包含关注者信息、是否已关注
        // - 系统通知: 包含标题、内容
    }

    // 场景7: 分页测试
    @Test
    @Order(7)
    void testNotificationPagination() {
        // 1. 创建50条通知
        // 2. 分页获取: pageSize=20
        // 3. 验证: 第1页20条，第2页20条，第3页10条
        // 4. 验证: hasMore标志正确
    }
}
```

**测试数据示例**:

```java
// 获取未读数响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "likeCount": 5,
    "commentCount": 3,
    "followCount": 2,
    "systemCount": 1,
    "activityCount": 0,
    "totalCount": 11
  }
}

// 获取通知列表响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "type": "like",
    "total": 5,
    "unreadCount": 5,
    "hasMore": false,
    "list": [
      {
        "notificationId": 1001,
        "type": "like",
        "senderId": 2001,
        "senderInfo": {
          "userId": 2001,
          "nickname": "张三",
          "avatar": "https://..."
        },
        "actionType": "like",
        "targetType": "post",
        "targetId": 5001,
        "targetContent": "这是一条动态内容...",
        "targetThumbnail": "https://...",
        "isRead": false,
        "createdAt": "2025-11-14T10:00:00Z"
      }
    ]
  }
}
```

---

## API集成测试

### API测试 1: 城市API测试

**测试文件**: `CityApiTest.java`

**测试的API**:
- `GET /api/city/list` - 获取城市列表

**测试用例设计**:

```java
class CityApiTest extends ApiTestBase {

    // 用例1: 成功获取城市列表
    @Test
    void testGetCityList_Success() {
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/city/list")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.hotCities", notNullValue())
            .body("data.hotCities.size()", greaterThan(0))
            .body("data.allCities", notNullValue())
            // 验证热门城市包含北京、上海等
            .body("data.hotCities.cityName", hasItems("北京", "上海", "深圳"));
    }

    // 用例2: 未登录访问 (如果需要登录)
    @Test
    void testGetCityList_Unauthorized() {
        given()
            // 不传Authorization header
        .when()
            .get("/api/city/list")
        .then()
            .statusCode(401);
    }

    // 用例3: 验证数据结构完整性
    @Test
    void testGetCityList_DataStructure() {
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/city/list")
        .then()
            .statusCode(200)
            .extract().response();

        // 验证每个城市包含必要字段
        List<Map> hotCities = response.jsonPath().getList("data.hotCities");
        for (Map city : hotCities) {
            assertNotNull(city.get("cityCode"));
            assertNotNull(city.get("cityName"));
            assertTrue(city.containsKey("isHot"));
        }
    }

    // 用例4: 验证字母分组
    @Test
    void testGetCityList_AlphabetGrouping() {
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/city/list")
        .then()
            .statusCode(200)
            .extract().response();

        Map<String, List> allCities = response.jsonPath().getMap("data.allCities");

        // 验证包含A-Z分组
        assertTrue(allCities.containsKey("A"));
        assertTrue(allCities.containsKey("B"));

        // 验证北京在B分组
        List bCities = allCities.get("B");
        assertTrue(bCities.stream()
            .anyMatch(city -> ((Map)city).get("cityName").equals("北京")));
    }

    // 用例5: 缓存测试
    @Test
    void testGetCityList_Cache() {
        // 第一次请求
        long start1 = System.currentTimeMillis();
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/city/list")
        .then()
            .statusCode(200);
        long time1 = System.currentTimeMillis() - start1;

        // 第二次请求 (应该走缓存)
        long start2 = System.currentTimeMillis();
        given()
            .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/api/city/list")
        .then()
            .statusCode(200);
        long time2 = System.currentTimeMillis() - start2;

        // 验证: 第二次请求更快 (走缓存)
        assertTrue(time2 < time1, "缓存应该更快");
    }
}
```

---

### API测试 2: 媒体上传API测试

**测试文件**: `ImageUploadApiTest.java`

**测试的API**:
- `POST /api/media/upload` - 上传媒体文件

**测试用例设计**:

```java
class ImageUploadApiTest extends ApiTestBase {

    // 用例1: 成功上传图片
    @Test
    void testUploadImage_Success() {
        File testImage = createTestImageFile("test.jpg", 1024 * 1024);  // 1MB

        given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.fileId", notNullValue())
            .body("data.fileUrl", startsWith("https://"))
            .body("data.thumbnail", notNullValue())
            .body("data.md5", notNullValue());
    }

    // 用例2: 文件大小超限
    @Test
    void testUploadImage_FileTooLarge() {
        File largeImage = createTestImageFile("large.jpg", 11 * 1024 * 1024);  // 11MB

        given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", largeImage, "image/jpeg")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(400)
            .body("msg", containsString("文件大小超过限制"));
    }

    // 用例3: 不支持的文件类型
    @Test
    void testUploadImage_UnsupportedType() {
        File exeFile = createTestFile("test.exe", 1024);

        given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", exeFile, "application/exe")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(400)
            .body("msg", containsString("不支持的文件类型"));
    }

    // 用例4: MD5秒传
    @Test
    void testUploadImage_InstantUpload() {
        File image = createTestImageFile("test.jpg", 1024 * 1024);

        // 第一次上传
        String fileUrl1 = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", image, "image/jpeg")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(200)
            .extract().path("data.fileUrl");

        // 第二次上传相同文件
        String fileUrl2 = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", image, "image/jpeg")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(200)
            .extract().path("data.fileUrl");

        // 验证: 返回相同URL
        assertEquals(fileUrl1, fileUrl2);
    }

    // 用例5: 验证图片压缩
    @Test
    void testUploadImage_Compression() {
        // 创建5MB高分辨率图片
        File largeImage = createTestImageFile("large.jpg", 5 * 1024 * 1024, 4000, 3000);

        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", largeImage, "image/jpeg")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(200)
            .extract().response();

        long uploadedSize = response.path("data.fileSize");

        // 验证: 压缩后小于原文件
        assertTrue(uploadedSize < 5 * 1024 * 1024, "图片应该被压缩");
    }

    // 用例6: 验证缩略图生成
    @Test
    void testUploadImage_ThumbnailGeneration() {
        File image = createTestImageFile("test.jpg", 1024 * 1024);

        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", image, "image/jpeg")
        .when()
            .post("/api/media/upload")
        .then()
            .statusCode(200)
            .extract().response();

        String thumbnailUrl = response.path("data.thumbnail");
        assertNotNull(thumbnailUrl);

        // 验证缩略图可访问
        // (需要实际访问URL验证，或检查OSS)
    }

    // 用例7: 并发上传测试
    @Test
    void testUploadImage_Concurrent() {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    File image = createTestImageFile("test_" + Thread.currentThread().getId() + ".jpg", 1024 * 500);

                    int statusCode = given()
                        .header("Authorization", "Bearer " + userToken)
                        .multiPart("file", image, "image/jpeg")
                    .when()
                        .post("/api/media/upload")
                    .then()
                        .extract().statusCode();

                    if (statusCode == 200) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(30, TimeUnit.SECONDS);
        assertEquals(threadCount, successCount.get(), "所有并发上传应该成功");
    }
}
```

---

## 数据准备

### 测试数据库初始化

**初始化脚本**: `test-data-init.sql`

```sql
-- 清空测试数据
TRUNCATE TABLE city;
TRUNCATE TABLE location;
TRUNCATE TABLE media_file;
TRUNCATE TABLE notification;
TRUNCATE TABLE report;
TRUNCATE TABLE punishment;

-- 插入测试城市数据
INSERT INTO city (city_code, city_name, province, pinyin, first_letter, center_lat, center_lng, is_hot, sort_order) VALUES
('110100', '北京', '北京市', 'beijing', 'B', 39.904989, 116.405285, 1, 1),
('310100', '上海', '上海市', 'shanghai', 'S', 31.230416, 121.473701, 1, 2),
('440300', '深圳', '广东省', 'shenzhen', 'S', 22.543099, 114.057868, 1, 3),
('440100', '广州', '广东省', 'guangzhou', 'G', 23.129110, 113.264385, 1, 4),
('330100', '杭州', '浙江省', 'hangzhou', 'H', 30.274084, 120.155070, 1, 5);

-- 插入测试用户
INSERT INTO user (user_id, nickname, avatar, mobile, password) VALUES
(1001, '测试用户1', 'https://avatar.com/1001.jpg', '13800001001', 'encrypted_password'),
(1002, '测试用户2', 'https://avatar.com/1002.jpg', '13800001002', 'encrypted_password');
```

### 测试数据构建器

**文件**: `TestDataBuilder.java`

```java
@Component
public class TestDataBuilder {

    /**
     * 创建测试图片文件
     */
    public File createTestImageFile(String fileName, long fileSize) {
        // 生成指定大小的测试图片
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 1920, 1080);
        g.dispose();

        File file = new File(System.getProperty("java.io.tmpdir"), fileName);
        ImageIO.write(image, "jpg", file);
        return file;
    }

    /**
     * 创建测试通知
     */
    public Notification createTestNotification(Long userId, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(2001L);
        notification.setType(type);
        notification.setContentType("post");
        notification.setContentId(5001L);
        notification.setContent("测试通知内容");
        notification.setIsRead(0);
        return notification;
    }

    /**
     * 创建测试城市
     */
    public City createTestCity(String cityCode, String cityName) {
        City city = new City();
        city.setCityCode(cityCode);
        city.setCityName(cityName);
        city.setProvince("测试省");
        city.setPinyin("test");
        city.setFirstLetter("T");
        city.setCenterLat(BigDecimal.valueOf(39.9));
        city.setCenterLng(BigDecimal.valueOf(116.4));
        return city;
    }
}
```

---

## 测试用例设计

### 总测试用例统计

| 测试类型 | 测试用例数 | 优先级 |
|---------|-----------|--------|
| **页面流程测试** | 25个 | P0 |
| **API集成测试** | 45个 | P0 |
| **RPC接口测试** | 35个 | P1 |
| **边界测试** | 20个 | P1 |
| **性能测试** | 10个 | P2 |
| **总计** | 135个 | - |

### 测试优先级定义

- **P0**: 核心功能，必须通过
- **P1**: 重要功能，建议通过
- **P2**: 增强功能，可以后续补充

---

## 执行计划

### 测试执行顺序

**Phase 1: 基础API测试** (预计2天)
- Day 1: 位置服务API测试
- Day 2: 媒体、通知、举报服务API测试

**Phase 2: 页面流程测试** (预计2天)
- Day 3: 城市/区域选择流程测试
- Day 4: 媒体上传、通知流程测试

**Phase 3: RPC接口测试** (预计1天)
- Day 5: 所有RPC接口测试

**Phase 4: 边界和性能测试** (预计1天)
- Day 6: 边界测试、性能测试、缺陷修复

### 执行环境配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_common_test
    username: test_user
    password: test_password

  redis:
    database: 15  # 使用独立的Redis数据库

oss:
  endpoint: https://test-bucket.oss.aliyuncs.com
  bucket: xypai-test
```

### 成功标准

- ✅ 所有P0用例100%通过
- ✅ P1用例通过率 ≥ 95%
- ✅ API响应时间 < 500ms (p95)
- ✅ 并发100用户无异常
- ✅ 无严重缺陷(Severity 1-2)

---

## 附录

### 测试工具

- **API测试**: RestAssured
- **数据库**: H2 (内存数据库) 或 MySQL Test
- **Mock**: WireMock (模拟第三方API)
- **性能测试**: JMeter 或 Gatling

### 参考文档

- [前端页面文档](e:\Users\Administrator\Documents\GitHub\XiangYuPai-Doc\Action-API\模块化架构\06-common模块\Frontend)
- [后端API文档](e:\Users\Administrator\Documents\GitHub\XiangYuPai-Doc\Action-API\模块化架构\06-common模块\Backend)
- [实现总结](./IMPLEMENTATION_SUMMARY.md)
- [接口验证文档](./FRONTEND_INTERFACE_VERIFICATION.md)

---

**文档版本**: v1.0
**创建日期**: 2025-11-14
**维护者**: QA Team
**状态**: ✅ **测试计划已完成，待执行**

---

**下一步**: 开始编写测试类实现，按照本文档的测试用例设计执行测试
