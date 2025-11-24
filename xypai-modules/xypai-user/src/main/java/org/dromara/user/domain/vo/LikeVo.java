package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞VO
 * Like VO
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Like response")
public class LikeVo {

    @Schema(description = "Like ID")
    private String likeId;

    @Schema(description = "Post/Moment")
    private PostVo post;

    @Schema(description = "Liked time")
    private LocalDateTime likedAt;
}
