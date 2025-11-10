package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 用户钱包实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_wallet")
public class UserWallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联用户ID
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 可用余额(分为单位,避免精度问题)
     */
    @TableField("balance")
    @Builder.Default
    private Long balance = 0L;

    /**
     * 冻结金额(分)
     */
    @TableField("frozen")
    @Builder.Default
    private Long frozen = 0L;

    /**
     * 金币余额
     */
    @TableField("coin_balance")
    @Builder.Default
    private Long coinBalance = 0L;

    /**
     * 累计收入(分)
     */
    @TableField("total_income")
    @Builder.Default
    private Long totalIncome = 0L;

    /**
     * 累计支出(分)
     */
    @TableField("total_expense")
    @Builder.Default
    private Long totalExpense = 0L;

    /**
     * 乐观锁版本号(并发控制)
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 获取余额(元)
     */
    public BigDecimal getBalanceYuan() {
        return BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置余额(元)
     */
    public void setBalanceYuan(BigDecimal balanceYuan) {
        this.balance = balanceYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 增加余额(分)
     */
    public void addBalance(Long amount) {
        if (amount != null && amount > 0) {
            this.balance += amount;
        }
    }

    /**
     * 减少余额(分)
     */
    public boolean deductBalance(Long amount) {
        if (amount != null && amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * 检查余额是否足够
     */
    public boolean hasEnoughBalance(Long amount) {
        return amount != null && this.balance >= amount;
    }

    /**
     * 检查是否有余额
     */
    public boolean hasBalance() {
        return this.balance > 0;
    }

    /**
     * 格式化余额显示
     */
    public String getFormattedBalance() {
        return "¥" + getBalanceYuan().toString();
    }

    /**
     * 冻结余额(分)
     */
    public boolean freezeBalance(Long amount) {
        if (amount != null && amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.frozen += amount;
            return true;
        }
        return false;
    }

    /**
     * 解冻余额(分)
     */
    public boolean unfreezeBalance(Long amount) {
        if (amount != null && amount > 0 && this.frozen >= amount) {
            this.frozen -= amount;
            this.balance += amount;
            return true;
        }
        return false;
    }

    /**
     * 增加金币
     */
    public void addCoins(Long coins) {
        if (coins != null && coins > 0) {
            this.coinBalance += coins;
        }
    }

    /**
     * 扣除金币
     */
    public boolean deductCoins(Long coins) {
        if (coins != null && coins > 0 && this.coinBalance >= coins) {
            this.coinBalance -= coins;
            return true;
        }
        return false;
    }

    /**
     * 增加收入统计
     */
    public void addIncome(Long amount) {
        if (amount != null && amount > 0) {
            this.totalIncome += amount;
        }
    }

    /**
     * 增加支出统计
     */
    public void addExpense(Long amount) {
        if (amount != null && amount > 0) {
            this.totalExpense += amount;
        }
    }

    /**
     * 获取总资产(可用+冻结)
     */
    public Long getTotalAssets() {
        return (balance != null ? balance : 0L) + (frozen != null ? frozen : 0L);
    }

    /**
     * 获取可用余额(元)
     */
    public BigDecimal getAvailableBalanceYuan() {
        return BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取冻结金额(元)
     */
    public BigDecimal getFrozenYuan() {
        return BigDecimal.valueOf(frozen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取总收入(元)
     */
    public BigDecimal getTotalIncomeYuan() {
        return BigDecimal.valueOf(totalIncome).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取总支出(元)
     */
    public BigDecimal getTotalExpenseYuan() {
        return BigDecimal.valueOf(totalExpense).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 检查冻结金额是否足够
     */
    public boolean hasEnoughFrozen(Long amount) {
        return amount != null && this.frozen >= amount;
    }

    /**
     * 检查金币余额是否足够
     */
    public boolean hasEnoughCoins(Long coins) {
        return coins != null && this.coinBalance >= coins;
    }
}

