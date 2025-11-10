# 🏗️ 首页Controller参考实现

> **为前端首页模块提供后端支持的Controller实现参考**
> 
> **创建时间**: 2025-10-22  
> **模块**: xypai-user

---

## 📋 **需要创建的文件清单**

### 1️⃣ **Controller层**（4个文件）

```
xypai-user/src/main/java/com/xypai/user/controller/app/
├── HomepageController.java           ← 首页功能控制器
└── HomepageUserController.java       ← 首页用户查询控制器
```

### 2️⃣ **Service层**（4个文件）

```
xypai-user/src/main/java/com/xypai/user/service/
├── IHomepageService.java
├── IHomepageUserService.java
└── impl/
    ├── HomepageServiceImpl.java
    └── HomepageUserServiceImpl.java
```

### 3️⃣ **VO层**（6个文件）

```
xypai-user/src/main/java/com/xypai/user/domain/vo/
├── HomepageConfigVO.java
├── HomepageDataVO.java
├── HomepageUserVO.java
├── ServiceItemVO.java
├── BannerVO.java
└── HomepageStatisticsVO.java
```

### 4️⃣ **DTO层**（1个文件）

```
xypai-user/src/main/java/com/xypai/user/domain/dto/
└── HomepageUserQueryDTO.java
```

---

## 📝 **文件1: HomepageController.java**

```java
package com.xypai.user.controller.app;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.user.domain.vo.*;
import com.xypai.user.service.IHomepageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页功能控制器
 *
 * @author xypai
 * @date 2025-10-22
 */
@Tag(name = "首页功能", description = "首页数据聚合和配置API")
@RestController
@RequestMapping("/api/v1/homepage")
@RequiredArgsConstructor
@Validated
public class HomepageController extends BaseController {

    private final IHomepageService homepageService;

    /**
     * 获取首页配置
     */
    @Operation(summary = "获取首页配置", description = "获取首页各区域的配置信息")
    @GetMapping("/config")
    public R<HomepageConfigVO> getConfig(
            @Parameter(description = "平台类型") 
            @RequestParam(required = false) String platform,
            @Parameter(description = "应用版本") 
            @RequestParam(required = false) String version) {
        HomepageConfigVO config = homepageService.getHomepageConfig(platform, version);
        return R.ok(config);
    }

    /**
     * 获取精选用户列表
     */
    @Operation(summary = "获取精选用户", description = "获取限时专享的优质用户列表")
    @GetMapping("/featured-users")
    @SaCheckPermission("homepage:user:query")
    public R<List<HomepageUserVO>> getFeaturedUsers(
            @Parameter(description = "数量限制") 
            @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "服务类型") 
            @RequestParam(required = false) String serviceType,
            @Parameter(description = "城市ID") 
            @RequestParam(required = false) Long cityId) {
        List<HomepageUserVO> users = homepageService.getFeaturedUsers(limit, serviceType, cityId);
        return R.ok(users);
    }

    /**
     * 获取服务配置列表
     */
    @Operation(summary = "获取服务配置", description = "获取功能网格的服务类型配置")
    @GetMapping("/services")
    public R<List<ServiceItemVO>> getServices() {
        List<ServiceItemVO> services = homepageService.getServiceItems();
        return R.ok(services);
    }

    /**
     * 获取横幅数据
     */
    @Operation(summary = "获取横幅数据", description = "获取游戏推广横幅配置")
    @GetMapping("/banner")
    public R<List<BannerVO>> getBanner() {
        List<BannerVO> banners = homepageService.getBannerData();
        return R.ok(banners);
    }

    /**
     * 获取首页统计数据
     */
    @Operation(summary = "获取首页统计", description = "获取首页展示的统计信息")
    @GetMapping("/statistics")
    public R<HomepageStatisticsVO> getStatistics() {
        HomepageStatisticsVO statistics = homepageService.getHomepageStatistics();
        return R.ok(statistics);
    }

    /**
     * 获取热门搜索关键词
     */
    @Operation(summary = "获取热门搜索", description = "获取热门搜索关键词列表")
    @GetMapping("/hot-keywords")
    public R<List<String>> getHotKeywords(
            @Parameter(description = "数量限制") 
            @RequestParam(defaultValue = "10") Integer limit) {
        List<String> keywords = homepageService.getHotKeywords(limit);
        return R.ok(keywords);
    }

    /**
     * 获取首页聚合数据（性能优化接口）
     */
    @Operation(summary = "获取首页数据", description = "一次性获取首页所有数据")
    @GetMapping("/data")
    @SaCheckPermission("homepage:data:query")
    public R<HomepageDataVO> getData(
            @Parameter(description = "城市ID") 
            @RequestParam(required = false) Long cityId,
            @Parameter(description = "是否包含统计") 
            @RequestParam(defaultValue = "true") Boolean includeStatistics) {
        HomepageDataVO data = homepageService.getHomepageData(cityId, includeStatistics);
        return R.ok(data);
    }
}
```

