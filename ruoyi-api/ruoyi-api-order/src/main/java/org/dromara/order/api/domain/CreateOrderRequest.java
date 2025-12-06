package org.dromara.order.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建订单请求（RPC层）
 *
 * <p>调用方：xypai-app-bff (订单确认支付)</p>
 * <p>场景：用户下单后创建订单记录</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 下单用户ID */
    private Long userId;

    /** 技能服务ID */
    private Long serviceId;

    /** 服务提供者ID（陪玩师） */
    private Long providerId;

    /** 数量（场次） */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 总金额（用于二次验证） */
    private BigDecimal totalAmount;

    /** 支付方式: balance=余额, alipay=支付宝, wechat=微信 */
    private String paymentMethod;

    /** 备注 */
    private String remark;
}
