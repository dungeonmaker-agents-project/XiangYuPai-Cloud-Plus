package com.xypai.content.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.content.domain.dto.CommentAddDTO;
import com.xypai.content.domain.vo.CommentVO;
import com.xypai.content.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理控制�?
 * 
 * 核心功能�?
 * 1. 发表评论（一级评�?+ 二级回复�?
 * 2. 删除评论
 * 3. 评论点赞
 * 4. 评论列表查询
 *
 * @author David (内容服务�?
 * @date 2025-01-15
 */
@Tag(name = "评论管理", description = "评论发表与管理API")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Validated
public class CommentController extends BaseController {

    private final ICommentService commentService;

    /**
     * 发表评论
     */
    @Operation(summary = "发表评论", description = "发表一级评论或二级回复")
    @PostMapping
    @SaCheckPermission("content:comment:add")
    @Log(title = "发表评论", businessType = BusinessType.INSERT)
    public R<Long> addComment(@Validated @RequestBody CommentAddDTO commentAddDTO) {
        Long commentId = commentService.addComment(commentAddDTO);
        return R.ok("发表评论成功", commentId);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "删除自己的评论")
    @DeleteMapping("/{commentId}")
    @SaCheckPermission("content:comment:remove")
    @Log(title = "删除评论", businessType = BusinessType.DELETE)
    public R<Void> deleteComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        return commentService.deleteComment(commentId) ? R.ok() : R.fail();
    }

    /**
     * 获取内容的评论列表
     */
    @Operation(summary = "获取评论列表", description = "分页获取内容的评论列表（包含二级回复）")
    @GetMapping("/content/{contentId}")
    @SaCheckPermission("content:comment:query")
    public R<List<CommentVO>> getCommentList(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<CommentVO> list = commentService.getCommentList(contentId, pageNum, pageSize);
        return R.ok(list);
    }

    /**
     * 获取评论的所有回复
     */
    @Operation(summary = "获取评论回复", description = "获取一级评论的所有二级回复")
    @GetMapping("/{parentId}/replies")
    @SaCheckPermission("content:comment:query")
    public R<List<CommentVO>> getCommentReplies(
            @Parameter(description = "一级评论ID", required = true)
            @PathVariable Long parentId) {
        return R.ok(commentService.getCommentReplies(parentId));
    }

    /**
     * 评论点赞/取消点赞
     */
    @Operation(summary = "评论点赞", description = "点赞或取消点赞评论")
    @PostMapping("/{commentId}/like")
    @SaCheckPermission("content:comment:like")
    @Log(title = "评论点赞", businessType = BusinessType.UPDATE)
    public R<Boolean> likeComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        boolean liked = commentService.likeComment(commentId);
        return R.ok(liked ? "点赞成功" : "取消点赞", liked);
    }

    /**
     * 置顶/取消置顶评论（管理员功能）
     */
    @Operation(summary = "置顶评论", description = "置顶或取消置顶评论")
    @PutMapping("/{commentId}/top")
    @SaCheckPermission("content:comment:edit")
    @Log(title = "置顶评论", businessType = BusinessType.UPDATE)
    public R<Void> topComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "是否置顶", required = true)
            @RequestParam Boolean isTop) {
        return commentService.topComment(commentId, isTop) ? R.ok() : R.fail();
    }

    /**
     * 统计内容评论数
     */
    @Operation(summary = "统计评论数", description = "统计内容的评论总数")
    @GetMapping("/count/{contentId}")
    @SaCheckPermission("content:comment:query")
    public R<Long> countComments(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        return R.ok(commentService.countComments(contentId));
    }
    
    // ========== APP专用接口（简化权限验证） ==========
    
    /**
     * APP - 发表评论（需要登录）
     */
    @Operation(summary = "APP-发表评论", description = "发表评论或回复（需要登录）")
    @PostMapping("/app")
    @cn.dev33.satoken.annotation.SaCheckLogin
    @Log(title = "APP发表评论", businessType = BusinessType.INSERT)
    public R<Long> appAddComment(@Validated @RequestBody CommentAddDTO commentAddDTO) {
        try {
            Long commentId = commentService.addComment(commentAddDTO);
            return R.ok("发表成功", commentId);
        } catch (Exception e) {
            return R.fail("发表失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 删除评论（需要登录，只能删除自己的评论）
     */
    @Operation(summary = "APP-删除评论", description = "删除自己的评论（需要登录）")
    @DeleteMapping("/app/{commentId}")
    @cn.dev33.satoken.annotation.SaCheckLogin
    @Log(title = "APP删除评论", businessType = BusinessType.DELETE)
    public R<Void> appDeleteComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        try {
            boolean success = commentService.deleteComment(commentId);
            return success ? R.ok("删除成功") : R.fail("删除失败");
        } catch (Exception e) {
            return R.fail("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 获取评论列表（无需登录）
     */
    @Operation(summary = "APP-评论列表", description = "获取内容的评论列表（无需登录）")
    @GetMapping("/app/content/{contentId}")
    public R<List<CommentVO>> appGetCommentList(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<CommentVO> list = commentService.getCommentList(contentId, pageNum, pageSize);
            return R.ok(list != null ? list : java.util.Collections.emptyList());
        } catch (Exception e) {
            return R.fail("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 获取评论回复（无需登录）
     */
    @Operation(summary = "APP-评论回复", description = "获取评论的所有回复（无需登录）")
    @GetMapping("/app/{parentId}/replies")
    public R<List<CommentVO>> appGetCommentReplies(
            @Parameter(description = "一级评论ID", required = true)
            @PathVariable Long parentId) {
        try {
            List<CommentVO> list = commentService.getCommentReplies(parentId);
            return R.ok(list != null ? list : java.util.Collections.emptyList());
        } catch (Exception e) {
            return R.fail("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 评论点赞（需要登录）
     */
    @Operation(summary = "APP-评论点赞", description = "点赞或取消点赞评论（需要登录）")
    @PostMapping("/app/{commentId}/like")
    @cn.dev33.satoken.annotation.SaCheckLogin
    @Log(title = "APP评论点赞", businessType = BusinessType.UPDATE)
    public R<Boolean> appLikeComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        try {
            boolean liked = commentService.likeComment(commentId);
            return R.ok(liked ? "点赞成功" : "取消点赞", liked);
        } catch (Exception e) {
            return R.fail("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * APP - 统计评论数（无需登录）
     */
    @Operation(summary = "APP-评论数统计", description = "统计内容的评论总数（无需登录）")
    @GetMapping("/app/count/{contentId}")
    public R<Long> appCountComments(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        try {
            Long count = commentService.countComments(contentId);
            return R.ok(count != null ? count : 0L);
        } catch (Exception e) {
            return R.fail("查询失败：" + e.getMessage());
        }
    }
}

