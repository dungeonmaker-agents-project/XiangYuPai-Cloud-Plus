package com.xypai.content.domain.dto;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 内容查询DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "内容查询DTO")
public class ContentQueryDTO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long userId;

    /**
     * 内容类型(1=动态,2=活动,3=技术)
     */
    @Schema(description = "内容类型(1=动态,2=活动,3=技术)")
    private Integer type;

    /**
     * 内容状态(0=草稿,1=发布,2=下架)
     */
    @Schema(description = "内容状态(0=草稿,1=发布,2=下架)")
    private Integer status;

    /**
     * 标题关键词
     */
    @Schema(description = "标题关键词")
    private String title;

    /**
     * 标签筛选
     */
    @Schema(description = "标签筛选")
    private String tags;

    /**
     * 是否只查询我关注的用户内容
     */
    @Schema(description = "是否只查询我关注的用户内容")
    private Boolean followingOnly;

    /**
     * 排序方式(latest=最新, popular=热门, recommended=推荐)
     */
    @Schema(description = "排序方式(latest=最新, popular=热门, recommended=推荐)")
    private String orderBy;

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
}
