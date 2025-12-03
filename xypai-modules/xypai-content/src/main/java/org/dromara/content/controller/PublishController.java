package org.dromara.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.web.core.BaseController;
import org.dromara.content.domain.dto.FeedDraftDTO;
import org.dromara.content.domain.vo.FeedDraftVO;
import org.dromara.content.domain.vo.PublishConfigVO;
import org.dromara.content.domain.vo.TopicCategoryVO;
import org.dromara.content.service.IFeedDraftService;
import org.dromara.content.service.IPublishService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发布功能控制器
 * 提供发布配置、话题分类、草稿管理等接口
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/content/publish")
@RequiredArgsConstructor
@Tag(name = "发布功能", description = "发布配置、话题分类、草稿管理")
public class PublishController extends BaseController {

    private final IPublishService publishService;
    private final IFeedDraftService feedDraftService;

    /**
     * 获取发布配置
     */
    @Operation(summary = "获取发布配置", description = "获取发布动态时的各种限制配置")
    @GetMapping("/config")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<PublishConfigVO> getPublishConfig() {
        PublishConfigVO config = publishService.getPublishConfig();
        return R.ok(config);
    }

    /**
     * 获取话题分类列表
     */
    @Operation(summary = "获取话题分类列表", description = "获取所有话题分类及其下的热门话题")
    @GetMapping("/topic-categories")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<List<TopicCategoryVO>> getTopicCategories() {
        List<TopicCategoryVO> categories = publishService.getTopicCategories();
        return R.ok(categories);
    }

    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿", description = "保存或更新动态草稿")
    @PostMapping("/draft")
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<Long> saveDraft(@Valid @RequestBody FeedDraftDTO draftDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        Long draftId = feedDraftService.saveDraft(draftDTO, userId);
        return R.ok("保存成功", draftId);
    }

    /**
     * 获取用户草稿列表
     */
    @Operation(summary = "获取用户草稿列表", description = "获取当前用户的所有草稿")
    @GetMapping("/drafts")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.USER)
    public R<List<FeedDraftVO>> getUserDrafts() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        List<FeedDraftVO> drafts = feedDraftService.getUserDrafts(userId);
        return R.ok(drafts);
    }

    /**
     * 获取草稿详情
     */
    @Operation(summary = "获取草稿详情", description = "获取指定草稿的详细内容")
    @GetMapping("/draft/{draftId}")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.USER)
    public R<FeedDraftVO> getDraftDetail(
        @Parameter(description = "草稿ID", required = true) @PathVariable Long draftId
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        FeedDraftVO draft = feedDraftService.getDraftDetail(draftId, userId);
        return R.ok(draft);
    }

    /**
     * 删除草稿
     */
    @Operation(summary = "删除草稿", description = "删除指定的草稿")
    @DeleteMapping("/draft/{draftId}")
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<Void> deleteDraft(
        @Parameter(description = "草稿ID", required = true) @PathVariable Long draftId
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        feedDraftService.deleteDraft(draftId, userId);
        return R.ok("删除成功");
    }

}
