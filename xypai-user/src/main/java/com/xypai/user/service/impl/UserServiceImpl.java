package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.user.domain.dto.AutoRegisterDTO;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.dto.UserValidateDTO;
import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfileNew;
import com.xypai.user.domain.entity.UserWallet;
import com.xypai.user.domain.vo.AuthUserVO;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.mapper.UserMapper;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.mapper.UserWalletMapper;
import com.xypai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户服务实现�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserWalletMapper userWalletMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<UserListVO> selectUserList(UserQueryDTO query) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .like(StringUtils.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
                .like(StringUtils.isNotBlank(query.getMobile()), User::getMobile, query.getMobile())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        User::getCreatedAt, query.getBeginTime(), query.getEndTime())
                .orderByDesc(User::getCreatedAt);

        List<User> users = userMapper.selectList(queryWrapper);
        List<UserListVO> result = new ArrayList<>();
        
        for (User user : users) {
            UserListVO vo = convertToListVO(user);
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public UserDetailVO selectUserById(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        return convertToDetailVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(UserAddDTO userAddDTO) {
        // 校验用户名和手机号唯一�?
        if (!checkUsernameUnique(userAddDTO.getUsername(), null)) {
            throw new ServiceException("用户名已存在");
        }
        if (!checkMobileUnique(userAddDTO.getMobile(), null)) {
            throw new ServiceException("手机号已被注册");
        }

        // 创建用户基础信息
        User user = User.builder()
                .username(userAddDTO.getUsername())
                .mobile(userAddDTO.getMobile())
                .password(passwordEncoder.encode(userAddDTO.getPassword()))
                .status(userAddDTO.getStatus() != null ? userAddDTO.getStatus() : 1)
                .createdAt(LocalDateTime.now())
                .build();

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new ServiceException("创建用户失败");
        }

        // 创建用户资料扩展信息
        createUserProfile(user.getId(), userAddDTO);
        
        // 创建用户钱包
        createUserWallet(user.getId());

        log.info("创建用户成功，用户ID：{}", user.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO.getId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        User existUser = userMapper.selectById(userUpdateDTO.getId());
        if (existUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 校验用户名和手机号唯一�?
        if (StringUtils.isNotBlank(userUpdateDTO.getUsername()) &&
                !checkUsernameUnique(userUpdateDTO.getUsername(), userUpdateDTO.getId())) {
            throw new ServiceException("用户名已存在");
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getMobile()) &&
                !checkMobileUnique(userUpdateDTO.getMobile(), userUpdateDTO.getId())) {
            throw new ServiceException("手机号已被注册");
        }

        // 更新用户基础信息
        User updateUser = User.builder()
                .id(userUpdateDTO.getId())
                .username(userUpdateDTO.getUsername())
                .mobile(userUpdateDTO.getMobile())
                .status(userUpdateDTO.getStatus())
                .version(userUpdateDTO.getVersion())
                .build();

        int result = userMapper.updateById(updateUser);
        if (result <= 0) {
            throw new ServiceException("更新用户失败");
        }

        // 更新用户资料信息
        updateUserProfile(userUpdateDTO.getId(), userUpdateDTO);

        log.info("更新用户成功，用户ID：{}", userUpdateDTO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        LambdaQueryWrapper<User> deleteWrapper = Wrappers.lambdaQuery(User.class)
                .in(User::getId, userIds);
        int result = userMapper.delete(deleteWrapper);
        log.info("批量删除用户成功，删除数量：{}", result);
        return result > 0;
    }

    @Override
    public UserDetailVO selectCurrentUser() {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }
        return selectUserById(currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCurrentUser(UserUpdateDTO userUpdateDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }
        userUpdateDTO.setId(currentUserId);
        return updateUser(userUpdateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserPassword(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        String defaultPassword = "123456"; // 默认密码
        User updateUser = User.builder()
                .id(userId)
                .password(passwordEncoder.encode(defaultPassword))
                .build();

        int result = userMapper.updateById(updateUser);
        log.info("重置用户密码成功，用户ID：{}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (status == null) {
            throw new ServiceException("用户状态不能为空");
        }

        User updateUser = User.builder()
                .id(userId)
                .status(status)
                .build();

        int result = userMapper.updateById(updateUser);
        log.info("更新用户状态成功，用户ID：{}，状态：{}", userId, status);
        return result > 0;
    }

    @Override
    public boolean checkUsernameUnique(String username, Long userId) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .ne(userId != null, User::getId, userId);

        return userMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public boolean checkMobileUnique(String mobile, Long userId) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, mobile)
                .ne(userId != null, User::getId, userId);

        return userMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public UserDetailVO selectUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new ServiceException("用户名不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        return convertToDetailVO(user);
    }

    @Override
    public UserDetailVO selectUserByMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            throw new ServiceException("手机号不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, mobile);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        return convertToDetailVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(UserAddDTO userAddDTO) {
        // 用户注册默认为正常状�?
        userAddDTO.setStatus(1);
        return insertUser(userAddDTO);
    }

    @Override
    public boolean validatePassword(Long userId, String password) {
        if (userId == null || StringUtils.isBlank(password)) {
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String newPassword) {
        if (userId == null || StringUtils.isBlank(newPassword)) {
            throw new ServiceException("参数不能为空");
        }

        User updateUser = User.builder()
                .id(userId)
                .password(passwordEncoder.encode(newPassword))
                .build();

        int result = userMapper.updateById(updateUser);
        log.info("更新用户密码成功，用户ID：{}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateUser(Long userId) {
        return updateUserStatus(userId, User.Status.NORMAL.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeUser(Long userId, String reason) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        boolean result = updateUserStatus(userId, User.Status.FROZEN.getCode());
        if (result) {
            log.info("冻结用户成功，用户ID：{}，原因：{}", userId, reason);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeUser(Long userId) {
        return updateUserStatus(userId, User.Status.NORMAL.getCode());
    }

    /**
     * 转换为列表VO
     */
    private UserListVO convertToListVO(User user) {
        UserProfileNew profile = userProfileMapper.selectById(user.getId());
        
        return UserListVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .nickname(profile != null ? profile.getNickname() : null)
                .avatar(profile != null ? profile.getAvatar() : null)
                .status(user.getStatus())
                .statusDesc(user.getStatusDesc())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 转换为详情VO
     */
    private UserDetailVO convertToDetailVO(User user) {
        UserProfileNew profile = userProfileMapper.selectById(user.getId());
        UserWallet wallet = userWalletMapper.selectById(user.getId());
        
        return UserDetailVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .nickname(profile != null ? profile.getNickname() : null)
                .avatar(profile != null ? profile.getAvatar() : null)
                .email(user.getEmail())
                .realName(profile != null ? profile.getRealName() : null)
                .location(profile != null ? profile.getLocation() : null)
                .bio(profile != null ? profile.getBio() : null)
                .status(user.getStatus())
                .statusDesc(user.getStatusDesc())
                .createdAt(user.getCreatedAt())
                .version(user.getVersion())
                .walletBalance(wallet != null ? wallet.getFormattedBalance() : "¥0.00")
                .followed(false) // TODO: 根据当前用户查询关注状�?
                .followingCount(0L) // TODO: 查询关注�?
                .followersCount(0L) // TODO: 查询粉丝�?
                .build();
    }

    /**
     * 创建用户资料
     */
    private void createUserProfile(Long userId, UserAddDTO userAddDTO) {
        UserProfileNew profile = UserProfileNew.builder()
                .userId(userId)
                .nickname(userAddDTO.getNickname())
                .avatar(userAddDTO.getAvatar())
                .realName(userAddDTO.getRealName())
                .location(userAddDTO.getLocation())
                .bio(userAddDTO.getBio())
                .gender(0)
                .onlineStatus(0)
                .profileCompleteness(0)
                .isRealVerified(false)
                .isGodVerified(false)
                .isActivityExpert(false)
                .isVip(false)
                .isPopular(false)
                .vipLevel(0)
                .build();

        userProfileMapper.insert(profile);
    }

    /**
     * 更新用户资料
     */
    private void updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO) {
        UserProfileNew existProfile = userProfileMapper.selectById(userId);
        if (existProfile == null) {
            // 如果不存在资料，则创�?
            createUserProfile(userId, convertToAddDTO(userUpdateDTO));
            return;
        }

        // 更新字段
        if (StringUtils.isNotBlank(userUpdateDTO.getNickname())) {
            existProfile.setNickname(userUpdateDTO.getNickname());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getAvatar())) {
            existProfile.setAvatar(userUpdateDTO.getAvatar());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getRealName())) {
            existProfile.setRealName(userUpdateDTO.getRealName());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getLocation())) {
            existProfile.setLocation(userUpdateDTO.getLocation());
        }
        if (StringUtils.isNotBlank(userUpdateDTO.getBio())) {
            existProfile.setBio(userUpdateDTO.getBio());
        }

        userProfileMapper.updateById(existProfile);
    }

    /**
     * 创建用户钱包
     */
    private void createUserWallet(Long userId) {
        UserWallet wallet = UserWallet.builder()
                .userId(userId)
                .balance(0L) // 初始余额�?
                .build();

        userWalletMapper.insert(wallet);
    }

    /**
     * 转换UpdateDTO为AddDTO（用于补充资料）
     */
    private UserAddDTO convertToAddDTO(UserUpdateDTO updateDTO) {
        return UserAddDTO.builder()
                .nickname(updateDTO.getNickname())
                .email(updateDTO.getEmail())
                .avatar(updateDTO.getAvatar())
                .realName(updateDTO.getRealName())
                .location(updateDTO.getLocation())
                .bio(updateDTO.getBio())
                .build();
    }

    // ========== 认证服务专用接口实现 ==========

    @Override
    public AuthUserVO selectAuthUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            return null;
        }

        return buildAuthUserVO(user);
    }

    @Override
    public AuthUserVO selectAuthUserByMobile(String mobile) {
        log.debug("🔍 查询用户: mobile={}", mobile);
        
        try {
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                    .eq(User::getMobile, mobile);
            User user = userMapper.selectOne(queryWrapper);
            
            if (user == null) {
                log.warn("⚠️ 用户不存在: mobile={}", mobile);
                return null;
            }

            log.debug("✅ 找到用户: userId={}, username={}, mobile={}", 
                    user.getId(), user.getUsername(), user.getMobile());
            
            AuthUserVO authUserVO = buildAuthUserVO(user);
            log.debug("✅ 构建AuthUserVO成功: userId={}", authUserVO.getId());
            
            return authUserVO;
        } catch (Exception e) {
            log.error("❌ 查询用户异常: mobile={}, error={}", mobile, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean validateUserPassword(UserValidateDTO validateDTO) {
        log.debug("🔐 开始验证密码: identifier={}", validateDTO.getUsername());
        
        // 1. 获取用户信息
        User user = null;
        if (validateDTO.getUsername().matches("^1[3-9]\\d{9}$")) {
            log.debug("🔍 使用手机号查询: mobile={}", validateDTO.getUsername());
            // 手机号查�?
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                    .eq(User::getMobile, validateDTO.getUsername());
            user = userMapper.selectOne(queryWrapper);
        } else {
            log.debug("🔍 使用用户名查询: username={}", validateDTO.getUsername());
            // 用户名查�?
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                    .eq(User::getUsername, validateDTO.getUsername());
            user = userMapper.selectOne(queryWrapper);
        }

        if (user == null) {
            log.warn("❌ 用户不存在: identifier={}", validateDTO.getUsername());
            return false;
        }

        log.warn("✅ 找到用户: userId={}, username={}", user.getId(), user.getUsername());
        log.warn("🔑 密码信息: 输入密码长度={}, 输入密码前3字符={}, 数据库哈希长度={}, 数据库哈希前缀={}", 
                validateDTO.getPassword() != null ? validateDTO.getPassword().length() : 0,
                validateDTO.getPassword() != null && validateDTO.getPassword().length() >= 3 
                    ? validateDTO.getPassword().substring(0, 3) + "..." : "null",
                user.getPassword() != null ? user.getPassword().length() : 0,
                user.getPassword() != null && user.getPassword().length() > 10 
                    ? user.getPassword().substring(0, 10) : "null");

        // 2. 验证密码
        boolean matches = passwordEncoder.matches(validateDTO.getPassword(), user.getPassword());
        
        if (matches) {
            log.info("✅ 密码验证成功: userId={}, username={}", user.getId(), user.getUsername());
        } else {
            log.warn("❌ 密码验证失败: userId={}, username={}, 请检查数据库密码哈希是否正确！", user.getId(), user.getUsername());
        }
        
        return matches;
    }

    @Override
    public boolean updateLastLoginTime(Long userId) {
        User updateUser = User.builder()
                .id(userId)
                .build();
        
        // 这里需要添加最后登录时间字段到User实体
        // updateUser.setLastLoginTime(LocalDateTime.now());
        
        int result = userMapper.updateById(updateUser);
        return result > 0;
    }

    /**
     * 构建认证用户VO
     */
    private AuthUserVO buildAuthUserVO(User user) {
        log.debug("🔨 构建AuthUserVO: userId={}, username={}", user.getId(), user.getUsername());
        
        try {
            // 获取用户资料
            UserProfileNew profile = userProfileMapper.selectById(user.getId());
            
            if (profile == null) {
                log.warn("⚠️ 用户资料不存在: userId={}, 将使用默认值", user.getId());
            } else {
                log.debug("✅ 找到用户资料: userId={}, nickname={}", user.getId(), profile.getNickname());
            }
            
            // 构建基础角色和权�?
            Set<String> roles = Set.of("USER");
            Set<String> permissions = Set.of("user:read");
            
            // 根据用户名判断是否为管理员（简化逻辑�?
            if ("admin".equals(user.getUsername())) {
                roles = Set.of("ADMIN", "USER");
                permissions = Set.of("user:read", "user:write", "admin:all", "system:config");
            }

            AuthUserVO authUserVO = AuthUserVO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .mobile(user.getMobile())
                    .nickname(profile != null ? profile.getNickname() : user.getUsername())
                    .avatar(profile != null ? profile.getAvatar() : null)
                    .status(user.getStatus())
                    .roles(roles)
                    .permissions(permissions)
                    .lastLoginTime(null) // 需要添加字�?
                    .createdAt(user.getCreatedAt())
                    .build();
            
            log.debug("✅ AuthUserVO构建完成: userId={}, username={}, mobile={}", 
                    authUserVO.getId(), authUserVO.getUsername(), authUserVO.getMobile());
            
            return authUserVO;
        } catch (Exception e) {
            log.error("❌ 构建AuthUserVO异常: userId={}, error={}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthUserVO autoRegisterUser(AutoRegisterDTO autoRegisterDTO) {
        log.info("开始自动注册用�? mobile={}, source={}", autoRegisterDTO.getMobile(), autoRegisterDTO.getSource());
        
        try {
            // 1. 检查手机号是否已存�?
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class);
            queryWrapper.eq(User::getMobile, autoRegisterDTO.getMobile());
            User existingUser = userMapper.selectOne(queryWrapper);
            
            if (existingUser != null) {
                log.warn("用户已存在，返回现有用户信息: mobile={}, userId={}", autoRegisterDTO.getMobile(), existingUser.getId());
                return buildAuthUserVO(existingUser);
            }

            // 2. 生成用户名（使用手机号）
            String username = generateUsernameFromMobile(autoRegisterDTO.getMobile());
            
            // 3. 创建用户
            User user = User.builder()
                    .username(username)
                    .mobile(autoRegisterDTO.getMobile())
                    .password(null) // 短信注册时密码为空，后续可以设置
                    .status(1) // 正常状�?
                    .build();

            int result = userMapper.insert(user);
            if (result <= 0) {
                throw new ServiceException("创建用户失败");
            }

            // 4. 创建用户资料
            UserProfileNew profile = UserProfileNew.builder()
                    .userId(user.getId())
                    .nickname("用户" + user.getId()) // 默认昵称
                    .avatar(null)
                    .gender(0)
                    .onlineStatus(0)
                    .profileCompleteness(0)
                    .isRealVerified(false)
                    .isGodVerified(false)
                    .isActivityExpert(false)
                    .isVip(false)
                    .isPopular(false)
                    .vipLevel(0)
                    .build();

            userProfileMapper.insert(profile);

            // 5. 创建用户钱包
            UserWallet wallet = UserWallet.builder()
                    .userId(user.getId())
                    .balance(0L) // 初始余额�?�?
                    .build();

            userWalletMapper.insert(wallet);

            log.info("自动注册用户成功: mobile={}, userId={}, username={}", 
                    autoRegisterDTO.getMobile(), user.getId(), username);

            // 6. 返回认证用户信息
            return buildAuthUserVO(user);

        } catch (Exception e) {
            log.error("自动注册用户失败: mobile={}, error={}", autoRegisterDTO.getMobile(), e.getMessage(), e);
            throw new ServiceException("自动注册失败: " + e.getMessage());
        }
    }

    /**
     * 根据手机号生成唯一用户�?
     */
    private String generateUsernameFromMobile(String mobile) {
        // 基础用户名：手机�?
        String baseUsername = mobile;
        
        // 检查是否已存在
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class);
        queryWrapper.eq(User::getUsername, baseUsername);
        User existingUser = userMapper.selectOne(queryWrapper);
        
        if (existingUser == null) {
            return baseUsername;
        }
        
        // 如果已存在，添加时间戳后缀
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        String uniqueUsername = "u" + mobile + "_" + timestamp;
        
        // 再次检查唯一性（理论上不会重复）
        queryWrapper.clear();
        queryWrapper.eq(User::getUsername, uniqueUsername);
        if (userMapper.selectOne(queryWrapper) != null) {
            // 极端情况，使用UUID
            uniqueUsername = "u" + mobile + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        
        return uniqueUsername;
    }
}
