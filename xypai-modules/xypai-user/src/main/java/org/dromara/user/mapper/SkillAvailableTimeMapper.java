package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.SkillAvailableTime;

import java.util.List;

/**
 * 技能可用时间Mapper
 * Skill Available Time Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface SkillAvailableTimeMapper extends BaseMapper<SkillAvailableTime> {

    /**
     * 查询技能的所有可用时间
     *
     * @param skillId 技能ID
     * @return 可用时间列表
     */
    @Select("SELECT * FROM skill_available_times WHERE skill_id = #{skillId} AND deleted = 0 ORDER BY day_of_week, start_time ASC")
    List<SkillAvailableTime> selectBySkillId(@Param("skillId") Long skillId);

    /**
     * 批量查询技能可用时间
     *
     * @param skillIds 技能ID列表
     * @return 可用时间列表
     */
    @Select("<script>"
        + "SELECT * FROM skill_available_times WHERE skill_id IN "
        + "<foreach collection='skillIds' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach>"
        + " AND deleted = 0 ORDER BY skill_id, day_of_week, start_time ASC"
        + "</script>")
    List<SkillAvailableTime> selectBatchBySkillIds(@Param("skillIds") List<Long> skillIds);
}
