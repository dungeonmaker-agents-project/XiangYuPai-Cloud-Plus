# 集成文档 04: 首页 Feed 流模块 (Page01-05)

> 版本: v1.0.0 | 创建日期: 2025-11-26 | 优先级: ⭐⭐⭐ (中)

---

## 📋 模块概述

### 业务描述
首页 Feed 流是向娱拍平台的核心入口，展示用户推荐、筛选功能和限时专享活动。包括线上/线下用户推荐、多维度筛选、限时优惠等功能模块。

### 对应前端页面
| 页面 | 路由 | 功能 |
|------|------|------|
| Page01 | `/home` | 首页 Feed 流（用户推荐） |
| Page02 | `/home/filter` | 筛选弹窗（配置/结果） |
| Page05 | `/home/limited` | 限时专享列表 |

### 当前状态
- **BFF 层**: 部分 RPC 集成 + 部分 Mock
- **筛选功能**: ✅ RPC 已集成
- **限时专享**: ✅ RPC 已集成
- **首页 Feed**: ⏳ Mock 实现，待优化
- **推荐算法**: 🔮 待实现

---

## 🎯 集成目标

1. 完善 `HomeFeedController` 中的 Mock 实现，添加 Service 层和 RPC 调用
2. 增强现有 RPC 集成的功能（批量获取用户信息、在线状态、距离计算）
3. 为未来的推荐算法预留接口

---

## 📁 涉及文件清单

### BFF 层 (xypai-app-bff) - 已集成 RPC

| 文件路径 | 类型 | 状态 | 说明 |
|----------|------|------|------|
| `controller/HomeFilterController.java` | Controller | ✅ 已集成 | 筛选配置和结果 |
| `service/HomeFilterService.java` | Interface | ✅ 已实现 | 筛选服务接口 |
| `service/impl/HomeFilterServiceImpl.java` | Impl | ✅ 已集成RPC | 使用 `RemoteAppUserService` |
| `controller/HomeLimitedTimeController.java` | Controller | ✅ 已集成 | 限时专享接口 |
| `service/HomeLimitedTimeService.java` | Interface | ✅ 已实现 | 限时服务接口 |
| `service/impl/HomeLimitedTimeServiceImpl.java` | Impl | ✅ 已集成RPC | 使用 `RemoteAppUserService` |

### BFF 层 (xypai-app-bff) - 待优化

| 文件路径 | 类型 | 状态 | 操作 |
|----------|------|------|------|
| `controller/HomeFeedController.java` | Controller | ⚠️ Mock实现 | ✅ **需重构** |
| `service/HomeFeedService.java` | Interface | ❌ 不存在 | ✅ **新建** |
| `service/impl/HomeFeedServiceImpl.java` | Impl | ❌ 不存在 | ✅ **新建** |
| `domain/dto/HomeFeedQueryDTO.java` | DTO | ✅ 已存在 | ❌ 无需修改 |
| `domain/vo/UserCardVO.java` | VO | ✅ 已存在 | ❌ 无需修改 |
| `domain/vo/HomeFeedResultVO.java` | VO | ❌ 不存在 | ✅ **新建** |

### RPC API 层 (待扩展)

| 文件路径 | 类型 | 状态 | 操作 |
|----------|------|------|------|
| `RemoteAppUserService.java` | Interface | ✅ 已有 | ✅ **新增方法** |
| `domain/dto/FeedQueryDto.java` | DTO | ❌ 不存在 | ✅ **新建** |
| `domain/vo/FeedUserVo.java` | VO | ❌ 不存在 | ✅ **新建** |
| `domain/vo/FeedPageResult.java` | VO | ❌ 不存在 | ✅ **新建** |

### 可选新增服务

| 服务 | 接口 | 用途 | 状态 |
|------|------|------|------|
| `xypai-chat` | `RemoteChatService.batchCheckOnlineStatus()` | 批量获取在线状态 | 🔮 可选 |
| `xypai-common` | `RemoteLocationService.calculateBatchDistance()` | 批量计算距离 | 🔮 可选 |

---

## 🔧 Step 1: 扩展 RemoteAppUserService

