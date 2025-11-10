/**
 * xypai-user 微服务示例代码
 * 
 * 这个文件展示了如何在业务微服务中使用 LoginHelper 获取当前用户信息
 * 
 * ⭐ 关键点：
 * 1. 不需要改 ruoyi-auth
 * 2. Token 由 ruoyi-auth 生成
 * 3. 所有微服务通过 LoginHelper.getUserId() 获取当前用户
 * 4. 业务数据存储在各自微服务的数据库中
 * 
 * 文件位置: xypai-modules/xypai-user/src/main/java/com/xypai/user/controller/UserProfileController.java
 */

package com.xypai.user.controller;

import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xypai.user.domain.vo.UserProfileVo;
import com.xypai.user.domain.dto.UserProfileDto;
import com.xypai.user.domain.vo.HealthRecordVo;
import com.xypai.user.domain.dto.HealthRecordDto;
import com.xypai.user.service.IUserProfileService;
import com.xypai.user.service.IHealthRecordService;

import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * APP 用户个人资料 Controller
 * 
 * 路由配置（在 Gateway）:
 * - Path: /xypai-user/**
 * - Strip: 1
 * 
 * 实际访问路径:
 * - http://localhost:8080/xypai-user/api/v1/user/profile
 * 
 * @author xypai
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserProfileController extends BaseController {
    
    private final IUserProfileService userProfileService;
    private final IHealthRecordService healthRecordService;
    
    // ==========================================
    // 个人资料相关接口
    // ==========================================
    
    /**
     * 获取当前用户的个人资料
     * 
     * ⭐ 核心逻辑：
     * 1. 从 Token 自动获取 userId (LoginHelper.getUserId())
     * 2. 查询业务数据（user_profile 表）
     * 3. 用户只能访问自己的数据（安全）
     * 
     * 前端调用：
     * GET /xypai-user/api/v1/user/profile
     * Headers: {
     *   "Authorization": "Bearer token",
     *   "clientid": "app-xypai-client-id"
     * }
     * 
     * @return 用户资料
     */
    @GetMapping("/profile")
    public R<UserProfileVo> getProfile() {
        // ⭐⭐⭐ 关键：从 Token 自动获取当前用户ID
        // 不需要前端传递，更安全
        Long userId = LoginHelper.getUserId();
        
        // 查询业务数据
        UserProfileVo profile = userProfileService.getByUserId(userId);
        
        // 如果首次访问，可能需要创建默认资料
        if (profile == null) {
            profile = userProfileService.createDefaultProfile(userId);
        }
        
        return R.ok(profile);
    }
    
    /**
     * 更新个人资料
     * 
     * 前端调用：
     * PUT /xypai-user/api/v1/user/profile
     * Body: {
     *   "realName": "张三",
     *   "gender": "男",
     *   "birthday": "1990-01-01",
     *   "height": 175,
     *   "weight": 70
     * }
     * 
     * @param dto 资料更新DTO
     * @return 操作结果
     */
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody @Validated UserProfileDto dto) {
        // 自动获取用户ID
        Long userId = LoginHelper.getUserId();
        
        // 更新资料（只能更新自己的）
        userProfileService.updateByUserId(userId, dto);
        
        return R.ok();
    }
    
    /**
     * 获取完整用户信息（包含基础信息 + 扩展信息）
     * 
     * 这个接口展示了如何组合多个数据源：
     * 1. 从 Token 获取基础信息（LoginHelper）
     * 2. 从 ruoyi-system 获取系统信息（可选，通过Dubbo）
     * 3. 从本地数据库获取业务信息
     * 
     * @return 完整用户信息
     */
    @GetMapping("/complete-info")
    public R<CompleteUserInfoVo> getCompleteInfo() {
        // 从 Token 获取用户信息
        Long userId = LoginHelper.getUserId();
        String userName = LoginHelper.getUsername();
        
        // 查询业务数据
        UserProfileVo profile = userProfileService.getByUserId(userId);
        
        // 组合返回
        CompleteUserInfoVo vo = new CompleteUserInfoVo();
        vo.setUserId(userId);
        vo.setUserName(userName);        // 来自 Token
        vo.setRealName(profile.getRealName());     // 来自 user_profile
        vo.setGender(profile.getGender());         // 来自 user_profile
        vo.setBirthday(profile.getBirthday());     // 来自 user_profile
        
        return R.ok(vo);
    }
    
    // ==========================================
    // 健康数据相关接口
    // ==========================================
    
    /**
     * 添加健康记录
     * 
     * 前端调用：
     * POST /xypai-user/api/v1/user/health/record
     * Body: {
     *   "recordType": "blood_pressure",
     *   "systolic": 120,
     *   "diastolic": 80,
     *   "recordTime": "2025-11-10 10:30:00"
     * }
     * 
     * @param dto 健康记录DTO
     * @return 操作结果
     */
    @PostMapping("/health/record")
    public R<Void> addHealthRecord(@RequestBody @Validated HealthRecordDto dto) {
        // 自动获取用户ID
        Long userId = LoginHelper.getUserId();
        
        // 保存健康记录
        healthRecordService.addRecord(userId, dto);
        
        return R.ok();
    }
    
    /**
     * 获取健康记录列表
     * 
     * 前端调用：
     * GET /xypai-user/api/v1/user/health/records?recordType=blood_pressure&days=7
     * 
     * @param recordType 记录类型（可选）
     * @param days 最近几天（默认30天）
     * @return 健康记录列表
     */
    @GetMapping("/health/records")
    public R<List<HealthRecordVo>> getHealthRecords(
        @RequestParam(required = false) String recordType,
        @RequestParam(defaultValue = "30") Integer days
    ) {
        // 自动获取用户ID
        Long userId = LoginHelper.getUserId();
        
        // 查询健康记录
        List<HealthRecordVo> records = healthRecordService.getRecords(userId, recordType, days);
        
        return R.ok(records);
    }
    
    /**
     * 获取健康统计
     * 
     * 前端调用：
     * GET /xypai-user/api/v1/user/health/stats
     * 
     * @return 健康统计数据
     */
    @GetMapping("/health/stats")
    public R<HealthStatsVo> getHealthStats() {
        // 自动获取用户ID
        Long userId = LoginHelper.getUserId();
        
        // 计算统计数据
        HealthStatsVo stats = healthRecordService.calculateStats(userId);
        
        return R.ok(stats);
    }
    
    // ==========================================
    // 其他业务接口示例
    // ==========================================
    
    /**
     * 获取用户关注列表
     * 
     * 这是一个类似微信的社交功能示例
     */
    @GetMapping("/following")
    public R<List<UserSimpleVo>> getFollowing(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        
        List<UserSimpleVo> following = userRelationService.getFollowing(userId, pageNum, pageSize);
        
        return R.ok(following);
    }
    
    /**
     * 获取用户粉丝列表
     */
    @GetMapping("/followers")
    public R<List<UserSimpleVo>> getFollowers(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        Long userId = LoginHelper.getUserId();
        
        List<UserSimpleVo> followers = userRelationService.getFollowers(userId, pageNum, pageSize);
        
        return R.ok(followers);
    }
    
    /**
     * 关注用户
     */
    @PostMapping("/follow/{targetUserId}")
    public R<Void> followUser(@PathVariable Long targetUserId) {
        Long userId = LoginHelper.getUserId();
        
        // 不能关注自己
        if (userId.equals(targetUserId)) {
            return R.fail("不能关注自己");
        }
        
        userRelationService.follow(userId, targetUserId);
        
        return R.ok();
    }
}

