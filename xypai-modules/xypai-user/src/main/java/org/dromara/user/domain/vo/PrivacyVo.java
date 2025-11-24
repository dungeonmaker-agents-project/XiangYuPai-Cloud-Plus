package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 隐私设置VO
 * Privacy Settings VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Privacy settings")
public class PrivacyVo {

    @Schema(description = "Can view profile")
    private Boolean canViewProfile;

    @Schema(description = "Can view moments")
    private Boolean canViewMoments;

    @Schema(description = "Can view skills")
    private Boolean canViewSkills;
}
