package org.dromara.appbff.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.SearchQueryDTO;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.HomeSearchResultService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索结果控制器
 * <p>
 * 🎯 主要功能:
 * 1. 执行综合搜索（支持4种Tab）
 * 2. 获取全部Tab结果（混合内容）
 * 3. 获取用户Tab结果
 * 4. 获取下单Tab结果
 * 5. 获取话题Tab结果
 * <p>
 * 📄 对应前端页面: 07-搜索结果页面 (路由: /search/results)
 * <p>
 * ⚠️ 注意事项:
 * - 所有搜索接口支持分页
 * - 关键词为必填参数
 * - type参数决定返回哪个Tab的数据
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "搜索结果", description = "搜索结果页面相关接口")
public class SearchResultController {

    private final HomeSearchResultService searchResultService;

    /**
     * 执行搜索（综合）
     * <p>
     * 🎯 接口功能:
     * - 根据type参数返回对应Tab的结果
     * - 同时返回所有Tab的统计信息
     * - 支持分页查询
     * <p>
     * 📱 调用时机:
     * - 页面打开时（首次搜索）
     * - 修改搜索词重新搜索
     * - Tab切换时
     * <p>
     * 🔐 权限要求: 无需登录（游客也可搜索）
     *
     * @param queryDTO 搜索查询参数
     * @return 搜索结果
     */
    @PostMapping("/search")
    @Operation(summary = "执行搜索（综合）", description = "根据type返回对应Tab的结果，同时返回所有Tab的统计信息")
    public R<SearchResultVO> search(@Valid @RequestBody SearchQueryDTO queryDTO) {
        try {
            log.info("执行搜索, keyword: {}, type: {}, pageNum: {}",
                queryDTO.getKeyword(), queryDTO.getType(), queryDTO.getPageNum());

            // 设置默认值
            if (queryDTO.getPageNum() == null) {
                queryDTO.setPageNum(1);
            }
            if (queryDTO.getPageSize() == null) {
                queryDTO.setPageSize(10);
            }
            if (queryDTO.getType() == null || queryDTO.getType().isEmpty()) {
                queryDTO.setType("all");
            }

            SearchResultVO result = searchResultService.search(queryDTO);
            return R.ok(result);

        } catch (Exception e) {
            log.error("搜索失败", e);
            return R.fail("搜索失败");
        }
    }

    /**
     * 获取全部Tab结果
     * <p>
     * 🎯 接口功能:
     * - 返回混合结果（动态、视频、用户）
     * - 瀑布流/网格布局展示
     * - 支持分页加载
     * <p>
     * 📱 调用时机: 用户点击"全部"Tab
     * <p>
     * 🔐 权限要求: 无需登录
     *
     * @param queryDTO 搜索查询参数
     * @return 全部Tab结果
     */
    @GetMapping("/all")
    @Operation(summary = "获取全部Tab结果", description = "返回混合结果（动态、视频、用户）")
    public R<SearchAllResultVO> searchAll(@Valid @ModelAttribute SearchQueryDTO queryDTO) {
        try {
            log.info("获取全部Tab结果, keyword: {}, pageNum: {}", queryDTO.getKeyword(), queryDTO.getPageNum());

            // 设置默认值
            if (queryDTO.getPageNum() == null) {
                queryDTO.setPageNum(1);
            }
            if (queryDTO.getPageSize() == null) {
                queryDTO.setPageSize(10);
            }

            SearchAllResultVO result = searchResultService.searchAll(queryDTO);
            return R.ok(result);

        } catch (Exception e) {
            log.error("获取全部Tab结果失败", e);
            return R.fail("获取全部Tab结果失败");
        }
    }

    /**
     * 获取用户Tab结果
     * <p>
     * 🎯 接口功能:
     * - 返回匹配的用户列表
     * - 包含关注状态（已登录时）
     * - 显示认证状态
     * <p>
     * 📱 调用时机: 用户点击"用户"Tab
     * <p>
     * 🔐 权限要求: 无需登录（但已登录时显示关注状态）
     *
     * @param queryDTO 搜索查询参数
     * @return 用户Tab结果
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户Tab结果", description = "返回匹配的用户列表，包含关注状态")
    public R<SearchUserResultVO> searchUsers(@Valid @ModelAttribute SearchQueryDTO queryDTO) {
        try {
            log.info("获取用户Tab结果, keyword: {}, pageNum: {}", queryDTO.getKeyword(), queryDTO.getPageNum());

            // 设置默认值
            if (queryDTO.getPageNum() == null) {
                queryDTO.setPageNum(1);
            }
            if (queryDTO.getPageSize() == null) {
                queryDTO.setPageSize(10);
            }

            // 获取当前用户ID（如果已登录）
            Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

            SearchUserResultVO result = searchResultService.searchUsers(queryDTO, currentUserId);
            return R.ok(result);

        } catch (Exception e) {
            log.error("获取用户Tab结果失败", e);
            return R.fail("获取用户Tab结果失败");
        }
    }

    /**
     * 获取下单Tab结果
     * <p>
     * 🎯 接口功能:
     * - 返回匹配的服务提供者列表
     * - 包含距离、价格、标签等信息
     * - 显示在线状态
     * <p>
     * 📱 调用时机: 用户点击"下单"Tab
     * <p>
     * 🔐 权限要求: 无需登录
     *
     * @param queryDTO 搜索查询参数
     * @return 下单Tab结果
     */
    @GetMapping("/orders")
    @Operation(summary = "获取下单Tab结果", description = "返回匹配的服务提供者列表")
    public R<SearchOrderResultVO> searchOrders(@Valid @ModelAttribute SearchQueryDTO queryDTO) {
        try {
            log.info("获取下单Tab结果, keyword: {}, pageNum: {}", queryDTO.getKeyword(), queryDTO.getPageNum());

            // 设置默认值
            if (queryDTO.getPageNum() == null) {
                queryDTO.setPageNum(1);
            }
            if (queryDTO.getPageSize() == null) {
                queryDTO.setPageSize(10);
            }

            SearchOrderResultVO result = searchResultService.searchOrders(queryDTO);
            return R.ok(result);

        } catch (Exception e) {
            log.error("获取下单Tab结果失败", e);
            return R.fail("获取下单Tab结果失败");
        }
    }

    /**
     * 获取话题Tab结果
     * <p>
     * 🎯 接口功能:
     * - 返回匹配的话题列表
     * - 包含话题统计信息（动态数、浏览量）
     * - 显示热门标签
     * <p>
     * 📱 调用时机: 用户点击"话题"Tab
     * <p>
     * 🔐 权限要求: 无需登录
     *
     * @param queryDTO 搜索查询参数
     * @return 话题Tab结果
     */
    @GetMapping("/topics")
    @Operation(summary = "获取话题Tab结果", description = "返回匹配的话题列表")
    public R<SearchTopicResultVO> searchTopics(@Valid @ModelAttribute SearchQueryDTO queryDTO) {
        try {
            log.info("获取话题Tab结果, keyword: {}, pageNum: {}", queryDTO.getKeyword(), queryDTO.getPageNum());

            // 设置默认值
            if (queryDTO.getPageNum() == null) {
                queryDTO.setPageNum(1);
            }
            if (queryDTO.getPageSize() == null) {
                queryDTO.setPageSize(10);
            }

            SearchTopicResultVO result = searchResultService.searchTopics(queryDTO);
            return R.ok(result);

        } catch (Exception e) {
            log.error("获取话题Tab结果失败", e);
            return R.fail("获取话题Tab结果失败");
        }
    }
}
