package com.xypai.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 评价创建DTO
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价创建DTO")
public class ReviewCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 综合评分（1.0-5.0）
     */
    @Schema(description = "综合评分（1.0-5.0）", required = true, example = "4.5")
    @NotNull(message = "综合评分不能为空")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingOverall;

    /**
     * 服务评分（1.0-5.0）
     */
    @Schema(description = "服务评分（1.0-5.0）", example = "4.0")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingService;

    /**
     * 态度评分（1.0-5.0）
     */
    @Schema(description = "态度评分（1.0-5.0）", example = "5.0")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingAttitude;

    /**
     * 质量评分（1.0-5.0）
     */
    @Schema(description = "质量评分（1.0-5.0）", example = "4.5")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingQuality;

    /**
     * 评价文字内容
     */
    @Schema(description = "评价文字内容（最多1000字）", example = "服务非常好，强烈推荐！")
    @Size(max = 1000, message = "评价内容不能超过1000字")
    private String reviewText;

    /**
     * 评价图片URL列表
     */
    @Schema(description = "评价图片URL列表（最多9张）")
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> reviewImages;

    /**
     * 是否匿名评价
     */
    @Schema(description = "是否匿名评价", defaultValue = "false")
    private Boolean isAnonymous;
}

