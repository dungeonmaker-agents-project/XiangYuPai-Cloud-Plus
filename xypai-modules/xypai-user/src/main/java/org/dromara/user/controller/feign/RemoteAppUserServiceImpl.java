package org.dromara.user.controller.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.dto.FilterQueryDto;
import org.dromara.appuser.api.domain.dto.WechatUnlockDto;
import org.dromara.appuser.api.domain.vo.FilterConfigVo;
import org.dromara.appuser.api.domain.vo.FilterUserPageResult;
import org.dromara.appuser.api.domain.vo.FilterUserVo;
import org.dromara.appuser.api.domain.vo.LimitedTimePageResult;
import org.dromara.appuser.api.domain.vo.LimitedTimeUserVo;
import org.dromara.appuser.api.domain.vo.OtherUserProfileVo;
import org.dromara.appuser.api.domain.vo.RemoteAppUserVo;
import org.dromara.appuser.api.domain.vo.SkillServiceDetailVo;
import org.dromara.appuser.api.domain.vo.SkillServicePageResult;
import org.dromara.appuser.api.domain.vo.SkillServiceReviewPageResult;
import org.dromara.appuser.api.domain.vo.SkillServiceReviewVo;
import org.dromara.appuser.api.domain.vo.SkillServiceVo;
import org.dromara.appuser.api.domain.vo.UserDetailInfoVo;
import org.dromara.appuser.api.domain.vo.UserSkillVo;
import org.dromara.appuser.api.domain.vo.UserSkillsPageResult;
import org.dromara.appuser.api.domain.vo.WechatUnlockResultVo;
import org.dromara.appuser.api.domain.dto.SkillServiceQueryDto;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.entity.UserRelation;
import org.dromara.user.domain.entity.UserStats;
import org.dromara.user.domain.entity.Skill;
import org.dromara.user.domain.entity.WechatUnlock;
import org.dromara.user.domain.entity.WechatUnlockConfig;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.mapper.UserRelationMapper;
import org.dromara.user.mapper.UserStatsMapper;
import org.dromara.user.mapper.SkillMapper;
import org.dromara.user.mapper.WechatUnlockMapper;
import org.dromara.user.mapper.WechatUnlockConfigMapper;
import org.dromara.user.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * App用户远程服务实现（Dubbo RPC Provider）
 * Remote App User Service Implementation
 *
 * <p>为xypai-auth提供RPC接口，实现用户认证和管理</p>
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteAppUserServiceImpl implements RemoteAppUserService {

    private final IUserService userService;
    private final UserMapper userMapper;
    private final UserRelationMapper userRelationMapper;
    private final UserStatsMapper userStatsMapper;
    private final SkillMapper skillMapper;
    private final WechatUnlockMapper wechatUnlockMapper;
    private final WechatUnlockConfigMapper wechatUnlockConfigMapper;

    /**
     * 默认解锁价格（金币）
     */
    private static final BigDecimal DEFAULT_UNLOCK_PRICE = new BigDecimal("10.00");

    @Override
    public AppLoginUser getUserByMobile(String mobile, String countryCode) throws UserException {
        User user = userService.getUserByMobile(mobile, countryCode);
        if (user == null) {
            throw new UserException("user.not.exists", "User not found");
        }

        return buildAppLoginUser(user, false);
    }

    @Override
    public AppLoginUser getUserById(Long userId) throws UserException {
        User user = userService.getById(userId);
        if (user == null) {
            throw new UserException("user.not.exists", "User not found");
        }

        return buildAppLoginUser(user, false);
    }

    @Override
    public AppLoginUser registerOrGetByMobile(String mobile, String countryCode) throws ServiceException {
        // Check if user exists
        User existingUser = userService.getUserByMobile(mobile, countryCode);
        if (existingUser != null) {
            // Existing user - return with isNewUser=false
            return buildAppLoginUser(existingUser, false);
        }

        // Create new user
        Long userId = System.currentTimeMillis(); // Generate userId (or get from auth service)
        String nickname = "User" + (int)(Math.random() * 999999);
        String avatar = "https://cdn.example.com/default-avatar.png";

        Long createdUserId = userService.createUser(userId, mobile, countryCode, nickname, avatar);

        // Return new user with isNewUser=true
        User newUser = userService.getById(createdUserId);
        return buildAppLoginUser(newUser, true);
    }

    @Override
    public boolean checkPassword(Long userId, String rawPassword) {
        // Password validation logic (BCrypt)
        // TODO: Implement password check
        return false;
    }

    @Override
    public boolean setPassword(String mobile, String countryCode, String newPassword) throws UserException {
        User user = userService.getUserByMobile(mobile, countryCode);
        if (user == null) {
            throw new UserException("user.not.exists", "User not found");
        }

        // TODO: Implement password encryption and storage
        return true;
    }

    @Override
    public boolean resetPassword(String mobile, String countryCode, String newPassword) throws UserException {
        return setPassword(mobile, countryCode, newPassword);
    }

    @Override
    public boolean updateLastLoginInfo(Long userId, String loginIp) {
        return userService.updateLastLoginInfo(userId, loginIp);
    }

    @Override
    public boolean markAsOldUser(Long userId) {
        // TODO: Mark user as old user (set isNewUser=false)
        return true;
    }

    @Override
    public boolean existsByMobile(String mobile, String countryCode) {
        User user = userService.getUserByMobile(mobile, countryCode);
        return user != null;
    }

    @Override
    public Integer getUserStatus(Long userId) throws UserException {
        User user = userService.getById(userId);
        if (user == null) {
            throw new UserException("user.not.exists", "User not found");
        }

        // TODO: Return user status (0=disabled, 1=normal, 2=locked)
        return 1; // Default to normal
    }

    @Override
    public boolean disableUser(Long userId, String reason) {
        // TODO: Implement user disable logic
        return true;
    }

    @Override
    public boolean enableUser(Long userId) {
        // TODO: Implement user enable logic
        return true;
    }

    @Override
    public boolean setPaymentPassword(Long userId, String paymentPassword) throws UserException {
        // TODO: Implement payment password logic
        return true;
    }

    @Override
    public boolean updatePaymentPassword(Long userId, String oldPaymentPassword, String newPaymentPassword) throws UserException {
        // TODO: Implement payment password update logic
        return true;
    }

    @Override
    public boolean verifyPaymentPassword(Long userId, String paymentPassword) throws UserException {
        // TODO: Implement payment password verification logic
        return true;
    }

    @Override
    public boolean hasPaymentPassword(Long userId) {
        // TODO: Implement payment password check logic
        return false;
    }

    /**
     * 构建AppLoginUser对象
     */
    private AppLoginUser buildAppLoginUser(User user, boolean isNewUser) {
        AppLoginUser loginUser = new AppLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setMobile(user.getMobile());
        loginUser.setCountryCode(user.getCountryCode());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());

        // Convert gender string to integer
        if ("male".equals(user.getGender())) {
            loginUser.setGender(1);
        } else if ("female".equals(user.getGender())) {
            loginUser.setGender(2);
        } else {
            loginUser.setGender(0);
        }

        loginUser.setStatus(1); // Normal status
        loginUser.setIsNewUser(isNewUser);

        return loginUser;
    }

    // ==================== 用户查询相关 ====================

    @Override
    public LimitedTimePageResult queryLimitedTimeUsers(
        String gender,
        String cityCode,
        String districtCode,
        Double latitude,
        Double longitude,
        Integer pageNum,
        Integer pageSize
    ) {
        // Query users with skills from database
        List<LimitedTimeUserVo> users = userMapper.queryLimitedTimeUsers(
            gender,
            cityCode,
            districtCode,
            latitude,
            longitude,
            (pageNum - 1) * pageSize, // offset
            pageSize
        );

        // Get total count
        Integer total = userMapper.countLimitedTimeUsers(gender, cityCode, districtCode);

        // Calculate if there are more pages
        boolean hasMore = (pageNum * pageSize) < total;

        // Calculate age for each user
        users.forEach(user -> {
            if (user.getAge() == null) {
                user.setAge(0); // Default age if birthday not set
            }
        });

        return LimitedTimePageResult.builder()
            .total(total)
            .list(users)
            .hasMore(hasMore)
            .build();
    }

    // ==================== 筛选功能相关 ====================

    @Override
    public FilterConfigVo getFilterConfig(String type) {
        boolean isOnline = "online".equalsIgnoreCase(type);

        FilterConfigVo.FilterConfigVoBuilder builder = FilterConfigVo.builder()
            .type(type);

        if (isOnline) {
            // 线上技能配置
            List<String> gameNames = userMapper.selectDistinctGameNames();
            List<String> gameRanks = userMapper.selectDistinctGameRanks();
            builder.gameNames(gameNames);
            builder.gameRanks(gameRanks);
        } else {
            // 线下技能配置
            List<String> serviceTypes = userMapper.selectDistinctServiceTypes();
            builder.serviceTypes(serviceTypes);
        }

        // 查询技能选项 (带用户数统计)
        List<Map<String, Object>> skillOptionsRaw = userMapper.selectSkillOptions(type);
        List<FilterConfigVo.SkillOptionVo> skillOptions = new ArrayList<>();
        for (Map<String, Object> row : skillOptionsRaw) {
            FilterConfigVo.SkillOptionVo option = FilterConfigVo.SkillOptionVo.builder()
                .value(row.get("value") != null ? row.get("value").toString() : "")
                .label(row.get("label") != null ? row.get("label").toString() : "")
                .category(row.get("category") != null ? row.get("category").toString() : "")
                .count(row.get("count") != null ? ((Number) row.get("count")).intValue() : 0)
                .build();
            skillOptions.add(option);
        }
        builder.skillOptions(skillOptions);

        // 查询价格范围
        Map<String, Object> priceRangeMap = userMapper.selectPriceRange(type);
        if (priceRangeMap != null) {
            Integer minPrice = priceRangeMap.get("minPrice") != null ?
                ((BigDecimal) priceRangeMap.get("minPrice")).intValue() : 0;
            Integer maxPrice = priceRangeMap.get("maxPrice") != null ?
                ((BigDecimal) priceRangeMap.get("maxPrice")).intValue() : 100;
            builder.priceRange(FilterConfigVo.PriceRangeVo.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build());
        }

        return builder.build();
    }

    @Override
    public FilterUserPageResult queryFilteredUsers(FilterQueryDto queryDto) {
        // 解析价格范围
        Integer priceMin = null;
        Integer priceMax = null;
        if (queryDto.getPriceRanges() != null && !queryDto.getPriceRanges().isEmpty()) {
            // 取第一个价格范围 (如 "4-9", "10-19", "20+")
            String priceRange = queryDto.getPriceRanges().get(0);
            if (priceRange.contains("-")) {
                String[] parts = priceRange.split("-");
                priceMin = Integer.parseInt(parts[0]);
                priceMax = Integer.parseInt(parts[1]);
            } else if (priceRange.endsWith("+")) {
                priceMin = Integer.parseInt(priceRange.replace("+", ""));
            }
        }

        // 计算分页偏移
        int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();

        // 查询用户列表
        List<FilterUserVo> users = userMapper.queryFilteredUsers(
            queryDto.getType(),
            queryDto.getGender(),
            queryDto.getAgeMin(),
            queryDto.getAgeMax(),
            queryDto.getStatus(),
            queryDto.getSkills(),
            priceMin,
            priceMax,
            queryDto.getLatitude(),
            queryDto.getLongitude(),
            offset,
            queryDto.getPageSize()
        );

        // 统计总数
        Integer total = userMapper.countFilteredUsers(
            queryDto.getType(),
            queryDto.getGender(),
            queryDto.getAgeMin(),
            queryDto.getAgeMax(),
            queryDto.getStatus(),
            queryDto.getSkills(),
            priceMin,
            priceMax
        );

        // 计算是否有更多
        boolean hasMore = (queryDto.getPageNum() * queryDto.getPageSize()) < total;

        return FilterUserPageResult.builder()
            .total(total)
            .list(users)
            .hasMore(hasMore)
            .build();
    }

    // ==================== 技能服务相关 ====================

    @Override
    public SkillServicePageResult querySkillServiceList(SkillServiceQueryDto queryDto) {
        // TODO: 实现技能服务列表查询
        // 1. 从 skills 表查询技能列表
        // 2. JOIN users 表获取用户信息
        // 3. 应用筛选条件 (性别、状态、游戏大区、段位、价格等)
        // 4. 应用排序 (智能排序、价格、评分、订单数)
        // 5. 构建筛选配置和Tab统计
        // 6. 返回分页结果

        // 临时返回空结果，避免编译错误
        return SkillServicePageResult.builder()
            .skillType(SkillServicePageResult.SkillTypeInfo.builder()
                .type(queryDto.getSkillType())
                .label(queryDto.getSkillType())
                .build())
            .tabs(new ArrayList<>())
            .filters(SkillServicePageResult.FilterConfig.builder().build())
            .list(new ArrayList<>())
            .total(0L)
            .hasMore(false)
            .build();
    }

    @Override
    public SkillServiceDetailVo getSkillServiceDetail(Long skillId, Long userId) {
        // TODO: 实现技能服务详情查询
        // 1. 从 skills 表查询技能基本信息
        // 2. JOIN users 表获取服务提供者信息
        // 3. 查询技能图片 (skill_images 表)
        // 4. 查询技能承诺 (skill_promises 表)
        // 5. 查询可用时间 (skill_available_times 表)
        // 6. 查询评价摘要和最近评价
        // 7. 返回完整详情

        // 临时返回null，避免编译错误
        return null;
    }

    @Override
    public SkillServiceReviewPageResult getSkillServiceReviews(Long skillId, Integer pageNum, Integer pageSize, String filterBy) {
        // TODO: 实现技能服务评价列表查询
        // 1. 从 reviews 表查询评价列表
        // 2. 根据 filterBy 筛选 (all/excellent/positive/negative)
        // 3. JOIN users 表获取评价者信息
        // 4. 统计评价摘要 (优秀/好评/差评数量)
        // 5. 统计评价标签
        // 6. 返回分页结果

        // 临时返回空结果，避免编译错误
        return SkillServiceReviewPageResult.builder()
            .skillId(skillId)
            .summary(SkillServiceReviewPageResult.ReviewSummaryVo.builder()
                .excellent(0)
                .positive(0)
                .negative(0)
                .build())
            .tags(new ArrayList<>())
            .list(new ArrayList<>())
            .total(0L)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .pages(0)
            .hasNext(false)
            .build();
    }

    // ==================== 用户信息查询（供内容服务使用） ====================

    @Override
    public RemoteAppUserVo getUserBasicInfo(Long userId, Long currentUserId) {
        // 1. 查询用户基本信息
        User user = userService.getById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return null;
        }

        // 2. 查询用户统计信息
        UserStats stats = userStatsMapper.selectByUserId(userId);

        // 3. 检查是否已关注
        boolean isFollowed = false;
        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowed = checkIsFollowed(currentUserId, userId);
        }

        // 4. 构建返回VO
        return buildRemoteAppUserVo(user, stats, isFollowed);
    }

    @Override
    public Map<Long, RemoteAppUserVo> batchGetUserBasicInfo(List<Long> userIds, Long currentUserId) {
        Map<Long, RemoteAppUserVo> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }

        // 1. 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        if (users.isEmpty()) {
            return result;
        }

        // 2. 批量查询统计信息
        List<UserStats> statsList = userStatsMapper.selectBatchByUserIds(userIds);
        Map<Long, UserStats> statsMap = statsList.stream()
            .collect(Collectors.toMap(UserStats::getUserId, s -> s, (a, b) -> a));

        // 3. 批量查询关注状态
        Set<Long> followedUserIds = new HashSet<>();
        if (currentUserId != null) {
            List<UserRelation> relations = userRelationMapper.selectBatchRelations(currentUserId, userIds);
            followedUserIds = relations.stream()
                .map(UserRelation::getFollowingId)
                .collect(Collectors.toSet());
        }

        // 4. 构建结果
        for (User user : users) {
            UserStats stats = statsMap.get(user.getUserId());
            boolean isFollowed = followedUserIds.contains(user.getUserId());
            result.put(user.getUserId(), buildRemoteAppUserVo(user, stats, isFollowed));
        }

        return result;
    }

    @Override
    public boolean checkIsFollowed(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }
        UserRelation relation = userRelationMapper.selectRelation(userId, targetUserId);
        return relation != null && "active".equals(relation.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }

        // 检查是否已关注
        UserRelation existingRelation = userRelationMapper.selectRelation(userId, targetUserId);
        if (existingRelation != null) {
            log.info("用户 {} 已关注用户 {}", userId, targetUserId);
            return false;
        }

        // 创建关注关系
        UserRelation relation = UserRelation.builder()
            .followerId(userId)
            .followingId(targetUserId)
            .status("active")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deleted(false)
            .version(0)
            .build();
        userRelationMapper.insert(relation);

        // 更新统计数据
        userStatsMapper.incrementFollowingCount(userId);  // 当前用户关注数+1
        userStatsMapper.incrementFansCount(targetUserId); // 目标用户粉丝数+1

        log.info("用户 {} 关注用户 {} 成功", userId, targetUserId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowUser(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }

        // 检查是否已关注
        UserRelation existingRelation = userRelationMapper.selectRelation(userId, targetUserId);
        if (existingRelation == null) {
            log.info("用户 {} 未关注用户 {}", userId, targetUserId);
            return false;
        }

        // 软删除关注关系 - 使用 deleteById 让 @TableLogic 自动处理
        // 注意: updateById 会忽略 @TableLogic 标记的字段，所以必须用 deleteById
        userRelationMapper.deleteById(existingRelation.getRelationId());

        // 更新统计数据
        userStatsMapper.decrementFollowingCount(userId);  // 当前用户关注数-1
        userStatsMapper.decrementFansCount(targetUserId); // 目标用户粉丝数-1

        log.info("用户 {} 取消关注用户 {} 成功", userId, targetUserId);
        return true;
    }

    @Override
    public List<Long> getFollowingIds(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return userRelationMapper.selectFollowing(userId);
    }

    /**
     * 构建RemoteAppUserVo
     */
    private RemoteAppUserVo buildRemoteAppUserVo(User user, UserStats stats, boolean isFollowed) {
        // 计算年龄
        Integer age = null;
        if (user.getBirthday() != null) {
            age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
        }

        return RemoteAppUserVo.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .gender(user.getGender())
            .birthday(user.getBirthday())
            .residence(user.getResidence())
            .bio(user.getBio())
            .level(user.getLevel() != null ? user.getLevel() : 1)
            .levelName(RemoteAppUserVo.getLevelNameByLevel(user.getLevel()))
            .isRealVerified(user.getIsRealVerified() != null && user.getIsRealVerified())
            .isGodVerified(user.getIsGodVerified() != null && user.getIsGodVerified())
            .isVip(user.getIsVip() != null && user.getIsVip())
            .isOnline(user.getIsOnline() != null && user.getIsOnline())
            .followingCount(stats != null ? stats.getFollowingCount() : 0)
            .fansCount(stats != null ? stats.getFansCount() : 0)
            .likesCount(stats != null ? stats.getLikesCount() : 0)
            .isFollowed(isFollowed)
            .build();
    }

    // ==================== 对方主页相关 ====================

    @Override
    public OtherUserProfileVo getOtherUserProfileData(Long targetUserId, Long currentUserId, Double latitude, Double longitude) {
        // 1. 查询目标用户基本信息
        User user = userService.getById(targetUserId);
        if (user == null) {
            log.warn("目标用户不存在: targetUserId={}", targetUserId);
            return null;
        }

        // 2. 查询用户统计信息
        UserStats stats = userStatsMapper.selectByUserId(targetUserId);

        // 3. 查询关系状态
        String followStatus = "none";
        boolean isBlocked = false;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            boolean iFollow = checkIsFollowed(currentUserId, targetUserId);
            boolean followsMe = checkIsFollowed(targetUserId, currentUserId);
            if (iFollow && followsMe) {
                followStatus = "mutual";
            } else if (iFollow) {
                followStatus = "following";
            }
        }

        // 4. 查询微信解锁状态
        boolean wechatUnlocked = false;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            wechatUnlocked = checkWechatUnlocked(currentUserId, targetUserId);
        }

        // 5. 获取解锁价格
        BigDecimal unlockPrice = getWechatUnlockPrice(targetUserId);

        // 6. 检查是否可解锁微信
        boolean canUnlockWechat = user.getWechat() != null && !user.getWechat().isEmpty() && !wechatUnlocked;

        // 7. 检查是否有上架技能
        long onlineSkillCount = skillMapper.countOnlineSkillsByUserId(targetUserId);
        boolean isAvailable = onlineSkillCount > 0;

        // 8. 计算距离
        Integer distance = null;
        String distanceDisplay = null;
        if (latitude != null && longitude != null && user.getLatitude() != null && user.getLongitude() != null) {
            distance = calculateDistance(latitude, longitude, user.getLatitude().doubleValue(), user.getLongitude().doubleValue());
            distanceDisplay = formatDistance(distance);
        }

        // 9. 计算年龄
        Integer age = null;
        if (user.getBirthday() != null) {
            age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
        }

        // 10. 构建等级信息
        OtherUserProfileVo.LevelInfo levelInfo = OtherUserProfileVo.LevelInfo.builder()
            .value(user.getLevel() != null ? user.getLevel() : 1)
            .name(getLevelName(user.getLevel()))
            .icon(getLevelIcon(user.getLevel()))
            .color(getLevelColor(user.getLevel()))
            .build();

        // 11. 构建统计信息
        OtherUserProfileVo.StatsInfo statsInfo = OtherUserProfileVo.StatsInfo.builder()
            .followingCount(stats != null ? stats.getFollowingCount() : 0)
            .fansCount(stats != null ? stats.getFansCount() : 0)
            .likesCount(stats != null ? stats.getLikesCount() : 0)
            .momentsCount(stats != null ? stats.getMomentsCount() : 0)
            .skillsCount(stats != null ? stats.getSkillsCount() : 0)
            .build();

        // 12. 构建返回VO
        return OtherUserProfileVo.builder()
            .userId(user.getUserId())
            .avatar(user.getAvatar())
            .coverUrl(null) // TODO: 添加封面图字段
            .nickname(user.getNickname())
            .gender(user.getGender())
            .age(age)
            .birthday(user.getBirthday())
            .bio(user.getBio())
            .level(levelInfo)
            .isRealVerified(user.getIsRealVerified() != null && user.getIsRealVerified())
            .isGodVerified(user.getIsGodVerified() != null && user.getIsGodVerified())
            .isVip(user.getIsVip() != null && user.getIsVip())
            .isOnline(user.getIsOnline() != null && user.getIsOnline())
            .isAvailable(isAvailable)
            .residence(user.getResidence())
            .ipLocation(null) // TODO: 添加IP归属地字段
            .distance(distance)
            .distanceDisplay(distanceDisplay)
            .stats(statsInfo)
            .followStatus(followStatus)
            .isBlocked(isBlocked)
            .canMessage(true) // 默认可发消息
            .canUnlockWechat(canUnlockWechat)
            .wechatUnlocked(wechatUnlocked)
            .unlockPrice(unlockPrice)
            .wechat(wechatUnlocked ? user.getWechat() : maskWechat(user.getWechat()))
            .build();
    }

    @Override
    public UserDetailInfoVo getUserDetailInfo(Long targetUserId, Long currentUserId) {
        // 1. 查询目标用户
        User user = userService.getById(targetUserId);
        if (user == null) {
            log.warn("目标用户不存在: targetUserId={}", targetUserId);
            return null;
        }

        // 2. 检查微信解锁状态
        boolean wechatUnlocked = false;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            wechatUnlocked = checkWechatUnlocked(currentUserId, targetUserId);
        } else if (currentUserId != null && currentUserId.equals(targetUserId)) {
            // 查看自己的资料，显示完整微信
            wechatUnlocked = true;
        }

        // 3. 计算年龄和星座
        Integer age = null;
        String zodiac = null;
        if (user.getBirthday() != null) {
            age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
            zodiac = getZodiac(user.getBirthday());
        }

        // 4. 构建返回VO
        return UserDetailInfoVo.builder()
            .userId(user.getUserId())
            .residence(user.getResidence())
            .ipLocation(null) // TODO: 添加IP归属地
            .height(user.getHeight())
            .weight(user.getWeight())
            .occupation(user.getOccupation())
            .wechat(wechatUnlocked ? user.getWechat() : maskWechat(user.getWechat()))
            .wechatUnlocked(wechatUnlocked)
            .birthday(user.getBirthday())
            .zodiac(zodiac)
            .age(age)
            .bio(user.getBio())
            .canViewFull(true)
            .viewRestrictionReason(null)
            .build();
    }

    @Override
    public UserSkillsPageResult getUserSkillsList(Long targetUserId, Long currentUserId, Integer pageNum, Integer pageSize) {
        // 1. 计算分页偏移
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询用户技能列表
        List<Skill> skills = skillMapper.selectSkillsByUserId(targetUserId, offset, pageSize);

        // 3. 统计总数
        long total = skillMapper.countSkillsByUserId(targetUserId);

        // 4. 转换为VO
        List<UserSkillVo> skillVos = skills.stream()
            .map(this::convertToUserSkillVo)
            .collect(Collectors.toList());

        // 5. 构建返回结果
        return UserSkillsPageResult.builder()
            .list(skillVos)
            .total(total)
            .hasMore((long) pageNum * pageSize < total)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .build();
    }

    // ==================== 微信解锁相关 ====================

    @Override
    public boolean checkWechatUnlocked(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }
        // 自己查看自己，视为已解锁
        if (userId.equals(targetUserId)) {
            return true;
        }
        return wechatUnlockMapper.existsUnlock(userId, targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatUnlockResultVo unlockWechat(WechatUnlockDto unlockDto) {
        Long userId = unlockDto.getUserId();
        Long targetUserId = unlockDto.getTargetUserId();

        // 1. 检查目标用户是否存在
        User targetUser = userService.getById(targetUserId);
        if (targetUser == null) {
            return WechatUnlockResultVo.builder()
                .success(false)
                .failCode("USER_NOT_FOUND")
                .failReason("目标用户不存在")
                .build();
        }

        // 2. 检查目标用户是否设置了微信
        if (targetUser.getWechat() == null || targetUser.getWechat().isEmpty()) {
            return WechatUnlockResultVo.builder()
                .success(false)
                .failCode("NO_WECHAT")
                .failReason("该用户未设置微信号")
                .build();
        }

        // 3. 检查是否已解锁
        if (checkWechatUnlocked(userId, targetUserId)) {
            return WechatUnlockResultVo.builder()
                .success(true)
                .wechat(targetUser.getWechat())
                .cost(BigDecimal.ZERO)
                .build();
        }

        // 4. 获取解锁价格
        BigDecimal unlockPrice = getWechatUnlockPrice(targetUserId);
        if (unlockPrice == null) {
            unlockPrice = DEFAULT_UNLOCK_PRICE;
        }

        // 5. TODO: 验证支付密码并扣除金币
        // 这里需要调用钱包服务扣除金币
        // 暂时跳过支付验证，直接解锁

        // 6. 创建解锁记录
        WechatUnlock unlock = WechatUnlock.builder()
            .userId(userId)
            .targetUserId(targetUserId)
            .costCoins(unlockPrice.intValue())
            .unlockType(unlockDto.getUnlockType() != null ? unlockDto.getUnlockType() : "coins")
            .createdAt(LocalDateTime.now())
            .deleted(false)
            .version(0)
            .build();
        wechatUnlockMapper.insert(unlock);

        log.info("用户 {} 解锁用户 {} 的微信成功，费用: {}", userId, targetUserId, unlockPrice);

        // 7. 返回成功结果
        return WechatUnlockResultVo.builder()
            .success(true)
            .wechat(targetUser.getWechat())
            .cost(unlockPrice)
            .build();
    }

    @Override
    public BigDecimal getWechatUnlockPrice(Long targetUserId) {
        // 1. 查询用户配置的解锁价格
        BigDecimal configPrice = wechatUnlockConfigMapper.selectUnlockPrice(targetUserId);
        if (configPrice != null) {
            return configPrice;
        }

        // 2. 检查用户是否设置了微信
        User user = userService.getById(targetUserId);
        if (user == null || user.getWechat() == null || user.getWechat().isEmpty()) {
            return null;
        }

        // 3. 返回默认价格
        return DEFAULT_UNLOCK_PRICE;
    }

    // ==================== 辅助方法 ====================

    /**
     * 脱敏微信号（显示前2后2，中间用****）
     */
    private String maskWechat(String wechat) {
        if (wechat == null || wechat.isEmpty()) {
            return null;
        }
        if (wechat.length() <= 4) {
            return wechat.charAt(0) + "****";
        }
        return wechat.substring(0, 2) + "****" + wechat.substring(wechat.length() - 2);
    }

    /**
     * 计算两点之间的距离（米）
     */
    private Integer calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371000; // 地球半径（米）
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(latRad1) * Math.cos(latRad2)
            * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (R * c);
    }

    /**
     * 格式化距离显示
     */
    private String formatDistance(Integer distance) {
        if (distance == null) {
            return null;
        }
        if (distance < 1000) {
            return distance + "m";
        }
        return String.format("%.1fkm", distance / 1000.0);
    }

    /**
     * 获取等级名称
     */
    private String getLevelName(Integer level) {
        if (level == null) level = 1;
        return switch (level) {
            case 1 -> "青铜";
            case 2 -> "白银";
            case 3 -> "黄金";
            case 4 -> "铂金";
            case 5 -> "钻石";
            case 6 -> "大师";
            case 7 -> "王者";
            default -> "青铜";
        };
    }

    /**
     * 获取等级图标
     */
    private String getLevelIcon(Integer level) {
        if (level == null) level = 1;
        return "level_" + level + ".png";
    }

    /**
     * 获取等级颜色
     */
    private String getLevelColor(Integer level) {
        if (level == null) level = 1;
        return switch (level) {
            case 1 -> "#CD7F32";
            case 2 -> "#C0C0C0";
            case 3 -> "#FFD700";
            case 4 -> "#E5E4E2";
            case 5 -> "#B9F2FF";
            case 6 -> "#9400D3";
            case 7 -> "#FF4500";
            default -> "#CD7F32";
        };
    }

    /**
     * 根据生日获取星座
     */
    private String getZodiac(LocalDate birthday) {
        if (birthday == null) return null;
        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();

        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座";
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "双鱼座";
        return null;
    }

    /**
     * 转换技能为VO
     */
    private UserSkillVo convertToUserSkillVo(Skill skill) {
        return UserSkillVo.builder()
            .skillId(skill.getSkillId())
            .skillName(skill.getSkillName())
            .skillType(skill.getSkillType())
            .coverImage(skill.getCoverImage())
            .description(skill.getDescription())
            .mediaData(UserSkillVo.MediaData.builder()
                .coverUrl(skill.getCoverImage())
                .build())
            .skillInfo(UserSkillVo.SkillInfo.builder()
                .gameName(skill.getGameName())
                .gameRank(skill.getGameRank())
                .serviceType(skill.getServiceType())
                .serviceLocation(skill.getServiceLocation())
                .serviceHours(skill.getServiceHours())
                .build())
            .priceData(UserSkillVo.PriceData.builder()
                .amount(skill.getPrice())
                .unit(skill.getPriceUnit())
                .displayText(skill.getPrice() + "元/" + skill.getPriceUnit())
                .build())
            .statsData(UserSkillVo.StatsData.builder()
                .rating(skill.getRating())
                .reviewCount(skill.getReviewCount())
                .orderCount(skill.getOrderCount())
                .build())
            .isOnline(skill.getIsOnline())
            .build();
    }
}
