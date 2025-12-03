package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 有技能用户查询参数DTO
 *
 * @author XyPai Team
 * @date 2025-11-30
 */
@Data
@Schema(description = "有技能用户查询参数")
public class SkilledUsersQueryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "性别筛选: all(不限), male(男), female(女)", example = "all")
    private String gender = "all";

    @Schema(description = "排序方式: smart_recommend(智能推荐), price_asc(价格从低到高), price_desc(价格从高到低), distance_asc(距离最近)",
        example = "smart_recommend")
    private String sortBy = "smart_recommend";

    @Schema(description = "城市代码", example = "440100")
    private String cityCode;

    @Schema(description = "区县代码", example = "440103")
    private String districtCode;

    @Schema(description = "用户纬度（用于计算距离）")
    private Double latitude;

    @Schema(description = "用户经度（用于计算距离）")
    private Double longitude;
}
