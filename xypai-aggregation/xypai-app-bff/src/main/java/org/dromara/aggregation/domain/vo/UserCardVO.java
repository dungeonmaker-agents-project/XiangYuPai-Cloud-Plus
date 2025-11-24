package org.dromara.aggregation.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户卡片 VO (首页推荐列表)
 *
 * @author XyPai Team
 * @date 2025-11-20
 */
@Data
public class UserCardVO implements Serializable {

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
     * 头像
     */
    private String avatar;

    /**
     * 性别: 0-未知, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 城市
     */
    private String city;

    /**
     * 距离 (米)
     */
    private Double distance;

    /**
     * 距离文本 (如: 1.2km, 500m)
     */
    private String distanceText;

    /**
     * 用户技能列表
     */
    private List<String> skills;

    /**
     * 动态数量
     */
    private Integer feedCount;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否已关注
     */
    private Boolean isFollowed;

    /**
     * 个人简介
     */
    private String bio;

}
