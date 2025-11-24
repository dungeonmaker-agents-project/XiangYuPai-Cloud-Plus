package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新生日DTO
 * Update Birthday DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Update birthday request")
public class UpdateBirthdayDto {

    @Schema(description = "Birthday")
    private LocalDate birthday;
}
