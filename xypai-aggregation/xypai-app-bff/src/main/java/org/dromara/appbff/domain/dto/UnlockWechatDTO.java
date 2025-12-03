package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 解锁微信请求DTO
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "解锁微信请求")
public class UnlockWechatDTO {

    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID", required = true)
    private Long targetUserId;

    @Schema(description = "解锁方式: coins-金币, vip-VIP免费")
    private String unlockType;

    @Schema(description = "支付密码（使用金币时需要）")
    private String paymentPassword;
}
