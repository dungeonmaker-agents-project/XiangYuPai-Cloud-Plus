package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dromara.common.core.exception.ServiceException;
import com.xypai.user.constant.UserConstants;
import com.xypai.user.domain.dto.UserProfileUpdateDTO;
import com.xypai.user.domain.entity.UserOccupation;
import com.xypai.user.domain.entity.UserProfileNew;
import com.xypai.user.domain.vo.ProfileCompletenessVO;
import com.xypai.user.domain.vo.UserOccupationVO;
import com.xypai.user.domain.vo.UserProfileVO;
import com.xypai.user.domain.vo.UserStatsVO;
import com.xypai.user.mapper.UserOccupationMapper;
import com.xypai.user.mapper.UserProfileMapper;
import com.xypai.user.service.IOccupationService;
import com.xypai.user.service.IUserProfileService;
import com.xypai.user.service.IUserStatsService;
import com.xypai.user.utils.ProfileCompletenessCalculator;
import com.xypai.user.utils.UserUtils;
import com.xypai.user.validator.UserProfileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户资料服务实现
 *
 * @author Bob
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserProfileMapper userProfileMapper;
    private final UserOccupationMapper userOccupationMapper;
    private final IUserStatsService userStatsService;
    private final IOccupationService occupationService;
    private final UserProfileValidator profileValidator;

    /**
     * 根据用户ID查询资料
     */
    @Override
    public UserProfileVO getUserProfile(Long userId) {
        // TODO: 实现完整的查询逻辑
        // 1. 查询UserProfileNew
        // 2. 查询UserStats
        // 3. 查询UserOccupation
        // 4. 组装VO
        
        log.info("查询用户资料，userId: {}", userId);
        return null;
    }

    /**
     * 更新用户资料
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        if (userId == null || updateDTO == null) {
            throw new ServiceException("参数不能为空");
        }

        // 1. 验证资料
        profileValidator.validateAndThrow(updateDTO);

        // 2. 查询当前资料
        UserProfileNew profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            throw new ServiceException(UserConstants.ERROR_USER_NOT_FOUND);
        }

        // 3. 更新字段
        updateFields(profile, updateDTO);

        // 4. 计算年龄
        if (updateDTO.getBirthday() != null) {
            profile.calculateAge();
        }

        // 5. 计算资料完整�?
        List<UserOccupation> occupations = userOccupationMapper.selectByUserId(userId);
        int score = ProfileCompletenessCalculator.calculate(profile, occupations);
        profile.setProfileCompleteness(score);

        // 6. 更新编辑时间
        profile.markAsEdited();

        // 7. 保存（乐观锁�?
        int result = userProfileMapper.updateById(profile);
        if (result == 0) {
            throw new ServiceException("更新失败，请重试");
        }

        log.info("更新用户资料成功，userId: {}, 完整�? {}%", userId, score);
        return true;
    }

    /**
     * 获取资料完整度信�?
     */
    @Override
    public ProfileCompletenessVO getProfileCompleteness(Long userId) {
        // 查询资料
        UserProfileNew profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            throw new ServiceException(UserConstants.ERROR_USER_NOT_FOUND);
        }

        // 查询职业
        List<UserOccupation> occupations = userOccupationMapper.selectByUserId(userId);

        // 计算完整�?
        int score = ProfileCompletenessCalculator.calculate(profile, occupations);
        String level = ProfileCompletenessCalculator.getCompletenessLevel(score);
        boolean isComplete = ProfileCompletenessCalculator.isComplete(score);
        List<String> suggestions = ProfileCompletenessCalculator.getSuggestions(profile, occupations);

        return ProfileCompletenessVO.builder()
            .userId(userId)
            .currentScore(score)
            .level(level)
            .isComplete(isComplete)
            .suggestions(suggestions)
            .coreFieldsScore(calculateCoreFieldsScore(profile))
            .extendedFieldsScore(calculateExtendedFieldsScore(profile, occupations))
            .build();
    }

    /**
     * 更新在线状�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOnlineStatus(Long userId, Integer onlineStatus) {
        if (!UserProfileValidator.isValidOnlineStatus(onlineStatus)) {
            throw new ServiceException("无效的在线状态");
        }

        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getOnlineStatus, onlineStatus)
               .set(UserProfileNew::getLastOnlineTime, LocalDateTime.now());

        int result = userProfileMapper.update(null, wrapper);
        return result > 0;
    }

    /**
     * 用户上线
     */
    @Override
    public boolean goOnline(Long userId) {
        return updateOnlineStatus(userId, UserConstants.ONLINE_STATUS_ONLINE);
    }

    /**
     * 用户离线
     */
    @Override
    public boolean goOffline(Long userId) {
        return updateOnlineStatus(userId, UserConstants.ONLINE_STATUS_OFFLINE);
    }

    /**
     * 用户隐身
     */
    @Override
    public boolean goInvisible(Long userId) {
        return updateOnlineStatus(userId, UserConstants.ONLINE_STATUS_INVISIBLE);
    }

    /**
     * 检查用户是否在线（5分钟内活跃）
     */
    @Override
    public boolean isUserOnline(Long userId) {
        UserProfileNew profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            return false;
        }
        
        return profile.isOnline() 
            && UserUtils.isUserOnline(profile.getLastOnlineTime());
    }

    /**
     * 实名认证通过
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markRealVerified(Long userId, String realName, String idCard) {
        // TODO: 加密身份证号（AES-256�?
        String idCardEncrypted = encryptIdCard(idCard);

        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsRealVerified, true)
               .set(UserProfileNew::getRealName, realName)
               .set(UserProfileNew::getIdCardEncrypted, idCardEncrypted);

        int result = userProfileMapper.update(null, wrapper);
        
        if (result > 0) {
            log.info("实名认证通过，userId: {}", userId);
            // 重新计算资料完整度（+15分）
            recalculateCompleteness(userId);
        }
        
        return result > 0;
    }

    /**
     * 大神认证通过
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markGodVerified(Long userId) {
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsGodVerified, true);

        int result = userProfileMapper.update(null, wrapper);
        log.info("大神认证通过，userId: {}", userId);
        return result > 0;
    }

    /**
     * 组局达人认证通过
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markActivityExpert(Long userId) {
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsActivityExpert, true);

        int result = userProfileMapper.update(null, wrapper);
        log.info("组局达人认证通过，userId: {}", userId);
        return result > 0;
    }

    /**
     * 设置VIP
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setVip(Long userId, Integer vipLevel, Integer durationDays) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(durationDays);

        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsVip, true)
               .set(UserProfileNew::getVipLevel, vipLevel)
               .set(UserProfileNew::getVipExpireTime, expireTime);

        int result = userProfileMapper.update(null, wrapper);
        log.info("设置VIP成功，userId: {}, level: {}, days: {}", userId, vipLevel, durationDays);
        return result > 0;
    }

    /**
     * 取消VIP
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelVip(Long userId) {
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsVip, false)
               .set(UserProfileNew::getVipLevel, 0)
               .set(UserProfileNew::getVipExpireTime, null);

        int result = userProfileMapper.update(null, wrapper);
        log.info("取消VIP成功，userId: {}", userId);
        return result > 0;
    }

    /**
     * 检查VIP是否有效
     */
    @Override
    public boolean isVipValid(Long userId) {
        UserProfileNew profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            return false;
        }
        return profile.isVipValid();
    }

    /**
     * 标记为人气用�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsPopular(Long userId) {
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsPopular, true);

        int result = userProfileMapper.update(null, wrapper);
        log.info("标记为人气用户，userId: {}", userId);
        return result > 0;
    }

    /**
     * 取消人气用户标记
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unmarkAsPopular(Long userId) {
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIsPopular, false);

        int result = userProfileMapper.update(null, wrapper);
        return result > 0;
    }

    /**
     * 批量查询用户资料
     */
    @Override
    public List<UserProfileVO> getBatchUserProfiles(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        try {
            // 批量查询用户资料
            List<UserProfileNew> profiles = userProfileMapper.selectByUserIds(userIds);
            
            if (profiles == null || profiles.isEmpty()) {
                log.warn("批量查询用户资料为空，userIds: {}", userIds);
                return new java.util.ArrayList<>();
            }
            
            // 转换为VO
            List<UserProfileVO> result = new java.util.ArrayList<>();
            for (UserProfileNew profile : profiles) {
                try {
                    UserProfileVO vo = new UserProfileVO();
                    BeanUtils.copyProperties(profile, vo);
                    
                    // 查询职业信息（增加异常保护）
                    try {
                        List<UserOccupationVO> occupations = occupationService.getUserOccupations(profile.getUserId());
                        vo.setOccupations(occupations != null ? occupations : new java.util.ArrayList<>());
                    } catch (Exception e) {
                        log.warn("查询用户职业信息失败，userId: {}, error: {}", profile.getUserId(), e.getMessage());
                        vo.setOccupations(new java.util.ArrayList<>());
                    }
                    
                    // 查询统计数据（增加异常保护）
                    try {
                        UserStatsVO stats = userStatsService.getUserStats(profile.getUserId());
                        vo.setStats(stats);
                    } catch (Exception e) {
                        log.warn("查询用户统计数据失败，userId: {}, error: {}", profile.getUserId(), e.getMessage());
                        vo.setStats(null);
                    }
                    
                    result.add(vo);
                } catch (Exception e) {
                    log.error("转换用户资料失败，userId: {}, error: {}", profile.getUserId(), e.getMessage());
                    // 跳过这条数据，继续处理其他数据
                }
            }
            
            log.info("批量查询用户资料成功，查询数量: {}, 返回数量: {}", userIds.size(), result.size());
            return result;
            
        } catch (Exception e) {
            log.error("批量查询用户资料失败，userIds: {}, error: {}", userIds, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 更新IP归属�?
     */
    @Override
    public boolean updateIpLocation(Long userId, String ipLocation) {
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getIpLocation, ipLocation);

        int result = userProfileMapper.update(null, wrapper);
        return result > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 更新字段
     */
    private void updateFields(UserProfileNew profile, UserProfileUpdateDTO updateDTO) {
        if (updateDTO.getNickname() != null) {
            profile.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatar() != null) {
            profile.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getAvatarThumbnail() != null) {
            profile.setAvatarThumbnail(updateDTO.getAvatarThumbnail());
        }
        if (updateDTO.getBackgroundImage() != null) {
            profile.setBackgroundImage(updateDTO.getBackgroundImage());
        }
        if (updateDTO.getGender() != null) {
            profile.setGender(updateDTO.getGender());
        }
        if (updateDTO.getBirthday() != null) {
            profile.setBirthday(updateDTO.getBirthday());
        }
        if (updateDTO.getCityId() != null) {
            profile.setCityId(updateDTO.getCityId());
        }
        if (updateDTO.getLocation() != null) {
            profile.setLocation(updateDTO.getLocation());
        }
        if (updateDTO.getAddress() != null) {
            profile.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getBio() != null) {
            profile.setBio(updateDTO.getBio());
        }
        if (updateDTO.getHeight() != null) {
            profile.setHeight(updateDTO.getHeight());
        }
        if (updateDTO.getWeight() != null) {
            profile.setWeight(updateDTO.getWeight());
        }
        if (updateDTO.getRealName() != null) {
            profile.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getWechat() != null) {
            profile.setWechat(updateDTO.getWechat());
        }
        if (updateDTO.getWechatUnlockCondition() != null) {
            profile.setWechatUnlockCondition(updateDTO.getWechatUnlockCondition());
        }
        if (updateDTO.getOnlineStatus() != null) {
            profile.setOnlineStatus(updateDTO.getOnlineStatus());
        }
    }

    /**
     * 重新计算资料完整�?
     */
    private void recalculateCompleteness(Long userId) {
        UserProfileNew profile = userProfileMapper.selectById(userId);
        List<UserOccupation> occupations = userOccupationMapper.selectByUserId(userId);
        
        int score = ProfileCompletenessCalculator.calculate(profile, occupations);
        
        LambdaUpdateWrapper<UserProfileNew> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfileNew::getUserId, userId)
               .set(UserProfileNew::getProfileCompleteness, score);
        
        userProfileMapper.update(null, wrapper);
        log.debug("重新计算资料完整度，userId: {}, score: {}", userId, score);
    }

    /**
     * 计算核心字段得分
     */
    private Integer calculateCoreFieldsScore(UserProfileNew profile) {
        int score = 0;
        if (profile.getNickname() != null && !profile.getNickname().isEmpty()) score += 10;
        if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) score += 10;
        if (profile.getGender() != null && profile.getGender() > 0) score += 10;
        if (profile.getBirthday() != null) score += 10;
        if (profile.getCityId() != null) score += 10;
        return score;
    }

    /**
     * 计算扩展字段得分
     */
    private Integer calculateExtendedFieldsScore(UserProfileNew profile, List<UserOccupation> occupations) {
        int score = 0;
        if (profile.getBio() != null && !profile.getBio().isEmpty()) score += 5;
        if (profile.getHeight() != null) score += 5;
        if (profile.getWeight() != null) score += 5;
        if (occupations != null && !occupations.isEmpty()) score += 10;
        if (profile.getWechat() != null && !profile.getWechat().isEmpty()) score += 5;
        if (profile.getBackgroundImage() != null && !profile.getBackgroundImage().isEmpty()) score += 5;
        if (Boolean.TRUE.equals(profile.getIsRealVerified())) score += 15;
        return score;
    }

    /**
     * 加密身份证号（AES-256�?
     */
    private String encryptIdCard(String idCard) {
        // TODO: 实现AES-256加密
        // 使用KMS密钥管理系统
        return idCard; // 临时返回原�?
    }
}

