package com.xypai.chat.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 参与者操作DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "参与者操作DTO")
public class ParticipantOperationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", required = true)
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 用户ID列表
     */
    @Schema(description = "用户ID列表", required = true)
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 操作类型(add=添加, remove=移除, quit=退出)
     */
    @Schema(description = "操作类型")
    private String operationType;

    /**
     * 操作原因
     */
    @Schema(description = "操作原因")
    private String reason;
}

