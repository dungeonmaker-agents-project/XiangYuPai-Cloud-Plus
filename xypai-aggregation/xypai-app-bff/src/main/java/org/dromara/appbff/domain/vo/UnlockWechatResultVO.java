package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 解锁微信结果VO
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "解锁微信结果")
public class UnlockWechatResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "微信号（解锁成功后返回）")
    private String wechat;

    @Schema(description = "扣费金额")
    private BigDecimal cost;

    @Schema(description = "剩余金币")
    private BigDecimal remainingCoins;

    @Schema(description = "失败原因")
    private String failReason;
}
