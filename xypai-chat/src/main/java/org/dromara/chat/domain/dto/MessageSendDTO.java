package org.dromara.chat.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发送消息DTO
 * Send Message Request
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息类型: text/image/voice/video
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 消息内容 (文字消息必填)
     */
    @Size(max = 500, message = "消息内容不能超过500字符")
    private String content;

    /**
     * 媒体URL (图片/语音/视频消息必填)
     */
    private String mediaUrl;

    /**
     * 缩略图URL (视频消息必填)
     */
    private String thumbnailUrl;

    /**
     * 时长 (语音/视频消息必填，单位：秒)
     */
    private Integer duration;
}
