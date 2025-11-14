package org.dromara.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.same.SaSameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 转发认证过滤器(内部服务外网隔离)
 *
 * @author Lion Li
 */
@Slf4j
@Component
public class ForwardAuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 未开启配置则直接跳过
        if (!SaManager.getConfig().getCheckSameToken()) {
            return chain.filter(exchange);
        }
        
        // 🔍 添加详细诊断日志
        String path = exchange.getRequest().getURI().getPath();
        String sameToken = SaSameUtil.getToken();
        
        log.info("\n🔑 [GATEWAY SAME-TOKEN] 为请求添加 Same-Token");
        log.info("   目标路径: {}", path);
        log.info("   Same-Token: {}", sameToken != null ? 
            (sameToken.length() > 40 ? sameToken.substring(0, 40) + "..." : sameToken) : 
            "❌ NULL");
        
        ServerHttpRequest newRequest = exchange
            .getRequest()
            .mutate()
            // 为请求追加 Same-Token 参数
            .header(SaSameUtil.SAME_TOKEN, sameToken)
            .build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        
        log.info("   ✅ Same-Token 已添加到请求头\n");
        
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

