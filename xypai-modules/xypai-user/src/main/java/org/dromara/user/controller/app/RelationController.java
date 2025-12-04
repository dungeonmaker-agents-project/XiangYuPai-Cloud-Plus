package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.UserBlockDto;
import org.dromara.user.domain.dto.UserReportDto;
import org.dromara.user.domain.dto.BatchRelationStatusDto;
import org.dromara.user.domain.vo.UserRelationVo;
import org.dromara.user.service.IRelationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关系控制器
 * User Relation Controller
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Slf4j
@Tag(name = "User Relation API", description = "用户关系接口")
@RestController
@RequestMapping("/api/user/relation")
@RequiredArgsConstructor
public class RelationController {

    private final IRelationService relationService;

    /**
     * 获取当前用户ID (支持从Header fallback)
     * ⭐ 优先从Sa-Token获取，如果失败则从Gateway传递的Header读取
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 优先从LoginHelper获取
        Long userId = LoginHelper.getUserId();

        // Fallback: 从Header读取
        if (userId == null) {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdHeader);
                    log.debug("从Header提取userId: {}", userId);
                } catch (NumberFormatException e) {
                    log.error("Header中的userId格式错误: {}", userIdHeader);
                }
            }
        }

        return userId;
    }

    @Operation(summary = "Follow user")
    @PostMapping("/follow/{followingId}")
    public R<Void> followUser(@PathVariable Long followingId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return relationService.followUser(userId, followingId);
    }

    @Operation(summary = "Unfollow user")
    @DeleteMapping("/follow/{followingId}")
    public R<Void> unfollowUser(@PathVariable Long followingId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return relationService.unfollowUser(userId, followingId);
    }

    @Operation(summary = "Get fans list")
    @GetMapping("/fans")
    public TableDataInfo<UserRelationVo> getFansList(
        @RequestParam(required = false) String keyword,
        PageQuery pageQuery,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        return relationService.getFansList(userId, keyword, pageQuery);
    }

    @Operation(summary = "Get following list")
    @GetMapping("/following")
    public TableDataInfo<UserRelationVo> getFollowingList(
        @RequestParam(required = false) String keyword,
        PageQuery pageQuery,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        return relationService.getFollowingList(userId, keyword, pageQuery);
    }

    @Operation(summary = "Block user")
    @PostMapping("/block/{blockedUserId}")
    public R<Void> blockUser(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(blockedUserId);
        return relationService.blockUser(userId, dto);
    }

    @Operation(summary = "Unblock user")
    @DeleteMapping("/block/{blockedUserId}")
    public R<Void> unblockUser(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return relationService.unblockUser(userId, blockedUserId);
    }

    @Operation(summary = "Report user")
    @PostMapping("/report/{reportedUserId}")
    public R<Void> reportUser(@PathVariable Long reportedUserId, @RequestBody @Validated UserReportDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        dto.setReportedUserId(reportedUserId);
        return relationService.reportUser(userId, dto);
    }

    @Operation(summary = "Batch get relation status", description = "批量获取与多个用户的关系状态")
    @PostMapping("/batch-status")
    public R<java.util.Map<Long, String>> batchGetRelationStatus(
        @RequestBody @Validated BatchRelationStatusDto dto,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return R.ok(relationService.batchGetRelationStatus(userId, dto.getUserIds()));
    }
}
