package org.dromara.appuser.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * App用户登录对象（用于xypai-auth认证）
 *
 * <p>特点：</p>
 * <ul>
 *     <li>无租户字段（app用户无需多租户）</li>
 *     <li>无部门/角色字段（app用户无组织架构）</li>
 *     <li>仅包含登录认证必需字段</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Data
@NoArgsConstructor
public class AppLoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 用户基本信息 ====================

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 手机号（登录凭证）
     */
    private String mobile;

    /**
     * 国家区号
     */
    private String countryCode;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别：0=未设置，1=男，2=女
     */
    private Integer gender;

    // ==================== 状态字段 ====================

    /**
     * 账号状态：0=禁用，1=正常，2=锁定
     */
    private Integer status;

    /**
     * 是否新用户（首次登录）
     */
    private Boolean isNewUser;

    // ==================== 认证字段 ====================

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 客户端Key（用于区分不同客户端）
     */
    private String clientKey;

    /**
     * 设备类型（PC/APP/小程序）
     */
    private String deviceType;

    // ==================== 业务方法 ====================

    /**
     * 获取登录ID（用于Sa-Token）
     * @return userId
     */
    public String getLoginId() {
        return String.valueOf(userId);
    }

    /**
     * 判断账号是否可用
     * @return true=正常，false=禁用/锁定
     */
    public boolean isAccountNonLocked() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断是否需要完善资料（新用户）
     * @return true=需要跳转完善资料页
     */
    public boolean needCompleteProfile() {
        return Boolean.TRUE.equals(this.isNewUser);
    }
}
