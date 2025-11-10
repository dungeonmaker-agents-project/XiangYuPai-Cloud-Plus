package com.xypai.content.config;

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
    //         // ====== 业务白名单（允许匿名访问）======
    //         // 发现页面公开接口（新增）
    //         .addExclude("/api/v1/discovery/**")            // 发现页面所有接口（热门、推荐、同城、搜索）
    //         // 原有内容接口（保留兼容）
    //         .addExclude("/api/v1/content/list")            // 内容列表（匿名浏览）
    //         .addExclude("/api/v1/content/*/detail")        // 内容详情（匿名浏览）
    //         .addExclude("/api/v1/content/hot")             // 热门内容（匿名浏览）
    //         // 注意：评论列表暂时不公开，可根据需求调整
    //         // .addExclude("/api/v1/comments/list")
    //         // ====== 文档接口 ======
    //         .addExclude("/v3/api-docs/**")                 // SpringDoc OpenAPI
    //         .addExclude("/favicon.ico", "/error")
    //         .addExclude("/actuator", "/actuator/**")
    //         .setAuth(obj -> {
    //             // ❌ 不要调用 checkLogin() - JWT 模式下会失败
    //             SaRouter.match("/**", r -> StpUtil.checkLogin());
    //         });
    // }
}

