package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏VO
 * Favorite VO
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Favorite response")
public class FavoriteVo {

    @Schema(description = "Favorite ID")
    private String favoriteId;

    @Schema(description = "Post/Moment")
    private PostVo post;

    @Schema(description = "Favorited time")
    private LocalDateTime favoritedAt;
}
