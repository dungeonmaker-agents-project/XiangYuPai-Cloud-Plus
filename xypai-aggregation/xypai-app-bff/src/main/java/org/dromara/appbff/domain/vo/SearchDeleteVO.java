package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除搜索历史响应VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除搜索历史结果")
public class SearchDeleteVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "提示信息", example = "删除成功")
    private String message;
}
