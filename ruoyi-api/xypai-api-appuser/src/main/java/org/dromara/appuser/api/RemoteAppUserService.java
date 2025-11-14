package org.dromara.appuser.api;

import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;

/**
 * App用户远程服务接口
 *
 * <p>用途：xypai-auth通过Dubbo调用此接口进行用户认证和管理</p>
 * <p>实现：xypai-user模块实现此接口</p>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
public interface RemoteAppUserService {

    // ==================== 登录相关 ====================

    /**
     * 通过手机号查询用户信息（用于登录）
     *
     * @param mobile      手机号
     * @param countryCode 国家区号（例如："+86"）
     * @return 用户登录信息
     * @throws UserException 用户不存在或已禁用
     */
    AppLoginUser getUserByMobile(String mobile, String countryCode) throws UserException;

    /**
     * 通过用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户登录信息
     * @throws UserException 用户不存在
     */
    AppLoginUser getUserById(Long userId) throws UserException;

    // ==================== 注册相关 ====================

    /**
     * 手机号注册（SMS验证码登录时自动注册）
     *
     * <p>流程：</p>
     * <ul>
     *     <li>1. 检查手机号是否已注册</li>
     *     <li>2. 已注册 → 返回现有用户信息（isNewUser=false）</li>
     *     <li>3. 未注册 → 创建新用户 → 返回用户信息（isNewUser=true）</li>
     * </ul>
     *
     * @param mobile      手机号
     * @param countryCode 国家区号
     * @return 用户登录信息（包含isNewUser标记）
     * @throws ServiceException 注册失败
     */
    AppLoginUser registerOrGetByMobile(String mobile, String countryCode) throws ServiceException;

    // ==================== 密码相关 ====================

    /**
     * 校验密码是否正确
     *
     * @param userId       用户ID
     * @param rawPassword  明文密码
     * @return true=密码正确，false=密码错误
     */
    boolean checkPassword(Long userId, String rawPassword);

    /**
     * 设置用户密码（首次设置或重置密码）
     *
     * @param mobile       手机号
     * @param countryCode  国家区号
     * @param newPassword  新密码（明文，后端负责BCrypt加密）
     * @return true=设置成功，false=设置失败
     * @throws UserException 用户不存在
     */
    boolean setPassword(String mobile, String countryCode, String newPassword) throws UserException;

    /**
     * 重置密码（忘记密码流程）
     *
     * @param mobile       手机号
     * @param countryCode  国家区号
     * @param newPassword  新密码（明文）
     * @return true=重置成功，false=重置失败
     * @throws UserException 用户不存在
     */
    boolean resetPassword(String mobile, String countryCode, String newPassword) throws UserException;

    // ==================== 登录信息更新 ====================

    /**
     * 更新最后登录信息
     *
     * @param userId   用户ID
     * @param loginIp  登录IP
     * @return true=更新成功
     */
    boolean updateLastLoginInfo(Long userId, String loginIp);

    /**
     * 标记用户为老用户（完成资料后调用）
     *
     * @param userId 用户ID
     * @return true=更新成功
     */
    boolean markAsOldUser(Long userId);

    // ==================== 账号状态管理 ====================

    /**
     * 检查用户是否存在
     *
     * @param mobile      手机号
     * @param countryCode 国家区号
     * @return true=存在，false=不存在
     */
    boolean existsByMobile(String mobile, String countryCode);

    /**
     * 检查用户账号状态
     *
     * @param userId 用户ID
     * @return 状态：0=禁用，1=正常，2=锁定
     * @throws UserException 用户不存在
     */
    Integer getUserStatus(Long userId) throws UserException;

    /**
     * 禁用用户账号
     *
     * @param userId 用户ID
     * @param reason 禁用原因
     * @return true=禁用成功
     */
    boolean disableUser(Long userId, String reason);

    /**
     * 启用用户账号
     *
     * @param userId 用户ID
     * @return true=启用成功
     */
    boolean enableUser(Long userId);
}
