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

/**
 * 订单列表VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单列表VO")
public class OrderListVO implements Serializable {

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
    private UserInfoVO buyer;

    /**
     * 卖家用户ID
     */
    @Schema(description = "卖家用户ID")
    private Long sellerId;

    /**
     * 卖家信息
     */
    @Schema(description = "卖家信息")
    private UserInfoVO seller;

    /**
     * 关联内容ID
     */
    @Schema(description = "关联内容ID")
    private Long contentId;

    /**
     * 关联内容信息
     */
    @Schema(description = "关联内容信息")
    private ContentInfoVO content;

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
     * 用户信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息VO")
    public static class UserInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "信用等级")
        private String creditLevel;

        @Schema(description = "评分")
        private BigDecimal rating;
    }

    /**
     * 内容信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "内容信息VO")
    public static class ContentInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "内容ID")
        private Long contentId;

        @Schema(description = "内容标题")
        private String title;

        @Schema(description = "内容类型")
        private Integer type;

        @Schema(description = "内容类型描述")
        private String typeDesc;

        @Schema(description = "封面图片")
        private String coverImage;

        @Schema(description = "技能分类")
        private String skillCategory;

        @Schema(description = "技能等级")
        private String skillLevel;
    }
}
