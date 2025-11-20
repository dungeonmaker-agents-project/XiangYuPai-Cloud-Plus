package org.dromara.xypai.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.xypai.auth.domain.dto.AppSmsLoginDto;
import org.dromara.xypai.auth.domain.vo.AppLoginVo;
import org.dromara.xypai.auth.service.IAppAuthStrategy;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * App短信验证码登录策略（自动注册 + 无租户）
 *
 * <p>特点：</p>
 * <ul>
 *     <li>✅ 支持自动注册（未注册手机号验证后自动创建账号）</li>
 *     <li>✅ 无需租户参数（app用户无多租户）</li>
 *     <li>✅ 返回 isNewUser 标记（前端判断是否需要完善资料）</li>
 *     <li>✅ 更新登录信息（登录时间、IP、登录次数）</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Slf4j
@Service("app_sms" + IAppAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class AppSmsAuthStrategy implements IAppAuthStrategy {

    @DubboReference
    private final RemoteAppUserService remoteAppUserService;

    /**
     * 是否启用短信服务
     * false = 测试模式，接受任意6位数字验证码
     * true = 生产模式，严格验证Redis中的真实验证码
     */
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Override
    public AppLoginVo login(String body, RemoteClientVo client) {
        // 1. 解析请求体
        AppSmsLoginDto loginDto = JsonUtils.parseObject(body, AppSmsLoginDto.class);
        ValidatorUtils.validate(loginDto);

        String mobile = loginDto.getMobile();
        String countryCode = loginDto.getCountryCode();
        String verificationCode = loginDto.getVerificationCode();

        log.info("App SMS登录：mobile={}, countryCode={}", mobile, countryCode);

        // 2. 校验短信验证码
        if (!validateSmsCode(mobile, verificationCode)) {
            log.warn("验证码错误：mobile={}, code={}", mobile, verificationCode);
            throw new CaptchaException("验证码错误，请重新输入");
        }

        // 3. 注册或获取用户（关键：自动注册）
        AppLoginUser loginUser;
        try {
            loginUser = remoteAppUserService.registerOrGetByMobile(mobile, countryCode);
            log.info("用户信息获取成功：userId={}, isNewUser={}", loginUser.getUserId(), loginUser.getIsNewUser());
        } catch (Exception e) {
            log.error("用户注册或登录失败：mobile={}, error={}", mobile, e.getMessage(), e);
            throw new UserException("登录失败：" + e.getMessage());
        }

        // 4. 检查账号状态
        if (!loginUser.isAccountNonLocked()) {
            log.warn("账号已被禁用：userId={}, status={}", loginUser.getUserId(), loginUser.getStatus());
            throw new UserException("账号已被禁用，请联系客服");
        }

        // 5. 更新登录信息（登录时间、IP、登录次数）
        String clientIp = ServletUtils.getClientIP();
        remoteAppUserService.updateLastLoginInfo(loginUser.getUserId(), clientIp);

        // 6. 设置登录会话信息
        if (client != null) {
            loginUser.setClientKey(client.getClientKey());
            loginUser.setDeviceType(client.getDeviceType());
        } else {
            // App登录默认值（无客户端认证）
            loginUser.setClientKey("app-client");
            loginUser.setDeviceType("app");
        }
        loginUser.setIpaddr(clientIp);
        loginUser.setLoginTime(System.currentTimeMillis());

        // 7. 生成 Sa-Token
        StpUtil.login(loginUser.getUserId());
        StpUtil.getTokenSession().set(LoginHelper.USER_KEY, loginUser);

        log.info("App SMS登录成功：userId={}, isNewUser={}, token={}",
            loginUser.getUserId(), loginUser.getIsNewUser(), StpUtil.getTokenValue());

        // 8. 构建响应
        return AppLoginVo.builder()
            .accessToken(StpUtil.getTokenValue())
            .expireIn(StpUtil.getTokenTimeout())
            .userId(String.valueOf(loginUser.getUserId()))
            .nickname(loginUser.getNickname())
            .avatar(loginUser.getAvatar())
            .isNewUser(loginUser.getIsNewUser())  // ⭐ 关键字段：前端根据此字段决定跳转
            .build();
    }

    /**
     * 校验短信验证码
     *
     * <p>智能验证模式：</p>
     * <ul>
     *     <li>✅ 生产模式（sms.enabled=true）：严格验证 Redis 中的验证码</li>
     *     <li>✅ 测试模式（sms.enabled=false）：接受任意6位数字验证码</li>
     * </ul>
     *
     * @param mobile 手机号
     * @param code   用户输入的验证码
     * @return true=验证码正确，false=验证码错误
     */
    private boolean validateSmsCode(String mobile, String code) {
        // 🔧 测试模式：接受任意6位数字验证码
        if (!smsEnabled) {
            log.warn("⚠️  短信服务未启用（测试模式），接受任意6位数字验证码：mobile={}, code={}", mobile, code);
            log.info("💡 提示：当前为测试模式，生产环境请设置 sms.enabled=true");

            // 验证格式：必须是6位数字
            if (StringUtils.isBlank(code)) {
                log.warn("❌ 验证码为空");
                throw new CaptchaException("验证码不能为空");
            }

            if (!code.matches("^\\d{6}$")) {
                log.warn("❌ 验证码格式错误，必须是6位数字：code={}", code);
                throw new CaptchaException("验证码格式不正确，必须是6位数字");
            }

            log.info("✅ 测试模式验证通过：接受任意6位数字 {}", code);
            return true;
        }

        // 🔐 生产模式：严格验证
        log.info("✅ 短信服务已启用（生产模式），严格验证验证码：mobile={}", mobile);

        // 从 Redis 获取验证码
        String cacheKey = GlobalConstants.CAPTCHA_CODE_KEY + mobile;
        String cachedCode = RedisUtils.getCacheObject(cacheKey);

        if (StringUtils.isBlank(cachedCode)) {
            log.warn("验证码已过期或不存在：mobile={}", mobile);
            throw new CaptchaExpireException();
        }

        // 验证成功后删除验证码（一次性使用）
        if (StringUtils.equals(code, cachedCode)) {
            RedisUtils.deleteObject(cacheKey);
            log.info("✅ 验证码验证成功：mobile={}", mobile);
            return true;
        }

        log.warn("❌ 验证码错误：mobile={}, expected={}, actual={}", mobile, cachedCode, code);
        return false;
    }
}
