package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 搜索查询请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Schema(description = "搜索查询请求")
public class SearchQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词", example = "王者荣耀", required = true)
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Schema(description = "搜索类型: all(全部), user(用户), order(下单), topic(话题)", example = "all", required = true)
    @NotBlank(message = "搜索类型不能为空")
    private String type;

    @Schema(description = "页码", example = "1", required = true)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "10", required = true)
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize;

    @Schema(description = "城市代码", example = "440100")
    private String cityCode;

    @Schema(description = "区域代码", example = "440103")
    private String districtCode;

    @Schema(description = "性别筛选: all(不限), male(男), female(女)", example = "all")
    private String gender;

    @Schema(description = "排序方式", example = "smart")
    private String sortBy;
}
