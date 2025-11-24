package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 收藏列表VO
 * Favorite List VO
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Favorite list response")
public class FavoriteListVo {

    @Schema(description = "Favorites")
    private List<FavoriteVo> favorites;

    @Schema(description = "Total count")
    private Long total;

    @Schema(description = "Has more data")
    private Boolean hasMore;
}
