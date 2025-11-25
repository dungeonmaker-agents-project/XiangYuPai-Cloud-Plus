package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务详情VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "服务详情")
public class ServiceDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "服务ID")
    private Long serviceId;

    // ========== 服务提供者信息 ==========

    @Schema(description = "服务提供者信息")
    private ProviderDetailVO provider;

    // ========== 封面和图片 ==========

    @Schema(description = "封面图片URL")
    private String bannerImage;

    @Schema(description = "图片列表")
    private List<String> images;

    // ========== 技能信息 ==========

    @Schema(description = "技能信息")
    private SkillDetailVO skillInfo;

    // ========== 标签和描述 ==========

    @Schema(description = "标签列表")
    private List<ServiceCardVO.TagVO> tags;

    @Schema(description = "服务描述")
    private String description;

    // ========== 价格信息 ==========

    @Schema(description = "价格信息")
    private ServiceCardVO.PriceVO price;

    // ========== 时间和位置 ==========

    @Schema(description = "可用时间")
    private ScheduleVO schedule;

    @Schema(description = "位置信息(线下服务)")
    private LocationVO location;

    // ========== 统计数据 ==========

    @Schema(description = "统计数据")
    private ServiceCardVO.StatsVO stats;

    // ========== 评价信息 ==========

    @Schema(description = "评价信息")
    private ReviewsVO reviews;

    // ========== 可用状态 ==========

    @Schema(description = "是否可下单")
    private Boolean isAvailable;

    @Schema(description = "不可下单原因")
    private String unavailableReason;

    /**
     * 服务提供者详细信息
     */
    @Data
    @Schema(description = "服务提供者详细信息")
    public static class ProviderDetailVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "头像URL")
        private String avatar;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "性别: male/female")
        private String gender;

        @Schema(description = "年龄")
        private Integer age;

        @Schema(description = "是否在线")
        private Boolean isOnline;

        @Schema(description = "是否实名认证")
        private Boolean isVerified;

        @Schema(description = "等级")
        private Integer level;

        @Schema(description = "认证标签", example = "[\"实名认证\", \"大神\"]")
        private List<String> certifications;
    }

    /**
     * 技能详细信息
     */
    @Data
    @Schema(description = "技能详细信息")
    public static class SkillDetailVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "技能类型代码")
        private String skillType;

        @Schema(description = "技能类型名称")
        private String skillLabel;

        @Schema(description = "游戏大区")
        private String gameArea;

        @Schema(description = "段位")
        private String rank;

        @Schema(description = "段位分数")
        private Integer rankScore;

        @Schema(description = "段位展示", example = "巅峰1800+")
        private String rankDisplay;

        @Schema(description = "擅长位置")
        private List<String> position;

        @Schema(description = "声音类型", example = "软萌")
        private String voiceType;
    }

    /**
     * 可用时间
     */
    @Data
    @Schema(description = "可用时间")
    public static class ScheduleVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "可用时间描述", example = "周一至周五 18:00-23:00")
        private String available;
    }

    /**
     * 位置信息
     */
    @Data
    @Schema(description = "位置信息")
    public static class LocationVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "地址")
        private String address;

        @Schema(description = "区域")
        private String district;

        @Schema(description = "距离(米)")
        private Integer distance;

        @Schema(description = "距离显示")
        private String distanceDisplay;
    }

    /**
     * 评价信息
     */
    @Data
    @Schema(description = "评价信息")
    public static class ReviewsVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "总评价数")
        private Integer total;

        @Schema(description = "评价统计")
        private ReviewSummaryVO summary;

        @Schema(description = "评价标签")
        private List<ReviewTagVO> tags;

        @Schema(description = "最近评价")
        private List<ReviewItemVO> recent;
    }

    /**
     * 评价统计
     */
    @Data
    @Schema(description = "评价统计")
    public static class ReviewSummaryVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "好评数")
        private Integer excellent;

        @Schema(description = "中评数")
        private Integer positive;

        @Schema(description = "差评数")
        private Integer negative;
    }

    /**
     * 评价标签
     */
    @Data
    @Schema(description = "评价标签")
    public static class ReviewTagVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "标签文本")
        private String text;

        @Schema(description = "出现次数")
        private Integer count;
    }

    /**
     * 评价项
     */
    @Data
    @Schema(description = "评价项")
    public static class ReviewItemVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "评价ID")
        private Long reviewId;

        @Schema(description = "评价者信息")
        private ReviewerVO reviewer;

        @Schema(description = "评分(1-5)")
        private Integer rating;

        @Schema(description = "评价内容")
        private String content;

        @Schema(description = "评价图片")
        private List<String> images;

        @Schema(description = "评价时间")
        private LocalDateTime createdAt;

        @Schema(description = "商家回复")
        private String reply;
    }

    /**
     * 评价者信息
     */
    @Data
    @Schema(description = "评价者信息")
    public static class ReviewerVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "头像URL")
        private String avatar;

        @Schema(description = "昵称")
        private String nickname;
    }
}
