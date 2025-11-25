package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发布组局配置VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "发布组局配置")
public class ActivityPublishConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动类型列表")
    private List<ActivityTypeVO> activityTypes;

    @Schema(description = "性别限制选项")
    private List<OptionVO> genderOptions;

    @Schema(description = "人数限制配置")
    private MemberLimitConfig memberLimitConfig;

    @Schema(description = "费用配置")
    private FeeConfig feeConfig;

    @Schema(description = "热门标签推荐")
    private List<String> hotTags;

    @Schema(description = "发布规则说明")
    private List<String> publishRules;

    @Schema(description = "当前用户是否可发布")
    private Boolean canPublish;

    @Schema(description = "不可发布原因")
    private String cannotPublishReason;

    @Schema(description = "用户剩余免费发布次数")
    private Integer freePublishRemaining;

    @Schema(description = "发布活动价格(超出免费次数后)")
    private BigDecimal publishPrice;

    /**
     * 活动类型
     */
    @Data
    @Schema(description = "活动类型")
    public static class ActivityTypeVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "类型代码")
        private String code;

        @Schema(description = "类型名称")
        private String name;

        @Schema(description = "图标URL")
        private String iconUrl;

        @Schema(description = "是否热门")
        private Boolean isHot;

        @Schema(description = "默认标签")
        private List<String> defaultTags;
    }

    /**
     * 通用选项
     */
    @Data
    @Schema(description = "通用选项")
    public static class OptionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "选项值")
        private String value;

        @Schema(description = "显示文本")
        private String label;
    }

    /**
     * 人数限制配置
     */
    @Data
    @Schema(description = "人数限制配置")
    public static class MemberLimitConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "最小人数下限")
        private Integer minLimit;

        @Schema(description = "最大人数上限")
        private Integer maxLimit;

        @Schema(description = "默认最小人数")
        private Integer defaultMin;

        @Schema(description = "默认最大人数")
        private Integer defaultMax;
    }

    /**
     * 费用配置
     */
    @Data
    @Schema(description = "费用配置")
    public static class FeeConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "是否允许收费")
        private Boolean allowPaid;

        @Schema(description = "最低收费金额")
        private BigDecimal minFee;

        @Schema(description = "最高收费金额")
        private BigDecimal maxFee;

        @Schema(description = "平台服务费率", example = "0.05")
        private BigDecimal platformFeeRate;

        @Schema(description = "平台服务费说明")
        private String platformFeeDescription;
    }
}
