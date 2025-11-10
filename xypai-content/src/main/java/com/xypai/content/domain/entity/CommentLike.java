package com.xypai.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞实体
 * 
 * 业务规则：
 * 1. 同一用户对同一评论只能点赞一次（数据库唯一约束）
 * 2. 点赞后可以取消（status=0），再次点赞恢复status=1
 * 3. 评论点赞数通过Comment.like_count字段冗余存储
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment_like")
public class CommentLike implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 点赞记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论ID
     */
    @TableField("comment_id")
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    /**
     * 点赞用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 点赞状态(0=取消,1=正常)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 点赞时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 是否已点赞
     */
    public boolean isLiked() {
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return Integer.valueOf(0).equals(this.status);
    }
}

