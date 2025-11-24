package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除搜索历史请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Schema(description = "删除搜索历史请求")
public class SearchHistoryDeleteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "要删除的关键词（为空表示删除单条）", example = "王者荣耀")
    private String keyword;

    @Schema(description = "是否清空所有", example = "false")
    private Boolean clearAll;
}
