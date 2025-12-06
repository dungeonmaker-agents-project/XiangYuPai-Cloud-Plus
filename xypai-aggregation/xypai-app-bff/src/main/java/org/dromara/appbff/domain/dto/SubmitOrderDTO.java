package org.dromara.appbff.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提交订单DTO
 *
 * <p>前端调用：订单确认页点击"确认支付"</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Data
public class SubmitOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 技能服务ID */
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    /** 数量（场次） */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 总金额（前端计算，后端校验） */
    @NotNull(message = "金额不能为空")
    private BigDecimal totalAmount;

    /** 支付密码（6位数字） */
    @NotBlank(message = "支付密码不能为空")
    private String paymentPassword;

    /** 备注 */
    private String remark;
}
