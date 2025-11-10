package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易记录VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 交易ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易金额(元)
     */
    private String amount;

    /**
     * 交易金额(分)
     */
    private Long amountFen;

    /**
     * 交易类型
     */
    private String type;

    /**
     * 交易类型描述
     */
    private String typeDesc;

    /**
     * 关联业务ID
     */
    private String refId;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 是否为收入
     */
    private Boolean isIncome;

    /**
     * 格式化金额显示
     */
    private String formattedAmount;
}
