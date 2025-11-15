package org.dromara.common.location.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 城市实体
 * City Entity
 *
 * @author XiangYuPai Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("city")
public class City extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 城市ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 城市代码 (行政区划代码)
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
     * 是否热门城市: 0=否, 1=是
     */
    private Integer isHot;

    /**
     * 是否包含区县数据: 0=否, 1=是
     */
    private Integer hasDistricts;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态: 0=禁用, 1=正常
     */
    private Integer status;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(value = "deleted")
    private Long deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(value = "version")
    private Long version;
}
