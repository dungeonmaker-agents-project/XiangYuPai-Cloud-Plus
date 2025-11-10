package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户基础信息实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识(雪花ID)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录用户名(唯一)
     */
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文")
    private String username;

    /**
     * 手机号(唯一,登录凭证)
     */
    @TableField("mobile")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 地区代码(如+86,默认+86)
     */
    @TableField("region_code")
    @Builder.Default
    private String regionCode = "+86";

    /**
     * 邮箱(唯一,辅助登录凭证,可为空)
     */
    @TableField("email")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    /**
     * 密码哈希值(BCrypt加密)
     */
    @TableField("password")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 密码盐值(随机生成)
     */
    @TableField("password_salt")
    private String passwordSalt;

    /**
     * 密码最后更新时间
     */
    @TableField("password_updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordUpdatedAt;

    /**
     * 用户状态(0=禁用,1=正常,2=冻结,3=待激活)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 登录失败次数(用于防暴力破解)
     */
    @TableField("login_fail_count")
    @Builder.Default
    private Integer loginFailCount = 0;

    /**
     * 账户锁定截止时间(5次失败锁定30分钟)
     */
    @TableField("login_locked_until")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginLockedUntil;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP地址
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后登录设备ID
     */
    @TableField("last_login_device_id")
    private String lastLoginDeviceId;

    /**
     * 是否启用双因子认证
     */
    @TableField("is_two_factor_enabled")
    @Builder.Default
    private Boolean isTwoFactorEnabled = false;

    /**
     * 双因子认证密钥(TOTP)
     */
    @TableField("two_factor_secret")
    private String twoFactorSecret;

    /**
     * 注册时间
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
     * 是否删除标志(逻辑删除)
     */
    @TableLogic
    @TableField("deleted")
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 用户状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        NORMAL(1, "正常"), 
        FROZEN(2, "冻结");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
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
     * 检查用户状态是否正常
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 检查用户是否被禁用
     */
    public boolean isDisabled() {
        return Status.DISABLED.getCode().equals(this.status);
    }

    /**
     * 检查用户是否被冻结
     */
    public boolean isFrozen() {
        return Status.FROZEN.getCode().equals(this.status);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        for (Status status : Status.values()) {
            if (status.getCode().equals(this.status)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }

    /**
     * 检查账户是否被锁定
     */
    public boolean isLocked() {
        return loginLockedUntil != null && loginLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 增加登录失败次数
     * @return 当前失败次数
     */
    public int incrementLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }
        this.loginFailCount++;
        return this.loginFailCount;
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.loginLockedUntil = null;
    }

    /**
     * 锁定账户(30分钟)
     */
    public void lockAccount() {
        this.loginLockedUntil = LocalDateTime.now().plusMinutes(30);
    }

    /**
     * 锁定账户(指定分钟数)
     */
    public void lockAccount(int minutes) {
        this.loginLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * 更新最后登录信息
     */
    public void updateLastLogin(String ip, String deviceId) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.lastLoginDeviceId = deviceId;
    }

    /**
     * 检查是否需要更新密码(超过90天)
     */
    public boolean needPasswordUpdate() {
        if (passwordUpdatedAt == null) {
            return true;
        }
        return passwordUpdatedAt.plusDays(90).isBefore(LocalDateTime.now());
    }

    /**
     * 检查邮箱是否已验证
     */
    public boolean isEmailVerified() {
        return email != null && !email.isEmpty();
    }

    /**
     * 获取脱敏手机号
     */
    public String getMaskedMobile() {
        if (mobile != null && mobile.length() >= 11) {
            return mobile.substring(0, 3) + "****" + mobile.substring(7);
        }
        return mobile;
    }

    /**
     * 获取脱敏邮箱
     */
    public String getMaskedEmail() {
        if (email != null && email.contains("@")) {
            String[] parts = email.split("@");
            String username = parts[0];
            String domain = parts[1];
            if (username.length() > 2) {
                username = username.substring(0, 2) + "***";
            }
            return username + "@" + domain;
        }
        return email;
    }
}
