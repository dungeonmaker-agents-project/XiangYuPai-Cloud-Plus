package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 发现页列表结果VO
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发现页列表结果")
public class DiscoverListResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "内容列表")
    private List<DiscoverContentItemVO> list;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "总数")
    private Long total;

}
