package org.dromara.common.api.location.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 地理位置点传输对象
 * Location Point DTO
 *
 * @author XiangYuPai Team
 */
@Data
public class LocationPointDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标ID（可选）
     */
    private Long id;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 名称（可选）
     */
    private String name;
}
