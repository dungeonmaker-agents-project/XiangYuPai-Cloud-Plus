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
 * 搜索全部Tab结果VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索全部Tab结果")
public class SearchAllResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数", example = "123")
    private Integer total;

    @Schema(description = "是否有更多数据", example = "true")
    private Boolean hasMore;

    @Schema(description = "混合结果列表")
    private List<SearchAllItem> list;

    /**
     * 全部Tab混合结果项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "全部Tab混合结果项")
    public static class SearchAllItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "项目类型: post(动态), video(视频), user(用户)", example = "post")
        private String itemType;

        @Schema(description = "项目ID", example = "123")
        private Long itemId;

        @Schema(description = "动态/视频信息")
        private PostInfo post;

        @Schema(description = "用户信息")
        private UserInfo user;
    }

    /**
     * 动态/视频信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "动态/视频信息")
    public static class PostInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "动态ID", example = "123")
        private Long postId;

        @Schema(description = "标题", example = "王者荣耀陪玩")
        private String title;

        @Schema(description = "描述", example = "专业陪玩，上分快")
        private String description;

        @Schema(description = "缩略图URL", example = "https://...")
        private String thumbnail;

        @Schema(description = "媒体类型: image, video", example = "image")
        private String mediaType;

        @Schema(description = "作者信息")
        private AuthorInfo author;

        @Schema(description = "统计信息")
        private StatsInfo stats;
    }

    /**
     * 作者信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "作者信息")
    public static class AuthorInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "头像URL", example = "https://...")
        private String avatar;

        @Schema(description = "昵称", example = "游戏达人")
        private String nickname;
    }

    /**
     * 统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "统计信息")
    public static class StatsInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "点赞数", example = "88")
        private Integer likes;

        @Schema(description = "评论数", example = "12")
        private Integer comments;
    }

    /**
     * 用户信息（简化版）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "头像URL", example = "https://...")
        private String avatar;

        @Schema(description = "昵称", example = "游戏达人")
        private String nickname;

        @Schema(description = "个性签名", example = "专业陪玩")
        private String signature;
    }
}
