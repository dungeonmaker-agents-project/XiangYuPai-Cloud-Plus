package org.dromara.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.auth.domain.dto.AppPasswordLoginDto;
import org.dromara.auth.domain.vo.AppLoginVo;
import org.dromara.auth.service.IAuthStrategy;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.springframework.stereotype.Service;

/**
 * App密码登录策略（无租户）
 *
 * <p>特点：</p>
 * <ul>
 *     <li>✅ 手机号 + 密码登录</li>
 *     <li>✅ 无需租户参数</li>
 *     <li>✅ 密码校验（BCrypt）</li>
 *     <li>✅ 更新登录信息</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>⚠️ 用户首次SMS登录时无密码，需在个人中心设置密码后才能使用密码登录</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Slf4j
@Service("app_password" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class AppPasswordAuthStrategy implements IAuthStrategy {

    @DubboReference
    private final RemoteAppUserService remoteAppUserService;

    @Override
    public AppLoginVo login(String body, RemoteClientVo client) {
        // 1. 解析请求体
        AppPasswordLoginDto loginDto = JsonUtils.parseObject(body, AppPasswordLoginDto.class);
        ValidatorUtils.validate(loginDto);

        String mobile = loginDto.getMobile();
        String countryCode = loginDto.getCountryCode();
        String password = loginDto.getPassword();

        log.info("App密码登录：mobile={}, countryCode={}", mobile, countryCode);

        // 2. 查询用户
        AppLoginUser loginUser;
        try {
            loginUser = remoteAppUserService.getUserByMobile(mobile, countryCode);
        } catch (UserException e) {
            log.warn("用户不存在：mobile={}", mobile);
            throw new UserException("手机号或密码错误");
        }

        // 3. 校验密码
        boolean passwordCorrect = remoteAppUserService.checkPassword(loginUser.getUserId(), password);
        if (!passwordCorrect) {
            log.warn("密码错误：userId={}", loginUser.getUserId());
            throw new UserException("手机号或密码错误");
        }

        // 4. 检查账号状态
        if (!loginUser.isAccountNonLocked()) {
            log.warn("账号已被禁用：userId={}, status={}", loginUser.getUserId(), loginUser.getStatus());
            throw new UserException("账号已被禁用，请联系客服");
        }

        // 5. 更新登录信息
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
        saLoginParameter.setTimeout(client.getTimeout());
        saLoginParameter.setActiveTimeout(client.getActiveTimeout());
        saLoginParameter.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());

        LoginHelper.login(loginUser, saLoginParameter);

        log.info("App密码登录成功：userId={}, token={}", loginUser.getUserId(), StpUtil.getTokenValue());

        // 8. 构建响应
        return AppLoginVo.builder()
            .accessToken(StpUtil.getTokenValue())
            .expireIn(StpUtil.getTokenTimeout())
            .userId(String.valueOf(loginUser.getUserId()))
            .nickname(loginUser.getNickname())
            .avatar(loginUser.getAvatar())
            .isNewUser(loginUser.getIsNewUser())
            .build();
    }
}
