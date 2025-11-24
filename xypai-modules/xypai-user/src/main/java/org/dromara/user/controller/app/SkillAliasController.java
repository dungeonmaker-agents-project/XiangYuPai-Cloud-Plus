package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.SkillCreateDto;
import org.dromara.user.domain.dto.SkillUpdateDto;
import org.dromara.user.domain.vo.SkillDetailVo;
import org.dromara.user.domain.vo.SkillVo;
import org.dromara.user.service.ISkillService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 技能接口别名控制器（向后兼容）
 * Skill API Alias Controller (Backward Compatibility)
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Tag(name = "Skill API (Deprecated)", description = "技能接口别名（已废弃，请使用 /api/user/skills）")
@RestController
@RequestMapping("/api/user/skill")
@RequiredArgsConstructor
public class SkillAliasController {

    private final ISkillService skillService;

    @Operation(summary = "Create skill", description = "已废弃：请使用 POST /api/user/skills")
    @PostMapping
    public R<Long> createSkill(@RequestBody @Validated SkillCreateDto dto) {
        Long userId = LoginHelper.getUserId();
        return skillService.createSkill(userId, dto);
    }

    @Operation(summary = "Update skill", description = "已废弃：请使用 PUT /api/user/skills/{skillId}")
    @PutMapping("/{skillId}")
    public R<Void> updateSkill(
        @PathVariable Long skillId,
        @RequestBody @Validated SkillUpdateDto dto
    ) {
        Long userId = LoginHelper.getUserId();
        return skillService.updateSkill(userId, skillId, dto);
    }

    @Operation(summary = "Delete skill", description = "已废弃：请使用 DELETE /api/user/skills/{skillId}")
    @DeleteMapping("/{skillId}")
    public R<Void> deleteSkill(@PathVariable Long skillId) {
        Long userId = LoginHelper.getUserId();
        return skillService.deleteSkill(userId, skillId);
    }

    @Operation(summary = "Toggle skill online/offline", description = "已废弃：请使用 PUT /api/user/skills/{skillId}/toggle")
    @PutMapping("/{skillId}/toggle")
    public R<Void> toggleSkillOnline(
        @PathVariable Long skillId,
        @RequestParam Boolean isOnline
    ) {
        Long userId = LoginHelper.getUserId();
        return skillService.toggleSkillOnline(userId, skillId, isOnline);
    }

    @Operation(summary = "Get skill detail", description = "已废弃：请使用 GET /api/user/skills/{skillId}")
    @GetMapping("/{skillId}")
    public R<SkillDetailVo> getSkillDetail(@PathVariable Long skillId) {
        return skillService.getSkillDetail(skillId);
    }

    @Operation(summary = "Get my skills", description = "已废弃：请使用 GET /api/user/skills/my")
    @GetMapping("/my")
    public TableDataInfo<SkillVo> getMySkills(PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        return skillService.getMySkills(userId, pageQuery);
    }

    @Operation(summary = "Get user skills", description = "已废弃：请使用 GET /api/user/skills/user/{userId}")
    @GetMapping("/user/{userId}")
    public TableDataInfo<SkillVo> getUserSkills(
        @PathVariable Long userId,
        PageQuery pageQuery
    ) {
        return skillService.getUserSkills(userId, pageQuery);
    }

    @Operation(summary = "Search nearby skills", description = "已废弃：请使用 GET /api/user/skills/nearby")
    @GetMapping("/nearby")
    public TableDataInfo<SkillVo> searchNearbySkills(
        @RequestParam BigDecimal latitude,
        @RequestParam BigDecimal longitude,
        @RequestParam(defaultValue = "10000") Integer radiusMeters,
        PageQuery pageQuery
    ) {
        return skillService.searchNearbySkills(latitude, longitude, radiusMeters, pageQuery);
    }
}
