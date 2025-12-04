package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.SkillRankConfig;

import java.util.List;

/**
 * 段位配置Mapper
 * Skill Rank Config Mapper
 *
 * 对应UI文档: 添加技能页_结构文档.md - RankPickerModal
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
@Mapper
public interface SkillRankConfigMapper extends BaseMapper<SkillRankConfig> {

    /**
     * 按技能配置ID查询段位列表
     *
     * @param skillConfigId 技能配置ID
     * @return 段位配置列表
     */
    @Select("SELECT * FROM skill_rank_config WHERE skill_config_id = #{skillConfigId} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillRankConfig> selectBySkillConfigId(@Param("skillConfigId") Long skillConfigId);

    /**
     * 按技能配置ID和服务区查询段位列表
     *
     * @param skillConfigId 技能配置ID
     * @param server        服务区: qq, weixin, default
     * @return 段位配置列表
     */
    @Select("SELECT * FROM skill_rank_config WHERE skill_config_id = #{skillConfigId} AND server = #{server} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillRankConfig> selectBySkillConfigIdAndServer(@Param("skillConfigId") Long skillConfigId, @Param("server") String server);

    /**
     * 查询所有启用的段位配置
     *
     * @return 段位配置列表
     */
    @Select("SELECT * FROM skill_rank_config WHERE status = 1 AND deleted = 0 ORDER BY skill_config_id, server, sort_order")
    List<SkillRankConfig> selectAllEnabled();

    /**
     * 查询所有服务区列表（去重）
     *
     * @return 服务区列表
     */
    @Select("SELECT DISTINCT server FROM skill_rank_config WHERE status = 1 AND deleted = 0")
    List<String> selectDistinctServers();
}
