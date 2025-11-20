package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建线下技能DTO
 * Create Offline Skill DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Create offline skill request")
public class OfflineSkillCreateDto {

    @Schema(description = "Skill type")
    private Integer skillType;

    @Schema(description = "Service type ID")
    private String serviceTypeId;

    @Schema(description = "Service type name")
    private String serviceTypeName;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Cover image URL")
    private String coverImage;

    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "Service type")
    private String serviceType;

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

    @Schema(description = "Price per service")
    private BigDecimal pricePerService;

    @Schema(description = "Service location (address string)")
    private String serviceLocation;

    @Schema(description = "Latitude")
    private BigDecimal latitude;

    @Schema(description = "Longitude")
    private BigDecimal longitude;

    @NotNull(message = "服务地点不能为空")
    @Schema(description = "Service location")
    private LocationDto location;

    @NotNull(message = "可用时间不能为空")
    @Size(min = 1, message = "至少设置一个可用时间段")
    @Schema(description = "Available times")
    private List<AvailableTimeDto> availableTimes;

    @Schema(description = "Skill images (URLs)")
    private List<String> images;

    @Schema(description = "Service promises")
    private List<String> promises;

    @Schema(description = "Is online (上架状态)")
    private Boolean isOnline;

    @Data
    @Schema(description = "Location information")
    public static class LocationDto {

        @NotBlank(message = "地址不能为空")
        @Schema(description = "Address")
        private String address;

        @NotNull(message = "纬度不能为空")
        @Schema(description = "Latitude")
        private BigDecimal latitude;

        @NotNull(message = "经度不能为空")
        @Schema(description = "Longitude")
        private BigDecimal longitude;
    }
}
