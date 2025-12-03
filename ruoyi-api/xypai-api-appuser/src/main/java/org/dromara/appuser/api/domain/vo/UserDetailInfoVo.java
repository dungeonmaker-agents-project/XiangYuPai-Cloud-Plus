package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户详细资料VO
 *
 * <p>对应UI文档中的 ProfileInfoData</p>
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 居住地（省市区）
     */
    private String residence;

    /**
     * IP归属地
     */
    private String ipLocation;

    /**
     * 身高（cm）
     */
    private Integer height;

    /**
     * 体重（kg）
     */
    private Integer weight;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 微信号（脱敏格式：显示前2后2，中间*号）
     * 例：wx****88
     * 如果已解锁则显示完整微信号
     */
    private String wechat;

    /**
     * 是否已解锁微信
     */
    private Boolean wechatUnlocked;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 星座
     */
    private String zodiac;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 个人简介
     */
    private String bio;

    // ==================== 隐私控制 ====================

    /**
     * 是否可查看完整资料
     */
    private Boolean canViewFull;

    /**
     * 不可查看原因
     */
    private String viewRestrictionReason;
}