---

## 📝 **文件2: HomepageUserController.java**

```java
package com.xypai.user.controller.app;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.xypai.user.domain.dto.HomepageUserQueryDTO;
import com.xypai.user.domain.vo.HomepageUserVO;
import com.xypai.user.service.IHomepageUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页用户查询控制器
 *
 * @author xypai
 * @date 2025-10-22
 */
@Tag(name = "首页用户", description = "首页用户查询和筛选API")
@RestController
@RequestMapping("/api/v1/homepage/users")
@RequiredArgsConstructor
@Validated
public class HomepageUserController extends BaseController {

    private final IHomepageUserService homepageUserService;

    /**
     * 首页用户列表（集成筛选）
     */
    @Operation(summary = "首页用户列表", description = "支持多维度筛选的用户列表查询")
    @GetMapping("/list")
    @SaCheckPermission("homepage:user:list")
    public TableDataInfo<HomepageUserVO> list(
            @Validated HomepageUserQueryDTO query,
            PageQuery pageQuery) {
        // 获取当前用户ID（用于个性化推荐）
        Long currentUserId = null;
        try {
            if (StpUtil.isLogin()) {
                currentUserId = LoginHelper.getUserId();
            }
        } catch (Exception e) {
            // 未登录用户，使用游客模式
        }
        
        startPage();
        List<HomepageUserVO> list = homepageUserService.getHomepageUserList(
            query, currentUserId);
        return getDataTable(list);
    }

    /**
     * 附近用户（空间索引查询）
     */
    @Operation(summary = "附近用户", description = "基于地理位置的附近用户查询（使用v7.1空间索引）")
    @GetMapping("/nearby")
    @SaCheckPermission("homepage:user:query")
    public R<List<HomepageUserVO>> nearby(
            @Parameter(description = "经度", required = true) 
            @RequestParam Double longitude,
            @Parameter(description = "纬度", required = true) 
            @RequestParam Double latitude,
            @Parameter(description = "半径（米）") 
            @RequestParam(defaultValue = "5000") Integer radius,
            @Parameter(description = "数量限制") 
            @RequestParam(defaultValue = "20") Integer limit,
            @Parameter(description = "城市ID") 
            @RequestParam(required = false) Long cityId) {
        
        // SQL示例（使用v7.1的空间索引POINT类型）:
        // SELECT 
        //   up.*, us.*, 
        //   ST_Distance_Sphere(
        //     up.location,  -- POINT类型字段
        //     ST_GeomFromText('POINT(? ?)', 4326)
        //   ) AS distance
        // FROM user_profile up
        // LEFT JOIN user_stats us ON up.user_id = us.user_id
        // WHERE up.online_status IN (1, 2)  -- 在线或忙碌
        //   AND up.is_real_verified = 1      -- 实名认证
        //   AND (up.city_id = ? OR ? IS NULL) -- 城市筛选
        //   AND ST_Distance_Sphere(
        //     up.location,
        //     ST_GeomFromText('POINT(? ?)', 4326)
        //   ) <= ?                           -- 距离筛选
        // ORDER BY distance ASC
        // LIMIT ?
        
        List<HomepageUserVO> users = homepageUserService.getNearbyUsers(
            longitude, latitude, radius, limit, cityId);
        return R.ok(users);
    }

    /**
     * 推荐用户（个性化算法）
     */
    @Operation(summary = "推荐用户", description = "基于用户行为的个性化推荐")
    @GetMapping("/recommend")
    @SaCheckPermission("homepage:user:query")
    public R<List<HomepageUserVO>> recommend(
            @Parameter(description = "数量限制") 
            @RequestParam(defaultValue = "20") Integer limit,
            @Parameter(description = "城市ID") 
            @RequestParam(required = false) Long cityId) {
        
        // 获取当前用户ID
        Long currentUserId = null;
        try {
            if (StpUtil.isLogin()) {
                currentUserId = LoginHelper.getUserId();
            }
        } catch (Exception e) {
            // 游客模式，使用通用推荐
        }
        
        // 推荐算法：
        // 1. 从UserBehavior表分析用户行为（浏览、点击、搜索记录）
        // 2. 从UserPreference表获取用户偏好设置
        // 3. 从SearchHistory表分析搜索历史
        // 4. 协同过滤算法（相似用户喜欢的内容）
        // 5. 内容质量评分（活跃度、评分、完整度）
        
        List<HomepageUserVO> users = homepageUserService.getRecommendedUsers(
            currentUserId, limit, cityId);
        return R.ok(users);
    }

    /**
     * 最新用户
     */
    @Operation(summary = "最新用户", description = "最新注册的用户列表")
    @GetMapping("/latest")
    @SaCheckPermission("homepage:user:query")
    public R<List<HomepageUserVO>> latest(
            @Parameter(description = "数量限制") 
            @RequestParam(defaultValue = "20") Integer limit,
            @Parameter(description = "城市ID") 
            @RequestParam(required = false) Long cityId,
            @Parameter(description = "注册天数内") 
            @RequestParam(defaultValue = "30") Integer withinDays) {
        
        // SQL示例:
        // SELECT up.*, us.*
        // FROM user_profile up
        // LEFT JOIN user_stats us ON up.user_id = us.user_id
        // WHERE up.created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)
        //   AND (up.city_id = ? OR ? IS NULL)
        //   AND up.profile_completeness >= 60  -- 资料完整度
        // ORDER BY up.created_at DESC
        // LIMIT ?
        
        List<HomepageUserVO> users = homepageUserService.getLatestUsers(
            limit, cityId, withinDays);
        return R.ok(users);
    }
}
```

