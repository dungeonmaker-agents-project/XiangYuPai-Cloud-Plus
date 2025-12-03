package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.UnlockWechatDTO;
import org.dromara.appbff.domain.vo.OtherUserProfileVO;
import org.dromara.appbff.domain.vo.ProfileInfoVO;
import org.dromara.appbff.domain.vo.UnlockWechatResultVO;
import org.dromara.appbff.domain.vo.UserSkillsListVO;

/**
 * 对方主页服务接口
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
public interface OtherUserProfileService {

    /**
     * 获取对方主页数据
     *
     * @param targetUserId  目标用户ID
     * @param currentUserId 当前用户ID
     * @param latitude      当前用户纬度（可选）
     * @param longitude     当前用户经度（可选）
     * @return 对方主页数据
     */
    OtherUserProfileVO getOtherUserProfile(Long targetUserId, Long currentUserId, Double latitude, Double longitude);

    /**
     * 获取用户资料详情
     *
     * @param targetUserId  目标用户ID
     * @param currentUserId 当前用户ID
     * @return 用户资料详情
     */
    ProfileInfoVO getProfileInfo(Long targetUserId, Long currentUserId);

    /**
     * 获取用户技能列表
     *
     * @param targetUserId  目标用户ID
     * @param currentUserId 当前用户ID
     * @param pageNum       页码
     * @param pageSize      每页数量
     * @return 技能列表
     */
    UserSkillsListVO getUserSkills(Long targetUserId, Long currentUserId, Integer pageNum, Integer pageSize);

    /**
     * 解锁微信
     *
     * @param currentUserId 当前用户ID
     * @param dto           解锁请求
     * @return 解锁结果
     */
    UnlockWechatResultVO unlockWechat(Long currentUserId, UnlockWechatDTO dto);

    /**
     * 关注用户
     *
     * @param currentUserId 当前用户ID
     * @param targetUserId  目标用户ID
     * @return 是否成功
     */
    boolean followUser(Long currentUserId, Long targetUserId);

    /**
     * 取消关注
     *
     * @param currentUserId 当前用户ID
     * @param targetUserId  目标用户ID
     * @return 是否成功
     */
    boolean unfollowUser(Long currentUserId, Long targetUserId);
}
