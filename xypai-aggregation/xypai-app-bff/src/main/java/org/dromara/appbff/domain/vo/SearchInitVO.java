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
 * 搜索初始化数据响应VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索初始化数据")
public class SearchInitVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索历史")
    private List<SearchHistoryItem> searchHistory;

    @Schema(description = "热门搜索")
    private List<HotKeywordItem> hotKeywords;

    @Schema(description = "搜索框占位符", example = "搜索更多")
    private String placeholder;

    /**
     * 搜索历史项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索历史项")
    public static class SearchHistoryItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "关键词", example = "王者荣耀")
        private String keyword;

        @Schema(description = "搜索时间", example = "2025-11-24 10:30:00")
        private String searchTime;

        @Schema(description = "类型: user(用户), topic(话题), keyword(关键词)", example = "keyword")
        private String type;
    }

    /**
     * 热门搜索项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热门搜索项")
    public static class HotKeywordItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "关键词", example = "王者荣耀")
        private String keyword;

        @Schema(description = "排名", example = "1")
        private Integer rank;

        @Schema(description = "是否热门", example = "true")
        private Boolean isHot;
    }
}
