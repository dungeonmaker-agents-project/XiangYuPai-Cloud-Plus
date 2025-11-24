package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 互动操作请求DTO(点赞/收藏)
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "互动操作请求")
public class InteractionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "目标类型: feed=动态, comment=评论", example = "feed", required = true)
    @NotBlank(message = "目标类型不能为空")
    @Pattern(regexp = "^(feed|comment)$", message = "目标类型必须是feed或comment")
    private String targetType;

    @Schema(description = "目标ID", example = "123456", required = true)
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @Schema(description = "操作类型: like=点赞, unlike=取消点赞, collect=收藏, uncollect=取消收藏", example = "like")
    @Pattern(regexp = "^(like|unlike|collect|uncollect)$", message = "操作类型无效")
    private String action;

    @Schema(description = "分享渠道: wechat=微信, moments=朋友圈, qq=QQ, qzone=QQ空间, weibo=微博, copy_link=复制链接", example = "wechat")
    @Pattern(regexp = "^(wechat|moments|qq|qzone|weibo|copy_link)$", message = "分享渠道无效")
    private String shareChannel;

}
