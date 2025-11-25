package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 筛选结果响应VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "筛选结果")
public class FilterResultVO {

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "用户列表")
    private List<UserCardVO> list;

    @Schema(description = "已应用的筛选条件")
    private AppliedFilters appliedFilters;

    /**
     * 已应用的筛选条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "已应用的筛选条件")
    public static class AppliedFilters {
        @Schema(description = "筛选条件数量")
        private Integer count;

        @Schema(description = "筛选条件摘要")
        private List<String> summary;
    }
}
