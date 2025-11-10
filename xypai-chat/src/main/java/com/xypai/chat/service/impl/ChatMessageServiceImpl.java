package com.xypai.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xypai.chat.domain.dto.MessageQueryDTO;
import com.xypai.chat.domain.dto.MessageSendDTO;
import com.xypai.chat.domain.entity.ChatMessage;
import com.xypai.chat.domain.entity.ChatParticipant;
import com.xypai.chat.domain.vo.MessageVO;
import com.xypai.chat.mapper.ChatMessageMapper;
import com.xypai.chat.mapper.ChatParticipantMapper;
import com.xypai.chat.service.IChatConversationService;
import com.xypai.chat.service.IChatMessageService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天消息服务实现�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatParticipantMapper chatParticipantMapper;
    private final IChatConversationService chatConversationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(MessageSendDTO sendDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 验证发送权限
        if (!validateSendPermission(sendDTO.getConversationId(), currentUserId, sendDTO.getMessageType())) {
            throw new ServiceException("无权限发送消息");
        }

        // v7.1: 消息去重检查（基于clientId�?
        if (sendDTO.getClientId() != null) {
            ChatMessage existMessage = chatMessageMapper.selectOne(
                Wrappers.lambdaQuery(ChatMessage.class)
                    .eq(ChatMessage::getClientId, sendDTO.getClientId())
            );
            if (existMessage != null) {
                log.warn("消息重复，已去重，clientId：{}", sendDTO.getClientId());
                return existMessage.getId();
            }
        }

        // v7.1: 生成消息序列号（保证有序性）
        Long sequenceId = generateSequenceId(sendDTO.getConversationId());

        LocalDateTime now = LocalDateTime.now();

        // v7.1: 创建消息（使用独立字段，不再使用JSON�?
        ChatMessage message = ChatMessage.builder()
                .conversationId(sendDTO.getConversationId())
                .senderId(currentUserId)
                .messageType(sendDTO.getMessageType())
                .content(sendDTO.getContent())
                // v7.1: 媒体字段展开
                .mediaUrl(sendDTO.getMediaUrl())
                .thumbnailUrl(sendDTO.getThumbnailUrl())
                .mediaSize(sendDTO.getFileSize())
                .mediaWidth(sendDTO.getMediaWidth())
                .mediaHeight(sendDTO.getMediaHeight())
                .mediaDuration(sendDTO.getDuration())
                .mediaCaption(sendDTO.getMediaCaption())
                // v7.1: 消息管理字段
                .clientId(sendDTO.getClientId())
                .sequenceId(sequenceId)
                .deliveryStatus(ChatMessage.DeliveryStatus.SENDING.getCode())
                // v7.1: 时间分离
                .sendTime(now)
                .serverTime(now)
                .replyToId(sendDTO.getReplyToId())
                .status(ChatMessage.Status.NORMAL.getCode())
                .createdAt(now)
                .build();

        int result = chatMessageMapper.insert(message);
        if (result <= 0) {
            throw new ServiceException("发送消息失败");
        }

        // v7.1: 更新投递状态为已发�?
        message.setDeliveryStatus(ChatMessage.DeliveryStatus.SENT.getCode());
        chatMessageMapper.updateById(message);

        // v7.1: 更新会话冗余字段（最后消息ID和时间）
        updateConversationLastMessage(sendDTO.getConversationId(), message.getId(), now);

        // v7.1: 增加其他参与者的未读数量
        incrementUnreadCountForOthers(sendDTO.getConversationId(), currentUserId);

        log.info("发送消息成功，消息ID：{}，会话ID：{}，发送者：{}，类型：{}，序列号：{}", 
                message.getId(), sendDTO.getConversationId(), currentUserId, sendDTO.getMessageType(), sequenceId);
        
        return message.getId();
    }

    @Override
    public List<MessageVO> selectMessageList(MessageQueryDTO queryDTO) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = buildQueryWrapper(queryDTO);
        List<ChatMessage> messages = chatMessageMapper.selectList(queryWrapper);
        return convertToVOs(messages, queryDTO.getIncludeSender(), queryDTO.getIncludeReplyTo());
    }

    @Override
    public MessageVO selectMessageById(Long messageId) {
        if (messageId == null) {
            throw new ServiceException("消息ID不能为空");
        }

        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 验证访问权限
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !chatConversationService.validateAccessPermission(message.getConversationId(), currentUserId)) {
            throw new ServiceException("无权限查看该消息");
        }

        return convertToVO(message, true, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recallMessage(Long messageId, String reason) {
        if (messageId == null) {
            throw new ServiceException("消息ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 验证撤回权限（v7.1: 2分钟限制）
        if (!canRecallMessage(messageId, currentUserId)) {
            throw new ServiceException("无权限撤回该消息或超出撤回时�?2分钟)");
        }

        // v7.1: 更新消息状态为已撤回，清空内容（隐私保护）
        ChatMessage updateMessage = ChatMessage.builder()
                .id(messageId)
                .status(ChatMessage.Status.RECALLED.getCode())
                .content("[该消息已被撤回]")
                .mediaUrl(null)  // v7.1: 清空媒体URL
                .thumbnailUrl(null)
                .recalledBy(currentUserId)  // v7.1: 记录撤回操作�?
                .build();

        int result = chatMessageMapper.updateById(updateMessage);
        if (result > 0) {
            log.info("撤回消息成功，消息ID：{}，撤回人：{}，原因：{}", messageId, currentUserId, reason);
            
            // TODO: WebSocket推送撤回通知给会话成�?
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long messageId, Boolean deleteForAll) {
        if (messageId == null) {
            throw new ServiceException("消息ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 验证删除权限
        if (!message.getSenderId().equals(currentUserId)) {
            throw new ServiceException("只能删除自己发送的消息");
        }

        if (Boolean.TRUE.equals(deleteForAll)) {
            // 为所有人删除
            ChatMessage updateMessage = ChatMessage.builder()
                    .id(messageId)
                    .status(ChatMessage.Status.DELETED.getCode())
                    .content("[该消息已被删除]")
                    .build();

            int result = chatMessageMapper.updateById(updateMessage);
            log.info("删除消息成功（所有人），消息ID：{}", messageId);
            return result > 0;
        } else {
            // 仅为自己删除（TODO: 实现个人删除记录�?
            log.info("删除消息成功（仅自己），消息ID：{}", messageId);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int forwardMessage(Long messageId, List<Long> targetConversationIds) {
        if (messageId == null || targetConversationIds == null || targetConversationIds.isEmpty()) {
            throw new ServiceException("消息ID和目标会话ID列表不能为空");
        }

        ChatMessage originalMessage = chatMessageMapper.selectById(messageId);
        if (originalMessage == null) {
            throw new ServiceException("原消息不存在");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        int successCount = 0;
        for (Long conversationId : targetConversationIds) {
            try {
                // 验证目标会话发送权�?
                if (!validateSendPermission(conversationId, currentUserId, originalMessage.getMessageType())) {
                    log.warn("无权限转发消息到会话：{}", conversationId);
                    continue;
                }

                // 创建转发消息
                ChatMessage forwardMessage = ChatMessage.builder()
                        .conversationId(conversationId)
                        .senderId(currentUserId)
                        .messageType(originalMessage.getMessageType())
                        .content("[转发] " + originalMessage.getContent())
                        .mediaData(originalMessage.getMediaData())
                        .status(ChatMessage.Status.NORMAL.getCode())
                        .createdAt(LocalDateTime.now())
                        .build();

                int result = chatMessageMapper.insert(forwardMessage);
                if (result > 0) {
                    successCount++;
                    // 更新目标会话最后活跃时�?
                    chatConversationService.updateLastActiveTime(conversationId);
                }
            } catch (Exception e) {
                log.error("转发消息到会话{}失败：{}", conversationId, e.getMessage());
            }
        }

        log.info("转发消息完成，原消息ID：{}，成功转发：{}/{}", messageId, successCount, targetConversationIds.size());
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageAsRead(Long conversationId, Long messageId) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        LocalDateTime now = LocalDateTime.now();

        // v7.1: 使用精确已读位置（同时更新messageId和时间，清空未读数）
        if (messageId != null) {
            // 更新已读位置到指定消�?
            int result = chatParticipantMapper.updateReadPosition(conversationId, currentUserId, messageId, now);
            log.debug("标记消息已读（精确位置），会话ID：{}，消息ID：{}，用户ID：{}", 
                    conversationId, messageId, currentUserId);
            return result > 0;
        } else {
            // 更新已读时间（兼容旧接口�?
            int result = chatParticipantMapper.updateLastReadTime(conversationId, currentUserId, now);
            log.debug("标记消息已读（时间），会话ID：{}，用户ID：{}", conversationId, currentUserId);
            return result > 0;
        }
    }

    @Override
    public List<MessageVO> selectConversationMessages(Long conversationId, Long baseMessageId, 
                                                     String direction, Integer limit) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        // 验证访问权限
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !chatConversationService.validateAccessPermission(conversationId, currentUserId)) {
            throw new ServiceException("无权限访问该会话");
        }

        List<ChatMessage> messages = chatMessageMapper.selectConversationMessages(
                conversationId, baseMessageId, direction, limit != null ? limit : 20);
        return convertToVOs(messages, true, true);
    }

    @Override
    public MessageVO selectLatestMessage(Long conversationId) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        ChatMessage message = chatMessageMapper.selectLatestMessage(conversationId);
        return message != null ? convertToVO(message, true, false) : null;
    }

    @Override
    public Integer countUnreadMessages(Long conversationId, Long userId) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // 获取用户最后已读时�?
        ChatParticipant participant = chatParticipantMapper.selectParticipant(conversationId, targetUserId);
        if (participant == null) {
            return 0;
        }

        return chatMessageMapper.countUnreadMessages(conversationId, targetUserId, participant.getLastReadTime());
    }

    @Override
    public List<MessageVO> searchMessages(Long conversationId, String keyword, Integer messageType, 
                                         Long senderId, Integer limit) {
        if (StringUtils.isBlank(keyword)) {
            throw new ServiceException("搜索关键词不能为空");
        }

        List<ChatMessage> messages = chatMessageMapper.searchMessages(
                conversationId, keyword, messageType, senderId, limit != null ? limit : 20);
        return convertToVOs(messages, true, false);
    }

    @Override
    public List<MessageVO> selectMessagesByTimeRange(Long conversationId, LocalDateTime startTime, LocalDateTime endTime) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        List<ChatMessage> messages = chatMessageMapper.selectMessagesByTimeRange(conversationId, startTime, endTime);
        return convertToVOs(messages, true, false);
    }

    @Override
    public Long sendTextMessage(Long conversationId, String content, Long replyToId) {
        MessageSendDTO sendDTO = MessageSendDTO.builder()
                .conversationId(conversationId)
                .messageType(ChatMessage.MessageType.TEXT.getCode())
                .content(content)
                .replyToId(replyToId)
                .build();
        return sendMessage(sendDTO);
    }

    @Override
    public Long sendImageMessage(Long conversationId, String imageUrl, String thumbnailUrl, 
                                Integer width, Integer height) {
        MessageSendDTO sendDTO = MessageSendDTO.builder()
                .conversationId(conversationId)
                .messageType(ChatMessage.MessageType.IMAGE.getCode())
                .content("[图片]")
                .mediaUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .extraMediaData(Map.of("width", width, "height", height))
                .build();
        return sendMessage(sendDTO);
    }

    @Override
    public Long sendVoiceMessage(Long conversationId, String voiceUrl, Integer duration, Long fileSize) {
        MessageSendDTO sendDTO = MessageSendDTO.builder()
                .conversationId(conversationId)
                .messageType(ChatMessage.MessageType.VOICE.getCode())
                .content("[语音]")
                .mediaUrl(voiceUrl)
                .duration(duration)
                .fileSize(fileSize)
                .build();
        return sendMessage(sendDTO);
    }

    @Override
    public Long sendVideoMessage(Long conversationId, String videoUrl, String thumbnailUrl, 
                                Integer duration, Long fileSize) {
        MessageSendDTO sendDTO = MessageSendDTO.builder()
                .conversationId(conversationId)
                .messageType(ChatMessage.MessageType.VIDEO.getCode())
                .content("[视频]")
                .mediaUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .duration(duration)
                .fileSize(fileSize)
                .build();
        return sendMessage(sendDTO);
    }

    @Override
    public Long sendFileMessage(Long conversationId, String fileUrl, String fileName, Long fileSize) {
        MessageSendDTO sendDTO = MessageSendDTO.builder()
                .conversationId(conversationId)
                .messageType(ChatMessage.MessageType.FILE.getCode())
                .content(fileName)
                .mediaUrl(fileUrl)
                .fileSize(fileSize)
                .build();
        return sendMessage(sendDTO);
    }

    @Override
    public Long sendLocationMessage(Long conversationId, Double longitude, Double latitude, String address) {
        MessageSendDTO sendDTO = MessageSendDTO.builder()
                .conversationId(conversationId)
                .messageType(ChatMessage.MessageType.LOCATION.getCode())
                .content("[位置] " + address)
                .longitude(longitude)
                .latitude(latitude)
                .address(address)
                .build();
        return sendMessage(sendDTO);
    }

    @Override
    public Long sendSystemMessage(Long conversationId, String content) {
        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .senderId(null) // 系统消息无发送�?
                .messageType(ChatMessage.MessageType.SYSTEM.getCode())
                .content(content)
                .status(ChatMessage.Status.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageMapper.insert(message);
        
        // 更新会话最后活跃时�?
        chatConversationService.updateLastActiveTime(conversationId);

        log.info("发送系统消息成功，消息ID：{}，会话ID：{}", message.getId(), conversationId);
        return message.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteMessages(List<Long> messageIds, Boolean deleteForAll) {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }

        Integer newStatus = ChatMessage.Status.DELETED.getCode();
        return chatMessageMapper.batchUpdateStatus(messageIds, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearConversationMessages(Long conversationId, Boolean clearForAll) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        if (Boolean.TRUE.equals(clearForAll)) {
            // 为所有人清空
            return chatMessageMapper.deleteConversationMessages(conversationId);
        } else {
            // 仅为自己清空（TODO: 实现个人清空记录�?
            log.info("清空会话消息（仅自己），会话ID：{}", conversationId);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getMessageStats(Long conversationId, Long userId, 
                                              LocalDateTime startTime, LocalDateTime endTime) {
        if (conversationId != null) {
            return chatMessageMapper.selectMessageStats(conversationId, startTime, endTime);
        } else if (userId != null) {
            return chatMessageMapper.selectUserMessageStats(userId, startTime, endTime);
        } else {
            throw new ServiceException("会话ID和用户ID不能同时为空");
        }
    }

    @Override
    public List<Map<String, Object>> getPopularEmojis(Integer limit, Integer days) {
        return chatMessageMapper.selectPopularEmojis(
                limit != null ? limit : 10, days != null ? days : 7);
    }

    @Override
    public List<MessageVO> getRecallableMessages(Long userId, Integer minutes) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        List<ChatMessage> messages = chatMessageMapper.selectRecallableMessages(
                targetUserId, minutes != null ? minutes : 5);
        return convertToVOs(messages, false, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCleanExpiredMessages(Integer retentionDays, Integer batchSize) {
        List<Long> expiredMessageIds = chatMessageMapper.selectExpiredMessages(
                retentionDays != null ? retentionDays : 30, 
                batchSize != null ? batchSize : 1000);

        if (expiredMessageIds.isEmpty()) {
            return 0;
        }

        return chatMessageMapper.batchUpdateStatus(expiredMessageIds, ChatMessage.Status.DELETED.getCode());
    }

    @Override
    public List<MessageVO> getConversationMediaMessages(Long conversationId, Integer messageType, Integer limit) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        List<ChatMessage> messages = chatMessageMapper.selectMediaMessages(
                conversationId, messageType, limit != null ? limit : 20);
        return convertToVOs(messages, true, false);
    }

    @Override
    public List<MessageVO.ReadStatusVO> getMessageReadStatus(Long messageId) {
        if (messageId == null) {
            throw new ServiceException("消息ID不能为空");
        }

        List<Map<String, Object>> readUsers = chatMessageMapper.selectMessageReadUsers(messageId);
        return readUsers.stream()
                .map(user -> MessageVO.ReadStatusVO.builder()
                        .userId((Long) user.get("user_id"))
                        .nickname((String) user.get("nickname"))
                        .avatar((String) user.get("avatar"))
                        .readAt((LocalDateTime) user.get("read_at"))
                        .isRead(user.get("read_at") != null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateSendPermission(Long conversationId, Long userId, Integer messageType) {
        if (conversationId == null || userId == null) {
            return false;
        }

        // 检查是否为会话参与�?
        if (!chatConversationService.isParticipant(conversationId, userId)) {
            return false;
        }

        // 检查用户权�?
        return chatConversationService.checkUserPermission(conversationId, userId, "send_message");
    }

    @Override
    public boolean canRecallMessage(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return false;
        }

        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            return false;
        }

        return message.canRecall(userId);
    }

    @Override
    public String generateMessagePreview(Integer messageType, String content, Map<String, Object> mediaData) {
        if (messageType == null) {
            return content;
        }

        ChatMessage.MessageType type = ChatMessage.MessageType.fromCode(messageType);
        if (type == null) {
            return content;
        }

        switch (type) {
            case TEXT:
                return StringUtils.isNotBlank(content) ? 
                       (content.length() > 50 ? content.substring(0, 50) + "..." : content) : "[文本]";
            case IMAGE:
                return "[图片]";
            case VOICE:
                Integer duration = mediaData != null ? (Integer) mediaData.get("duration") : null;
                return duration != null ? String.format("[语音] %d\"", duration) : "[语音]";
            case VIDEO:
                return "[视频]";
            case FILE:
                return "[文件] " + (StringUtils.isNotBlank(content) ? content : "未知文件");
            case LOCATION:
                return "[位置] " + (StringUtils.isNotBlank(content) ? content : "位置信息");
            case EMOJI:
                return "[表情]";
            case SYSTEM:
                return content;
            default:
                return "[未知消息]";
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ChatMessage> buildQueryWrapper(MessageQueryDTO query) {
        return Wrappers.lambdaQuery(ChatMessage.class)
                .eq(query.getConversationId() != null, ChatMessage::getConversationId, query.getConversationId())
                .eq(query.getSenderId() != null, ChatMessage::getSenderId, query.getSenderId())
                .eq(query.getMessageType() != null, ChatMessage::getMessageType, query.getMessageType())
                .eq(query.getStatus() != null, ChatMessage::getStatus, query.getStatus())
                .eq(query.getReplyToId() != null, ChatMessage::getReplyToId, query.getReplyToId())
                .like(StringUtils.isNotBlank(query.getContent()), ChatMessage::getContent, query.getContent())
                .ge(query.getParams() != null && query.getParams().get("beginTime") != null,
                        ChatMessage::getCreatedAt, query.getParams().get("beginTime"))
                .le(query.getParams() != null && query.getParams().get("endTime") != null,
                        ChatMessage::getCreatedAt, query.getParams().get("endTime"))
                .orderByDesc(ChatMessage::getCreatedAt);
    }

    /**
     * 构建媒体数据
     */
    private Map<String, Object> buildMediaData(MessageSendDTO dto) {
        Map<String, Object> mediaData = new HashMap<>();
        
        if (StringUtils.isNotBlank(dto.getMediaUrl())) {
            mediaData.put("url", dto.getMediaUrl());
        }
        if (dto.getFileSize() != null) {
            mediaData.put("size", dto.getFileSize());
        }
        if (dto.getDuration() != null) {
            mediaData.put("duration", dto.getDuration());
        }
        if (StringUtils.isNotBlank(dto.getThumbnailUrl())) {
            mediaData.put("thumbnail", dto.getThumbnailUrl());
        }
        if (dto.getLongitude() != null) {
            mediaData.put("longitude", dto.getLongitude());
        }
        if (dto.getLatitude() != null) {
            mediaData.put("latitude", dto.getLatitude());
        }
        if (StringUtils.isNotBlank(dto.getAddress())) {
            mediaData.put("address", dto.getAddress());
        }
        if (dto.getExtraMediaData() != null) {
            mediaData.putAll(dto.getExtraMediaData());
        }
        
        return mediaData.isEmpty() ? null : mediaData;
    }

    /**
     * 转换为VO列表
     */
    private List<MessageVO> convertToVOs(List<ChatMessage> messages, Boolean includeSender, Boolean includeReplyTo) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        return messages.stream()
                .map(message -> convertToVO(message, includeSender, includeReplyTo))
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO（v7.1: 优先使用独立字段�?
     */
    private MessageVO convertToVO(ChatMessage message, Boolean includeSender, Boolean includeReplyTo) {
        MessageVO.MessageVOBuilder builder = MessageVO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .messageType(message.getMessageType())
                .messageTypeDesc(message.getMessageTypeDesc())
                .content(message.getContent())
                .replyToId(message.getReplyToId())
                .status(message.getStatus())
                .statusDesc(message.getStatusDesc())
                .createdAt(message.getCreatedAt())
                .isSystem(message.isSystem())
                .canRecall(message.canRecall(LoginHelper.getUserId()))
                // v7.1新增字段
                .clientId(message.getClientId())
                .sequenceId(message.getSequenceId())
                .deliveryStatus(message.getDeliveryStatus())
                .deliveryStatusDesc(message.getDeliveryStatusDesc())
                .readCount(message.getReadCount())
                .likeCount(message.getLikeCount())
                .isRecalled(message.getStatus() != null && message.getStatus() == 2)
                .recalledBy(message.getRecalledBy())
                .sendTime(message.getSendTime())
                .serverTime(message.getServerTime())
                .sequenceNumber(message.getSequenceId()); // 兼容旧字�?

        // v7.1: 构建媒体数据VO（优先使用独立字段）
        if (message.getMediaUrl() != null || message.getMediaData() != null) {
            MessageVO.MediaDataVO mediaDataVO = MessageVO.MediaDataVO.builder()
                    .url(message.getMediaUrl() != null ? message.getMediaUrl() : message.getMediaUrlCompat())
                    .thumbnail(message.getThumbnailUrl() != null ? message.getThumbnailUrl() : message.getThumbnailUrlCompat())
                    .size(message.getFileSize())
                    .duration(message.getDuration())
                    .width(message.getMediaWidth())
                    .height(message.getMediaHeight())
                    .build();
            
            // 从媒体数据JSON中获取其他扩展字段（兼容旧数据）
            if (message.getMediaData() != null) {
                Map<String, Object> data = message.getMediaData();
                if (data.containsKey("longitude")) {
                    mediaDataVO.setLongitude((Double) data.get("longitude"));
                }
                if (data.containsKey("latitude")) {
                    mediaDataVO.setLatitude((Double) data.get("latitude"));
                }
                if (data.containsKey("address")) {
                    mediaDataVO.setAddress((String) data.get("address"));
                }
            }
            
            builder.mediaData(mediaDataVO);
        }

        // TODO: 添加发送者信息和回复消息信息的查询逻辑
        if (Boolean.TRUE.equals(includeSender) && message.getSenderId() != null) {
            // 查询发送者信�?
        }
        
        if (Boolean.TRUE.equals(includeReplyTo) && message.getReplyToId() != null) {
            // 查询回复的消息信�?
        }

        return builder.build();
    }

    // ========== v7.1新增：辅助方�?==========
    
    /**
     * 生成消息序列号（保证有序性）
     * v7.1: 使用Redis INCR保证全局递增
     */
    private Long generateSequenceId(Long conversationId) {
        // TODO: 使用Redis INCR实现
        // String key = "chat:sequence:" + conversationId;
        // return redisTemplate.opsForValue().increment(key);
        
        // 临时方案：使用时间戳 + 随机�?
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }

    /**
     * 更新会话最后消息冗余字段（v7.1性能优化�?
     */
    private void updateConversationLastMessage(Long conversationId, Long messageId, LocalDateTime messageTime) {
        try {
            chatMessageMapper.updateConversationLastMessage(conversationId, messageId, messageTime);
            log.debug("更新会话最后消息成功，会话ID：{}，消息ID：{}", conversationId, messageId);
        } catch (Exception e) {
            log.error("更新会话最后消息失败，会话ID：{}，错误：{}", conversationId, e.getMessage());
        }
    }

    /**
     * 增加其他参与者的未读数量（v7.1未读计数优化�?
     */
    private void incrementUnreadCountForOthers(Long conversationId, Long senderId) {
        try {
            chatParticipantMapper.incrementUnreadCountExcept(conversationId, senderId);
            log.debug("增加未读数量成功，会话ID：{}，发送者：{}", conversationId, senderId);
        } catch (Exception e) {
            log.error("增加未读数量失败，会话ID：{}，错误：{}", conversationId, e.getMessage());
        }
    }
}
