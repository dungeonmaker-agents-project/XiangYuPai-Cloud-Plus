package com.xypai.content.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

/**
 * 评论添加DTO
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 一级评论ID（发表二级回复时使用）
     */
    private Long parentId;

    /**
     * 回复的评论ID（发表二级回复时使用）
     */
    private Long replyToId;

    /**
     * 被回复的用户ID（发表二级回复时使用）
     */
    private Long replyToUserId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度必须在1-1000字符之间")
    private String commentText;

    /**
     * 是否为一级评论
     */
    public boolean isFirstLevel() {
        return parentId == null;
    }

    /**
     * 是否为二级回复
     */
    public boolean isSecondLevel() {
        return parentId != null;
    }
}

