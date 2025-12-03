package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.vo.SkillConfigVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 技能配置控制器
 * Skill Configuration Controller
 *
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * @author XiangYuPai
 * @since 2025-11-14
 * @updated 2025-12-02 - 匹配新UI文档
 */
@Tag(name = "Skill Configuration API", description = "技能配置接口")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillConfigController {

    // ============ 新版配置数据（匹配UI文档） ============

    /**
     * 技能列表 - 用于技能选择器网格
     * 按UI文档: 王者荣耀、英雄联盟、和平精英、荒野乱斗(线上) + 探店、私影、台球、K歌、喝酒、按摩(线下)
     */
    private static final List<SkillConfigVo.SkillItem> SKILLS = Arrays.asList(
        // 线上技能（游戏类）
        SkillConfigVo.SkillItem.builder().id("wzry").name("王者荣耀").icon("https://cdn.example.com/skills/wzry.png").type("online").category("游戏").build(),
        SkillConfigVo.SkillItem.builder().id("lol").name("英雄联盟").icon("https://cdn.example.com/skills/lol.png").type("online").category("游戏").build(),
        SkillConfigVo.SkillItem.builder().id("pubg").name("和平精英").icon("https://cdn.example.com/skills/pubg.png").type("online").category("游戏").build(),
        SkillConfigVo.SkillItem.builder().id("hyld").name("荒野乱斗").icon("https://cdn.example.com/skills/hyld.png").type("online").category("游戏").build(),
        // 线下技能（本地服务）
        SkillConfigVo.SkillItem.builder().id("tanding").name("探店").icon("https://cdn.example.com/skills/tanding.png").type("offline").category("生活").build(),
        SkillConfigVo.SkillItem.builder().id("siying").name("私影").icon("https://cdn.example.com/skills/siying.png").type("offline").category("生活").build(),
        SkillConfigVo.SkillItem.builder().id("taiqiu").name("台球").icon("https://cdn.example.com/skills/taiqiu.png").type("offline").category("运动").build(),
        SkillConfigVo.SkillItem.builder().id("kge").name("K歌").icon("https://cdn.example.com/skills/kge.png").type("offline").category("娱乐").build(),
        SkillConfigVo.SkillItem.builder().id("hejiu").name("喝酒").icon("https://cdn.example.com/skills/hejiu.png").type("offline").category("生活").build(),
        SkillConfigVo.SkillItem.builder().id("anmo").name("按摩").icon("https://cdn.example.com/skills/anmo.png").type("offline").category("服务").build()
    );

    /**
     * 段位选项配置
     */
    private static final SkillConfigVo.RankOptions RANK_OPTIONS;
    static {
        Map<String, List<String>> ranksBySkill = new LinkedHashMap<>();
        // 王者荣耀段位（按UI文档）
        ranksBySkill.put("wzry", Arrays.asList("永恒钻石", "至尊星耀", "最强王者", "非凡王者", "无双王者", "荣耀王者", "传奇王者"));
        // 英雄联盟段位
        ranksBySkill.put("lol", Arrays.asList("黄金", "铂金", "翡翠", "钻石", "超凡大师", "傲世宗师", "最强王者"));
        // 和平精英段位
        ranksBySkill.put("pubg", Arrays.asList("铂金", "钻石", "皇冠", "王牌", "无敌战神", "荣耀战神"));
        // 荒野乱斗段位
        ranksBySkill.put("hyld", Arrays.asList("黄金", "钻石", "神话", "传奇"));

        RANK_OPTIONS = SkillConfigVo.RankOptions.builder()
            .servers(Arrays.asList("QQ区", "微信区"))
            .ranksBySkill(ranksBySkill)
            .build();
    }

    /**
     * 时间选项配置
     */
    private static final SkillConfigVo.TimeOptions TIME_OPTIONS = SkillConfigVo.TimeOptions.builder()
        .startHour(0)
        .endHour(23)
        .intervalMinutes(30)
        .build();

    // ============ 旧版兼容数据 ============

    @SuppressWarnings("deprecation")
    private static final List<SkillConfigVo.GameInfo> GAMES = Arrays.asList(
        SkillConfigVo.GameInfo.builder().gameId("wzry").gameName("王者荣耀").ranks(Arrays.asList("青铜", "白银", "黄金", "铂金", "钻石", "星耀", "王者")).build(),
        SkillConfigVo.GameInfo.builder().gameId("hpjy").gameName("和平精英").ranks(Arrays.asList("青铜", "白银", "黄金", "铂金", "钻石", "皇冠", "王牌", "无敌战神")).build(),
        SkillConfigVo.GameInfo.builder().gameId("yxlm").gameName("英雄联盟").ranks(Arrays.asList("黑铁", "青铜", "白银", "黄金", "铂金", "钻石", "大师", "宗师", "王者")).build(),
        SkillConfigVo.GameInfo.builder().gameId("ys").gameName("原神").ranks(Arrays.asList("初学者", "熟练者", "精通者", "大师")).build()
    );

    @SuppressWarnings("deprecation")
    private static final List<SkillConfigVo.ServiceTypeInfo> SERVICE_TYPES = Arrays.asList(
        SkillConfigVo.ServiceTypeInfo.builder().typeId("tanding").typeName("探店").icon("https://cdn.example.com/skills/tanding.png").build(),
        SkillConfigVo.ServiceTypeInfo.builder().typeId("siying").typeName("私影").icon("https://cdn.example.com/skills/siying.png").build(),
        SkillConfigVo.ServiceTypeInfo.builder().typeId("taiqiu").typeName("台球").icon("https://cdn.example.com/skills/taiqiu.png").build(),
        SkillConfigVo.ServiceTypeInfo.builder().typeId("kge").typeName("K歌").icon("https://cdn.example.com/skills/kge.png").build(),
        SkillConfigVo.ServiceTypeInfo.builder().typeId("hejiu").typeName("喝酒").icon("https://cdn.example.com/skills/hejiu.png").build(),
        SkillConfigVo.ServiceTypeInfo.builder().typeId("anmo").typeName("按摩").icon("https://cdn.example.com/skills/anmo.png").build()
    );

    @Operation(summary = "Get skill configuration", description = "获取技能配置（技能列表、段位选项、时间选项）")
    @GetMapping("/config")
    @SuppressWarnings("deprecation")
    public R<SkillConfigVo> getSkillConfig() {
        SkillConfigVo config = SkillConfigVo.builder()
            // 新版字段
            .skills(SKILLS)
            .rankOptions(RANK_OPTIONS)
            .timeOptions(TIME_OPTIONS)
            // 旧版兼容字段
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
