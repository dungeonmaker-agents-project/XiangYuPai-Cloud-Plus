package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.ContentStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容统计Mapper接口
 *
 * @author Charlie (内容服务组)
 * @date 2025-01-15
 */
@Mapper
public interface ContentStatsMapper extends BaseMapper<ContentStats> {

    /**
     * 增加浏览数
     *
     * @param contentId 内容ID
     * @param increment 增量（默认1）
     * @return 影响行数
     */
    int incrementViewCount(@Param("contentId") Long contentId, 
                          @Param("increment") Integer increment);

    /**
     * 增加点赞数
     *
     * @param contentId 内容ID
     * @param increment 增量（1=点赞，-1=取消点赞）
     * @return 影响行数
     */
    int incrementLikeCount(@Param("contentId") Long contentId, 
                          @Param("increment") Integer increment);

    /**
     * 增加评论数
     *
     * @param contentId 内容ID
     * @param increment 增量（1=新增，-1=删除）
     * @return 影响行数
     */
    int incrementCommentCount(@Param("contentId") Long contentId, 
                             @Param("increment") Integer increment);

    /**
     * 增加分享数
     *
     * @param contentId 内容ID
     * @param increment 增量
     * @return 影响行数
     */
    int incrementShareCount(@Param("contentId") Long contentId, 
                           @Param("increment") Integer increment);

    /**
     * 增加收藏数
     *
     * @param contentId 内容ID
     * @param increment 增量（1=收藏，-1=取消收藏）
     * @return 影响行数
     */
    int incrementCollectCount(@Param("contentId") Long contentId, 
                             @Param("increment") Integer increment);

    /**
     * 批量更新统计数据（从Redis同步）
     *
     * @param statsList 统计数据列表
     * @return 影响行数
     */
    int batchUpdateStats(@Param("list") List<ContentStats> statsList);

    /**
     * 初始化内容统计记录
     *
     * @param contentId 内容ID
     * @return 影响行数
     */
    int initStats(@Param("contentId") Long contentId);
}

