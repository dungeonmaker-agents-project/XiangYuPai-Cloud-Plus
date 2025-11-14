package org.dromara.common.security.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.utils.SpringUtils;
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
@Slf4j
@AutoConfiguration
public class SecurityConfiguration implements WebMvcConfigurer {

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
                    // 🔍 添加详细诊断日志
                    try {
                        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                        String requestUri = request.getRequestURI();
                        String requestSameToken = request.getHeader(SaSameUtil.SAME_TOKEN);
                        String expectedSameToken = SaSameUtil.getToken();
                        String serviceName = SpringUtils.getProperty("spring.application.name");
                        
                        log.info("\n🔐 [SAME-TOKEN CHECK] {} - 验证请求是否来自Gateway", serviceName);
                        log.info("   请求路径: {}", requestUri);
                        log.info("   请求中的 Same-Token: {}", requestSameToken != null ? 
                            (requestSameToken.length() > 40 ? requestSameToken.substring(0, 40) + "..." : requestSameToken) : 
                            "❌ NULL");
                        log.info("   期望的 Same-Token: {}", expectedSameToken != null ? 
                            (expectedSameToken.length() > 40 ? expectedSameToken.substring(0, 40) + "..." : expectedSameToken) : 
                            "❌ NULL");
                        log.info("   两者是否一致: {}", requestSameToken != null && requestSameToken.equals(expectedSameToken));
                        
                        // 执行原始验证
                        SaSameUtil.checkCurrentRequestToken();
                        
                        log.info("   ✅ Same-Token验证通过\n");
                    } catch (Exception e) {
                        log.error("\n   ❌ Same-Token验证失败: {}", e.getMessage());
                        log.error("   异常类型: {}", e.getClass().getName());
                        log.error("   💡 可能原因:");
                        log.error("      1. Gateway 未正确添加 Same-Token header");
                        log.error("      2. 微服务和 Gateway 的 Same-Token 不同步");
                        log.error("      3. check-same-token 配置不一致\n");
                        throw e;
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
