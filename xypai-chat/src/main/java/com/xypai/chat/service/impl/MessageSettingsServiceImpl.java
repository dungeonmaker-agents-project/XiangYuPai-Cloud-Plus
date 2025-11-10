package com.xypai.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xypai.chat.domain.dto.MessageSettingsUpdateDTO;
import com.xypai.chat.domain.entity.MessageSettings;
import com.xypai.chat.domain.vo.MessageSettingsVO;
import com.xypai.chat.mapper.MessageSettingsMapper;
import com.xypai.chat.service.IMessageSettingsService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户消息设置服务实现
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSettingsServiceImpl implements IMessageSettingsService {

    private final MessageSettingsMapper messageSettingsMapper;

    @Override
    public MessageSettingsVO getSettings(Long userId) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        MessageSettings settings = messageSettingsMapper.selectByUserId(targetUserId);
        
        // 如果不存在，创建默认设置
        if (settings == null) {
            initSettings(targetUserId);
            settings = messageSettingsMapper.selectByUserId(targetUserId);
        }

        return convertToVO(settings);
    }

    @Override
    public MessageSettingsVO getMySettings() {
        return getSettings(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSettings(MessageSettingsUpdateDTO updateDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        MessageSettings settings = messageSettingsMapper.selectByUserId(currentUserId);
        if (settings == null) {
            throw new ServiceException("用户消息设置不存在，请先初始化");
        }

        // 构建更新对象（只更新非NULL字段）
        MessageSettings updateSettings = MessageSettings.builder()
                .id(settings.getId())
                .build();

        // 推送设置
        if (updateDTO.getPushEnabled() != null) {
            updateSettings.setPushEnabled(updateDTO.getPushEnabled());
        }
        if (updateDTO.getPushSoundEnabled() != null) {
            updateSettings.setPushSoundEnabled(updateDTO.getPushSoundEnabled());
        }
        if (updateDTO.getPushVibrateEnabled() != null) {
            updateSettings.setPushVibrateEnabled(updateDTO.getPushVibrateEnabled());
        }
        if (updateDTO.getPushPreviewEnabled() != null) {
            updateSettings.setPushPreviewEnabled(updateDTO.getPushPreviewEnabled());
        }
        if (updateDTO.getPushStartTime() != null) {
            updateSettings.setPushStartTime(updateDTO.getPushStartTime());
        }
        if (updateDTO.getPushEndTime() != null) {
            updateSettings.setPushEndTime(updateDTO.getPushEndTime());
        }

        // 分类推送
        if (updateDTO.getPushLikeEnabled() != null) {
            updateSettings.setPushLikeEnabled(updateDTO.getPushLikeEnabled());
        }
        if (updateDTO.getPushCommentEnabled() != null) {
            updateSettings.setPushCommentEnabled(updateDTO.getPushCommentEnabled());
        }
        if (updateDTO.getPushFollowEnabled() != null) {
            updateSettings.setPushFollowEnabled(updateDTO.getPushFollowEnabled());
        }
        if (updateDTO.getPushSystemEnabled() != null) {
            updateSettings.setPushSystemEnabled(updateDTO.getPushSystemEnabled());
        }

        // 隐私设置
        if (updateDTO.getWhoCanMessage() != null) {
            updateSettings.setWhoCanMessage(updateDTO.getWhoCanMessage());
        }
        if (updateDTO.getWhoCanAddFriend() != null) {
            updateSettings.setWhoCanAddFriend(updateDTO.getWhoCanAddFriend());
        }

        // 消息设置
        if (updateDTO.getMessageReadReceipt() != null) {
            updateSettings.setMessageReadReceipt(updateDTO.getMessageReadReceipt());
        }
        if (updateDTO.getOnlineStatusVisible() != null) {
            updateSettings.setOnlineStatusVisible(updateDTO.getOnlineStatusVisible());
        }

        // 自动下载设置
        if (updateDTO.getAutoDownloadImage() != null) {
            updateSettings.setAutoDownloadImage(updateDTO.getAutoDownloadImage());
        }
        if (updateDTO.getAutoDownloadVideo() != null) {
            updateSettings.setAutoDownloadVideo(updateDTO.getAutoDownloadVideo());
        }
        if (updateDTO.getAutoPlayVoice() != null) {
            updateSettings.setAutoPlayVoice(updateDTO.getAutoPlayVoice());
        }

        // 其他
        if (updateDTO.getMessageRetentionDays() != null) {
            updateSettings.setMessageRetentionDays(updateDTO.getMessageRetentionDays());
        }

        int result = messageSettingsMapper.updateById(updateSettings);
        
        if (result > 0) {
            log.info("更新消息设置成功，用户ID：{}", currentUserId);
            // TODO: 清除Redis缓存
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetToDefault(Long userId) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // 删除现有设置
        messageSettingsMapper.deleteByUserId(targetUserId);
        
        // 创建默认设置
        return initSettings(targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initSettings(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // 检查是否已存在
        MessageSettings existSettings = messageSettingsMapper.selectByUserId(userId);
        if (existSettings != null) {
            log.warn("用户消息设置已存在，跳过初始化，用户ID：{}", userId);
            return true;
        }

        // 创建默认设置（使用Builder默认值）
        MessageSettings settings = MessageSettings.builder()
                .userId(userId)
                .build();

        int result = messageSettingsMapper.insert(settings);
        
        if (result > 0) {
            log.info("初始化消息设置成功，用户ID：{}", userId);
        }

        return result > 0;
    }

    @Override
    public boolean canUserSendMessage(Long userId, Long senderId) {
        if (userId == null || senderId == null) {
            return false;
        }

        MessageSettings settings = messageSettingsMapper.selectByUserId(userId);
        if (settings == null) {
            return true; // 默认允许
        }

        // TODO: 查询关注关系（需要集成用户服务）
        boolean isFollowing = false;
        boolean isMutualFollowing = false;

        return settings.canUserSendMessage(isFollowing, isMutualFollowing);
    }

    @Override
    public boolean shouldShowOnlineStatus(Long userId) {
        if (userId == null) {
            return true;
        }

        MessageSettings settings = messageSettingsMapper.selectByUserId(userId);
        return settings == null || settings.showOnlineStatus();
    }

    @Override
    public boolean shouldPush(Long userId, String messageCategory) {
        if (userId == null) {
            return false;
        }

        MessageSettings settings = messageSettingsMapper.selectByUserId(userId);
        if (settings == null || !settings.canPush()) {
            return false;
        }

        // TODO: 检查推送时间
        if (!settings.isInPushTimeRange()) {
            return false;
        }

        // 检查分类推送开关
        switch (messageCategory) {
            case "like":
                return Boolean.TRUE.equals(settings.getPushLikeEnabled());
            case "comment":
                return Boolean.TRUE.equals(settings.getPushCommentEnabled());
            case "follow":
                return Boolean.TRUE.equals(settings.getPushFollowEnabled());
            case "system":
                return Boolean.TRUE.equals(settings.getPushSystemEnabled());
            default:
                return true;
        }
    }

    /**
     * 转换为VO
     */
    private MessageSettingsVO convertToVO(MessageSettings settings) {
        if (settings == null) {
            return null;
        }

        return MessageSettingsVO.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                // 推送设置
                .pushEnabled(settings.getPushEnabled())
                .pushSoundEnabled(settings.getPushSoundEnabled())
                .pushVibrateEnabled(settings.getPushVibrateEnabled())
                .pushPreviewEnabled(settings.getPushPreviewEnabled())
                .pushStartTime(settings.getPushStartTime())
                .pushEndTime(settings.getPushEndTime())
                // 分类推送
                .pushLikeEnabled(settings.getPushLikeEnabled())
                .pushCommentEnabled(settings.getPushCommentEnabled())
                .pushFollowEnabled(settings.getPushFollowEnabled())
                .pushSystemEnabled(settings.getPushSystemEnabled())
                // 隐私设置
                .whoCanMessage(settings.getWhoCanMessage())
                .whoCanMessageDesc(settings.getWhoCanMessageDesc())
                .whoCanAddFriend(settings.getWhoCanAddFriend())
                .whoCanAddFriendDesc(settings.getWhoCanAddFriendDesc())
                // 消息设置
                .messageReadReceipt(settings.getMessageReadReceipt())
                .onlineStatusVisible(settings.getOnlineStatusVisible())
                // 自动下载
                .autoDownloadImage(settings.getAutoDownloadImage())
                .autoDownloadImageDesc(settings.getAutoDownloadImageDesc())
                .autoDownloadVideo(settings.getAutoDownloadVideo())
                .autoDownloadVideoDesc(settings.getAutoDownloadVideoDesc())
                .autoPlayVoice(settings.getAutoPlayVoice())
                // 其他
                .messageRetentionDays(settings.getMessageRetentionDays())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}

