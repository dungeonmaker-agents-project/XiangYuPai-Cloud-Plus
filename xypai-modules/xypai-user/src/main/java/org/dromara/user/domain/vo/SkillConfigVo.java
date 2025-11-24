package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技能配置VO
 * Skill Configuration VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Skill configuration")
public class SkillConfigVo {

    @Schema(description = "Game list")
    private List<GameInfo> games;

    @Schema(description = "Service types")
    private List<ServiceTypeInfo> serviceTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Game information")
    public static class GameInfo {

        @Schema(description = "Game ID")
        private String gameId;

        @Schema(description = "Game name")
        private String gameName;

        @Schema(description = "Rank list")
        private List<String> ranks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Service type information")
    public static class ServiceTypeInfo {

        @Schema(description = "Type ID")
        private String typeId;

        @Schema(description = "Type name")
        private String typeName;

        @Schema(description = "Icon URL")
        private String icon;
    }
}
