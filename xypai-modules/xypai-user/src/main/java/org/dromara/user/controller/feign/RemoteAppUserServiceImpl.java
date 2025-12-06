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
import org.dromara.user.domain.entity.SkillImage;
import org.dromara.user.domain.entity.SkillPromise;
import org.dromara.user.domain.entity.SkillAvailableTime;
import org.dromara.user.domain.entity.WechatUnlock;
import org.dromara.user.domain.entity.WechatUnlockConfig;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.mapper.UserRelationMapper;
import org.dromara.user.mapper.UserStatsMapper;
import org.dromara.user.mapper.SkillMapper;
import org.dromara.user.mapper.SkillImageMapper;
import org.dromara.user.mapper.SkillPromiseMapper;
import org.dromara.user.mapper.SkillAvailableTimeMapper;
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
    private final SkillImageMapper skillImageMapper;
    private final SkillPromiseMapper skillPromiseMapper;
    private final SkillAvailableTimeMapper skillAvailableTimeMapper;
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

    /**
     * 查询技能服务列表（王者荣耀陪玩）
     * 调用场景：BFF层获取陪玩列表
     * 核心逻辑：
     *   1. 解析查询参数（分页、筛选、排序）
     *   2. 调用Mapper查询技能+用户联合数据
     *   3. 转换为VO并构建筛选配置
     *   4. 构建Tab统计信息
     * 内部函数：buildSkillServiceVoFromMap, buildFilterConfig, buildTabInfoList, parsePriceRange, calculateAge
     * 外部函数：skillMapper.querySkillServiceListWithUser, skillMapper.countSkillServiceList
     */
    @Override
    public SkillServicePageResult querySkillServiceList(SkillServiceQueryDto queryDto) {
        // 参数默认值处理
        String gameName = queryDto.getSkillType();
        String tabType = queryDto.getTabType() != null ? queryDto.getTabType() : "glory_king";
        String sortBy = queryDto.getSortBy() != null ? queryDto.getSortBy() : "smart";
        int pageNum = queryDto.getPageNum() != null ? queryDto.getPageNum() : 1;
        int pageSize = queryDto.getPageSize() != null ? queryDto.getPageSize() : 20;
        int offset = (pageNum - 1) * pageSize;

        // 解析筛选条件
        String gender = queryDto.getGender();
        Integer isOnline = "online".equals(queryDto.getStatus()) ? 1 : null;
        String server = queryDto.getGameArea();
        List<String> ranks = queryDto.getRanks();

        // 解析价格区间
        BigDecimal priceMin = null, priceMax = null;
        if (queryDto.getPriceRanges() != null && !queryDto.getPriceRanges().isEmpty()) {
            BigDecimal[] priceRange = parsePriceRange(queryDto.getPriceRanges());
            priceMin = priceRange[0];
            priceMax = priceRange[1];
        }

        // 查询数据
        List<Map<String, Object>> dataList = skillMapper.querySkillServiceListWithUser(
            gameName, tabType, sortBy, gender, isOnline, server, ranks, priceMin, priceMax, offset, pageSize
        );

        // 查询总数
        Long total = skillMapper.countSkillServiceList(gameName, gender, isOnline, server, ranks, priceMin, priceMax);

        // 转换为VO
        List<SkillServiceVo> voList = dataList.stream()
            .map(this::buildSkillServiceVoFromMap)
            .collect(Collectors.toList());

        // 构建结果
        boolean hasMore = (pageNum * pageSize) < total;

        return SkillServicePageResult.builder()
            .skillType(SkillServicePageResult.SkillTypeInfo.builder()
                .type(gameName)
                .label(gameName)
                .build())
            .tabs(buildTabInfoList(gameName))
            .filters(buildFilterConfig())
            .list(voList)
            .total(total)
            .hasMore(hasMore)
            .build();
    }

    /**
     * 从Map构建SkillServiceVo
     * 调用场景：querySkillServiceList内部转换
     * 核心逻辑：将SQL查询结果Map转换为VO对象
     */
    private SkillServiceVo buildSkillServiceVoFromMap(Map<String, Object> data) {
        // 计算年龄
        Integer age = null;
        Object birthday = data.get("birthday");
        if (birthday instanceof LocalDate) {
            age = calculateAge((LocalDate) birthday);
        }

        // 构建价格显示
        BigDecimal price = data.get("price") != null ? new BigDecimal(data.get("price").toString()) : BigDecimal.ZERO;
        String priceUnit = (String) data.get("price_unit");
        String priceDisplay = price.stripTrailingZeros().toPlainString() + " 金币/" + (priceUnit != null ? priceUnit : "局");

        return SkillServiceVo.builder()
            .skillId(getLongValue(data, "skill_id"))
            .userId(getLongValue(data, "user_id"))
            .nickname((String) data.get("nickname"))
            .avatar((String) data.get("avatar"))
            .gender((String) data.get("gender"))
            .age(age)
            .isOnline(getBoolValue(data, "user_is_online"))
            .isVerified(getBoolValue(data, "is_real_verified") || getBoolValue(data, "is_god_verified"))
            .skillType((String) data.get("skill_type"))
            .skillTypeName((String) data.get("game_name"))
            .gameArea((String) data.get("server"))
            .rank((String) data.get("game_rank"))
            .peakScore(getIntValue(data, "peak_score"))
            .price(price)
            .priceUnit(priceUnit)
            .priceDisplay(priceDisplay)
            .description((String) data.get("description"))
            .orderCount(getIntValue(data, "order_count") != null ? getIntValue(data, "order_count") : 0)
            .rating(data.get("rating") != null ? new BigDecimal(data.get("rating").toString()) : new BigDecimal("5.0"))
            .reviewCount(getIntValue(data, "review_count") != null ? getIntValue(data, "review_count") : 0)
            .build();
    }

    /**
     * 构建筛选配置
     * 调用场景：querySkillServiceList构建FilterConfig
     * 核心逻辑：返回王者荣耀陪玩列表的筛选选项配置
     */
    private SkillServicePageResult.FilterConfig buildFilterConfig() {
        return SkillServicePageResult.FilterConfig.builder()
            .sortOptions(List.of(
                SkillServicePageResult.OptionInfo.builder().value("smart").label("智能排序").build(),
                SkillServicePageResult.OptionInfo.builder().value("newest").label("最新发布").build(),
                SkillServicePageResult.OptionInfo.builder().value("recent").label("最近活跃").build(),
                SkillServicePageResult.OptionInfo.builder().value("popular").label("热门推荐").build(),
                SkillServicePageResult.OptionInfo.builder().value("price_asc").label("价格最低").build(),
                SkillServicePageResult.OptionInfo.builder().value("price_desc").label("价格最高").build()
            ))
            .genderOptions(List.of(
                SkillServicePageResult.OptionInfo.builder().value("all").label("不限").build(),
                SkillServicePageResult.OptionInfo.builder().value("male").label("男").build(),
                SkillServicePageResult.OptionInfo.builder().value("female").label("女").build()
            ))
            .statusOptions(List.of(
                SkillServicePageResult.OptionInfo.builder().value("all").label("不限").build(),
                SkillServicePageResult.OptionInfo.builder().value("online").label("在线").build()
            ))
            .gameAreas(List.of(
                SkillServicePageResult.OptionInfo.builder().value("微信区").label("微信区").build(),
                SkillServicePageResult.OptionInfo.builder().value("QQ区").label("QQ区").build()
            ))
            .ranks(List.of(
                SkillServicePageResult.OptionInfo.builder().value("荣耀王者").label("荣耀王者").build(),
                SkillServicePageResult.OptionInfo.builder().value("超凡大师").label("超凡大师").build(),
                SkillServicePageResult.OptionInfo.builder().value("无双王者").label("无双王者").build(),
                SkillServicePageResult.OptionInfo.builder().value("星耀").label("星耀").build(),
                SkillServicePageResult.OptionInfo.builder().value("钻石").label("钻石").build()
            ))
            .priceRanges(List.of(
                SkillServicePageResult.PriceRangeInfo.builder().value("0-10").label("0-10金币").min(BigDecimal.ZERO).max(BigDecimal.TEN).build(),
                SkillServicePageResult.PriceRangeInfo.builder().value("10-30").label("10-30金币").min(BigDecimal.TEN).max(new BigDecimal("30")).build(),
                SkillServicePageResult.PriceRangeInfo.builder().value("30-50").label("30-50金币").min(new BigDecimal("30")).max(new BigDecimal("50")).build(),
                SkillServicePageResult.PriceRangeInfo.builder().value("50+").label("50金币以上").min(new BigDecimal("50")).max(null).build()
            ))
            .build();
    }

    /**
     * 构建Tab列表
     * 调用场景：querySkillServiceList构建Tab统计
     * 核心逻辑：查询各Tab对应数量
     */
    private List<SkillServicePageResult.TabInfo> buildTabInfoList(String gameName) {
        Long totalCount = skillMapper.countByGameName(gameName);
        Long onlineCount = skillMapper.countOnlineByGameName(gameName);
        return List.of(
            SkillServicePageResult.TabInfo.builder().value("glory_king").label("荣耀王者").count(totalCount.intValue()).build(),
            SkillServicePageResult.TabInfo.builder().value("online").label("在线").count(onlineCount.intValue()).build()
        );
    }

    /**
     * 解析价格区间
     * 调用场景：querySkillServiceList解析priceRanges参数
     * 核心逻辑：合并多个价格区间为最小值和最大值
     */
    private BigDecimal[] parsePriceRange(List<String> priceRanges) {
        BigDecimal min = null, max = null;
        for (String range : priceRanges) {
            if (range.endsWith("+")) {
                BigDecimal rangeMin = new BigDecimal(range.replace("+", ""));
                if (min == null || rangeMin.compareTo(min) < 0) min = rangeMin;
            } else if (range.contains("-")) {
                String[] parts = range.split("-");
                BigDecimal rangeMin = new BigDecimal(parts[0]);
                BigDecimal rangeMax = new BigDecimal(parts[1]);
                if (min == null || rangeMin.compareTo(min) < 0) min = rangeMin;
                if (max == null || rangeMax.compareTo(max) > 0) max = rangeMax;
            }
        }
        return new BigDecimal[]{min, max};
    }

    /**
     * 计算年龄
     */
    private Integer calculateAge(LocalDate birthday) {
        if (birthday == null) return null;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    /**
     * 从Map获取Long值（工具方法）
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        return Long.parseLong(val.toString());
    }

    /**
     * 从Map获取Integer值（工具方法）
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        return Integer.parseInt(val.toString());
    }

    /**
     * 从Map获取Boolean值（工具方法）
     */
    private Boolean getBoolValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() == 1;
        return Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取技能服务详情
     * Invocation: BFF layer calls this for service detail page
     * Core Logic:
     *   1. Query skill + user info from database
     *   2. Query skill images, promises, available times
     *   3. Build complete SkillServiceDetailVo
     * Internal: buildSkillDetailVoFromMap, buildProviderDetailVo, buildScheduleInfo
     * External: skillMapper.selectSkillDetailWithUser, skillImageMapper.selectBySkillId
     */
    @Override
    public SkillServiceDetailVo getSkillServiceDetail(Long skillId, Long userId) {
        log.info("查询技能服务详情: skillId={}, userId={}", skillId, userId);

        // 1. Query skill + user info
        Map<String, Object> data = skillMapper.selectSkillDetailWithUser(skillId);
        if (data == null || data.isEmpty()) {
            log.warn("技能不存在: skillId={}", skillId);
            return null;
        }

        // 2. Query skill images
        List<SkillImage> images = skillImageMapper.selectBySkillId(skillId);
        List<String> imageUrls = images.stream()
            .map(SkillImage::getImageUrl)
            .collect(Collectors.toList());

        // 3. Query skill promises (for tags)
        List<SkillPromise> promises = skillPromiseMapper.selectBySkillId(skillId);
        List<SkillServiceVo.SkillTagVo> tags = promises.stream()
            .map(p -> SkillServiceVo.SkillTagVo.builder()
                .text(p.getPromiseText())
                .type("promise")
                .color("#FF6B9D")
                .build())
            .collect(Collectors.toList());

        // 4. Query available times (for schedule)
        List<SkillAvailableTime> availableTimes = skillAvailableTimeMapper.selectBySkillId(skillId);
        String scheduleText = buildScheduleText(availableTimes);

        // 5. Build provider info
        Integer age = null;
        Object birthday = data.get("birthday");
        if (birthday instanceof LocalDate) {
            age = calculateAge((LocalDate) birthday);
        }

        boolean isRealVerified = getBoolValue(data, "is_real_verified");
        boolean isGodVerified = getBoolValue(data, "is_god_verified");

        List<String> certifications = new ArrayList<>();
        if (isRealVerified) certifications.add("实名认证");
        if (isGodVerified) certifications.add("大神认证");

        SkillServiceDetailVo.ProviderDetailVo provider = SkillServiceDetailVo.ProviderDetailVo.builder()
            .userId(getLongValue(data, "user_id"))
            .nickname((String) data.get("nickname"))
            .avatar((String) data.get("avatar"))
            .gender((String) data.get("gender"))
            .age(age)
            .isOnline(getBoolValue(data, "user_is_online"))
            .isVerified(isRealVerified || isGodVerified)
            .level(getIntValue(data, "level"))
            .certifications(certifications)
            .build();

        // 6. Build skill info
        SkillServiceDetailVo.SkillDetailInfo skillInfo = SkillServiceDetailVo.SkillDetailInfo.builder()
            .skillType((String) data.get("skill_type"))
            .skillLabel((String) data.get("game_name"))
            .gameArea((String) data.get("server"))
            .rank((String) data.get("game_rank"))
            .rankScore(getIntValue(data, "peak_score"))
            .rankDisplay((String) data.get("game_rank"))
            .build();

        // 7. Build price info
        BigDecimal price = data.get("price") != null ? new BigDecimal(data.get("price").toString()) : BigDecimal.ZERO;
        String priceUnit = (String) data.get("price_unit");
        String priceDisplay = price.stripTrailingZeros().toPlainString() + " 金币/" + (priceUnit != null ? priceUnit : "局");

        SkillServiceDetailVo.PriceInfo priceInfo = SkillServiceDetailVo.PriceInfo.builder()
            .amount(price)
            .unit(priceUnit)
            .displayText(priceDisplay)
            .build();

        // 8. Build stats info
        SkillServiceDetailVo.StatsInfo stats = SkillServiceDetailVo.StatsInfo.builder()
            .orders(getIntValue(data, "order_count"))
            .rating(data.get("rating") != null ? new BigDecimal(data.get("rating").toString()) : BigDecimal.ZERO)
            .reviewCount(getIntValue(data, "review_count"))
            .build();

        // 9. Build schedule info
        SkillServiceDetailVo.ScheduleInfo schedule = SkillServiceDetailVo.ScheduleInfo.builder()
            .available(scheduleText)
            .build();

        // 10. Build location info (for offline skills)
        SkillServiceDetailVo.LocationInfo location = null;
        if ("offline".equals(data.get("skill_type"))) {
            location = SkillServiceDetailVo.LocationInfo.builder()
                .address((String) data.get("service_location"))
                .district((String) data.get("residence"))
                .build();
        }

        // 11. Build reviews placeholder (basic info)
        SkillServiceDetailVo.ReviewsInfo reviews = SkillServiceDetailVo.ReviewsInfo.builder()
            .total(getIntValue(data, "review_count"))
            .summary(SkillServiceDetailVo.ReviewSummaryVo.builder()
                .excellent(0)
                .positive(getIntValue(data, "review_count"))
                .negative(0)
                .build())
            .tags(new ArrayList<>())
            .recent(new ArrayList<>())
            .build();

        // 12. Build complete VO
        String coverImage = (String) data.get("cover_image");
        return SkillServiceDetailVo.builder()
            .skillId(skillId)
            .bannerImage(coverImage)
            .images(imageUrls.isEmpty() ? (coverImage != null ? List.of(coverImage) : new ArrayList<>()) : imageUrls)
            .provider(provider)
            .skillInfo(skillInfo)
            .tags(tags)
            .description((String) data.get("description"))
            .price(priceInfo)
            .schedule(schedule)
            .location(location)
            .stats(stats)
            .reviews(reviews)
            .isAvailable(getBoolValue(data, "skill_is_online"))
            .unavailableReason(getBoolValue(data, "skill_is_online") ? null : "该技能已下架")
            .build();
    }

    /**
     * Build schedule text from available times
     * Core Logic: Combine day-of-week and time range into display text
     */
    private String buildScheduleText(List<SkillAvailableTime> availableTimes) {
        if (availableTimes == null || availableTimes.isEmpty()) {
            return "随时可约";
        }
        String[] dayNames = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        StringBuilder sb = new StringBuilder();
        for (SkillAvailableTime at : availableTimes) {
            // All records in database are enabled (filtered by deleted=0 in query)
            if (!sb.isEmpty()) sb.append("、");
            int day = at.getDayOfWeek() != null ? at.getDayOfWeek() : 0;
            sb.append(dayNames[day % 7]);
        }
        return sb.isEmpty() ? "随时可约" : sb.toString();
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
        // 1. 查询技能提供者信息
        User provider = userService.getById(targetUserId);
        if (provider == null) {
            log.warn("技能提供者不存在: targetUserId={}", targetUserId);
            return UserSkillsPageResult.builder()
                .list(new ArrayList<>())
                .total(0L)
                .hasMore(false)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
        }

        // 2. 计算分页偏移
        int offset = (pageNum - 1) * pageSize;

        // 3. 查询用户技能列表
        List<Skill> skills = skillMapper.selectSkillsByUserId(targetUserId, offset, pageSize);

        // 4. 统计总数
        long total = skillMapper.countSkillsByUserId(targetUserId);

        // 5. 转换为VO（包含提供者信息）
        List<UserSkillVo> skillVos = skills.stream()
            .map(skill -> convertToUserSkillVo(skill, provider))
            .collect(Collectors.toList());

        // 6. 构建返回结果
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
     * 转换技能为VO（包含提供者信息）
     */
    private UserSkillVo convertToUserSkillVo(Skill skill, User provider) {
        return UserSkillVo.builder()
            .skillId(skill.getSkillId())
            .skillName(skill.getSkillName())
            .skillType(skill.getSkillType())
            .coverImage(skill.getCoverImage())
            .description(skill.getDescription())
            .mediaData(UserSkillVo.MediaData.builder()
                .coverUrl(skill.getCoverImage())
                .build())
            .providerData(UserSkillVo.ProviderData.builder()
                .userId(provider.getUserId())
                .nickname(provider.getNickname())
                .avatar(provider.getAvatar())
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
