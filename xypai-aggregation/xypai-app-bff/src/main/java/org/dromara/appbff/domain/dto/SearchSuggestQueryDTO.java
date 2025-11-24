package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 搜索建议查询请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Schema(description = "搜索建议查询请求")
public class SearchSuggestQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词", example = "王者荣耀", required = true)
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Schema(description = "建议数量", example = "10")
    private Integer limit;
}
