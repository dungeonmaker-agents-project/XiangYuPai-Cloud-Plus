# Testing Structure Summary - Quick Reference

## 测试结构快速参考

**目的**: 快速了解需要创建的测试文件和测试内容
**基于**: 前端页面流程和API需求
**完整计划**: 请查看 [BACKEND_TESTING_PLAN.md](./BACKEND_TESTING_PLAN.md)

---

## 📊 测试文件统计

### 总览

| 类别 | 文件数 | 测试用例数 | 说明 |
|------|--------|-----------|------|
| **页面流程测试** | 4个 | 25个 | 模拟前端完整操作流程 |
| **API集成测试** | 12个 | 45个 | HTTP API端到端测试 |
| **RPC接口测试** | 4个 | 35个 | Dubbo服务调用测试 |
| **测试支持类** | 6个 | - | 基类、工具类、数据构建器 |
| **总计** | **26个文件** | **105个用例** | 完整测试覆盖 |

---

## 📁 测试文件结构

```
xypai-common/src/test/java/org/dromara/common/
│
├── integration/                              # 集成测试目录
│   │
│   ├── flow/                                 # 【流程测试 - 4个文件】
│   │   ├── DistrictSelectionFlowTest.java       ① 区域选择流程 (5个用例)
│   │   ├── CityLocationFlowTest.java            ② 城市定位流程 (7个用例)
│   │   ├── MediaUploadFlowTest.java             ③ 媒体上传流程 (7个用例)
│   │   └── NotificationFlowTest.java            ④ 通知查看流程 (7个用例)
│   │
│   ├── api/                                  # 【API测试 - 12个文件】
│   │   ├── location/
│   │   │   ├── CityApiTest.java                 ⑤ 城市API (5个用例)
│   │   │   ├── DistrictApiTest.java             ⑥ 区域API (4个用例)
│   │   │   ├── LocationApiTest.java             ⑦ 位置API (5个用例)
│   │   │   └── GeocodeApiTest.java              ⑧ 地理编码API (3个用例)
│   │   │
│   │   ├── media/
│   │   │   ├── ImageUploadApiTest.java          ⑨ 图片上传API (7个用例)
│   │   │   ├── VideoUploadApiTest.java          ⑩ 视频上传API (5个用例)
│   │   │   └── MediaQueryApiTest.java           ⑪ 媒体查询API (3个用例)
│   │   │
│   │   ├── notification/
│   │   │   ├── NotificationListApiTest.java     ⑫ 通知列表API (4个用例)
│   │   │   ├── UnreadCountApiTest.java          ⑬ 未读数API (3个用例)
│   │   │   └── NotificationActionApiTest.java   ⑭ 通知操作API (4个用例)
│   │   │
│   │   └── report/
│   │       ├── ReportSubmitApiTest.java         ⑮ 举报提交API (3个用例)
│   │       └── PunishmentApiTest.java           ⑯ 处罚管理API (4个用例)
│   │
│   └── rpc/                                  # 【RPC测试 - 4个文件】
│       ├── RemoteLocationServiceTest.java       ⑰ 位置RPC (已有11个)
│       ├── RemoteMediaServiceTest.java          ⑱ 媒体RPC (已有17个)
│       ├── RemoteNotificationServiceTest.java   ⑲ 通知RPC (已有18个)
│       └── RemoteReportServiceTest.java         ⑳ 举报RPC (已有20个)
│
└── support/                                  # 【支持类 - 6个文件】
    ├── ApiTestBase.java                      ㉑ API测试基类
    ├── FlowTestBase.java                     ㉒ 流程测试基类
    ├── TestDataBuilder.java                  ㉓ 测试数据构建器
    ├── TestDataCleaner.java                  ㉔ 测试数据清理器
    └── assertions/
        ├── LocationAssertions.java           ㉕ 位置断言
        └── MediaAssertions.java              ㉖ 媒体断言
```

---

## 🎯 四大流程测试详解

### 流程1: 区域选择页面 (03-区域选择页面.md)

**测试文件**: `DistrictSelectionFlowTest.java`

**模拟用户操作**:
```
打开区域选择页
  ↓ GET /api/location/districts?cityCode=110100
获取北京的区域列表
  ↓ 显示: 全北京、东城区、西城区...
用户点击"朝阳区"
  ↓ POST /api/location/district/select
保存选择，返回首页
```

**5个测试用例**:
1. ✅ 完整选择流程
2. ✅ 选择"全城"
3. ✅ 城市无区域处理
4. ✅ 网络异常处理
5. ✅ 无效城市代码

---

