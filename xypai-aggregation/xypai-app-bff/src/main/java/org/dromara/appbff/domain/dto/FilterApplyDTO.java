package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 应用筛选条件请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Schema(description = "应用筛选条件请求")
public class FilterApplyDTO {

    @Schema(description = "类型: online-线上, offline-线下")
    @NotBlank(message = "类型不能为空")
    private String type;

    @Schema(description = "筛选条件")
    @NotNull(message = "筛选条件不能为空")
    private FilterCriteria filters;

    @Schema(description = "页码", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "10")
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize;

    /**
     * 筛选条件
     */
    @Data
    @Schema(description = "筛选条件")
    public static class FilterCriteria {

        @Schema(description = "年龄范围")
        private AgeRange age;

        @Schema(description = "性别: all-全部, male-男, female-女")
        private String gender;

        @Schema(description = "状态: online-在线, active_3d-近三天活跃, active_7d-近七天活跃")
        private String status;

        @Schema(description = "技能列表")
        private List<String> skills;

        @Schema(description = "价格范围列表(仅线上)")
        private List<String> priceRange;

        @Schema(description = "位置列表(仅线上)")
        private List<String> positions;

        @Schema(description = "标签列表")
        private List<String> tags;
    }

    /**
     * 年龄范围
     */
    @Data
    @Schema(description = "年龄范围")
    public static class AgeRange {

        @Schema(description = "最小年龄", example = "18")
        @Min(value = 18, message = "最小年龄不能低于18岁")
        private Integer min;

        @Schema(description = "最大年龄(null表示不限)", example = "35")
        private Integer max;
    }
}
