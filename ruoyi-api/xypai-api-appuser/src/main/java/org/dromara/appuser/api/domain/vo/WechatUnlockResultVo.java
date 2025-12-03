package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
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
public class WechatUnlockResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否解锁成功
     */
    private Boolean success;

    /**
     * 完整微信号（解锁成功后返回）
     */
    private String wechat;

    /**
     * 扣费金额
     */
    private BigDecimal cost;

    /**
     * 剩余金币
     */
    private BigDecimal remainingCoins;

    /**
     * 失败原因（解锁失败时返回）
     */
    private String failReason;

    /**
     * 失败代码
     */
    private String failCode;
}
