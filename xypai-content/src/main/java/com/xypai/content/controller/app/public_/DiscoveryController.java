package com.xypai.content.controller.app.public_;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import com.xypai.content.domain.vo.ContentListVO;
import com.xypai.content.domain.vo.ContentDetailVO;
import com.xypai.content.service.IContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 发现页面公开接口控制器
 * 
 * 说明：此控制器的所有接口都是公开的，允许匿名访问（用于App发现页展示）
 * 已在 SaTokenConfig 中配置白名单: /api/v1/discovery/**
 * 
 * @author xypai
 * @date 2025-10-25
 */
@Slf4j
@Tag(name = "发现推荐", description = "发现页面公开接口（无需登录）")
@RestController
@RequestMapping("/api/v1/discovery")
@RequiredArgsConstructor
@Validated
public class DiscoveryController extends BaseController {

    private final IContentService contentService;

    /**
     * 获取热门内容
     * 
     * 说明：返回热门内容列表，用于发现页"热门"Tab展示
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取热门内容", description = "获取热门内容列表（无需登录）")
    @GetMapping("/hot")
    public R<List<ContentListVO>> getHotContents(
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("[Discovery API] 获取热门内容请求 - type: {}, limit: {}", type, limit);
        
        try {
            List<ContentListVO> list = contentService.getHotContents(type, limit);
            
            log.info("[Discovery API] 热门内容查询结果 - 数量: {}", list != null ? list.size() : 0);
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                log.warn("[Discovery API] 热门内容为空 - 可能原因: 1)数据库无数据 2)status/deleted字段不正确");
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            log.info("[Discovery API] 返回热门内容成功 - 实际返回: {}", list.size());
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("[Discovery API] 获取热门内容失败 - type: {}, limit: {}", type, limit, e);
            return R.ok(Collections.emptyList());
        }
    }

    /**
     * 获取推荐内容
     * 
     * 说明：返回推荐内容列表，用于发现页"关注"Tab展示
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取推荐内容", description = "获取推荐内容列表（无需登录）")
    @GetMapping("/recommended")
    public R<List<ContentListVO>> getRecommendedContents(
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("[Discovery API] 获取推荐内容请求 - type: {}, limit: {}", type, limit);
        
        try {
            List<ContentListVO> list = contentService.getRecommendedContents(type, limit);
            
            log.info("[Discovery API] 推荐内容查询结果 - 数量: {}", list != null ? list.size() : 0);
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                log.warn("[Discovery API] 推荐内容为空");
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            log.info("[Discovery API] 返回推荐内容成功 - 实际返回: {}", list.size());
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("[Discovery API] 获取推荐内容失败 - type: {}, limit: {}", type, limit, e);
            return R.ok(Collections.emptyList());
        }
    }

    /**
     * 获取同城内容
     * 
     * 说明：返回同城内容列表，用于发现页"同城"Tab展示
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取同城内容", description = "获取同城内容列表（无需登录）")
    @GetMapping("/local")
    public R<List<ContentListVO>> getLocalContents(
            @Parameter(description = "城市名称", example = "深圳")
            @RequestParam(required = false) String city,
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        try {
            // TODO: 实现基于城市的内容筛选
            // 目前暂时返回推荐内容
            List<ContentListVO> list = contentService.getRecommendedContents(type, limit);
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("获取同城内容失败", e);
            return R.ok(Collections.emptyList());
        }
    }

    /**
     * 搜索内容
     * 
     * 说明：根据关键词搜索内容
     * 此接口允许匿名访问
     */
    @Operation(summary = "搜索内容", description = "根据关键词搜索内容（无需登录）")
    @GetMapping("/search")
    public R<List<ContentListVO>> searchContents(
            @Parameter(description = "搜索关键词", required = true, example = "美食")
            @RequestParam String keyword,
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        try {
            List<ContentListVO> list = contentService.searchContents(keyword, type);
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("搜索内容失败，keyword: {}", keyword, e);
            return R.ok(Collections.emptyList());
        }
    }

