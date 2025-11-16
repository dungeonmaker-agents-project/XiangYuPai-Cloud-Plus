package org.dromara.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.domain.entity.UserRelation;
import org.dromara.user.domain.entity.UserBlacklist;
import org.dromara.user.domain.entity.UserReport;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.dto.UserBlockDto;
import org.dromara.user.domain.dto.UserReportDto;
import org.dromara.user.domain.vo.UserRelationVo;
import org.dromara.user.mapper.UserRelationMapper;
import org.dromara.user.mapper.UserBlacklistMapper;
import org.dromara.user.mapper.UserReportMapper;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.mapper.UserStatsMapper;
import org.dromara.user.service.IRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户关系服务实现
 * User Relation Service Implementation
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Service
@RequiredArgsConstructor
public class RelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation> implements IRelationService {

    private final UserRelationMapper userRelationMapper;
    private final UserBlacklistMapper userBlacklistMapper;
    private final UserReportMapper userReportMapper;
    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return R.fail("Cannot follow yourself");
        }

        // Check if already following
        UserRelation existing = userRelationMapper.selectRelation(followerId, followingId);
        if (existing != null) {
            return R.fail("Already following");
        }

        // Check if blocked
        boolean isBlocked = userBlacklistMapper.hasBlacklist(followerId, followingId);
        if (isBlocked) {
            return R.fail("Cannot follow blocked user");
        }

        // Create relation
        UserRelation relation = UserRelation.builder()
            .followerId(followerId)
            .followingId(followingId)
            .build();
        userRelationMapper.insert(relation);

        // Update stats
        userStatsMapper.incrementFollowingCount(followerId);
        userStatsMapper.incrementFansCount(followingId);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> unfollowUser(Long followerId, Long followingId) {
        UserRelation relation = userRelationMapper.selectRelation(followerId, followingId);
        if (relation == null) {
            return R.fail("Not following");
        }

        // Delete relation (soft delete)
        userRelationMapper.deleteById(relation.getRelationId());

        // Update stats
        userStatsMapper.decrementFollowingCount(followerId);
        userStatsMapper.decrementFansCount(followingId);

        return R.ok();
    }

    @Override
    public TableDataInfo<UserRelationVo> getFansList(Long userId, String keyword, PageQuery pageQuery) {
        // Get fans IDs
        List<Long> fansIds = userRelationMapper.selectFans(userId);

        if (fansIds.isEmpty()) {
            return TableDataInfo.build(new ArrayList<>());
        }

        // Query users with pagination
        Page<User> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getUserId, fansIds);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickname, keyword);
        }

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // Build VO list
        List<UserRelationVo> voList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            String followStatus = getFollowStatus(userId, user.getUserId());
            UserRelationVo vo = UserRelationVo.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .bio(user.getBio())
                .isOnline(user.getIsOnline())
                .followStatus(followStatus)
                .build();
            voList.add(vo);
        }

        return new TableDataInfo<>(voList, userPage.getTotal());
    }

    @Override
    public TableDataInfo<UserRelationVo> getFollowingList(Long userId, String keyword, PageQuery pageQuery) {
        // Get following IDs
        List<Long> followingIds = userRelationMapper.selectFollowing(userId);

        if (followingIds.isEmpty()) {
            return TableDataInfo.build(new ArrayList<>());
        }

        // Query users with pagination
        Page<User> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getUserId, followingIds);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickname, keyword);
        }

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // Build VO list
        List<UserRelationVo> voList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserRelationVo vo = UserRelationVo.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .bio(user.getBio())
                .isOnline(user.getIsOnline())
                .followStatus("following")
                .build();
            voList.add(vo);
        }

        return new TableDataInfo<>(voList, userPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> blockUser(Long userId, UserBlockDto dto) {
        if (userId.equals(dto.getBlockedUserId())) {
            return R.fail("Cannot block yourself");
        }

        // Check if already blocked
        UserBlacklist existing = userBlacklistMapper.selectBlacklist(userId, dto.getBlockedUserId());
        if (existing != null) {
            return R.fail("Already blocked");
        }

        // Create blacklist
        UserBlacklist blacklist = UserBlacklist.builder()
            .userId(userId)
            .blockedUserId(dto.getBlockedUserId())
            .reason(dto.getReason())
            .build();
        userBlacklistMapper.insert(blacklist);

        // Unfollow each other
        unfollowUser(userId, dto.getBlockedUserId());
        unfollowUser(dto.getBlockedUserId(), userId);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> unblockUser(Long userId, Long blockedUserId) {
        UserBlacklist blacklist = userBlacklistMapper.selectBlacklist(userId, blockedUserId);
        if (blacklist == null) {
            return R.fail("Not blocked");
        }

        userBlacklistMapper.deleteById(blacklist.getBlacklistId());

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> reportUser(Long reporterId, UserReportDto dto) {
        UserReport report = UserReport.builder()
            .reporterId(reporterId)
            .reportedUserId(dto.getReportedUserId())
            .reason(dto.getReason())
            .description(dto.getDescription())
            .evidence(dto.getEvidence())
            .status("pending")
            .build();

        userReportMapper.insert(report);

        return R.ok();
    }

    @Override
    public String getFollowStatus(Long followerId, Long followingId) {
        boolean isFollowing = userRelationMapper.selectRelation(followerId, followingId) != null;
        boolean isFollowedBy = userRelationMapper.selectRelation(followingId, followerId) != null;

        if (isFollowing && isFollowedBy) {
            return "mutual";
        } else if (isFollowing) {
            return "following";
        } else {
            return "none";
        }
    }

    @Override
    public boolean checkPrivacy(Long userId, Long targetUserId) {
        // Check if blocked
        boolean isBlocked = userBlacklistMapper.hasBlacklist(userId, targetUserId);
        return !isBlocked;
    }
}
