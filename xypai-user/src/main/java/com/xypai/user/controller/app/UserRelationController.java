package com.xypai.user.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.user.domain.dto.UserRelationQueryDTO;
import com.xypai.user.domain.vo.UserRelationVO;
import com.xypai.user.service.IUserRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户关系控制�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "用户关系", description = "用户关系管理API")
@RestController
@RequestMapping("/api/v1/relations")
@RequiredArgsConstructor
@Validated
public class UserRelationController extends BaseController {

    private final IUserRelationService userRelationService;

    /**
     * 关注用户
     */
    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/follow/{targetUserId}")
    @SaCheckPermission("user:relation:add")
    @Log(title = "关注用户", businessType = BusinessType.INSERT)
    public R<Void> followUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return toAjax(userRelationService.followUser(targetUserId));
    }

    /**
     * 取消关注
     */
    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/follow/{targetUserId}")
    @SaCheckPermission("user:relation:remove")
    @Log(title = "取消关注", businessType = BusinessType.DELETE)
    public R<Void> unfollowUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return toAjax(userRelationService.unfollowUser(targetUserId));
    }

    /**
     * 拉黑用户
     */
    @Operation(summary = "拉黑用户", description = "拉黑指定用户")
    @PostMapping("/block/{targetUserId}")
    @SaCheckPermission("user:relation:add")
    @Log(title = "拉黑用户", businessType = BusinessType.INSERT)
    public R<Void> blockUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return toAjax(userRelationService.blockUser(targetUserId));
    }

    /**
     * 取消拉黑
     */
    @Operation(summary = "取消拉黑", description = "取消拉黑指定用户")
    @DeleteMapping("/block/{targetUserId}")
    @SaCheckPermission("user:relation:remove")
    @Log(title = "取消拉黑", businessType = BusinessType.DELETE)
    public R<Void> unblockUser(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return toAjax(userRelationService.unblockUser(targetUserId));
    }

    /**
     * 获取关注列表
     */
    @Operation(summary = "获取关注列表", description = "获取当前用户的关注列表")
    @GetMapping("/following")
    @SaCheckPermission("user:relation:query")
    public TableDataInfo<UserRelationVO> getFollowingList(UserRelationQueryDTO query) {
        List<UserRelationVO> list = userRelationService.getFollowingList(query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取粉丝列表
     */
    @Operation(summary = "获取粉丝列表", description = "获取当前用户的粉丝列表")
    @GetMapping("/followers")
    @SaCheckPermission("user:relation:query")
    public TableDataInfo<UserRelationVO> getFollowersList(UserRelationQueryDTO query) {
        List<UserRelationVO> list = userRelationService.getFollowersList(query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取拉黑列表
     */
    @Operation(summary = "获取拉黑列表", description = "获取当前用户的拉黑列表")
    @GetMapping("/blocked")
    @SaCheckPermission("user:relation:query")
    public TableDataInfo<UserRelationVO> getBlockedList(UserRelationQueryDTO query) {
        List<UserRelationVO> list = userRelationService.getBlockedList(query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取指定用户关注列表
     */
    @Operation(summary = "获取指定用户关注列表", description = "获取指定用户的关注列表")
    @GetMapping("/{userId}/following")
    @SaCheckPermission("user:relation:query")
    public TableDataInfo<UserRelationVO> getUserFollowingList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            UserRelationQueryDTO query) {
        List<UserRelationVO> list = userRelationService.getUserFollowingList(userId, query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取指定用户粉丝列表
     */
    @Operation(summary = "获取指定用户粉丝列表", description = "获取指定用户的粉丝列表")
    @GetMapping("/{userId}/followers")
    @SaCheckPermission("user:relation:query")
    public TableDataInfo<UserRelationVO> getUserFollowersList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            UserRelationQueryDTO query) {
        List<UserRelationVO> list = userRelationService.getUserFollowersList(userId, query);
        return TableDataInfo.build(list);
    }

    /**
     * 检查用户关�?
     */
    @Operation(summary = "检查用户关系", description = "检查与指定用户的关系状态")
    @GetMapping("/check/{targetUserId}")
    @SaCheckPermission("user:relation:query")
    public R<Map<String, Boolean>> checkUserRelation(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.ok(userRelationService.checkUserRelation(targetUserId));
    }

    /**
     * 获取关系统计
     */
    @Operation(summary = "获取关系统计", description = "获取用户关系统计数据")
    @GetMapping("/statistics")
    @SaCheckPermission("user:relation:query")
    public R<Map<String, Long>> getRelationStatistics() {
        return R.ok(userRelationService.getRelationStatistics());
    }

    /**
     * 获取指定用户关系统计
     */
    @Operation(summary = "获取指定用户关系统计", description = "获取指定用户的关系统计数据")
    @GetMapping("/{userId}/statistics")
    @SaCheckPermission("user:relation:query")
    public R<Map<String, Long>> getUserRelationStatistics(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return R.ok(userRelationService.getUserRelationStatistics(userId));
    }

    /**
     * 批量关注
     */
    @Operation(summary = "批量关注", description = "批量关注多个用户")
    @PostMapping("/batch-follow")
    @SaCheckPermission("user:relation:add")
    @Log(title = "批量关注", businessType = BusinessType.INSERT)
    public R<Void> batchFollowUsers(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        return toAjax(userRelationService.batchFollowUsers(userIds));
    }

    /**
     * 批量取消关注
     */
    @Operation(summary = "批量取消关注", description = "批量取消关注多个用户")
    @PostMapping("/batch-unfollow")
    @SaCheckPermission("user:relation:remove")
    @Log(title = "批量取消关注", businessType = BusinessType.DELETE)
    public R<Void> batchUnfollowUsers(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        return toAjax(userRelationService.batchUnfollowUsers(userIds));
    }
}
