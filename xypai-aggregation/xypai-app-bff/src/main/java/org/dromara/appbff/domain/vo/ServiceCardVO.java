package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务卡片VO（列表展示用）
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "服务卡片信息")
public class ServiceCardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "服务ID")
    private Long serviceId;

    @Schema(description = "服务提供者信息")
    private ProviderBriefVO provider;

    @Schema(description = "技能信息")
    private SkillBriefVO skillInfo;

    @Schema(description = "标签列表")
    private List<TagVO> tags;

    @Schema(description = "服务描述")
    private String description;

    @Schema(description = "价格信息")
    private PriceVO price;

    @Schema(description = "距离(米)")
    private Integer distance;

    @Schema(description = "距离显示", example = "3.2km")
    private String distanceDisplay;

    @Schema(description = "统计数据")
    private StatsVO stats;

    /**
     * 服务提供者简要信息
     */
    @Data
    @Schema(description = "服务提供者简要信息")
    public static class ProviderBriefVO implements Serializable {
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

        @Schema(description = "是否认证")
        private Boolean isVerified;
    }

    /**
     * 技能简要信息
     */
    @Data
    @Schema(description = "技能简要信息")
    public static class SkillBriefVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "技能类型")
        private String skillType;

        @Schema(description = "游戏大区")
        private String gameArea;

        @Schema(description = "段位")
        private String rank;

        @Schema(description = "段位分数")
        private Integer rankScore;

        @Schema(description = "擅长位置")
        private List<String> position;
    }

    /**
     * 标签
     */
    @Data
    @Schema(description = "标签")
    public static class TagVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "标签文本")
        private String text;

        @Schema(description = "标签类型")
        private String type;

        @Schema(description = "标签颜色")
        private String color;
    }

    /**
     * 价格信息
     */
    @Data
    @Schema(description = "价格信息")
    public static class PriceVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "单位", example = "局")
        private String unit;

        @Schema(description = "显示文本", example = "10 金币/局")
        private String displayText;
    }

    /**
     * 统计数据
     */
    @Data
    @Schema(description = "统计数据")
    public static class StatsVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "订单数")
        private Integer orders;

        @Schema(description = "评分")
        private BigDecimal rating;

        @Schema(description = "评价数")
        private Integer reviewCount;
    }
}
