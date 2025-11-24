package org.dromara.chat.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天记录查询DTO
 * Chat History Query Request
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于等于1")
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Min(value = 10, message = "每页数量最少10条")
    @Builder.Default
    private Integer pageSize = 20;

    /**
     * 最后一条消息ID (用于分页)
     */
    private Long lastMessageId;
}