---

## 📝 **文件3: HomepageConfigVO.java**

```java
package com.xypai.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页配置VO
 *
 * @author xypai
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 顶部功能配置
     */
    private TopFunctionConfig topFunction;

    /**
     * 游戏横幅配置
     */
    private GameBannerConfig gameBanner;

    /**
     * 服务网格配置
     */
    private ServiceGridConfig serviceGrid;

    /**
     * 精选用户配置
     */
    private FeaturedUsersConfig featuredUsers;

    /**
     * 组局中心配置
     */
    private EventCenterConfig eventCenter;

    /**
     * 用户列表配置
     */
    private UserListConfig userList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopFunctionConfig implements Serializable {
        private Boolean enabled;
        private Boolean showLocation;
        private Boolean showSearch;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameBannerConfig implements Serializable {
        private Boolean enabled;
        private Boolean autoPlay;
        private Integer interval;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceGridConfig implements Serializable {
        private Boolean enabled;
        private Integer columns;
        private Integer rows;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturedUsersConfig implements Serializable {
        private Boolean enabled;
        private Integer maxCount;
        private Integer refreshInterval;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventCenterConfig implements Serializable {
        private Boolean enabled;
        private Boolean showPromo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserListConfig implements Serializable {
        private Boolean enabled;
        private Integer pageSize;
        private Boolean infiniteScroll;
    }
}
```

---

## 📝 **文件4: HomepageUserVO.java**

