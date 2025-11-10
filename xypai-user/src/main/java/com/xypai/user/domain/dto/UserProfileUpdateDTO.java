package com.xypai.user.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户资料更新DTO（支持42个字段）
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（更新时必填）
     */
    private Long userId;

    /**
     * 用户昵称(1-20字符)
     */
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20个字符之间")
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    /**
     * 头像缩略图URL
     */
    @Size(max = 500, message = "缩略图URL长度不能超过500个字符")
    private String avatarThumbnail;

    /**
     * 背景图URL
     */
    @Size(max = 500, message = "背景图URL长度不能超过500个字符")
    private String backgroundImage;

    /**
     * 性别(1=男,2=女,3=其他,0=未设置)
     */
    @Min(value = 0, message = "性别值必须在0-3之间")
    @Max(value = 3, message = "性别值必须在0-3之间")
    private Integer gender;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "生日必须是过去的日期")
    private LocalDate birthday;

    /**
     * 所在城市ID
     */
    private Long cityId;

    /**
     * 位置信息
     */
    @Size(max = 100, message = "位置信息长度不能超过100个字符")
    private String location;

    /**
     * 详细地址
     */
    @Size(max = 255, message = "详细地址长度不能超过255个字符")
    private String address;

    /**
     * 个人简介(0-500字符)
     */
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    /**
     * 身高(140-200cm)
     */
    @Min(value = 140, message = "身高不能低于140cm")
    @Max(value = 200, message = "身高不能超过200cm")
    private Integer height;

    /**
     * 体重(30-150kg)
     */
    @Min(value = 30, message = "体重不能低于30kg")
    @Max(value = 150, message = "体重不能超过150kg")
    private Integer weight;

    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 微信号(6-20位)
     */
    @Size(min = 6, max = 20, message = "微信号长度必须在6-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "微信号只能包含字母、数字、下划线和中划线")
    private String wechat;

    /**
     * 微信解锁条件(0=公开,1=关注后可见,2=付费可见,3=私密)
     */
    @Min(value = 0, message = "微信解锁条件值必须在0-3之间")
    @Max(value = 3, message = "微信解锁条件值必须在0-3之间")
    private Integer wechatUnlockCondition;

    /**
     * 在线状态(0=离线,1=在线,2=忙碌,3=隐身)
     */
    @Min(value = 0, message = "在线状态值必须在0-3之间")
    @Max(value = 3, message = "在线状态值必须在0-3之间")
    private Integer onlineStatus;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}

