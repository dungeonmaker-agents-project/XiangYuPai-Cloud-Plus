package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 限时专享查询请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Schema(description = "限时专享查询请求")
public class LimitedTimeQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "城市代码", example = "440100")
    private String cityCode;

    @Schema(description = "区域代码", example = "440103")
    private String districtCode;

    @Schema(description = "页码", example = "1", required = true)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "10", required = true)
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize;

    @Schema(description = "排序方式: smart_recommend(智能推荐), price_asc(价格从低到高), price_desc(价格从高到低), distance_asc(距离最近)",
            example = "smart_recommend")
    private String sortBy;

    @Schema(description = "性别筛选: all(不限), male(男), female(女)", example = "all")
    private String gender;

    @Schema(description = "语言筛选: all(不限), mandarin(普通话), cantonese(粤语), english(英语)", example = "all")
    private String language;
}
