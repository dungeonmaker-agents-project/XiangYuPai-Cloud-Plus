package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发现页点赞请求DTO
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
@Schema(description = "发现页点赞请求")
public class DiscoverLikeDTO {

    @Schema(description = "内容ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @Schema(description = "操作类型: like(点赞), unlike(取消点赞)", example = "like", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "操作类型不能为空")
    private String action;

}
