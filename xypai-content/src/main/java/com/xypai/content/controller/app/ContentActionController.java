package com.xypai.content.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xypai.content.domain.dto.ContentActionDTO;
import com.xypai.content.service.IContentActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 内容互动控制器（点赞、收藏、分享）
 *
 * @author xypai
 * @date 2025-01-20
 */
@Slf4j
@Tag(name = "内容互动", description = "内容点赞、收藏、分享API（需要登录）")
@RestController
@RequestMapping("/api/v1/actions")
@RequiredArgsConstructor
@Validated
public class ContentActionController extends BaseController {

    private final IContentActionService contentActionService;

    /**
     * 点赞/取消点赞
     */
    @Operation(summary = "点赞/取消点赞", description = "对内容进行点赞或取消点赞操作")
    @PostMapping("/like")
    @SaCheckLogin
    @Log(title = "内容点赞", businessType = BusinessType.UPDATE)
    public R<Void> like(@Validated @RequestBody ContentActionDTO actionDTO) {
        try {
            boolean success = contentActionService.toggleLike(actionDTO.getContentId());
            return success ? R.ok("操作成功") : R.fail("操作失败");
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return R.fail("操作失败：" + e.getMessage());
        }
    }

    /**
     * 收藏/取消收藏
     */
    @Operation(summary = "收藏/取消收藏", description = "对内容进行收藏或取消收藏操作")
    @PostMapping("/collect")
    @SaCheckLogin
    @Log(title = "内容收藏", businessType = BusinessType.UPDATE)
    public R<Void> collect(@Validated @RequestBody ContentActionDTO actionDTO) {
        try {
            boolean success = contentActionService.toggleCollect(actionDTO.getContentId());
            return success ? R.ok("操作成功") : R.fail("操作失败");
        } catch (Exception e) {
            log.error("收藏操作失败", e);
            return R.fail("操作失败：" + e.getMessage());
        }
    }

    /**
     * 分享
     */
    @Operation(summary = "分享内容", description = "分享内容（记录分享次数）")
    @PostMapping("/share")
    @SaCheckLogin
    @Log(title = "内容分享", businessType = BusinessType.UPDATE)
    public R<Void> share(@Validated @RequestBody ContentActionDTO actionDTO) {
        try {
            boolean success = contentActionService.recordShare(actionDTO.getContentId());
            return success ? R.ok("分享成功") : R.fail("分享失败");
        } catch (Exception e) {
            log.error("分享操作失败", e);
            return R.fail("分享失败：" + e.getMessage());
        }
    }

    /**
     * 检查点赞状态
     */
    @Operation(summary = "检查点赞状态", description = "检查当前用户是否点赞了指定内容")
    @GetMapping("/like/status/{contentId}")
    @SaCheckLogin
    public R<Boolean> checkLikeStatus(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        try {
            boolean isLiked = contentActionService.checkLikeStatus(contentId);
            return R.ok(isLiked);
        } catch (Exception e) {
            log.error("检查点赞状态失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 检查收藏状态
     */
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否收藏了指定内容")
    @GetMapping("/collect/status/{contentId}")
    @SaCheckLogin
    public R<Boolean> checkCollectStatus(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        try {
            boolean isCollected = contentActionService.checkCollectStatus(contentId);
            return R.ok(isCollected);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 批量检查点赞状态
     */
    @Operation(summary = "批量检查点赞状态", description = "批量检查多个内容的点赞状态")
    @PostMapping("/like/batch-status")
    @SaCheckLogin
    public R<java.util.Map<Long, Boolean>> batchCheckLikeStatus(
            @Parameter(description = "内容ID列表", required = true)
            @RequestBody java.util.List<Long> contentIds) {
        try {
            java.util.Map<Long, Boolean> statusMap = contentActionService.batchCheckLikeStatus(contentIds);
            return R.ok(statusMap);
        } catch (Exception e) {
            log.error("批量检查点赞状态失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 批量检查收藏状态
     */
    @Operation(summary = "批量检查收藏状态", description = "批量检查多个内容的收藏状态")
    @PostMapping("/collect/batch-status")
    @SaCheckLogin
    public R<java.util.Map<Long, Boolean>> batchCheckCollectStatus(
            @Parameter(description = "内容ID列表", required = true)
            @RequestBody java.util.List<Long> contentIds) {
        try {
            java.util.Map<Long, Boolean> statusMap = contentActionService.batchCheckCollectStatus(contentIds);
            return R.ok(statusMap);
        } catch (Exception e) {
            log.error("批量检查收藏状态失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的点赞列表
     */
    @Operation(summary = "我的点赞", description = "获取当前用户点赞的内容列表")
    @GetMapping("/like/my")
    @SaCheckLogin
    public R<java.util.List<com.xypai.content.domain.vo.ContentListVO>> getMyLikes(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 不使用分页，直接返回列表（或在Service层实现分页）
            java.util.List<com.xypai.content.domain.vo.ContentListVO> list = contentActionService.getMyLikes();
            return R.ok(list);
        } catch (Exception e) {
            log.error("获取我的点赞列表失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的收藏列表
     */
    @Operation(summary = "我的收藏", description = "获取当前用户收藏的内容列表")
    @GetMapping("/collect/my")
    @SaCheckLogin
    public R<java.util.List<com.xypai.content.domain.vo.ContentListVO>> getMyCollects(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 不使用分页，直接返回列表（或在Service层实现分页）
            java.util.List<com.xypai.content.domain.vo.ContentListVO> list = contentActionService.getMyCollects();
            return R.ok(list);
        } catch (Exception e) {
            log.error("获取我的收藏列表失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }
}
