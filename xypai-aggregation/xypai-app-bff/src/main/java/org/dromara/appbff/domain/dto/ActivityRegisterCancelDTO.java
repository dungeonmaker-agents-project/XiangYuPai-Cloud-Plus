package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 取消活动报名请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "取消活动报名请求")
public class ActivityRegisterCancelDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "取消原因", example = "临时有事")
    @Size(max = 200, message = "取消原因最多200字")
    private String reason;
}
