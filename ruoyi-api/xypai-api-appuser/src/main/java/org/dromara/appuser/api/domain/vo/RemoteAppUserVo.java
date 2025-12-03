package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 远程调用APP用户VO
 * Remote App User VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteAppUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别: male, female, other
     */
    private String gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 居住地
     */
    private String residence;

    /**
     * 个性签名
     */
    private String bio;

    /**
     * 用户等级: 1-青铜,2-白银,3-黄金,4-铂金,5-钻石,6-大师,7-王者
     */
    private Integer level;

    /**
     * 用户等级名称
     */
    private String levelName;

    /**
     * 是否实名认证
     */
    private Boolean isRealVerified;

    /**
     * 是否大神认证
     */
    private Boolean isGodVerified;

    /**
     * 是否VIP
     */
    private Boolean isVip;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 关注数
     */
    private Integer followingCount;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 获赞数
     */
    private Integer likesCount;

    /**
     * 是否已被当前用户关注（需要传入currentUserId才会计算）
     */
    private Boolean isFollowed;

    /**
     * 获取等级名称
     */
    public static String getLevelNameByLevel(Integer level) {
        if (level == null) return "青铜";
        return switch (level) {
            case 1 -> "青铜";
            case 2 -> "白银";
            case 3 -> "黄金";
            case 4 -> "铂金";
            case 5 -> "钻石";
            case 6 -> "大师";
            case 7 -> "王者";
            default -> "青铜";
        };
    }
}
