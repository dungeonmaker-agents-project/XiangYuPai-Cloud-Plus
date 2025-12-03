package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户技能VO
 *
 * <p>对应UI文档中的 SkillItem</p>
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能类型: online-线上, offline-线下
     */
    private String skillType;

    /**
     * 封面图URL
     */
    private String coverImage;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 媒体数据
     */
    private MediaData mediaData;

    /**
     * 技能详情
     */
    private SkillInfo skillInfo;

    /**
     * 价格数据
     */
    private PriceData priceData;

    /**
     * 统计数据
     */
    private StatsData statsData;

    /**
     * 是否上架
     */
    private Boolean isOnline;

    // ==================== 内嵌类 ====================

    /**
     * 媒体数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 封面图URL
         */
        private String coverUrl;

        /**
         * 展示图片列表
         */
        private List<String> images;

        /**
         * 视频URL（如有）
         */
        private String videoUrl;
    }

    /**
     * 技能详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 游戏名称（线上技能）
         */
        private String gameName;

        /**
         * 游戏段位（线上技能）
         */
        private String gameRank;

        /**
         * 服务类型（线下技能）
         */
        private String serviceType;

        /**
         * 服务地点（线下技能）
         */
        private String serviceLocation;

        /**
         * 服务时长（小时/局）
         */
        private BigDecimal serviceHours;

        /**
         * 标签列表
         */
        private List<String> tags;
    }

    /**
     * 价格数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 价格（元）
         */
        private BigDecimal amount;

        /**
         * 单位: 局, 小时
         */
        private String unit;

        /**
         * 显示文本，如 "30元/局"
         */
        private String displayText;

        /**
         * 原价（用于促销）
         */
        private BigDecimal originalPrice;

        /**
         * 是否有折扣
         */
        private Boolean hasDiscount;
    }

    /**
     * 统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 评分 (0-5.00)
         */
        private BigDecimal rating;

        /**
         * 评价数
         */
        private Integer reviewCount;

        /**
         * 订单数
         */
        private Integer orderCount;
    }
}
