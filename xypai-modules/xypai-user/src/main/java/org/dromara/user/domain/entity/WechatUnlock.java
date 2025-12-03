package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信解锁记录实体
 * 对应表: wechat_unlocks
 *
 * @author XyPai Team
 * @since 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("wechat_unlocks")
public class WechatUnlock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 解锁记录ID（主键）
     */
    @TableId(value = "unlock_id", type = IdType.AUTO)
    private Long unlockId;

    /**
     * 解锁者用户ID（花费金币的用户）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 被解锁者用户ID（微信被查看的用户）
     */
    @TableField("target_user_id")
    private Long targetUserId;

    /**
     * 消耗金币数（VIP免费解锁时为0）
     */
    @TableField("cost_coins")
    private Integer costCoins;

    /**
     * 解锁方式: coins-金币, vip-VIP免费
     */
    @TableField("unlock_type")
    private String unlockType;

    /**
     * 解锁时间
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
