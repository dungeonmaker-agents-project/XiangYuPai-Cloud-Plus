package org.dromara.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计VO
 * User Statistics VO
 *
 * 🔗 前端字段兼容说明：
 * - followerCount = fansCount (粉丝数别名，前端使用)
 * - contentCount = postsCount (内容数别名，前端使用)
 * - totalLikeCount = likesCount (获赞总数别名，前端使用)
 *
 * @author XiangYuPai
 * @since 2025-11-14
 * @updated 2025-11-29 添加前端兼容字段别名
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User statistics")
public class UserStatsVo {

    @Schema(description = "Following count (关注数)")
    private Integer followingCount;

    @Schema(description = "Fans count (粉丝数)")
    private Integer fansCount;

    @Schema(description = "Likes count (获赞总数)")
    private Integer likesCount;

    @Schema(description = "Moments count (动态数-旧字段)")
    private Integer momentsCount;

    @Schema(description = "Posts count (动态数)")
    private Integer postsCount;

    @Schema(description = "Collections count (收藏数)")
    private Integer collectionsCount;

    @Schema(description = "Skills count (技能数)")
    private Integer skillsCount;

    @Schema(description = "Orders count (订单数)")
    private Integer ordersCount;

    // ==================== 前端兼容字段别名 ====================

    /**
     * 粉丝数别名 (前端使用 followerCount)
     * @return fansCount 的值
     */
    @JsonProperty("followerCount")
    @Schema(description = "Follower count (粉丝数别名，前端兼容)")
    public Integer getFollowerCount() {
        return fansCount;
    }

    /**
     * 内容数别名 (前端使用 contentCount)
     * @return postsCount 的值
     */
    @JsonProperty("contentCount")
    @Schema(description = "Content count (内容数别名，前端兼容)")
    public Integer getContentCount() {
        return postsCount;
    }

    /**
     * 获赞总数别名 (前端使用 totalLikeCount)
     * @return likesCount 的值
     */
    @JsonProperty("totalLikeCount")
    @Schema(description = "Total like count (获赞总数别名，前端兼容)")
    public Integer getTotalLikeCount() {
        return likesCount;
    }
}
