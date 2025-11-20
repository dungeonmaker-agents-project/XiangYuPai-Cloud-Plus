package org.dromara.gateway.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.util.SaTokenConsts;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.gateway.config.properties.IgnoreWhiteProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * [Sa-Token 权限认证] 拦截器
 *
 * @author Lion Li
 */
@Configuration
public class AuthFilter {

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter getSaReactorFilter(IgnoreWhiteProperties ignoreWhite) {
        // 🔍 调试：打印白名单配置
        System.out.println("==================== Gateway 白名单配置 ====================");
        System.out.println("白名单数量: " + ignoreWhite.getWhites().size());
        ignoreWhite.getWhites().forEach(white -> System.out.println("  ✅ " + white));
        System.out.println("===========================================================");
        
        return new SaReactorFilter()
            // 拦截地址
            .addInclude("/**")
            .addExclude("/favicon.ico", "/actuator", "/actuator/**", "/resource/sse")
            // 鉴权方法：每次访问进入
            .setAuth(obj -> {
                // 🔍 调试：打印请求路径
                ServerHttpRequest request = SaReactorSyncHolder.getExchange().getRequest();
                String path = request.getURI().getPath();
                System.out.println("🔍 Gateway收到请求: " + request.getMethod() + " " + path);
                
                // 登录校验 -- 拦截所有路由
                SaRouter.match("/**")
                    .notMatch(ignoreWhite.getWhites())
                    .check(r -> {
                        System.out.println("  ❌ 需要认证: " + path);
                        System.out.println("\n🔐 [AUTH-FILTER] ========== 开始认证流程 ==========");

                        // 1. 获取请求中的 Token
                        String tokenValue = StpUtil.getTokenValue();
                        String authHeader = request.getHeaders().getFirst("Authorization");
                        System.out.println("  📋 步骤1: 获取 Token");
                        System.out.println("     Authorization Header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "NULL"));
                        System.out.println("     Sa-Token 解析到的 Token: " + (tokenValue != null ? tokenValue.substring(0, Math.min(50, tokenValue.length())) + "..." : "NULL"));

                        // 2. 检查是否登录 是否有token
                        System.out.println("  📋 步骤2: 检查登录状态");
                        try {
                            StpUtil.checkLogin();
                            System.out.println("     ✅ Token 验证通过！用户已登录");

                            // ⭐ 关键修复：提取userId并存储到exchange attributes，供ForwardAuthFilter使用
                            Object loginIdObj = StpUtil.getLoginId();
                            String userId = loginIdObj != null ? loginIdObj.toString() : null;
                            System.out.println("     用户ID: " + userId);
                            System.out.println("     Token 剩余有效时间: " + StpUtil.getTokenTimeout() + "秒");

                            // 将userId存储到exchange attributes中
                            if (userId != null) {
                                SaReactorSyncHolder.getExchange().getAttributes().put("X-User-Id", userId);
                                System.out.println("     💾 已将 userId 存储到 exchange attributes");
                            }
                        } catch (NotLoginException e) {
                            System.out.println("     ❌ Token 验证失败: " + e.getMessage());
                            System.out.println("     失败原因: " + e.getType());
                            throw e;
                        }

                        // 3. 检查 header 与 param 里的 clientid 与 token 里的是否一致
                        System.out.println("  📋 步骤3: 验证 ClientId");
                        String headerCid = request.getHeaders().getFirst(LoginHelper.CLIENT_KEY);
                        String paramCid = request.getQueryParams().getFirst(LoginHelper.CLIENT_KEY);
                        Object clientIdObj = StpUtil.getExtra(LoginHelper.CLIENT_KEY);
                        String clientId = clientIdObj != null ? clientIdObj.toString() : null;

                        System.out.println("     Header ClientId: " + headerCid);
                        System.out.println("     Param ClientId: " + paramCid);
                        System.out.println("     Token中的 ClientId: " + clientId);

                        if (!StringUtils.equalsAny(clientId, headerCid, paramCid)) {
                            System.out.println("     ❌ ClientId 不匹配！");
                            // token 无效
                            throw NotLoginException.newInstance(StpUtil.getLoginType(),
                                "-100", "客户端ID与Token不匹配",
                                StpUtil.getTokenValue());
                        }
                        System.out.println("     ✅ ClientId 验证通过");

                        // 将 clientKey 存储到 exchange attributes 中，供 ForwardAuthFilter 使用
                        if (clientId != null) {
                            SaReactorSyncHolder.getExchange().getAttributes().put("X-Client-Key", clientId);
                            System.out.println("     💾 已将 clientKey 存储到 exchange attributes");
                        }

                        System.out.println("  ✅ [AUTH-FILTER] ========== 认证成功 ==========\n");
                    });
            }).setError(e -> {
                System.out.println("\n❌ [AUTH-FILTER] ========== 认证失败 ==========");
                System.out.println("  异常类型: " + e.getClass().getName());
                System.out.println("  异常消息: " + e.getMessage());

                ServerHttpResponse response = SaReactorSyncHolder.getExchange().getResponse();
                response.getHeaders().set(SaTokenConsts.CONTENT_TYPE_KEY, SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);

                if (e instanceof NotLoginException) {
                    NotLoginException nle = (NotLoginException) e;
                    System.out.println("  未登录原因: " + nle.getType());
                    System.out.println("  返回错误码: " + HttpStatus.UNAUTHORIZED);
                    System.out.println("❌ [AUTH-FILTER] ========================================\n");
                    return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED);
                }

                System.out.println("  返回错误: 认证失败，无法访问系统资源");
                System.out.println("❌ [AUTH-FILTER] ========================================\n");
                return SaResult.error("认证失败，无法访问系统资源").setCode(HttpStatus.UNAUTHORIZED);
            });
    }

    /**
     * 对 actuator 健康检查接口 做账号密码鉴权
     */
    @Bean
    public SaReactorFilter actuatorFilter() {
        String username = SpringUtils.getProperty("spring.cloud.nacos.discovery.metadata.username");
        String password = SpringUtils.getProperty("spring.cloud.nacos.discovery.metadata.userpassword");
        return new SaReactorFilter()
            .addInclude("/actuator", "/actuator/**")
            .setAuth(obj -> {
                SaHttpBasicUtil.check(username + ":" + password);
            })
            .setError(e -> {
                ServerHttpResponse response = SaReactorSyncHolder.getExchange().getResponse();
                response.getHeaders().set(SaTokenConsts.CONTENT_TYPE_KEY, SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED);
            });
    }

}
