package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户黑名单实体
 * 对应表: user_blacklist
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_blacklist")
public class UserBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 黑名单ID（主键）
     */
    @TableId(value = "blacklist_id", type = IdType.AUTO)
    private Long blacklistId;

    /**
     * 用户ID（拉黑发起者）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 被拉黑用户ID
     */
    @TableField("blocked_user_id")
    private Long blockedUserId;

    /**
     * 拉黑原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
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
