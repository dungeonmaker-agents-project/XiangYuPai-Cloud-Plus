package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 活动报名请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "活动报名请求")
public class ActivityRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "报名留言", example = "我是新手，请多关照")
    @Size(max = 200, message = "报名留言最多200字")
    private String message;

    @Schema(description = "联系电话(可选)")
    private String contactPhone;
}
