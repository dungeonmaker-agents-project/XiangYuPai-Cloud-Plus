package org.dromara.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态详情VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动态详情")
public class FeedDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "动态ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "动态类型")
    private Integer type;

    @Schema(description = "动态类型描述: 动态/活动/技能")
    private String typeDesc;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "内容摘要(截取前100字符)")
    private String summary;

    @Schema(description = "用户信息")
    private FeedListVO.UserInfoVO userInfo;

    @Schema(description = "媒体列表")
    private List<FeedListVO.MediaVO> mediaList;

    @Schema(description = "话题列表")
    private List<FeedListVO.TopicVO> topicList;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "详细地址")
    private String locationAddress;

    @Schema(description = "距离(km)")
    private Double distance;

    @Schema(description = "城市ID")
    private Long cityId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "分享数")
    private Integer shareCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已收藏")
    private Boolean isCollected;

    @Schema(description = "是否可编辑")
    private Boolean canEdit;

    @Schema(description = "是否可删除")
    private Boolean canDelete;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
