package org.dromara.xypai.auth.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.constant.GlobalConstants;
import org.springframework.beans.factory.annotation.Value;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.resource.api.RemoteSmsService;
import org.dromara.resource.api.domain.RemoteSms;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SMS验证码控制器
 *
 * 功能：
 * - 发送短信验证码（登录/注册/重置密码）
 * - 验证短信验证码
 * - 防刷机制（频率限制、IP限制）
 *
 * @author XiangYuPai Team
 * @date 2025-11-11
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/sms")
public class SmsController {

    @DubboReference
    private final RemoteSmsService remoteSmsService;

    /**
     * 是否开发环境（用于返回验证码到前端）
     */
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * Redis Key前缀
     */
    private static final String SMS_CODE_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "sms_code:";
    private static final String SMS_SEND_COUNT_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "sms_send_count:";

    /**
     * 验证码配置
     */
    private static final int CODE_LENGTH = 6;           // 验证码长度
    private static final int CODE_EXPIRATION = 5;       // 过期时间（分钟）
    private static final int SEND_INTERVAL = 60;        // 发送间隔（秒）
    private static final int MAX_DAILY_SENDS = 10;      // 每日最大发送次数

    /**
     * 发送短信验证码
     *
     * @param request 请求体
     * @return 结果
     */
    @PostMapping("/send")
    @RateLimiter(time = 60, count = 5, limitType = LimitType.IP)
    public R<Map<String, Object>> sendCode(@RequestBody @Validated SendSmsRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String purpose = request.getPurpose(); // LOGIN, RESET_PASSWORD

        log.info("发送短信验证码: phoneNumber={}, purpose={}", phoneNumber, purpose);

        // 1. 校验手机号格式
        if (!ValidatorUtils.isMobile(phoneNumber)) {
            return R.fail("手机号格式不正确");
        }

        // 2. 检查发送频率（防止频繁发送）
        String intervalKey = SMS_CODE_KEY + phoneNumber + ":interval";
        if (RedisUtils.hasKey(intervalKey)) {
            Long ttl = RedisUtils.getTimeToLive(intervalKey);
            return R.fail("验证码发送过于频繁，请" + ttl + "秒后再试");
        }

        // 3. 检查每日发送次数（防止恶意刷验证码）
        String countKey = SMS_SEND_COUNT_KEY + phoneNumber;
        Integer sendCount = RedisUtils.getCacheObject(countKey);
        if (sendCount != null && sendCount >= MAX_DAILY_SENDS) {
            return R.fail("今日发送次数已达上限，请明天再试");
        }

        // 4. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        String codeId = IdUtil.simpleUUID();

        // 5. 保存验证码到Redis（5分钟有效期）
        String verifyKey = SMS_CODE_KEY + phoneNumber + ":" + purpose + ":" + codeId;
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(CODE_EXPIRATION));

        // 6. 设置发送间隔锁（60秒内不能重复发送）
        RedisUtils.setCacheObject(intervalKey, "1", Duration.ofSeconds(SEND_INTERVAL));

        // 7. 累计发送次数（24小时有效期）
        if (sendCount == null) {
            RedisUtils.setCacheObject(countKey, 1, Duration.ofDays(1));
        } else {
            RedisUtils.setCacheObject(countKey, sendCount + 1, Duration.ofDays(1));
        }

        // 8. 发送短信（调用短信服务）
        try {
            // 🔥 准备短信模板参数
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("code", code);

            // 🔥 根据类型选择模板
            String templateCode = getTemplateCode(purpose);

            // 🔥 调用短信服务发送
            // ⚠️ 注意：需要在 application.yml 中配置短信服务商（阿里云/腾讯云等）
            RemoteSms smsResponse = remoteSmsService.sendMessage(phoneNumber, templateCode, params);
            if (smsResponse == null || Boolean.FALSE.equals(smsResponse.getSuccess())) {
                String errorMsg = smsResponse != null ? smsResponse.getResponse() : "短信服务未返回结果";
                throw new IllegalStateException(errorMsg);
            }

            log.info("短信发送成功: phoneNumber={}, code={}, codeId={}", phoneNumber, code, codeId);

        } catch (Exception e) {
            log.error("短信发送失败: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            // 发送失败时清除验证码
            RedisUtils.deleteObject(verifyKey);
            return R.fail("短信发送失败，请稍后重试");
        }

        // 9. 返回结果（不返回验证码内容！）
        Map<String, Object> result = new HashMap<>();
        result.put("codeId", codeId);
        result.put("expiresIn", CODE_EXPIRATION * 60);  // 秒
        result.put("nextSendTime", SEND_INTERVAL);      // 秒
        result.put("phoneNumber", phoneNumber);

        // 🔧 开发环境下返回验证码（方便测试）
        // ⚠️ 仅在 dev 或 test 环境下返回验证码，生产环境不返回！
        boolean isDevelopment = "dev".equals(activeProfile) || "test".equals(activeProfile);
        if (isDevelopment) {
            result.put("code", code);  // 返回验证码给前端
            log.warn("【{}环境】验证码已返回到响应: phoneNumber={}, code={}", activeProfile, phoneNumber, code);
        } else {
            log.info("【{}环境】验证码已发送，不返回到响应", activeProfile);
        }

        return R.ok(result);
    }


    /**
     * 根据类型获取短信模板代码
     *
     * @param purpose 用途
     * @return 模板代码
     */
    private String getTemplateCode(String purpose) {
        return switch (purpose) {
            case "LOGIN" -> "SMS_LOGIN_TEMPLATE";              // 登录验证码模板
            case "RESET_PASSWORD" -> "SMS_RESET_TEMPLATE";     // 重置密码模板
            default -> "SMS_DEFAULT_TEMPLATE";                 // 默认模板
        };
    }

    // #region 请求类定义

    /**
     * 发送短信请求
     */
    public static class SendSmsRequest {

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phoneNumber;

        @NotBlank(message = "验证码类型不能为空")
        @Pattern(regexp = "^(LOGIN|RESET_PASSWORD)$", message = "验证码类型不正确")
        private String purpose;

        private String countryCode = "+86";  // 区号（可选，默认+86）

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }
    }

    // #endregion
}

