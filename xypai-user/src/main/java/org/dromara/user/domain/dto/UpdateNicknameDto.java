package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新昵称DTO
 * Update Nickname DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Update nickname request")
public class UpdateNicknameDto {

    @Schema(description = "Nickname")
    private String nickname;
}
