package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发布评论请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "发布评论请求")
public class CommentPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "动态ID", example = "123456", required = true)
    @NotNull(message = "动态ID不能为空")
    private Long feedId;

    @Schema(description = "评论内容(1-500字符)", example = "写得真好!", required = true)
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字符之间")
    private String content;

    @Schema(description = "父评论ID(回复时传入)", example = "789012")
    private Long parentId;

    @Schema(description = "回复的用户ID(回复时传入)", example = "456789")
    private Long replyToUserId;

}
