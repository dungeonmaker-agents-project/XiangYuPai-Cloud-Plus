package org.dromara.appbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.DiscoverLikeDTO;
import org.dromara.appbff.domain.dto.DiscoverListQueryDTO;
import org.dromara.appbff.domain.vo.DiscoverLikeResultVO;
import org.dromara.appbff.domain.vo.DiscoverListResultVO;
import org.dromara.appbff.service.DiscoverService;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 发现页控制器
 * 提供发现页内容列表和互动功能的API
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/discover")
@RequiredArgsConstructor
@Tag(name = "发现页", description = "发现页内容列表和互动功能")
public class DiscoverController {

    private final DiscoverService discoverService;

    /**
     * 获取发现页内容列表
     * 支持三种Tab: follow(关注), hot(热门), nearby(同城)
     */
    @GetMapping("/list")
    @Operation(summary = "获取发现列表", description = "支持关注/热门/同城三个Tab切换")
    public R<DiscoverListResultVO> getDiscoverList(@Valid DiscoverListQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        log.info("获取发现列表: tab={}, pageNum={}, pageSize={}, userId={}",
            queryDTO.getTab(), queryDTO.getPageNum(), queryDTO.getPageSize(), userId);

        DiscoverListResultVO result = discoverService.queryDiscoverList(queryDTO, userId);
        return R.ok(result);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    @Operation(summary = "点赞/取消点赞", description = "action: like(点赞), unlike(取消点赞)")
    public R<DiscoverLikeResultVO> toggleLike(@Valid @RequestBody DiscoverLikeDTO likeDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        log.info("点赞操作: contentId={}, action={}, userId={}", likeDTO.getContentId(), likeDTO.getAction(), userId);

        DiscoverLikeResultVO result = discoverService.toggleLike(likeDTO, userId);
        if (!result.getSuccess()) {
            return R.fail("操作失败");
        }
        return R.ok(result);
    }

    /**
     * 获取当前登录用户ID
     * 优先从Gateway传递的X-User-Id请求头获取
     * 如果未登录返回null（某些接口允许未登录访问）
     */
    private Long getCurrentUserId() {
        // 优先从Gateway传递的请求头获取用户ID
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    log.debug("从请求头X-User-Id获取到用户ID: {}", userIdHeader);
                    return Long.parseLong(userIdHeader);
                }
            }
        } catch (Exception e) {
            log.debug("从请求头获取用户ID失败: {}", e.getMessage());
        }

        // 回退：尝试从Sa-Token获取
        try {
            Long userId = LoginHelper.getUserId();
            if (userId != null) {
                log.debug("从Sa-Token获取到用户ID: {}", userId);
                return userId;
            }
        } catch (Exception e) {
            log.debug("从Sa-Token获取用户ID失败: {}", e.getMessage());
        }

        log.debug("未能获取到用户ID，用户可能未登录");
        return null;
    }
}
