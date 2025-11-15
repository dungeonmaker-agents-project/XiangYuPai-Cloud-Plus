package org.dromara.common.location.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 城市分组视图对象
 * City Group VO - 按首字母分组的城市列表
 *
 * @author XiangYuPai Team
 */
@Data
public class CityGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 首字母 (A-Z)
     */
    private String letter;

    /**
     * 该字母下的城市列表
     */
    private List<CityInfoVo> cities;
}
