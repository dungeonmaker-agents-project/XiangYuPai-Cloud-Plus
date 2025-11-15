package org.dromara.order.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新订单预览DTO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderPreviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务ID
     */
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
