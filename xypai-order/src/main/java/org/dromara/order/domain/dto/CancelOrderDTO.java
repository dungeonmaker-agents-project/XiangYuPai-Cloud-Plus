package org.dromara.order.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 取消订单DTO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    /**
     * 取消原因（可选）
     */
    private String reason;
}
