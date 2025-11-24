package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 用户举报DTO
 * User Report DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Report user request")
public class UserReportDto {

    @NotNull(message = "被举报用户ID不能为空")
    @Schema(description = "Reported user ID")
    private Long reportedUserId;

    @Schema(description = "Report type")
    private Integer reportType;

    @Schema(description = "Report reason")
    private String reportReason;

    @NotBlank(message = "举报原因不能为空")
    @Schema(description = "Reason: spam, abuse, inappropriate, fraud, other")
    private String reason;

    @Size(max = 500, message = "描述不能超过500字符")
    @Schema(description = "Description")
    private String description;

    @Schema(description = "Evidence (image URLs, comma separated)")
    private String evidence;

    @Schema(description = "Report images (URLs list)")
    private java.util.List<String> reportImages;
}