### 1.1 新增 RPC 方法

**文件**: `ruoyi-api/xypai-api-appuser/src/main/java/org/dromara/appuser/api/RemoteAppUserService.java`

```java
// 在现有接口中新增以下方法 (Feed流专用):

/**
 * 查询首页 Feed 流用户列表
 *
 * @param queryDto 查询条件
 * @return 用户分页结果
 */
FeedPageResult queryFeedUsers(FeedQueryDto queryDto);

/**
 * 批量获取用户信息
 *
 * @param userIds 用户ID列表
 * @return 用户信息Map
 */
Map<Long, FeedUserVo> batchGetUsersByIds(List<Long> userIds);

/**
 * 获取用户推荐列表（基于推荐算法）
 *
 * @param userId 当前用户ID
 * @param type 类型: online/offline
 * @param pageNum 页码
 * @param pageSize 每页数量
 * @return 推荐用户列表
 */
FeedPageResult getRecommendedUsers(Long userId, String type, Integer pageNum, Integer pageSize);
```

### 1.2 新增 DTO/VO 类

**文件**: `ruoyi-api/xypai-api-appuser/src/main/java/org/dromara/appuser/api/domain/dto/FeedQueryDto.java`

```java
package org.dromara.appuser.api.domain.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serial;
import java.io.Serializable;

/**
 * Feed 流查询 DTO
 */
@Data
@Builder
public class FeedQueryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Feed类型: online(线上), offline(线下) */
    private String type;

    /** 页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;

    /** 城市代码 */
    private String cityCode;

    /** 区域代码 */
    private String districtCode;

    /** 当前用户ID（用于排除自己、计算推荐等） */
    private Long currentUserId;

    /** 当前用户经度 */
    private Double longitude;

    /** 当前用户纬度 */
    private Double latitude;

    /** 排序方式: smart(智能推荐), distance(距离), online(在线优先), newest(最新) */
    private String sortBy;
}
```

**文件**: `ruoyi-api/xypai-api-appuser/src/main/java/org/dromara/appuser/api/domain/vo/FeedUserVo.java`

```java
package org.dromara.appuser.api.domain.vo;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Feed 流用户 VO
 */
@Data
public class FeedUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ===== 基本信息 =====
    private Long userId;
    private String nickname;
    private String avatar;
    private String gender;
    private Integer age;
    private String bio;

    // ===== 位置信息 =====
    private String city;
    private String district;
    private Integer distance;

    // ===== 状态信息 =====
    private Boolean isOnline;
    private String lastActiveTime;

    // ===== 技能信息 =====
    private List<String> skills;
    private String mainSkill;
    private BigDecimal minPrice;
    private String priceUnit;

    // ===== 统计信息 =====
    private Integer fansCount;
    private Integer feedCount;
    private Integer orderCount;
    private BigDecimal rating;

    // ===== 认证信息 =====
    private Boolean isVerified;
    private String verifyType;

    // ===== 标签 =====
    private List<TagVo> tags;

    @Data
    public static class TagVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String text;
        private String type;
        private String color;
    }
}
```

**文件**: `ruoyi-api/xypai-api-appuser/src/main/java/org/dromara/appuser/api/domain/vo/FeedPageResult.java`

```java
package org.dromara.appuser.api.domain.vo;

import lombok.Data;
import lombok.Builder;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Feed 流分页结果
 */
@Data
@Builder
public class FeedPageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<FeedUserVo> list;

    /** 总数 */
    private Long total;

    /** 是否有更多 */
    private Boolean hasMore;

    /** Feed 类型 */
    private String type;

    /** 刷新时间戳 */
    private Long refreshTimestamp;
}
```

---

## 🔧 Step 2: 领域服务实现

### 2.1 扩展 RemoteAppUserServiceImpl

**文件**: `xypai-modules/xypai-user/src/main/java/org/dromara/user/service/remote/RemoteAppUserServiceImpl.java`

