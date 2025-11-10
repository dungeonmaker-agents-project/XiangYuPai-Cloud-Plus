package com.xypai.content.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.content.domain.dto.ContentAddDTO;
import com.xypai.content.domain.dto.ContentQueryDTO;
import com.xypai.content.domain.dto.ContentUpdateDTO;
import com.xypai.content.domain.vo.ContentDetailVO;
import com.xypai.content.domain.vo.ContentListVO;
import com.xypai.content.service.IContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 内容管理控制�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "内容管理", description = "内容发布与管理API")
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Validated
public class ContentController extends BaseController {

    private final IContentService contentService;

    /**
     * 查询内容列表
     */
    @Operation(summary = "查询内容列表", description = "分页查询内容列表信息")
    @GetMapping("/list")
    @SaCheckPermission("content:content:list")
    public R<List<ContentListVO>> list(ContentQueryDTO query) {
        List<ContentListVO> list = contentService.selectContentList(query);
        return R.ok(list);
    }

    /**
     * 获取内容详细信息
     */
    @Operation(summary = "获取内容详细信息", description = "根据内容ID获取详细信息")
    @GetMapping("/{contentId}")
    @SaCheckPermission("content:content:query")
    @Log(title = "内容管理", businessType = BusinessType.OTHER)
    public R<ContentDetailVO> getInfo(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return R.ok(contentService.selectContentById(contentId));
    }

    /**
     * 发布内容
     */
    @Operation(summary = "发布内容", description = "创建新内容")
    @PostMapping
    @SaCheckPermission("content:content:add")
    @Log(title = "内容管理", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated @RequestBody ContentAddDTO contentAddDTO) {
        return contentService.insertContent(contentAddDTO) ? R.ok() : R.fail();
    }

    /**
     * 修改内容
     */
    @Operation(summary = "修改内容", description = "更新内容信息")
    @PutMapping
    @SaCheckPermission("content:content:edit")
    @Log(title = "内容管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody ContentUpdateDTO contentUpdateDTO) {
        return contentService.updateContent(contentUpdateDTO) ? R.ok() : R.fail();
    }

    /**
     * 删除内容
     */
    @Operation(summary = "删除内容", description = "根据内容ID删除内容")
    @DeleteMapping("/{contentIds}")
    @SaCheckPermission("content:content:remove")
    @Log(title = "内容管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "内容ID数组", required = true)
            @PathVariable Long[] contentIds) {
        return contentService.deleteContentByIds(Arrays.asList(contentIds)) ? R.ok() : R.fail();
    }

