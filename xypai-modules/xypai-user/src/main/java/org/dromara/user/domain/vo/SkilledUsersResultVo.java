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
 * 有技能用户列表结果VO
 *
 * @author XyPai Team
 * @date 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "有技能用户列表结果")
public class SkilledUsersResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数")
    private Integer total;

    @Schema(description = "是否有更多数据")
    private Boolean hasMore;

    @Schema(description = "筛选选项")
    private FilterOptions filters;

    @Schema(description = "用户列表")
    private List<SkilledUserVo> list;

    /**
     * 筛选选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "筛选选项")
    public static class FilterOptions implements Serializable {

        @Schema(description = "排序选项")
        private List<Option> sortOptions;

        @Schema(description = "性别选项")
        private List<Option> genderOptions;

        @Schema(description = "语言选项")
        private List<Option> languageOptions;
    }

    /**
     * 选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "选项")
    public static class Option implements Serializable {
        @Schema(description = "选项值")
        private String value;

        @Schema(description = "选项标签")
        private String label;
    }
}