```java
// 新增 Feed 相关方法

@Override
public FeedPageResult queryFeedUsers(FeedQueryDto queryDto) {
    log.info("查询Feed流用户: {}", queryDto);

    // 1. 构建查询条件
    LambdaQueryWrapper<XyUser> wrapper = new LambdaQueryWrapper<>();

    // 排除当前用户
    if (queryDto.getCurrentUserId() != null) {
        wrapper.ne(XyUser::getId, queryDto.getCurrentUserId());
    }

    // 线上/线下筛选
    if ("online".equals(queryDto.getType())) {
        // 线上：有技能的用户
        wrapper.exists("SELECT 1 FROM skill WHERE skill.user_id = xy_user.id AND skill.status = 1");
    } else {
        // 线下：所有活跃用户
        wrapper.ge(XyUser::getLastLoginTime, LocalDateTime.now().minusDays(30));
    }

    // 城市筛选
    if (StringUtils.isNotBlank(queryDto.getCityCode())) {
        wrapper.eq(XyUser::getCityCode, queryDto.getCityCode());
    }

    // 排序
    applySorting(wrapper, queryDto.getSortBy());

    // 2. 分页查询
    Page<XyUser> page = userMapper.selectPage(
        new Page<>(queryDto.getPageNum(), queryDto.getPageSize()),
        wrapper
    );

    // 3. 转换结果
    List<FeedUserVo> voList = page.getRecords().stream()
        .map(user -> convertToFeedUserVo(user, queryDto))
        .collect(Collectors.toList());

    // 4. 计算距离
    if (queryDto.getLongitude() != null && queryDto.getLatitude() != null) {
        calculateDistances(voList, queryDto.getLongitude(), queryDto.getLatitude());
    }

    return FeedPageResult.builder()
        .list(voList)
        .total(page.getTotal())
        .hasMore(page.hasNext())
        .type(queryDto.getType())
        .refreshTimestamp(System.currentTimeMillis())
        .build();
}

@Override
public Map<Long, FeedUserVo> batchGetUsersByIds(List<Long> userIds) {
    log.info("批量获取用户: ids={}", userIds);

    if (userIds == null || userIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<XyUser> users = userMapper.selectBatchIds(userIds);
    return users.stream()
        .collect(Collectors.toMap(
            XyUser::getId,
            user -> convertToFeedUserVo(user, null)
        ));
}

@Override
public FeedPageResult getRecommendedUsers(Long userId, String type, Integer pageNum, Integer pageSize) {
    log.info("获取推荐用户: userId={}, type={}", userId, type);

    // TODO: 实现推荐算法
    // 目前使用简单的随机+在线优先策略
    FeedQueryDto queryDto = FeedQueryDto.builder()
        .type(type)
        .pageNum(pageNum)
        .pageSize(pageSize)
        .currentUserId(userId)
        .sortBy("smart")
        .build();

    return queryFeedUsers(queryDto);
}

// ===== 私有方法 =====

private void applySorting(LambdaQueryWrapper<XyUser> wrapper, String sortBy) {
    if (sortBy == null) {
        sortBy = "smart";
    }

    switch (sortBy) {
        case "online":
            wrapper.orderByDesc(XyUser::getIsOnline);
            break;
        case "newest":
            wrapper.orderByDesc(XyUser::getCreateTime);
            break;
        case "distance":
            // 距离排序需要在 Java 层处理
            wrapper.orderByDesc(XyUser::getIsOnline);
            break;
        default: // smart
            wrapper.orderByDesc(XyUser::getIsOnline)
                   .orderByDesc(XyUser::getLastLoginTime);
    }
}

private FeedUserVo convertToFeedUserVo(XyUser user, FeedQueryDto queryDto) {
    FeedUserVo vo = new FeedUserVo();
    vo.setUserId(user.getId());
    vo.setNickname(user.getNickname());
    vo.setAvatar(user.getAvatar());
    vo.setGender(user.getGender());
    vo.setAge(calculateAge(user.getBirthday()));
    vo.setBio(user.getBio());
    vo.setCity(user.getCityName());
    vo.setIsOnline(user.getIsOnline());

    // 获取用户技能
    List<Skill> skills = skillMapper.selectByUserId(user.getId());
    if (skills != null && !skills.isEmpty()) {
        vo.setSkills(skills.stream()
            .map(Skill::getSkillName)
            .collect(Collectors.toList()));
        Skill mainSkill = skills.get(0);
        vo.setMainSkill(mainSkill.getSkillName());
        vo.setMinPrice(mainSkill.getPrice());
        vo.setPriceUnit(mainSkill.getPriceUnit());
    }

    // 获取统计信息
    UserStats stats = userStatsMapper.selectByUserId(user.getId());
    if (stats != null) {
        vo.setFansCount(stats.getFansCount());
        vo.setFeedCount(stats.getPostsCount());
        vo.setOrderCount(stats.getOrderCount());
        vo.setRating(stats.getRating());
    }

    // 认证信息
    vo.setIsVerified(user.getIsVerified());
    vo.setVerifyType(user.getVerifyType());

    return vo;
}

private void calculateDistances(List<FeedUserVo> users, Double longitude, Double latitude) {
    // 简单的距离计算（可选：调用 RemoteLocationService）
    for (FeedUserVo user : users) {
        // 从用户位置表获取用户经纬度
        UserLocation location = userLocationMapper.selectByUserId(user.getUserId());
        if (location != null && location.getLongitude() != null && location.getLatitude() != null) {
            int distance = calculateDistanceInMeters(
                latitude, longitude,
                location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue()
            );
            user.setDistance(distance);
        }
    }
}

private int calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371000; // 地球半径（米）
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return (int) (R * c);
}
```

