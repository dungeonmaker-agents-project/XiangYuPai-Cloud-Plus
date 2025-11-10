package com.xypai.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家回复DTO
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商家回复DTO")
public class ReviewReplyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @Schema(description = "评价ID", required = true)
    @NotNull(message = "评价ID不能为空")
    private Long reviewId;

    /**
     * 回复内容
     */
    @Schema(description = "回复内容（最多500字）", required = true)
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500字")
    private String replyText;
}

