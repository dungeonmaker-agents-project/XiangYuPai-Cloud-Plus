package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 拉黑用户DTO
 * Block User DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Block user request")
public class UserBlockDto {

    @NotNull(message = "被拉黑用户ID不能为空")
    @Schema(description = "Blocked user ID")
    private Long blockedUserId;

    @Size(max = 200, message = "拉黑原因不能超过200字符")
    @Schema(description = "Reason for blocking")
    private String reason;
}
