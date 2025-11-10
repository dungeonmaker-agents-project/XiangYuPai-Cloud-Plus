package com.xypai.user.service;

import com.xypai.user.domain.dto.AutoRegisterDTO;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.dto.UserValidateDTO;
import com.xypai.user.domain.vo.AuthUserVO;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IUserService {

    /**
     * 查询用户列表
     */
    List<UserListVO> selectUserList(UserQueryDTO query);

    /**
     * 根据用户ID查询用户详情
     */
    UserDetailVO selectUserById(Long userId);

    /**
     * 新增用户
     */
    boolean insertUser(UserAddDTO userAddDTO);

    /**
     * 修改用户
     */
    boolean updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 批量删除用户
     */
    boolean deleteUserByIds(List<Long> userIds);

    /**
     * 获取当前用户信息
     */
    UserDetailVO selectCurrentUser();

    /**
     * 更新当前用户信息
     */
    boolean updateCurrentUser(UserUpdateDTO userUpdateDTO);

    /**
     * 重置用户密码
     */
    boolean resetUserPassword(Long userId);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 检查用户名是否唯一
     */
    boolean checkUsernameUnique(String username, Long userId);

    /**
     * 检查手机号是否唯一
     */
    boolean checkMobileUnique(String mobile, Long userId);

    // ========== 认证服务专用接口 ==========

    /**
     * 根据用户名获取认证用户信息
     */
    AuthUserVO selectAuthUserByUsername(String username);

    /**
     * 根据手机号获取认证用户信息
     */
    AuthUserVO selectAuthUserByMobile(String mobile);

    /**
     * 验证用户密码
     */
    boolean validateUserPassword(UserValidateDTO validateDTO);

    /**
     * 更新用户最后登录时间
     */
    boolean updateLastLoginTime(Long userId);

    /**
     * 根据用户名查询用户
     */
    UserDetailVO selectUserByUsername(String username);

    /**
     * 根据手机号查询用户
     */
    UserDetailVO selectUserByMobile(String mobile);

    /**
     * 用户注册
     */
    boolean registerUser(UserAddDTO userAddDTO);

    /**
     * 验证用户密码
     */
    boolean validatePassword(Long userId, String password);

    /**
     * 更新用户密码
     */
    boolean updatePassword(Long userId, String newPassword);

    /**
     * 激活用户
     */
    boolean activateUser(Long userId);

    /**
     * 冻结用户
     */
    boolean freezeUser(Long userId, String reason);

    /**
     * 解冻用户
     */
    boolean unfreezeUser(Long userId);

    /**
     * 短信登录自动注册用户
     */
    AuthUserVO autoRegisterUser(AutoRegisterDTO autoRegisterDTO);
}
