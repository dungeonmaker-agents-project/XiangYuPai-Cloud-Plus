package com.xypai.chat.config;

// 已禁用 SaServletFilter，保留 import 以便将来需要时取消注释
// import cn.dev33.satoken.filter.SaServletFilter;
// import cn.dev33.satoken.router.SaRouter;
// import cn.dev33.satoken.stp.StpUtil;
// import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.core.annotation.Order;

/**
 * Sa-Token 配置
 *
 * JWT Simple Mode（无状态模式）说明：
 * - Gateway 已经验证所有 token（签名、过期时间等）
 * - 服务不需要重复验证 token（避免 NotLoginException: token 无效）
 * - 服务信任 Gateway 的验证结果，直接使用 LoginHelper 获取用户信息
 * - JWT 是自包含的，不需要 Redis session 验证
 *
 * @author xypai
 */
@Configuration
public class SaTokenConfig {

    /**
     * ❌ 已禁用 SaServletFilter
     * 
     * 原因：使用 JWT Simple Mode（无状态模式）
     * - Gateway 已经验证了所有 token
     * - 服务调用 StpUtil.checkLogin() 会失败（JWT 无 session）
     * - 微服务架构下，服务应信任 Gateway 的验证结果
     */
    // @Bean
    // @Order(-100)
    // public SaServletFilter saServletFilter() {
    //     return new SaServletFilter()
    //         .addInclude("/**")
    //         .addExclude("/api/v1/chat/**")
    //         .addExclude("/ws/**")
    //         // 放行 SpringDoc OpenAPI 文档（用于 Apifox 等工具）
    //         .addExclude("/v3/api-docs/**")
    //         .addExclude("/favicon.ico", "/error")
    //         .addExclude("/actuator", "/actuator/**")
    //         .setAuth(obj -> {
    //             // ❌ 不要调用 checkLogin() - JWT 模式下会失败
    //             SaRouter.match("/**", r -> StpUtil.checkLogin());
    //         });
    // }
}

