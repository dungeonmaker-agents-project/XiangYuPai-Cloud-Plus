package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.SkillImage;

import java.util.List;

/**
 * 技能图片Mapper
 * Skill Image Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface SkillImageMapper extends BaseMapper<SkillImage> {

    /**
     * 查询技能的所有图片
     *
     * @param skillId 技能ID
     * @return 图片列表
     */
    @Select("SELECT * FROM skill_images WHERE skill_id = #{skillId} AND deleted = 0 ORDER BY sort_order ASC")
    List<SkillImage> selectBySkillId(@Param("skillId") Long skillId);

    /**
     * 批量查询技能图片
     *
     * @param skillIds 技能ID列表
     * @return 图片列表
     */
    @Select("<script>"
        + "SELECT * FROM skill_images WHERE skill_id IN "
        + "<foreach collection='skillIds' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach>"
        + " AND deleted = 0 ORDER BY skill_id, sort_order ASC"
        + "</script>")
    List<SkillImage> selectBatchBySkillIds(@Param("skillIds") List<Long> skillIds);
}
