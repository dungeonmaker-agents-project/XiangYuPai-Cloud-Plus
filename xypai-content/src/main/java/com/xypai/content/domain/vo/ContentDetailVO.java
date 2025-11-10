package com.xypai.content.domain.vo;

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
import java.util.Map;

/**
 * 内容详情VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容详情VO")
public class ContentDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    @Schema(description = "内容ID")
    private Long id;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long userId;

    /**
     * 创建者信息
     */
    @Schema(description = "创建者信息")
    private ContentListVO.AuthorVO author;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private Integer type;

    /**
     * 内容类型描述
     */
    @Schema(description = "内容类型描述")
    private String typeDesc;

    /**
     * 内容标题
     */
    @Schema(description = "内容标题")
    private String title;

    /**
     * 内容摘要
     */
    @Schema(description = "内容摘要")
    private String summary;

    /**
     * 内容正文
     */
    @Schema(description = "内容正文")
    private String content;

    /**
     * 封面图片
     */
    @Schema(description = "封面图片")
    private String coverImage;

    /**
     * 图片列表
     */
    @Schema(description = "图片列表")
    private List<String> images;

    /**
     * 视频URL
     */
    @Schema(description = "视频URL")
    private String videoUrl;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 位置信息
     */
    @Schema(description = "位置信息")
    private String location;

    /**
     * 内容状态
     */
    @Schema(description = "内容状态")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Long likeCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    private Long commentCount;

    /**
     * 分享数
     */
    @Schema(description = "分享数")
    private Long shareCount;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    private Long collectCount;

    /**
     * 查看数
     */
    @Schema(description = "查看数")
    private Long viewCount;

    /**
     * 当前用户是否已点赞
     */
    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    /**
     * 当前用户是否已收藏
     */
    @Schema(description = "当前用户是否已收藏")
    private Boolean collected;

    /**
     * 当前用户是否已关注作者
     */
    @Schema(description = "当前用户是否已关注作者")
    private Boolean followedAuthor;

    /**
     * 活动特定信息
     */
    @Schema(description = "活动特定信息")
    private ContentListVO.ActivityInfoVO activityInfo;

    /**
     * 技能特定信息
     */
    @Schema(description = "技能特定信息")
    private ContentListVO.SkillInfoVO skillInfo;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;

    /**
     * 最新评论列表
     */
    @Schema(description = "最新评论列表")
    private List<ContentActionVO> latestComments;

    /**
     * 相关推荐内容
     */
    @Schema(description = "相关推荐内容")
    private List<ContentListVO> relatedContents;
}
