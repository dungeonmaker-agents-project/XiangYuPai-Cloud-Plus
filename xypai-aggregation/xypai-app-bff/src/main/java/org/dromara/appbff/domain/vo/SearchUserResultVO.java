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
 * 搜索用户Tab结果VO
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索用户Tab结果")
public class SearchUserResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数", example = "45")
    private Integer total;

    @Schema(description = "是否有更多数据", example = "true")
    private Boolean hasMore;

    @Schema(description = "用户列表")
    private List<SearchUserItem> list;

    /**
     * 用户搜索结果项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户搜索结果项")
    public static class SearchUserItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "头像URL", example = "https://...")
        private String avatar;

        @Schema(description = "昵称", example = "游戏达人")
        private String nickname;

        @Schema(description = "年龄", example = "19")
        private Integer age;

        @Schema(description = "性别: male, female", example = "male")
        private String gender;

        @Schema(description = "个性签名", example = "专业陪玩，上分快")
        private String signature;

        @Schema(description = "是否实名认证", example = "true")
        private Boolean isVerified;

        @Schema(description = "关系状态: none(无关系), following(关注中), followed(被关注), mutual(互相关注)", example = "none")
        private String relationStatus;
    }
}
