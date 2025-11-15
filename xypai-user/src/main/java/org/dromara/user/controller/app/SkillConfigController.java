package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.vo.SkillConfigVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 技能配置控制器
 * Skill Configuration Controller
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Tag(name = "Skill Configuration API", description = "技能配置接口")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillConfigController {

    // TODO: 将来可以从数据库或配置文件读取
    private static final List<SkillConfigVo.GameInfo> GAMES = Arrays.asList(
        SkillConfigVo.GameInfo.builder()
            .gameId("wzry")
            .gameName("王者荣耀")
            .ranks(Arrays.asList("青铜", "白银", "黄金", "铂金", "钻石", "星耀", "王者"))
            .build(),
        SkillConfigVo.GameInfo.builder()
            .gameId("hpjy")
            .gameName("和平精英")
            .ranks(Arrays.asList("青铜", "白银", "黄金", "铂金", "钻石", "皇冠", "王牌", "无敌战神"))
            .build(),
        SkillConfigVo.GameInfo.builder()
            .gameId("yxlm")
            .gameName("英雄联盟")
            .ranks(Arrays.asList("黑铁", "青铜", "白银", "黄金", "铂金", "钻石", "大师", "宗师", "王者"))
            .build(),
        SkillConfigVo.GameInfo.builder()
            .gameId("ys")
            .gameName("原神")
            .ranks(Arrays.asList("初学者", "熟练者", "精通者", "大师"))
            .build()
    );

    private static final List<SkillConfigVo.ServiceTypeInfo> SERVICE_TYPES = Arrays.asList(
        SkillConfigVo.ServiceTypeInfo.builder()
            .typeId("cleaning")
            .typeName("家政服务")
            .icon("https://cdn.example.com/icons/cleaning.png")
            .build(),
        SkillConfigVo.ServiceTypeInfo.builder()
            .typeId("repair")
            .typeName("维修服务")
            .icon("https://cdn.example.com/icons/repair.png")
            .build(),
        SkillConfigVo.ServiceTypeInfo.builder()
            .typeId("photography")
            .typeName("摄影服务")
            .icon("https://cdn.example.com/icons/photography.png")
            .build(),
        SkillConfigVo.ServiceTypeInfo.builder()
            .typeId("tutoring")
            .typeName("教学辅导")
            .icon("https://cdn.example.com/icons/tutoring.png")
            .build(),
        SkillConfigVo.ServiceTypeInfo.builder()
            .typeId("fitness")
            .typeName("健身教练")
            .icon("https://cdn.example.com/icons/fitness.png")
            .build(),
        SkillConfigVo.ServiceTypeInfo.builder()
            .typeId("beauty")
            .typeName("美容美发")
            .icon("https://cdn.example.com/icons/beauty.png")
            .build()
    );

    @Operation(summary = "Get skill configuration", description = "获取技能配置（游戏列表、服务类型）")
    @GetMapping("/config")
    public R<SkillConfigVo> getSkillConfig() {
        SkillConfigVo config = SkillConfigVo.builder()
            .games(GAMES)
            .serviceTypes(SERVICE_TYPES)
            .build();

        return R.ok(config);
    }

    @Operation(summary = "Upload skill image", description = "上传技能展示图片")
    @PostMapping("/images/upload")
    public R<String> uploadSkillImage(@RequestParam("image") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            return R.fail("图片不能为空");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return R.fail("图片大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
            return R.fail("仅支持JPG/PNG格式");
        }

        // TODO: 实现OSS上传
        // String imageUrl = ossService.upload(file, "skills/");

        // 临时返回模拟URL
        String imageUrl = "https://cdn.example.com/skills/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        return R.ok(imageUrl);
    }
}
