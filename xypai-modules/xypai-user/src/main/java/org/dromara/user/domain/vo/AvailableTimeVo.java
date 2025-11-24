package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 可用时间VO
 * Available Time VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Available time slot")
public class AvailableTimeVo {

    @Schema(description = "Day of week (1-7, 1=Monday, 7=Sunday)")
    private Integer dayOfWeek;

    @Schema(description = "Start time")
    private LocalTime startTime;

    @Schema(description = "End time")
    private LocalTime endTime;
}
