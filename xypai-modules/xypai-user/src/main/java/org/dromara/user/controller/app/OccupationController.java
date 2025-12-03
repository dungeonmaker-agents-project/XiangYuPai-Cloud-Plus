package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.UpdateOccupationsDto;
import org.dromara.user.service.IUserOccupationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户职业控制器（支持多职业）
 * User Occupation Controller
 *
 * 对应UI文档: 个人主页-编辑_结构文档.md
 * - 支持多职业标签选择
 * - 最多5个职业
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Slf4j
@Tag(name = "User Occupation API", description = "用户职业接口（多选）")
@RestController
@RequestMapping("/api/user/profile/occupations")
@RequiredArgsConstructor
public class OccupationController {

    private final IUserOccupationService occupationService;

    /**
     * 获取当前用户ID (支持从Header fallback)
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = LoginHelper.getUserId();

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

    @Operation(summary = "Get user occupations", description = "获取用户职业列表")
    @GetMapping
    public R<List<String>> getOccupations(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return occupationService.getUserOccupations(userId);
    }

    @Operation(summary = "Update user occupations", description = "更新用户职业列表（批量替换）")
    @PutMapping
    public R<Void> updateOccupations(@RequestBody @Validated UpdateOccupationsDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return occupationService.updateOccupations(userId, dto);
    }

    @Operation(summary = "Add occupation", description = "添加单个职业")
    @PostMapping("/{occupationName}")
    public R<Void> addOccupation(@PathVariable String occupationName, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return occupationService.addOccupation(userId, occupationName);
    }

    @Operation(summary = "Remove occupation", description = "删除单个职业")
    @DeleteMapping("/{occupationName}")
    public R<Void> removeOccupation(@PathVariable String occupationName, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return occupationService.removeOccupation(userId, occupationName);
    }
}
