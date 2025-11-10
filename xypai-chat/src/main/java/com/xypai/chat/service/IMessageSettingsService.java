package com.xypai.chat.service;

import com.xypai.chat.domain.dto.MessageSettingsUpdateDTO;
import com.xypai.chat.domain.vo.MessageSettingsVO;

/**
 * 用户消息设置服务接口
 * 
 * @author xypai
 * @date 2025-01-14
 */
public interface IMessageSettingsService {

    /**
     * 获取用户消息设置
     * 
     * @param userId 用户ID(NULL=当前用户)
     * @return 消息设置
     */
    MessageSettingsVO getSettings(Long userId);

    /**
     * 获取当前用户的消息设置
     * 
     * @return 消息设置
     */
    MessageSettingsVO getMySettings();

    /**
     * 更新消息设置
     * 
     * @param updateDTO 更新DTO
     * @return 是否成功
     */
    boolean updateSettings(MessageSettingsUpdateDTO updateDTO);

    /**
     * 重置为默认设置
     * 
     * @param userId 用户ID(NULL=当前用户)
     * @return 是否成功
     */
    boolean resetToDefault(Long userId);

    /**
     * 初始化用户消息设置（用户注册时调用）
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean initSettings(Long userId);

    /**
     * 检查用户是否允许某人发消息
     * 
     * @param userId 被检查的用户ID
     * @param senderId 发送者ID
     * @return 是否允许
     */
    boolean canUserSendMessage(Long userId, Long senderId);

    /**
     * 检查是否显示在线状态
     * 
     * @param userId 用户ID
     * @return 是否显示
     */
    boolean shouldShowOnlineStatus(Long userId);

    /**
     * 检查是否需要推送
     * 
     * @param userId 用户ID
     * @param messageCategory 消息分类(like/comment/follow/system)
     * @return 是否推送
     */
    boolean shouldPush(Long userId, String messageCategory);
}

