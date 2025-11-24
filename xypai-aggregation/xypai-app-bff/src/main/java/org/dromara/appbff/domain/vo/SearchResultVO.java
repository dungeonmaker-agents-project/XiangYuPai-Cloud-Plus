package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 搜索结果响应VO（通用）
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索结果")
public class SearchResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词", example = "王者荣耀")
    private String keyword;

    @Schema(description = "总记录数", example = "123")
    private Integer total;

    @Schema(description = "是否有更多数据", example = "true")
    private Boolean hasMore;

    @Schema(description = "Tab统计信息")
    private List<TabInfo> tabs;

    @Schema(description = "搜索结果列表（根据type返回不同结构）")
    private Object results;

    /**
     * Tab统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tab统计信息")
    public static class TabInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Tab类型", example = "all")
        private String type;

        @Schema(description = "Tab标签", example = "全部")
        private String label;

        @Schema(description = "结果数量", example = "123")
        private Integer count;
    }
}
