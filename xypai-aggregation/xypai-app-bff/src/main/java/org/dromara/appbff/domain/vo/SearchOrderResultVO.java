package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 搜索下单Tab结果VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索下单Tab结果")
public class SearchOrderResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数", example = "67")
    private Integer total;

    @Schema(description = "是否有更多数据", example = "true")
    private Boolean hasMore;

    @Schema(description = "服务提供者列表")
    private List<SearchOrderItem> list;

    /**
     * 服务提供者搜索结果项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "服务提供者搜索结果项")
    public static class SearchOrderItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "头像URL", example = "https://...")
        private String avatar;

        @Schema(description = "昵称", example = "王者陪练")
        private String nickname;

        @Schema(description = "性别: male, female", example = "female")
        private String gender;

        @Schema(description = "距离（米）", example = "3200")
        private Integer distance;

        @Schema(description = "距离文本", example = "3.2km")
        private String distanceText;

        @Schema(description = "标签列表")
        private List<UserTag> tags;

        @Schema(description = "服务描述", example = "王者荣耀上分，5年经验")
        private String description;

        @Schema(description = "价格信息")
        private PriceInfo price;

        @Schema(description = "是否在线", example = "true")
        private Boolean isOnline;
    }

    /**
     * 用户标签
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户标签")
    public static class UserTag implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "标签文本", example = "可线上")
        private String text;

        @Schema(description = "标签类型", example = "service")
        private String type;

        @Schema(description = "标签颜色", example = "#7C3AED")
        private String color;
    }

    /**
     * 价格信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "价格信息")
    public static class PriceInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "金额", example = "10")
        private Integer amount;

        @Schema(description = "单位", example = "单")
        private String unit;

        @Schema(description = "显示文本", example = "10 金币/单")
        private String displayText;
    }
}
