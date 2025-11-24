# XiangYuPai User Module - Test Suite Documentation

## 概述 Overview

本测试套件为 **XiangYuPai 用户服务模块** 提供全面的自动化测试,涵盖控制器层、服务层和完整的业务流程集成测试。

This test suite provides comprehensive automated testing for the **XiangYuPai User Service Module**, covering controller layer, service layer, and complete business workflow integration tests.

## 测试组织结构 Test Structure

```
src/test/java/org/dromara/user/
├── BaseTest.java                           # 测试基类
├── controller/                             # 控制器测试
│   ├── ProfileControllerTest.java         # 用户资料控制器测试 (页面01, 02)
│   ├── SkillControllerTest.java           # 技能控制器测试 (页面06)
│   ├── SkillConfigControllerTest.java     # 技能配置控制器测试 (页面06)
│   └── RelationControllerTest.java        # 用户关系控制器测试 (页面03, 04, 05)
├── service/                                # 服务层测试
│   └── UserServiceTest.java               # 用户服务业务逻辑测试
└── integration/                            # 集成测试
    ├── ProfileEditIntegrationTest.java    # 个人资料编辑完整流程测试
    └── UserWorkflowIntegrationTest.java   # 用户工作流集成测试
```

## 测试覆盖范围 Test Coverage

### 1. 控制器层测试 (Controller Tests)

#### ProfileControllerTest (60+ 测试用例)
**覆盖页面:**
- **页面01: 个人主页** (`/user/profile`)
- **页面02: 编辑资料** (`/user/profile/edit`)

**测试场景:**
- ✅ 获取个人主页头部信息
- ✅ 获取他人主页(包含隐私控制)
- ✅ 获取编辑资料数据
- ✅ 更新昵称(含验证)
- ✅ 更新性别、生日、居住地
- ✅ 更新身高、体重、职业
- ✅ 更新微信号、个人简介
- ✅ 上传头像(含文件验证)
- ✅ 实时保存机制(300ms防抖)
- ✅ 异常场景(未认证、用户不存在)

#### SkillControllerTest (50+ 测试用例)
**覆盖页面:**
- **页面06: 技能管理** (`/user/skills`)

**测试场景:**
- ✅ 创建线上技能(陪玩类)
- ✅ 创建线下技能(服务类)
- ✅ 更新技能信息
- ✅ 删除技能
- ✅ 上架/下架技能
- ✅ 获取技能详情
- ✅ 获取我的技能列表
- ✅ 获取用户技能列表
- ✅ 搜索附近技能(基于地理位置)
- ✅ 分页查询(hasMore标志)
- ✅ 数据验证(价格、图片数量等)

#### SkillConfigControllerTest (15+ 测试用例)
**覆盖页面:**
- **页面06: 技能配置** (`/api/skills/config`)

**测试场景:**
- ✅ 获取游戏列表和段位
- ✅ 获取服务类型列表
- ✅ 上传技能图片(JPG/PNG)
- ✅ 文件验证(大小、格式)
- ✅ 边界测试(正好5MB、超过5MB)

#### RelationControllerTest (40+ 测试用例)
**覆盖页面:**
- **页面03: 他人主页** (`/users/{userId}/profile`)
- **页面04: 粉丝列表** (`/user/relation/fans`)
- **页面05: 关注列表** (`/user/relation/following`)

**测试场景:**
- ✅ 关注用户
- ✅ 取消关注
- ✅ 获取粉丝列表
- ✅ 获取关注列表
- ✅ 搜索粉丝/关注(500ms防抖)
- ✅ 回关粉丝
- ✅ 拉黑用户
- ✅ 取消拉黑
- ✅ 举报用户(多种举报类型)
- ✅ 分页查询(hasMore判断)
- ✅ 业务规则验证(不能关注/拉黑/举报自己)

### 2. 服务层测试 (Service Tests)

#### UserServiceTest (23+ 测试用例)
**测试重点:**
- ✅ 用户资料获取(存在/不存在)
- ✅ 昵称更新(长度限制、敏感词过滤)
- ✅ 性别更新(有效值验证)
- ✅ 生日更新(未来日期、年龄验证、过于久远)
- ✅ 身高/体重更新(合理范围验证)
- ✅ 个人简介更新(长度限制)
- ✅ 头像上传(文件验证、格式检查)
- ✅ 并发更新(乐观锁测试)
- ✅ 业务规则(微信号格式、隐私控制)

