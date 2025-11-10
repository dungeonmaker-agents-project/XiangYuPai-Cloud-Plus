package com.xypai.trade.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.trade.domain.dto.ReviewCreateDTO;
import com.xypai.trade.domain.dto.ReviewReplyDTO;
import com.xypai.trade.domain.vo.ReviewDetailVO;
import com.xypai.trade.domain.vo.ReviewListVO;
import com.xypai.trade.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务评价控制器
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Tag(name = "服务评价", description = "服务评价管理API")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Validated
public class ServiceReviewController extends BaseController {

    private final IReviewService reviewService;

    /**
     * 创建评价
     */
    @Operation(summary = "创建评价", description = "订单完成后7天内可评价")
    @PostMapping
    @SaCheckPermission("trade:review:add")
    @Log(title = "创建评价", businessType = BusinessType.INSERT)
    public R<Long> createReview(@Validated @RequestBody ReviewCreateDTO reviewCreateDTO) {
        Long reviewId = reviewService.createReview(reviewCreateDTO);
        return R.ok("评价成功", reviewId);
    }

    /**
     * 商家回复评价
     */
    @Operation(summary = "商家回复评价", description = "被评价人（卖家）回复买家评价")
    @PostMapping("/reply")
    @SaCheckPermission("trade:review:reply")
    @Log(title = "商家回复评价", businessType = BusinessType.UPDATE)
    public R<Void> replyReview(@Validated @RequestBody ReviewReplyDTO reviewReplyDTO) {
        return reviewService.replyReview(reviewReplyDTO) ? R.ok("回复成功") : R.fail("回复失败");
    }

    /**
     * 获取评价详情
     */
    @Operation(summary = "获取评价详情", description = "根据评价ID获取详细信息")
    @GetMapping("/{reviewId}")
    @SaCheckPermission("trade:review:query")
    public R<ReviewDetailVO> getReviewInfo(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId) {
        return R.ok(reviewService.selectReviewById(reviewId));
    }

    /**
     * 查询订单的评价
     */
    @Operation(summary = "查询订单的评价", description = "根据订单ID查询评价信息")
    @GetMapping("/order/{orderId}")
    @SaCheckPermission("trade:review:query")
    public R<ReviewDetailVO> getOrderReview(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        ReviewDetailVO review = reviewService.selectReviewByOrderId(orderId);
        return review != null ? R.ok(review) : R.fail("该订单暂无评价");
    }

    /**
     * 查询内容的评价列表
     */
    @Operation(summary = "查询内容的评价列表", description = "查询某个服务/活动的所有评价")
    @GetMapping("/content/{contentId}")
    @SaCheckPermission("trade:review:query")
    public TableDataInfo<ReviewListVO> getContentReviews(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "状态筛选（1=已发布）")
            @RequestParam(required = false, defaultValue = "1") Integer status) {
        List<ReviewListVO> list = reviewService.selectContentReviews(contentId, status);
        return TableDataInfo.build(list);
    }

    /**
     * 查询用户收到的评价
     */
    @Operation(summary = "查询用户收到的评价", description = "查询某个卖家收到的所有评价")
    @GetMapping("/user/{userId}")
    @SaCheckPermission("trade:review:query")
    public TableDataInfo<ReviewListVO> getUserReviews(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "状态筛选（1=已发布）")
            @RequestParam(required = false, defaultValue = "1") Integer status) {
        List<ReviewListVO> list = reviewService.selectUserReviews(userId, status);
        return TableDataInfo.build(list);
    }

    /**
     * 查询我发表的评价
     */
    @Operation(summary = "查询我发表的评价", description = "查询当前用户发表的所有评价")
    @GetMapping("/my-reviews")
    @SaCheckPermission("trade:review:query")
    public TableDataInfo<ReviewListVO> getMyReviews() {
        List<ReviewListVO> list = reviewService.selectMyReviews();
        return TableDataInfo.build(list);
    }

    /**
     * 查询内容的评价统计
     */
    @Operation(summary = "查询内容的评价统计", description = "查询某个服务的平均评分、好评率等统计数据")
    @GetMapping("/content/{contentId}/stats")
    @SaCheckPermission("trade:review:query")
    public R<Map<String, Object>> getContentReviewStats(
            @Parameter(description = "内容ID", required = true)
            @PathVariable Long contentId) {
        Map<String, Object> stats = reviewService.getContentReviewStats(contentId);
        return R.ok(stats);
    }

    /**
     * 查询用户的评价统计
     */
    @Operation(summary = "查询用户的评价统计", description = "查询某个卖家的平均评分、好评率等统计数据")
    @GetMapping("/user/{userId}/stats")
    @SaCheckPermission("trade:review:query")
    public R<Map<String, Object>> getUserReviewStats(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        Map<String, Object> stats = reviewService.getUserReviewStats(userId);
        return R.ok(stats);
    }

    /**
     * 点赞评价
     */
    @Operation(summary = "点赞评价", description = "给评价点赞")
    @PostMapping("/{reviewId}/like")
    @SaCheckPermission("trade:review:like")
    @Log(title = "点赞评价", businessType = BusinessType.UPDATE)
    public R<Void> likeReview(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId) {
        return reviewService.likeReview(reviewId) ? R.ok("点赞成功") : R.fail("点赞失败");
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞", description = "取消评价点赞")
    @DeleteMapping("/{reviewId}/like")
    @SaCheckPermission("trade:review:like")
    @Log(title = "取消点赞", businessType = BusinessType.UPDATE)
    public R<Void> unlikeReview(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId) {
        return reviewService.unlikeReview(reviewId) ? R.ok("取消点赞成功") : R.fail("取消点赞失败");
    }

    /**
     * 隐藏评价（管理员）
     */
    @Operation(summary = "隐藏评价", description = "管理员隐藏不当评价")
    @PutMapping("/{reviewId}/hide")
    @SaCheckPermission("trade:review:admin")
    @Log(title = "隐藏评价", businessType = BusinessType.UPDATE)
    public R<Void> hideReview(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId) {
        return reviewService.hideReview(reviewId) ? R.ok("隐藏成功") : R.fail("隐藏失败");
    }

    /**
     * 删除评价
     */
    @Operation(summary = "删除评价", description = "删除自己的评价（已回复的不能删除）")
    @DeleteMapping("/{reviewId}")
    @SaCheckPermission("trade:review:remove")
    @Log(title = "删除评价", businessType = BusinessType.DELETE)
    public R<Void> deleteReview(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId) ? R.ok("删除成功") : R.fail("删除失败");
    }

    /**
     * 检查订单是否可以评价
     */
    @Operation(summary = "检查订单是否可以评价", description = "检查订单是否满足评价条件")
    @GetMapping("/check/{orderId}")
    @SaCheckPermission("trade:review:query")
    public R<Map<String, Object>> checkCanReview(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        boolean canReview = reviewService.canReview(orderId);
        boolean hasReviewed = reviewService.hasReviewed(orderId);
        
        Map<String, Object> result = Map.of(
                "canReview", canReview,
                "hasReviewed", hasReviewed,
                "message", hasReviewed ? "已评价" : (canReview ? "可以评价" : "不满足评价条件")
        );
        
        return R.ok(result);
    }
}

