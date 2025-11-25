package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务列表结果VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "服务列表结果")
public class ServiceListResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "技能类型信息")
    private SkillTypeVO skillType;

    @Schema(description = "Tab列表")
    private List<TabVO> tabs;

    @Schema(description = "筛选配置")
    private FilterConfigVO filters;

    @Schema(description = "总数量")
    private Long total;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "服务列表")
    private List<ServiceCardVO> list;

    /**
     * 技能类型信息
     */
    @Data
    @Schema(description = "技能类型信息")
    public static class SkillTypeVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "类型代码")
        private String type;

        @Schema(description = "显示名称")
        private String label;

        @Schema(description = "图标URL")
        private String icon;
    }

    /**
     * Tab信息
     */
    @Data
    @Schema(description = "Tab信息")
    public static class TabVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Tab值")
        private String value;

        @Schema(description = "显示文本")
        private String label;

        @Schema(description = "数量")
        private Integer count;
    }

    /**
     * 筛选配置
     */
    @Data
    @Schema(description = "筛选配置")
    public static class FilterConfigVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "排序选项")
        private List<OptionVO> sortOptions;

        @Schema(description = "性别选项")
        private List<OptionVO> genderOptions;

        @Schema(description = "状态选项")
        private List<OptionVO> statusOptions;

        @Schema(description = "游戏大区选项")
        private List<OptionVO> gameAreas;

        @Schema(description = "段位选项")
        private List<OptionVO> ranks;

        @Schema(description = "价格区间选项")
        private List<PriceRangeVO> priceRanges;

        @Schema(description = "位置选项")
        private List<OptionVO> positions;

        @Schema(description = "标签选项")
        private List<OptionVO> tags;
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
     * 价格区间选项
     */
    @Data
    @Schema(description = "价格区间选项")
    public static class PriceRangeVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "选项值")
        private String value;

        @Schema(description = "显示文本")
        private String label;

        @Schema(description = "最小价格")
        private BigDecimal min;

        @Schema(description = "最大价格(null表示无上限)")
        private BigDecimal max;
    }
}
