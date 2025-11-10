package com.xypai.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 交易流水实体
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("transaction")
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 交易记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 交易金额（分，正数=收入，负数=支出）
     */
    @TableField("amount")
    @NotNull(message = "交易金额不能为空")
    private Long amount;

    /**
     * 交易类型（recharge/consume/refund/withdraw/income/transfer）
     */
    @TableField("type")
    @NotNull(message = "交易类型不能为空")
    private String type;

    /**
     * 关联类型（order/activity/reward/transfer）
     */
    @TableField("ref_type")
    private String refType;

    /**
     * 关联业务ID
     */
    @TableField("ref_id")
    private Long refId;

    /**
     * 交易状态（0=处理中,1=成功,2=失败,3=已取消）
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 支付方式（wechat/alipay/balance/bankcard）
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 第三方支付流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 交易描述
     */
    @TableField("description")
    private String description;

    /**
     * 交易前余额（分）
     */
    @TableField("balance_before")
    private Long balanceBefore;

    /**
     * 交易后余额（分）
     */
    @TableField("balance_after")
    private Long balanceAfter;

    /**
     * 交易时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 交易类型枚举
     */
    public enum Type {
        RECHARGE("recharge", "充值"),
        CONSUME("consume", "消费"),
        REFUND("refund", "退款"),
        WITHDRAW("withdraw", "提现"),
        INCOME("income", "收入"),
        TRANSFER("transfer", "转账");

        private final String code;
        private final String desc;

        Type(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Type fromCode(String code) {
            for (Type type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 交易状态枚举
     */
    public enum Status {
        PROCESSING(0, "处理中"),
        SUCCESS(1, "成功"),
        FAILED(2, "失败"),
        CANCELLED(3, "已取消");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    // ==========================================
    // 业务方法
    // ==========================================

    /**
     * 获取交易金额（元）
     */
    public BigDecimal getAmountYuan() {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取交易金额（绝对值，元）
     */
    public BigDecimal getAbsAmountYuan() {
        return BigDecimal.valueOf(Math.abs(amount)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 格式化金额显示
     */
    public String getFormattedAmount() {
        String prefix = amount >= 0 ? "+" : "";
        return prefix + "¥" + getAmountYuan().toString();
    }

    /**
     * 是否收入
     */
    public boolean isIncome() {
        return amount != null && amount > 0;
    }

    /**
     * 是否支出
     */
    public boolean isExpense() {
        return amount != null && amount < 0;
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return Status.SUCCESS.getCode().equals(this.status);
    }

    /**
     * 是否处理中
     */
    public boolean isProcessing() {
        return Status.PROCESSING.getCode().equals(this.status);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return Status.FAILED.getCode().equals(this.status);
    }

    /**
     * 获取交易类型描述
     */
    public String getTypeDesc() {
        Type transactionType = Type.fromCode(this.type);
        return transactionType != null ? transactionType.getDesc() : "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status transactionStatus = Status.fromCode(this.status);
        return transactionStatus != null ? transactionStatus.getDesc() : "未知";
    }

    /**
     * 获取交易流水号（格式化显示）
     */
    public String getTransactionNo() {
        return "TXN" + id;
    }

    /**
     * 获取关联业务描述
     */
    public String getRefDescription() {
        if (refType == null) {
            return "无关联业务";
        }
        return switch (refType) {
            case "order" -> "订单 #" + refId;
            case "activity" -> "活动 #" + refId;
            case "reward" -> "奖励 #" + refId;
            case "transfer" -> "转账 #" + refId;
            default -> refType + " #" + refId;
        };
    }

    /**
     * 验证余额快照一致性
     */
    public boolean isBalanceSnapshotValid() {
        if (balanceBefore == null || balanceAfter == null) {
            return true; // 没有快照，不校验
        }
        
        long expectedChange = amount;
        long actualChange = balanceAfter - balanceBefore;
        
        return expectedChange == actualChange;
    }

    /**
     * 获取余额变化（元）
     */
    public BigDecimal getBalanceChange() {
        if (balanceBefore == null || balanceAfter == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(balanceAfter - balanceBefore)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取交易摘要
     */
    public String getSummary() {
        return String.format("%s | 用户%d | %s | %s | %s",
                getTransactionNo(), userId, getTypeDesc(), 
                getFormattedAmount(), getStatusDesc());
    }
}

