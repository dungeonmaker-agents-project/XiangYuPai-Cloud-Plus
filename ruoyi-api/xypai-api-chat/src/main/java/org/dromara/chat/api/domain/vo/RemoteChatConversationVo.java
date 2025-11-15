package org.dromara.chat.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Remote Chat Conversation VO
 * 远程调用-会话VO
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteChatConversationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Conversation ID
     * 会话ID
     */
    private Long conversationId;

    /**
     * User ID (the owner of this conversation)
     * 用户ID（会话所有者）
     */
    private Long userId;

    /**
     * Other user ID (the chat partner)
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * Last message content
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * Last message time
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * Unread message count
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * Is other user online
     * 对方是否在线
     */
    private Boolean isOnline;

    /**
     * Create time
     * 创建时间
     */
    private LocalDateTime createTime;
}
