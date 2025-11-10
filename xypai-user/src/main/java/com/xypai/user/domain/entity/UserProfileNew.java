package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * 用户资料扩展实体（v7.1完整版 - 42个具体字段）
 * 
 * 设计理念：
 * 1. 字段完全展开，符合数据库第一范式
 * 2. 支持索引查询，性能优化
 * 3. 支持个人主页完整展示
 * 4. 支持资料编辑和验证
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_profile")
public class UserProfileNew implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    /**
     * 关联用户ID（主键）
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 用户昵称(显示名,1-20字符)
     */
    @TableField("nickname")
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20个字符之间")
    private String nickname;

    /**
     * 头像URL(正方形裁剪,CDN地址)
     */
    @TableField("avatar")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    /**
     * 头像缩略图URL(用于列表显示)
     */
    @TableField("avatar_thumbnail")
    @Size(max = 500, message = "缩略图URL长度不能超过500个字符")
    private String avatarThumbnail;

    /**
     * 个人主页背景图URL
     */
    @TableField("background_image")
    @Size(max = 500, message = "背景图URL长度不能超过500个字符")
    private String backgroundImage;

    /**
     * 性别(1=男,2=女,3=其他,0=未设置)
     */
    @TableField("gender")
    @Builder.Default
    private Integer gender = 0;

    /**
     * 生日(YYYY-MM-DD格式)
     */
    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /**
     * 年龄(根据生日自动计算)
     */
    @TableField("age")
    private Integer age;

    // ==================== 位置信息 ====================

    /**
     * 所在城市ID(关联City表)
     */
    @TableField("city_id")
    private Long cityId;

    /**
     * 城市位置信息(如"广东 深圳")
     */
    @TableField("location")
    @Size(max = 100, message = "位置信息长度不能超过100个字符")
    private String location;

    /**
     * 详细地址
     */
    @TableField("address")
    @Size(max = 255, message = "详细地址长度不能超过255个字符")
    private String address;

    /**
     * IP归属地(如"广东 深圳",用于显示)
     */
    @TableField("ip_location")
    @Size(max = 100, message = "IP归属地长度不能超过100个字符")
    private String ipLocation;

    // ==================== 个人资料 ====================

    /**
     * 个人简介(0-500字符,多行文本)
     */
    @TableField("bio")
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    /**
     * 身高(cm,范围140-200)
     */
    @TableField("height")
    @Min(value = 140, message = "身高不能低于140cm")
    @Max(value = 200, message = "身高不能超过200cm")
    private Integer height;

    /**
     * 体重(kg,范围30-150)
     */
    @TableField("weight")
    @Min(value = 30, message = "体重不能低于30kg")
    @Max(value = 150, message = "体重不能超过150kg")
    private Integer weight;

    // ==================== 联系方式与认证 ====================

    /**
     * 真实姓名(实名认证使用)
     */
    @TableField("real_name")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 身份证号(AES-256加密存储,使用KMS密钥管理)
     */
    @TableField("id_card_encrypted")
    @Size(max = 200, message = "身份证密文长度不能超过200个字符")
    private String idCardEncrypted;

    /**
     * 微信号(6-20位字母数字下划线)
     */
    @TableField("wechat")
    @Size(min = 6, max = 20, message = "微信号长度必须在6-20个字符之间")
    private String wechat;

    /**
     * 微信解锁条件(0=公开,1=关注后可见,2=付费可见,3=私密)
     */
    @TableField("wechat_unlock_condition")
    @Builder.Default
    private Integer wechatUnlockCondition = 0;

    // ==================== 认证标识 ====================

    /**
     * 是否实名认证
     */
    @TableField("is_real_verified")
    @Builder.Default
    private Boolean isRealVerified = false;

    /**
     * 是否大神认证
     */
    @TableField("is_god_verified")
    @Builder.Default
    private Boolean isGodVerified = false;

    /**
     * 是否组局达人认证
     */
    @TableField("is_activity_expert")
    @Builder.Default
    private Boolean isActivityExpert = false;

    /**
     * 是否VIP用户
     */
    @TableField("is_vip")
    @Builder.Default
    private Boolean isVip = false;

    /**
     * 是否人气用户(系统认证)
     */
    @TableField("is_popular")
    @Builder.Default
    private Boolean isPopular = false;

    /**
     * VIP等级(1-5级)
     */
    @TableField("vip_level")
    @Builder.Default
    private Integer vipLevel = 0;

    /**
     * VIP过期时间
     */
    @TableField("vip_expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime vipExpireTime;

    // ==================== 在线状态 ====================

    /**
     * 在线状态(0=离线,1=在线,2=忙碌,3=隐身)
     */
    @TableField("online_status")
    @Builder.Default
    private Integer onlineStatus = 0;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    // ==================== 其他字段 ====================

    /**
     * 资料完整度(0-100百分比,用于推荐算法)
     */
    @TableField("profile_completeness")
    @Builder.Default
    private Integer profileCompleteness = 0;

    /**
     * 最后编辑资料时间
     */
    @TableField("last_edit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEditTime;

    /**
     * 软删除时间
     */
    @TableField("deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    // ==================== 业务方法 ====================

    /**
     * 性别枚举
     */
    public enum Gender {
        UNSET(0, "未设置"),
        MALE(1, "男"),
        FEMALE(2, "女"),
        OTHER(3, "其他");

        private final Integer code;
        private final String desc;

        Gender(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 在线状态枚举
     */
    public enum OnlineStatus {
        OFFLINE(0, "离线"),
        ONLINE(1, "在线"),
        BUSY(2, "忙碌"),
        INVISIBLE(3, "隐身");

        private final Integer code;
        private final String desc;

        OnlineStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 微信解锁条件枚举
     */
    public enum WechatUnlockCondition {
        PUBLIC(0, "公开"),
        FOLLOW(1, "关注后可见"),
        PAID(2, "付费可见"),
        PRIVATE(3, "私密");

        private final Integer code;
        private final String desc;

        WechatUnlockCondition(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 自动计算年龄（基于生日）
     */
    public void calculateAge() {
        if (birthday != null) {
            this.age = Period.between(birthday, LocalDate.now()).getYears();
        }
    }

    /**
     * 检查是否为男性
     */
    public boolean isMale() {
        return Gender.MALE.getCode().equals(this.gender);
    }

    /**
     * 检查是否为女性
     */
    public boolean isFemale() {
        return Gender.FEMALE.getCode().equals(this.gender);
    }

    /**
     * 检查是否在线
     */
    public boolean isOnline() {
        return OnlineStatus.ONLINE.getCode().equals(this.onlineStatus);
    }

    /**
     * 检查是否隐身
     */
    public boolean isInvisible() {
        return OnlineStatus.INVISIBLE.getCode().equals(this.onlineStatus);
    }

    /**
     * 检查VIP是否有效
     */
    public boolean isVipValid() {
        if (!Boolean.TRUE.equals(isVip)) {
            return false;
        }
        if (vipExpireTime == null) {
            return true; // 永久VIP
        }
        return vipExpireTime.isAfter(LocalDateTime.now());
    }

    /**
     * 检查是否为高级认证用户（实名+大神+组局达人）
     */
    public boolean isFullyVerified() {
        return Boolean.TRUE.equals(isRealVerified) 
            && Boolean.TRUE.equals(isGodVerified) 
            && Boolean.TRUE.equals(isActivityExpert);
    }

    /**
     * 检查是否可以查看微信号
     */
    public boolean canViewWechat(boolean isFollowed, boolean isPaid) {
        if (wechatUnlockCondition == null || wechatUnlockCondition == 0) {
            return true; // 公开
        }
        if (wechatUnlockCondition == 1) {
            return isFollowed; // 关注后可见
        }
        if (wechatUnlockCondition == 2) {
            return isPaid; // 付费可见
        }
        return false; // 私密
    }

    /**
     * 检查资料是否完整（≥80%）
     */
    public boolean isProfileComplete() {
        return profileCompleteness != null && profileCompleteness >= 80;
    }

    /**
     * 更新在线状态
     */
    public void updateOnlineStatus(Integer status) {
        this.onlineStatus = status;
        this.lastOnlineTime = LocalDateTime.now();
    }

    /**
     * 上线
     */
    public void goOnline() {
        updateOnlineStatus(OnlineStatus.ONLINE.getCode());
    }

    /**
     * 离线
     */
    public void goOffline() {
        updateOnlineStatus(OnlineStatus.OFFLINE.getCode());
    }

    /**
     * 隐身
     */
    public void goInvisible() {
        updateOnlineStatus(OnlineStatus.INVISIBLE.getCode());
    }

    /**
     * 更新编辑时间
     */
    public void markAsEdited() {
        this.lastEditTime = LocalDateTime.now();
    }

    /**
     * 获取性别描述
     */
    public String getGenderDesc() {
        for (Gender g : Gender.values()) {
            if (g.getCode().equals(this.gender)) {
                return g.getDesc();
            }
        }
        return "未设置";
    }

    /**
     * 获取在线状态描述
     */
    public String getOnlineStatusDesc() {
        for (OnlineStatus status : OnlineStatus.values()) {
            if (status.getCode().equals(this.onlineStatus)) {
                return status.getDesc();
            }
        }
        return "未知";
    }

    /**
     * 计算资料完整度
     */
    public int calculateCompleteness() {
        int score = 0;
        
        // 核心字段（50分）
        if (nickname != null && !nickname.isEmpty()) score += 10;
        if (avatar != null && !avatar.isEmpty()) score += 10;
        if (gender != null && gender > 0) score += 10;
        if (birthday != null) score += 10;
        if (cityId != null) score += 10;
        
        // 扩展字段（50分）
        if (bio != null && !bio.isEmpty()) score += 5;
        if (height != null) score += 5;
        if (weight != null) score += 5;
        if (wechat != null && !wechat.isEmpty()) score += 5;
        if (backgroundImage != null && !backgroundImage.isEmpty()) score += 5;
        if (Boolean.TRUE.equals(isRealVerified)) score += 15;
        if (location != null && !location.isEmpty()) score += 5;
        if (address != null && !address.isEmpty()) score += 5;
        
        this.profileCompleteness = score;
        return score;
    }
}

