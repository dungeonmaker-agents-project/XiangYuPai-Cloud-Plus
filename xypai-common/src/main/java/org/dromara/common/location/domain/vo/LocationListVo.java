package org.dromara.common.location.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.location.domain.entity.Location;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 地点列表视图对象
 * Location List VO
 *
 * @author XiangYuPai Team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Location.class)
public class LocationListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地点ID
     */
    @ExcelProperty(value = "地点ID")
    private Long id;

    /**
     * 地点名称
     */
    @ExcelProperty(value = "地点名称")
    private String name;

    /**
     * 详细地址
     */
    @ExcelProperty(value = "详细地址")
    private String address;

    /**
     * 纬度
     */
    @ExcelProperty(value = "纬度")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @ExcelProperty(value = "经度")
    private BigDecimal longitude;

    /**
     * 地点分类
     */
    @ExcelProperty(value = "分类")
    private String category;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    @ExcelProperty(value = "城市")
    private String city;

    /**
     * 区县
     */
    @ExcelProperty(value = "区县")
    private String district;

    /**
     * 距离当前位置的距离 (单位: km)
     */
    private BigDecimal distance;

    /**
     * 格式化的距离文本 (如: "1.2km", "500m")
     */
    private String distanceText;

    /**
     * 状态: 0=正常, 1=禁用
     */
    private Integer status;
}
