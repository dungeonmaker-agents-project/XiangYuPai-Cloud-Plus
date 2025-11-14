package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * App用户实体（对应 app_user 表）
 *
 * <p>特点：</p>
 * <ul>
 *     <li>使用 MyBatis Plus 注解（无需 XML mapper）</li>
 *     <li>支持 LambdaQueryWrapper 查询</li>
 *     <li>自动填充创建时间和更新时间</li>
 *     <li>软删除支持（@TableLogic）</li>
 *     <li>乐观锁支持（@Version）</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("app_user")
public class AppUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== Primary Key ====================

    /**
     * 用户ID（自增主键）
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    // ==================== Login Credentials ====================

    /**
     * 手机号（登录凭证，唯一）
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 国家区号（例如：+86, +852）
     */
    @TableField("country_code")
    @Builder.Default
    private String countryCode = "+86";

    /**
     * 密码（BCrypt加密，可选）
     */
    @TableField("password")
    private String password;

    // ==================== Basic Profile ====================

    /**
     * 用户昵称（默认：手机号脱敏）
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别：0=未设置，1=男，2=女
     */
    @TableField("gender")
    @Builder.Default
    private Integer gender = 0;

    // ==================== Status Fields ====================

    /**
     * 账号状态：0=禁用，1=正常，2=锁定
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 是否新用户：0=否（已完善资料），1=是（需完善资料）
     */
    @TableField("is_new_user")
    @Builder.Default
    private Boolean isNewUser = true;

    // ==================== Login Info ====================

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 登录次数
     */
    @TableField("login_count")
    @Builder.Default
    private Integer loginCount = 0;

    // ==================== Audit Fields ====================

    /**
     * 创建时间（注册时间）
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
     * 软删除时间（NULL=未删除）
     */
    @TableLogic(value = "NULL", delval = "NOW()")
    @TableField("deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    // ==================== Version Lock ====================

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    // ==================== Business Methods ====================

    /**
     * 判断账号是否可用
     *
     * @return true=正常，false=禁用/锁定
     */
    public boolean isAccountNonLocked() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断是否需要完善资料
     *
     * @return true=新用户，需要完善资料
     */
    public boolean needCompleteProfile() {
        return Boolean.TRUE.equals(this.isNewUser);
    }

    /**
     * 判断是否已设置密码
     *
     * @return true=已设置密码
     */
    public boolean hasPassword() {
        return this.password != null && !this.password.isEmpty();
    }

    /**
     * 生成默认昵称（手机号脱敏）
     *
     * @param mobile 手机号
     * @return 脱敏昵称，例如："131****6323"
     */
    public static String generateDefaultNickname(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "用户" + System.currentTimeMillis() % 10000;
        }
        // 131****6323
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
