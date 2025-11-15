package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户资料VO
 * User Profile VO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response")
public class UserProfileVo {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Gender: male, female, other")
    private String gender;

    @Schema(description = "Birthday")
    private LocalDate birthday;

    @Schema(description = "Residence")
    private String residence;

    @Schema(description = "Height (cm)")
    private Integer height;

    @Schema(description = "Weight (kg)")
    private Integer weight;

    @Schema(description = "Occupation")
    private String occupation;

    @Schema(description = "WeChat ID")
    private String wechat;

    @Schema(description = "Bio")
    private String bio;

    @Schema(description = "Is online")
    private Boolean isOnline;

    @Schema(description = "User statistics")
    private UserStatsVo stats;

    @Schema(description = "Follow status: none, following, mutual")
    private String followStatus;

    @Schema(description = "Privacy settings")
    private PrivacyVo privacy;
}