### 流程2: 城市定位页面 (04-城市定位页面.md)

**测试文件**: `CityLocationFlowTest.java`

**模拟用户操作**:
```
打开城市选择页
  ↓ GET /api/location/cities
显示: 热门城市、全部城市(A-Z)
  ↓ 方式1: GPS定位
        POST /api/location/detect (lat, lng)
        → 返回: 北京
  ↓ 方式2: 点击热门城市"上海"
        POST /api/location/city/select
        → hasDistricts=true → 跳转区域选择
  ↓ 方式3: 浏览全部城市选择
```

**7个测试用例**:
1. ✅ 完整城市选择流程
2. ✅ GPS定位流程
3. ✅ 选择无区域城市
4. ✅ 城市列表数据验证
5. ✅ 字母分组正确性
6. ✅ 热门城市排序
7. ✅ 最近访问记录

---

### 流程3: 媒体上传流程

**测试文件**: `MediaUploadFlowTest.java`

**模拟用户操作**:
```
用户选择图片
  ↓ 前端验证: 类型、大小
  ↓ POST /api/media/upload (multipart/form-data)
后端处理:
  - 计算MD5 (检查秒传)
  - 如果已存在 → 直接返回URL (秒传)
  - 如果不存在:
      ├─ 压缩图片
      ├─ 生成缩略图
      └─ 上传OSS
  ↓ 返回: fileUrl, thumbnail
前端显示上传成功
```

**7个测试用例**:
1. ✅ 图片上传完整流程
2. ✅ 视频上传流程
3. ✅ MD5秒传
4. ✅ 文件大小限制
5. ✅ 文件类型验证
6. ✅ 图片压缩验证
7. ✅ 业务关联

---

### 流程4: 通知查看流程

**测试文件**: `NotificationFlowTest.java`

**模拟用户操作**:
```
打开通知页
  ↓ GET /api/notification/unread-count
显示红点: 11条未读
  ↓ 点击"点赞"分类
  ↓ GET /api/notification/list?type=like
显示: 5条点赞通知
  ↓ 用户点击第1条通知
  ↓ PUT /api/notification/read/{id}
标记已读，跳转到目标内容
  ↓ 未读数变为: 10条
```

**7个测试用例**:
1. ✅ 完整通知查看流程
2. ✅ 分类通知列表
3. ✅ 批量标记已读
4. ✅ 全部标记已读
5. ✅ 清除已读通知
6. ✅ 通知详情验证
7. ✅ 分页测试

---

## 🔧 测试支持类说明

### ApiTestBase.java - API测试基类

**作用**: 所有API测试的父类

**提供功能**:
```java
public abstract class ApiTestBase {
    protected String baseUrl = "http://localhost:9407";
    protected String userToken;  // 测试用户Token

    @BeforeEach
    void setUp() {
        // 1. 初始化测试数据
        // 2. 生成测试用户Token
        // 3. 配置RestAssured
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
    }

    // 辅助方法
    protected String loginTestUser() { ... }
    protected void cleanDatabase() { ... }
}
```

---

### FlowTestBase.java - 流程测试基类

**作用**: 所有流程测试的父类

**提供功能**:
```java
public abstract class FlowTestBase extends ApiTestBase {

    @Autowired
    protected TestDataBuilder dataBuilder;

    // 辅助方法: 模拟前端操作
    protected Response getCityList() { ... }
    protected Response selectCity(String cityCode) { ... }
    protected Response uploadImage(File file) { ... }
}
```

---

### TestDataBuilder.java - 测试数据构建器

**作用**: 创建各种测试数据

**提供方法**:
```java
@Component
public class TestDataBuilder {

    // 创建测试图片
    public File createTestImageFile(String name, long size);

    // 创建测试用户
    public User createTestUser(String mobile);

    // 创建测试通知
    public Notification createNotification(Long userId, String type);

    // 创建测试城市
    public City createTestCity(String code, String name);
}
```

---

## 📝 测试数据说明

### 前端发送的数据格式

**1. 区域选择**:
```json
POST /api/location/district/select
{
  "cityCode": "110100",
  "districtCode": "110105"  // "all" 表示全城
}
```

**2. GPS定位**:
```json
POST /api/location/detect
{
  "latitude": 39.904989,
  "longitude": 116.405285
}
```

**3. 图片上传**:
```
POST /api/media/upload
Content-Type: multipart/form-data

file: (binary)
bizType: "post"
```

**4. 通知查询**:
```
GET /api/notification/list?type=like&pageNum=1&pageSize=20
```

### 期望的响应格式

