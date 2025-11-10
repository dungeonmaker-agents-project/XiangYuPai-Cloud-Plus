package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import com.xypai.user.domain.entity.UserStats;
import com.xypai.user.domain.vo.UserStatsVO;
import com.xypai.user.mapper.UserStatsMapper;
import com.xypai.user.service.IUserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.dromara.common.redis.utils.RedisUtils;

/**
 * 用户统计服务实现
 * 
 * @author Bob
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements IUserStatsService {

    private final UserStatsMapper userStatsMapper;
    
    /**
     * 缓存key前缀
     */
    private static final String CACHE_KEY_PREFIX = "user:stats:";
    
    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 24L;

    /**
     * 根据用户ID查询统计信息（优先从 Redis 读取）
     */
    @Override
    public UserStatsVO getUserStats(Long userId) {
        // 1. 尝试从 Redis 获取
        String cacheKey = CACHE_KEY_PREFIX + userId;
        Map<String, Object> cacheData = RedisUtils.getCacheMap(cacheKey);
        
        if (cacheData != null && !cacheData.isEmpty()) {
            log.debug("从 Redis 获取用户统计，userId: {}", userId);
            return convertMapToVO(userId, cacheData);
        }

        // 2. Redis 不存在，从 MySQL 查询
        UserStats userStats = userStatsMapper.selectById(userId);
        if (userStats == null) {
            log.warn("用户统计不存在，userId: {}", userId);
            return null;
        }

        // 3. 缓存到 Redis
        cacheUserStats(userId, userStats);
        
        return convertToVO(userStats);
    }

    /**
     * 批量查询用户统计
     */
    @Override
    public List<UserStatsVO> getBatchUserStats(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserStats> statsList = userStatsMapper.selectBatchByUserIds(userIds);
        return statsList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    /**
     * 初始化用户统计（新用户注册时调用�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initUserStats(Long userId) {
        UserStats userStats = UserStats.builder()
            .userId(userId)
            .followerCount(0)
            .followingCount(0)
            .contentCount(0)
            .totalLikeCount(0)
            .totalCollectCount(0)
            .activityOrganizerCount(0)
            .activityParticipantCount(0)
            .activitySuccessCount(0)
            .activityCancelCount(0)
            .activityOrganizerScore(BigDecimal.ZERO)
            .activitySuccessRate(BigDecimal.ZERO)
            .lastSyncTime(LocalDateTime.now())
            .build();

        int result = userStatsMapper.insert(userStats);
        if (result > 0) {
            cacheUserStats(userId, userStats);
            log.info("初始化用户统计成功，userId: {}", userId);
        }
        return result > 0;
    }

    /**
     * 增加粉丝�?
     */
    @Override
    public boolean incrementFollowerCount(Long userId) {
        // 1. 更新Redis
        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "followerCount", 1);

        // 2. 异步更新MySQL
        int result = userStatsMapper.incrementFollowerCount(userId);
        log.debug("增加粉丝数，userId: {}, result: {}", userId, result);
        return result > 0;
    }

    /**
     * 减少粉丝�?
     */
    @Override
    public boolean decrementFollowerCount(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "followerCount", -1);

        int result = userStatsMapper.decrementFollowerCount(userId);
        return result > 0;
    }

    /**
     * 增加关注�?
     */
    @Override
    public boolean incrementFollowingCount(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "followingCount", 1);

        int result = userStatsMapper.incrementFollowingCount(userId);
        return result > 0;
    }

    /**
     * 减少关注�?
     */
    @Override
    public boolean decrementFollowingCount(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "followingCount", -1);

        int result = userStatsMapper.decrementFollowingCount(userId);
        return result > 0;
    }

    /**
     * 增加内容�?
     */
    @Override
    public boolean incrementContentCount(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "contentCount", 1);

        int result = userStatsMapper.incrementContentCount(userId);
        return result > 0;
    }

    /**
     * 减少内容�?
     */
    @Override
    public boolean decrementContentCount(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "contentCount", -1);

        int result = userStatsMapper.decrementContentCount(userId);
        return result > 0;
    }

    /**
     * 增加点赞�?
     */
    @Override
    public boolean incrementLikeCount(Long userId, Integer count) {
        if (count == null || count <= 0) {
            return false;
        }

        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "totalLikeCount", count);

        int result = userStatsMapper.incrementLikeCount(userId, count);
        return result > 0;
    }

    /**
     * 增加收藏�?
     */
    @Override
    public boolean incrementCollectCount(Long userId, Integer count) {
        if (count == null || count <= 0) {
            return false;
        }

        String cacheKey = CACHE_KEY_PREFIX + userId;
        incrementMapValue(cacheKey, "totalCollectCount", count);

        int result = userStatsMapper.incrementCollectCount(userId, count);
        return result > 0;
    }

    /**
     * 更新组局统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActivityStats(Long userId, boolean isSuccess) {
        UserStats userStats = userStatsMapper.selectById(userId);
        if (userStats == null) {
            throw new ServiceException("用户统计不存在");
        }

        if (isSuccess) {
            userStats.markActivitySuccess();
        } else {
            userStats.markActivityCancel();
        }

        int result = userStatsMapper.updateById(userStats);
        if (result > 0) {
            cacheUserStats(userId, userStats);
        }
        return result > 0;
    }

    /**
     * 从Redis同步到MySQL（定时任务调用）
     */
    @Override
    public void syncStatsToMySQL() {
        log.info("开始同步用户统计数据到MySQL");
        
        // TODO: 实现定时同步逻辑
        // 1. 扫描Redis中的所有user:stats:*�?
        // 2. 批量更新MySQL
        // 3. 记录同步日志
        
        log.info("用户统计数据同步完成");
    }

    /**
     * 刷新用户统计缓存
     */
    @Override
    public boolean refreshCache(Long userId) {
        UserStats userStats = userStatsMapper.selectById(userId);
        if (userStats == null) {
            return false;
        }

        cacheUserStats(userId, userStats);
        log.info("刷新用户统计缓存成功，userId: {}", userId);
        return true;
    }

    /**
     * 查询人气用户
     */
    @Override
    public List<UserStatsVO> getPopularUsers(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<UserStats> statsList = userStatsMapper.selectPopularUsers(1000, limit);
        return statsList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    /**
     * 查询优质组局�?
     */
    @Override
    public List<UserStatsVO> getQualityOrganizers(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<UserStats> statsList = userStatsMapper.selectQualityOrganizers(4.5, 80.0, limit);
        return statsList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 缓存用户统计到Redis
     */
    private void cacheUserStats(Long userId, UserStats userStats) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("followerCount", userStats.getFollowerCount());
        cacheData.put("followingCount", userStats.getFollowingCount());
        cacheData.put("contentCount", userStats.getContentCount());
        cacheData.put("totalLikeCount", userStats.getTotalLikeCount());
        cacheData.put("totalCollectCount", userStats.getTotalCollectCount());
        cacheData.put("activityOrganizerCount", userStats.getActivityOrganizerCount());
        cacheData.put("activityParticipantCount", userStats.getActivityParticipantCount());
        cacheData.put("activitySuccessCount", userStats.getActivitySuccessCount());
        cacheData.put("activityCancelCount", userStats.getActivityCancelCount());
        cacheData.put("activityOrganizerScore", userStats.getActivityOrganizerScore().toString());
        cacheData.put("activitySuccessRate", userStats.getActivitySuccessRate().toString());

        RedisUtils.setCacheMap(cacheKey, cacheData);
        RedisUtils.expire(cacheKey, Duration.ofHours(CACHE_EXPIRE_HOURS));
    }

    /**
     * 转换Entity为VO
     */
    private UserStatsVO convertToVO(UserStats userStats) {
        UserStatsVO vo = new UserStatsVO();
        BeanUtils.copyProperties(userStats, vo);
        
        // 设置计算字段
        vo.setIsActive(userStats.isActiveUser());
        vo.setIsPopular(userStats.isPopularUser());
        vo.setIsQualityOrganizer(userStats.isQualityOrganizer());
        vo.setFollowerFollowingRatio(userStats.getFollowerFollowingRatio());
        
        return vo;
    }

    /**
     * 增加Map中某个字段的值（Redis Hash 增量操作）
     * 
     * @param cacheKey Redis key
     * @param field Hash 字段名
     * @param delta 增量（可以为负数）
     */
    private void incrementMapValue(String cacheKey, String field, int delta) {
        try {
            // 1. 获取当前值
            Integer currentValue = RedisUtils.getCacheMapValue(cacheKey, field);
            if (currentValue == null) {
                currentValue = 0;
            }
            
            // 2. 计算新值
            int newValue = currentValue + delta;
            
            // 3. 设置新值
            RedisUtils.setCacheMapValue(cacheKey, field, newValue);
            
            // 4. 设置过期时间
            RedisUtils.expire(cacheKey, Duration.ofHours(CACHE_EXPIRE_HOURS));
            
            log.debug("Redis Hash 增量操作成功，key: {}, field: {}, delta: {}, newValue: {}", 
                cacheKey, field, delta, newValue);
        } catch (Exception e) {
            log.error("Redis Hash 增量操作失败，key: {}, field: {}, delta: {}", cacheKey, field, delta, e);
        }
    }

    /**
     * 增加Map中某个字段的值（支持 Integer 类型）
     * 
     * @param cacheKey Redis key
     * @param field Hash 字段名
     * @param delta 增量
     */
    private void incrementMapValue(String cacheKey, String field, Integer delta) {
        if (delta != null) {
            incrementMapValue(cacheKey, field, delta.intValue());
        }
    }

    /**
     * 从 Redis Map 转换为 VO 对象
     * 
     * @param userId 用户ID
     * @param cacheData Redis 缓存数据
     * @return UserStatsVO
     */
    private UserStatsVO convertMapToVO(Long userId, Map<String, Object> cacheData) {
        return UserStatsVO.builder()
            .userId(userId)
            .followerCount(getIntValue(cacheData, "followerCount"))
            .followingCount(getIntValue(cacheData, "followingCount"))
            .contentCount(getIntValue(cacheData, "contentCount"))
            .totalLikeCount(getIntValue(cacheData, "totalLikeCount"))
            .totalCollectCount(getIntValue(cacheData, "totalCollectCount"))
            .activityOrganizerCount(getIntValue(cacheData, "activityOrganizerCount"))
            .activityParticipantCount(getIntValue(cacheData, "activityParticipantCount"))
            .activitySuccessCount(getIntValue(cacheData, "activitySuccessCount"))
            .activityCancelCount(getIntValue(cacheData, "activityCancelCount"))
            .activityOrganizerScore(getBigDecimalValue(cacheData, "activityOrganizerScore"))
            .activitySuccessRate(getBigDecimalValue(cacheData, "activitySuccessRate"))
            .build();
    }

    /**
     * 从 Map 中获取 Integer 值
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 从 Map 中获取 BigDecimal 值
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }
}