    /**
     * 发布内容
     */
    @Operation(summary = "发布内容", description = "将草稿内容发布")
    @PutMapping("/{contentId}/publish")
    @SaCheckPermission("content:content:edit")
    @Log(title = "发布内容", businessType = BusinessType.UPDATE)
    public R<Void> publishContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return contentService.publishContent(contentId) ? R.ok() : R.fail();
    }

    /**
     * 下架内容
     */
    @Operation(summary = "下架内容", description = "将已发布内容下架")
    @PutMapping("/{contentId}/archive")
    @SaCheckPermission("content:content:edit")
    @Log(title = "下架内容", businessType = BusinessType.UPDATE)
    public R<Void> archiveContent(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return contentService.archiveContent(contentId) ? R.ok() : R.fail();
    }

    /**
     * 获取热门内容
     */
    @Operation(summary = "获取热门内容", description = "获取热门内容列表")
    @GetMapping("/hot")
    @SaCheckPermission("content:content:query")
    public R<List<ContentListVO>> getHotContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ContentListVO> list = contentService.getHotContents(type, limit);
        return R.ok(list);
    }

    /**
     * 获取推荐内容
     */
    @Operation(summary = "获取推荐内容", description = "获取个性化推荐内容")
    @GetMapping("/recommended")
    @SaCheckPermission("content:content:query")
    public R<List<ContentListVO>> getRecommendedContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "20") Integer limit) {
        List<ContentListVO> list = contentService.getRecommendedContents(type, limit);
        return R.ok(list);
    }

    /**
     * 搜索内容
     */
    @Operation(summary = "搜索内容", description = "根据关键词搜索内容")
    @GetMapping("/search")
    @SaCheckPermission("content:content:query")
    public R<List<ContentListVO>> searchContents(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type) {
        List<ContentListVO> list = contentService.searchContents(keyword, type);
        return R.ok(list);
    }

    /**
     * 获取用户内容
     */
    @Operation(summary = "获取用户内容", description = "获取指定用户发布的内容")
    @GetMapping("/user/{userId}")
    @SaCheckPermission("content:content:query")
    public R<List<ContentListVO>> getUserContents(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type) {
        List<ContentListVO> list = contentService.getUserContents(userId, type);
        return R.ok(list);
    }

    /**
     * 获取我的内容
     */
    @Operation(summary = "获取我的内容", description = "获取当前用户的内容列表")
    @GetMapping("/my")
    @SaCheckPermission("content:content:query")
    public R<List<ContentListVO>> getMyContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "内容状态")
            @RequestParam(required = false) Integer status) {
        List<ContentListVO> list = contentService.getMyContents(type, status);
        return R.ok(list);
    }

    /**
     * 获取内容统计
     */
    @Operation(summary = "获取内容统计", description = "获取内容统计数据")
    @GetMapping("/statistics")
    @SaCheckPermission("content:content:query")
    public R<Map<String, Object>> getContentStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(contentService.getContentStatistics(startDate, endDate));
    }

    /**
     * 获取内容类型统计
     */
    @Operation(summary = "获取内容类型统计", description = "获取各类型内容数量统计")
    @GetMapping("/type-statistics")
    @SaCheckPermission("content:content:query")
    public R<Map<String, Object>> getContentTypeStatistics() {
        return R.ok(contentService.getContentTypeStatistics());
    }

    /**
     * 批量发布内容
     */
    @Operation(summary = "批量发布内容", description = "批量发布多个内容")
    @PutMapping("/batch-publish")
    @SaCheckPermission("content:content:edit")
    @Log(title = "批量发布内容", businessType = BusinessType.UPDATE)
    public R<Void> batchPublishContents(
            @Parameter(description = "内容ID列表", required = true)
            @RequestBody List<Long> contentIds) {
        return contentService.batchPublishContents(contentIds) ? R.ok() : R.fail();
    }

    /**
     * 批量下架内容
     */
    @Operation(summary = "批量下架内容", description = "批量下架多个内容")
    @PutMapping("/batch-archive")
    @SaCheckPermission("content:content:edit")
    @Log(title = "批量下架内容", businessType = BusinessType.UPDATE)
    public R<Void> batchArchiveContents(
            @Parameter(description = "内容ID列表", required = true)
            @RequestBody List<Long> contentIds) {
        return contentService.batchArchiveContents(contentIds) ? R.ok() : R.fail();
    }

    /**
     * 查询附近的内容（v7.1新增 - 空间索引查询�?
     */
    @Operation(summary = "查询附近内容", description = "基于地理位置查询附近的内容（使用空间索引，性能提升10倍）")
    @GetMapping("/nearby")
    @SaCheckPermission("content:content:query")
    @Log(title = "附近内容查询", businessType = BusinessType.OTHER)
    public R<List<ContentListVO>> getNearbyContents(
            @Parameter(description = "经度", required = true)
            @RequestParam Double longitude,
            @Parameter(description = "纬度", required = true)
            @RequestParam Double latitude,
            @Parameter(description = "半径（米）")
            @RequestParam(defaultValue = "5000") Integer radius,
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "20") Integer limit) {
        List<ContentListVO> list = contentService.getNearbyContents(
                longitude, latitude, radius, type, limit);
        return R.ok(list);
    }

    /**
     * 查询城市内的内容（v7.1新增）
     */
    @Operation(summary = "查询城市内容", description = "查询指定城市的内容列表")
    @GetMapping("/city/{cityId}")
    @SaCheckPermission("content:content:query")
    public R<List<ContentListVO>> getContentsByCity(
            @Parameter(description = "城市ID", required = true)
            @PathVariable Long cityId,
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "50") Integer limit) {
        List<ContentListVO> list = contentService.getContentsByCity(cityId, type, limit);
        return R.ok(list);
    }
    
    // ========== APP专用接口（只需要登录，不需要特定权限） ==========
    
    /**
     * APP - 发布内容
     */
    @Operation(summary = "APP-发布内容", description = "用户发布新内容（需要登录）")
    @PostMapping("/app/publish")
    @cn.dev33.satoken.annotation.SaCheckLogin
    @Log(title = "APP发布内容", businessType = BusinessType.INSERT)
    public R<Long> appPublish(@Validated @RequestBody ContentAddDTO contentAddDTO) {
        try {
            // 调用service发布内容，返回内容ID
            Long contentId = contentService.insertContentReturnId(contentAddDTO);
            return R.ok("发布成功", contentId);
        } catch (Exception e) {
            return R.fail("发布失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 编辑内容
     */
    @Operation(summary = "APP-编辑内容", description = "编辑自己发布的内容（需要登录）")
    @PutMapping("/app/{contentId}")
    @cn.dev33.satoken.annotation.SaCheckLogin
    @Log(title = "APP编辑内容", businessType = BusinessType.UPDATE)
    public R<Void> appEdit(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId,
            @Validated @RequestBody ContentUpdateDTO contentUpdateDTO) {
        try {
            // 设置内容ID
            contentUpdateDTO.setId(contentId);
            // 调用service编辑内容（service层会验证是否为作者本人）
            boolean success = contentService.updateContentByUser(contentUpdateDTO);
            return success ? R.ok("编辑成功") : R.fail("编辑失败");
        } catch (Exception e) {
            return R.fail("编辑失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 删除内容
     */
    @Operation(summary = "APP-删除内容", description = "删除自己发布的内容（需要登录）")
    @DeleteMapping("/app/{contentId}")
    @cn.dev33.satoken.annotation.SaCheckLogin
    @Log(title = "APP删除内容", businessType = BusinessType.DELETE)
    public R<Void> appDelete(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        try {
            // 调用service删除内容（service层会验证是否为作者本人）
            boolean success = contentService.deleteContentByUser(contentId);
            return success ? R.ok("删除成功") : R.fail("删除失败");
        } catch (Exception e) {
            return R.fail("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 获取我的内容列表
     */
    @Operation(summary = "APP-我的内容", description = "获取当前用户的内容列表（需要登录）")
    @GetMapping("/app/my")
    @cn.dev33.satoken.annotation.SaCheckLogin
    public R<List<ContentListVO>> appGetMyContents(
            @Parameter(description = "内容类型")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "内容状态")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 不使用分页，直接返回列表（或在Service层实现分页）
            List<ContentListVO> list = contentService.getMyContents(type, status);
            return R.ok(list);
        } catch (Exception e) {
            return R.fail("查询失败：" + e.getMessage());
        }
    }
}
