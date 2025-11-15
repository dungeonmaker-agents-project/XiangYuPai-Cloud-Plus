package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建技能DTO
 * Create Skill DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Create skill request")
public class SkillCreateDto {

    @NotBlank(message = "技能名称不能为空")
    @Size(min = 2, max = 50, message = "技能名称长度为2-50字符")
    @Schema(description = "Skill name")
    private String skillName;

    @NotBlank(message = "技能类型不能为空")
    @Schema(description = "Skill type: online, offline")
    private String skillType;

    @Schema(description = "Cover image URL")
    private String coverImage;

    @NotBlank(message = "技能介绍不能为空")
    @Size(min = 10, max = 500, message = "技能介绍长度为10-500字符")
    @Schema(description = "Skill description")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Schema(description = "Price")
    private BigDecimal price;

    @NotBlank(message = "价格单位不能为空")
    @Schema(description = "Price unit: 局, 小时")
    private String priceUnit;

    // === Online Skill Fields ===
    @Schema(description = "Game name (for online skills)")
    private String gameName;

    @Schema(description = "Game rank (for online skills)")
    private String gameRank;

    @Schema(description = "Service hours (for online skills)")
    private BigDecimal serviceHours;

    // === Offline Skill Fields ===
    @Schema(description = "Service type (for offline skills)")
    private String serviceType;

    @Schema(description = "Service location (for offline skills)")
    private String serviceLocation;

    @Schema(description = "Latitude (for offline skills)")
    private BigDecimal latitude;

    @Schema(description = "Longitude (for offline skills)")
    private BigDecimal longitude;

    // === Additional Fields ===
    @Schema(description = "Skill images (URLs)")
    private List<String> images;

    @Schema(description = "Service promises")
    private List<String> promises;

    @Schema(description = "Available times")
    private List<AvailableTimeDto> availableTimes;
}
