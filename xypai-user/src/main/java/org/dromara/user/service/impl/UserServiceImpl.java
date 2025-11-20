package org.dromara.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.entity.UserStats;
import org.dromara.user.domain.dto.*;
import org.dromara.user.domain.vo.*;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.mapper.UserStatsMapper;
import org.dromara.user.mapper.UserBlacklistMapper;
import org.dromara.user.service.IUserService;
import org.dromara.user.service.IRelationService;
import org.dromara.user.service.ISkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 * User Service Implementation
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserBlacklistMapper userBlacklistMapper;
    private final IRelationService relationService;
    private final ISkillService skillService;

    private static final String CACHE_KEY_PREFIX = "user:profile:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    @Override
    public R<UserProfileVo> getUserProfile(Long userId) {
        log.info("========== getUserProfile START ==========");
        log.info("🔍 查询用户资料 - userId: {}", userId);

        String cacheKey = CACHE_KEY_PREFIX + userId;

        // Try cache first
        UserProfileVo cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            log.info("✅ 从缓存获取用户资料 - userId: {}", userId);
            return R.ok(cached);
        }
        log.info("⚠️ 缓存未命中，查询数据库 - userId: {}", userId);

        // Query database
        log.info("📊 执行数据库查询: userMapper.selectById({})", userId);
        User user = userMapper.selectById(userId);

        log.info("📊 数据库查询结果: user = {}", user);
        if (user == null) {
            log.error("❌ 用户不存在 - userId: {}", userId);
            log.error("💡 提示: 请检查数据库中是否存在该用户");
            log.error("💡 SQL: SELECT * FROM users WHERE user_id = {} AND deleted = 0", userId);
            return R.fail("User not found");
        }
        log.info("✅ 用户查询成功 - userId: {}, nickname: {}, mobile: {}",
            user.getUserId(), user.getNickname(), user.getMobile());

        // Get stats
        log.info("📊 查询用户统计数据 - userId: {}", userId);
        UserStats stats = userStatsMapper.selectByUserId(userId);
        log.info("📊 统计数据查询结果: stats = {}", stats);
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
        RedisUtils.setCacheObject(cacheKey, vo, CACHE_DURATION);
        log.info("✅ 用户资料已缓存 - userId: {}, cacheKey: {}", userId, cacheKey);

        log.info("========== getUserProfile END - SUCCESS ==========");
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
        if (!R.isSuccess(result)) {
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
        log.info("========== createUser START ==========");
        log.info("📝 创建新用户 - userId: {}, mobile: {}, countryCode: {}", userId, mobile, countryCode);

        // Generate default nickname if not provided
        if (nickname == null || nickname.isEmpty()) {
            nickname = "User" + new Random().nextInt(999999);
            log.info("🎲 生成默认昵称: {}", nickname);
        }

        // Use default avatar if not provided
        if (avatar == null || avatar.isEmpty()) {
            avatar = "https://cdn.example.com/default-avatar.png";
            log.info("🖼️ 使用默认头像: {}", avatar);
        }

        // Create user
        User user = User.builder()
            .userId(userId)
            .mobile(mobile)
            .countryCode(countryCode)
            .nickname(nickname)
            .avatar(avatar)
            .isOnline(false)
            .deleted(false)  // ⭐ CRITICAL: 必须明确设置deleted=false，否则soft delete会导致查询不到用户
            .build();

        log.info("💾 准备插入用户数据 - User对象: {}", user);
        log.info("💡 注意: userId将由IdType.INPUT模式直接使用，不会自动生成");

        int insertResult = userMapper.insert(user);
        log.info("📊 用户插入结果 - 影响行数: {}, 用户ID: {}", insertResult, user.getUserId());

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

        log.info("📊 创建用户统计数据 - userId: {}", userId);
        int statsResult = userStatsMapper.insert(stats);
        log.info("📊 统计数据插入结果 - 影响行数: {}", statsResult);

        log.info("✅ 用户创建成功 - userId: {}, nickname: {}", userId, nickname);
        log.info("========== createUser END ==========");

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
        RedisUtils.deleteObject(CACHE_KEY_PREFIX + userId);
    }

    @Override
    public R<PostListVo> getUserPosts(Long userId, Integer page, Integer pageSize) {
        log.info("获取用户动态列表 - userId: {}, page: {}, pageSize: {}", userId, page, pageSize);

        // TODO: 当前数据库中没有 posts/moments 表，返回空列表
        // 此功能需要 xypai-content 模块实现后集成

        PostListVo vo = PostListVo.builder()
            .posts(Collections.emptyList())
            .total(0L)
            .hasMore(false)
            .build();

        return R.ok(vo);
    }

    @Override
    public R<FavoriteListVo> getUserFavorites(Long userId, Integer page, Integer pageSize) {
        log.info("获取用户收藏列表 - userId: {}, page: {}, pageSize: {}", userId, page, pageSize);

        // TODO: 当前数据库中没有 favorites 表，返回空列表
        // 此功能需要 xypai-content 模块实现后集成

        FavoriteListVo vo = FavoriteListVo.builder()
            .favorites(Collections.emptyList())
            .total(0L)
            .hasMore(false)
            .build();

        return R.ok(vo);
    }

    @Override
    public R<LikeListVo> getUserLikes(Long userId, Integer page, Integer pageSize) {
        log.info("获取用户点赞列表 - userId: {}, page: {}, pageSize: {}", userId, page, pageSize);

        // TODO: 当前数据库中没有 likes 表，返回空列表
        // 此功能需要 xypai-content 模块实现后集成

        LikeListVo vo = LikeListVo.builder()
            .likes(Collections.emptyList())
            .total(0L)
            .hasMore(false)
            .build();

        return R.ok(vo);
    }

    @Override
    public R<ProfileInfoVo> getUserProfileInfo(Long userId) {
        log.info("获取用户详细资料 - userId: {}", userId);

        // 获取用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("User not found");
        }

        // 获取用户技能列表
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(100); // 获取所有技能
        TableDataInfo<SkillVo> skillsData = skillService.getUserSkills(userId, pageQuery);
        List<SkillVo> skills = skillsData != null && skillsData.getRows() != null
            ? skillsData.getRows()
            : Collections.emptyList();

        // 构建响应VO
        ProfileInfoVo vo = ProfileInfoVo.builder()
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
            .skills(skills)
            .build();

        log.info("✅ 获取用户详细资料成功 - userId: {}, skills count: {}", userId, skills.size());
        return R.ok(vo);
    }
}
