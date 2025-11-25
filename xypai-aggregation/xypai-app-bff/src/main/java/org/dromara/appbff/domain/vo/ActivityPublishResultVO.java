package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 发布组局结果VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "发布组局结果")
public class ActivityPublishResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "发布是否成功")
    private Boolean success;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "活动状态")
    private String status;

    @Schema(description = "状态说明", example = "活动已发布，等待其他用户报名")
    private String statusMessage;

    @Schema(description = "是否需要支付(发布费)")
    private Boolean needPay;

    @Schema(description = "发布费金额")
    private BigDecimal publishFee;

    @Schema(description = "支付订单ID(如果需要支付)")
    private String payOrderId;

    @Schema(description = "用户剩余免费发布次数")
    private Integer freePublishRemaining;

    @Schema(description = "分享链接")
    private String shareUrl;

    @Schema(description = "分享二维码URL")
    private String shareQrCodeUrl;
}
