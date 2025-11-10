package com.xypai.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 内容行为查询DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容行为查询DTO")
public class ContentActionQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    @Schema(description = "内容ID")
    private Long contentId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 行为类型
     */
    @Schema(description = "行为类型")
    private Integer action;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String beginTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;

    /**
     * 只查询评论内容
     */
    @Schema(description = "只查询评论内容")
    private Boolean commentsOnly;

    /**
     * 父评论ID(查询某条评论的回复)
     */
    @Schema(description = "父评论ID")
    private Long parentCommentId;
}
