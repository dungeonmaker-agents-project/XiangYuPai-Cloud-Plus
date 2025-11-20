package org.dromara.xypai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.xypai.auth.domain.entity.XyUser;

/**
 * XyUser Mapper接口
 *
 * @author XiangYuPai
 * @since 2025-11-17
 */
public interface XyUserMapper extends BaseMapper<XyUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户信息
     */
    XyUser selectByUsername(@Param("username") String username, @Param("tenantId") String tenantId);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @param tenantId 租户ID
     * @return 用户信息
     */
    XyUser selectByEmail(@Param("email") String email, @Param("tenantId") String tenantId);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @param tenantId 租户ID
     * @return 用户信息
     */
    XyUser selectByMobile(@Param("mobile") String mobile, @Param("tenantId") String tenantId);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 用户信息
     */
    XyUser selectByUserId(@Param("userId") Long userId, @Param("tenantId") String tenantId);

    /**
     * 根据OpenID查询用户
     *
     * @param openid OpenID
     * @return 用户信息
     */
    XyUser selectByOpenid(@Param("openid") String openid);

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int updateLastLoginTime(@Param("userId") Long userId);

    /**
     * 更新用户在线状态
     *
     * @param userId 用户ID
     * @param onlineStatus 在线状态
     * @return 影响行数
     */
    int updateOnlineStatus(@Param("userId") Long userId, @Param("onlineStatus") String onlineStatus);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 数量
     */
    int checkUsernameExists(@Param("username") String username, @Param("tenantId") String tenantId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @param tenantId 租户ID
     * @return 数量
     */
    int checkEmailExists(@Param("email") String email, @Param("tenantId") String tenantId);

    /**
     * 检查手机号是否存在
     *
     * @param mobile 手机号
     * @param tenantId 租户ID
     * @return 数量
     */
    int checkMobileExists(@Param("mobile") String mobile, @Param("tenantId") String tenantId);
}
