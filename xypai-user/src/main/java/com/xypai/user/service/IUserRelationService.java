package com.xypai.user.service;

import com.xypai.user.domain.dto.UserRelationQueryDTO;
import com.xypai.user.domain.vo.UserRelationVO;

import java.util.List;
import java.util.Map;

/**
 * 用户关系服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IUserRelationService {

    /**
     * 关注用户
     */
    boolean followUser(Long targetUserId);

    /**
     * 取消关注
     */
    boolean unfollowUser(Long targetUserId);

    /**
     * 拉黑用户
     */
    boolean blockUser(Long targetUserId);

    /**
     * 取消拉黑
     */
    boolean unblockUser(Long targetUserId);

    /**
     * 获取关注列表
     */
    List<UserRelationVO> getFollowingList(UserRelationQueryDTO query);

    /**
     * 获取粉丝列表
     */
    List<UserRelationVO> getFollowersList(UserRelationQueryDTO query);

    /**
     * 获取拉黑列表
     */
    List<UserRelationVO> getBlockedList(UserRelationQueryDTO query);

    /**
     * 获取指定用户的关注列表
     */
    List<UserRelationVO> getUserFollowingList(Long userId, UserRelationQueryDTO query);

    /**
     * 获取指定用户的粉丝列表
     */
    List<UserRelationVO> getUserFollowersList(Long userId, UserRelationQueryDTO query);

    /**
     * 检查用户关系状态
     */
    Map<String, Boolean> checkUserRelation(Long targetUserId);

    /**
     * 获取关系统计
     */
    Map<String, Long> getRelationStatistics();

    /**
     * 获取指定用户的关系统计
     */
    Map<String, Long> getUserRelationStatistics(Long userId);

    /**
     * 批量关注
     */
    boolean batchFollowUsers(List<Long> userIds);

    /**
     * 批量取消关注
     */
    boolean batchUnfollowUsers(List<Long> userIds);

    /**
     * 检查是否关注了指定用户
     */
    boolean isFollowing(Long userId, Long targetUserId);

    /**
     * 检查是否拉黑了指定用户
     */
    boolean isBlocked(Long userId, Long targetUserId);

    /**
     * 获取互相关注的用户列表
     */
    List<UserRelationVO> getMutualFollowingList();

    /**
     * 推荐关注用户
     */
    List<UserRelationVO> getRecommendedUsers(Integer limit);
}
