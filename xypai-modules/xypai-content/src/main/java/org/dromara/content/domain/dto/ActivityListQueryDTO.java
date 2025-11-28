package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动列表查询请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "活动列表查询请求")
public class ActivityListQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;

    @Schema(description = "排序方式: smart_recommend=智能推荐, newest=最新发布, distance_asc=距离最近, start_time_asc=即将开始", example = "smart_recommend")
    private String sortBy;

    @Schema(description = "活动类型编码", example = "billiards")
    private String typeCode;

    @Schema(description = "性别筛选: all=全部, male=仅男, female=仅女", example = "all")
    private String gender;

    @Schema(description = "人数范围", example = "2-4")
    private String memberCount;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区县", example = "南山区")
    private String district;

    @Schema(description = "用户纬度(用于距离排序)", example = "22.5431")
    private BigDecimal latitude;

    @Schema(description = "用户经度(用于距离排序)", example = "114.0579")
    private BigDecimal longitude;
}
