package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

/**
 * 可用时间DTO
 * Available Time DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Available time slot")
public class AvailableTimeDto {

    @NotNull(message = "星期不能为空")
    @Min(value = 1, message = "星期范围为1-7")
    @Max(value = 7, message = "星期范围为1-7")
    @Schema(description = "Day of week (1-7, 1=Monday, 7=Sunday)")
    private Integer dayOfWeek;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "Start time")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "End time")
    private LocalTime endTime;
}
