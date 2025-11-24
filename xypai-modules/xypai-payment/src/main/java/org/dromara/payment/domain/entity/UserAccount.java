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
 * 用户账户实体
 * 对应表: user_account
 *
 * @author XyPai Team
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_account")
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账户ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID（唯一）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    /**
     * 累计收入
     */
    @TableField("total_income")
    private BigDecimal totalIncome;

    /**
     * 累计支出
     */
    @TableField("total_expense")
    private BigDecimal totalExpense;

    /**
     * 支付密码哈希（BCrypt）
     */
    @TableField("payment_password_hash")
    private String paymentPasswordHash;

    /**
     * 密码错误次数
     */
    @TableField("password_error_count")
    private Integer passwordErrorCount;

    /**
     * 密码锁定至
     */
    @TableField("password_locked_until")
    private LocalDateTime passwordLockedUntil;

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
