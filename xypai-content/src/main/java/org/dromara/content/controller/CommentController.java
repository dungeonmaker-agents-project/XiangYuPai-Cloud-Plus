package org.dromara.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.dromara.content.domain.dto.CommentListQueryDTO;
import org.dromara.content.domain.dto.CommentPublishDTO;
import org.dromara.content.domain.vo.CommentListVO;
import org.dromara.content.service.ICommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理控制器
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController extends BaseController {

    private final ICommentService commentService;

    /**
     * 获取评论列表
     */
    @Operation(summary = "获取评论列表", description = "获取动态的评论列表,包含二级回复")
    @GetMapping("/comments/{feedId}")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<Page<CommentListVO>> getCommentList(
        @Parameter(description = "动态ID", required = true) @PathVariable Long feedId,
        @Valid CommentListQueryDTO queryDTO
    ) {
        queryDTO.setFeedId(feedId);
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        Page<CommentListVO> page = commentService.getCommentList(queryDTO, userId);
        return R.ok(page);
    }

    /**
     * 发布评论
     */
    @Operation(summary = "发布评论", description = "发布评论或回复")
    @PostMapping("/comment")
    @RateLimiter(count = 20, time = 60, limitType = LimitType.USER)
    public R<CommentListVO> publishComment(@Valid @RequestBody CommentPublishDTO publishDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        CommentListVO comment = commentService.publishComment(publishDTO, userId);
        return R.ok("评论成功", comment);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "删除自己的评论")
    @DeleteMapping("/comment/{commentId}")
    @RateLimiter(count = 20, time = 60, limitType = LimitType.USER)
    public R<Void> deleteComment(
        @Parameter(description = "评论ID", required = true) @PathVariable Long commentId
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        commentService.deleteComment(commentId, userId);
        return R.ok("删除成功");
    }

}