// ==========================================
// VO/DTO 示例
// ==========================================

/**
 * 用户资料 VO
 */
@Data
class UserProfileVo {
    private Long userId;
    private String realName;
    private String gender;
    private LocalDate birthday;
    private Integer height;
    private Integer weight;
    private String bio;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

/**
 * 用户资料更新 DTO
 */
@Data
class UserProfileDto {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    private String gender;
    private LocalDate birthday;
    private Integer height;
    private Integer weight;
    private String bio;
}

/**
 * 完整用户信息 VO
 */
@Data
class CompleteUserInfoVo {
    // 来自 Token/sys_user
    private Long userId;
    private String userName;
    private String phoneNumber;
    
    // 来自 user_profile
    private String realName;
    private String gender;
    private LocalDate birthday;
    private Integer height;
    private Integer weight;
}

/**
 * 健康记录 VO
 */
@Data
class HealthRecordVo {
    private Long id;
    private Long userId;
    private String recordType;
    private Integer systolic;      // 收缩压
    private Integer diastolic;     // 舒张压
    private Integer heartRate;     // 心率
    private Double bloodSugar;     // 血糖
    private LocalDateTime recordTime;
    private LocalDateTime createTime;
}

/**
 * 健康记录 DTO
 */
@Data
class HealthRecordDto {
    @NotBlank(message = "记录类型不能为空")
    private String recordType;
    
