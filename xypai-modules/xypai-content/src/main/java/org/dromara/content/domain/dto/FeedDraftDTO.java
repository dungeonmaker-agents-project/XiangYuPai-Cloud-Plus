package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 保存草稿请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "保存草稿请求")
public class FeedDraftDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "草稿ID(更新时必传)")
    private Long id;

    @Schema(description = "标题(0-50字符)")
    @Size(max = 50, message = "标题不能超过50字符")
    private String title;

    @Schema(description = "内容(0-2000字符)")
    @Size(max = 2000, message = "内容不能超过2000字符")
    private String content;

    @Schema(description = "媒体列表")
    private List<Map<String, Object>> mediaList;

    @Schema(description = "话题列表")
    private List<Map<String, Object>> topicList;

    @Schema(description = "地点ID")
    private String locationId;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "详细地址")
    private String locationAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "可见范围: 0=公开,1=仅好友,2=仅自己")
    private Integer visibility;

}
