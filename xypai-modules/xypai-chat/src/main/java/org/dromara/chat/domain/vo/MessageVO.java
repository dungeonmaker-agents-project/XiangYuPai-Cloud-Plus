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
 * 消息VO
 * Message Response
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型: text/image/voice/video
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 媒体URL
     */
    private String mediaUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 消息状态: 0=发送中, 1=已送达, 2=已读, 3=失败
     */
    private Integer status;

    /**
     * 是否已撤回
     */
    @Builder.Default
    private Boolean isRecalled = false;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
}
