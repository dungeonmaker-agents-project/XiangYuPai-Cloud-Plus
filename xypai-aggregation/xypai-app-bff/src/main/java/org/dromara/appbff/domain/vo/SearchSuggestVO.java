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
 * 搜索建议响应VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索建议")
public class SearchSuggestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "建议列表")
    private List<SuggestionItem> suggestions;

    /**
     * 建议项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索建议项")
    public static class SuggestionItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "建议文本", example = "王者荣耀陪玩")
        private String text;

        @Schema(description = "类型: user(用户), topic(话题), keyword(关键词)", example = "keyword")
        private String type;

        @Schema(description = "高亮文本（匹配部分）", example = "王者")
        private String highlight;

        @Schema(description = "图标", example = "🔍")
        private String icon;

        @Schema(description = "额外信息", example = "123条结果")
        private String extra;
    }
}
