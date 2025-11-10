package com.xypai.content.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 内容草稿保存DTO
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDraftSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 草稿ID（更新时使用）
     */
    private Long id;

    /**
     * 内容类型(1=动态,2=活动,3=技能)
     */
    @NotNull(message = "内容类型不能为空")
    private Integer type;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文内容
     */
    private String content;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 城市ID
     */
    private Long cityId;

    /**
     * 扩展数据（活动、技能特有字段）
     */
    private Map<String, Object> data;
}

