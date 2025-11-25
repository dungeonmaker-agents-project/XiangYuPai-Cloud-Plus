package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 活动列表结果VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "活动列表结果")
public class ActivityListResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动列表")
    private List<ActivityCardVO> list;

    @Schema(description = "总数量")
    private Long total;

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "每页数量")
    private Integer pageSize;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "是否有下一页")
    private Boolean hasNext;

    @Schema(description = "筛选统计信息")
    private FilterStatsVO filterStats;

    /**
     * 筛选统计信息
     */
    @Data
    @Schema(description = "筛选统计信息")
    public static class FilterStatsVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "总活动数")
        private Integer totalActivities;

        @Schema(description = "今日新增")
        private Integer todayNew;

        @Schema(description = "本周热门活动数")
        private Integer weeklyHot;
    }
}
