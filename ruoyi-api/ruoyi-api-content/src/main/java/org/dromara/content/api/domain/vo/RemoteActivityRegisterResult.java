package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动报名结果VO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityRegisterResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 报名状态: pending, approved
     */
    private String status;

    /**
     * 状态消息
     */
    private String statusMessage;

    /**
     * 参与者记录ID
     */
    private Long participantId;

    /**
     * 是否需要支付
     */
    private Boolean needPay;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 当前人数
     */
    private Integer currentMembers;

    /**
     * 最大人数
     */
    private Integer maxMembers;
}
