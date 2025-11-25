package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 筛选配置VO (用于RPC传输)
 * <p>
 * 返回数据库中实际存在的技能、段位、游戏名称等选项
 *
 * @author XyPai Team
 * @date 2025-11-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能类型 (online/offline)
     */
    private String type;

    /**
     * 技能/段位选项列表 (从skills表聚合)
     */
    private List<SkillOptionVo> skillOptions;

    /**
     * 游戏名称列表 (线上技能专用)
     */
    private List<String> gameNames;

    /**
     * 段位列表 (线上技能专用)
     */
    private List<String> gameRanks;

    /**
     * 服务类型列表 (线下技能专用)
     */
    private List<String> serviceTypes;

    /**
     * 价格范围统计
     */
    private PriceRangeVo priceRange;

    /**
     * 技能选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillOptionVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 技能值 (用于筛选)
         */
        private String value;

        /**
         * 显示标签
         */
        private String label;

        /**
         * 分类 (游戏名称或服务类型)
         */
        private String category;

        /**
         * 该选项的用户数量
         */
        private Integer count;
    }

    /**
     * 价格范围统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRangeVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 最低价格
         */
        private Integer minPrice;

        /**
         * 最高价格
         */
        private Integer maxPrice;
    }
}
