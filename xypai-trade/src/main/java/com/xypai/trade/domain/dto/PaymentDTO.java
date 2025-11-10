package com.xypai.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 支付操作DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付操作DTO")
public class PaymentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 支付方式(wallet=钱包, wechat=微信, alipay=支付宝)
     */
    @Schema(description = "支付方式", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 支付密码(钱包支付时需要)
     */
    @Schema(description = "支付密码")
    private String paymentPassword;

    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP")
    private String clientIp;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    private String deviceInfo;

    /**
     * 回调地址
     */
    @Schema(description = "回调地址")
    private String notifyUrl;

    /**
     * 返回地址
     */
    @Schema(description = "返回地址")
    private String returnUrl;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;
}
