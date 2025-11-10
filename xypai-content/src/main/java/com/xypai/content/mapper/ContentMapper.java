package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 查询用户关注的内容列表
     *
     * @param userId 当前用户ID
     * @return 内容列表
     */
    List<Content> selectFollowingContents(@Param("userId") Long userId);

    /**
     * 查询热门内容列表
     *
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectPopularContents(@Param("limit") Integer limit);

    /**
     * 查询推荐内容列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectRecommendedContents(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 增加查看数
     *
     * @param contentId 内容ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("contentId") Long contentId);

    /**
     * 查询相关内容
     *
     * @param contentId 内容ID
     * @param type 内容类型
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectRelatedContents(@Param("contentId") Long contentId, 
                                       @Param("type") Integer type, 
                                       @Param("limit") Integer limit);

    /**
     * 查询附近的内容（空间索引查询 - v7.1新增）
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 半径（米）
     * @param type 内容类型（可选）
     * @param limit 限制数量
     * @return 内容列表（包含距离信息）
     */
    List<Content> selectNearbyContents(@Param("longitude") Double longitude,
                                       @Param("latitude") Double latitude,
                                       @Param("radius") Integer radius,
                                       @Param("type") Integer type,
                                       @Param("limit") Integer limit);

    /**
     * 查询城市内的内容（v7.1新增）
     *
     * @param cityId 城市ID
     * @param type 内容类型（可选）
     * @param limit 限制数量
     * @return 内容列表
     */
    List<Content> selectContentsByCity(@Param("cityId") Long cityId,
                                       @Param("type") Integer type,
                                       @Param("limit") Integer limit);

    /**
     * 批量更新用户冗余信息（v7.1新增）
     *
     * @param userId 用户ID
     * @param nickname 新昵称
     * @param avatar 新头像
     * @return 影响行数
     */
    int updateUserRedundantInfo(@Param("userId") Long userId,
                                @Param("nickname") String nickname,
                                @Param("avatar") String avatar);
}
