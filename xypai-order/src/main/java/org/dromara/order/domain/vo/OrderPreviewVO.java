package org.dromara.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单预览VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务提供者信息
     */
    private ProviderInfo provider;

    /**
     * 服务信息
     */
    private ServiceInfo service;

    /**
     * 价格信息
     */
    private PriceInfo price;

    /**
     * 数量选项
     */
    private QuantityOptions quantityOptions;

    /**
     * 预览信息
     */
    private PreviewInfo preview;

    /**
     * 用户余额
     */
    private BigDecimal userBalance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String avatar;
        private String nickname;
        private String gender;
        private Integer age;
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
    public static class PreviewInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer quantity;
        private BigDecimal subtotal;
        private BigDecimal serviceFee;
        private BigDecimal total;
    }
}
