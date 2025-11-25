package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动报名结果VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "活动报名结果")
public class ActivityRegisterResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "报名是否成功")
    private Boolean success;

    @Schema(description = "报名状态: pending(待审核), approved(已通过)")
    private String status;

    @Schema(description = "状态说明")
    private String statusMessage;

    @Schema(description = "是否需要支付")
    private Boolean needPay;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付订单ID(如果需要支付)")
    private String payOrderId;

    @Schema(description = "当前报名人数")
    private Integer currentMembers;

    @Schema(description = "最大人数")
    private Integer maxMembers;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "活动时间显示")
    private String activityTimeDisplay;

    @Schema(description = "活动地点")
    private String activityLocation;
}
