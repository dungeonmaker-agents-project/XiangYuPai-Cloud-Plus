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
}
