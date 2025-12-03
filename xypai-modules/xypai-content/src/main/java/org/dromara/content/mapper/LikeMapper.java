package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 点赞Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {

    /**
     * 查询用户对指定目标的点赞状态
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetIds  目标ID列表
     * @return 已点赞的目标ID集合
     */
    @Select("<script>" +
            "SELECT target_id FROM `like` " +
            "WHERE user_id = #{userId} " +
            "AND target_type = #{targetType} " +
            "AND target_id IN " +
            "<foreach collection='targetIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    Set<Long> findLikedTargetIds(@Param("userId") Long userId,
                                  @Param("targetType") String targetType,
                                  @Param("targetIds") List<Long> targetIds);

}
