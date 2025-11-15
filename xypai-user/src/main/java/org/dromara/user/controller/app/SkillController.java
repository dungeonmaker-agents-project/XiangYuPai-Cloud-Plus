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
import org.dromara.user.domain.dto.OnlineSkillCreateDto;
import org.dromara.user.domain.dto.OfflineSkillCreateDto;
import org.dromara.user.domain.vo.SkillDetailVo;
import org.dromara.user.domain.vo.SkillVo;
import org.dromara.user.service.ISkillService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 技能控制器
 * Skill Controller
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Tag(name = "Skill API", description = "技能接口")
@RestController
@RequestMapping("/api/user/skills")
@RequiredArgsConstructor
public class SkillController {

    private final ISkillService skillService;

    @Operation(summary = "Create skill")
    @PostMapping
    public R<Long> createSkill(@RequestBody @Validated SkillCreateDto dto) {
        Long userId = LoginHelper.getUserId();
        return skillService.createSkill(userId, dto);
    }

    @Operation(summary = "Create online skill", description = "创建线上技能（陪玩类）")
    @PostMapping("/online")
    public R<Long> createOnlineSkill(@RequestBody @Validated OnlineSkillCreateDto dto) {
        Long userId = LoginHelper.getUserId();
        return skillService.createOnlineSkill(userId, dto);
    }

    @Operation(summary = "Create offline skill", description = "创建线下技能（服务类）")
    @PostMapping("/offline")
    public R<Long> createOfflineSkill(@RequestBody @Validated OfflineSkillCreateDto dto) {
        Long userId = LoginHelper.getUserId();
        return skillService.createOfflineSkill(userId, dto);
    }

    @Operation(summary = "Update skill")
    @PutMapping("/{skillId}")
    public R<Void> updateSkill(
        @PathVariable Long skillId,
        @RequestBody @Validated SkillUpdateDto dto
    ) {
        Long userId = LoginHelper.getUserId();
        return skillService.updateSkill(userId, skillId, dto);
    }

    @Operation(summary = "Delete skill")
    @DeleteMapping("/{skillId}")
    public R<Void> deleteSkill(@PathVariable Long skillId) {
        Long userId = LoginHelper.getUserId();
        return skillService.deleteSkill(userId, skillId);
    }

    @Operation(summary = "Toggle skill online/offline")
    @PutMapping("/{skillId}/toggle")
    public R<Void> toggleSkillOnline(
        @PathVariable Long skillId,
        @RequestParam Boolean isOnline
    ) {
        Long userId = LoginHelper.getUserId();
        return skillService.toggleSkillOnline(userId, skillId, isOnline);
    }

    @Operation(summary = "Get skill detail")
    @GetMapping("/{skillId}")
    public R<SkillDetailVo> getSkillDetail(@PathVariable Long skillId) {
        return skillService.getSkillDetail(skillId);
    }

    @Operation(summary = "Get my skills")
    @GetMapping("/my")
    public TableDataInfo<SkillVo> getMySkills(PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        return skillService.getMySkills(userId, pageQuery);
    }

    @Operation(summary = "Get user skills")
    @GetMapping("/user/{userId}")
    public TableDataInfo<SkillVo> getUserSkills(
        @PathVariable Long userId,
        PageQuery pageQuery
    ) {
        return skillService.getUserSkills(userId, pageQuery);
    }

    @Operation(summary = "Search nearby skills")
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
