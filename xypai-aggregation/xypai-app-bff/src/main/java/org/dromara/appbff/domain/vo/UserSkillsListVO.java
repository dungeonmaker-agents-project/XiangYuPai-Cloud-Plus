package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户技能列表VO
 * 对应UI文档中的 SkillsListData
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户技能列表")
public class UserSkillsListVO {

    @Schema(description = "技能列表")
    private List<SkillItemVO> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    /**
     * 技能项VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "技能项")
    public static class SkillItemVO {

        @Schema(description = "技能ID")
        private Long id;

        @Schema(description = "媒体数据")
        private MediaDataVO mediaData;

        @Schema(description = "提供者信息")
        private ProviderVO providerData;

        @Schema(description = "技能信息")
        private SkillInfoVO skillInfo;

        @Schema(description = "价格数据")
        private PriceVO priceData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "媒体数据")
    public static class MediaDataVO {
        @Schema(description = "封面图URL")
        private String coverUrl;

        @Schema(description = "图片列表")
        private List<String> images;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "提供者信息")
    public static class ProviderVO {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "技能信息")
    public static class SkillInfoVO {
        @Schema(description = "技能名称")
        private String name;

        @Schema(description = "游戏段位")
        private String rank;

        @Schema(description = "标签列表")
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "价格")
    public static class PriceVO {
        @Schema(description = "价格金额")
        private BigDecimal amount;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "显示文本")
        private String displayText;
    }
}
