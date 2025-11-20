package org.dromara.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.gateway.config.properties.IgnoreWhiteProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 转发认证过滤器(内部服务外网隔离)
 *
 * @author Lion Li
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardAuthFilter implements GlobalFilter, Ordered {

    /**
     * 统一的Redis key，所有服务使用此key读取Same-Token
     */
    private static final String SAME_TOKEN_REDIS_KEY = "satoken:var:same-token";

    /**
     * 白名单配置
     */
    private final IgnoreWhiteProperties ignoreWhite;

    /**
     * 路径匹配器
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 未开启配置则直接跳过
        if (!SaManager.getConfig().getCheckSameToken()) {
            return chain.filter(exchange);
        }

        // 🔍 添加详细诊断日志
        String path = exchange.getRequest().getURI().getPath();

        // 🔧 修复：从Redis读取Same-Token（而不是调用SaSameUtil.getToken()）
        // 原因：WebFlux和Servlet环境下SaSameUtil存储机制不同
        String sameToken = RedisUtils.getCacheObject(SAME_TOKEN_REDIS_KEY);

        System.out.println("\n🔑 [FORWARD-AUTH-FILTER] ========== 添加 Same-Token ==========");
        System.out.println("  目标路径: " + path);
        System.out.println("  Redis Key: " + SAME_TOKEN_REDIS_KEY);

        // 读取 Same-Token
        String shortToken = sameToken != null && sameToken.length() > 40 ?
            sameToken.substring(0, 40) + "..." : (sameToken != null ? sameToken : "NULL");
        System.out.println("  从 Redis 读取的 Same-Token: " + shortToken);

        // Fail-Fast: 确保token存在
        if (sameToken == null || sameToken.isEmpty()) {
            System.out.println("  ❌ Same-Token 未初始化！");
            System.out.println("  💡 可能原因:");
            System.out.println("     1. Gateway启动时初始化失败");
            System.out.println("     2. Redis中的token已过期或被删除");
            System.out.println("     3. check-same-token配置不正确");
            System.out.println("❌ [FORWARD-AUTH-FILTER] ========================================\n");
            throw new IllegalStateException("Same-Token未初始化，请检查Gateway启动日志");
        }

        // 🔍 对比验证：Sa-Token 内部的 same-token vs Redis 中的 same-token
        String saTokenInternal = SaSameUtil.getToken();
        System.out.println("\n  🔍 [对比验证] Sa-Token 内部 vs Redis:");
        System.out.println("     Sa-Token 内部: " + (saTokenInternal != null && saTokenInternal.length() > 40 ?
            saTokenInternal.substring(0, 40) + "..." : (saTokenInternal != null ? saTokenInternal : "NULL")));
        System.out.println("     Redis 存储:    " + shortToken);

        if (saTokenInternal != null && sameToken != null) {
            if (saTokenInternal.equals(sameToken)) {
                System.out.println("     ✅ 两者一致");
            } else {
                System.out.println("     ⚠️  两者不一致！这可能导致后端验证失败！");
                System.out.println("     Sa-Token 完整: " + saTokenInternal);
                System.out.println("     Redis 完整:    " + sameToken);
            }
        }

        // 检查是否是白名单路径
        boolean isWhiteListed = ignoreWhite.getWhites().stream()
            .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));

        // ⭐ 关键修复：从 exchange attributes 读取 userId 和 clientKey（由 AuthFilter 存储）
        System.out.println("\n  📋 提取用户信息:");
        System.out.println("     请求路径: " + path);
        System.out.println("     是否白名单: " + (isWhiteListed ? "✅ 是（无需认证）" : "❌ 否（需要认证）"));

        String userId = null;
        String clientKey = null;

        // 从 exchange attributes 读取 userId（避免 SaTokenContext 上下文未初始化的问题）
        Object userIdAttr = exchange.getAttributes().get("X-User-Id");
        if (userIdAttr != null) {
            userId = userIdAttr.toString();
            System.out.println("     ✅ 从 exchange attributes 读取 userId: " + userId);
        } else {
            // 只对非白名单路径打印警告（白名单路径本来就不需要用户信息）
            if (!isWhiteListed) {
                System.out.println("     ⚠️ exchange attributes 中没有 userId（非白名单接口应该有用户信息）");
            }
        }

        // 从 exchange attributes 读取 clientKey
        Object clientKeyAttr = exchange.getAttributes().get("X-Client-Key");
        if (clientKeyAttr != null) {
            clientKey = clientKeyAttr.toString();
            System.out.println("     ✅ 从 exchange attributes 读取 clientKey: " + clientKey);
        } else {
            // 只对非白名单路径打印警告
            if (!isWhiteListed) {
                System.out.println("     ⚠️ exchange attributes 中没有 clientKey（非白名单接口应该有客户端标识）");
            }
        }

        // 添加到请求头
        ServerHttpRequest.Builder requestBuilder = exchange
            .getRequest()
            .mutate()
            // 为请求追加 Same-Token 参数
            .header(SaSameUtil.SAME_TOKEN, sameToken);

        // ⭐ 关键修复：添加用户ID到请求头，让下游微服务能够读取
        if (userId != null) {
            requestBuilder.header("X-User-Id", userId);
            requestBuilder.header("X-Login-Id", userId);  // 兼容性header
            System.out.println("  ✅ 已添加用户ID到请求头: X-User-Id = " + userId);
        }

        if (clientKey != null) {
            requestBuilder.header("X-Client-Key", clientKey);
        }

        ServerHttpRequest newRequest = requestBuilder.build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

        System.out.println("\n  ✅ Same-Token 已添加到请求头");
        System.out.println("  请求头名称: " + SaSameUtil.SAME_TOKEN);
        System.out.println("  请求头值: " + shortToken);
        System.out.println("✅ [FORWARD-AUTH-FILTER] ========================================\n");
        
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

