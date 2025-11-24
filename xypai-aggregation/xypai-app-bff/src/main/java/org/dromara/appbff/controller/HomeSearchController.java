package org.dromara.appbff.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.SearchHistoryDeleteDTO;
import org.dromara.appbff.domain.dto.SearchSuggestQueryDTO;
import org.dromara.appbff.domain.vo.SearchDeleteVO;
import org.dromara.appbff.domain.vo.SearchInitVO;
import org.dromara.appbff.domain.vo.SearchSuggestVO;
import org.dromara.appbff.service.HomeSearchService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页搜索控制器
 * <p>
 * 🎯 主要功能:
 * 1. 获取搜索初始数据（搜索历史、热门搜索）
 * 2. 获取搜索建议（实时输入提示）
 * 3. 删除搜索历史（单条或全部）
 * <p>
 * 📄 对应前端页面: 06-搜索页面 (路由: /search)
 * <p>
 * ⚠️ 注意事项:
 * - 搜索建议接口前端需防抖300ms后调用
 * - 搜索历史最多显示10条
 * - 删除历史需要提供keyword或clearAll参数
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "首页搜索", description = "搜索页面相关接口")
public class HomeSearchController {

    private final HomeSearchService searchService;

    /**
     * 获取搜索初始数据
     * <p>
     * 🎯 接口功能:
     * - 返回用户的搜索历史（最多10条）
     * - 返回热门搜索关键词
     * - 返回搜索框占位符
     * <p>
     * 📱 调用时机: 打开搜索页面时
     * <p>
     * 🔐 权限要求: 需要登录
     *
     * @return 搜索初始数据
     */
    @GetMapping("/init")
    @Operation(summary = "获取搜索初始数据", description = "返回搜索历史、热门搜索、搜索框占位符")
    public R<SearchInitVO> getSearchInit() {
        try {
            // 从 Sa-Token 获取当前用户ID
            Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

            log.info("获取搜索初始数据, userId: {}", userId);

            SearchInitVO result = searchService.getSearchInit(userId);
            return R.ok(result);

        } catch (Exception e) {
            log.error("获取搜索初始数据失败", e);
            return R.fail("获取搜索初始数据失败");
        }
    }

    /**
     * 获取搜索建议
     * <p>
     * 🎯 接口功能:
     * - 根据输入关键词实时返回建议
     * - 包括用户、话题、关键词三种类型
     * - 高亮匹配的关键词部分
     * <p>
     * 📱 调用时机: 用户输入关键词时（防抖300ms）
     * <p>
     * 🔐 权限要求: 无需登录（游客也可搜索）
     *
     * @param queryDTO 搜索建议查询参数
     * @return 搜索建议列表
     */
    @GetMapping("/suggest")
    @Operation(summary = "获取搜索建议", description = "根据输入关键词返回建议列表（防抖300ms后调用）")
    public R<SearchSuggestVO> getSearchSuggestions(
        @Valid @ModelAttribute SearchSuggestQueryDTO queryDTO
    ) {
        try {
            log.info("获取搜索建议, keyword: {}", queryDTO.getKeyword());

            // 设置默认limit
            if (queryDTO.getLimit() == null) {
                queryDTO.setLimit(10);
            }

            SearchSuggestVO result = searchService.getSearchSuggestions(queryDTO);
            return R.ok(result);

        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return R.fail("获取搜索建议失败");
        }
    }

    /**
     * 删除搜索历史
     * <p>
     * 🎯 接口功能:
     * - 删除单条搜索历史（指定keyword）
     * - 清空所有搜索历史（clearAll=true）
     * <p>
     * 📱 调用时机:
     * - 长按历史记录项（删除单条）
     * - 点击"清空"按钮（清空所有）
     * <p>
     * 🔐 权限要求: 需要登录
     *
     * @param deleteDTO 删除参数
     * @return 删除结果
     */
    @DeleteMapping("/history")
    @Operation(summary = "删除搜索历史", description = "删除单条历史或清空所有历史")
    public R<SearchDeleteVO> deleteSearchHistory(
        @Valid @RequestBody SearchHistoryDeleteDTO deleteDTO
    ) {
        try {
            // 从 Sa-Token 获取当前用户ID
            if (!StpUtil.isLogin()) {
                return R.fail("请先登录");
            }
            Long userId = StpUtil.getLoginIdAsLong();

            log.info("删除搜索历史, userId: {}, deleteDTO: {}", userId, deleteDTO);

            SearchDeleteVO result = searchService.deleteSearchHistory(userId, deleteDTO);
            return R.ok(result);

        } catch (Exception e) {
            log.error("删除搜索历史失败", e);
            return R.fail("删除搜索历史失败");
        }
    }
}
