package org.dromara.chat.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Remote Chat Message VO
 * 远程调用-消息VO
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteChatMessageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Message ID
     * 消息ID
     */
    private Long messageId;

    /**
     * Conversation ID
     * 会话ID
     */
    private Long conversationId;

    /**
     * Sender ID
     * 发送者ID
     */
    private Long senderId;

    /**
     * Receiver ID
     * 接收者ID
     */
    private Long receiverId;

    /**
     * Message type: text/image/voice/video
     * 消息类型
     */
    private String messageType;

    /**
     * Message content (for text messages)
     * 消息内容（文字消息）
     */
    private String content;

    /**
     * Media URL (for image/voice/video messages)
     * 媒体URL
     */
    private String mediaUrl;

    /**
     * Thumbnail URL (for video messages)
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * Duration in seconds (for voice/video messages)
     * 时长（秒）
     */
    private Integer duration;

    /**
     * Message status: 0=sending, 1=delivered, 2=read, 3=failed
     * 消息状态
     */
    private Integer status;

    /**
     * Is message recalled
     * 是否已撤回
     */
    private Boolean isRecalled;

    /**
     * Create time
     * 创建时间
     */
    private LocalDateTime createdAt;
}
