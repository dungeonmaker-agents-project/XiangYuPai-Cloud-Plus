package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发现页列表查询DTO
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
@Schema(description = "发现页列表查询")
public class DiscoverListQueryDTO {

    @Schema(description = "Tab类型: follow(关注), hot(热门), nearby(同城)", example = "hot")
    private String tab = "hot";

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    private Integer pageSize = 20;

    @Schema(description = "纬度（同城Tab必填）", example = "22.5431")
    private BigDecimal latitude;

    @Schema(description = "经度（同城Tab必填）", example = "113.9298")
    private BigDecimal longitude;

}
