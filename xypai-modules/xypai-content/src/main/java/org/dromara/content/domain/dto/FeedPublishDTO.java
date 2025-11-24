package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发布动态请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "发布动态请求")
public class FeedPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "动态类型: 1=动态,2=活动,3=技能", example = "1")
    @NotNull(message = "动态类型不能为空")
    private Integer type;

    @Schema(description = "标题(0-50字符)", example = "今天的美食")
    @Size(max = 50, message = "标题不能超过50字符")
    private String title;

    @Schema(description = "内容(1-1000字符)", example = "今天去了一家很棒的餐厅...")
    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 1000, message = "内容长度必须在1-1000字符之间")
    private String content;

    @Schema(description = "媒体ID列表(最多9张图或1个视频)", example = "[\"media_001\", \"media_002\"]")
    @Size(max = 9, message = "最多上传9张图片")
    private List<Long> mediaIds;

    @Schema(description = "话题名称列表(最多5个)", example = "[\"探店日记\", \"美食推荐\"]")
    @Size(max = 5, message = "最多选择5个话题")
    private List<String> topicNames;

    @Schema(description = "地点ID", example = "123456")
    private Long locationId;

    @Schema(description = "地点名称", example = "深圳市南山区")
    private String locationName;

    @Schema(description = "详细地址", example = "深圳湾1号")
    private String locationAddress;

    @Schema(description = "经度", example = "114.0579")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "22.5431")
    private BigDecimal latitude;

    @Schema(description = "可见范围: 0=公开,1=仅好友,2=仅自己", example = "0")
    private Integer visibility;

}
