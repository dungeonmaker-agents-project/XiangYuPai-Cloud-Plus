package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 获取动态列表请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "获取动态列表请求")
public class FeedListQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Tab类型: follow=关注, hot=热门, local=同城", example = "hot")
    private String tabType;

    @Schema(description = "页码(兼容page和pageNum两种参数名)", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "页码(前端使用page参数名)", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page;

    @Schema(description = "每页数量", example = "20")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize;

    @Schema(description = "用户纬度(同城Tab必传)", example = "22.5431")
    private BigDecimal latitude;

    @Schema(description = "用户经度(同城Tab必传)", example = "114.0579")
    private BigDecimal longitude;

    @Schema(description = "搜索半径(km,默认5)", example = "5")
    private Integer radius;

    @Schema(description = "内容类型: 1=动态, 2=活动, 3=技能 (不传则返回所有类型)", example = "3")
    @Min(value = 1, message = "内容类型最小为1")
    @Max(value = 3, message = "内容类型最大为3")
    private Integer type;

    @Schema(description = "排序方式: distance=距离最近, followed=关注的用户, likes=点赞最多 (默认likes)", example = "likes")
    private String sortBy;

    /**
     * 获取实际页码 (兼容 page 和 pageNum 两种参数名)
     */
    public Integer getPageNum() {
        if (pageNum != null) {
            return pageNum;
        }
        if (page != null) {
            return page;
        }
        return 1; // 默认第一页
    }

    /**
     * 获取每页数量 (提供默认值)
     */
    public Integer getPageSize() {
        return pageSize != null ? pageSize : 10; // 默认每页10条
    }

}
