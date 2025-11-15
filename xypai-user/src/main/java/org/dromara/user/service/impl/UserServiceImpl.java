package org.dromara.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.entity.UserStats;
import org.dromara.user.domain.dto.*;
import org.dromara.user.domain.vo.UserProfileVo;
import org.dromara.user.domain.vo.UserSimpleVo;
import org.dromara.user.domain.vo.UserStatsVo;
import org.dromara.user.domain.vo.PrivacyVo;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.mapper.UserStatsMapper;
import org.dromara.user.mapper.UserBlacklistMapper;
import org.dromara.user.service.IUserService;
import org.dromara.user.service.IRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 用户服务实现
 * User Service Implementation
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserBlacklistMapper userBlacklistMapper;
    private final IRelationService relationService;
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY_PREFIX = "user:profile:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    @Override
    public R<UserProfileVo> getUserProfile(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // Try cache first
        UserProfileVo cached = redisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return R.ok(cached);
        }

        // Query database
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("User not found");
        }

        // Get stats
        UserStats stats = userStatsMapper.selectByUserId(userId);
        UserStatsVo statsVo = buildUserStatsVo(stats);

        // Build VO
        UserProfileVo vo = UserProfileVo.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .gender(user.getGender())
            .birthday(user.getBirthday())
            .residence(user.getResidence())
            .height(user.getHeight())
            .weight(user.getWeight())
            .occupation(user.getOccupation())
            .wechat(user.getWechat())
            .bio(user.getBio())
            .isOnline(user.getIsOnline())
            .stats(statsVo)
            .followStatus("none")
            .privacy(PrivacyVo.builder()
                .canViewProfile(true)
                .canViewMoments(true)
                .canViewSkills(true)
                .build())
            .build();

        // Cache result
        redisUtils.setCacheObject(cacheKey, vo, CACHE_DURATION);

        return R.ok(vo);
    }

    @Override
    public R<UserProfileVo> getOtherUserProfile(Long userId, Long targetUserId) {
        // Check if blocked
        boolean isBlocked = userBlacklistMapper.hasBlacklist(userId, targetUserId);
        if (isBlocked) {
            return R.fail("User is blocked or has blocked you");
        }

        // Get user profile
        R<UserProfileVo> result = getUserProfile(targetUserId);
        if (!result.isSuccess()) {
            return result;
        }

        UserProfileVo vo = result.getData();

        // Get follow status
        String followStatus = relationService.getFollowStatus(userId, targetUserId);
        vo.setFollowStatus(followStatus);

        // Check privacy
        boolean hasPrivacy = relationService.checkPrivacy(userId, targetUserId);
        vo.setPrivacy(PrivacyVo.builder()
            .canViewProfile(true)
            .canViewMoments(hasPrivacy)
            .canViewSkills(hasPrivacy)
            .build());

        return R.ok(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateNickname(Long userId, UpdateNicknameDto dto) {
        User user = User.builder()
            .userId(userId)
            .nickname(dto.getNickname())
            .build();

        userMapper.updateById(user);
        invalidateCache(userId);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateGender(Long userId, UpdateGenderDto dto) {
        User user = User.builder()
            .userId(userId)
            .gender(dto.getGender())
            .build();

        userMapper.updateById(user);
        invalidateCache(userId);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateBirthday(Long userId, UpdateBirthdayDto dto) {
        User user = User.builder()
            .userId(userId)
            .birthday(dto.getBirthday())
            .build();

        userMapper.updateById(user);
        invalidateCache(userId);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateUserProfile(Long userId, UserUpdateDto dto) {
        User user = User.builder()
            .userId(userId)
            .nickname(dto.getNickname())
            .avatar(dto.getAvatar())
            .gender(dto.getGender())
            .birthday(dto.getBirthday())
            .residence(dto.getResidence())
            .height(dto.getHeight())
            .weight(dto.getWeight())
            .occupation(dto.getOccupation())
            .wechat(dto.getWechat())
            .bio(dto.getBio())
            .build();

        userMapper.updateById(user);
        invalidateCache(userId);

        return R.ok();
    }

    @Override
    public R<String> uploadAvatar(Long userId, MultipartFile file) {
        // TODO: Implement OSS upload
        String avatarUrl = "https://cdn.example.com/avatar/" + userId + ".jpg";

        User user = User.builder()
            .userId(userId)
            .avatar(avatarUrl)
            .build();

        userMapper.updateById(user);
        invalidateCache(userId);

        return R.ok(avatarUrl);
    }

    @Override
    public R<List<UserSimpleVo>> batchGetUserSimpleInfo(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return R.ok(new ArrayList<>());
        }

        List<User> users = userMapper.selectBatchByIds(userIds);
        List<UserSimpleVo> voList = new ArrayList<>();

        for (User user : users) {
            UserSimpleVo vo = UserSimpleVo.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .isOnline(user.getIsOnline())
                .followStatus("none")
                .build();
            voList.add(vo);
        }

        return R.ok(voList);
    }

    @Override
    public User getUserByMobile(String mobile, String countryCode) {
        return userMapper.selectByMobile(mobile, countryCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(Long userId, String mobile, String countryCode, String nickname, String avatar) {
        // Generate default nickname if not provided
        if (nickname == null || nickname.isEmpty()) {
            nickname = "User" + new Random().nextInt(999999);
        }

        // Use default avatar if not provided
        if (avatar == null || avatar.isEmpty()) {
            avatar = "https://cdn.example.com/default-avatar.png";
        }

        // Create user
        User user = User.builder()
            .userId(userId)
            .mobile(mobile)
            .countryCode(countryCode)
            .nickname(nickname)
            .avatar(avatar)
            .isOnline(false)
            .build();

        userMapper.insert(user);

        // Create user stats
        UserStats stats = UserStats.builder()
            .userId(userId)
            .followingCount(0)
            .fansCount(0)
            .likesCount(0)
            .momentsCount(0)
            .postsCount(0)
            .collectionsCount(0)
            .skillsCount(0)
            .ordersCount(0)
            .build();

        userStatsMapper.insert(stats);

        return userId;
    }

    @Override
    public boolean updateLastLoginInfo(Long userId, String loginIp) {
        return userMapper.updateLastLoginInfo(userId, loginIp) > 0;
    }

    private UserStatsVo buildUserStatsVo(UserStats stats) {
        if (stats == null) {
            return UserStatsVo.builder()
                .followingCount(0)
                .fansCount(0)
                .likesCount(0)
                .momentsCount(0)
                .postsCount(0)
                .collectionsCount(0)
                .skillsCount(0)
                .ordersCount(0)
                .build();
        }

        return UserStatsVo.builder()
            .followingCount(stats.getFollowingCount())
            .fansCount(stats.getFansCount())
            .likesCount(stats.getLikesCount())
            .momentsCount(stats.getMomentsCount())
            .postsCount(stats.getPostsCount())
            .collectionsCount(stats.getCollectionsCount())
            .skillsCount(stats.getSkillsCount())
            .ordersCount(stats.getOrdersCount())
            .build();
    }

    private void invalidateCache(Long userId) {
        redisUtils.deleteObject(CACHE_KEY_PREFIX + userId);
    }
}
