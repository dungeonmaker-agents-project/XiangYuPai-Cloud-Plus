package com.xypai.content.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.content.domain.dto.ContentDraftSaveDTO;
import com.xypai.content.domain.vo.ContentDraftVO;
import com.xypai.content.service.IContentDraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容草稿控制�?
 * 
 * 支持功能�?
 * 1. 自动保存草稿（每10秒）
 * 2. 草稿列表查询
 * 3. 发布草稿
 * 4. 删除草稿
 *
 * @author David (内容服务�?
 * @date 2025-01-15
 */
@Tag(name = "内容草稿", description = "草稿自动保存API")
@RestController
@RequestMapping("/api/v1/drafts")
@RequiredArgsConstructor
@Validated
public class ContentDraftController extends BaseController {

    private final IContentDraftService contentDraftService;

    /**
     * 保存草稿（自动保存/手动保存）
     */
    @Operation(summary = "保存草稿", description = "自动保存或手动保存草稿")
    @PostMapping("/save")
    @SaCheckPermission("content:draft:add")
    @Log(title = "保存草稿", businessType = BusinessType.INSERT)
    public R<Long> saveDraft(@Validated @RequestBody ContentDraftSaveDTO draftSaveDTO) {
        Long draftId = contentDraftService.saveDraft(draftSaveDTO);
        return R.ok("保存草稿成功", draftId);
    }

    /**
     * 获取草稿详情
     */
    @Operation(summary = "获取草稿详情", description = "根据草稿ID获取详细信息")
    @GetMapping("/{draftId}")
    @SaCheckPermission("content:draft:query")
    public R<ContentDraftVO> getDraft(
            @Parameter(description = "草稿ID", required = true)
            @PathVariable Long draftId) {
        return R.ok(contentDraftService.getDraftById(draftId));
    }

    /**
     * 获取我的草稿列表
     */
    @Operation(summary = "获取草稿列表", description = "获取当前用户的草稿列表")
    @GetMapping("/my")
    @SaCheckPermission("content:draft:query")
    public R<List<ContentDraftVO>> getMyDrafts(
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(contentDraftService.getUserDrafts(limit));
    }

    /**
     * 删除草稿
     */
    @Operation(summary = "删除草稿", description = "删除指定草稿")
    @DeleteMapping("/{draftId}")
    @SaCheckPermission("content:draft:remove")
    @Log(title = "删除草稿", businessType = BusinessType.DELETE)
    public R<Void> deleteDraft(
            @Parameter(description = "草稿ID", required = true)
            @PathVariable Long draftId) {
        return contentDraftService.deleteDraft(draftId) ? R.ok() : R.fail();
    }

    /**
     * 发布草稿
     */
    @Operation(summary = "发布草稿", description = "将草稿发布为正式内容")
    @PostMapping("/{draftId}/publish")
    @SaCheckPermission("content:draft:publish")
    @Log(title = "发布草稿", businessType = BusinessType.INSERT)
    public R<Long> publishDraft(
            @Parameter(description = "草稿ID", required = true)
            @PathVariable Long draftId) {
        Long contentId = contentDraftService.publishDraft(draftId);
        return R.ok("发布成功", contentId);
    }

    /**
     * 统计草稿数量
     */
    @Operation(summary = "统计草稿数量", description = "统计当前用户的草稿数量")
    @GetMapping("/count")
    @SaCheckPermission("content:draft:query")
    public R<Long> countDrafts() {
        return R.ok(contentDraftService.countUserDrafts());
    }
}

