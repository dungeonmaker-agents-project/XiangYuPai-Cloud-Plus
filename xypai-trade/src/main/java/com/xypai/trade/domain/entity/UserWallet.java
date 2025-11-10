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
 * 用户钱包实体
 *
 * @author xypai (Frank)
 * @date 2025-01-14
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
     * 用户ID（主键）
     */
    @TableId(type = IdType.INPUT)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 可用余额（分）
     */
    @TableField("balance")
    @NotNull(message = "余额不能为空")
    @Builder.Default
    private Long balance = 0L;

    /**
     * 冻结金额（分，订单待支付时冻结）
     */
    @TableField("frozen")
    @NotNull(message = "冻结金额不能为空")
    @Builder.Default
    private Long frozen = 0L;

    /**
     * 金币余额（虚拟货币）
     */
    @TableField("coin_balance")
    @NotNull(message = "金币余额不能为空")
    @Builder.Default
    private Long coinBalance = 0L;

    /**
     * 累计收入（分）
     */
    @TableField("total_income")
    @NotNull(message = "累计收入不能为空")
    @Builder.Default
    private Long totalIncome = 0L;

    /**
     * 累计支出（分）
     */
    @TableField("total_expense")
    @NotNull(message = "累计支出不能为空")
    @Builder.Default
    private Long totalExpense = 0L;

    /**
     * ⭐ 乐观锁版本号（核心安全机制）
     */
    @Version
    @TableField("version")
    @NotNull(message = "版本号不能为空")
    @Builder.Default
    private Integer version = 0;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ==========================================
    // 业务方法
    // ==========================================

    /**
     * 获取可用余额（元）
     */
    public BigDecimal getBalanceYuan() {
        return BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置可用余额（元）
     */
    public void setBalanceYuan(BigDecimal balanceYuan) {
        this.balance = balanceYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 获取冻结金额（元）
     */
    public BigDecimal getFrozenYuan() {
        return BigDecimal.valueOf(frozen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取总资产（元）= 可用余额 + 冻结金额
     */
    public BigDecimal getTotalAssetsYuan() {
        return BigDecimal.valueOf(balance + frozen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取累计收入（元）
     */
    public BigDecimal getTotalIncomeYuan() {
        return BigDecimal.valueOf(totalIncome).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取累计支出（元）
     */
    public BigDecimal getTotalExpenseYuan() {
        return BigDecimal.valueOf(totalExpense).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取净收益（元）= 累计收入 - 累计支出
     */
    public BigDecimal getNetProfitYuan() {
        return BigDecimal.valueOf(totalIncome - totalExpense).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 格式化余额显示
     */
    public String getFormattedBalance() {
        return "¥" + getBalanceYuan().toString();
    }

    /**
     * 格式化总资产显示
     */
    public String getFormattedTotalAssets() {
        return "¥" + getTotalAssetsYuan().toString();
    }

    /**
     * 检查余额是否足够
     */
    public boolean hasEnoughBalance(Long amount) {
        return balance >= amount;
    }

    /**
     * 检查可用余额是否足够（考虑冻结金额）
     */
    public boolean hasEnoughAvailableBalance(Long amount) {
        return balance >= amount;
    }

    /**
     * 充值（增加余额）
     */
    public void recharge(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        this.balance += amount;
        this.totalIncome += amount;
    }

    /**
     * 扣款（减少余额）
     */
    public void deduct(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("扣款金额必须大于0");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("余额不足");
        }
        this.balance -= amount;
        this.totalExpense += amount;
    }

    /**
     * 冻结金额（下单时）
     */
    public void freeze(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("冻结金额必须大于0");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("可用余额不足");
        }
        this.balance -= amount;
        this.frozen += amount;
    }

    /**
     * 解冻金额（取消订单时）
     */
    public void unfreeze(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("解冻金额必须大于0");
        }
        if (this.frozen < amount) {
            throw new IllegalStateException("冻结金额不足");
        }
        this.frozen -= amount;
        this.balance += amount;
    }

    /**
     * 扣减冻结金额（支付成功时）
     */
    public void deductFrozen(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("扣减金额必须大于0");
        }
        if (this.frozen < amount) {
            throw new IllegalStateException("冻结金额不足");
        }
        this.frozen -= amount;
        this.totalExpense += amount;
    }

    /**
     * 增加金币
     */
    public void addCoins(Long coins) {
        if (coins == null || coins <= 0) {
            throw new IllegalArgumentException("金币数量必须大于0");
        }
        this.coinBalance += coins;
    }

    /**
     * 扣减金币
     */
    public void deductCoins(Long coins) {
        if (coins == null || coins <= 0) {
            throw new IllegalArgumentException("金币数量必须大于0");
        }
        if (this.coinBalance < coins) {
            throw new IllegalStateException("金币余额不足");
        }
        this.coinBalance -= coins;
    }

    /**
     * 转账（从当前钱包转出到其他钱包）
     */
    public void transfer(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("转账金额必须大于0");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("余额不足");
        }
        this.balance -= amount;
        this.totalExpense += amount;
    }

    /**
     * 接收转账（从其他钱包转入）
     */
    public void receiveTransfer(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("转账金额必须大于0");
        }
        this.balance += amount;
        this.totalIncome += amount;
    }

    /**
     * 检查钱包数据一致性
     */
    public boolean isDataConsistent() {
        // 余额和冻结金额不能为负
        if (balance < 0 || frozen < 0 || coinBalance < 0) {
            return false;
        }
        
        // 累计收入和支出不能为负
        if (totalIncome < 0 || totalExpense < 0) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取钱包摘要信息
     */
    public String getSummary() {
        return String.format("用户%d的钱包：余额%.2f元，冻结%.2f元，金币%d，累计收入%.2f元，累计支出%.2f元",
                userId, getBalanceYuan(), getFrozenYuan(), coinBalance, 
                getTotalIncomeYuan(), getTotalExpenseYuan());
    }
}

