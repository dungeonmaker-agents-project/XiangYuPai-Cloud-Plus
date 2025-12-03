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

    @Schema(description = "举报类型: insult=辱骂引战, porn=色情低俗, fraud=诈骗, illegal=违法犯罪, fake=不实信息, minor=未成年人相关, uncomfortable=内容引人不适, other=其他", example = "insult", required = true)
    @NotBlank(message = "举报类型不能为空")
    @Pattern(regexp = "^(insult|porn|fraud|illegal|fake|minor|uncomfortable|other)$", message = "举报类型无效")
    private String reasonType;

    @Schema(description = "举报描述(0-200字符)", example = "该内容包含不当言论")
    @Size(max = 200, message = "举报描述不能超过200字符")
    private String description;

    @Schema(description = "举报图片URL列表(最多9张)", example = "[\"https://example.com/image1.jpg\"]")
    @Size(max = 9, message = "最多上传9张举报图片")
    private List<String> evidenceImages;

}
