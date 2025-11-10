package com.xypai.trade.domain.dto;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 订单查询DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单查询DTO")
public class OrderQueryDTO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 买家用户ID
     */
    @Schema(description = "买家用户ID")
    private Long buyerId;

    /**
     * 卖家用户ID
     */
    @Schema(description = "卖家用户ID")
    private Long sellerId;

    /**
     * 关联内容ID
     */
    @Schema(description = "关联内容ID")
    private Long contentId;

    /**
     * 订单状态：0=待付款，1=已付款，2=服务中，3=已完成，4=已取消，5=已退款
     */
    @Schema(description = "订单状态")
    private Integer status;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * 最小金额（元）
     */
    @Schema(description = "最小金额（元）")
    private String minAmount;

    /**
     * 最大金额（元）
     */
    @Schema(description = "最大金额（元）")
    private String maxAmount;

    /**
     * 查询类型(my_buy=我购买的, my_sell=我出售的, all=全部)
     */
    @Schema(description = "查询类型")
    private String queryType;

    /**
     * 排序方式(latest=最新, amount_asc=金额升序, amount_desc=金额降序)
     */
    @Schema(description = "排序方式")
    private String orderBy;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String beginTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;
}
