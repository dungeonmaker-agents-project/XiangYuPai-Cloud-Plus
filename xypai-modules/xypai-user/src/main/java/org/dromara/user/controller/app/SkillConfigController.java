package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.vo.SkillConfigVo;
import org.dromara.user.service.ISkillConfigService;
import org.springframework.web.bind.annotation.*;

/**
 * 技能配置控制器
 * Skill Configuration Controller
 *
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * 图片上传使用 MediaController: POST /api/media/upload
 *
 * @author XiangYuPai
 * @since 2025-11-14
 * @updated 2025-12-03 - 改为数据库查询
 */
@Tag(name = "Skill Configuration API", description = "技能配置接口")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillConfigController {

    private final ISkillConfigService skillConfigService;

    /**
     * 获取技能配置
     * 包含技能列表、段位选项、时间选项
     *
     * @return 技能配置
     */
    @Operation(summary = "Get skill configuration", description = "获取技能配置（技能列表、段位选项、时间选项）")
    @GetMapping("/config")
    public R<SkillConfigVo> getSkillConfig() {
        SkillConfigVo config = skillConfigService.getSkillConfig();
        return R.ok(config);
    }
}
