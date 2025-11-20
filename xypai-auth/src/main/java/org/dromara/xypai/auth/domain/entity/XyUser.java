package org.dromara.xypai.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应表: xy_user
 *
 * @author XiangYuPai
 * @since 2025-11-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("xy_user")
public class XyUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键）
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 是否实名认证
     */
    @TableField("is_real_verified")
    private Boolean isRealVerified;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 身份证加密
     */
    @TableField("id_card_encrypted")
    private String idCardEncrypted;

    /**
     * 身份证后4位
     */
    @TableField("id_card_last4")
    private String idCardLast4;

    /**
     * 是否神认证
     */
    @TableField("is_god_verified")
    private Boolean isGodVerified;

    /**
     * 是否活动专家
     */
    @TableField("is_activity_expert")
    private Boolean isActivityExpert;

    /**
     * 是否VIP
     */
    @TableField("is_vip")
    private Boolean isVip;

    /**
     * VIP过期时间
     */
    @TableField("vip_expire_time")
    private LocalDateTime vipExpireTime;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 在线状态
     */
    @TableField("online_status")
    private String onlineStatus;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 个人资料JSON
     */
    @TableField("profile_json")
    private String profileJson;

    /**
     * 认证信息JSON
     */
    @TableField("certification_json")
    private String certificationJson;

    /**
     * 统计信息JSON
     */
    @TableField("stats_json")
    private String statsJson;

    /**
     * 钱包信息JSON
     */
    @TableField("wallet_json")
    private String walletJson;

    /**
     * 设置信息JSON
     */
    @TableField("settings_json")
    private String settingsJson;

    /**
     * 社交信息JSON
     */
    @TableField("social_json")
    private String socialJson;

    /**
     * 偏好设置JSON
     */
    @TableField("preferences_json")
    private String preferencesJson;

    /**
     * 自定义信息JSON
     */
    @TableField("custom_json")
    private String customJson;
}
