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
import org.dromara.content.domain.dto.InteractionDTO;
import org.dromara.content.domain.vo.InteractionResultVO;
import org.dromara.content.service.IInteractionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 互动管理控制器
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/interaction")
@RequiredArgsConstructor
@Tag(name = "互动管理", description = "点赞、收藏、分享相关接口")
public class InteractionController extends BaseController {

    private final IInteractionService interactionService;

    /**
     * 点赞/取消点赞
     */
    @Operation(summary = "点赞/取消点赞", description = "对动态或评论进行点赞/取消点赞操作")
    @PostMapping("/like")
    @RateLimiter(count = 50, time = 60, limitType = LimitType.USER)
    public R<InteractionResultVO> like(@Valid @RequestBody InteractionDTO interactionDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        InteractionResultVO result = interactionService.handleLike(interactionDTO, userId);
        return R.ok(result);
    }

    /**
     * 收藏/取消收藏
     */
    @Operation(summary = "收藏/取消收藏", description = "对动态进行收藏/取消收藏操作")
    @PostMapping("/collect")
    @RateLimiter(count = 50, time = 60, limitType = LimitType.USER)
    public R<InteractionResultVO> collect(@Valid @RequestBody InteractionDTO interactionDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        InteractionResultVO result = interactionService.handleCollect(interactionDTO, userId);
        return R.ok(result);
    }

    /**
     * 分享
     */
    @Operation(summary = "分享", description = "分享动态到不同渠道")
    @PostMapping("/share")
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<InteractionResultVO> share(@Valid @RequestBody InteractionDTO interactionDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        InteractionResultVO result = interactionService.handleShare(interactionDTO.getTargetId(), interactionDTO.getShareChannel(), userId);
        return R.ok(result, "分享成功");
    }

}
