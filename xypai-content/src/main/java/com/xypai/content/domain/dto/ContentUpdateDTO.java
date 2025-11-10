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
import java.util.List;
import java.util.Map;

/**
 * 内容更新DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容更新DTO")
public class ContentUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    @Schema(description = "内容ID", required = true)
    @NotNull(message = "内容ID不能为空")
    private Long id;

    /**
     * 内容标题
     */
    @Schema(description = "内容标题")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    /**
     * 内容摘要
     */
    @Schema(description = "内容摘要")
    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    /**
     * 内容正文
     */
    @Schema(description = "内容正文")
    private String content;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    private String coverImage;

    /**
     * 图片列表
     */
    @Schema(description = "图片列表")
    private List<String> images;

    /**
     * 视频URL
     */
    @Schema(description = "视频URL")
    private String videoUrl;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 位置信息
     */
    @Schema(description = "位置信息")
    private String location;

    /**
     * 内容状态(0=草稿,1=发布,2=下架)
     */
    @Schema(description = "内容状态(0=草稿,1=发布,2=下架)")
    private Integer status;

    /**
     * 活动相关字段 - 活动开始时间
     */
    @Schema(description = "活动开始时间")
    private String activityStartTime;

    /**
     * 活动相关字段 - 活动结束时间
     */
    @Schema(description = "活动结束时间")
    private String activityEndTime;

    /**
     * 活动相关字段 - 活动地点
     */
    @Schema(description = "活动地点")
    private String activityLocation;

    /**
     * 活动相关字段 - 报名人数限制
     */
    @Schema(description = "报名人数限制")
    private Integer activityMaxParticipants;

    /**
     * 技能相关字段 - 技能分类
     */
    @Schema(description = "技能分类")
    private String skillCategory;

    /**
     * 技能相关字段 - 技能等级
     */
    @Schema(description = "技能等级")
    private String skillLevel;

    /**
     * 技能相关字段 - 价格(分)
     */
    @Schema(description = "技能价格,单位:分")
    private Long skillPrice;

    /**
     * 版本号(乐观锁)
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;
}
