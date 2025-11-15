package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 获取动态列表请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "获取动态列表请求")
public class FeedListQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Tab类型: follow=关注, hot=热门, local=同城", example = "hot")
    @NotNull(message = "Tab类型不能为空")
    private String tabType;

    @Schema(description = "页码", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "20")
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize;

    @Schema(description = "用户纬度(同城Tab必传)", example = "22.5431")
    private BigDecimal latitude;

    @Schema(description = "用户经度(同城Tab必传)", example = "114.0579")
    private BigDecimal longitude;

    @Schema(description = "搜索半径(km,默认5)", example = "5")
    private Integer radius;

}
