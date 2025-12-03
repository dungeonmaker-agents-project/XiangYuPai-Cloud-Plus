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
 * 对应新UI文档 UserItem 数据结构:
 * - avatarData.avatarUrl -> avatar
 * - basicInfo.nickname -> nickname
 * - basicInfo.gender -> gender
 * - basicInfo.age -> age
 * - basicInfo.isVerified -> isVerified
 * - basicInfo.signature -> signature (bio)
 * - relationData.relationStatus -> relationStatus
 *
 * relationStatus 状态说明:
 * - none: 未关注
 * - following: 已关注对方
 * - followed: 对方关注我（我未关注对方）
 * - mutual: 互相关注
 *
 * @author XiangYuPai
 * @since 2025-11-14
 * @updated 2025-12-02 - 按新UI文档完善字段
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

    @Schema(description = "Age (calculated from birthday)")
    private Integer age;

    @Schema(description = "Is verified (实名认证)")
    private Boolean isVerified;

    @Schema(description = "Signature / Bio (个性签名)")
    private String signature;

    @Schema(description = "Bio (alias for signature)")
    private String bio;

    @Schema(description = "Is online")
    private Boolean isOnline;

    @Schema(description = "Relation status: none, following, followed, mutual")
    private String relationStatus;

    @Schema(description = "Follow status (alias for relationStatus)")
    private String followStatus;

    @Schema(description = "Fans count")
    private Integer fansCount;

    @Schema(description = "Is following")
    private Boolean isFollowing;

    @Schema(description = "Is mutual follow")
    private Boolean isMutualFollow;
}
