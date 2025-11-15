# XiangYuPai User Service - Complete Implementation Guide

> **Version**: 1.0.0
> **Date**: 2025-11-14
> **Module**: xypai-user (User Microservice)
> **Status**: Implementation Complete - Ready for Coding

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Module Structure](#module-structure)
3. [Database Schema](#database-schema)
4. [Entity Layer](#entity-layer)
5. [DTO & VO Layer](#dto--vo-layer)
6. [Mapper Layer](#mapper-layer)
7. [Service Layer](#service-layer)
8. [Controller Layer](#controller-layer)
9. [RPC Layer](#rpc-layer)
10. [Configuration](#configuration)
11. [Main Application](#main-application)
12. [Testing](#testing)
13. [Deployment](#deployment)

---

## 1. Project Overview

### 1.1 Module Information

| Property | Value |
|----------|-------|
| **Artifact ID** | `xypai-user` |
| **Package** | `org.dromara.user` |
| **Port** | `9401` |
| **Database** | `xypai_user` |
| **Service Name** | `xypai-user` |

### 1.2 Technology Stack

- **Spring Boot**: 3.2.0
- **Spring Cloud Gateway**: WebFlux
- **Nacos**: 2.x (Service Discovery + Config)
- **Dubbo**: 3.x (RPC)
- **MyBatis Plus**: 3.5.7 (ORM)
- **Redis**: 7.0+ (Cache)
- **MySQL**: 8.0+ (Database)
- **Sa-Token**: Latest (Auth)

### 1.3 Core Features

1. **User Profile Management** - 用户资料管理
   - View profile (personal/others)
   - Edit profile (11 fields with real-time save)
   - Avatar upload

2. **Social Relations** - 社交关系
   - Follow/Unfollow users
   - Fans list with search
   - Following list with search
   - Block/Report users

3. **Skills Management** - 技能管理
   - Create skills (online/offline)
   - Edit/Delete skills
   - Toggle online/offline status
   - Skill images and promises

4. **RPC Services** - RPC接口
   - `createUser()` - Create user (called by auth service)
   - `getUserByPhone()` - Get user by phone
   - `getUserInfo()` - Get user info

---

## 2. Module Structure

### 2.1 Directory Tree

```
xypai-user/
├── pom.xml                                  ✅ Created
├── sql/
│   └── xypai_user.sql                      ✅ Created
├── src/
│   ├── main/
│   │   ├── java/org/dromara/user/
│   │   │   ├── XyPaiUserApplication.java   ⏳ Pending
│   │   │   ├── config/
│   │   │   │   ├── MyBatisPlusConfig.java  ⏳ Pending
│   │   │   │   └── RedisConfig.java        ⏳ Pending
│   │   │   ├── controller/
│   │   │   │   ├── app/                    ⏳ Pending
│   │   │   │   │   ├── ProfileController.java
│   │   │   │   │   ├── RelationController.java
│   │   │   │   │   └── SkillController.java
│   │   │   │   └── feign/
│   │   │   │       └── RemoteUserController.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/                 ⏳ Partial (4/9)
│   │   │   │   │   ├── User.java           ✅ Created
│   │   │   │   │   ├── UserStats.java      ✅ Created
│   │   │   │   │   ├── UserRelation.java   ✅ Created
│   │   │   │   │   ├── Skill.java          ✅ Created
│   │   │   │   │   ├── UserBlacklist.java  ⏳ Pending
│   │   │   │   │   ├── UserReport.java     ⏳ Pending
│   │   │   │   │   ├── SkillImage.java     ⏳ Pending
│   │   │   │   │   ├── SkillPromise.java   ⏳ Pending
│   │   │   │   │   └── SkillAvailableTime.java ⏳ Pending
│   │   │   │   ├── dto/                    ⏳ Pending
│   │   │   │   │   ├── UserUpdateDto.java
│   │   │   │   │   ├── SkillCreateDto.java
│   │   │   │   │   └── ...
│   │   │   │   └── vo/                     ⏳ Pending
│   │   │   │       ├── UserProfileVo.java
│   │   │   │       ├── SkillDetailVo.java
│   │   │   │       └── ...
│   │   │   ├── mapper/                     ⏳ Pending
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── UserStatsMapper.java
│   │   │   │   ├── UserRelationMapper.java
│   │   │   │   └── SkillMapper.java
│   │   │   └── service/                    ⏳ Pending
│   │   │       ├── IUserService.java
│   │   │       ├── IRelationService.java
│   │   │       ├── ISkillService.java
│   │   │       └── impl/
│   │   │           ├── UserServiceImpl.java
│   │   │           ├── RelationServiceImpl.java
│   │   │           └── SkillServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml              ⏳ Pending
│   │       ├── application-dev.yml          ⏳ Pending
│   │       ├── bootstrap.yml                ⏳ Pending
│   │       └── logback-spring.xml           ⏳ Pending
│   └── test/
│       └── java/                            ⏳ Pending
└── README.md                                ⏳ Pending
```

---

## 3. Database Schema

### 3.1 Tables Overview

| Table | Chinese Name | Records | Status |
|-------|-------------|---------|--------|
| `users` | 用户基本信息表 | User profiles | ✅ Created |
| `user_stats` | 用户统计表 | Follower/Fans counts | ✅ Created |
| `user_relations` | 用户关系表 | Follow relationships | ✅ Created |
| `user_blacklist` | 用户黑名单表 | Blocked users | ✅ Created |
| `user_reports` | 用户举报表 | User reports | ✅ Created |
| `skills` | 技能表 | Skills (online/offline) | ✅ Created |
| `skill_images` | 技能展示图片表 | Skill display images | ✅ Created |
| `skill_promises` | 技能服务承诺表 | Skill promises | ✅ Created |
| `skill_available_times` | 技能可用时间表 | Available times | ✅ Created |

### 3.2 Key Indexes

**Spatial Indexes** (for nearby queries):
- `users.idx_location` - Find nearby users
- `skills.idx_location` - Find nearby skills

**Unique Indexes** (prevent duplicates):
- `user_stats.uk_user_id` - One stats record per user
- `user_relations.uk_follower_following` - Prevent duplicate follows
- `user_blacklist.uk_user_blocked` - Prevent duplicate blocks

### 3.3 Import Schema

```bash
mysql -u root -p < sql/xypai_user.sql
```

---

## 4. Entity Layer

### 4.1 Entities Created

✅ **User.java** - User basic information
✅ **UserStats.java** - User statistics
✅ **UserRelation.java** - Follow relationships
✅ **Skill.java** - Skills

### 4.2 Remaining Entities to Create

Create these files in `src/main/java/org/dromara/user/domain/entity/`:

#### 4.2.1 UserBlacklist.java

```java
package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_blacklist")
public class UserBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "blacklist_id", type = IdType.AUTO)
    private Long blacklistId;

    @TableField("user_id")
    private Long userId;

    @TableField("blocked_user_id")
    private Long blockedUserId;

    @TableField("reason")
    private String reason;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    @Version
    @TableField("version")
    private Integer version;
}
```

#### 4.2.2 UserReport.java

```java
package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_reports")
public class UserReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("reported_user_id")
    private Long reportedUserId;

    @TableField("reason")
    private String reason;

    @TableField("description")
    private String description;

    @TableField("evidence")
    private String evidence;

    @TableField("status")
    private String status;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("review_result")
    private String reviewResult;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    @Version
    @TableField("version")
    private Integer version;
}
```

#### 4.2.3 SkillImage.java, SkillPromise.java, SkillAvailableTime.java

[Similar pattern - follow User.java structure]

---

## 5. DTO & VO Layer

### 5.1 DTO (Request Objects)

Create these files in `src/main/java/org/dromara/user/domain/dto/`:

#### Example: UserUpdateDto.java

```java
package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Schema(description = "Update user profile request")
public class UserUpdateDto {

    @Schema(description = "Nickname (2-20 characters)")
    @Size(min = 2, max = 20, message = "昵称长度为2-20字符")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Gender: male, female, other")
    private String gender;

    @Schema(description = "Birthday")
    private LocalDate birthday;

    @Schema(description = "Residence")
    @Size(max = 200, message = "居住地不能超过200字符")
    private String residence;

    @Schema(description = "Height (cm, 100-250)")
    @Min(value = 100, message = "身高范围为100-250cm")
    @Max(value = 250, message = "身高范围为100-250cm")
    private Integer height;

    @Schema(description = "Weight (kg, 30-200)")
    @Min(value = 30, message = "体重范围为30-200kg")
    @Max(value = 200, message = "体重范围为30-200kg")
    private Integer weight;

    @Schema(description = "Occupation")
    @Size(max = 100, message = "职业不能超过100字符")
    private String occupation;

    @Schema(description = "WeChat ID (6-20 characters)")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,20}$", message = "微信号格式不正确")
    private String wechat;

    @Schema(description = "Bio (0-200 characters)")
    @Size(max = 200, message = "个性签名不能超过200字符")
    private String bio;
}
```

### 5.2 VO (Response Objects)

Create these files in `src/main/java/org/dromara/user/domain/vo/`:

#### Example: UserProfileVo.java

```java
package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response")
public class UserProfileVo {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Gender")
    private String gender;

    @Schema(description = "Birthday")
    private LocalDate birthday;

    @Schema(description = "Residence")
    private String residence;

    @Schema(description = "Height (cm)")
    private Integer height;

    @Schema(description = "Weight (kg)")
    private Integer weight;

    @Schema(description = "Occupation")
    private String occupation;

    @Schema(description = "WeChat ID")
    private String wechat;

    @Schema(description = "Bio")
    private String bio;

    @Schema(description = "Is online")
    private Boolean isOnline;

    @Schema(description = "User statistics")
    private UserStatsVo stats;

    @Schema(description = "Follow status")
    private String followStatus;
}
```

---

## 6. Mapper Layer

Create these files in `src/main/java/org/dromara/user/mapper/`:

### Example: UserMapper.java

```java
package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.User;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * Find nearby users by location
     *
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusMeters Search radius in meters
     * @param limit Max results
     * @return List of nearby users
     */
    @Select("""
        SELECT *,
          ST_Distance_Sphere(
            location,
            ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)
          ) / 1000 AS distance_km
        FROM users
        WHERE ST_Distance_Sphere(
            location,
            ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)
          ) <= #{radiusMeters}
          AND deleted = 0
        ORDER BY distance_km
        LIMIT #{limit}
        """)
    List<User> findNearbyUsers(
        @Param("latitude") BigDecimal latitude,
        @Param("longitude") BigDecimal longitude,
        @Param("radiusMeters") int radiusMeters,
        @Param("limit") int limit
    );
}
```

---

## 7. Service Layer

### Example: IUserService.java

```java
package org.dromara.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.dto.UserUpdateDto;
import org.dromara.user.domain.vo.UserProfileVo;

public interface IUserService extends IService<User> {

    /**
     * Get user profile
     */
    R<UserProfileVo> getUserProfile(Long userId);

    /**
     * Update user profile
     */
    R<Void> updateUserProfile(Long userId, UserUpdateDto dto);

    /**
     * Upload avatar
     */
    R<String> uploadAvatar(Long userId, MultipartFile file);
}
```

### Example: UserServiceImpl.java

```java
package org.dromara.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.dto.UserUpdateDto;
import org.dromara.user.domain.vo.UserProfileVo;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY_PREFIX = "user:profile:";

    @Override
    public R<UserProfileVo> getUserProfile(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // Try cache first
        UserProfileVo cached = redisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return R.ok(cached);
        }

        // Query database
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("User not found");
        }

        // Build VO
        UserProfileVo vo = UserProfileVo.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            // ... other fields
            .build();

        // Cache result
        redisUtils.setCacheObject(cacheKey, vo, Duration.ofMinutes(30));

        return R.ok(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateUserProfile(Long userId, UserUpdateDto dto) {
        User user = User.builder()
            .userId(userId)
            .nickname(dto.getNickname())
            .avatar(dto.getAvatar())
            // ... other fields
            .build();

        userMapper.updateById(user);

        // Invalidate cache
        redisUtils.deleteObject(CACHE_KEY_PREFIX + userId);

        return R.ok();
    }
}
```

---

## 8. Controller Layer

### Example: ProfileController.java

```java
package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.UserUpdateDto;
import org.dromara.user.domain.vo.UserProfileVo;
import org.dromara.user.service.IUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User Profile API", description = "用户资料接口")
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserService userService;

    @Operation(summary = "Get user profile header")
    @GetMapping("/header")
    public R<UserProfileVo> getProfileHeader() {
        Long userId = LoginHelper.getUserId();
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Get profile edit data")
    @GetMapping("/edit")
    public R<UserProfileVo> getProfileEdit() {
        Long userId = LoginHelper.getUserId();
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Update nickname")
    @PutMapping("/nickname")
    public R<Void> updateNickname(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Upload avatar")
    @PostMapping("/avatar/upload")
    public R<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Long userId = LoginHelper.getUserId();
        return userService.uploadAvatar(userId, file);
    }

    // ... other endpoints
}
```

---

## 9. RPC Layer

### 9.1 Create API Module

Create `ruoyi-api/ruoyi-api-user` module:

#### RemoteUserService.java

```java
package org.dromara.user.api;

import org.dromara.common.core.domain.R;
import org.dromara.user.api.domain.vo.RemoteUserVo;
import org.dromara.user.api.domain.dto.CreateUserDto;

public interface RemoteUserService {

    /**
     * Create user (called by auth service during registration)
     */
    R<Long> createUser(CreateUserDto dto);

    /**
     * Get user by phone number
     */
    R<RemoteUserVo> getUserByPhone(String phone);

    /**
     * Get user info by user ID
     */
    R<RemoteUserVo> getUserInfo(Long userId);
}
```

### 9.2 Implement RPC Provider

#### RemoteUserServiceImpl.java

```java
package org.dromara.user.controller.feign;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.domain.R;
import org.dromara.user.api.RemoteUserService;
import org.dromara.user.api.domain.dto.CreateUserDto;
import org.dromara.user.api.domain.vo.RemoteUserVo;
import org.dromara.user.service.IUserService;
import org.springframework.stereotype.Service;

@Service
@DubboService
@RequiredArgsConstructor
public class RemoteUserServiceImpl implements RemoteUserService {

    private final IUserService userService;

    @Override
    public R<Long> createUser(CreateUserDto dto) {
        return userService.createUser(dto);
    }

    @Override
    public R<RemoteUserVo> getUserByPhone(String phone) {
        return userService.getUserByPhone(phone);
    }

    @Override
    public R<RemoteUserVo> getUserInfo(Long userId) {
        return userService.getUserInfo(userId);
    }
}
```

---

## 10. Configuration

### 10.1 application.yml

```yaml
spring:
  application:
    name: xypai-user

server:
  port: 9401

# Knife4j API Documentation
knife4j:
  enable: true
  production: false

# Logging
logging:
  level:
    org.dromara.user: debug
```

### 10.2 application-dev.yml

```yaml
# Development environment
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_user?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # Redis
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 10.3 bootstrap.yml

```yaml
spring:
  application:
    name: xypai-user

  cloud:
    nacos:
      # Service Discovery
      discovery:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: DEFAULT_GROUP

      # Dynamic Configuration
      config:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: DEFAULT_GROUP
        file-extension: yml
        shared-configs:
          - data-id: common-redis.yml
            group: DEFAULT_GROUP
            refresh: true
          - data-id: common-mysql.yml
            group: DEFAULT_GROUP
            refresh: true

# Dubbo
dubbo:
  application:
    name: ${spring.application.name}
  protocol:
    name: dubbo
    port: -1
  registry:
    address: nacos://${spring.cloud.nacos.discovery.server-addr}
```

---

## 11. Main Application

### XyPaiUserApplication.java

```java
package org.dromara.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class XyPaiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyPaiUserApplication.java, args);
        System.out.println("(♥◠‿◠)ノ゙  XiangYuPai User Service started successfully   ლ(´ڡ`ლ)゙");
    }
}
```

---

## 12. Testing

### Test User Creation

```bash
curl -X POST http://localhost:9401/api/user/profile/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "nickname": "TestUser",
    "avatar": "https://cdn.example.com/avatar.png"
  }'
```

---

## 13. Deployment

### Build

```bash
mvn clean package -DskipTests
```

### Run

```bash
java -jar target/xypai-user.jar --spring.profiles.active=dev
```

### Docker

```bash
docker build -t xypai-user:1.0.0 .
docker run -d -p 9401:9401 xypai-user:1.0.0
```

---

## 14. API Documentation

Access Knife4j API documentation:

**URL**: http://localhost:9401/doc.html

---

## 15. Summary

### Completed ✅

1. ✅ Module structure
2. ✅ pom.xml with dependencies
3. ✅ Database schema (9 tables)
4. ✅ Entity layer (4/9 entities)

### Remaining ⏳

1. ⏳ Complete remaining 5 entities
2. ⏳ Create 25+ DTO classes
3. ⏳ Create 20+ VO classes
4. ⏳ Create 9 Mapper interfaces
5. ⏳ Create 5+ Service interfaces and implementations
6. ⏳ Create 3+ Controllers
7. ⏳ Create RPC API module
8. ⏳ Create configuration files
9. ⏳ Create Application main class
10. ⏳ Write tests

### Next Steps

1. Complete all entity classes
2. Create all DTO/VO classes following frontend API contracts
3. Implement mapper layer with custom SQL
4. Implement service layer with business logic
5. Implement controllers with all REST endpoints
6. Create RPC API module and implement providers
7. Create configuration files
8. Test all endpoints
9. Deploy to test environment

---

**Document Version**: 1.0.0
**Last Updated**: 2025-11-14
**Author**: XiangYuPai Team
**Status**: Ready for Implementation
