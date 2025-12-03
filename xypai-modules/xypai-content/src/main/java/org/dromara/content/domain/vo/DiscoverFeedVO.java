package org.dromara.content.domain.vo;

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
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发现页内容项")
public class DiscoverFeedVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "内容ID")
    private Long id;

    @Schema(description = "内容类型: image/video")
    private String type;

    // ========== 媒体数据 ==========

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "宽高比")
    private BigDecimal aspectRatio;

    @Schema(description = "视频时长(秒)")
    private Integer duration;

    @Schema(description = "媒体宽度")
    private Integer width;

    @Schema(description = "媒体高度")
    private Integer height;

    // ========== 文本数据 ==========

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    // ========== 作者数据 ==========

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "作者头像")
    private String userAvatar;

    @Schema(description = "作者昵称")
    private String userNickname;

    // ========== 统计数据 ==========

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

    // ========== 元信息 ==========

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "距离(米)")
    private Double distance;

}
