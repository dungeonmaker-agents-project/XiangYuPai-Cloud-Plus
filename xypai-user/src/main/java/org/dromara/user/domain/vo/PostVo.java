package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态VO
 * Post/Moment VO
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Post/Moment response")
public class PostVo {

    @Schema(description = "Post ID")
    private String postId;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Content")
    private String content;

    @Schema(description = "Images")
    private List<String> images;

    @Schema(description = "Video info")
    private VideoVo video;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;

    @Schema(description = "Statistics")
    private PostStatsVo stats;

    @Schema(description = "Is liked by current user")
    private Boolean isLiked;

    @Schema(description = "Is favorited by current user")
    private Boolean isFavorited;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Video info")
    public static class VideoVo {
        @Schema(description = "Video URL")
        private String url;

        @Schema(description = "Cover URL")
        private String coverUrl;

        @Schema(description = "Duration (seconds)")
        private Integer duration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Post statistics")
    public static class PostStatsVo {
        @Schema(description = "Comments count")
        private Integer commentsCount;

        @Schema(description = "Likes count")
        private Integer likesCount;

        @Schema(description = "Favorites count")
        private Integer favoritesCount;
    }
}
