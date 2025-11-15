package org.dromara.common.api.location.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 城市信息视图对象
 * City Info VO (for API)
 *
 * @author XiangYuPai Team
 */
@Data
public class CityInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 城市ID
     */
    private Long id;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 省份
     */
    private String province;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 首字母
     */
    private String firstLetter;

    /**
     * 中心点纬度
     */
    private BigDecimal centerLat;

    /**
     * 中心点经度
     */
    private BigDecimal centerLng;

    /**
     * 是否热门城市
     */
    private Integer isHot;
}
