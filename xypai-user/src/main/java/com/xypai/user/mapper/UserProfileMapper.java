package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.UserProfileNew;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户资料Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfileNew> {

    /**
     * 根据用户ID查询用户资料
     */
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    UserProfileNew selectByUserId(Long userId);

    /**
     * 批量查询用户资料
     */
    @Select("<script>" +
            "SELECT * FROM user_profile WHERE user_id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    List<UserProfileNew> selectByUserIds(List<Long> userIds);
}
