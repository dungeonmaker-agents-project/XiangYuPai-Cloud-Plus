package org.dromara.user.controller.feign;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.user.domain.entity.User;
import org.dromara.user.service.IUserService;
import org.springframework.stereotype.Service;

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
}
