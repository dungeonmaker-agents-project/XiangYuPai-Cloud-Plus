package com.xypai.chat.service;

import com.xypai.chat.domain.dto.MessageQueryDTO;
import com.xypai.chat.domain.dto.MessageSendDTO;
import com.xypai.chat.domain.vo.MessageVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IChatMessageService {

    /**
     * 发送消息
     *
     * @param sendDTO 发送数据
     * @return 消息ID
     */
    Long sendMessage(MessageSendDTO sendDTO);

    /**
     * 查询消息列表
     *
     * @param queryDTO 查询条件
     * @return 消息列表
     */
    List<MessageVO> selectMessageList(MessageQueryDTO queryDTO);

    /**
     * 根据ID查询消息详情
     *
     * @param messageId 消息ID
     * @return 消息详情
     */
    MessageVO selectMessageById(Long messageId);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param reason 撤回原因
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, String reason);

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @param deleteForAll 是否为所有人删除
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, Boolean deleteForAll);

    /**
     * 转发消息
     *
     * @param messageId 原消息ID
     * @param targetConversationIds 目标会话ID列表
     * @return 转发成功的数量
     */
    int forwardMessage(Long messageId, List<Long> targetConversationIds);

    /**
     * 标记消息已读
     *
     * @param conversationId 会话ID
     * @param messageId 消息ID(可选，不传则标记所有消息为已读)
     * @return 是否成功
     */
    boolean markMessageAsRead(Long conversationId, Long messageId);

    /**
     * 查询会话消息(分页)
     *
     * @param conversationId 会话ID
     * @param baseMessageId 基准消息ID
     * @param direction 查询方向(before=向前, after=向后)
     * @param limit 限制数量
     * @return 消息列表
     */
    List<MessageVO> selectConversationMessages(Long conversationId, Long baseMessageId, String direction, Integer limit);

    /**
     * 查询会话最新消息
     *
     * @param conversationId 会话ID
     * @return 最新消息
     */
    MessageVO selectLatestMessage(Long conversationId);

    /**
     * 查询未读消息数量
     *
     * @param conversationId 会话ID
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @return 未读消息数量
     */
    Integer countUnreadMessages(Long conversationId, Long userId);

    /**
     * 搜索消息
     *
     * @param conversationId 会话ID(可选)
     * @param keyword 关键词
     * @param messageType 消息类型(可选)
     * @param senderId 发送者ID(可选)
     * @param limit 限制数量
     * @return 消息列表
     */
    List<MessageVO> searchMessages(Long conversationId, String keyword, Integer messageType, Long senderId, Integer limit);

    /**
     * 查询时间范围内的消息
     *
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<MessageVO> selectMessagesByTimeRange(Long conversationId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 发送文本消息
     *
     * @param conversationId 会话ID
     * @param content 消息内容
     * @param replyToId 回复消息ID(可选)
     * @return 消息ID
     */
    Long sendTextMessage(Long conversationId, String content, Long replyToId);

    /**
     * 发送图片消息
     *
     * @param conversationId 会话ID
     * @param imageUrl 图片URL
     * @param thumbnailUrl 缩略图URL
     * @param width 宽度
     * @param height 高度
     * @return 消息ID
     */
    Long sendImageMessage(Long conversationId, String imageUrl, String thumbnailUrl, Integer width, Integer height);

    /**
     * 发送语音消息
     *
     * @param conversationId 会话ID
     * @param voiceUrl 语音URL
     * @param duration 语音时长(秒)
     * @param fileSize 文件大小
     * @return 消息ID
     */
    Long sendVoiceMessage(Long conversationId, String voiceUrl, Integer duration, Long fileSize);

    /**
     * 发送视频消息
     *
     * @param conversationId 会话ID
     * @param videoUrl 视频URL
     * @param thumbnailUrl 缩略图URL
     * @param duration 视频时长(秒)
     * @param fileSize 文件大小
     * @return 消息ID
     */
    Long sendVideoMessage(Long conversationId, String videoUrl, String thumbnailUrl, Integer duration, Long fileSize);

    /**
     * 发送文件消息
     *
     * @param conversationId 会话ID
     * @param fileUrl 文件URL
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return 消息ID
     */
    Long sendFileMessage(Long conversationId, String fileUrl, String fileName, Long fileSize);

    /**
     * 发送位置消息
     *
     * @param conversationId 会话ID
     * @param longitude 经度
     * @param latitude 纬度
     * @param address 地址
     * @return 消息ID
     */
    Long sendLocationMessage(Long conversationId, Double longitude, Double latitude, String address);

    /**
     * 发送系统通知消息
     *
     * @param conversationId 会话ID
     * @param content 通知内容
     * @return 消息ID
     */
    Long sendSystemMessage(Long conversationId, String content);

    /**
     * 批量删除消息
     *
     * @param messageIds 消息ID列表
     * @param deleteForAll 是否为所有人删除
     * @return 删除数量
     */
    int batchDeleteMessages(List<Long> messageIds, Boolean deleteForAll);

    /**
     * 清空会话消息
     *
     * @param conversationId 会话ID
     * @param clearForAll 是否为所有人清空
     * @return 清空数量
     */
    int clearConversationMessages(Long conversationId, Boolean clearForAll);

    /**
     * 查询消息统计
     *
     * @param conversationId 会话ID(可选)
     * @param userId 用户ID(可选)
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @return 统计信息
     */
    Map<String, Object> getMessageStats(Long conversationId, Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询热门表情统计
     *
     * @param limit 限制数量
     * @param days 统计天数
     * @return 表情统计列表
     */
    List<Map<String, Object>> getPopularEmojis(Integer limit, Integer days);

    /**
     * 查询可撤回的消息
     *
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @param minutes 可撤回时间(分钟)
     * @return 可撤回消息列表
     */
    List<MessageVO> getRecallableMessages(Long userId, Integer minutes);

    /**
     * 自动清理过期消息
     *
     * @param retentionDays 保留天数
     * @param batchSize 批次大小
     * @return 清理数量
     */
    int autoCleanExpiredMessages(Integer retentionDays, Integer batchSize);

    /**
     * 查询会话媒体消息
     *
     * @param conversationId 会话ID
     * @param messageType 消息类型(图片、视频、文件等)
     * @param limit 限制数量
     * @return 媒体消息列表
     */
    List<MessageVO> getConversationMediaMessages(Long conversationId, Integer messageType, Integer limit);

    /**
     * 获取消息已读状态
     *
     * @param messageId 消息ID
     * @return 已读状态列表
     */
    List<MessageVO.ReadStatusVO> getMessageReadStatus(Long messageId);

    /**
     * 验证消息发送权限
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageType 消息类型
     * @return 是否有发送权限
     */
    boolean validateSendPermission(Long conversationId, Long userId, Integer messageType);

    /**
     * 检查消息是否可以撤回
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否可以撤回
     */
    boolean canRecallMessage(Long messageId, Long userId);

    /**
     * 生成消息预览内容
     *
     * @param messageType 消息类型
     * @param content 消息内容
     * @param mediaData 媒体数据
     * @return 预览内容
     */
    String generateMessagePreview(Integer messageType, String content, Map<String, Object> mediaData);
}

