package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户关系VO (粉丝/关注列表)
 * User Relation VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User relation (follower/following)")
public class UserRelationVo {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Gender: male, female, other")
    private String gender;

    @Schema(description = "Bio")
    private String bio;

    @Schema(description = "Is online")
    private Boolean isOnline;

    @Schema(description = "Follow status: none, following, mutual")
    private String followStatus;

    @Schema(description = "Fans count")
    private Integer fansCount;
}
