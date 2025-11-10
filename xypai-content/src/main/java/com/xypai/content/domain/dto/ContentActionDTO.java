package com.xypai.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 内容行为操作DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容行为操作DTO")
public class ContentActionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    @Schema(description = "内容ID", required = true)
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 行为类型(1=点赞,2=评论,3=分享,4=收藏,5=举报,6=报名,7=查看,8=关注)
     */
    @Schema(description = "行为类型", required = true)
    @NotNull(message = "行为类型不能为空")
    private Integer action;

    /**
     * 评论内容(评论行为必填)
     */
    @Schema(description = "评论内容")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String commentContent;

    /**
     * 回复的评论ID(回复评论时填写)
     */
    @Schema(description = "回复的评论ID")
    private Long replyToId;

    /**
     * 回复的用户ID(回复评论时填写)
     */
    @Schema(description = "回复的用户ID")
    private Long replyToUserId;

    /**
     * 报名信息(报名行为时填写)
     */
    @Schema(description = "报名信息")
    private Map<String, Object> signupInfo;

    /**
     * 举报理由(举报行为时填写)
     */
    @Schema(description = "举报理由")
    private String reportReason;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;
}
