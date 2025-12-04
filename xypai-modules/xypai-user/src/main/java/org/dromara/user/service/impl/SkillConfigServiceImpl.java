package org.dromara.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.user.domain.entity.SkillConfig;
import org.dromara.user.domain.entity.SkillRankConfig;
import org.dromara.user.domain.vo.SkillConfigVo;
import org.dromara.user.mapper.SkillConfigMapper;
import org.dromara.user.mapper.SkillRankConfigMapper;
import org.dromara.user.service.ISkillConfigService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 技能配置服务实现
 * Skill Config Service Implementation
 *
 * 从数据库加载技能配置数据
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
@Service
@RequiredArgsConstructor
public class SkillConfigServiceImpl implements ISkillConfigService {

    private final SkillConfigMapper skillConfigMapper;
    private final SkillRankConfigMapper skillRankConfigMapper;

    /**
     * 服务区显示名称映射
     */
    private static final Map<String, String> SERVER_DISPLAY_NAMES = Map.of(
        "qq", "QQ区",
        "weixin", "微信区",
        "default", "通用"
    );

    @Override
    public SkillConfigVo getSkillConfig() {
        // 1. 加载技能列表
        List<SkillConfigVo.SkillItem> skills = loadSkillItems();

        // 2. 加载段位选项
        SkillConfigVo.RankOptions rankOptions = loadRankOptions();

        // 3. 构建时间选项（固定配置）
        SkillConfigVo.TimeOptions timeOptions = SkillConfigVo.TimeOptions.builder()
            .startHour(0)
            .endHour(23)
            .intervalMinutes(30)
            .build();

        // 4. 构建旧版兼容数据
        List<SkillConfigVo.GameInfo> games = buildLegacyGames(skills);
        List<SkillConfigVo.ServiceTypeInfo> serviceTypes = buildLegacyServiceTypes(skills);

        return SkillConfigVo.builder()
            .skills(skills)
            .rankOptions(rankOptions)
            .timeOptions(timeOptions)
            .games(games)
            .serviceTypes(serviceTypes)
            .build();
    }

    @Override
    public boolean isValidSkillConfigId(Long skillConfigId) {
        if (skillConfigId == null) {
            return false;
        }
        SkillConfig config = skillConfigMapper.selectById(skillConfigId);
        return config != null && config.getStatus() == 1 && !config.getDeleted();
    }

    @Override
    public boolean isSkillTypeMatch(Long skillConfigId, String skillType) {
        if (skillConfigId == null || skillType == null) {
            return false;
        }
        SkillConfig config = skillConfigMapper.selectById(skillConfigId);
        return config != null && skillType.equals(config.getSkillType());
    }

    /**
     * 加载技能列表
     */
    private List<SkillConfigVo.SkillItem> loadSkillItems() {
        List<SkillConfig> configs = skillConfigMapper.selectAllEnabled();
        return configs.stream()
            .map(this::convertToSkillItem)
            .collect(Collectors.toList());
    }

    /**
     * 转换为技能项
     */
    private SkillConfigVo.SkillItem convertToSkillItem(SkillConfig config) {
        return SkillConfigVo.SkillItem.builder()
            .id(String.valueOf(config.getConfigId()))
            .name(config.getName())
            .icon(config.getIcon())
            .type(config.getSkillType())
            .category(config.getCategory())
            .build();
    }

    /**
     * 加载段位选项
     */
    private SkillConfigVo.RankOptions loadRankOptions() {
        // 加载所有段位配置
        List<SkillRankConfig> rankConfigs = skillRankConfigMapper.selectAllEnabled();

        // 获取服务区列表（去重并转换为显示名称）
        List<String> servers = rankConfigs.stream()
            .map(SkillRankConfig::getServer)
            .distinct()
            .filter(s -> !"default".equals(s)) // 默认服务区不显示在选择器中
            .map(s -> SERVER_DISPLAY_NAMES.getOrDefault(s, s))
            .collect(Collectors.toList());

        // 如果没有服务区数据，使用默认值
        if (servers.isEmpty()) {
            servers = Arrays.asList("QQ区", "微信区");
        }

        // 按技能ID分组段位
        Map<String, List<String>> ranksBySkill = new LinkedHashMap<>();
        Map<Long, List<SkillRankConfig>> groupedBySkill = rankConfigs.stream()
            .collect(Collectors.groupingBy(SkillRankConfig::getSkillConfigId));

        for (Map.Entry<Long, List<SkillRankConfig>> entry : groupedBySkill.entrySet()) {
            String skillId = String.valueOf(entry.getKey());
            List<String> ranks = entry.getValue().stream()
                .map(SkillRankConfig::getRankName)
                .distinct()
                .collect(Collectors.toList());
            ranksBySkill.put(skillId, ranks);
        }

        return SkillConfigVo.RankOptions.builder()
            .servers(servers)
            .ranksBySkill(ranksBySkill)
            .build();
    }

    /**
     * 构建旧版游戏列表（向后兼容）
     */
    @SuppressWarnings("deprecation")
    private List<SkillConfigVo.GameInfo> buildLegacyGames(List<SkillConfigVo.SkillItem> skills) {
        // 只提取线上技能（游戏类）
        List<SkillRankConfig> rankConfigs = skillRankConfigMapper.selectAllEnabled();
        Map<Long, List<SkillRankConfig>> ranksBySkill = rankConfigs.stream()
            .collect(Collectors.groupingBy(SkillRankConfig::getSkillConfigId));

        return skills.stream()
            .filter(s -> "online".equals(s.getType()))
            .map(skill -> {
                Long skillId = Long.parseLong(skill.getId());
                List<String> ranks = ranksBySkill.getOrDefault(skillId, Collections.emptyList())
                    .stream()
                    .map(SkillRankConfig::getRankName)
                    .distinct()
                    .collect(Collectors.toList());

                return SkillConfigVo.GameInfo.builder()
                    .gameId(skill.getId())
                    .gameName(skill.getName())
                    .ranks(ranks)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 构建旧版服务类型列表（向后兼容）
     */
    @SuppressWarnings("deprecation")
    private List<SkillConfigVo.ServiceTypeInfo> buildLegacyServiceTypes(List<SkillConfigVo.SkillItem> skills) {
        // 只提取线下技能
        return skills.stream()
            .filter(s -> "offline".equals(s.getType()))
            .map(skill -> SkillConfigVo.ServiceTypeInfo.builder()
                .typeId(skill.getId())
                .typeName(skill.getName())
                .icon(skill.getIcon())
                .build())
            .collect(Collectors.toList());
    }
}
