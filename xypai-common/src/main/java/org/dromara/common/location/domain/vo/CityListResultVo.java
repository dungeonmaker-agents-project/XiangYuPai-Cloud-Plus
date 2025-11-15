package org.dromara.common.location.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 城市列表结果视图对象
 * City List Result VO - 包含热门城市和全部城市
 *
 * @author XiangYuPai Team
 */
@Data
public class CityListResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热门城市列表
     */
    private List<CityInfoVo> hotCities;

    /**
     * 全部城市分组列表 (按首字母分组)
     */
    private List<CityGroupVo> allCities;
}