```java
package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页用户VO（聚合数据）
 *
 * @author xypai
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================
    private Long userId;
    private String nickname;
    private String avatar;
    private String avatarThumbnail;
    private Integer age;
    private Integer gender;
    private String genderDesc;
    private String bio;
    
    // ==================== 位置信息 ====================
    private String location;
    private Long cityId;
    private String cityName;
    private Double distance;          // 距离（km，前端计算或后端计算）
    private String distanceDesc;      // 格式化距离 "2.3km"
    
    // ==================== 认证标识 ====================
    private Boolean isRealVerified;
    private Boolean isGodVerified;
    private Boolean isVip;
    private Integer vipLevel;
    private Boolean isPopular;
    private Boolean isActivityExpert;
    
    // ==================== 在线状态 ====================
    private Integer onlineStatus;
    private String onlineStatusDesc;
    private Boolean isOnline;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;
    
    // ==================== 职业标签 ====================
    private List<String> occupationTags;
    
    // ==================== 统计数据 ====================
    private Integer followerCount;
    private Integer contentCount;
    private Integer totalLikeCount;
    
    // ==================== 服务信息 ====================
    private BigDecimal pricePerHour;     // 每小时价格
    private BigDecimal pricePerGame;     // 每局价格
    private BigDecimal avgRating;        // 平均评分
    private Integer reviewCount;         // 评价数量
    
    // ==================== 用户照片 ====================
    private List<String> photoUrls;      // 用户上传的照片（最新3张）
    
    // ==================== 关系状态 ====================
    private Boolean isFollowed;          // 当前用户是否已关注
    private Boolean isFavorite;          // 当前用户是否已收藏
    
    // ==================== 系统字段 ====================
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
```

---

## 📝 **文件5: HomepageServiceImpl.java（核心逻辑）**

