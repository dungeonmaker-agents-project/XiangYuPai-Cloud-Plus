package org.dromara.common.security.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.util.SaResult;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限安全配置
 *
 * @author Lion Li
 */
@AutoConfiguration
public class SecurityConfiguration implements WebMvcConfigurer {

    /**
     * 统一的Redis key，所有服务使用此key读取Same-Token
     */
    private static final String SAME_TOKEN_REDIS_KEY = "satoken:var:same-token";

    /**
     * 注册sa-token的拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义验证规则
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

    /**
     * 校验是否从网关转发
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
            .addInclude("/**")
            .addExclude("/actuator", "/actuator/**")
            .setAuth(obj -> {
                if (SaManager.getConfig().getCheckSameToken()) {
                    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

                    // 使用自定义验证逻辑，从Redis读取Gateway生成的Same-Token
                    // 原因：WebFlux和Servlet环境下SaSameUtil内部存储机制不同
                    String expectedToken = RedisUtils.getCacheObject(SAME_TOKEN_REDIS_KEY);
                    String actualToken = request.getHeader(SaSameUtil.SAME_TOKEN);

                    if (expectedToken == null || expectedToken.isEmpty()) {
                        throw new RuntimeException("Redis中的Same-Token未初始化或已过期");
                    }

                    if (actualToken == null || actualToken.isEmpty()) {
                        throw new RuntimeException("请求头中未携带Same-Token");
                    }

                    if (!expectedToken.equals(actualToken)) {
                        throw new RuntimeException("Same-Token不匹配");
                    }
                }
            })
            .setError(e -> SaResult.error("认证失败，无法访问系统资源").setCode(HttpStatus.UNAUTHORIZED));
    }

    /**
     * 对 actuator 健康检查接口 做账号密码鉴权
     */
    @Bean
    public SaServletFilter actuatorFilter() {
        String username = SpringUtils.getProperty("spring.cloud.nacos.discovery.metadata.username");
        String password = SpringUtils.getProperty("spring.cloud.nacos.discovery.metadata.userpassword");
        return new SaServletFilter()
            .addInclude("/actuator", "/actuator/**")
            .setAuth(obj -> {
                SaHttpBasicUtil.check(username + ":" + password);
            })
            .setError(e -> SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED));
    }

}
