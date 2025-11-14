package org.dromara.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.user.domain.entity.AppUser;
import org.dromara.user.mapper.AppUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * App用户远程服务实现类
 *
 * <p>使用 MyBatis Plus LambdaQueryWrapper 实现所有数据库操作，无需 XML</p>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteAppUserServiceImpl implements RemoteAppUserService {

    private final AppUserMapper appUserMapper;

    // ==================== 登录相关 ====================

    @Override
    public AppLoginUser getUserByMobile(String mobile, String countryCode) throws UserException {
        log.info("查询用户：mobile={}, countryCode={}", mobile, countryCode);

        // 使用 LambdaQueryWrapper 查询
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getMobile, mobile)
               .eq(AppUser::getCountryCode, countryCode);

        AppUser user = appUserMapper.selectOne(wrapper);

        if (user == null) {
            throw new UserException("user.not.exists", mobile);
        }

        return convertToLoginUser(user);
    }

    @Override
    public AppLoginUser getUserById(Long userId) throws UserException {
        log.info("查询用户：userId={}", userId);

        AppUser user = appUserMapper.selectById(userId);

        if (user == null) {
            throw new UserException("user.not.exists", userId);
        }

        return convertToLoginUser(user);
    }

    // ==================== 注册相关 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppLoginUser registerOrGetByMobile(String mobile, String countryCode) throws ServiceException {
        log.info("注册或获取用户：mobile={}, countryCode={}", mobile, countryCode);

        // 1. 使用 LambdaQueryWrapper 查询是否已存在
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getMobile, mobile)
               .eq(AppUser::getCountryCode, countryCode);

        AppUser existingUser = appUserMapper.selectOne(wrapper);

        // 2. 如果已存在，直接返回
        if (existingUser != null) {
            log.info("用户已存在，直接登录：userId={}, isNewUser={}", existingUser.getUserId(), existingUser.getIsNewUser());
            return convertToLoginUser(existingUser);
        }

        // 3. 不存在，创建新用户（自动注册）
        AppUser newUser = AppUser.builder()
            .mobile(mobile)
            .countryCode(countryCode)
            .nickname(AppUser.generateDefaultNickname(mobile))  // 131****6323
            .password(null)  // 首次注册无密码
            .status(1)  // 正常状态
            .isNewUser(true)  // 新用户标记
            .loginCount(0)
            .build();

        try {
            int rows = appUserMapper.insert(newUser);
            if (rows > 0) {
                log.info("自动注册成功：userId={}, mobile={}", newUser.getUserId(), mobile);
                return convertToLoginUser(newUser);
            } else {
                throw new ServiceException("用户注册失败");
            }
        } catch (Exception e) {
            log.error("自动注册失败：mobile={}, error={}", mobile, e.getMessage(), e);
            throw new ServiceException("用户注册失败：" + e.getMessage());
        }
    }

    // ==================== 密码相关 ====================

    @Override
    public boolean checkPassword(Long userId, String rawPassword) {
        log.info("校验密码：userId={}", userId);

        if (StringUtils.isEmpty(rawPassword)) {
            return false;
        }

        // 使用 LambdaQueryWrapper 只查询密码字段（性能优化）
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getUserId, userId)
               .select(AppUser::getPassword);

        AppUser user = appUserMapper.selectOne(wrapper);

        if (user == null || user.getPassword() == null) {
            log.warn("用户不存在或未设置密码：userId={}", userId);
            return false;
        }

        // BCrypt 密码校验
        return BCrypt.checkpw(rawPassword, user.getPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setPassword(String mobile, String countryCode, String newPassword) throws UserException {
        log.info("设置密码：mobile={}", mobile);

        // 校验密码格式
        validatePassword(newPassword);

        // 使用 LambdaUpdateWrapper 更新密码
        LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AppUser::getMobile, mobile)
               .eq(AppUser::getCountryCode, countryCode)
               .set(AppUser::getPassword, BCrypt.hashpw(newPassword));  // BCrypt 加密

        int rows = appUserMapper.update(null, wrapper);

        if (rows == 0) {
            throw new UserException("user.not.exists", mobile);
        }

        log.info("密码设置成功：mobile={}", mobile);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String mobile, String countryCode, String newPassword) throws UserException {
        log.info("重置密码：mobile={}", mobile);

        // 重置密码与设置密码逻辑相同
        return setPassword(mobile, countryCode, newPassword);
    }

    // ==================== 登录信息更新 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastLoginInfo(Long userId, String loginIp) {
        log.info("更新登录信息：userId={}, ip={}", userId, loginIp);

        // 使用 LambdaUpdateWrapper 更新登录信息 + 登录次数自增
        LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AppUser::getUserId, userId)
               .set(AppUser::getLastLoginTime, LocalDateTime.now())
               .set(AppUser::getLastLoginIp, loginIp)
               .setSql("login_count = login_count + 1");  // SQL 表达式自增

        int rows = appUserMapper.update(null, wrapper);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsOldUser(Long userId) {
        log.info("标记为老用户：userId={}", userId);

        // 使用 LambdaUpdateWrapper 更新 is_new_user 标记
        LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AppUser::getUserId, userId)
               .set(AppUser::getIsNewUser, false);

        int rows = appUserMapper.update(null, wrapper);
        return rows > 0;
    }

    // ==================== 账号状态管理 ====================

    @Override
    public boolean existsByMobile(String mobile, String countryCode) {
        log.debug("检查用户是否存在：mobile={}, countryCode={}", mobile, countryCode);

        // 使用 LambdaQueryWrapper 统计数量
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getMobile, mobile)
               .eq(AppUser::getCountryCode, countryCode);

        Long count = appUserMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    @Override
    public Integer getUserStatus(Long userId) throws UserException {
        log.debug("查询用户状态：userId={}", userId);

        // 只查询 status 字段（性能优化）
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getUserId, userId)
               .select(AppUser::getStatus);

        AppUser user = appUserMapper.selectOne(wrapper);

        if (user == null) {
            throw new UserException("user.not.exists", userId);
        }

        return user.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableUser(Long userId, String reason) {
        log.warn("禁用用户：userId={}, reason={}", userId, reason);

        // 使用 LambdaUpdateWrapper 更新状态
        LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AppUser::getUserId, userId)
               .set(AppUser::getStatus, 0);  // 0=禁用

        int rows = appUserMapper.update(null, wrapper);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableUser(Long userId) {
        log.info("启用用户：userId={}", userId);

        // 使用 LambdaUpdateWrapper 更新状态
        LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AppUser::getUserId, userId)
               .set(AppUser::getStatus, 1);  // 1=正常

        int rows = appUserMapper.update(null, wrapper);
        return rows > 0;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 将 AppUser 实体转换为 AppLoginUser 登录对象
     */
    private AppLoginUser convertToLoginUser(AppUser user) {
        AppLoginUser loginUser = new AppLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setMobile(user.getMobile());
        loginUser.setCountryCode(user.getCountryCode());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setGender(user.getGender());
        loginUser.setStatus(user.getStatus());
        loginUser.setIsNewUser(user.getIsNewUser());
        loginUser.setPassword(user.getPassword());
        return loginUser;
    }

    /**
     * 校验密码格式
     *
     * @param password 密码
     * @throws ServiceException 格式不正确时抛出异常
     */
    private void validatePassword(String password) throws ServiceException {
        if (StringUtils.isEmpty(password)) {
            throw new ServiceException("密码不能为空");
        }

        // 长度检查：6-20位
        if (password.length() < 6 || password.length() > 20) {
            throw new ServiceException("密码长度必须在6-20位之间");
        }

        // 不可纯数字
        if (password.matches("^\\d+$")) {
            throw new ServiceException("密码不能为纯数字");
        }
    }
}
