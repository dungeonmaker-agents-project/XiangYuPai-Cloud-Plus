package org.dromara.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重置密码请求DTO（忘记密码流程 - 步骤3）
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Data
public class ResetPasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 国家区号
     */
    @NotBlank(message = "国家区号不能为空")
    private String countryCode;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 6位验证码（已验证过的）
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String verificationCode;

    /**
     * 新密码（6-20位，不可纯数字）
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;
}
