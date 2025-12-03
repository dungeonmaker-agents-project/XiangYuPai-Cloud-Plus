package org.dromara.appbff.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发现页内容项VO
 * 对应前端 ContentItem 数据模型
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发现页内容项")
public class DiscoverContentItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "内容ID")
    private String id;

    @Schema(description = "内容类型: image/video")
    private String type;

    @Schema(description = "媒体数据")
    private MediaData mediaData;

    @Schema(description = "文本数据")
    private TextData textData;

    @Schema(description = "作者数据")
    private AuthorData authorData;

    @Schema(description = "统计数据")
    private StatsData statsData;

    @Schema(description = "元信息")
    private MetaData metaData;

    /**
     * 媒体数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "媒体数据")
    public static class MediaData implements Serializable {
        @Schema(description = "封面图URL")
        private String coverUrl;

        @Schema(description = "宽高比")
        private BigDecimal aspectRatio;

        @Schema(description = "视频时长(秒)")
        private Integer duration;

        @Schema(description = "宽度")
        private Integer width;

        @Schema(description = "高度")
        private Integer height;
    }

    /**
     * 文本数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "文本数据")
    public static class TextData implements Serializable {
        @Schema(description = "标题")
        private String title;

        @Schema(description = "内容摘要")
        private String content;
    }

    /**
     * 作者数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "作者数据")
    public static class AuthorData implements Serializable {
        @Schema(description = "用户ID")
        private String userId;

        @Schema(description = "头像URL")
        private String avatar;

        @Schema(description = "昵称")
        private String nickname;
    }

    /**
     * 统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "统计数据")
    public static class StatsData implements Serializable {
        @Schema(description = "点赞数")
        private Integer likeCount;

        @Schema(description = "是否已点赞")
        private Boolean isLiked;

        @Schema(description = "评论数")
        private Integer commentCount;

        @Schema(description = "收藏数")
        private Integer collectCount;

        @Schema(description = "是否已收藏")
        private Boolean isCollected;
    }

    /**
     * 元信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "元信息")
    public static class MetaData implements Serializable {
        @Schema(description = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT+8")
        private LocalDateTime createTime;

        @Schema(description = "位置名称")
        private String location;

        @Schema(description = "距离(米)")
        private Double distance;
    }

}
