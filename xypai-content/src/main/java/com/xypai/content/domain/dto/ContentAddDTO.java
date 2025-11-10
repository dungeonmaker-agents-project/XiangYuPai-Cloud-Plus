package com.xypai.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 内容新增DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容新增DTO")
public class ContentAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容类型(1=动态,2=活动,3=技能)
     */
    @Schema(description = "内容类型(1=动态,2=活动,3=技能)", required = true)
    @NotNull(message = "内容类型不能为空")
    private Integer type;

    /**
     * 内容标题
     */
    @Schema(description = "内容标题", required = true)
    @NotBlank(message = "内容标题不能为空")
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
     * 位置信息（旧版，保留兼容）
     */
    @Schema(description = "位置信息")
    private String location;

    /**
     * 地点名称（v7.1新增）
     */
    @Schema(description = "地点名称")
    private String locationName;

    /**
     * 详细地址（v7.1新增）
     */
    @Schema(description = "详细地址")
    private String locationAddress;

    /**
     * 经度（v7.1新增）
     */
    @Schema(description = "经度")
    private Double longitude;

    /**
     * 纬度（v7.1新增）
     */
    @Schema(description = "纬度")
    private Double latitude;

    /**
     * 城市ID（v7.1新增）
     */
    @Schema(description = "城市ID")
    private Long cityId;

    /**
     * 是否立即发布(true=发布,false=保存为草稿)
     */
    @Schema(description = "是否立即发布")
    @Builder.Default
    private Boolean publish = true;

    /**
     * 活动相关字段 - 活动开始时间
     */
    @Schema(description = "活动开始时间(活动类型必填)")
    private String activityStartTime;

    /**
     * 活动相关字段 - 活动结束时间
     */
    @Schema(description = "活动结束时间(活动类型必填)")
    private String activityEndTime;

    /**
     * 活动相关字段 - 活动地点
     */
    @Schema(description = "活动地点(活动类型必填)")
    private String activityLocation;

    /**
     * 活动相关字段 - 报名人数限制
     */
    @Schema(description = "报名人数限制(活动类型可选)")
    private Integer activityMaxParticipants;

    /**
     * 技能相关字段 - 技能分类
     */
    @Schema(description = "技能分类(技能类型必填)")
    private String skillCategory;

    /**
     * 技能相关字段 - 技能等级
     */
    @Schema(description = "技能等级(技能类型可选)")
    private String skillLevel;

    /**
     * 技能相关字段 - 价格(分)
     */
    @Schema(description = "技能价格,单位:分(技能类型可选)")
    private Long skillPrice;

    /**
     * 扩展数据
     */
    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;
}