```java
package com.xypai.user.service.impl;

import com.xypai.user.domain.vo.*;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.mapper.UserStatsMapper;
import com.xypai.user.service.IHomepageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页服务实现
 *
 * @author xypai
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomepageServiceImpl implements IHomepageService {

    private final UserProfileMapper userProfileMapper;
    private final UserStatsMapper userStatsMapper;

    @Override
    public HomepageConfigVO getHomepageConfig(String platform, String version) {
        // 从system_config表或缓存获取配置
        // 支持按平台和版本返回不同配置
        
        return HomepageConfigVO.builder()
            .topFunction(HomepageConfigVO.TopFunctionConfig.builder()
                .enabled(true)
                .showLocation(true)
                .showSearch(true)
                .build())
            .gameBanner(HomepageConfigVO.GameBannerConfig.builder()
                .enabled(true)
                .autoPlay(true)
                .interval(5000)
                .build())
            .serviceGrid(HomepageConfigVO.ServiceGridConfig.builder()
                .enabled(true)
                .columns(5)
                .rows(2)
                .build())
            .featuredUsers(HomepageConfigVO.FeaturedUsersConfig.builder()
                .enabled(true)
                .maxCount(10)
                .refreshInterval(30000)
                .build())
            .eventCenter(HomepageConfigVO.EventCenterConfig.builder()
                .enabled(true)
                .showPromo(true)
                .build())
            .userList(HomepageConfigVO.UserListConfig.builder()
                .enabled(true)
                .pageSize(20)
                .infiniteScroll(true)
                .build())
            .build();
    }

    @Override
    public List<HomepageUserVO> getFeaturedUsers(Integer limit, String serviceType, Long cityId) {
        // SQL示例:
        // SELECT 
        //   up.*,
        //   us.*,
        //   GROUP_CONCAT(uo.name ORDER BY uo.sort_order) AS occupation_tags
        // FROM user_profile up
        // LEFT JOIN user_stats us ON up.user_id = us.user_id
        // LEFT JOIN user_occupation uo ON up.user_id = uo.user_id
        // WHERE up.is_real_verified = 1           -- 实名认证
        //   AND up.profile_completeness >= 80     -- 资料完整度
        //   AND (up.is_vip = 1 OR us.is_popular = 1)  -- VIP或人气用户
        //   AND up.last_online_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)  -- 24小时内活跃
        //   AND (up.city_id = ? OR ? IS NULL)     -- 城市筛选
        //   AND up.online_status IN (1, 2)        -- 在线或忙碌
        // GROUP BY up.user_id
        // ORDER BY 
        //   up.is_vip DESC,                       -- VIP优先
        //   up.vip_level DESC,                    -- VIP等级
        //   us.activity_organizer_score DESC,     -- 信誉评分
        //   up.profile_completeness DESC          -- 资料完整度
        // LIMIT ?
        
        // 临时实现：返回空列表，前端会使用降级方案
        log.warn("[HomepageService] getFeaturedUsers未完整实现，返回空列表");
        return new ArrayList<>();
    }

    @Override
    public List<ServiceItemVO> getServiceItems() {
        // 从配置表或硬编码返回10个服务配置
        List<ServiceItemVO> services = new ArrayList<>();
        
        services.add(createServiceItem("1", "王者荣耀", "honor_of_kings", 1, "#FFD700"));
        services.add(createServiceItem("2", "英雄联盟", "league_of_legends", 2, "#4A90E2"));
        services.add(createServiceItem("3", "和平精英", "peace_elite", 3, "#FF8C00"));
        services.add(createServiceItem("4", "荒野乱斗", "brawl_stars", 4, "#8B5CF6"));
        services.add(createServiceItem("5", "探店", "explore_shop", 5, "#32CD32"));
        services.add(createServiceItem("6", "私影", "private_cinema", 6, "#FF4500"));
        services.add(createServiceItem("7", "台球", "billiards", 7, "#FF69B4"));
        services.add(createServiceItem("8", "K歌", "ktv", 8, "#FFD700"));
        services.add(createServiceItem("9", "喝酒", "drinking", 9, "#4A90E2"));
        services.add(createServiceItem("10", "按摩", "massage", 10, "#999999"));
        
        return services;
    }

    private ServiceItemVO createServiceItem(String id, String name, String type, 
                                           int sortOrder, String backgroundColor) {
        return ServiceItemVO.builder()
            .id(id)
            .name(name)
            .type(type)
            .enabled(true)
            .sortOrder(sortOrder)
            .displayName(name)
            .description(name + "服务")
            .backgroundColor(backgroundColor)
            .build();
    }

    @Override
    public List<BannerVO> getBannerData() {
        // 从数据库或配置获取横幅数据
        // 临时返回空列表，前端会使用默认横幅
        return new ArrayList<>();
    }

    @Override
    public HomepageStatisticsVO getHomepageStatistics() {
        // 从Redis或数据库获取实时统计
        // SELECT COUNT(*) FROM user WHERE status = 1
        // SELECT COUNT(*) FROM user_profile WHERE online_status = 1
        // SELECT COUNT(*) FROM service_order
        
        return HomepageStatisticsVO.builder()
            .totalUsers(10000L)
            .onlineUsers(1500L)
            .totalOrders(50000L)
            .totalServices(10)
            .averageRating(BigDecimal.valueOf(4.7))
            .build();
    }

    @Override
    public List<String> getHotKeywords(Integer limit) {
        // 从hot_search表查询热门关键词
        // SELECT keyword 
        // FROM hot_search 
        // WHERE status = 1 
        // ORDER BY heat_score DESC 
        // LIMIT ?
        
        List<String> keywords = new ArrayList<>();
        keywords.add("王者荣耀");
        keywords.add("英雄联盟");
        keywords.add("探店");
        keywords.add("K歌");
        keywords.add("私影");
        return keywords.subList(0, Math.min(limit, keywords.size()));
    }

    @Override
    public HomepageDataVO getHomepageData(Long cityId, Boolean includeStatistics) {
        // 聚合查询，一次性返回所有首页数据
        return HomepageDataVO.builder()
            .featuredUsers(getFeaturedUsers(10, null, cityId))
            .serviceItems(getServiceItems())
            .banner(getBannerData().isEmpty() ? null : getBannerData().get(0))
            .statistics(includeStatistics ? getHomepageStatistics() : null)
            .build();
    }
}
```

---

## 📝 **文件6: HomepageUserServiceImpl.java（核心查询逻辑）**

