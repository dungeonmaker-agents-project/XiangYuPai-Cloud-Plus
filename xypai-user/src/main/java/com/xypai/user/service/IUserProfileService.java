package com.xypai.user.service;

import com.xypai.user.domain.dto.UserProfileUpdateDTO;
import com.xypai.user.domain.vo.ProfileCompletenessVO;
import com.xypai.user.domain.vo.UserProfileVO;

/**
 * 用户资料服务接口
 *
 * @author Bob
 * @date 2025-01-14
 */
public interface IUserProfileService {

    /**
     * 根据用户ID查询资料
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 更新用户资料
     */
    boolean updateUserProfile(Long userId, UserProfileUpdateDTO updateDTO);

    /**
     * 获取资料完整度信息
     */
    ProfileCompletenessVO getProfileCompleteness(Long userId);

    /**
     * 更新在线状态
     */
    boolean updateOnlineStatus(Long userId, Integer onlineStatus);

    /**
     * 用户上线
     */
    boolean goOnline(Long userId);

    /**
     * 用户离线
     */
    boolean goOffline(Long userId);

    /**
     * 用户隐身
     */
    boolean goInvisible(Long userId);

    /**
     * 检查用户是否在线（5分钟内活跃）
     */
    boolean isUserOnline(Long userId);

    /**
     * 实名认证通过（更新认证状态）
     */
    boolean markRealVerified(Long userId, String realName, String idCard);

    /**
     * 大神认证通过
     */
    boolean markGodVerified(Long userId);

    /**
     * 组局达人认证通过
     */
    boolean markActivityExpert(Long userId);

    /**
     * 设置VIP（更新VIP状态）
     */
    boolean setVip(Long userId, Integer vipLevel, Integer durationDays);

    /**
     * 取消VIP
     */
    boolean cancelVip(Long userId);

    /**
     * 检查VIP是否有效
     */
    boolean isVipValid(Long userId);

    /**
     * 标记为人气用户
     */
    boolean markAsPopular(Long userId);

    /**
     * 取消人气用户标记
     */
    boolean unmarkAsPopular(Long userId);

    /**
     * 批量查询用户资料
     */
    java.util.List<UserProfileVO> getBatchUserProfiles(java.util.List<Long> userIds);

    /**
     * 更新IP归属地
     */
    boolean updateIpLocation(Long userId, String ipLocation);
}

