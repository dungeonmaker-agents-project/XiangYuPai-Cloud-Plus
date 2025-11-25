package org.dromara.user.controller.feign;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.dto.FilterQueryDto;
import org.dromara.appuser.api.domain.vo.FilterConfigVo;
import org.dromara.appuser.api.domain.vo.FilterUserPageResult;
import org.dromara.appuser.api.domain.vo.FilterUserVo;
import org.dromara.appuser.api.domain.vo.LimitedTimePageResult;
import org.dromara.appuser.api.domain.vo.LimitedTimeUserVo;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.user.domain.entity.User;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.service.IUserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * App用户远程服务实现（Dubbo RPC Provider）
 * Remote App User Service Implementation
 *
 * <p>为xypai-auth提供RPC接口，实现用户认证和管理</p>
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteAppUserServiceImpl implements RemoteAppUserService {

    private final IUserService userService;
    private final UserMapper userMapper;

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
}
