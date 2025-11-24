package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计VO
 * User Statistics VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User statistics")
public class UserStatsVo {

    @Schema(description = "Following count")
    private Integer followingCount;

    @Schema(description = "Fans count")
    private Integer fansCount;

    @Schema(description = "Likes count")
    private Integer likesCount;

    @Schema(description = "Moments count")
    private Integer momentsCount;

    @Schema(description = "Posts count")
    private Integer postsCount;

    @Schema(description = "Collections count")
    private Integer collectionsCount;

    @Schema(description = "Skills count")
    private Integer skillsCount;

    @Schema(description = "Orders count")
    private Integer ordersCount;
}
