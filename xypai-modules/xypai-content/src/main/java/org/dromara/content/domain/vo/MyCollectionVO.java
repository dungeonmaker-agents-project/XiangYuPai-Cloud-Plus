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
 * 我的收藏列表项VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的收藏列表项")
public class MyCollectionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏记录ID")
    private Long id;

    @Schema(description = "收藏类型: feed=动态")
    private String targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标内容(动态内容摘要)")
    private String targetContent;

    @Schema(description = "目标封面(动态第一张图片)")
    private String targetCover;

    @Schema(description = "目标作者信息")
    private AuthorVO author;

    @Schema(description = "收藏时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;

    /**
     * 作者信息内嵌VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "作者信息")
    public static class AuthorVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;
    }

}