---

## 🔧 Step 3: BFF 层实现

### 3.1 新建 HomeFeedService

**文件**: `xypai-aggregation/xypai-app-bff/src/main/java/org/dromara/appbff/service/HomeFeedService.java`

```java
package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.HomeFeedResultVO;

/**
 * 首页Feed流服务接口
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
public interface HomeFeedService {

    /**
     * 获取首页Feed流用户列表
     *
     * @param queryDTO 查询条件
     * @param currentUserId 当前用户ID（可为null）
     * @return Feed流结果
     */
    HomeFeedResultVO getFeedList(HomeFeedQueryDTO queryDTO, Long currentUserId);
}
```

### 3.2 新建 HomeFeedServiceImpl

**文件**: `xypai-aggregation/xypai-app-bff/src/main/java/org/dromara/appbff/service/impl/HomeFeedServiceImpl.java`

```java
package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.HomeFeedResultVO;
import org.dromara.appbff.domain.vo.UserCardVO;
import org.dromara.appbff.service.HomeFeedService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.dto.FeedQueryDto;
import org.dromara.appuser.api.domain.vo.FeedPageResult;
import org.dromara.appuser.api.domain.vo.FeedUserVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页Feed流服务实现类（RPC 版本）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeFeedServiceImpl implements HomeFeedService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @Override
    public HomeFeedResultVO getFeedList(HomeFeedQueryDTO queryDTO, Long currentUserId) {
        log.info("获取Feed流: queryDTO={}, currentUserId={}", queryDTO, currentUserId);

        // 1. 构建 RPC 请求
        FeedQueryDto rpcQuery = FeedQueryDto.builder()
            .type(queryDTO.getType())
            .pageNum(queryDTO.getPageNum())
            .pageSize(queryDTO.getPageSize())
            .cityCode(queryDTO.getCityCode())
            .districtCode(queryDTO.getDistrictCode())
            .currentUserId(currentUserId)
            .longitude(queryDTO.getLongitude())
            .latitude(queryDTO.getLatitude())
            .sortBy("smart")
            .build();

        // 2. 调用 RPC
        FeedPageResult rpcResult = remoteAppUserService.queryFeedUsers(rpcQuery);

        // 3. 转换结果
        List<UserCardVO> userList = rpcResult.getList().stream()
            .map(this::convertToUserCard)
            .collect(Collectors.toList());

        // 4. 构建响应
        return HomeFeedResultVO.builder()
            .list(userList)
            .total(rpcResult.getTotal())
            .hasMore(rpcResult.getHasMore())
            .type(rpcResult.getType())
            .refreshTimestamp(rpcResult.getRefreshTimestamp())
            .build();
    }

    /**
     * 转换 RPC VO 为 BFF VO
     */
    private UserCardVO convertToUserCard(FeedUserVo rpcUser) {
        UserCardVO user = new UserCardVO();
        user.setUserId(rpcUser.getUserId());
        user.setNickname(rpcUser.getNickname());
        user.setAvatar(rpcUser.getAvatar());

        // 性别转换: male/female -> 1/2
        if ("male".equals(rpcUser.getGender())) {
            user.setGender(1);
        } else if ("female".equals(rpcUser.getGender())) {
            user.setGender(2);
        } else {
            user.setGender(0);
        }

        user.setAge(rpcUser.getAge() != null ? rpcUser.getAge() : 0);
        user.setCity(rpcUser.getCity());
        user.setBio(rpcUser.getBio());
        user.setIsOnline(rpcUser.getIsOnline() != null ? rpcUser.getIsOnline() : false);
        user.setSkills(rpcUser.getSkills());
        user.setFansCount(rpcUser.getFansCount() != null ? rpcUser.getFansCount() : 0);
        user.setFeedCount(rpcUser.getFeedCount() != null ? rpcUser.getFeedCount() : 0);

        // 距离
        if (rpcUser.getDistance() != null && rpcUser.getDistance() > 0) {
            user.setDistance((double) rpcUser.getDistance());
            user.setDistanceText(formatDistance(rpcUser.getDistance()));
        }

        // 关注状态（需要后续查询，暂时默认false）
        user.setIsFollowed(false);

        return user;
    }

    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "m";
        } else {
            return String.format("%.1fkm", meters / 1000.0);
        }
    }
}
```

