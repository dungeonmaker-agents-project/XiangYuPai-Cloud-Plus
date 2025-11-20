package org.dromara.xypai.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 验证支付密码DTO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
public class VerifyPaymentPasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支付密码（6位数字）
     */
    @NotBlank(message = "支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    private String paymentPassword;
}
