package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Tab类型: follow(关注), hot(热门), nearby(同城)")
    private String tab = "hot";

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;

    @Schema(description = "纬度（同城Tab必填）")
    private BigDecimal latitude;

    @Schema(description = "经度（同城Tab必填）")
    private BigDecimal longitude;

    @Schema(description = "城市ID")
    private Long cityId;

}
