package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 筛选用户VO (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 用户基本信息 ==========

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
     * 性别: male/female/other
     */
    private String gender;

    /**
     * 年龄 (根据生日计算)
     */
    private Integer age;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 个性签名
     */
    private String bio;

    /**
     * 居住地
     */
    private String residence;

    // ========== 位置信息 ==========

    /**
     * 距离(米) - 由服务端根据查询者位置计算
     */
    private Integer distance;

    // ========== 技能信息 (从skills表) ==========

    /**
     * 主要技能ID
     */
    private Long skillId;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能类型: online/offline
     */
    private String skillType;

    /**
     * 游戏名称 (线上技能)
     */
    private String gameName;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 价格单位: 局, 小时
     */
    private String priceUnit;

    /**
     * 技能等级/段位 (线上技能的game_rank)
     */
    private String skillLevel;

    /**
     * 技能评分 (0-5.00)
     */
    private BigDecimal rating;

    /**
     * 订单数量
     */
    private Integer orderCount;

    // ========== 统计信息 ==========

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 获赞数
     */
    private Integer likesCount;

    /**
     * 动态数
     */
    private Integer postsCount;

    /**
     * 最后活跃时间 (用于活跃状态筛选)
     */
    private java.time.LocalDateTime lastActiveAt;
}
