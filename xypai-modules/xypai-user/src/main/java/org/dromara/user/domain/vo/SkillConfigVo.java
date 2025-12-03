package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 技能配置VO - 添加技能页面使用
 * Skill Configuration VO for Add Skill Page
 *
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * @author XiangYuPai
 * @since 2025-11-14
 * @updated 2025-12-02 - 匹配新UI文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Skill configuration for Add Skill page")
public class SkillConfigVo {

    @Schema(description = "技能列表（用于技能选择器网格）")
    private List<SkillItem> skills;

    @Schema(description = "段位选项配置")
    private RankOptions rankOptions;

    @Schema(description = "时间选项配置")
    private TimeOptions timeOptions;

    /**
     * 技能项 - 对应UI中的SkillCard
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Skill item for selector grid")
    public static class SkillItem {

        @Schema(description = "技能配置ID")
        private String id;

        @Schema(description = "技能名称")
        private String name;

        @Schema(description = "技能图标URL")
        private String icon;

        @Schema(description = "技能类型: online=线上, offline=线下")
        private String type;

        @Schema(description = "分类")
        private String category;
    }

    /**
     * 段位选项 - 对应UI中的RankPickerModal
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Rank options for online skills")
    public static class RankOptions {

        @Schema(description = "服务区列表: ['QQ区', '微信区']")
        private List<String> servers;

        @Schema(description = "各游戏的段位配置，key=skillId")
        private Map<String, List<String>> ranksBySkill;
    }

    /**
     * 时间选项 - 对应UI中的TimePickerModal
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Time options for offline skills")
    public static class TimeOptions {

        @Schema(description = "开始小时 (0-23)")
        private Integer startHour;

        @Schema(description = "结束小时 (0-23)")
        private Integer endHour;

        @Schema(description = "时间间隔（分钟）")
        private Integer intervalMinutes;
    }

    // ============ 旧版兼容字段（保留向后兼容） ============

    @Schema(description = "游戏列表 (deprecated, use skills instead)")
    @Deprecated
    private List<GameInfo> games;

    @Schema(description = "服务类型列表 (deprecated, use skills instead)")
    @Deprecated
    private List<ServiceTypeInfo> serviceTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Game information (deprecated)")
    @Deprecated
    public static class GameInfo {

        @Schema(description = "Game ID")
        private String gameId;

        @Schema(description = "Game name")
        private String gameName;

        @Schema(description = "Rank list")
        private List<String> ranks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Service type information (deprecated)")
    @Deprecated
    public static class ServiceTypeInfo {

        @Schema(description = "Type ID")
        private String typeId;

        @Schema(description = "Type name")
        private String typeName;

        @Schema(description = "Icon URL")
        private String icon;
    }
}
