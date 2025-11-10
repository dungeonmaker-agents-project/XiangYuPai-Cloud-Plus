package com.xypai.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 自动注册DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自动注册请求")
public class AutoRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138001")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 注册来源
     */
    @Schema(description = "注册来源", example = "sms_login")
    @Builder.Default
    private String source = "sms_login";

    /**
     * 客户端类型
     */
    @Schema(description = "客户端类型", example = "web")
    @Builder.Default
    private String clientType = "web";

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "device_12345")
    private String deviceId;
}