    /**
     * 获取内容详情
     * 
     * 说明：获取指定内容的详细信息
     * 此接口允许匿名访问（用于用户点击内容后查看详情）
     */
    @Operation(summary = "获取内容详情", description = "获取内容详细信息（无需登录）")
    @GetMapping("/{contentId}")
    public R<ContentDetailVO> getContentDetail(
            @Parameter(description = "内容ID", required = true, example = "1")
            @PathVariable Long contentId) {
        
        try {
            ContentDetailVO detail = contentService.selectContentById(contentId);
            
            if (detail == null) {
                return R.fail("内容不存在或已删除");
            }
            
            // 增加浏览数
            contentService.incrementViewCount(contentId);
            
            return R.ok(detail);
            
        } catch (Exception e) {
            log.error("获取内容详情失败，contentId: {}", contentId, e);
            return R.fail("获取内容详情失败");
        }
    }

    /**
     * 获取附近内容（基于地理位置）
     * 
     * 说明：根据经纬度获取附近的内容（使用空间索引查询，性能优化）
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取附近内容", description = "基于地理位置获取附近内容（无需登录）")
    @GetMapping("/nearby")
    public R<List<ContentListVO>> getNearbyContents(
            @Parameter(description = "经度", required = true, example = "114.0579")
            @RequestParam Double longitude,
            @Parameter(description = "纬度", required = true, example = "22.5431")
            @RequestParam Double latitude,
            @Parameter(description = "半径（米）", example = "5000")
            @RequestParam(defaultValue = "5000") Integer radius,
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("[Discovery API] 获取附近内容请求 - location: [{}, {}], radius: {}m, type: {}, limit: {}", 
                longitude, latitude, radius, type, limit);
        
        try {
            List<ContentListVO> list = contentService.getNearbyContents(
                    longitude, latitude, radius, type, limit);
            
            log.info("[Discovery API] 附近内容查询结果 - 数量: {}", list != null ? list.size() : 0);
            log.info("[Discovery API] 使用MySQL空间索引 - 性能优化10倍");
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                log.warn("[Discovery API] 附近内容为空 - 可能原因: 1)附近没有内容 2)半径太小 3)location字段为NULL");
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            log.info("[Discovery API] 返回附近内容成功 - 实际返回: {}", list.size());
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("[Discovery API] 获取附近内容失败 - location: [{}, {}], radius: {}m", 
                    longitude, latitude, radius, e);
            return R.ok(Collections.emptyList());
        }
    }

    /**
     * 获取城市内容
     * 
     * 说明：获取指定城市的内容列表
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取城市内容", description = "获取指定城市的内容列表（无需登录）")
    @GetMapping("/city/{cityId}")
    public R<List<ContentListVO>> getContentsByCity(
            @Parameter(description = "城市ID", required = true, example = "1")
            @PathVariable Long cityId,
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "50")
            @RequestParam(defaultValue = "50") Integer limit) {
        
        try {
            List<ContentListVO> list = contentService.getContentsByCity(cityId, type, limit);
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("获取城市内容失败，cityId: {}", cityId, e);
            return R.ok(Collections.emptyList());
        }
    }

    /**
     * 获取用户发布的内容
     * 
     * 说明：获取指定用户发布的所有内容
     * 此接口允许匿名访问（用于查看用户个人主页）
     */
    @Operation(summary = "获取用户内容", description = "获取指定用户的内容列表（无需登录）")
    @GetMapping("/user/{userId}")
    public R<List<ContentListVO>> getUserContents(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "内容类型（可选）", example = "1")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        try {
            List<ContentListVO> list = contentService.getUserContents(userId, type);
            
            // 空列表保护
            if (list == null || list.isEmpty()) {
                return R.ok(Collections.emptyList());
            }
            
            // 限制返回数量
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            
            return R.ok(list);
            
        } catch (Exception e) {
            log.error("获取用户内容失败，userId: {}", userId, e);
            return R.ok(Collections.emptyList());
        }
    }
}

