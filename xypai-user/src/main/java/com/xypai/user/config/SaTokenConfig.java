package com.xypai.user.config;

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
 * 架构说明：
 * Frontend → Gateway (验证 token) → Service (信任 Gateway)
 *              ✅ 统一认证           ✅ 无需重复验证
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
     * 
     * 如需启用：
     * 1. 仅在 Gateway 宕机的灾备场景下考虑
     * 2. 不要调用 StpUtil.checkLogin()（会要求 Redis session）
     * 3. 只验证 token 是否存在即可
     */
    // @Bean
    // @Order(-100)
    // public SaServletFilter saServletFilter() {
    //     return new SaServletFilter()
    //         .addInclude("/**")
    //         // ====== 内部服务调用接口（无需token验证）======
    //         .addExclude("/api/v1/users/auth/**")           // 认证服务内部调用接口 ⭐⭐⭐
    //         // ====== 业务白名单（唯一公开接口 - 集中管理）======
    //         .addExclude("/api/v1/homepage/**")             // 首页推荐接口（匿名浏览）⭐
    //         // ====== v2/v3 接口（Gateway 已认证，服务内部不再验证）======
    //         .addExclude("/api/v2/**")                      // v2 接口（Gateway已验证token）⭐
    //         .addExclude("/api/v3/**")                      // v3 接口（Gateway已验证token）⭐
    //         // 说明：v1 匿名访问，v2/v3 需要Gateway认证，服务内部信任Gateway
    //         // ====== 系统接口 ======
    //         .addExclude("/v3/api-docs/**")                 // SpringDoc OpenAPI
    //         .addExclude("/favicon.ico", "/error")
    //         .addExclude("/actuator", "/actuator/**")
    //         .setAuth(obj -> {
    //             // ❌ 不要调用 checkLogin() - JWT 模式下会失败
    //             SaRouter.match("/**", r -> StpUtil.checkLogin());
    //         });
    // }
}