### 3. 集成测试 (Integration Tests)

#### ProfileEditIntegrationTest (8个E2E流程)
**完整流程测试:**
- ✅ **E2E-EDIT-001**: 完整编辑资料流程(所有11个字段)
- ✅ **E2E-EDIT-002**: 实时保存机制(连续快速编辑)
- ✅ **E2E-EDIT-003**: 编辑失败后重试流程
- ✅ **E2E-EDIT-004**: 部分字段更新
- ✅ **E2E-EDIT-005**: 头像上传失败后重新上传
- ✅ **E2E-EDIT-006**: 数据验证完整流程
- ✅ **E2E-EDIT-007**: 编辑资料后他人查看
- ✅ **E2E-EDIT-008**: 乐观更新UI测试

#### UserWorkflowIntegrationTest (8个E2E流程)
**完整流程测试:**
- ✅ **E2E-SKILL-001**: 创建线上技能完整流程
- ✅ **E2E-SKILL-002**: 创建线下技能完整流程
- ✅ **E2E-SKILL-003**: 编辑技能 -> 下架 -> 重新上架
- ✅ **E2E-RELATION-001**: 查看他人主页 -> 关注 -> 查看技能
- ✅ **E2E-RELATION-002**: 粉丝列表 -> 回关 -> 互相关注
- ✅ **E2E-RELATION-003**: 搜索粉丝/关注 -> 取消关注
- ✅ **E2E-RELATION-004**: 举报用户 -> 拉黑
- ✅ **E2E-NEARBY-001**: 搜索附近技能 -> 查看详情 -> 联系
- ✅ **E2E-COMPREHENSIVE-001**: 新用户完整使用流程

## 如何运行测试 How to Run Tests

### 1. 运行所有测试 Run All Tests

```bash
# Maven命令
mvn test

# 或指定模块
mvn test -pl xypai-user
```

### 2. 运行特定测试类 Run Specific Test Class

```bash
# 运行ProfileController测试
mvn test -Dtest=ProfileControllerTest

# 运行集成测试
mvn test -Dtest=ProfileEditIntegrationTest
```

### 3. 运行特定测试方法 Run Specific Test Method

```bash
mvn test -Dtest=ProfileControllerTest#testGetProfileHeader_Success
```

### 4. 使用IDE运行 Run with IDE

**IntelliJ IDEA:**
- 右键点击测试类或测试方法
- 选择 "Run 'TestClassName'" 或 "Debug 'TestClassName'"

**Eclipse:**
- 右键点击测试类或测试方法
- 选择 "Run As" -> "JUnit Test"

### 5. 生成测试报告 Generate Test Report

```bash
mvn test surefire-report:report
```

报告位置: `target/site/surefire-report.html`

## 测试数据准备 Test Data Preparation

测试使用以下预定义数据:

```java
// 测试用户ID
protected static final Long TEST_USER_ID = 1000L;
protected static final Long TEST_OTHER_USER_ID = 2000L;
protected static final Long TEST_SKILL_ID = 3000L;

// 测试Token
protected static final String TEST_TOKEN = "test-token-123456";
```

**数据库初始化:**
测试使用H2内存数据库,每次测试运行前自动初始化。可在 `src/test/resources/data.sql` 中准备测试数据。

## 测试配置 Test Configuration

测试配置文件: `src/test/resources/application-test.yml`

**关键配置:**
- 数据库: H2内存数据库 (自动初始化/清理)
- Redis: 嵌入式Redis或Mock
- 文件上传: 本地临时目录
- 日志级别: DEBUG (便于调试)

## 测试最佳实践 Testing Best Practices

### 1. 测试命名规范

```java
@Test
@DisplayName("TC-PROFILE-001: 获取个人主页头部信息 - 成功")
public void testGetProfileHeader_Success() {
    // Given: 准备测试数据
    // When: 执行操作
    // Then: 验证结果
}
```

**命名约定:**
- 测试ID: `TC-模块-序号`
- 方法名: `test{Operation}_{Scenario}`
- 显示名: 清晰的中文描述

### 2. AAA模式 (Arrange-Act-Assert)

