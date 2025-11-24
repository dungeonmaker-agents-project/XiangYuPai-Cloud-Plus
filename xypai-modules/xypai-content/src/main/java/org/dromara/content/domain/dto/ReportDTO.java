package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 举报请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "举报请求")
public class ReportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "目标类型: feed=动态, comment=评论, user=用户", example = "feed", required = true)
    @NotBlank(message = "目标类型不能为空")
    @Pattern(regexp = "^(feed|comment|user)$", message = "目标类型必须是feed、comment或user")
    private String targetType;

    @Schema(description = "目标ID", example = "123456", required = true)
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @Schema(description = "举报类型: harassment=骚扰, pornography=色情, fraud=诈骗, illegal=违法, spam=垃圾, other=其他", example = "harassment", required = true)
    @NotBlank(message = "举报类型不能为空")
    @Pattern(regexp = "^(harassment|pornography|fraud|illegal|spam|other)$", message = "举报类型无效")
    private String reasonType;

    @Schema(description = "举报描述(0-200字符)", example = "该内容包含不当言论")
    @Size(max = 200, message = "举报描述不能超过200字符")
    private String description;

    @Schema(description = "举报图片URL列表(最多3张)", example = "[\"https://example.com/image1.jpg\"]")
    @Size(max = 3, message = "最多上传3张举报图片")
    private List<String> evidenceImages;

}
