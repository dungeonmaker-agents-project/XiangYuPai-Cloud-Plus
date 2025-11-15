package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 更新用户资料DTO
 * Update User Profile DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Schema(description = "Update user profile request")
public class UserUpdateDto {

    @Schema(description = "Nickname (2-20 characters)")
    @Size(min = 2, max = 20, message = "昵称长度为2-20字符")
    private String nickname;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Gender: male, female, other")
    private String gender;

    @Schema(description = "Birthday")
    private LocalDate birthday;

    @Schema(description = "Residence")
    @Size(max = 200, message = "居住地不能超过200字符")
    private String residence;

    @Schema(description = "Height (cm, 100-250)")
    @Min(value = 100, message = "身高范围为100-250cm")
    @Max(value = 250, message = "身高范围为100-250cm")
    private Integer height;

    @Schema(description = "Weight (kg, 30-200)")
    @Min(value = 30, message = "体重范围为30-200kg")
    @Max(value = 200, message = "体重范围为30-200kg")
    private Integer weight;

    @Schema(description = "Occupation")
    @Size(max = 100, message = "职业不能超过100字符")
    private String occupation;

    @Schema(description = "WeChat ID (6-20 characters)")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,20}$", message = "微信号格式不正确")
    private String wechat;

    @Schema(description = "Bio (0-200 characters)")
    @Size(max = 200, message = "个性签名不能超过200字符")
    private String bio;
}
