package com.xypai.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 一级评论ID
     */
    private Long parentId;

    /**
     * 回复的评论ID
     */
    private Long replyToId;

    /**
     * 被回复的用户ID
     */
    private Long replyToUserId;

    /**
     * 被回复用户昵称
     */
    private String replyToUserNickname;

    /**
     * 评论内容
     */
    private String commentText;

    /**
     * 点赞数量
     */
    private Integer likeCount;

    /**
     * 回复数量
     */
    private Integer replyCount;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 二级回复列表（仅一级评论返回，最多3条）
     */
    private List<CommentVO> replies;

    /**
     * 二级回复总数
     */
    private Integer totalReplies;

    /**
     * 是否有更多回复
     */
    private Boolean hasMoreReplies;
}

