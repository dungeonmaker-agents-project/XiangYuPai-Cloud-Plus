package org.dromara.payment.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 支付方式VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付方式列表
     */
    private List<PaymentMethod> methods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethod implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 支付方式类型: balance-余额支付, alipay-支付宝, wechat-微信支付
         */
        private String type;

        /**
         * 显示名称
         */
        private String name;

        /**
         * 图标URL
         */
        private String icon;

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 是否需要密码
         */
        private Boolean requirePassword;

        /**
         * 余额（仅余额支付显示）
         */
        private BigDecimal balance;
    }
}
