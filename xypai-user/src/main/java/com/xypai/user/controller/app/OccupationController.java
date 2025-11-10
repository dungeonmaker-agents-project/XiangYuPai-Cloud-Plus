package com.xypai.user.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.user.domain.dto.UserOccupationUpdateDTO;
import com.xypai.user.domain.vo.OccupationDictVO;
import com.xypai.user.domain.vo.UserOccupationVO;
import com.xypai.user.service.IOccupationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职业标签控制�?
 *
 * @author Bob
 * @date 2025-01-14
 */
@Tag(name = "职业标签", description = "用户职业标签管理API")
@RestController
@RequestMapping("/api/v1/occupation")
@RequiredArgsConstructor
@Validated
public class OccupationController extends BaseController {

    private final IOccupationService occupationService;

    /**
     * 查询所有职业列�?
     */
    @Operation(summary = "查询所有职业", description = "获取所有启用的职业列表（按排序）")
    @GetMapping("/list")
    public R<List<OccupationDictVO>> listAllOccupations() {
        List<OccupationDictVO> occupations = occupationService.listAllOccupations();
        return R.ok(occupations);
    }

    /**
     * 根据分类查询职业
     */
    @Operation(summary = "根据分类查询职业", description = "查询指定分类下的所有职业")
    @GetMapping("/category/{category}")
    public R<List<OccupationDictVO>> listOccupationsByCategory(
            @Parameter(description = "职业分类", required = true)
            @PathVariable String category) {
        List<OccupationDictVO> occupations = occupationService.listOccupationsByCategory(category);
        return R.ok(occupations);
    }

    /**
     * 查询所有职业分�?
     */
    @Operation(summary = "查询所有分类", description = "获取所有职业分类列表")
    @GetMapping("/categories")
    public R<List<String>> listAllCategories() {
        List<String> categories = occupationService.listAllCategories();
        return R.ok(categories);
    }

    /**
     * 查询用户的职业标�?
     */
    @Operation(summary = "查询用户职业", description = "获取指定用户的所有职业标签")
    @GetMapping("/user/{userId}")
    @SaCheckPermission("user:occupation:query")
    public R<List<UserOccupationVO>> getUserOccupations(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        List<UserOccupationVO> occupations = occupationService.getUserOccupations(userId);
        return R.ok(occupations);
    }

    /**
     * 查询当前用户的职业标�?
     */
    @Operation(summary = "查询当前用户职业", description = "获取当前登录用户的所有职业标签")
    @GetMapping("/current")
    @SaCheckPermission("user:occupation:query")
    public R<List<UserOccupationVO>> getCurrentUserOccupations() {
        Long userId = LoginHelper.getUserId();
        List<UserOccupationVO> occupations = occupationService.getUserOccupations(userId);
        return R.ok(occupations);
    }

    /**
     * 更新用户职业标签
     */
    @Operation(summary = "更新用户职业", description = "批量更新用户的职业标签（最多5个）")
    @PutMapping("/user/{userId}")
    @SaCheckPermission("user:occupation:edit")
    @Log(title = "更新职业标签", businessType = BusinessType.UPDATE)
    public R<Void> updateUserOccupations(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Validated @RequestBody UserOccupationUpdateDTO updateDTO) {
        boolean result = occupationService.updateUserOccupations(userId, updateDTO);
        return result ? R.ok("更新职业标签成功") : R.fail("更新职业标签失败");
    }

    /**
     * 更新当前用户职业标签
     */
    @Operation(summary = "更新当前用户职业", description = "批量更新当前登录用户的职业标签")
    @PutMapping("/current")
    @SaCheckPermission("user:occupation:edit")
    @Log(title = "更新职业标签", businessType = BusinessType.UPDATE)
    public R<Void> updateCurrentUserOccupations(
            @Validated @RequestBody UserOccupationUpdateDTO updateDTO) {
        Long userId = LoginHelper.getUserId();
        boolean result = occupationService.updateUserOccupations(userId, updateDTO);
        return result ? R.ok("更新职业标签成功") : R.fail("更新职业标签失败");
    }

    /**
     * 添加单个职业标签
     */
    @Operation(summary = "添加职业标签", description = "为用户添加单个职业标签")
    @PostMapping("/user/{userId}/add")
    @SaCheckPermission("user:occupation:edit")
    @Log(title = "添加职业标签", businessType = BusinessType.INSERT)
    public R<Void> addUserOccupation(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "职业编码", required = true)
            @RequestParam String occupationCode) {
        boolean result = occupationService.addUserOccupation(userId, occupationCode);
        return result ? R.ok("添加职业标签成功") : R.fail("添加职业标签失败");
    }

    /**
     * 删除单个职业标签
     */
    @Operation(summary = "删除职业标签", description = "删除用户的单个职业标签")
    @DeleteMapping("/user/{userId}/remove")
    @SaCheckPermission("user:occupation:edit")
    @Log(title = "删除职业标签", businessType = BusinessType.DELETE)
    public R<Void> removeUserOccupation(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "职业编码", required = true)
            @RequestParam String occupationCode) {
        boolean result = occupationService.removeUserOccupation(userId, occupationCode);
        return result ? R.ok("删除职业标签成功") : R.fail("删除职业标签失败");
    }

    /**
     * 清空用户所有职业标�?
     */
    @Operation(summary = "清空职业标签", description = "删除用户的所有职业标签")
    @DeleteMapping("/user/{userId}/clear")
    @SaCheckPermission("user:occupation:edit")
    @Log(title = "清空职业标签", businessType = BusinessType.DELETE)
    public R<Void> clearUserOccupations(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean result = occupationService.clearUserOccupations(userId);
        return result ? R.ok("清空职业标签成功") : R.fail("清空职业标签失败");
    }

    /**
     * 检查用户是否有某个职业
     */
    @Operation(summary = "检查职业标签", description = "检查用户是否拥有指定职业标签")
    @GetMapping("/user/{userId}/has")
    public R<Boolean> hasOccupation(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "职业编码", required = true)
            @RequestParam String occupationCode) {
        boolean has = occupationService.hasOccupation(userId, occupationCode);
        return R.ok(has);
    }

    /**
     * 统计拥有某个职业的用户数�?
     */
    @Operation(summary = "统计职业用户数", description = "统计拥有指定职业的用户总数")
    @GetMapping("/{occupationCode}/count")
    public R<Integer> countUsersByOccupation(
            @Parameter(description = "职业编码", required = true)
            @PathVariable String occupationCode) {
        int count = occupationService.countUsersByOccupation(occupationCode);
        return R.ok(count);
    }

    /**
     * 查询拥有某个职业的用户列�?
     */
    @Operation(summary = "查询职业用户列表", description = "查询拥有指定职业的用户ID列表")
    @GetMapping("/{occupationCode}/users")
    @SaCheckPermission("user:occupation:query")
    public R<List<Long>> getUserIdsByOccupation(
            @Parameter(description = "职业编码", required = true)
            @PathVariable String occupationCode) {
        List<Long> userIds = occupationService.getUserIdsByOccupation(occupationCode);
        return R.ok(userIds);
    }
}

