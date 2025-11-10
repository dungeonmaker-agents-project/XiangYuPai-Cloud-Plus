package com.xypai.user.domain.entity;

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
 * 统一交易流水实体
 *
 * @author xypai
 * @date 2025-01-01
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
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 交易金额(正负表示收支，分为单位)
     */
    @TableField("amount")
    @NotNull(message = "交易金额不能为空")
    private Long amount;

    /**
     * 交易类型(recharge/consume/refund/withdraw)
     */
    @TableField("type")
    @NotNull(message = "交易类型不能为空")
    private String type;

    /**
     * 关联类型(order/activity/reward)
     */
    @TableField("ref_type")
    private String refType;

    /**
     * 关联业务ID
     */
    @TableField("ref_id")
    private String refId;

    /**
     * 交易状态(0=处理中,1=成功,2=失败)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 0;

    /**
     * 支付方式(wechat/alipay/balance)
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 交易描述
     */
    @TableField("description")
    private String description;

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
        REWARD("reward", "奖励"),
        WITHDRAW("withdraw", "提现"),
        TRANSFER_IN("transfer_in", "转入"),
        TRANSFER_OUT("transfer_out", "转出");

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
     * 获取交易金额(元)
     */
    public BigDecimal getAmountYuan() {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置交易金额(元)
     */
    public void setAmountYuan(BigDecimal amountYuan) {
        this.amount = amountYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 是否为收入交易
     */
    public boolean isIncome() {
        return amount > 0;
    }

    /**
     * 是否为支出交易
     */
    public boolean isExpense() {
        return amount < 0;
    }

    /**
     * 获取交易类型描述
     */
    public String getTypeDesc() {
        Type transactionType = Type.fromCode(this.type);
        return transactionType != null ? transactionType.getDesc() : this.type;
    }

    /**
     * 格式化金额显示
     */
    public String getFormattedAmount() {
        String prefix = isIncome() ? "+" : "";
        return prefix + "¥" + getAmountYuan().abs().toString();
    }

    /**
     * 交易状态枚举
     */
    public enum Status {
        PROCESSING(0, "处理中"),
        SUCCESS(1, "成功"),
        FAILED(2, "失败");

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
    }

    /**
     * 关联类型枚举
     */
    public enum RefType {
        ORDER("order", "订单"),
        ACTIVITY("activity", "活动"),
        REWARD("reward", "奖励"),
        SYSTEM("system", "系统");

        private final String code;
        private final String desc;

        RefType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 检查交易是否成功
     */
    public boolean isSuccess() {
        return Status.SUCCESS.getCode().equals(this.status);
    }

    /**
     * 检查交易是否失败
     */
    public boolean isFailed() {
        return Status.FAILED.getCode().equals(this.status);
    }

    /**
     * 检查交易是否处理中
     */
    public boolean isProcessing() {
        return Status.PROCESSING.getCode().equals(this.status);
    }

    /**
     * 标记为成功
     */
    public void markAsSuccess() {
        this.status = Status.SUCCESS.getCode();
    }

    /**
     * 标记为失败
     */
    public void markAsFailed() {
        this.status = Status.FAILED.getCode();
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        for (Status s : Status.values()) {
            if (s.getCode().equals(this.status)) {
                return s.getDesc();
            }
        }
        return "未知";
    }

    /**
     * 检查是否为充值交易
     */
    public boolean isRecharge() {
        return Type.RECHARGE.getCode().equals(this.type);
    }

    /**
     * 检查是否为消费交易
     */
    public boolean isConsume() {
        return Type.CONSUME.getCode().equals(this.type);
    }

    /**
     * 检查是否为退款交易
     */
    public boolean isRefund() {
        return Type.REFUND.getCode().equals(this.type);
    }

    /**
     * 检查是否为提现交易
     */
    public boolean isWithdraw() {
        return Type.WITHDRAW.getCode().equals(this.type);
    }

    /**
     * 设置关联订单
     */
    public void setOrderRef(String orderId) {
        this.refType = RefType.ORDER.getCode();
        this.refId = orderId;
    }

    /**
     * 设置关联活动
     */
    public void setActivityRef(String activityId) {
        this.refType = RefType.ACTIVITY.getCode();
        this.refId = activityId;
    }
}

