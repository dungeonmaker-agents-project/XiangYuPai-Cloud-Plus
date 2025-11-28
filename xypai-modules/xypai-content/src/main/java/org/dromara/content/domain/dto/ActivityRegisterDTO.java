package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 报名活动请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "报名活动请求")
public class ActivityRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID", example = "1001")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "报名留言", example = "期待参加！")
    private String message;
}
