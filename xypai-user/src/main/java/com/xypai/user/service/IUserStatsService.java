package com.xypai.user.service;

import com.xypai.user.domain.entity.UserStats;
import com.xypai.user.domain.vo.UserStatsVO;

import java.util.List;

/**
 * 用户统计服务接口
 *
 * @author Bob
 * @date 2025-01-14
 */
public interface IUserStatsService {

    /**
     * 根据用户ID查询统计信息
     * 优先从Redis读取，不存在时回源MySQL
     */
    UserStatsVO getUserStats(Long userId);

    /**
     * 批量查询用户统计
     */
    List<UserStatsVO> getBatchUserStats(List<Long> userIds);

    /**
     * 初始化用户统计（新用户注册时调用）
     */
    boolean initUserStats(Long userId);

    /**
     * 增加粉丝数
     */
    boolean incrementFollowerCount(Long userId);

    /**
     * 减少粉丝数
     */
    boolean decrementFollowerCount(Long userId);

    /**
     * 增加关注数
     */
    boolean incrementFollowingCount(Long userId);

    /**
     * 减少关注数
     */
    boolean decrementFollowingCount(Long userId);

    /**
     * 增加内容数
     */
    boolean incrementContentCount(Long userId);

    /**
     * 减少内容数
     */
    boolean decrementContentCount(Long userId);

    /**
     * 增加点赞数
     */
    boolean incrementLikeCount(Long userId, Integer count);

    /**
     * 增加收藏数
     */
    boolean incrementCollectCount(Long userId, Integer count);

    /**
     * 更新组局统计
     */
    boolean updateActivityStats(Long userId, boolean isSuccess);

    /**
     * 从Redis同步到MySQL（定时任务调用）
     */
    void syncStatsToMySQL();

    /**
     * 刷新用户统计缓存
     */
    boolean refreshCache(Long userId);

    /**
     * 查询人气用户
     */
    List<UserStatsVO> getPopularUsers(Integer limit);

    /**
     * 查询优质组局者
     */
    List<UserStatsVO> getQualityOrganizers(Integer limit);
}

