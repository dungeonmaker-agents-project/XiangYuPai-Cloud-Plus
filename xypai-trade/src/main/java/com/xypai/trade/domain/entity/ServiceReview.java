package com.xypai.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 服务评价实体
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("service_review")
public class ServiceReview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评价记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID
     */
    @TableField("order_id")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 关联内容ID（服务/活动）
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * 服务类型（1=游戏陪玩,2=生活服务,3=活动报名）
     */
    @TableField("service_type")
    @NotNull(message = "服务类型不能为空")
    private Integer serviceType;

    /**
     * 评价人ID（买家）
     */
    @TableField("reviewer_id")
    @NotNull(message = "评价人ID不能为空")
    private Long reviewerId;

    /**
     * 被评价人ID（卖家）
     */
    @TableField("reviewee_id")
    @NotNull(message = "被评价人ID不能为空")
    private Long revieweeId;

    /**
     * 综合评分（1.00-5.00，必填）
     */
    @TableField("rating_overall")
    @NotNull(message = "综合评分不能为空")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingOverall;

    /**
     * 服务评分（1.00-5.00，可选）
     */
    @TableField("rating_service")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingService;

    /**
     * 态度评分（1.00-5.00，可选）
     */
    @TableField("rating_attitude")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingAttitude;

    /**
     * 质量评分（1.00-5.00，可选）
     */
    @TableField("rating_quality")
    @DecimalMin(value = "1.0", message = "评分不能低于1.0")
    @DecimalMax(value = "5.0", message = "评分不能高于5.0")
    private BigDecimal ratingQuality;

    /**
     * 评价文字内容（最多1000字）
     */
    @TableField("review_text")
    @Size(max = 1000, message = "评价内容不能超过1000字")
    private String reviewText;

    /**
     * 评价图片URLs（逗号分隔，最多9张）
     */
    @TableField("review_images")
    private String reviewImages;

    /**
     * 是否匿名评价
     */
    @TableField("is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    /**
     * 点赞数量（其他用户可以点赞评价）
     */
    @TableField("like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 商家回复内容（最多500字）
     */
    @TableField("reply_text")
    @Size(max = 500, message = "回复内容不能超过500字")
    private String replyText;

    /**
     * 回复时间
     */
    @TableField("reply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    /**
     * 评价状态（0=待审核,1=已发布,2=已隐藏,3=已删除）
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 评价时间
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
     * 评价状态枚举
     */
    public enum Status {
        PENDING_REVIEW(0, "待审核"),
        PUBLISHED(1, "已发布"),
        HIDDEN(2, "已隐藏"),
        DELETED(3, "已删除");

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

    // ==========================================
    // 业务方法
    // ==========================================

    /**
     * 获取评分星级（1-5星）
     */
    public int getStarLevel() {
        if (ratingOverall == null) return 0;
        return ratingOverall.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * 获取评分等级描述
     */
    public String getRatingLevel() {
        if (ratingOverall == null) return "未评分";
        
        BigDecimal rating = ratingOverall;
        if (rating.compareTo(BigDecimal.valueOf(4.5)) >= 0) {
            return "5星（好评）";
        } else if (rating.compareTo(BigDecimal.valueOf(3.5)) >= 0) {
            return "4星（中评）";
        } else {
            return "1-3星（差评）";
        }
    }

    /**
     * 是否好评（4.5星及以上）
     */
    public boolean isGoodReview() {
        return ratingOverall != null && ratingOverall.compareTo(BigDecimal.valueOf(4.5)) >= 0;
    }

    /**
     * 是否中评（3.5-4.5星）
     */
    public boolean isMediumReview() {
        if (ratingOverall == null) return false;
        return ratingOverall.compareTo(BigDecimal.valueOf(3.5)) >= 0 && 
               ratingOverall.compareTo(BigDecimal.valueOf(4.5)) < 0;
    }

    /**
     * 是否差评（3.5星以下）
     */
    public boolean isBadReview() {
        return ratingOverall != null && ratingOverall.compareTo(BigDecimal.valueOf(3.5)) < 0;
    }

    /**
     * 是否有图评价
     */
    public boolean hasImages() {
        return reviewImages != null && !reviewImages.trim().isEmpty();
    }

    /**
     * 获取图片列表
     */
    public List<String> getImageList() {
        if (!hasImages()) {
            return List.of();
        }
        return Arrays.asList(reviewImages.split(","));
    }

    /**
     * 设置图片列表
     */
    public void setImageList(List<String> images) {
        if (images == null || images.isEmpty()) {
            this.reviewImages = null;
        } else {
            // 最多9张图片
            int limit = Math.min(images.size(), 9);
            this.reviewImages = String.join(",", images.subList(0, limit));
        }
    }

    /**
     * 是否已回复
     */
    public boolean hasReply() {
        return replyText != null && !replyText.trim().isEmpty();
    }

    /**
     * 添加商家回复
     */
    public void addReply(String reply) {
        this.replyText = reply;
        this.replyTime = LocalDateTime.now();
    }

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return Status.PUBLISHED.getCode().equals(this.status);
    }

    /**
     * 是否待审核
     */
    public boolean isPending() {
        return Status.PENDING_REVIEW.getCode().equals(this.status);
    }

    /**
     * 是否已隐藏
     */
    public boolean isHidden() {
        return Status.HIDDEN.getCode().equals(this.status);
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
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status reviewStatus = Status.fromCode(this.status);
        return reviewStatus != null ? reviewStatus.getDesc() : "未知";
    }

    /**
     * 点赞
     */
    public void like() {
        this.likeCount = (this.likeCount != null ? this.likeCount : 0) + 1;
    }

    /**
     * 取消点赞
     */
    public void unlike() {
        this.likeCount = Math.max(0, (this.likeCount != null ? this.likeCount : 1) - 1);
    }

    /**
     * 发布评价
     */
    public void publish() {
        this.status = Status.PUBLISHED.getCode();
    }

    /**
     * 隐藏评价
     */
    public void hide() {
        this.status = Status.HIDDEN.getCode();
    }

    /**
     * 删除评价（软删除）
     */
    public void delete() {
        this.status = Status.DELETED.getCode();
    }

    /**
     * 计算平均分维度评分（如果有）
     */
    public BigDecimal getAverageDimensionRating() {
        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
        
        if (ratingService != null) {
            total = total.add(ratingService);
            count++;
        }
        if (ratingAttitude != null) {
            total = total.add(ratingAttitude);
            count++;
        }
        if (ratingQuality != null) {
            total = total.add(ratingQuality);
            count++;
        }
        
        return count > 0 ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : null;
    }

    /**
     * 格式化显示评分
     */
    public String getFormattedRating() {
        if (ratingOverall == null) return "未评分";
        return ratingOverall.setScale(1, RoundingMode.HALF_UP).toString() + "星";
    }
}

