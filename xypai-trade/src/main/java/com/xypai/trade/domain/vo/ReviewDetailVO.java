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

/**
 * 评价详情VO
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价详情VO")
public class ReviewDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "内容ID")
    private Long contentId;

    @Schema(description = "服务类型（1=游戏陪玩,2=生活服务,3=活动报名）")
    private Integer serviceType;

    @Schema(description = "服务类型描述")
    private String serviceTypeDesc;

    @Schema(description = "评价人ID")
    private Long reviewerId;

    @Schema(description = "评价人昵称（匿名则为'匿名用户'）")
    private String reviewerNickname;

    @Schema(description = "评价人头像（匿名则不显示）")
    private String reviewerAvatar;

    @Schema(description = "被评价人ID")
    private Long revieweeId;

    @Schema(description = "被评价人昵称")
    private String revieweeNickname;

    @Schema(description = "综合评分")
    private BigDecimal ratingOverall;

    @Schema(description = "服务评分")
    private BigDecimal ratingService;

    @Schema(description = "态度评分")
    private BigDecimal ratingAttitude;

    @Schema(description = "质量评分")
    private BigDecimal ratingQuality;

    @Schema(description = "星级（1-5星）")
    private Integer starLevel;

    @Schema(description = "评级等级（好评/中评/差评）")
    private String ratingLevel;

    @Schema(description = "评价文字内容")
    private String reviewText;

    @Schema(description = "评价图片列表")
    private List<String> reviewImages;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous;

    @Schema(description = "点赞数量")
    private Integer likeCount;

    @Schema(description = "商家回复内容")
    private String replyText;

    @Schema(description = "回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    @Schema(description = "是否已回复")
    private Boolean hasReply;

    @Schema(description = "评价状态（0=待审核,1=已发布,2=已隐藏,3=已删除）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "评价时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