### 3.3 新建 HomeFeedResultVO

**文件**: `xypai-aggregation/xypai-app-bff/src/main/java/org/dromara/appbff/domain/vo/HomeFeedResultVO.java`

```java
package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 首页Feed流结果VO
 */
@Data
@Builder
@Schema(description = "首页Feed流结果")
public class HomeFeedResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户列表")
    private List<UserCardVO> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "Feed类型: online/offline")
    private String type;

    @Schema(description = "刷新时间戳")
    private Long refreshTimestamp;
}
```

### 3.4 重构 HomeFeedController

**文件**: `xypai-aggregation/xypai-app-bff/src/main/java/org/dromara/appbff/controller/HomeFeedController.java`

```java
package org.dromara.appbff.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.HomeFeedResultVO;
import org.dromara.appbff.service.HomeFeedService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页 Feed 流控制器 (BFF 聚合服务)
 *
 * 重构后使用 RPC 调用真实数据
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/home")
@Tag(name = "首页Feed流", description = "首页用户推荐流相关接口")
public class HomeFeedController {

    private final HomeFeedService homeFeedService;

    /**
     * 获取首页用户推荐 Feed 流
     *
     * @param queryDTO 查询参数
     * @return Feed流结果
     */
    @GetMapping("/feed")
    @Operation(summary = "获取首页Feed流", description = "获取线上/线下用户推荐列表")
    public R<HomeFeedResultVO> getHomeFeed(HomeFeedQueryDTO queryDTO) {
        log.info("获取首页Feed流 - type: {}, pageNum: {}, cityCode: {}",
            queryDTO.getType(), queryDTO.getPageNum(), queryDTO.getCityCode());

        // 获取当前登录用户ID
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

        // 设置默认值
        if (queryDTO.getType() == null) {
            queryDTO.setType("online");
        }
        if (queryDTO.getPageNum() == null) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null) {
            queryDTO.setPageSize(10);
        }

        // 调用服务层
        HomeFeedResultVO result = homeFeedService.getFeedList(queryDTO, currentUserId);

        return R.ok(result);
    }
}
```

---

## 🧪 Step 4: 测试验证

### 4.1 单元测试

