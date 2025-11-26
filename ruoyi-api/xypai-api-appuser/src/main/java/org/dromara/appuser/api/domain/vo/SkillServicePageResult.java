package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 技能服务分页结果 (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillServicePageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能类型信息
     */
    private SkillTypeInfo skillType;

    /**
     * Tab 列表
     */
    private List<TabInfo> tabs;

    /**
     * 筛选配置
     */
    private FilterConfig filters;

    /**
     * 数据列表
     */
    private List<SkillServiceVo> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 是否有更多
     */
    private Boolean hasMore;

    /**
     * 技能类型信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillTypeInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 类型值 (如: honor_of_kings)
         */
        private String type;

        /**
         * 类型标签 (如: 王者荣耀)
         */
        private String label;

        /**
         * 图标URL
         */
        private String icon;
    }

    /**
     * Tab信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Tab值
         */
        private String value;

        /**
         * Tab标签
         */
        private String label;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 筛选配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 排序选项
         */
        private List<OptionInfo> sortOptions;

        /**
         * 性别选项
         */
        private List<OptionInfo> genderOptions;

        /**
         * 状态选项
         */
        private List<OptionInfo> statusOptions;

        /**
         * 游戏大区选项
         */
        private List<OptionInfo> gameAreas;

        /**
         * 段位选项
         */
        private List<OptionInfo> ranks;

        /**
         * 价格区间选项
         */
        private List<PriceRangeInfo> priceRanges;

        /**
         * 位置/英雄选项
         */
        private List<OptionInfo> positions;

        /**
         * 标签选项
         */
        private List<OptionInfo> tags;
    }

    /**
     * 选项信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 选项值
         */
        private String value;

        /**
         * 选项标签
         */
        private String label;
    }

    /**
     * 价格区间信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRangeInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 区间值 (如: "0-10", "50+")
         */
        private String value;

        /**
         * 区间标签 (如: "0-10金币")
         */
        private String label;

        /**
         * 最小价格
         */
        private BigDecimal min;

        /**
         * 最大价格 (null表示无上限)
         */
        private BigDecimal max;
    }
}
