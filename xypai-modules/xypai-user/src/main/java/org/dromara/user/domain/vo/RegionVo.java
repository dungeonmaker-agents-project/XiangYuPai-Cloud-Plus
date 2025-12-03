package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地区数据VO
 * Region data VO for city picker
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Region data response")
public class RegionVo {

    @Schema(description = "Region code")
    private String code;

    @Schema(description = "Region name")
    private String name;

    @Schema(description = "Has children regions")
    private Boolean hasChildren;
}
