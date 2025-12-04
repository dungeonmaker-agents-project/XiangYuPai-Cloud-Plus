package org.dromara.user.service;

import org.dromara.user.domain.vo.SkillConfigVo;

/**
 * 技能配置服务接口
 * Skill Config Service Interface
 *
 * 提供添加技能页面所需的配置数据
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
public interface ISkillConfigService {

    /**
     * 获取完整的技能配置
     * 包含技能列表、段位选项、时间选项
     *
     * @return 技能配置VO
     */
    SkillConfigVo getSkillConfig();

    /**
     * 校验技能配置ID是否有效
     *
     * @param skillConfigId 技能配置ID
     * @return 是否有效
     */
    boolean isValidSkillConfigId(Long skillConfigId);

    /**
     * 校验技能类型是否匹配
     *
     * @param skillConfigId 技能配置ID
     * @param skillType     技能类型: online/offline
     * @return 是否匹配
     */
    boolean isSkillTypeMatch(Long skillConfigId, String skillType);
}
