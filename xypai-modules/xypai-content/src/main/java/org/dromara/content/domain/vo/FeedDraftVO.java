package org.dromara.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 动态草稿VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动态草稿")
public class FeedDraftVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "草稿ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
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

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

}