```java
package com.xypai.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xypai.user.domain.dto.HomepageUserQueryDTO;
import com.xypai.user.domain.entity.UserProfile;
import com.xypai.user.domain.entity.UserStats;
import com.xypai.user.domain.vo.HomepageUserVO;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.mapper.UserStatsMapper;
import com.xypai.user.mapper.UserOccupationMapper;
import com.xypai.user.service.IHomepageUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页用户服务实现
 *
 * @author xypai
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomepageUserServiceImpl implements IHomepageUserService {

    private final UserProfileMapper userProfileMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserOccupationMapper userOccupationMapper;

    @Override
    public List<HomepageUserVO> getHomepageUserList(
            HomepageUserQueryDTO query, Long currentUserId) {
        
        // 构建查询条件
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        
        // 基础筛选
        wrapper.eq(UserProfile::getDeletedAt, null);  // 未删除
        
        // 城市筛选
        if (query.getCityId() != null) {
            wrapper.eq(UserProfile::getCityId, query.getCityId());
        }
        
        // 性别筛选
        if (query.getGender() != null && query.getGender() > 0) {
            wrapper.eq(UserProfile::getGender, query.getGender());
        }
        
        // 年龄筛选
        if (query.getAgeMin() != null) {
            wrapper.ge(UserProfile::getAge, query.getAgeMin());
        }
        if (query.getAgeMax() != null) {
            wrapper.le(UserProfile::getAge, query.getAgeMax());
        }
        
        // 认证筛选
        if (query.getIsVerified() != null && query.getIsVerified()) {
            wrapper.eq(UserProfile::getIsRealVerified, true);
        }
        
        // VIP筛选
        if (query.getIsVip() != null && query.getIsVip()) {
            wrapper.eq(UserProfile::getIsVip, true);
        }
        
        // 在线状态筛选
        if (query.getOnlineStatus() != null && !query.getOnlineStatus().isEmpty()) {
            wrapper.in(UserProfile::getOnlineStatus, query.getOnlineStatus());
        }
        
        // 排序
        if ("newest".equals(query.getSortBy())) {
            wrapper.orderByDesc(UserProfile::getCreatedAt);
        } else if ("popular".equals(query.getSortBy())) {
            // 需要关联user_stats表排序
            wrapper.orderByDesc(UserProfile::getIsPopular);
        } else {
            // 默认按资料完整度排序
            wrapper.orderByDesc(UserProfile::getProfileCompleteness);
        }
        
        // 查询用户列表
        List<UserProfile> profiles = userProfileMapper.selectList(wrapper);
        
        // 转换为VO并聚合数据
        return profiles.stream()
            .map(profile -> buildHomepageUserVO(profile, currentUserId))
            .collect(Collectors.toList());
    }

    @Override
    public List<HomepageUserVO> getNearbyUsers(
            Double longitude, Double latitude, Integer radius, 
            Integer limit, Long cityId) {
        
        // 使用v7.1的空间索引查询
        // 调用Mapper的自定义SQL方法
        // List<UserProfile> profiles = userProfileMapper.selectNearbyUsers(
        //     longitude, latitude, radius, limit, cityId);
        
        // 临时实现：返回空列表
        log.warn("[HomepageUserService] getNearbyUsers需要实现空间索引查询");
        return new ArrayList<>();
    }

    @Override
    public List<HomepageUserVO> getRecommendedUsers(
            Long currentUserId, Integer limit, Long cityId) {
        
        // 推荐算法实现：
        // 1. 如果用户已登录，基于UserBehavior和UserPreference推荐
        // 2. 如果用户未登录，推荐热门用户
        
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getDeletedAt, null);
        wrapper.eq(UserProfile::getIsRealVerified, true);
        wrapper.in(UserProfile::getOnlineStatus, List.of(1, 2));  // 在线或忙碌
        
        if (cityId != null) {
            wrapper.eq(UserProfile::getCityId, cityId);
        }
        
        // 优先推荐VIP和人气用户
        wrapper.orderByDesc(UserProfile::getIsVip);
        wrapper.orderByDesc(UserProfile::getIsPopular);
        wrapper.last("LIMIT " + limit);
        
        List<UserProfile> profiles = userProfileMapper.selectList(wrapper);
        
        return profiles.stream()
            .map(profile -> buildHomepageUserVO(profile, currentUserId))
            .collect(Collectors.toList());
    }

    @Override
    public List<HomepageUserVO> getLatestUsers(
            Integer limit, Long cityId, Integer withinDays) {
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(withinDays);
        
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getDeletedAt, null);
        wrapper.ge(UserProfile::getCreatedAt, cutoffDate);
        wrapper.ge(UserProfile::getProfileCompleteness, 60);  // 资料完整度
        
        if (cityId != null) {
            wrapper.eq(UserProfile::getCityId, cityId);
        }
        
        wrapper.orderByDesc(UserProfile::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        
        List<UserProfile> profiles = userProfileMapper.selectList(wrapper);
        
        return profiles.stream()
            .map(profile -> buildHomepageUserVO(profile, null))
            .collect(Collectors.toList());
    }

    /**
     * 构建首页用户VO（聚合多表数据）
     */
    private HomepageUserVO buildHomepageUserVO(UserProfile profile, Long currentUserId) {
        HomepageUserVO vo = new HomepageUserVO();
        
        // 复制基础字段
        BeanUtil.copyProperties(profile, vo);
        
        // 查询统计数据
        UserStats stats = userStatsMapper.selectByUserId(profile.getUserId());
        if (stats != null) {
            vo.setFollowerCount(stats.getFollowerCount());
            vo.setContentCount(stats.getContentCount());
            vo.setTotalLikeCount(stats.getTotalLikeCount());
        }
        
        // 查询职业标签
        List<String> occupations = userOccupationMapper.selectOccupationNamesByUserId(
            profile.getUserId());
        vo.setOccupationTags(occupations);
        
        // TODO: 查询用户照片（关联content表，type=1，最新3张）
        // vo.setPhotoUrls(photoUrls);
        
        // TODO: 查询服务价格（关联game_service或life_service表）
        // vo.setPricePerHour(pricePerHour);
        
        // TODO: 查询关系状态（如果currentUserId不为null）
        // vo.setIsFollowed(isFollowed);
        
        // 格式化距离显示
        if (vo.getDistance() != null) {
            vo.setDistanceDesc(formatDistance(vo.getDistance()));
        }
        
        return vo;
    }

    private String formatDistance(Double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0fm", distanceKm * 1000);
        }
        return String.format("%.1fkm", distanceKm);
    }
}
```

