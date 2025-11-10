package com.xypai.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 服务订单实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "service_order", autoResultMap = true)
public class ServiceOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 买家用户ID
     */
    @TableField("buyer_id")
    @NotNull(message = "买家ID不能为空")
    private Long buyerId;

    /**
     * 卖家用户ID
     */
    @TableField("seller_id")
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    /**
     * 关联技能内容ID
     */
    @TableField("content_id")
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 订单编号（格式：SO+雪花ID）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 服务类型（1=游戏陪玩,2=生活服务,3=活动报名）
     */
    @TableField("service_type")
    @Builder.Default
    private Integer serviceType = 1;

    /**
     * 服务名称（冗余存储，方便查询）
     */
    @TableField("service_name")
    private String serviceName;

    /**
     * 服务时间（预约时间）
     */
    @TableField("service_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceTime;

    /**
     * 订单金额(分)
     */
    @TableField("amount")
    @NotNull(message = "订单金额不能为空")
    @Positive(message = "订单金额必须大于0")
    private Long amount;

    /**
     * 服务时长(小时)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 服务时长（分钟）- 新字段
     */
    @TableField("service_duration")
    private Integer serviceDuration;

    /**
     * 参与人数（活动组局使用）
     */
    @TableField("participant_count")
    @Builder.Default
    private Integer participantCount = 1;

    /**
     * 基础服务费（分）
     */
    @TableField("base_fee")
    @Builder.Default
    private Long baseFee = 0L;

    /**
     * 人数费用（分，活动AA制使用）
     */
    @TableField("person_fee")
    @Builder.Default
    private Long personFee = 0L;

    /**
     * 平台服务费（分，抽成）
     */
    @TableField("platform_fee")
    @Builder.Default
    private Long platformFee = 0L;

    /**
     * 优惠减免金额（分）
     */
    @TableField("discount_amount")
    @Builder.Default
    private Long discountAmount = 0L;

    /**
     * 实际支付金额（分）
     */
    @TableField("actual_amount")
    @NotNull(message = "实际支付金额不能为空")
    @Builder.Default
    private Long actualAmount = 0L;

    /**
     * 联系人姓名
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 特殊要求/备注
     */
    @TableField("special_request")
    private String specialRequest;

    /**
     * 支付方式（wallet/wechat/alipay）
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 订单完成时间
     */
    @TableField("completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * 订单状态(0=待付款,1=已付款,2=服务中,3=已完成,4=已取消,5=已退款)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 0;

    /**
     * 订单扩展信息JSON
     */
    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> data;

    /**
     * 下单时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

    /**
     * 是否已迁移（data字段→具体字段）
     */
    @TableField("is_migrated")
    @Builder.Default
    private Boolean isMigrated = false;

    /**
     * 迁移时间
     */
    @TableField("migrate_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime migrateTime;

    /**
     * 订单状态枚举
     */
    public enum Status {
        PENDING_PAYMENT(0, "待付款"),
        PAID(1, "已付款"),
        IN_SERVICE(2, "服务中"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消"),
        REFUNDED(5, "已退款");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 获取订单金额(元)
     */
    public BigDecimal getAmountYuan() {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置订单金额(元)
     */
    public void setAmountYuan(BigDecimal amountYuan) {
        this.amount = amountYuan.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 是否为待付款状态
     */
    public boolean isPendingPayment() {
        return Status.PENDING_PAYMENT.getCode().equals(this.status);
    }

    /**
     * 是否已付款
     */
    public boolean isPaid() {
        return Status.PAID.getCode().equals(this.status);
    }

    /**
     * 是否服务中
     */
    public boolean isInService() {
        return Status.IN_SERVICE.getCode().equals(this.status);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return Status.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return Status.CANCELLED.getCode().equals(this.status);
    }

    /**
     * 是否已退款
     */
    public boolean isRefunded() {
        return Status.REFUNDED.getCode().equals(this.status);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status orderStatus = Status.fromCode(this.status);
        return orderStatus != null ? orderStatus.getDesc() : "未知";
    }

    /**
     * 格式化金额显示
     */
    public String getFormattedAmount() {
        return "¥" + getAmountYuan().toString();
    }

    /**
     * 获取订单编号（格式化显示）
     * 优先使用orderNo字段，如果为空则使用id生成
     */
    public String getOrderNo() {
        return (orderNo != null && !orderNo.isEmpty()) ? orderNo : "SO" + id;
    }

    /**
     * 生成并设置订单编号
     */
    public void generateOrderNo() {
        if (this.id != null) {
            this.orderNo = "SO" + this.id;
        }
    }

    /**
     * 计算费用明细（简化版本）
     */
    public void calculateFees() {
        // 基础费用等于订单金额
        if (this.baseFee == null || this.baseFee == 0) {
            this.baseFee = this.amount;
        }
        
        // 计算平台服务费（5%）
        if (this.platformFee == null || this.platformFee == 0) {
            this.platformFee = (long) (this.amount * 0.05);
        }
        
        // 计算实际支付金额 = 基础费用 + 人数费用 - 优惠金额
        this.actualAmount = this.baseFee + 
                           (this.personFee != null ? this.personFee : 0L) - 
                           (this.discountAmount != null ? this.discountAmount : 0L);
    }

    /**
     * 计算费用明细（完整版本）
     * @param baseFee 基础费用
     * @param personFee 人数费用
     * @param platformFee 平台服务费
     * @param discountAmount 优惠金额
     */
    public void calculateFees(Long baseFee, Long personFee, Long platformFee, Long discountAmount) {
        this.baseFee = baseFee != null ? baseFee : 0L;
        this.personFee = personFee != null ? personFee : 0L;
        this.platformFee = platformFee != null ? platformFee : 0L;
        this.discountAmount = discountAmount != null ? discountAmount : 0L;
        
        // 订单总金额 = 基础费用 + 人数费用
        this.amount = this.baseFee + this.personFee;
        
        // 实际支付金额 = 订单总金额 - 优惠金额
        this.actualAmount = this.amount - this.discountAmount;
    }

    /**
     * 获取实际支付金额(元)
     */
    public BigDecimal getActualAmountYuan() {
        return BigDecimal.valueOf(actualAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 格式化实际支付金额显示
     */
    public String getFormattedActualAmount() {
        return "¥" + getActualAmountYuan().toString();
    }

    /**
     * 获取服务类型描述
     */
    public String getServiceTypeDesc() {
        if (serviceType == null) return "未知";
        return switch (serviceType) {
            case 1 -> "游戏陪玩";
            case 2 -> "生活服务";
            case 3 -> "活动报名";
            default -> "未知";
        };
    }

    /**
     * 检查是否已支付
     */
    public boolean hasPaid() {
        return this.paymentTime != null && this.isPaid();
    }

    /**
     * 标记为已迁移
     */
    public void markAsMigrated() {
        this.isMigrated = true;
        this.migrateTime = LocalDateTime.now();
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return isPendingPayment();
    }

    /**
     * 检查是否可以退款
     */
    public boolean canRefund() {
        return isPaid() || isInService();
    }

    /**
     * 检查是否可以确认完成
     */
    public boolean canComplete() {
        return isInService();
    }

    /**
     * 获取服务描述
     */
    public String getServiceDescription() {
        return data != null ? (String) data.get("service_description") : null;
    }

    /**
     * 设置服务描述
     */
    public void setServiceDescription(String description) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("service_description", description);
    }

    /**
     * 获取服务要求
     */
    public String getServiceRequirements() {
        return data != null ? (String) data.get("service_requirements") : null;
    }

    /**
     * 设置服务要求
     */
    public void setServiceRequirements(String requirements) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("service_requirements", requirements);
    }
}
