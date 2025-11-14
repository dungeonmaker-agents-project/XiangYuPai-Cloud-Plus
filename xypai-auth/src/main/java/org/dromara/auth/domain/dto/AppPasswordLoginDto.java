package org.dromara.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * App密码登录请求DTO
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Data
public class AppPasswordLoginDto implements Serializable {

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
     * 密码（6-20位，不可纯数字）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    /**
     * 是否同意用户协议
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
