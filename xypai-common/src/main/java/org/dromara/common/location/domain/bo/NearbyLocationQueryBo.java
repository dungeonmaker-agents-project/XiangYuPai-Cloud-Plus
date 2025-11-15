package org.dromara.common.location.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.location.domain.entity.Location;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 附近地点查询业务对象
 * Nearby Location Query BO
 *
 * @author XiangYuPai Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Location.class, reverseConvertGenerate = false)
public class NearbyLocationQueryBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前位置纬度
     */
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90", message = "纬度必须在-90到90之间")
    private BigDecimal latitude;

    /**
     * 当前位置经度
     */
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180", message = "经度必须在-180到180之间")
    private BigDecimal longitude;

    /**
     * 搜索半径 (单位: km)
     */
    @Min(value = 1, message = "搜索半径最小为1km")
    @Max(value = 20, message = "搜索半径最大为20km")
    private Integer radius = 5;

    /**
     * 地点分类筛选 (可选)
     */
    private String category;

    /**
     * 城市代码筛选 (可选)
     */
    private String cityCode;

    /**
     * 关键词搜索 (可选)
     */
    private String keyword;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 20;
}
