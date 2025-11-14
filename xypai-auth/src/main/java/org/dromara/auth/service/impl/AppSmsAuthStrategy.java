package org.dromara.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.auth.domain.dto.AppSmsLoginDto;
import org.dromara.auth.domain.vo.AppLoginVo;
import org.dromara.auth.service.IAuthStrategy;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.domain.vo.RemoteClientVo;
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
@Service("app_sms" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class AppSmsAuthStrategy implements IAuthStrategy {

    @DubboReference
    private final RemoteAppUserService remoteAppUserService;

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
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        loginUser.setIpaddr(clientIp);
        loginUser.setLoginTime(System.currentTimeMillis());

        // 7. 生成 Sa-Token
        SaLoginParameter saLoginParameter = new SaLoginParameter();
        saLoginParameter.setDeviceType(client.getDeviceType());
        saLoginParameter.setTimeout(client.getTimeout());  // Token 过期时间（从client配置读取）
        saLoginParameter.setActiveTimeout(client.getActiveTimeout());  // 活跃过期时间
        saLoginParameter.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());

        LoginHelper.login(loginUser, saLoginParameter);

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
     * @param mobile 手机号
     * @param code   用户输入的验证码
     * @return true=验证码正确，false=验证码错误
     */
    private boolean validateSmsCode(String mobile, String code) {
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
            return true;
        }

        return false;
    }
}
