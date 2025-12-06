package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 技能服务 VO (列表用，用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillServiceVo implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 技能ID ==========

    /**
     * 技能ID (即 serviceId)
     */
    private Long skillId;

    // ========== 用户信息 ==========

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
     * 年龄
     */
    private Integer age;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否认证
     */
    private Boolean isVerified;

    // ========== 技能信息 ==========

    /**
     * 技能类型 (如: honor_of_kings)
     */
    private String skillType;

    /**
     * 技能类型名称 (如: 王者荣耀)
     */
    private String skillTypeName;

    /**
     * 游戏大区 (如: 微信区)
     */
    private String gameArea;

    /**
     * 段位 (如: 荣耀王者)
     */
    private String rank;

    /**
     * 段位分数
     */
    private Integer rankScore;

    /**
     * 巅峰分（王者荣耀专用，如：2680）
     */
    private Integer peakScore;

    /**
     * 位置/英雄 (如: ["打野", "中路"])
     */
    private List<String> positions;

    // ========== 标签 ==========

    /**
     * 标签列表
     */
    private List<SkillTagVo> tags;

    // ========== 价格 ==========

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 价格单位 (如: 局, 小时)
     */
    private String priceUnit;

    /**
     * 价格显示文本 (如: "5 金币/局")
     */
    private String priceDisplay;

    // ========== 描述 ==========

    /**
     * 技能描述
     */
    private String description;

    // ========== 统计 ==========

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 评分 (0-5.00)
     */
    private BigDecimal rating;

    /**
     * 评价数量
     */
    private Integer reviewCount;

    // ========== 距离 (由调用方计算或传入) ==========

    /**
     * 距离 (米)
     */
    private Integer distance;

    /**
     * 标签VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillTagVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 标签文本
         */
        private String text;

        /**
         * 标签类型 (如: service, skill)
         */
        private String type;

        /**
         * 标签颜色
         */
        private String color;
    }
}
