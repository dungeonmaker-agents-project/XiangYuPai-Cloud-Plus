package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.user.domain.dto.UserRelationQueryDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfileNew;
import com.xypai.user.domain.entity.UserRelation;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.domain.vo.UserRelationVO;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.mapper.UserRelationMapper;
import com.xypai.user.service.IUserRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户关系服务实现�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRelationServiceImpl implements IUserRelationService {

    private final UserRelationMapper userRelationMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        
        if (currentUserId.equals(targetUserId)) {
            throw new ServiceException("不能关注自己");
        }

        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查是否已经关�?
        if (isFollowing(currentUserId, targetUserId)) {
            throw new ServiceException("已经关注了该用户");
        }

        // 检查是否被拉黑
        if (isBlocked(currentUserId, targetUserId) || isBlocked(targetUserId, currentUserId)) {
            throw new ServiceException("无法关注该用户");
        }

        UserRelation relation = UserRelation.builder()
                .userId(currentUserId)
                .targetId(targetUserId)
                .type(UserRelation.Type.FOLLOW.getCode())
                .createdAt(LocalDateTime.now())
                .build();

        int result = userRelationMapper.insert(relation);
        log.info("用户关注成功，用户ID：{}，目标用户ID：{}", currentUserId, targetUserId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowUser(Long targetUserId) {
        Long currentUserId = getCurrentUserId();

        LambdaQueryWrapper<UserRelation> queryWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, currentUserId)
                .eq(UserRelation::getTargetId, targetUserId)
                .eq(UserRelation::getType, UserRelation.Type.FOLLOW.getCode());

        int result = userRelationMapper.delete(queryWrapper);
        log.info("取消关注成功，用户ID：{}，目标用户ID：{}", currentUserId, targetUserId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean blockUser(Long targetUserId) {
        Long currentUserId = getCurrentUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new ServiceException("不能拉黑自己");
        }

        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 如果已经关注，先取消关注
        if (isFollowing(currentUserId, targetUserId)) {
            unfollowUser(targetUserId);
        }

        // 检查是否已经拉�?
        if (isBlocked(currentUserId, targetUserId)) {
            throw new ServiceException("已经拉黑了该用户");
        }

        UserRelation relation = UserRelation.builder()
                .userId(currentUserId)
                .targetId(targetUserId)
                .type(UserRelation.Type.BLOCK.getCode())
                .createdAt(LocalDateTime.now())
                .build();

        int result = userRelationMapper.insert(relation);
        log.info("拉黑用户成功，用户ID：{}，目标用户ID：{}", currentUserId, targetUserId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unblockUser(Long targetUserId) {
        Long currentUserId = getCurrentUserId();

        LambdaQueryWrapper<UserRelation> queryWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, currentUserId)
                .eq(UserRelation::getTargetId, targetUserId)
                .eq(UserRelation::getType, UserRelation.Type.BLOCK.getCode());

        int result = userRelationMapper.delete(queryWrapper);
        log.info("取消拉黑成功，用户ID：{}，目标用户ID：{}", currentUserId, targetUserId);
        return result > 0;
    }

    @Override
    public List<UserRelationVO> getFollowingList(UserRelationQueryDTO query) {
        Long currentUserId = getCurrentUserId();
        query.setUserId(currentUserId);
        query.setType(UserRelation.Type.FOLLOW.getCode());
        return getRelationList(query);
    }

    @Override
    public List<UserRelationVO> getFollowersList(UserRelationQueryDTO query) {
        Long currentUserId = getCurrentUserId();
        query.setTargetUserId(currentUserId);
        query.setType(UserRelation.Type.FOLLOW.getCode());
        return getRelationList(query);
    }

    @Override
    public List<UserRelationVO> getBlockedList(UserRelationQueryDTO query) {
        Long currentUserId = getCurrentUserId();
        query.setUserId(currentUserId);
        query.setType(UserRelation.Type.BLOCK.getCode());
        return getRelationList(query);
    }

    @Override
    public List<UserRelationVO> getUserFollowingList(Long userId, UserRelationQueryDTO query) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        query.setUserId(userId);
        query.setType(UserRelation.Type.FOLLOW.getCode());
        return getRelationList(query);
    }

    @Override
    public List<UserRelationVO> getUserFollowersList(Long userId, UserRelationQueryDTO query) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        query.setTargetUserId(userId);
        query.setType(UserRelation.Type.FOLLOW.getCode());
        return getRelationList(query);
    }

    @Override
    public Map<String, Boolean> checkUserRelation(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", isFollowing(currentUserId, targetUserId));
        result.put("isFollower", isFollowing(targetUserId, currentUserId));
        result.put("isBlocked", isBlocked(currentUserId, targetUserId));
        result.put("isMutual", isFollowing(currentUserId, targetUserId) && isFollowing(targetUserId, currentUserId));
        
        return result;
    }

    @Override
    public Map<String, Long> getRelationStatistics() {
        Long currentUserId = getCurrentUserId();
        return getUserRelationStatistics(currentUserId);
    }

    @Override
    public Map<String, Long> getUserRelationStatistics(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        Map<String, Long> statistics = new HashMap<>();
        
        // 关注�?
        Long followingCount = userRelationMapper.countFollowing(userId);
        statistics.put("followingCount", followingCount);
        
        // 粉丝�?
        Long followersCount = userRelationMapper.countFollowers(userId);
        statistics.put("followersCount", followersCount);
        
        // 拉黑�?
        LambdaQueryWrapper<UserRelation> blockQueryWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, userId)
                .eq(UserRelation::getType, UserRelation.Type.BLOCK.getCode());
        Long blockedCount = userRelationMapper.selectCount(blockQueryWrapper);
        statistics.put("blockedCount", blockedCount);
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchFollowUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        Long currentUserId = getCurrentUserId();
        int successCount = 0;
        
        for (Long targetUserId : userIds) {
            try {
                if (!currentUserId.equals(targetUserId) && 
                    !isFollowing(currentUserId, targetUserId) &&
                    !isBlocked(currentUserId, targetUserId) &&
                    !isBlocked(targetUserId, currentUserId)) {
                    
                    UserRelation relation = UserRelation.builder()
                            .userId(currentUserId)
                            .targetId(targetUserId)
                            .type(UserRelation.Type.FOLLOW.getCode())
                            .createdAt(LocalDateTime.now())
                            .build();
                    
                    if (userRelationMapper.insert(relation) > 0) {
                        successCount++;
                    }
                }
            } catch (Exception e) {
                log.warn("批量关注失败，目标用户ID：{}，错误：{}", targetUserId, e.getMessage());
            }
        }
        
        log.info("批量关注完成，成功数量：{}", successCount);
        return successCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUnfollowUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        Long currentUserId = getCurrentUserId();
        
        LambdaQueryWrapper<UserRelation> queryWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, currentUserId)
                .in(UserRelation::getTargetId, userIds)
                .eq(UserRelation::getType, UserRelation.Type.FOLLOW.getCode());

        int result = userRelationMapper.delete(queryWrapper);
        log.info("批量取消关注完成，取消数量：{}", result);
        return result > 0;
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }

        UserRelation relation = userRelationMapper.selectRelation(userId, targetUserId, 
                UserRelation.Type.FOLLOW.getCode());
        return relation != null;
    }

    @Override
    public boolean isBlocked(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }

        UserRelation relation = userRelationMapper.selectRelation(userId, targetUserId, 
                UserRelation.Type.BLOCK.getCode());
        return relation != null;
    }

    @Override
    public List<UserRelationVO> getMutualFollowingList() {
        Long currentUserId = getCurrentUserId();
        
        // 查询我关注的用户
        LambdaQueryWrapper<UserRelation> myFollowingWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, currentUserId)
                .eq(UserRelation::getType, UserRelation.Type.FOLLOW.getCode());
        List<UserRelation> myFollowing = userRelationMapper.selectList(myFollowingWrapper);
        
        List<UserRelationVO> mutualList = new ArrayList<>();
        
        for (UserRelation relation : myFollowing) {
            // 检查对方是否也关注了我
            if (isFollowing(relation.getTargetId(), currentUserId)) {
                UserRelationVO vo = convertToRelationVO(relation);
                mutualList.add(vo);
            }
        }
        
        return mutualList;
    }

    @Override
    public List<UserRelationVO> getRecommendedUsers(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Long currentUserId = getCurrentUserId();
        
        // 获取我已关注的用户ID列表
        LambdaQueryWrapper<UserRelation> followingWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, currentUserId)
                .eq(UserRelation::getType, UserRelation.Type.FOLLOW.getCode());
        List<UserRelation> following = userRelationMapper.selectList(followingWrapper);
        Set<Long> followingIds = following.stream()
                .map(UserRelation::getTargetId)
                .collect(Collectors.toSet());
        followingIds.add(currentUserId); // 排除自己

        // 获取我拉黑的用户ID列表
        LambdaQueryWrapper<UserRelation> blockedWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(UserRelation::getUserId, currentUserId)
                .eq(UserRelation::getType, UserRelation.Type.BLOCK.getCode());
        List<UserRelation> blocked = userRelationMapper.selectList(blockedWrapper);
        Set<Long> blockedIds = blocked.stream()
                .map(UserRelation::getTargetId)
                .collect(Collectors.toSet());

        // 查询推荐用户（排除已关注和已拉黑的）
        LambdaQueryWrapper<User> userWrapper = Wrappers.lambdaQuery(User.class)
                .notIn(!followingIds.isEmpty(), User::getId, followingIds)
                .notIn(!blockedIds.isEmpty(), User::getId, blockedIds)
                .eq(User::getStatus, User.Status.NORMAL.getCode())
                .orderByDesc(User::getCreatedAt)
                .last("LIMIT " + limit);

        List<User> recommendedUsers = userMapper.selectList(userWrapper);
        
        return recommendedUsers.stream()
                .map(this::convertUserToRelationVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取关系列表的通用方法
     */
    private List<UserRelationVO> getRelationList(UserRelationQueryDTO query) {
        LambdaQueryWrapper<UserRelation> queryWrapper = Wrappers.lambdaQuery(UserRelation.class)
                .eq(query.getUserId() != null, UserRelation::getUserId, query.getUserId())
                .eq(query.getTargetUserId() != null, UserRelation::getTargetId, query.getTargetUserId())
                .eq(query.getType() != null, UserRelation::getType, query.getType())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        UserRelation::getCreatedAt, query.getBeginTime(), query.getEndTime())
                .orderByDesc(UserRelation::getCreatedAt);

        List<UserRelation> relations = userRelationMapper.selectList(queryWrapper);
        
        return relations.stream()
                .map(this::convertToRelationVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为关系VO
     */
    private UserRelationVO convertToRelationVO(UserRelation relation) {
        // 确定目标用户ID
        Long targetUserId = relation.getTargetId();
        
        // 查询目标用户信息
        User targetUser = userMapper.selectById(targetUserId);
        UserProfileNew targetProfile = userProfileMapper.selectById(targetUserId);
        
        UserListVO targetUserVO = null;
        if (targetUser != null) {
            targetUserVO = UserListVO.builder()
                    .id(targetUser.getId())
                    .username(targetUser.getUsername())
                    .mobile(targetUser.getMobile())
                    .nickname(targetProfile != null ? targetProfile.getNickname() : null)
                    .avatar(targetProfile != null ? targetProfile.getAvatar() : null)
                    .status(targetUser.getStatus())
                    .statusDesc(targetUser.getStatusDesc())
                    .createdAt(targetUser.getCreatedAt())
                    .build();
        }

        return UserRelationVO.builder()
                .id(relation.getId())
                .userId(relation.getUserId())
                .targetUserId(relation.getTargetId())
                .targetUser(targetUserVO)
                .type(relation.getType())
                .typeDesc(relation.getTypeDesc())
                .createdAt(relation.getCreatedAt())
                .isFollow(relation.isFollow())
                .isBlock(relation.isBlock())
                .build();
    }

    /**
     * 转换用户为关系VO（用于推荐）
     */
    private UserRelationVO convertUserToRelationVO(User user) {
        UserProfileNew profile = userProfileMapper.selectById(user.getId());
        
        UserListVO userVO = UserListVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .nickname(profile != null ? profile.getNickname() : null)
                .avatar(profile != null ? profile.getAvatar() : null)
                .status(user.getStatus())
                .statusDesc(user.getStatusDesc())
                .createdAt(user.getCreatedAt())
                .build();

        return UserRelationVO.builder()
                .targetUserId(user.getId())
                .targetUser(userVO)
                .isFollow(false)
                .isBlock(false)
                .build();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }
        return currentUserId;
    }
}
