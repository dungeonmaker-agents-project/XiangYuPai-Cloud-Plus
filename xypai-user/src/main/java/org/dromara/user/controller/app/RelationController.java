package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.UserBlockDto;
import org.dromara.user.domain.dto.UserReportDto;
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
@Tag(name = "User Relation API", description = "用户关系接口")
@RestController
@RequestMapping("/api/user/relation")
@RequiredArgsConstructor
public class RelationController {

    private final IRelationService relationService;

    @Operation(summary = "Follow user")
    @PostMapping("/follow/{followingId}")
    public R<Void> followUser(@PathVariable Long followingId) {
        Long userId = LoginHelper.getUserId();
        return relationService.followUser(userId, followingId);
    }

    @Operation(summary = "Unfollow user")
    @DeleteMapping("/follow/{followingId}")
    public R<Void> unfollowUser(@PathVariable Long followingId) {
        Long userId = LoginHelper.getUserId();
        return relationService.unfollowUser(userId, followingId);
    }

    @Operation(summary = "Get fans list")
    @GetMapping("/fans")
    public TableDataInfo<UserRelationVo> getFansList(
        @RequestParam(required = false) String keyword,
        PageQuery pageQuery
    ) {
        Long userId = LoginHelper.getUserId();
        return relationService.getFansList(userId, keyword, pageQuery);
    }

    @Operation(summary = "Get following list")
    @GetMapping("/following")
    public TableDataInfo<UserRelationVo> getFollowingList(
        @RequestParam(required = false) String keyword,
        PageQuery pageQuery
    ) {
        Long userId = LoginHelper.getUserId();
        return relationService.getFollowingList(userId, keyword, pageQuery);
    }

    @Operation(summary = "Block user")
    @PostMapping("/block")
    public R<Void> blockUser(@RequestBody @Validated UserBlockDto dto) {
        Long userId = LoginHelper.getUserId();
        return relationService.blockUser(userId, dto);
    }

    @Operation(summary = "Unblock user")
    @DeleteMapping("/block/{blockedUserId}")
    public R<Void> unblockUser(@PathVariable Long blockedUserId) {
        Long userId = LoginHelper.getUserId();
        return relationService.unblockUser(userId, blockedUserId);
    }

    @Operation(summary = "Report user")
    @PostMapping("/report")
    public R<Void> reportUser(@RequestBody @Validated UserReportDto dto) {
        Long userId = LoginHelper.getUserId();
        return relationService.reportUser(userId, dto);
    }
}
