package org.dromara.payment.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 余额VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenBalance;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;
}
