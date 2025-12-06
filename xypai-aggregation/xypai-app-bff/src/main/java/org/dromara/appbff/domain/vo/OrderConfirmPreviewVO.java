package org.dromara.appbff.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认预览VO
 *
 * <p>对应前端：OrderConfirmPage 订单确认页</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 陪玩师信息 */
    private ProviderInfo provider;

    /** 服务信息 */
    private ServiceInfo service;

    /** 价格信息 */
    private PriceInfo price;

    /** 数量选择配置 */
    private QuantityOptions quantityOptions;

    /** 价格预览 */
    private PricePreview preview;

    /** 用户余额 */
    private BigDecimal userBalance;

    /** 是否已设置支付密码 */
    private Boolean hasPaymentPassword;

    // ========== 内部类 ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long userId;
        private String nickname;
        private String avatar;
        private String gender;
        private Integer age;
        private Boolean isOnline;
        private Boolean isVerified;
        private List<String> tags;
        private SkillInfo skillInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String gameArea;
        private String rank;
        private String rankDisplay;
        private Integer peakScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long serviceId;
        private String name;
        private String icon;
        private String skillType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal unitPrice;
        private String unit;
        private String displayText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantityOptions implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer min;
        private Integer max;
        private Integer defaultValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePreview implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer quantity;
        private BigDecimal subtotal;
        private BigDecimal total;
    }
}
