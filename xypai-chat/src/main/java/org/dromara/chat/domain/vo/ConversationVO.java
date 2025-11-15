package org.dromara.chat.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表项VO
 * Conversation List Item Response
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 对方用户ID
     */
    private Long userId;

    /**
     * 对方昵称
     */
    private String nickname;

    /**
     * 对方头像URL
     */
    private String avatar;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    @Builder.Default
    private Integer unreadCount = 0;

    /**
     * 对方是否在线
     */
    @Builder.Default
    private Boolean isOnline = false;
}
