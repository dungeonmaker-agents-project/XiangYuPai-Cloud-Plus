package org.dromara.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 互动操作结果VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "互动操作结果")
public class InteractionResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "操作是否成功")
    private Boolean success;

    @Schema(description = "最新计数(点赞数/收藏数)")
    private Integer count;

    @Schema(description = "当前状态(是否已点赞/是否已收藏)")
    private Boolean isActive;

}
