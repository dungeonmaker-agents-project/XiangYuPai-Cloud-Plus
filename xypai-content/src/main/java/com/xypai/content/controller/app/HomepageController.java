package com.xypai.content.controller.app;

// ❌ 不使用 @SaCheckLogin - JWT Simple Mode下，Gateway已验证Token
// import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页控制器
 * 为移动端首页提供用户列表、推荐用户等数据
 *
 * @author xypai
 * @date 2025-11-08
 */
@Slf4j
@Tag(name = "首页", description = "首页数据接口（用户列表、推荐用户等）")
@RestController
@RequestMapping("/api/v1/homepage")
@RequiredArgsConstructor
@Validated
public class HomepageController {

    /**
     * 获取首页用户列表（需要认证）
     * 
     * 前端路径: src/features/Homepage/MainPage/useHomeData.ts
     * 前端调用: homepageApiEnhanced.getUserList()
     * 
     * 筛选类型:
     * - all: 全部用户
     * - nearby: 附近的人（基于位置）
     * - online: 在线用户
     * - new: 新用户
     * - vip: VIP用户
     * 
     * @param filterTab 筛选类型（all/nearby/online/new/vip）
     * @param region 地区代码（可选）
     * @param page 页码（从1开始）
     * @param limit 每页数量（默认20）
     * @return 用户列表数据
     */
    @Operation(summary = "获取首页用户列表", description = "根据筛选条件获取用户列表（需要登录Token，由Gateway验证）")
    @GetMapping("/users/list")
    // ❌ 不使用 @SaCheckLogin - JWT Simple Mode下会失败
    // ✅ Gateway已经验证了Token，Content Service信任Gateway的结果
    public R<Map<String, Object>> getUserList(
            @Parameter(description = "筛选类型: all/nearby/online/new/vip") 
            @RequestParam(defaultValue = "all") String filterTab,
            
            @Parameter(description = "地区代码") 
            @RequestParam(required = false) String region,
            
            @Parameter(description = "页码（从1开始）") 
            @RequestParam(defaultValue = "1") Integer page,
            
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🎯 [HOMEPAGE CONTROLLER] ✅ 请求成功到达Controller！");
        log.info("📋 [HOMEPAGE] 首页用户列表接口被调用");
        
        try {
            // 从 Sa-Token 上下文中提取用户信息
            Long userId = LoginHelper.getUserId();
            String username = LoginHelper.getUsername();
            String clientId = (String) StpUtil.getExtra(LoginHelper.CLIENT_KEY);
            
            log.info("   ✅ 认证成功: userId={}, username={}, clientId={}", userId, username, clientId);
            log.info("   📊 查询参数: filterTab={}, region={}, page={}, limit={}", filterTab, region, page, limit);
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            
            // 用户列表（模拟数据 - 实际应该查询数据库）
            List<Map<String, Object>> users = generateMockUsers(filterTab, region, limit);
            data.put("users", users);
            data.put("total", users.size());
            data.put("page", page);
            data.put("limit", limit);
            data.put("hasMore", true);
            
            // 查询信息
            Map<String, Object> queryInfo = new HashMap<>();
            queryInfo.put("filterTab", filterTab);
            queryInfo.put("region", region);
            queryInfo.put("requestedBy", username);
            queryInfo.put("userId", userId);
            queryInfo.put("timestamp", LocalDateTime.now());
            data.put("queryInfo", queryInfo);
            
            log.info("   ✅ 返回用户数量: {}", users.size());
            return R.ok(data);
            
        } catch (Exception e) {
            log.error("❌ [HOMEPAGE] 获取用户列表失败: {}", e.getMessage(), e);
            return R.fail("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取精选用户列表（需要认证）
     * 
     * @param limit 数量限制
     * @return 精选用户列表
     */
    @Operation(summary = "获取精选用户", description = "获取首页精选用户推荐（需要登录Token，由Gateway验证）")
    @GetMapping("/featured-users")
    // ❌ 不使用 @SaCheckLogin - Gateway已验证
    public R<List<Map<String, Object>>> getFeaturedUsers(
            @Parameter(description = "数量限制") 
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("📋 [HOMEPAGE] 精选用户接口被调用: limit={}", limit);
        
        try {
            Long userId = LoginHelper.getUserId();
            String username = LoginHelper.getUsername();
            String clientId = (String) StpUtil.getExtra(LoginHelper.CLIENT_KEY);
            
            log.info("   ✅ 认证成功: userId={}, username={}, clientId={}", userId, username, clientId);
            
            // 生成精选用户（模拟数据）
            List<Map<String, Object>> users = generateFeaturedUsers(limit);
            
            log.info("   ✅ 返回精选用户数量: {}", users.size());
            return R.ok(users);
            
        } catch (Exception e) {
            log.error("❌ [HOMEPAGE] 获取精选用户失败: {}", e.getMessage(), e);
            return R.fail("获取精选用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取首页配置（需要认证）
     * 
     * @return 首页配置信息
     */
    @Operation(summary = "获取首页配置", description = "获取首页模块配置信息（需要登录Token，由Gateway验证）")
    @GetMapping("/config")
    // ❌ 不使用 @SaCheckLogin - Gateway已验证
    public R<Map<String, Object>> getHomepageConfig() {
        log.info("📋 [HOMEPAGE] 首页配置接口被调用");
        
        try {
            Long userId = LoginHelper.getUserId();
            String username = LoginHelper.getUsername();
            String clientId = (String) StpUtil.getExtra(LoginHelper.CLIENT_KEY);
            
            log.info("   ✅ 认证成功: userId={}, username={}, clientId={}", userId, username, clientId);
            
            Map<String, Object> config = new HashMap<>();
            config.put("gameBannerEnabled", true);
            config.put("teamPartyEnabled", true);
            config.put("limitedOffersEnabled", true);
            config.put("filterTabs", List.of("all", "nearby", "online", "new", "vip"));
            config.put("defaultFilterTab", "all");
            config.put("requestedBy", username);
            config.put("timestamp", LocalDateTime.now());
            
            log.info("   ✅ 返回首页配置");
            return R.ok(config);
            
        } catch (Exception e) {
            log.error("❌ [HOMEPAGE] 获取首页配置失败: {}", e.getMessage(), e);
            return R.fail("获取首页配置失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成模拟用户数据
     */
    private List<Map<String, Object>> generateMockUsers(String filterTab, String region, Integer limit) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        for (int i = 1; i <= limit; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("userId", 1000 + i);
            user.put("nickname", filterTab + "用户" + i);
            user.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=" + i);
            user.put("age", 20 + (i % 15));
            user.put("gender", i % 2 == 0 ? "male" : "female");
            user.put("city", region != null ? region : "深圳");
            user.put("distance", String.format("%.1fkm", 0.5 + (i * 0.3)));
            user.put("isOnline", i % 3 == 0);
            user.put("isVip", i % 5 == 0);
            user.put("isRealVerified", i % 4 == 0);
            user.put("rating", 4.0 + (i % 10) * 0.1);
            user.put("services", List.of("陪玩", "聊天"));
            user.put("price", "¥" + (50 + i * 10) + "/小时");
            user.put("tags", List.of("温柔", "有趣", "靠谱"));
            
            users.add(user);
        }
        
        return users;
    }

    /**
     * 生成精选用户数据
     */
    private List<Map<String, Object>> generateFeaturedUsers(Integer limit) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        for (int i = 1; i <= limit; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("userId", 2000 + i);
            user.put("nickname", "精选用户" + i);
            user.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=featured" + i);
            user.put("age", 22 + (i % 8));
            user.put("gender", i % 2 == 0 ? "female" : "male");
            user.put("city", "深圳");
            user.put("isVip", true);
            user.put("isRealVerified", true);
            user.put("rating", 4.8 + (i % 3) * 0.1);
            user.put("services", List.of("游戏陪玩", "语音聊天", "技能教学"));
            user.put("price", "¥" + (100 + i * 20) + "/小时");
            user.put("tags", List.of("高人气", "认证", "专业"));
            user.put("orderCount", 500 + i * 50);
            
            users.add(user);
        }
        
        return users;
    }
}

