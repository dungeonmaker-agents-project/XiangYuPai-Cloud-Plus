package org.dromara.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Conversation Entity
 * 会话表 - 用户私信会话记录
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("conversation")
public class Conversation extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    @Builder.Default
    private Integer unreadCount = 0;

    /**
     * 是否已删除 (逻辑删除)
     */
    @TableLogic
    @Builder.Default
    private Integer deleted = 0;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 版本号 (乐观锁)
     */
    @Version
    @Builder.Default
    private Integer version = 0;
}
