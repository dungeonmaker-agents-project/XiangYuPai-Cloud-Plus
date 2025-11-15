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
import org.dromara.content.domain.dto.FeedListQueryDTO;
import org.dromara.content.domain.dto.FeedPublishDTO;
import org.dromara.content.domain.vo.FeedDetailVO;
import org.dromara.content.domain.vo.FeedListVO;
import org.dromara.content.service.IFeedService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 动态管理控制器
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "动态管理", description = "动态Feed流相关接口")
public class FeedController extends BaseController {

    private final IFeedService feedService;

    /**
     * 获取动态列表
     */
    @Operation(summary = "获取动态列表", description = "支持关注/热门/同城三种Tab")
    @GetMapping("/feed/{tabType}")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<Page<FeedListVO>> getFeedList(
        @Parameter(description = "Tab类型", required = true) @PathVariable String tabType,
        @Valid FeedListQueryDTO queryDTO
    ) {
        queryDTO.setTabType(tabType);
        Page<FeedListVO> page = feedService.getFeedList(queryDTO);
        return R.ok(page);
    }

    /**
     * 获取动态详情
     */
    @Operation(summary = "获取动态详情", description = "获取单个动态的完整信息")
    @GetMapping("/detail/{feedId}")
    @RateLimiter(count = 200, time = 60, limitType = LimitType.IP)
    public R<FeedDetailVO> getFeedDetail(
        @Parameter(description = "动态ID", required = true) @PathVariable Long feedId
    ) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        FeedDetailVO detail = feedService.getFeedDetail(feedId, userId);
        return R.ok(detail);
    }

    /**
     * 发布动态
     */
    @Operation(summary = "发布动态", description = "发布新动态,支持文字、图片、视频、话题、地点")
    @PostMapping("/publish")
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<Long> publishFeed(@Valid @RequestBody FeedPublishDTO publishDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        Long feedId = feedService.publishFeed(publishDTO, userId);
        return R.ok(feedId, "发布成功");
    }

    /**
     * 删除动态
     */
    @Operation(summary = "删除动态", description = "删除自己发布的动态")
    @DeleteMapping("/{feedId}")
    @RateLimiter(count = 20, time = 60, limitType = LimitType.USER)
    public R<Void> deleteFeed(
        @Parameter(description = "动态ID", required = true) @PathVariable Long feedId
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        feedService.deleteFeed(feedId, userId);
        return R.ok(null, "删除成功");
    }

}
