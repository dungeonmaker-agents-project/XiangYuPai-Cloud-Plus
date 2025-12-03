package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.dromara.user.domain.entity.UserOccupation;

import java.util.List;

/**
 * 用户职业Mapper（支持多职业）
 * User Occupation Mapper
 *
 * 对应UI文档: 个人主页-编辑_结构文档.md
 * - 支持多职业标签选择
 * - 最多5个职业
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Mapper
public interface UserOccupationMapper extends BaseMapper<UserOccupation> {

    /**
     * 查询用户的所有职业（按排序序号）
     *
     * @param userId 用户ID
     * @return 职业列表
     */
    @Select("SELECT * FROM user_occupations WHERE user_id = #{userId} AND deleted = 0 ORDER BY sort_order ASC")
    List<UserOccupation> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的职业名称列表
     *
     * @param userId 用户ID
     * @return 职业名称列表
     */
    @Select("SELECT occupation_name FROM user_occupations WHERE user_id = #{userId} AND deleted = 0 ORDER BY sort_order ASC")
    List<String> selectOccupationNamesByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的职业数量
     *
     * @param userId 用户ID
     * @return 职业数量
     */
    @Select("SELECT COUNT(*) FROM user_occupations WHERE user_id = #{userId} AND deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 物理删除用户的所有职业（用于重新设置）
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    @Delete("DELETE FROM user_occupations WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * 逻辑删除用户的所有职业
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Delete("UPDATE user_occupations SET deleted = 1, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int softDeleteByUserId(@Param("userId") Long userId);
}
