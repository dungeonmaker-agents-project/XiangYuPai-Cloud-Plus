package org.dromara.order.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 对应表: `order`
 *
 * @author XyPai Team
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单编号（唯一）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 下单用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 服务提供者ID
     */
    @TableField("provider_id")
    private Long providerId;

    /**
     * 服务ID
     */
    @TableField("service_id")
    private Long serviceId;

    /**
     * 订单类型: service-技能服务订单, activity-活动订单
     */
    @TableField("order_type")
    private String orderType;

    /**
     * 数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 小计（数量 * 单价）
     */
    @TableField("subtotal")
    private BigDecimal subtotal;

    /**
     * 服务费（平台服务费）
     */
    @TableField("service_fee")
    private BigDecimal serviceFee;

    /**
     * 总金额（小计 + 服务费）
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 订单状态: pending-待接单, accepted-已接单, in_progress-进行中, completed-已完成, cancelled-已取消, refunded-已退款
     */
    @TableField("status")
    private String status;

    /**
     * 支付状态: pending-待支付, success-支付成功, failed-支付失败
     */
    @TableField("payment_status")
    private String paymentStatus;

    /**
     * 支付方式: balance-余额支付, alipay-支付宝, wechat-微信支付
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /**
     * 接单时间
     */
    @TableField("accepted_time")
    private LocalDateTime acceptedTime;

    /**
     * 完成时间
     */
    @TableField("completed_time")
    private LocalDateTime completedTime;

    /**
     * 取消时间
     */
    @TableField("cancelled_time")
    private LocalDateTime cancelledTime;

    /**
     * 自动取消时间（10分钟未接单自动取消）
     */
    @TableField("auto_cancel_time")
    private LocalDateTime autoCancelTime;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 软删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
