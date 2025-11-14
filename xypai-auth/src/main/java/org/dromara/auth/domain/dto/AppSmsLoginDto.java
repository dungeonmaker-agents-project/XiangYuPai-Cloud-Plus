package org.dromara.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * App SMS登录请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Data
public class AppSmsLoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 国家区号（例如："+86"）
     */
    @NotBlank(message = "国家区号不能为空")
    private String countryCode;

    /**
     * 手机号（例如："13800138000"）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 6位验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String verificationCode;

    /**
     * 是否同意用户协议（必须为true）
     */
    @NotNull(message = "必须同意用户协议")
    private Boolean agreeToTerms;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 授权类型
     */
    private String grantType;
}