```java
@SpringBootTest
public class HomeFeedServiceImplTest {

    @Autowired
    private HomeFeedService homeFeedService;

    @Test
    void testGetFeedList_Online() {
        HomeFeedQueryDTO query = new HomeFeedQueryDTO();
        query.setType("online");
        query.setPageNum(1);
        query.setPageSize(10);

        HomeFeedResultVO result = homeFeedService.getFeedList(query, null);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals("online", result.getType());
    }

    @Test
    void testGetFeedList_Offline() {
        HomeFeedQueryDTO query = new HomeFeedQueryDTO();
        query.setType("offline");
        query.setPageNum(1);
        query.setPageSize(10);

        HomeFeedResultVO result = homeFeedService.getFeedList(query, null);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals("offline", result.getType());
    }
}
```

### 4.2 集成测试

使用已有的测试文件:
- `AppHomeFeedTest.java`

---

## 📊 已集成功能总结

### 筛选功能 (✅ 已集成)

| 接口 | RPC 方法 | 状态 |
|------|----------|------|
| `GET /api/filter/config` | `getFilterConfig()` | ✅ |
| `POST /api/filter/apply` | `queryFilteredUsers()` | ✅ |

### 限时专享 (✅ 已集成)

| 接口 | RPC 方法 | 状态 |
|------|----------|------|
| `GET /api/limited/list` | `queryLimitedTimeUsers()` | ✅ |

### 首页Feed流 (⏳ 待完善)

| 接口 | RPC 方法 | 状态 |
|------|----------|------|
| `GET /api/home/feed` | `queryFeedUsers()` | ⏳ 本文档定义 |

---

## 🔮 未来优化方向

### 1. 推荐算法

```java
/**
 * 推荐因子:
 * - 在线状态 (权重: 30%)
 * - 距离远近 (权重: 25%)
 * - 活跃度 (权重: 20%)
 * - 技能匹配度 (权重: 15%)
 * - 好评率 (权重: 10%)
 */
public interface RecommendationService {
    List<Long> getRecommendedUserIds(Long userId, String type, int limit);
}
```

### 2. 缓存策略

```java
// Redis 缓存方案
@Cacheable(value = "feed:users", key = "#type + ':' + #cityCode + ':' + #pageNum")
public FeedPageResult getCachedFeedUsers(String type, String cityCode, Integer pageNum);

// 缓存过期: 5分钟
// 主动刷新: 用户上线/下线时
```

### 3. 在线状态服务

```java
// RemoteChatService (xypai-chat)
public interface RemoteChatService {
    /**
     * 批量获取用户在线状态
     */
    Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds);
}
```

---

## ⚠️ 注意事项

1. **性能优化**: Feed流是高频接口，需要做好缓存
2. **分页策略**: 使用游标分页替代偏移分页，防止数据漂移
3. **距离计算**: 用户量大时考虑使用 PostGIS 或 Redis GEO
4. **推荐算法**: 初期使用简单规则，后期可接入机器学习
5. **关注状态**: 批量查询时需要限制数量，防止大量数据库访问

---

## 📅 预计工时

| 任务 | 工时 | 负责人 |
|------|------|--------|
| RPC 接口定义 (DTO/VO) | 0.5 天 | |
| 领域服务实现 | 1 天 | |
| BFF 层重构 | 0.5 天 | |
| 测试验证 | 0.5 天 | |
| **总计** | **2.5 天** | |

### 可选扩展

| 任务 | 工时 | 说明 |
|------|------|------|
| 推荐算法实现 | 2 天 | 基于规则的推荐 |
| Redis 缓存层 | 1 天 | 提升性能 |
| 在线状态服务集成 | 1 天 | 实时在线状态 |

---

## 🔗 相关文档

- [集成文档-01-技能服务模块](./集成文档-01-技能服务模块.md)
- [集成文档-02-组局中心模块](./集成文档-02-组局中心模块.md)
- [集成文档-03-搜索功能模块](./集成文档-03-搜索功能模块.md)
- [BFF 快速理解](../快速理解.md)
- [RPC 集成计划](../RPC集成计划.md)
- [前端文档: 01-首页Feed流.md](前端文档链接)
- [前端文档: 02-筛选功能.md](前端文档链接)
- [前端文档: 05-限时专享.md](前端文档链接)

---

**文档版本**: v1.0.0
**最后更新**: 2025-11-26
