package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点赞列表VO
 * Like List VO
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Like list response")
public class LikeListVo {

    @Schema(description = "Likes")
    private List<LikeVo> likes;

    @Schema(description = "Total count")
    private Long total;

    @Schema(description = "Has more data")
    private Boolean hasMore;
}
