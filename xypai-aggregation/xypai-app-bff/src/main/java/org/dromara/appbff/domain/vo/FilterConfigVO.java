package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 筛选配置响应VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "筛选配置")
public class FilterConfigVO {

    @Schema(description = "年龄范围")
    private AgeRange ageRange;

    @Schema(description = "性别选项")
    private List<Option> genderOptions;

    @Schema(description = "状态选项")
    private List<Option> statusOptions;

    @Schema(description = "技能选项")
    private List<SkillOption> skillOptions;

    @Schema(description = "价格选项(仅线上)")
    private List<PriceOption> priceOptions;

    @Schema(description = "位置选项(仅线上)")
    private List<Option> positionOptions;

    @Schema(description = "标签选项")
    private List<TagOption> tagOptions;

    /**
     * 年龄范围
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "年龄范围")
    public static class AgeRange {
        @Schema(description = "最小年龄", example = "18")
        private Integer min;

        @Schema(description = "最大年龄(null表示不限)")
        private Integer max;
    }

    /**
     * 基础选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "选项")
    public static class Option {
        @Schema(description = "选项值")
        private String value;

        @Schema(description = "显示标签")
        private String label;
    }

    /**
     * 技能选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "技能选项")
    public static class SkillOption {
        @Schema(description = "技能值")
        private String value;

        @Schema(description = "技能标签")
        private String label;

        @Schema(description = "技能类别")
        private String category;
    }

    /**
     * 价格选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "价格选项")
    public static class PriceOption {
        @Schema(description = "价格范围值")
        private String value;

        @Schema(description = "显示标签")
        private String label;

        @Schema(description = "最小价格")
        private Integer min;

        @Schema(description = "最大价格(null表示不限)")
        private Integer max;
    }

    /**
     * 标签选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "标签选项")
    public static class TagOption {
        @Schema(description = "标签值")
        private String value;

        @Schema(description = "标签名称")
        private String label;

        @Schema(description = "是否高亮")
        private Boolean highlighted;
    }
}
