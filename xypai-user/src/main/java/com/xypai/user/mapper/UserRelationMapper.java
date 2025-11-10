package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.UserRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户关系Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelation> {

    /**
     * 统计用户关注数
     */
    Long countFollowing(@Param("userId") Long userId);

    /**
     * 统计用户粉丝数
     */
    Long countFollowers(@Param("userId") Long userId);

    /**
     * 检查用户关系是否存在
     */
    UserRelation selectRelation(@Param("userId") Long userId, 
                               @Param("targetId") Long targetId, 
                               @Param("type") Integer type);
}
