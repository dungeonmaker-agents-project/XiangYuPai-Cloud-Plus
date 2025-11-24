package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 技能列表项VO
 * Skill List Item VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Skill list item")
public class SkillVo {

    @Schema(description = "Skill ID")
    private Long skillId;

    @Schema(description = "Skill name")
    private String skillName;

    @Schema(description = "Skill type: online, offline")
    private String skillType;

    @Schema(description = "Cover image URL")
    private String coverImage;

    @Schema(description = "Price")
    private BigDecimal price;

    @Schema(description = "Price unit: 局, 小时")
    private String priceUnit;

    @Schema(description = "Is online")
    private Boolean isOnline;

    @Schema(description = "Rating (0-5)")
    private BigDecimal rating;

    @Schema(description = "Review count")
    private Integer reviewCount;

    @Schema(description = "Order count")
    private Integer orderCount;

    @Schema(description = "Game name (for online skills)")
    private String gameName;

    @Schema(description = "Game rank (for online skills)")
    private String gameRank;

    @Schema(description = "Service type (for offline skills)")
    private String serviceType;

    @Schema(description = "Service location (for offline skills)")
    private String serviceLocation;

    @Schema(description = "Distance (km, for nearby search)")
    private BigDecimal distance;
}
