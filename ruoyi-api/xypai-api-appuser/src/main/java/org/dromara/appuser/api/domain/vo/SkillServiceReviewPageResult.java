package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 技能服务评价分页结果 (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillServiceReviewPageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 评价摘要
     */
    private ReviewSummaryVo summary;

    /**
     * 评价标签
     */
    private List<ReviewTagVo> tags;

    /**
     * 评价列表
     */
    private List<SkillServiceReviewVo> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 评价摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSummaryVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 优秀评价数 (5星)
         */
        private Integer excellent;

        /**
         * 好评数 (4星)
         */
        private Integer positive;

        /**
         * 差评数 (1-3星)
         */
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

        /**
         * 标签文本
         */
        private String text;

        /**
         * 标签数量
         */
        private Integer count;
    }
}
