package com.xypai.trade.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付结果VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付结果VO")
public class PaymentResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * 支付状态(success=成功, pending=处理中, failed=失败, cancelled=已取消)
     */
    @Schema(description = "支付状态")
    private String paymentStatus;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式")
    private String paymentMethod;

    /**
     * 支付金额(元)
     */
    @Schema(description = "支付金额(元)")
    private BigDecimal paymentAmount;

    /**
     * 支付流水号
     */
    @Schema(description = "支付流水号")
    private String paymentNo;

    /**
     * 第三方交易号
     */
    @Schema(description = "第三方交易号")
    private String thirdPartyNo;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 支付二维码(第三方支付)
     */
    @Schema(description = "支付二维码")
    private String qrCode;

    /**
     * 支付链接(第三方支付)
     */
    @Schema(description = "支付链接")
    private String paymentUrl;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 错误代码
     */
    @Schema(description = "错误代码")
    private String errorCode;

    /**
     * 是否需要跳转
     */
    @Schema(description = "是否需要跳转")
    private Boolean needRedirect;

    /**
     * 跳转URL
     */
    @Schema(description = "跳转URL")
    private String redirectUrl;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;

    /**
     * 支付成功
     */
    public boolean isSuccess() {
        return "success".equals(paymentStatus);
    }

    /**
     * 支付处理中
     */
    public boolean isPending() {
        return "pending".equals(paymentStatus);
    }

    /**
     * 支付失败
     */
    public boolean isFailed() {
        return "failed".equals(paymentStatus);
    }

    /**
     * 支付已取消
     */
    public boolean isCancelled() {
        return "cancelled".equals(paymentStatus);
    }
}