    private Integer systolic;
    private Integer diastolic;
    private Integer heartRate;
    private Double bloodSugar;
    
    private LocalDateTime recordTime;
}

/**
 * 健康统计 VO
 */
@Data
class HealthStatsVo {
    private Integer totalRecords;           // 总记录数
    private Double avgSystolic;             // 平均收缩压
    private Double avgDiastolic;            // 平均舒张压
    private Double avgHeartRate;            // 平均心率
    private LocalDateTime lastRecordTime;   // 最后记录时间
}

// ==========================================
// 数据库表结构示例
// ==========================================

/*

-- 用户资料扩展表
CREATE TABLE `user_profile` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（关联sys_user.user_id）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `height` int(11) DEFAULT NULL COMMENT '身高(cm)',
  `weight` int(11) DEFAULT NULL COMMENT '体重(kg)',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料扩展表';

-- 健康记录表
CREATE TABLE `health_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `record_type` varchar(50) NOT NULL COMMENT '记录类型（blood_pressure, heart_rate等）',
  `systolic` int(11) DEFAULT NULL COMMENT '收缩压',
  `diastolic` int(11) DEFAULT NULL COMMENT '舒张压',
  `heart_rate` int(11) DEFAULT NULL COMMENT '心率',
  `blood_sugar` decimal(5,2) DEFAULT NULL COMMENT '血糖',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_type` (`record_type`),
  KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录表';

-- 用户统计表
CREATE TABLE `user_stats` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_count` int(11) DEFAULT 0 COMMENT '发帖数',
  `follow_count` int(11) DEFAULT 0 COMMENT '关注数',
  `fans_count` int(11) DEFAULT 0 COMMENT '粉丝数',
  `health_score` int(11) DEFAULT 0 COMMENT '健康评分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计表';

*/

// ==========================================
// 配置文件示例
// ==========================================

/*

# application.yml (xypai-user)

server:
  port: 9501

spring:
  application:
    name: xypai-user
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/xypai_user?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

# Dubbo 配置
dubbo:
  application:
    name: ${spring.application.name}
  protocol:
    name: dubbo
    port: -1
  registry:
    address: nacos://localhost:8848

# Sa-Token 配置（继承自 ruoyi-common-satoken）
sa-token:
  token-name: Authorization

*/

// ==========================================
// Gateway 路由配置
// ==========================================

/*

# ruoyi-gateway.yml (在 Nacos 中配置)

spring:
  cloud:
    gateway:
      routes:
        # xypai-user 路由
        - id: xypai-user
          uri: lb://xypai-user
          predicates:
            - Path=/xypai-user/**
          filters:
            - StripPrefix=1
            - CacheRequestFilter

*/

