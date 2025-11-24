package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户基本信息实体
 * 对应表: users
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键）
     * 使用INPUT类型，允许手动指定ID（由auth服务生成）
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 国家代码
     */
    @TableField("country_code")
    private String countryCode;

    /**
     * 昵称（2-20字符）
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别: male, female, other
     */
    @TableField("gender")
    private String gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 居住地（省市区）
     */
    @TableField("residence")
    private String residence;

    /**
     * 身高（cm, 100-250）
     */
    @TableField("height")
    private Integer height;

    /**
     * 体重（kg, 30-200）
     */
    @TableField("weight")
    private Integer weight;

    /**
     * 职业（1-30字符）
     */
    @TableField("occupation")
    private String occupation;

    /**
     * 微信号（6-20字符）
     */
    @TableField("wechat")
    private String wechat;

    /**
     * 个性签名（0-200字符）
     */
    @TableField("bio")
    private String bio;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 隐私设置 - 资料可见性 (1=公开, 2=仅粉丝, 3=私密)
     */
    @TableField("privacy_profile")
    private Integer privacyProfile;

    /**
     * 是否在线（0-否，1-是）
     */
    @TableField("is_online")
    private Boolean isOnline;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
