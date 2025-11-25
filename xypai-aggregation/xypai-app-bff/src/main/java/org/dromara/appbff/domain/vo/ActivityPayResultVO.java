package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动支付结果VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "活动支付结果")
public class ActivityPayResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "支付是否成功")
    private Boolean success;

    @Schema(description = "支付订单ID")
    private String payOrderId;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "支付状态: pending(待支付), success(支付成功), failed(支付失败)")
    private String paymentStatus;

    @Schema(description = "支付状态说明")
    private String statusMessage;

    @Schema(description = "微信/支付宝支付参数(用于唤起支付)")
    private String paymentParams;

    @Schema(description = "支付二维码URL(PC端扫码支付)")
    private String qrCodeUrl;

    @Schema(description = "支付超时时间(秒)")
    private Integer timeoutSeconds;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;
}
