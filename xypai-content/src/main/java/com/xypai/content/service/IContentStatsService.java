package com.xypai.content.service;

import com.xypai.content.domain.entity.ContentStats;

import java.util.Map;

/**
 * 内容统计服务接口
 * 
 * Redis + MySQL双写架构：
 * 1. 写入：只写Redis，异步同步到MySQL
 * 2. 读取：优先Redis，不存在时回源MySQL
 * 3. 同步：定时任务每5分钟批量同步
 * 4. 修正：定时任务每10分钟修正脏数据
 *
 * @author Charlie (内容服务组)
 * @date 2025-01-15
 */
public interface IContentStatsService {

    /**
     * 获取内容统计数据
     *
     * @param contentId 内容ID
     * @return 统计数据
     */
    ContentStats getStats(Long contentId);

    /**
     * 增加浏览数
     *
     * @param contentId 内容ID
     */
    void incrementViewCount(Long contentId);

    /**
     * 增加点赞数
     *
     * @param contentId 内容ID
     * @param increment 增量（1=点赞，-1=取消）
     */
    void incrementLikeCount(Long contentId, Integer increment);

    /**
     * 增加评论数
     *
     * @param contentId 内容ID
     * @param increment 增量（1=新增，-1=删除）
     */
    void incrementCommentCount(Long contentId, Integer increment);

    /**
     * 增加分享数
     *
     * @param contentId 内容ID
     */
    void incrementShareCount(Long contentId);

    /**
     * 增加收藏数
     *
     * @param contentId 内容ID
     * @param increment 增量（1=收藏，-1=取消）
     */
    void incrementCollectCount(Long contentId, Integer increment);

    /**
     * 批量获取统计数据
     *
     * @param contentIds 内容ID列表
     * @return Map<内容ID, 统计数据>
     */
    Map<Long, ContentStats> batchGetStats(java.util.List<Long> contentIds);

    /**
     * 初始化内容统计
     *
     * @param contentId 内容ID
     */
    void initStats(Long contentId);

    /**
     * 同步统计数据到MySQL（定时任务调用）
     */
    void syncStatsToMysql();

    /**
     * 预热缓存（从MySQL加载到Redis）
     *
     * @param contentId 内容ID
     */
    void warmUpCache(Long contentId);

    /**
     * 清除缓存
     *
     * @param contentId 内容ID
     */
    void clearCache(Long contentId);
}

