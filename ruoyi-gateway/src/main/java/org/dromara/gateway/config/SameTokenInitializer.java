package org.dromara.gateway.config;

import cn.dev33.satoken.same.SaSameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Same-Token 初始化器
 *
 * <p>在Gateway启动时生成Same-Token并存储到Redis统一key</p>
 * <p>解决WebFlux (Gateway) 和 Servlet (微服务) 环境下Same-Token不一致的问题</p>
 *
 * @author XyPai Team
 * @date 2025-11-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SameTokenInitializer implements ApplicationRunner {

    /**
     * 统一的Redis key，所有服务使用此key读取Same-Token
     */
    private static final String SAME_TOKEN_REDIS_KEY = "satoken:var:same-token";

    /**
     * Same-Token有效期：7天
     */
    private static final Duration SAME_TOKEN_DURATION = Duration.ofDays(7);

    @Override
    public void run(ApplicationArguments args) {
        log.info("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔐 [SAME-TOKEN INIT] 开始初始化Same-Token");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // 1. 使用Sa-Token API生成Same-Token
            String sameToken = SaSameUtil.refreshToken();
            log.info("   ✅ 通过Sa-Token API生成Same-Token: {}...",
                sameToken != null ? sameToken.substring(0, Math.min(40, sameToken.length())) : "NULL");

            // 2. Fail-Fast: 确保token生成成功
            if (sameToken == null || sameToken.isEmpty()) {
                throw new IllegalStateException("❌ Same-Token生成失败！请检查Sa-Token配置");
            }

            // 3. 验证Sa-Token自动存储
            String tokenFromSaToken = SaSameUtil.getToken();
            log.info("   📋 Sa-Token已自动存储到Redis");
            log.info("   🔍 验证：从Sa-Token API读取: {}...",
                tokenFromSaToken != null ? tokenFromSaToken.substring(0, Math.min(40, tokenFromSaToken.length())) : "NULL");

            if (!sameToken.equals(tokenFromSaToken)) {
                log.warn("   ⚠️  Sa-Token API返回的token不一致");
            } else {
                log.info("   ✅ 验证成功：Same-Token正确存储");
            }

            // 4. 同时存储到统一的Redis key供所有服务使用
            RedisUtils.setCacheObject(SAME_TOKEN_REDIS_KEY, sameToken, SAME_TOKEN_DURATION);
            log.info("   📋 同时存储到Redis Key: {}", SAME_TOKEN_REDIS_KEY);
            log.info("   ⏰ 有效期: {} 天", SAME_TOKEN_DURATION.toDays());

            // 5. 验证Redis存储
            String tokenFromRedis = RedisUtils.getCacheObject(SAME_TOKEN_REDIS_KEY);
            if (tokenFromRedis == null) {
                throw new IllegalStateException("❌ Same-Token存储到Redis失败！");
            }

            if (!sameToken.equals(tokenFromRedis)) {
                throw new IllegalStateException("❌ Same-Token存储后读取不一致！");
            }

            log.info("   ✅ Redis存储验证成功");

            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("🎉 [SAME-TOKEN INIT] Same-Token初始化完成");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        } catch (Exception e) {
            log.error("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("❌ [SAME-TOKEN INIT] Same-Token初始化失败！");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("   错误: {}", e.getMessage());
            log.error("   💡 请检查:");
            log.error("      1. Sa-Token配置是否正确");
            log.error("      2. Redis是否正常运行");
            log.error("      3. check-same-token是否已开启");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            // Fail-Fast: Gateway无法启动
            throw new IllegalStateException("Gateway初始化失败：Same-Token初始化异常", e);
        }
    }
}
