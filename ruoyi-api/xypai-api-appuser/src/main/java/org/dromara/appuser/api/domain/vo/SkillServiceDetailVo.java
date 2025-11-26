package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 技能服务详情 VO (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillServiceDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 基本信息 ==========

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 封面图URL
     */
    private String bannerImage;

    /**
     * 图片列表
     */
    private List<String> images;

    // ========== 服务提供者信息 ==========

    private ProviderDetailVo provider;

    // ========== 技能信息 ==========

    private SkillDetailInfo skillInfo;

    // ========== 标签 ==========

    /**
     * 标签列表
     */
    private List<SkillServiceVo.SkillTagVo> tags;

    // ========== 描述 ==========

    /**
     * 技能描述
     */
    private String description;

    // ========== 价格 ==========

    private PriceInfo price;

    // ========== 时间安排 ==========

    private ScheduleInfo schedule;

    // ========== 位置信息 (线下服务) ==========

    private LocationInfo location;

    // ========== 统计数据 ==========

    private StatsInfo stats;

    // ========== 评价信息 ==========

    private ReviewsInfo reviews;

    // ========== 可用状态 ==========

    /**
     * 是否可用
     */
    private Boolean isAvailable;

    /**
     * 不可用原因
     */
    private String unavailableReason;

    /**
     * 服务提供者详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderDetailVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String nickname;
        private String avatar;
        private String gender;
        private Integer age;
        private Boolean isOnline;
        private Boolean isVerified;
        private Integer level;
        private List<String> certifications;
    }

    /**
     * 技能详细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDetailInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String skillType;
        private String skillLabel;
        private String gameArea;
        private String rank;
        private Integer rankScore;
        private String rankDisplay;
        private List<String> position;
        private String voiceType;
    }

    /**
     * 价格信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private BigDecimal amount;
        private String unit;
        private String displayText;
    }

    /**
     * 时间安排信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String available;
    }

    /**
     * 位置信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String address;
        private String district;
        private Integer distance;
        private String distanceDisplay;
    }

    /**
     * 统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer orders;
        private BigDecimal rating;
        private Integer reviewCount;
    }

    /**
     * 评价信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewsInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer total;
        private ReviewSummaryVo summary;
        private List<ReviewTagVo> tags;
        private List<ReviewItemVo> recent;
    }

    /**
     * 评价摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSummaryVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer excellent;
        private Integer positive;
        private Integer negative;
    }

    /**
     * 评价标签
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewTagVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String text;
        private Integer count;
    }

    /**
     * 评价项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItemVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long reviewId;
        private Integer rating;
        private String content;
        private LocalDateTime createdAt;
        private ReviewerVo reviewer;
        private String reply;
    }

    /**
     * 评价者
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewerVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String nickname;
        private String avatar;
    }
}
