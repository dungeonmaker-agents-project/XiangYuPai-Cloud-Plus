package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.vo.UserProfileVo;
import org.dromara.user.domain.vo.UserRelationVo;
import org.dromara.user.service.IRelationService;
import org.dromara.user.service.IUserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口别名控制器（向后兼容）
 * User API Alias Controller (Backward Compatibility)
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Tag(name = "User API (Deprecated)", description = "用户接口别名（已废弃，请使用新接口）")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAliasController {

    private final IRelationService relationService;
    private final IUserService userService;

    @Operation(summary = "Get fans list", description = "已废弃：请使用 GET /api/user/relation/fans")
    @GetMapping("/fans")
    public TableDataInfo<UserRelationVo> getFansList(
        @RequestParam(required = false) String keyword,
        PageQuery pageQuery
    ) {
        Long userId = LoginHelper.getUserId();
        return relationService.getFansList(userId, keyword, pageQuery);
    }

    @Operation(summary = "Get following list", description = "已废弃：请使用 GET /api/user/relation/following")
    @GetMapping("/following")
    public TableDataInfo<UserRelationVo> getFollowingList(
        @RequestParam(required = false) String keyword,
        PageQuery pageQuery
    ) {
        Long userId = LoginHelper.getUserId();
        return relationService.getFollowingList(userId, keyword, pageQuery);
    }

    @Operation(summary = "Follow user", description = "已废弃：请使用 POST /api/user/relation/follow/{userId}")
    @PostMapping("/{userId}/follow")
    public R<Void> followUser(@PathVariable Long userId) {
        Long currentUserId = LoginHelper.getUserId();
        return relationService.followUser(currentUserId, userId);
    }

    @Operation(summary = "Unfollow user", description = "已废弃：请使用 DELETE /api/user/relation/follow/{userId}")
    @DeleteMapping("/{userId}/follow")
    public R<Void> unfollowUser(@PathVariable Long userId) {
        Long currentUserId = LoginHelper.getUserId();
        return relationService.unfollowUser(currentUserId, userId);
    }

    @Operation(summary = "Get user profile", description = "已废弃：请使用 GET /api/user/profile/other/{userId}")
    @GetMapping("/{userId}/profile")
    public R<UserProfileVo> getUserProfile(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }
}
