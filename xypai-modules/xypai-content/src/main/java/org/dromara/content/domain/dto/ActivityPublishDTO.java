package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布活动请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "发布活动请求")
public class ActivityPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动类型编码", example = "billiards")
    @NotBlank(message = "活动类型不能为空")
    private String typeCode;

    @Schema(description = "活动标题", example = "周末台球局")
    @NotBlank(message = "活动标题不能为空")
    private String title;

    @Schema(description = "活动描述", example = "欢迎大家来参加台球活动")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "活动图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "活动开始时间", example = "2025-12-01T14:00:00")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", example = "2025-12-01T18:00:00")
    private LocalDateTime endTime;

    @Schema(description = "报名截止时间")
    private LocalDateTime registrationDeadline;

    @Schema(description = "地点名称", example = "星球台球俱乐部")
    @NotBlank(message = "地点名称不能为空")
    private String locationName;

    @Schema(description = "详细地址", example = "深圳市南山区科技园南路88号")
    private String locationAddress;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区县", example = "南山区")
    private String district;

    @Schema(description = "经度", example = "113.9430")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "22.5440")
    private BigDecimal latitude;

    @Schema(description = "最少人数", example = "2")
    private Integer minMembers = 2;

    @Schema(description = "最多人数", example = "6")
    @NotNull(message = "最多人数不能为空")
    private Integer maxMembers;

    @Schema(description = "性别限制: all=不限, male=仅男, female=仅女", example = "all")
    private String genderLimit = "all";

    @Schema(description = "是否收费", example = "false")
    private Boolean isPaid = false;

    @Schema(description = "费用金额(元/人)", example = "30.00")
    private BigDecimal fee;

    @Schema(description = "费用说明", example = "包含场地费和饮料")
    private String feeDescription;

    @Schema(description = "是否需要审核", example = "false")
    private Boolean needApproval = false;

    @Schema(description = "联系方式", example = "微信: xxx")
    private String contactInfo;

    @Schema(description = "标签列表", example = "[\"新手友好\", \"周末局\"]")
    private List<String> tags;
}
