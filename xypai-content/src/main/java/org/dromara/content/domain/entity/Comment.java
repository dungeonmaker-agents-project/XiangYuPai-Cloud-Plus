package org.dromara.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表
 *
 * @author XiangYuPai
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
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 动态ID
     */
    private Long feedId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID(NULL=一级评论)
     */
    private Long parentId;

    /**
     * 回复的用户ID
     */
    private Long replyToUserId;

    /**
     * 点赞数
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer likeCount;

    /**
     * 回复数
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer replyCount;

    /**
     * 是否置顶: 0=否,1=是
     */
    private Integer isTop;

    /**
     * 删除标记: 0=未删除,1=已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 乐观锁
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

}
