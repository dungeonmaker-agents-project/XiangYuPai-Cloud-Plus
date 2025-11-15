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
 * 账户流水实体
 * 对应表: account_transaction
 *
 * @author XyPai Team
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("account_transaction")
public class AccountTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 交易流水号（唯一）
     */
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 交易类型: income-收入, expense-支出, freeze-冻结, unfreeze-解冻
     */
    @TableField("transaction_type")
    private String transactionType;

    /**
     * 交易金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    @TableField("balance_before")
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 关联支付流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 关联ID
     */
    @TableField("reference_id")
    private String referenceId;

    /**
     * 关联类型
     */
    @TableField("reference_type")
    private String referenceType;

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
}
