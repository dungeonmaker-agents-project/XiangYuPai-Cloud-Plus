package org.dromara.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论列表项VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论列表项")
public class CommentListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "动态ID")
    private Long feedId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "用户信息")
    private UserInfoVO userInfo;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "是否置顶")
    private Boolean isTop;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "二级回复列表(前3条)")
    private List<ReplyVO> replies;

    @Schema(description = "总回复数")
    private Integer totalReplies;

    @Schema(description = "是否有更多回复")
    private Boolean hasMoreReplies;

    @Schema(description = "是否可删除")
    private Boolean canDelete;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 用户信息内嵌VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfoVO implements Serializable {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;
    }

    /**
     * 二级回复内嵌VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "二级回复")
    public static class ReplyVO implements Serializable {
        @Schema(description = "回复ID")
        private Long id;

        @Schema(description = "回复内容")
        private String content;

        @Schema(description = "回复用户信息")
        private UserInfoVO userInfo;

        @Schema(description = "被回复用户昵称")
        private String replyToUserNickname;

        @Schema(description = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

}
