package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新性别DTO
 * Update Gender DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Update gender request")
public class UpdateGenderDto {

    @Schema(description = "Gender: male, female, other")
    private String gender;
}
