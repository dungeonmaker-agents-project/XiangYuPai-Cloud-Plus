package org.dromara.xypai.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
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

    // ========== 以下字段由后端自动处理，前端无需传递 ==========

    /**
     * 客户端ID（后端从User-Agent自动判断）
     */
    private String clientId;

    /**
     * 授权类型（后端自动设置为"app_sms"）
     */
    private String grantType;
}
