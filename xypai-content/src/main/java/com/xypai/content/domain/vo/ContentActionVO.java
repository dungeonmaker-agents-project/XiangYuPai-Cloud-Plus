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
 * 内容行为VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容行为VO")
public class ContentActionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 行为ID
     */
    @Schema(description = "行为ID")
    private Long id;

    /**
     * 内容ID
     */
    @Schema(description = "内容ID")
    private Long contentId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private ContentListVO.AuthorVO user;

    /**
     * 行为类型
     */
    @Schema(description = "行为类型")
    private Integer action;

    /**
     * 行为类型描述
     */
    @Schema(description = "行为类型描述")
    private String actionDesc;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String commentContent;

    /**
     * 回复的评论ID
     */
    @Schema(description = "回复的评论ID")
    private Long replyToId;

    /**
     * 回复的用户ID
     */
    @Schema(description = "回复的用户ID")
    private Long replyToUserId;

    /**
     * 回复的用户信息
     */
    @Schema(description = "回复的用户信息")
    private ContentListVO.AuthorVO replyToUser;

    /**
     * 回复的评论内容
     */
    @Schema(description = "回复的评论内容")
    private String replyToContent;

    /**
     * 报名信息
     */
    @Schema(description = "报名信息")
    private Map<String, Object> signupInfo;

    /**
     * 行为时间
     */
    @Schema(description = "行为时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 该评论的点赞数
     */
    @Schema(description = "该评论的点赞数")
    private Long likeCount;

    /**
     * 该评论的回复数
     */
    @Schema(description = "该评论的回复数")
    private Long replyCount;

    /**
     * 当前用户是否已点赞该评论
     */
    @Schema(description = "当前用户是否已点赞该评论")
    private Boolean liked;

    /**
     * 子回复列表(用于展示评论的回复)
     */
    @Schema(description = "子回复列表")
    private List<ContentActionVO> replies;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;
}
