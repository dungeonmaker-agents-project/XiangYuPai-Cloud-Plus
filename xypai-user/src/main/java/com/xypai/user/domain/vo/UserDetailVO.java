package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户详情VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 位置信息
     */
    private String location;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 用户状态描述
     */
    private String statusDesc;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 是否已关注（当前用户视角）
     */
    private Boolean followed;

    /**
     * 关注数
     */
    private Long followingCount;

    /**
     * 粉丝数
     */
    private Long followersCount;

    /**
     * 钱包余额（元）
     */
    private String walletBalance;

    /**
     * 脱敏后的手机号
     */
    public String getMaskedMobile() {
        if (mobile != null && mobile.length() >= 11) {
            return mobile.substring(0, 3) + "****" + mobile.substring(7);
        }
        return mobile;
    }

    /**
     * 脱敏后的邮箱
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
