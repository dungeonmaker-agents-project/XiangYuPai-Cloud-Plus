package org.dromara.xypai.auth.service;

import org.dromara.xypai.auth.domain.vo.AppLoginVo;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.system.api.domain.vo.RemoteClientVo;

/**
 * App授权策略接口
 *
 * <p>专门用于App登录，返回AppLoginVo格式</p>
 *
 * @author XyPai Team
 * @date 2025-11-16
 */
public interface IAppAuthStrategy {

    String BASE_NAME = "AuthStrategy";

    /**
     * 登录
     *
     * @param body      登录对象
     * @param client    授权管理视图对象
     * @param grantType 授权类型
     * @return App登录验证信息
     */
    static AppLoginVo login(String body, RemoteClientVo client, String grantType) {
        // 授权类型和客户端id
        String beanName = grantType + BASE_NAME;
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("授权类型不正确!");
        }
        IAppAuthStrategy instance = SpringUtils.getBean(beanName);
        return instance.login(body, client);
    }

    /**
     * 登录
     *
     * @param body   登录对象
     * @param client 授权管理视图对象
     * @return App登录验证信息
     */
    AppLoginVo login(String body, RemoteClientVo client);

}
