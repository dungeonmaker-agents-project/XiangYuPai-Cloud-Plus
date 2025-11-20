package org.dromara.xypai.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改支付密码DTO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
public class UpdatePaymentPasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原支付密码（6位数字）
     */
    @NotBlank(message = "原支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "原支付密码必须为6位数字")
    private String oldPaymentPassword;

    /**
     * 新支付密码（6位数字）
     */
    @NotBlank(message = "新支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "新支付密码必须为6位数字")
    private String newPaymentPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "确认新密码必须为6位数字")
    private String confirmPassword;
}
