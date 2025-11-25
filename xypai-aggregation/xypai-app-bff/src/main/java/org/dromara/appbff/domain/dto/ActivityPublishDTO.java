package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布组局请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "发布组局请求")
public class ActivityPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动类型", example = "billiards", required = true)
    @NotBlank(message = "活动类型不能为空")
    private String activityType;

    @Schema(description = "活动标题", example = "周末台球局", required = true)
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 50, message = "活动标题最多50字")
    private String title;

    @Schema(description = "活动描述", example = "一起来打台球，新手老手都欢迎")
    @Size(max = 500, message = "活动描述最多500字")
    private String description;

    @Schema(description = "活动封面图片ID")
    private String coverImageId;

    @Schema(description = "活动开始时间", required = true)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", required = true)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "活动地点名称", example = "星球台球俱乐部", required = true)
    @NotBlank(message = "活动地点不能为空")
    private String locationName;

    @Schema(description = "详细地址", example = "南山区科技园南路88号")
    private String locationAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "城市代码", example = "440300")
    private String cityCode;

    @Schema(description = "区域代码", example = "440305")
    private String districtCode;

    @Schema(description = "最少人数", example = "2", required = true)
    @NotNull(message = "最少人数不能为空")
    @Min(value = 2, message = "最少人数不能低于2人")
    private Integer minMembers;

    @Schema(description = "最多人数", example = "8", required = true)
    @NotNull(message = "最多人数不能为空")
    @Max(value = 50, message = "最多人数不能超过50人")
    private Integer maxMembers;

    @Schema(description = "性别限制: all(不限), male(仅男), female(仅女)", example = "all")
    private String genderLimit;

    @Schema(description = "是否收费", example = "true")
    private Boolean isPaid;

    @Schema(description = "费用金额(元)", example = "50.00")
    @DecimalMin(value = "0", message = "费用不能为负数")
    private BigDecimal fee;

    @Schema(description = "费用说明", example = "包含场地费和饮料")
    private String feeDescription;

    @Schema(description = "是否需要审核报名", example = "false")
    private Boolean needApproval;

    @Schema(description = "报名截止时间")
    private LocalDateTime registrationDeadline;

    @Schema(description = "活动标签", example = "[\"新手友好\", \"周末局\"]")
    private List<String> tags;

    @Schema(description = "联系方式(可选)")
    private String contactInfo;
}
