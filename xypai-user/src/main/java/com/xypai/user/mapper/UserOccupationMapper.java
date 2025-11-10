package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.UserOccupation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户职业关联Mapper接口
 *
 * @author Bob
 * @date 2025-01-14
 */
@Mapper
public interface UserOccupationMapper extends BaseMapper<UserOccupation> {

    /**
     * 查询用户的所有职业（按排序）
     */
    @Select("SELECT * FROM user_occupation WHERE user_id = #{userId} " +
            "ORDER BY sort_order ASC, created_at ASC")
    List<UserOccupation> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的职业编码列表
     */
    @Select("SELECT occupation_code FROM user_occupation WHERE user_id = #{userId} " +
            "ORDER BY sort_order ASC")
    List<String> selectOccupationCodesByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有职业
     */
    @Delete("DELETE FROM user_occupation WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 查询拥有某个职业的用户ID列表
     */
    @Select("SELECT user_id FROM user_occupation WHERE occupation_code = #{occupationCode}")
    List<Long> selectUserIdsByOccupationCode(@Param("occupationCode") String occupationCode);

    /**
     * 统计用户职业数量
     */
    @Select("SELECT COUNT(*) FROM user_occupation WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已有该职业
     */
    @Select("SELECT COUNT(*) > 0 FROM user_occupation " +
            "WHERE user_id = #{userId} AND occupation_code = #{occupationCode}")
    boolean existsByUserIdAndCode(@Param("userId") Long userId, 
                                   @Param("occupationCode") String occupationCode);

    /**
     * 批量插入用户职业
     */
    @Select("<script>" +
            "INSERT INTO user_occupation (user_id, occupation_code, sort_order) VALUES " +
            "<foreach collection='occupations' item='item' separator=','>" +
            "(#{item.userId}, #{item.occupationCode}, #{item.sortOrder})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("occupations") List<UserOccupation> occupations);
}

