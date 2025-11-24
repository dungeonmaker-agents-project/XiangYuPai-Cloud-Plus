package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关系实体
 * 对应表: user_relations
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_relations")
public class UserRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID（主键）
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    /**
     * 关注者ID（谁关注）
     */
    @TableField("follower_id")
    private Long followerId;

    /**
     * 被关注者ID（被谁关注）
     */
    @TableField("following_id")
    private Long followingId;

    /**
     * 关系状态: active, blocked
     */
    @TableField("status")
    private String status;

    /**
     * 关注时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
