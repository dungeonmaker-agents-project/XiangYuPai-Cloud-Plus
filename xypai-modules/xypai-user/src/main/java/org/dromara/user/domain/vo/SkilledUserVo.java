package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 有技能用户VO
 *
 * @author XyPai Team
 * @date 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "有技能用户信息")
public class SkilledUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "性别: male/female", example = "female")
    private String gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "距离(米)", example = "3200")
    private Integer distance;

    @Schema(description = "距离显示文本", example = "3.2km")
    private String distanceText;

    @Schema(description = "标签列表")
    private List<UserTag> tags;

    @Schema(description = "描述文字")
    private String description;

    @Schema(description = "价格信息")
    private PriceInfo price;

    @Schema(description = "促销标签", example = "限时特价")
    private String promotionTag;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "技能等级", example = "大神")
    private String skillLevel;

    @Schema(description = "技能ID")
    private Long skillId;

    @Schema(description = "技能名称")
    private String skillName;

    /**
     * 用户标签
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户标签")
    public static class UserTag implements Serializable {
        @Schema(description = "标签文本")
        private String text;

        @Schema(description = "标签类型: feature(特色), price(价格), skill(技能)")
        private String type;

        @Schema(description = "标签颜色", example = "#FF5722")
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
        @Schema(description = "金额")
        private Integer amount;

        @Schema(description = "单位: per_order(单), per_hour(小时), per_game(局)")
        private String unit;

        @Schema(description = "显示文本", example = "10 金币/单")
        private String displayText;

        @Schema(description = "原价(用于显示划线价)", example = "15")
        private Integer originalPrice;
    }
}
