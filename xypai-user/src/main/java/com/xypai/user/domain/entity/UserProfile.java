package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 用户资料扩展实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "user_profile", autoResultMap = true)
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联用户ID
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 用户昵称(显示名)
     */
    @TableField("nickname")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    /**
     * 扩展信息JSON{email,real_name,location,bio...}
     */
    @TableField(value = "metadata", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 获取邮箱
     */
    public String getEmail() {
        return metadata != null ? (String) metadata.get("email") : null;
    }

    /**
     * 设置邮箱
     */
    public void setEmail(String email) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("email", email);
    }

    /**
     * 获取真实姓名
     */
    public String getRealName() {
        return metadata != null ? (String) metadata.get("real_name") : null;
    }

    /**
     * 设置真实姓名
     */
    public void setRealName(String realName) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("real_name", realName);
    }

    /**
     * 获取位置信息
     */
    public String getLocation() {
        return metadata != null ? (String) metadata.get("location") : null;
    }

    /**
     * 设置位置信息
     */
    public void setLocation(String location) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("location", location);
    }

    /**
     * 获取个人简介
     */
    public String getBio() {
        return metadata != null ? (String) metadata.get("bio") : null;
    }

    /**
     * 设置个人简介
     */
    public void setBio(String bio) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("bio", bio);
    }
}