**统一响应结构**:
```json
{
  "code": 200,           // 状态码: 200=成功
  "msg": "操作成功",      // 消息
  "data": { ... }        // 业务数据
}
```

**城市列表响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "hotCities": [
      { "cityCode": "110100", "cityName": "北京", "isHot": 1 }
    ],
    "allCities": {
      "A": [...],
      "B": [...]
    }
  }
}
```

---

## ⏱️ 测试执行时间估算

| 阶段 | 文件数 | 用例数 | 预计时间 |
|------|--------|--------|---------|
| **流程测试** | 4 | 26 | 2天 |
| **API测试** | 12 | 45 | 2天 |
| **RPC测试** | 4 | 35 | 1天 |
| **编写支持类** | 6 | - | 0.5天 |
| **缺陷修复** | - | - | 0.5天 |
| **总计** | 26 | 106 | **6天** |

---

## ✅ 测试执行清单

### Phase 1: 准备工作 (Day 1上午)
- [ ] 创建测试支持类 (6个文件)
- [ ] 准备测试数据库
- [ ] 配置测试环境

### Phase 2: 流程测试 (Day 1下午 - Day 2)
- [ ] DistrictSelectionFlowTest.java (5个用例)
- [ ] CityLocationFlowTest.java (7个用例)
- [ ] MediaUploadFlowTest.java (7个用例)
- [ ] NotificationFlowTest.java (7个用例)

### Phase 3: API测试 (Day 3 - Day 4)
- [ ] Location API测试 (4个文件, 17个用例)
- [ ] Media API测试 (3个文件, 15个用例)
- [ ] Notification API测试 (3个文件, 11个用例)
- [ ] Report API测试 (2个文件, 7个用例)

### Phase 4: RPC测试 (Day 5)
- [ ] 验证已有的RPC单元测试
- [ ] 补充集成测试场景

### Phase 5: 总结 (Day 6)
- [ ] 执行所有测试
- [ ] 生成测试报告
- [ ] 缺陷修复
- [ ] 回归测试

---

## 🎯 成功标准

### 功能测试
- ✅ 所有流程测试通过 (26/26)
- ✅ 所有API测试通过 (50/50)
- ✅ 所有RPC测试通过 (66/66)

### 性能标准
- ✅ API响应时间 < 500ms (P95)
- ✅ 图片上传时间 < 2s
- ✅ 支持并发100用户

### 质量标准
- ✅ 代码覆盖率 > 90%
- ✅ 无P0/P1级别缺陷
- ✅ 测试用例通过率 100%

---

## 📚 相关文档

1. **完整测试计划**: [BACKEND_TESTING_PLAN.md](./BACKEND_TESTING_PLAN.md) ⭐ **必读**
2. **前端接口验证**: [FRONTEND_INTERFACE_VERIFICATION.md](./FRONTEND_INTERFACE_VERIFICATION.md)
3. **单元测试文档**: [TESTING_DOCUMENTATION.md](./TESTING_DOCUMENTATION.md)
4. **实现总结**: [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)

---

## 🚀 快速开始

### 创建第一个测试文件

```bash
# 1. 创建测试目录
mkdir -p src/test/java/org/dromara/common/integration/flow
mkdir -p src/test/java/org/dromara/common/support

# 2. 创建ApiTestBase.java (基类)
# 3. 创建DistrictSelectionFlowTest.java (第一个流程测试)
# 4. 运行测试
mvn test -Dtest=DistrictSelectionFlowTest
```

### 测试示例代码

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DistrictSelectionFlowTest extends FlowTestBase {

    @Test
    @DisplayName("完整的区域选择流程")
    void testCompleteDistrictSelectionFlow() {
        // Given: 用户已登录，在北京
        String cityCode = "110100";

        // When: 获取区域列表
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .param("cityCode", cityCode)
        .when()
            .get("/api/location/districts")
        .then()
            .statusCode(200)
            .body("data.cityName", equalTo("北京"))
            .body("data.districts", hasSize(greaterThan(0)))
            .extract().response();

        // Then: 选择朝阳区
        given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body("{\"cityCode\":\"110100\",\"districtCode\":\"110105\"}")
        .when()
            .post("/api/location/district/select")
        .then()
            .statusCode(200)
            .body("data.success", equalTo(true))
            .body("data.selectedDistrict.name", equalTo("朝阳区"));
    }
}
```

---

**文档版本**: v1.0
**创建日期**: 2025-11-14
**维护者**: QA Team
**下一步**: 查看 [BACKEND_TESTING_PLAN.md](./BACKEND_TESTING_PLAN.md) 开始编写测试
