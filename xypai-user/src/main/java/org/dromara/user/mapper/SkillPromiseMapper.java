package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.SkillPromise;

import java.util.List;

/**
 * 技能承诺Mapper
 * Skill Promise Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface SkillPromiseMapper extends BaseMapper<SkillPromise> {

    /**
     * 查询技能的所有承诺
     *
     * @param skillId 技能ID
     * @return 承诺列表
     */
    @Select("SELECT * FROM skill_promises WHERE skill_id = #{skillId} AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillPromise> selectBySkillId(@Param("skillId") Long skillId);

    /**
     * 批量查询技能承诺
     *
     * @param skillIds 技能ID列表
     * @return 承诺列表
     */
    @Select("<script>"
        + "SELECT * FROM skill_promises WHERE skill_id IN "
        + "<foreach collection='skillIds' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach>"
        + " AND deleted = 0 ORDER BY skill_id, sort_order ASC"
        + "</script>")
    List<SkillPromise> selectBatchBySkillIds(@Param("skillIds") List<Long> skillIds);
}