```java
@Test
public void testExample() {
    // Arrange (Given): 准备测试数据和环境
    UserProfileVo mockProfile = UserProfileVo.builder()
        .userId(TEST_USER_ID)
        .nickname("测试用户")
        .build();

    // Act (When): 执行被测试的操作
    R<UserProfileVo> result = userService.getUserProfile(TEST_USER_ID);

    // Assert (Then): 验证结果
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals("测试用户", result.getData().getNickname());
}
```

### 3. Mock使用原则

- **控制器测试**: Mock Service层
- **Service测试**: Mock Mapper层和外部依赖
- **集成测试**: 尽量减少Mock,测试真实交互

### 4. 测试隔离

每个测试方法独立运行,互不影响:
- 使用 `@BeforeEach` 初始化测试环境
- 使用 `@AfterEach` 清理测试数据
- 避免测试间的数据污染

## 前端页面对应关系 Frontend Page Mapping

| 测试类 | 覆盖页面 | 页面路由 | 核心功能 |
|--------|---------|----------|---------|
| ProfileControllerTest | 01-个人主页 | `/user/profile` | 查看个人资料、统计数据 |
| ProfileControllerTest | 02-编辑资料 | `/user/profile/edit` | 实时编辑个人资料 |
| RelationControllerTest | 03-他人主页 | `/users/{id}/profile` | 查看他人资料、关注、举报 |
| RelationControllerTest | 04-粉丝列表 | `/user/relation/fans` | 查看粉丝、回关 |
| RelationControllerTest | 05-关注列表 | `/user/relation/following` | 查看关注、取消关注 |
| SkillControllerTest | 06-技能管理 | `/user/skills` | 创建/编辑/管理技能 |

## 性能基准 Performance Benchmarks

基于测试结果的性能要求:

| 接口类型 | P95响应时间 | 并发要求 |
|---------|------------|---------|
| 用户资料查询 | < 100ms | 1000 QPS |
| 用户资料更新 | < 200ms | 500 QPS |
| 技能列表查询 | < 150ms | 800 QPS |
| 附近技能搜索 | < 300ms | 200 QPS |
| 关注/取消关注 | < 100ms | 500 QPS |

**性能测试工具:**
- 单元测试: JUnit 5
- 压力测试: JMeter (参考 `完整测试用例文档.md`)
- 接口测试: Postman Collection

## CI/CD集成 CI/CD Integration

### GitHub Actions示例

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test -pl xypai-user
      - name: Generate test report
        run: mvn surefire-report:report
      - name: Upload test results
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: xypai-user/target/surefire-reports/
```

## 故障排查 Troubleshooting

### 常见问题

**1. 测试运行失败: "No tests found"**
```bash
# 确保测试类名以Test结尾
# 确保测试方法有@Test注解
```

**2. H2数据库连接失败**
```bash
# 检查application-test.yml中的数据库配置
# 确保H2依赖已添加到pom.xml
```

**3. Mock对象返回null**
```java
// 确保Mock对象正确初始化
@Mock
private IUserService userService;

@InjectMocks
private ProfileController profileController;

// 确保when-then正确配置
when(userService.getUserProfile(anyLong())).thenReturn(R.ok(mockData));
```

**4. 集成测试失败: "Authentication required"**
```java
// 确保请求包含认证头
mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/header")
    .header(AUTHORIZATION_HEADER, getAuthHeader()) // 必须
    .contentType(MediaType.APPLICATION_JSON))
```

## 测试覆盖率 Test Coverage

**目标覆盖率:**
- 控制器层: > 90%
- 服务层: > 85%
- 整体代码: > 80%

**生成覆盖率报告:**

```bash
mvn jacoco:prepare-agent test jacoco:report
```

报告位置: `target/site/jacoco/index.html`

## 下一步改进 Next Steps

- [ ] 添加性能测试用例
- [ ] 集成Testcontainers进行真实数据库测试
- [ ] 添加契约测试(Contract Testing)
- [ ] 增加前端E2E测试(Cypress)
- [ ] 集成SonarQube进行代码质量分析
- [ ] 添加混沌工程测试

## 联系方式 Contact

如有问题或建议,请联系:
- **后端团队**: backend-team@xiangyupai.com
- **测试团队**: qa-team@xiangyupai.com

---

**版本**: v1.0.0
**更新日期**: 2025-11-15
**作者**: XiangYuPai Backend Team
