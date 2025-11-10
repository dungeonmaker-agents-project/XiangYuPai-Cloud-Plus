package com.xypai.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包转账DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransferDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接收方用户ID
     */
    @NotNull(message = "接收方用户ID不能为空")
    private Long toUserId;

    /**
     * 转账金额(元)
     */
    @NotNull(message = "转账金额不能为空")
    @DecimalMin(value = "0.01", message = "转账金额最少为0.01元")
    private BigDecimal amount;

    /**
     * 转账说明
     */
    @Size(max = 200, message = "转账说明长度不能超过200个字符")
    private String description;

    /**
     * 支付密码
     */
    private String paymentPassword;
}
