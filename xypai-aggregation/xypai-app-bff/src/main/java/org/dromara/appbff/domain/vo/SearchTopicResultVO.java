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
 * 搜索话题Tab结果VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索话题Tab结果")
public class SearchTopicResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数", example = "11")
    private Integer total;

    @Schema(description = "是否有更多数据", example = "false")
    private Boolean hasMore;

    @Schema(description = "话题列表")
    private List<SearchTopicItem> list;

    /**
     * 话题搜索结果项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "话题搜索结果项")
    public static class SearchTopicItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "话题ID", example = "1")
        private Long topicId;

        @Schema(description = "话题名称", example = "王者荣耀")
        private String topicName;

        @Schema(description = "话题图标", example = "🎮")
        private String icon;

        @Schema(description = "话题描述", example = "王者荣耀游戏陪玩与交流")
        private String description;

        @Schema(description = "是否热门", example = "true")
        private Boolean isHot;

        @Schema(description = "热门标签", example = "热门")
        private String hotLabel;

        @Schema(description = "统计信息")
        private TopicStats stats;
    }

    /**
     * 话题统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "话题统计信息")
    public static class TopicStats implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "动态数量", example = "1234")
        private Integer posts;

        @Schema(description = "浏览量", example = "56789")
        private Integer views;
    }
}
