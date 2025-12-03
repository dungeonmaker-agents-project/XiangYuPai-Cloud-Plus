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

/**
 * 评论列表项VO（仅支持一级评论）
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

    @Schema(description = "是否置顶")
    private Boolean isTop;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否可删除")
    private Boolean canDelete;

    @Schema(description = "评论位置/IP归属地")
    private String location;

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

        @Schema(description = "用户等级: 1-青铜,2-白银,3-黄金,4-铂金,5-钻石,6-大师,7-王者")
        private Integer level;

        @Schema(description = "用户等级名称")
        private String levelName;
    }

}
