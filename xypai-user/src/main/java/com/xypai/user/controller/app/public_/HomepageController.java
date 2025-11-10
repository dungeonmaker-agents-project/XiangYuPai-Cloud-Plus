package com.xypai.user.controller.app.public_;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.domain.vo.UserProfileVO;
import com.xypai.user.service.IUserService;
import com.xypai.user.service.IUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页公开接口控制器
 * 
 * 说明：此控制器的所有接口都是公开的，允许匿名访问（用于App首页展示）
 * 已在 SaTokenConfig 中配置白名单: /api/v1/homepage/**
 * 
 * @author xypai
 * @date 2025-10-24
 */
@Tag(name = "首页推荐", description = "首页公开接口（无需登录）")
@RestController
@RequestMapping("/api/v1/homepage")
@RequiredArgsConstructor
@Validated
public class HomepageController extends BaseController {

    private final IUserService userService;
    private final IUserProfileService userProfileService;

    /**
     * 获取精选用户
     * 
     * 说明：返回推荐的精选用户列表，用于首页展示（含完整资料）
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取精选用户", description = "获取首页推荐的精选用户列表（无需登录）")
    @GetMapping("/featured-users")
    public R<List<UserProfileVO>> getFeaturedUsers(
            @Parameter(description = "返回数量限制", example = "5")
            @RequestParam(defaultValue = "5") Integer limit) {
        
        // 创建查询条件：只查询启用状态的用户
        UserQueryDTO query = new UserQueryDTO();
        query.setStatus(1);  // 1=正常状态
        
        // 第一步：获取用户基础列表
        List<UserListVO> userList = userService.selectUserList(query);
        
        // 空列表保护
        if (userList == null || userList.isEmpty()) {
            return R.ok(new java.util.ArrayList<>());
        }
        
        // 如果返回数量超过limit，截取前N条
        if (userList.size() > limit) {
            userList = userList.subList(0, limit);
        }
        
        // 第二步：提取用户ID，批量查询完整资料
        List<Long> userIds = userList.stream()
            .map(UserListVO::getId)
            .collect(Collectors.toList());
        
        List<UserProfileVO> profileList = userProfileService.getBatchUserProfiles(userIds);
        
        // 防止返回null
        return R.ok(profileList != null ? profileList : new java.util.ArrayList<>());
    }

    /**
     * 获取附近的人
     * 
     * 说明：返回附近的用户列表（基于位置信息，含完整资料）
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取附近的人", description = "获取附近的用户列表（无需登录）")
    @GetMapping("/nearby-users")
    public R<List<UserProfileVO>> getNearbyUsers(
            @Parameter(description = "城市名称", example = "深圳")
            @RequestParam(required = false) String city,
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") Integer limit) {
        
        UserQueryDTO query = new UserQueryDTO();
        query.setStatus(1);  // 只查询启用状态的用户
        // TODO: 后续可以添加基于地理位置的筛选
        
        // 第一步：获取用户基础列表
        List<UserListVO> userList = userService.selectUserList(query);
        
        // 空列表保护
        if (userList == null || userList.isEmpty()) {
            return R.ok(new java.util.ArrayList<>());
        }
        
        if (userList.size() > limit) {
            userList = userList.subList(0, limit);
        }
        
        // 第二步：批量查询完整资料
        List<Long> userIds = userList.stream()
            .map(UserListVO::getId)
            .collect(Collectors.toList());
        
        List<UserProfileVO> profileList = userProfileService.getBatchUserProfiles(userIds);
        
        return R.ok(profileList != null ? profileList : new java.util.ArrayList<>());
    }

    /**
     * 获取推荐用户
     * 
     * 说明：返回系统推荐的用户列表（含完整资料）
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取推荐用户", description = "获取系统推荐的用户列表（无需登录）")
    @GetMapping("/recommended-users")
    public R<List<UserProfileVO>> getRecommendedUsers(
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        
        UserQueryDTO query = new UserQueryDTO();
        query.setStatus(1);
        
        // 第一步：获取用户基础列表
        List<UserListVO> userList = userService.selectUserList(query);
        
        // 空列表保护
        if (userList == null || userList.isEmpty()) {
            return R.ok(new java.util.ArrayList<>());
        }
        
        if (userList.size() > limit) {
            userList = userList.subList(0, limit);
        }
        
        // 第二步：批量查询完整资料
        List<Long> userIds = userList.stream()
            .map(UserListVO::getId)
            .collect(Collectors.toList());
        
        List<UserProfileVO> profileList = userProfileService.getBatchUserProfiles(userIds);
        
        return R.ok(profileList != null ? profileList : new java.util.ArrayList<>());
    }

    /**
     * 获取新用户
     * 
     * 说明：返回最新注册的用户列表（含完整资料）
     * 此接口允许匿名访问
     */
    @Operation(summary = "获取新用户", description = "获取最新注册的用户列表（无需登录）")
    @GetMapping("/new-users")
    public R<List<UserProfileVO>> getNewUsers(
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        
        UserQueryDTO query = new UserQueryDTO();
        query.setStatus(1);
        // TODO: 可以添加按注册时间倒序排列的逻辑
        
        // 第一步：获取用户基础列表
        List<UserListVO> userList = userService.selectUserList(query);
        
        // 空列表保护
        if (userList == null || userList.isEmpty()) {
            return R.ok(new java.util.ArrayList<>());
        }
        
        if (userList.size() > limit) {
            userList = userList.subList(0, limit);
        }
        
        // 第二步：批量查询完整资料
        List<Long> userIds = userList.stream()
            .map(UserListVO::getId)
            .collect(Collectors.toList());
        
        List<UserProfileVO> profileList = userProfileService.getBatchUserProfiles(userIds);
        
        return R.ok(profileList != null ? profileList : new java.util.ArrayList<>());
    }
}

