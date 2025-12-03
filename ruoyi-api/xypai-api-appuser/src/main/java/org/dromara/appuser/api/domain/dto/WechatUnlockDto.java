package org.dromara.appuser.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

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
public class WechatUnlockDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前用户ID（解锁者）
     */
    private Long userId;

    /**
     * 目标用户ID（被解锁者）
     */
    private Long targetUserId;

    /**
     * 解锁方式: coins-金币, vip-VIP免费
     */
    private String unlockType;

    /**
     * 支付密码（使用金币解锁时需要）
     */
    private String paymentPassword;
}
