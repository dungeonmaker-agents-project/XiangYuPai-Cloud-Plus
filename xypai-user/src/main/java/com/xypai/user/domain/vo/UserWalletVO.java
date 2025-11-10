package com.xypai.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户钱包VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 余额(元)
     */
    private String balance;

    /**
     * 余额(分)
     */
    private Long balanceFen;

    /**
     * 是否可用
     */
    private Boolean available;

    /**
     * 版本号
     */
    private Integer version;
}
