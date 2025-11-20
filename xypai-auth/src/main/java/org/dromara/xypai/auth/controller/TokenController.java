package org.dromara.xypai.auth.controller;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.xypai.auth.domain.vo.LoginVo;
import org.dromara.xypai.auth.service.IAuthStrategy;
import org.dromara.xypai.auth.service.SysLoginService;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.LoginBody;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.encrypt.annotation.ApiEncrypt;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.resource.api.RemoteMessageService;
import org.dromara.system.api.RemoteClientService;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Token 控制器（系统API）
 *
 * <p>功能：</p>
 * <ul>
 *     <li>系统登录接口（保留用于系统调用）</li>
 * </ul>
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TokenController {

    private final SysLoginService sysLoginService;
    private final ScheduledExecutorService scheduledExecutorService;

    @DubboReference
    private final RemoteClientService remoteClientService;
    @DubboReference(stub = "true")
    private final RemoteMessageService remoteMessageService;

    /**
     * 登录方法
     *
     * @param body 登录信息
     * @return 结果
     */
    @ApiEncrypt
    @PostMapping("/login")
    public R<LoginVo> login(@RequestBody String body) {
        LoginBody loginBody = JsonUtils.parseObject(body, LoginBody.class);
        ValidatorUtils.validate(loginBody);
        // 授权类型和客户端id
        String clientId = loginBody.getClientId();
        String grantType = loginBody.getGrantType();
        RemoteClientVo clientVo = remoteClientService.queryByClientId(clientId);

        // 查询不到 client 或 client 内不包含 grantType
        if (ObjectUtil.isNull(clientVo) || !StringUtils.contains(clientVo.getGrantType(), grantType)) {
            log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
            return R.fail(MessageUtils.message("auth.grant.type.error"));
        } else if (!SystemConstants.NORMAL.equals(clientVo.getStatus())) {
            return R.fail(MessageUtils.message("auth.grant.type.blocked"));
        }
        // 校验租户
        sysLoginService.checkTenant(loginBody.getTenantId());
        // 登录
        LoginVo loginVo = IAuthStrategy.login(body, clientVo, grantType);

        Long userId = LoginHelper.getUserId();
        scheduledExecutorService.schedule(() -> {
            remoteMessageService.publishMessage(List.of(userId), "欢迎登录RuoYi-Cloud-Plus微服务管理系统");
        }, 5, TimeUnit.SECONDS);
        return R.ok(loginVo);
    }





}
