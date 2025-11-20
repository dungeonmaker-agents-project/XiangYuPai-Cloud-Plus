package org.dromara.xypai.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 认证工具控制器
 *
 * <p>功能：</p>
 * <ul>
 *     <li>检查手机号是否已注册</li>
 *     <li>其他认证相关工具接口</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "认证工具", description = "认证相关工具接口")
public class AuthUtilController {

    @DubboReference
    private final RemoteAppUserService remoteAppUserService;

    /**
     * 检查手机号是否已注册
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/check/phone
     * {
     *   "countryCode": "+86",
     *   "phoneNumber": "13800138000"
     * }
     * </pre>
     *
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "查询成功",
     *   "data": {
     *     "isRegistered": true
     *   }
     * }
     * </pre>
     *
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/check/phone")
    @Operation(summary = "检查手机号是否已注册", description = "验证手机号是否已在系统中注册")
    public R<CheckPhoneResult> checkPhone(@RequestBody @Valid CheckPhoneRequest request) {
        String mobile = request.getPhoneNumber();
        String countryCode = request.getCountryCode();

        log.info("检查手机号是否已注册：mobile={}, countryCode={}", mobile, countryCode);

        // 调用远程服务检查手机号
        boolean isRegistered = remoteAppUserService.existsByMobile(mobile, countryCode);

        log.info("手机号注册状态：mobile={}, isRegistered={}", mobile, isRegistered);
        return R.ok(new CheckPhoneResult(isRegistered));
    }

    /**
     * 检查手机号请求
     */
    public static class CheckPhoneRequest implements Serializable {

        @NotBlank(message = "国家区号不能为空")
        private String countryCode;

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phoneNumber;

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    /**
     * 检查手机号结果
     */
    public record CheckPhoneResult(Boolean isRegistered) {
    }
}
