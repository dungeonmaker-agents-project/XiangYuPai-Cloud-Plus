package org.dromara.common.api.location.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 距离信息视图对象
 * Distance VO
 *
 * @author XiangYuPai Team
 */
@Data
public class DistanceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标ID
     */
    private Long id;

    /**
     * 距离（单位：km）
     */
    private BigDecimal distance;

    /**
     * 单位
     */
    private String unit = "km";

    /**
     * 格式化显示文本（如："1.2km", "500m"）
     */
    private String displayText;
}