---

## 📝 **Mapper层SQL示例**

### UserProfileMapper.xml（空间索引查询）

```xml
<!-- 附近用户查询（使用v7.1空间索引） -->
<select id="selectNearbyUsers" resultMap="HomepageUserResultMap">
    SELECT 
        up.user_id,
        up.nickname,
        up.avatar,
        up.avatar_thumbnail,
        up.age,
        up.gender,
        up.bio,
        up.location,
        up.city_id,
        up.online_status,
        up.is_real_verified,
        up.is_god_verified,
        up.is_vip,
        up.vip_level,
        up.is_popular,
        up.last_online_time,
        up.profile_completeness,
        us.follower_count,
        us.content_count,
        us.total_like_count,
        ST_Distance_Sphere(
            up.location,
            ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)
        ) / 1000 AS distance
    FROM user_profile up
    LEFT JOIN user_stats us ON up.user_id = us.user_id
    WHERE up.deleted_at IS NULL
      AND up.online_status IN (1, 2)
      AND up.is_real_verified = 1
      AND (up.city_id = #{cityId} OR #{cityId} IS NULL)
      AND ST_Distance_Sphere(
            up.location,
            ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)
          ) <= #{radius}
    ORDER BY distance ASC
    LIMIT #{limit}
</select>
```

---

## 🎯 **实施优先级**

### Phase 1: 核心功能（本周）

1. ✅ **HomepageController.getConfig()** - 返回硬编码配置
2. ✅ **HomepageController.getServices()** - 返回10个服务配置
3. 🔴 **HomepageController.getFeaturedUsers()** - 查询优质用户
4. 🔴 **HomepageUserController.list()** - 通用用户列表

### Phase 2: 高级功能（下周）

5. 🟡 **HomepageUserController.nearby()** - 空间索引查询
6. 🟡 **HomepageController.getBanner()** - 横幅数据
7. 🟡 **HomepageController.getStatistics()** - 统计数据

### Phase 3: 优化功能（后续）

8. 🟢 **HomepageUserController.recommend()** - 推荐算法
9. 🟢 **HomepageController.getHotKeywords()** - 热门搜索
10. 🟢 **性能优化** - 缓存、索引、SQL优化

---

## 📖 **参考文档**

- [PL.md](../../PL.md) - 完整数据库设计（v7.1）
- [UserProfileController.java](./src/main/java/com/xypai/user/controller/app/UserProfileController.java) - 现有Controller参考
- [前端集成方案](../../../XiangYuPai-RNExpoAPP/src/features/Homepage/BACKEND_INTEGRATION_PLAN.md)

---

**创建时间**: 2025-10-22  
**维护者**: 后端开发团队  
**状态**: 📝 参考文档，待实施


