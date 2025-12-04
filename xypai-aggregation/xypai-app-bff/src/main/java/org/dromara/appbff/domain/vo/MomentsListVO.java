package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户动态列表VO
 * 对应UI文档中的 MomentsListData
 *
 * @author XyPai Team
 * @date 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户动态列表")
public class MomentsListVO {

    @Schema(description = "动态列表")
    private List<MomentItemVO> list;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "总数")
    private Long total;

    /**
     * 动态项VO
     * 对应UI文档中的 MomentItem
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "动态项")
    public static class MomentItemVO {

        @Schema(description = "动态ID")
        private String id;

        @Schema(description = "媒体类型: image/video")
        private String type;

        @Schema(description = "媒体数据")
        private MediaDataVO mediaData;

        @Schema(description = "文本数据")
        private TextDataVO textData;

        @Schema(description = "作者数据")
        private AuthorDataVO authorData;

        @Schema(description = "统计数据")
        private StatsDataVO statsData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "媒体数据")
    public static class MediaDataVO {

        @Schema(description = "封面图URL")
        private String coverUrl;

        @Schema(description = "宽高比")
        private BigDecimal aspectRatio;

        @Schema(description = "视频时长(秒)，仅video类型有效")
        private Integer duration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "文本数据")
    public static class TextDataVO {

        @Schema(description = "标题")
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "作者数据")
    public static class AuthorDataVO {

        @Schema(description = "用户ID")
        private String userId;

        @Schema(description = "头像URL")
        private String avatar;

        @Schema(description = "昵称")
        private String nickname;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "统计数据")
    public static class StatsDataVO {

        @Schema(description = "点赞数")
        private Integer likeCount;

        @Schema(description = "当前用户是否已点赞")
        private Boolean isLiked;
    }
}
