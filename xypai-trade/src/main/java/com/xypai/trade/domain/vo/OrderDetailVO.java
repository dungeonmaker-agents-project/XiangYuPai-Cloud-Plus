package com.xypai.trade.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单详情VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单详情VO")
public class OrderDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * 买家用户ID
     */
    @Schema(description = "买家用户ID")
    private Long buyerId;

    /**
     * 买家信息
     */
    @Schema(description = "买家信息")
    private OrderListVO.UserInfoVO buyer;

    /**
     * 卖家用户ID
     */
    @Schema(description = "卖家用户ID")
    private Long sellerId;

    /**
     * 卖家信息
     */
    @Schema(description = "卖家信息")
    private OrderListVO.UserInfoVO seller;

    /**
     * 关联内容ID
     */
    @Schema(description = "关联内容ID")
    private Long contentId;

    /**
     * 关联内容信息
     */
    @Schema(description = "关联内容信息")
    private OrderListVO.ContentInfoVO content;

    /**
     * 订单金额(元)
     */
    @Schema(description = "订单金额(元)")
    private BigDecimal amount;

    /**
     * 格式化金额显示
     */
    @Schema(description = "格式化金额显示")
    private String formattedAmount;

    /**
     * 服务时长(小时)
     */
    @Schema(description = "服务时长(小时)")
    private Integer duration;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 服务描述
     */
    @Schema(description = "服务描述")
    private String serviceDescription;

    /**
     * 服务要求
     */
    @Schema(description = "服务要求")
    private String serviceRequirements;

    /**
     * 期望开始时间
     */
    @Schema(description = "期望开始时间")
    private String expectedStartTime;

    /**
     * 期望结束时间
     */
    @Schema(description = "期望结束时间")
    private String expectedEndTime;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    private String contactInfo;

    /**
     * 特殊要求
     */
    @Schema(description = "特殊要求")
    private String specialRequirements;

    /**
     * 下单时间
     */
    @Schema(description = "下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 是否可以取消
     */
    @Schema(description = "是否可以取消")
    private Boolean canCancel;

    /**
     * 是否可以退款
     */
    @Schema(description = "是否可以退款")
    private Boolean canRefund;

    /**
     * 是否可以确认完成
     */
    @Schema(description = "是否可以确认完成")
    private Boolean canComplete;

    /**
     * 是否可以评价
     */
    @Schema(description = "是否可以评价")
    private Boolean canReview;

    /**
     * 支付信息
     */
    @Schema(description = "支付信息")
    private PaymentInfoVO paymentInfo;

    /**
     * 服务进度
     */
    @Schema(description = "服务进度")
    private List<ServiceProgressVO> serviceProgress;

    /**
     * 订单日志
     */
    @Schema(description = "订单日志")
    private List<OrderLogVO> orderLogs;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;

    /**
     * 支付信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "支付信息VO")
    public static class PaymentInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "支付方式")
        private String paymentMethod;

        @Schema(description = "支付时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime paymentTime;

        @Schema(description = "支付流水号")
        private String paymentNo;

        @Schema(description = "第三方交易号")
        private String thirdPartyNo;
    }

    /**
     * 服务进度VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "服务进度VO")
    public static class ServiceProgressVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "阶段")
        private String stage;

        @Schema(description = "阶段描述")
        private String stageDesc;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "更新时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 订单日志VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "订单日志VO")
    public static class OrderLogVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "操作类型")
        private String actionType;

        @Schema(description = "操作描述")
        private String actionDesc;

        @Schema(description = "操作人")
        private String operatorName;

        @Schema(description = "操作时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operateTime;

        @Schema(description = "备注")
        private String remark;
    }
}
