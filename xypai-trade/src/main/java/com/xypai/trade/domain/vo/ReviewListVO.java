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
 * 评价列表VO（简化版）
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评价列表VO")
public class ReviewListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "评价人昵称")
    private String reviewerNickname;

    @Schema(description = "评价人头像")
    private String reviewerAvatar;

    @Schema(description = "综合评分")
    private BigDecimal ratingOverall;

    @Schema(description = "星级（1-5星）")
    private Integer starLevel;

    @Schema(description = "评级等级（好评/中评/差评）")
    private String ratingLevel;

    @Schema(description = "评价文字内容（截断版，最多100字）")
    private String reviewText;

    @Schema(description = "评价图片列表（最多显示3张）")
    private List<String> reviewImages;

    @Schema(description = "是否有图")
    private Boolean hasImages;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous;

    @Schema(description = "点赞数量")
    private Integer likeCount;

    @Schema(description = "是否已回复")
    private Boolean hasReply;

    @Schema(description = "商家回复内容（简略版）")
    private String replyText;

    @Schema(description = "评价时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

