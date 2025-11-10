package com.xypai.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体
 * 
 * 支持一级评论和二级回复：
 * - 一级评论：parent_id = null
 * - 二级回复：parent_id = 一级评论ID, reply_to_id = 直接回复的评论ID
 * 
 * 示例：
 * A发表评论（一级）
 *   ├─ B回复A（二级，parent_id=A.id, reply_to_id=A.id）
 *   └─ C回复B（二级，parent_id=A.id, reply_to_id=B.id）
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment")
public class Comment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 内容ID
     */
    @TableField("content_id")
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 评论用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 一级评论ID（二级回复时使用）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 直接回复的评论ID
     */
    @TableField("reply_to_id")
    private Long replyToId;

    /**
     * 被回复的用户ID
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 评论内容
     */
    @TableField("comment_text")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字符")
    private String commentText;

    /**
     * 点赞数量
     */
    @TableField("like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 回复数量
     */
    @TableField("reply_count")
    @Builder.Default
    private Integer replyCount = 0;

    /**
     * 是否置顶
     */
    @TableField("is_top")
    @Builder.Default
    private Boolean isTop = false;

    /**
     * 评论状态(0=删除,1=正常,2=审核,3=屏蔽)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 评论时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 评论状态枚举
     */
    public enum Status {
        DELETED(0, "已删除"),
        NORMAL(1, "正常"),
        REVIEWING(2, "审核中"),
        BLOCKED(3, "已屏蔽");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

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

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }
}

