package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.SkillConfig;

import java.util.List;

/**
 * 技能配置Mapper
 * Skill Config Mapper
 *
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
@Mapper
public interface SkillConfigMapper extends BaseMapper<SkillConfig> {

    /**
     * 查询所有启用的技能配置
     *
     * @return 技能配置列表
     */
    @Select("SELECT * FROM skill_config WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillConfig> selectAllEnabled();

    /**
     * 按技能类型查询启用的技能配置
     *
     * @param skillType 技能类型: online=线上, offline=线下
     * @return 技能配置列表
     */
    @Select("SELECT * FROM skill_config WHERE skill_type = #{skillType} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillConfig> selectBySkillType(@Param("skillType") String skillType);

    /**
     * 按分类查询启用的技能配置
     *
     * @param category 分类（游戏、生活服务等）
     * @return 技能配置列表
     */
    @Select("SELECT * FROM skill_config WHERE category = #{category} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillConfig> selectByCategory(@Param("category") String category);
}
