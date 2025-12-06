package org.dromara.appbff.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户余额VO
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 可用余额 */
    private BigDecimal availableBalance;

    /** 是否已设置支付密码 */
    private Boolean hasPaymentPassword;
}
