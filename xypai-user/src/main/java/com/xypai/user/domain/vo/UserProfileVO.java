package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户资料VO（完整版42字段）
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    private Long userId;
    private String nickname;
    private String avatar;
    private String avatarThumbnail;
    private String backgroundImage;
    private Integer gender;
    private String genderDesc;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private Integer age;
    private String ageRange;

    // ==================== 位置信息 ====================

    private Long cityId;
    private String cityName;
    private String location;
    private String address;
    private String ipLocation;

    // ==================== 个人资料 ====================

    private String bio;
    private Integer height;
    private Integer weight;
    private Double bmi;
    private String bmiLevel;

    // ==================== 联系方式（脱敏） ====================

    private String realName;      // 脱敏
    private String wechat;         // 根据权限显示
    private String wechatMasked;   // 脱敏版本
    private Integer wechatUnlockCondition;
    private String wechatUnlockDesc;
    private Boolean canViewWechat; // 当前用户是否可查看

    // ==================== 认证标识 ====================

    private Boolean isRealVerified;
    private Boolean isGodVerified;
    private Boolean isActivityExpert;
    private Boolean isVip;
    private Boolean isVipValid;
    private Boolean isPopular;
    private Integer vipLevel;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime vipExpireTime;

    // ==================== 在线状态 ====================

    private Integer onlineStatus;
    private String onlineStatusDesc;
    private Boolean isOnline;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    // ==================== 资料完整度 ====================

    private Integer profileCompleteness;
    private String completenessLevel;
    private Boolean isProfileComplete;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEditTime;

    // ==================== 扩展信息 ====================

    /**
     * 职业标签列表
     */
    private List<UserOccupationVO> occupations;

    /**
     * 统计数据
     */
    private UserStatsVO stats;

    /**
     * 是否已关注（当前用户视角）
     */
    private Boolean isFollowed;

    /**
     * 是否互相关注
     */
    private Boolean isMutualFollow;

    /**
     * 是否拉黑
     */
    private Boolean isBlocked;

    // ==================== 系统字段 ====================

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer version;
}

