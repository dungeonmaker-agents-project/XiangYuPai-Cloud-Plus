package com.xypai.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款操作DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款操作DTO")
public class RefundDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 退款金额(元) - 不填则全额退款
     */
    @Schema(description = "退款金额(元)")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @Schema(description = "退款原因", required = true)
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因不能超过500个字符")
    private String refundReason;

    /**
     * 申请类型(buyer=买家申请, seller=卖家同意, admin=管理员处理)
     */
    @Schema(description = "申请类型")
    private String applicationType;

    /**
     * 处理说明
     */
    @Schema(description = "处理说明")
    @Size(max = 500, message = "处理说明不能超过500个字符")
    private String processNote;
}
