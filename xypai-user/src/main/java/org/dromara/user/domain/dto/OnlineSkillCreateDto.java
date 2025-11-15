package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建线上技能DTO
 * Create Online Skill DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Create online skill request")
public class OnlineSkillCreateDto {

    @Schema(description = "Cover image URL")
    private String coverImage;

    @NotBlank(message = "游戏名称不能为空")
    @Schema(description = "Game name")
    private String gameName;

    @NotBlank(message = "游戏段位不能为空")
    @Schema(description = "Game rank")
    private String gameRank;

    @NotBlank(message = "技能名称不能为空")
    @Size(min = 2, max = 50, message = "技能名称长度为2-50字符")
    @Schema(description = "Skill name")
    private String skillName;

    @NotBlank(message = "技能介绍不能为空")
    @Size(min = 10, max = 500, message = "技能介绍长度为10-500字符")
    @Schema(description = "Description")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Schema(description = "Price")
    private BigDecimal price;

    @NotNull(message = "服务时长不能为空")
    @Schema(description = "Service hours per match/session")
    private BigDecimal serviceHours;

    @Schema(description = "Skill images (URLs)")
    private List<String> images;

    @Schema(description = "Service promises")
    private List<String> promises;

    @Schema(description = "Is online (上架状态)")
    private Boolean isOnline;
}
