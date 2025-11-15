package org.dromara.common.location.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.location.domain.entity.City;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 城市信息视图对象
 * City Info VO
 *
 * @author XiangYuPai Team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = City.class)
public class CityInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 城市ID
     */
    @ExcelProperty(value = "城市ID")
    private Long id;

    /**
     * 城市代码
     */
    @ExcelProperty(value = "城市代码")
    private String cityCode;

    /**
     * 城市名称
     */
    @ExcelProperty(value = "城市名称")
    private String cityName;

    /**
     * 省份
     */
    @ExcelProperty(value = "省份")
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

    /**
     * 状态
     */
    private Integer status;
}
