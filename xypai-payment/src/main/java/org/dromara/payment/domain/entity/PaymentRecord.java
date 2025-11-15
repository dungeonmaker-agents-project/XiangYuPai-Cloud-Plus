package org.dromara.payment.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 * 对应表: payment_record
 *
 * @author XyPai Team
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment_record")
public class PaymentRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付流水号（唯一）
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 付款用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收款用户ID（可空，平台收款时为空）
     */
    @TableField("payee_id")
    private Long payeeId;

    /**
     * 支付方式: balance-余额支付, alipay-支付宝, wechat-微信支付
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付类型: order-订单支付, activity_publish-活动发布, activity_register-活动报名
     */
    @TableField("payment_type")
    private String paymentType;

    /**
     * 关联ID（订单ID/活动ID）
     */
    @TableField("reference_id")
    private String referenceId;

    /**
     * 关联类型
     */
    @TableField("reference_type")
    private String referenceType;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 服务费
     */
    @TableField("service_fee")
    private BigDecimal serviceFee;

    /**
     * 状态: pending-待支付, success-支付成功, failed-支付失败, refunded-已退款
     */
    @TableField("status")
    private String status;

    /**
     * 支付成功时间
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

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
     * 备注
     */
    @TableField("remark")
    private String remark;

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
}
