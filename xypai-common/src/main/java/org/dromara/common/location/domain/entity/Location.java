package org.dromara.common.location.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 地点实体
 * Location Entity
 *
 * @author XiangYuPai Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("location")
public class Location extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地点ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 地点名称
     */
    private String name;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 纬度 (WGS84坐标系)
     */
    private BigDecimal latitude;

    /**
     * 经度 (WGS84坐标系)
     */
    private BigDecimal longitude;

    /**
     * Geohash编码 (用于快速空间查询)
     */
    private String geohash;

    /**
     * 地点分类 (如: 餐厅、商场、景点等)
     */
    private String category;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 街道
     */
    private String street;

    /**
     * 数据来源 (amap/tencent/baidu等)
     */
    private String source;

    /**
     * 额外信息 (JSON格式)
     */
    private String extraInfo;

    /**
     * 状态: 0=正常, 1=禁用
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

    /**
     * 计算距离 (非数据库字段，由Service层计算填充)
     */
    @TableField(exist = false)
    private BigDecimal distance;
}
