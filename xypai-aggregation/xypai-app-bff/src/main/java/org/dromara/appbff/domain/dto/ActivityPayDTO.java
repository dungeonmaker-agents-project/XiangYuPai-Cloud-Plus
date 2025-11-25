package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 活动付款请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "活动付款请求")
public class ActivityPayDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "支付方式: wechat(微信), alipay(支付宝), balance(余额)", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "支付密码(余额支付时需要)")
    private String paymentPassword;
}
