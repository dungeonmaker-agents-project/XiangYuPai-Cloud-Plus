package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 个人资料详细信息VO
 * Profile Info VO (includes skills)
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile info response")
public class ProfileInfoVo {

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

    @Schema(description = "常居地编码")
    private String locationCode;

    @Schema(description = "职业列表（多选，最多5个）")
    private List<String> occupations;

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

    @Schema(description = "Skills list")
    private List<SkillVo> skills;
}
