package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 服务列表查询请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "服务列表查询请求")
public class ServiceListQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "技能类型", example = "王者荣耀", required = true)
    @NotBlank(message = "技能类型不能为空")
    private String skillType;

    @Schema(description = "页码", example = "1", required = true)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "10", required = true)
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize;

    @Schema(description = "Tab类型: glory_king(荣耀王者), online(线上), offline(线下), my(我的)", example = "online")
    private String tabType;

    @Schema(description = "排序方式: smart(智能排序), price_asc(价格最低), rating_desc(评分最高), orders_desc(订单最多)", example = "smart")
    private String sortBy;

    @Schema(description = "筛选条件")
    private ServiceFilterDTO filters;

    /**
     * 筛选条件内部类
     */
    @Data
    @Schema(description = "服务筛选条件")
    public static class ServiceFilterDTO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "在线状态: online(在线), active_3d(3天内活跃), active_7d(7天内活跃)")
        private String status;

        @Schema(description = "性别: all(不限), male(男), female(女)")
        private String gender;

        @Schema(description = "游戏大区", example = "微信区")
        private String gameArea;

        @Schema(description = "段位筛选", example = "[\"荣耀王者\", \"超凡大师\"]")
        private List<String> rank;

        @Schema(description = "价格区间", example = "[\"0-10\", \"10-30\"]")
        private List<String> priceRange;

        @Schema(description = "位置/英雄", example = "[\"打野\", \"中路\"]")
        private List<String> position;

        @Schema(description = "标签筛选", example = "[\"可线上\", \"技术好\"]")
        private List<String> tags;

        @Schema(description = "位置筛选: same_city(同城)")
        private String location;

        @Schema(description = "城市代码", example = "440300")
        private String cityCode;
    }
}
