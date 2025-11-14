package org.dromara.auth.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.MessageUtils;
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
@RequestMapping("/sms")
public class SmsController {

    @DubboReference
    private final RemoteSmsService remoteSmsService;

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
        String mobile = request.getMobile();
        String type = request.getType(); // login, register, reset
        
        log.info("发送短信验证码: mobile={}, type={}", mobile, type);
        
        // 1. 校验手机号格式
        if (!ValidatorUtils.isMobile(mobile)) {
            return R.fail("手机号格式不正确");
        }
        
        // 2. 检查发送频率（防止频繁发送）
        String intervalKey = SMS_CODE_KEY + mobile + ":interval";
        if (RedisUtils.hasKey(intervalKey)) {
            Long ttl = RedisUtils.getTimeToLive(intervalKey);
            return R.fail("验证码发送过于频繁，请" + ttl + "秒后再试");
        }
        
        // 3. 检查每日发送次数（防止恶意刷验证码）
        String countKey = SMS_SEND_COUNT_KEY + mobile;
        Integer sendCount = RedisUtils.getCacheObject(countKey);
        if (sendCount != null && sendCount >= MAX_DAILY_SENDS) {
            return R.fail("今日发送次数已达上限，请明天再试");
        }
        
        // 4. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        String codeId = IdUtil.simpleUUID();
        
        // 5. 保存验证码到Redis（5分钟有效期）
        String verifyKey = SMS_CODE_KEY + mobile + ":" + type + ":" + codeId;
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
            String templateCode = getTemplateCode(type);
            
            // 🔥 调用短信服务发送
            // ⚠️ 注意：需要在 application.yml 中配置短信服务商（阿里云/腾讯云等）
            RemoteSms smsResponse = remoteSmsService.sendMessage(mobile, templateCode, params);
            if (smsResponse == null || Boolean.FALSE.equals(smsResponse.getSuccess())) {
                String errorMsg = smsResponse != null ? smsResponse.getResponse() : "短信服务未返回结果";
                throw new IllegalStateException(errorMsg);
            }
            
            log.info("短信发送成功: mobile={}, code={}, codeId={}", mobile, code, codeId);
            
        } catch (Exception e) {
            log.error("短信发送失败: mobile={}, error={}", mobile, e.getMessage(), e);
            // 发送失败时清除验证码
            RedisUtils.deleteObject(verifyKey);
            return R.fail("短信发送失败，请稍后重试");
        }
        
        // 9. 返回结果（不返回验证码内容！）
        Map<String, Object> result = new HashMap<>();
        result.put("codeId", codeId);
        result.put("expiresIn", CODE_EXPIRATION * 60);  // 秒
        result.put("nextSendTime", SEND_INTERVAL);      // 秒
        result.put("mobile", mobile);
        
        // 🔧 开发环境下返回验证码（方便测试）
        if (log.isDebugEnabled()) {
            result.put("code", code);  // ⚠️ 生产环境必须删除！
            log.debug("【开发环境】验证码: {}", code);
        }
        
        return R.ok(result);
    }
    
    /**
     * 验证短信验证码
     * 
     * @param request 请求体
     * @return 结果
     */
    @PostMapping("/verify")
    public R<Void> verifyCode(@RequestBody @Validated VerifySmsRequest request) {
        String mobile = request.getMobile();
        String code = request.getCode();
        String type = request.getType();
        String codeId = request.getCodeId();
        
        log.info("验证短信验证码: mobile={}, type={}, codeId={}", mobile, type, codeId);
        
        // 1. 从Redis获取验证码
        String verifyKey = SMS_CODE_KEY + mobile + ":" + type + ":" + codeId;
        String cachedCode = RedisUtils.getCacheObject(verifyKey);
        
        // 2. 验证码不存在或已过期
        if (StringUtils.isEmpty(cachedCode)) {
            return R.fail("验证码已过期，请重新获取");
        }
        
        // 3. 验证码错误
        if (!StringUtils.equals(code, cachedCode)) {
            return R.fail("验证码错误");
        }
        
        // 4. 验证成功，删除验证码（一次性使用）
        RedisUtils.deleteObject(verifyKey);
        
        log.info("验证码验证成功: mobile={}, type={}", mobile, type);
        return R.ok();
    }
    
    /**
     * 根据类型获取短信模板代码
     * 
     * @param type 类型
     * @return 模板代码
     */
    private String getTemplateCode(String type) {
        return switch (type) {
            case "login" -> "SMS_LOGIN_TEMPLATE";       // 登录验证码模板
            case "register" -> "SMS_REGISTER_TEMPLATE"; // 注册验证码模板
            case "reset" -> "SMS_RESET_TEMPLATE";       // 重置密码模板
            default -> "SMS_DEFAULT_TEMPLATE";          // 默认模板
        };
    }
    
    // #region 请求类定义
    
    /**
     * 发送短信请求
     */
    public static class SendSmsRequest {
        
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String mobile;
        
        @NotBlank(message = "验证码类型不能为空")
        @Pattern(regexp = "^(login|register|reset)$", message = "验证码类型不正确")
        private String type;
        
        private String region = "+86";  // 区号（可选，默认+86）
        
        public String getMobile() {
            return mobile;
        }
        
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
    }
    
    /**
     * 验证短信请求
     */
    public static class VerifySmsRequest {
        
        @NotBlank(message = "手机号不能为空")
        private String mobile;
        
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
        private String code;
        
        @NotBlank(message = "验证码类型不能为空")
        private String type;
        
        @NotBlank(message = "验证码ID不能为空")
        private String codeId;
        
        public String getMobile() {
            return mobile;
        }
        
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getCodeId() {
            return codeId;
        }
        
        public void setCodeId(String codeId) {
            this.codeId = codeId;
        }
    }
    
    // #endregion
}

