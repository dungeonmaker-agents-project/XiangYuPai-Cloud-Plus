package org.dromara.chat.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.chat.domain.dto.ChatHistoryQueryDTO;
import org.dromara.chat.domain.dto.ConversationQueryDTO;
import org.dromara.chat.domain.dto.MessageSendDTO;
import org.dromara.chat.domain.entity.Conversation;
import org.dromara.chat.domain.entity.Message;
import org.dromara.chat.domain.vo.ConversationVO;
import org.dromara.chat.domain.vo.MessageVO;
import org.dromara.chat.domain.vo.UnreadCountVO;
import org.dromara.chat.mapper.ConversationMapper;
import org.dromara.chat.mapper.MessageMapper;
import org.dromara.chat.service.IMessageService;
import org.dromara.chat.websocket.MessageWebSocketHandler;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message Service Implementation
 * 消息服务实现类
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final MessageWebSocketHandler webSocketHandler;

    // Cache key prefixes
    private static final String CACHE_UNREAD_COUNT = "chat:unread:count:";
    private static final String CACHE_CONVERSATION_LIST = "chat:conversation:list:";
    private static final String CACHE_ONLINE_STATUS = "chat:online:";

    // Cache TTL (seconds)
    private static final Duration TTL_UNREAD_COUNT = Duration.ofMinutes(3);
    private static final Duration TTL_CONVERSATION_LIST = Duration.ofMinutes(5);
    private static final Duration TTL_ONLINE_STATUS = Duration.ofMinutes(5);

    // Message recall timeout (2 minutes)
    private static final long RECALL_TIMEOUT_MILLIS = 2 * 60 * 1000;

    @Override
    public UnreadCountVO getUnreadCount(Long userId) {
        String cacheKey = CACHE_UNREAD_COUNT + userId;

        // 1. Try cache
        UnreadCountVO cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for unread count: userId={}", userId);
            return cached;
        }

        // 2. Query database
        Integer conversationUnread = conversationMapper.getTotalUnreadCount(userId);
        if (conversationUnread == null) {
            conversationUnread = 0;
        }

        // 3. Build result (notification counts would come from NotificationService RPC)
        UnreadCountVO result = UnreadCountVO.builder()
            .likes(0)  // TODO: Call NotificationService RPC
            .comments(0)  // TODO: Call NotificationService RPC
            .followers(0)  // TODO: Call NotificationService RPC
            .system(0)  // TODO: Call NotificationService RPC
            .total(conversationUnread)
            .build();

        // 4. Cache result
        RedisUtils.setCacheObject(cacheKey, result, TTL_UNREAD_COUNT);
        log.debug("Cached unread count: userId={}, total={}", userId, result.getTotal());

        return result;
    }

    @Override
    public Page<ConversationVO> getConversations(Long userId, ConversationQueryDTO queryDTO) {
        String cacheKey = CACHE_CONVERSATION_LIST + userId + ":page:" + queryDTO.getPage();

        // 1. Try cache (only for first page)
        if (queryDTO.getPage() == 1) {
            Page<ConversationVO> cached = RedisUtils.getCacheObject(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for conversation list: userId={}, page={}", userId, queryDTO.getPage());
                return cached;
            }
        }

        // 2. Build query
        Page<Conversation> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
            .eq(Conversation::getDeleted, 0)
            .orderByDesc(Conversation::getLastMessageTime);

        // 3. Execute query
        Page<Conversation> result = conversationMapper.selectPage(page, wrapper);

        // 4. Convert to VOs
        List<ConversationVO> voList = result.getRecords().stream()
            .map(conv -> {
                // TODO: Batch get user info via RPC
                // TODO: Check online status from Redis
                return ConversationVO.builder()
                    .conversationId(conv.getId())
                    .userId(conv.getOtherUserId())
                    .nickname("User" + conv.getOtherUserId())  // TODO: Get from UserService
                    .avatar("")  // TODO: Get from UserService
                    .lastMessage(conv.getLastMessage())
                    .lastMessageTime(conv.getLastMessageTime())
                    .unreadCount(conv.getUnreadCount())
                    .isOnline(isUserOnline(conv.getOtherUserId()))
                    .build();
            })
            .collect(Collectors.toList());

        // 5. Build paginated result
        Page<ConversationVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(result.getTotal());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setPages(result.getPages());

        // 6. Cache first page
        if (queryDTO.getPage() == 1) {
            RedisUtils.setCacheObject(cacheKey, voPage, TTL_CONVERSATION_LIST);
            log.debug("Cached conversation list: userId={}, page={}, total={}",
                userId, queryDTO.getPage(), voPage.getTotal());
        }

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long conversationId) {
        // 1. Verify ownership
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new ServiceException("会话不存在或无权限删除");
        }

        // 2. Soft delete
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getId, conversationId)
            .eq(Conversation::getUserId, userId)
            .set(Conversation::getDeleted, 1)
            .set(Conversation::getDeletedAt, LocalDateTime.now());

        conversationMapper.update(null, wrapper);

        // 3. Clear cache
        clearUserCache(userId);
        log.info("Deleted conversation: userId={}, conversationId={}", userId, conversationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllMessages(Long userId) {
        // 1. Soft delete all conversations
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
            .eq(Conversation::getDeleted, 0)
            .set(Conversation::getDeleted, 1)
            .set(Conversation::getDeletedAt, LocalDateTime.now())
            .set(Conversation::getUnreadCount, 0);

        conversationMapper.update(null, wrapper);

        // 2. Clear cache
        clearUserCache(userId);
        log.info("Cleared all messages: userId={}", userId);
    }

    @Override
    public Page<MessageVO> getChatHistory(Long userId, ChatHistoryQueryDTO queryDTO) {
        // 1. Verify conversation access
        Conversation conversation = conversationMapper.selectById(queryDTO.getConversationId());
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new ServiceException("会话不存在或无权限访问");
        }

        // 2. Build query
        Page<Message> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, queryDTO.getConversationId())
            .eq(Message::getDeleted, 0)
            .orderByDesc(Message::getCreateTime);

        // 3. Execute query
        Page<Message> result = messageMapper.selectPage(page, wrapper);

        // 4. Convert to VOs
        List<MessageVO> voList = result.getRecords().stream()
            .map(this::convertToMessageVO)
            .collect(Collectors.toList());

        // 5. Build paginated result
        Page<MessageVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(result.getTotal());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setPages(result.getPages());

        log.debug("Retrieved chat history: conversationId={}, page={}, total={}",
            queryDTO.getConversationId(), queryDTO.getPage(), voPage.getTotal());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long userId, MessageSendDTO sendDTO) {
        // 1. Validate message
        validateMessage(sendDTO);

        // 2. Check if conversation exists
        Conversation conversation = conversationMapper.selectById(sendDTO.getConversationId());
        if (conversation == null) {
            // Create new conversation (bidirectional)
            conversation = createConversation(userId, sendDTO.getReceiverId());
            createConversation(sendDTO.getReceiverId(), userId);
        }

        // 3. Create message
        Message message = Message.builder()
            .conversationId(sendDTO.getConversationId())
            .senderId(userId)
            .receiverId(sendDTO.getReceiverId())
            .messageType(sendDTO.getMessageType())
            .content(sendDTO.getContent())
            .mediaUrl(sendDTO.getMediaUrl())
            .thumbnailUrl(sendDTO.getThumbnailUrl())
            .duration(sendDTO.getDuration())
            .status(1)  // Delivered
            .isRecalled(false)
            .build();

        messageMapper.insert(message);

        // 4. Update conversation
        String lastMessagePreview = getMessagePreview(sendDTO);
        updateConversationLastMessage(conversation.getId(), lastMessagePreview);

        // 5. Increment receiver's unread count
        incrementUnreadCount(sendDTO.getReceiverId(), userId);

        // 6. Clear cache
        clearUserCache(userId);
        clearUserCache(sendDTO.getReceiverId());

        // 7. Send via WebSocket if receiver is online
        if (webSocketHandler.isUserOnline(sendDTO.getReceiverId())) {
            webSocketHandler.sendNewMessage(sendDTO.getReceiverId(), java.util.Map.of(
                "messageId", message.getId(),
                "senderId", userId,
                "messageType", sendDTO.getMessageType(),
                "content", sendDTO.getContent() != null ? sendDTO.getContent() : "",
                "mediaUrl", sendDTO.getMediaUrl() != null ? sendDTO.getMediaUrl() : "",
                "timestamp", message.getCreateTime().toString()
            ));
        }

        // 8. TODO: Send offline push notification if receiver is offline

        log.info("Message sent: senderId={}, receiverId={}, type={}, online={}",
            userId, sendDTO.getReceiverId(), sendDTO.getMessageType(),
            webSocketHandler.isUserOnline(sendDTO.getReceiverId()));

        return convertToMessageVO(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer markMessagesAsRead(Long userId, Long conversationId) {
        // 1. Verify conversation ownership
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new ServiceException("会话不存在或无权限");
        }

        // 2. Update message status to "read"
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
            .eq(Message::getReceiverId, userId)
            .eq(Message::getStatus, 1)  // Only mark "delivered" messages
            .set(Message::getStatus, 2);  // Set to "read"

        int readCount = messageMapper.update(null, wrapper);

        // 3. Clear unread count for this conversation
        LambdaUpdateWrapper<Conversation> convWrapper = new LambdaUpdateWrapper<>();
        convWrapper.eq(Conversation::getId, conversationId)
            .eq(Conversation::getUserId, userId)
            .set(Conversation::getUnreadCount, 0);

        conversationMapper.update(null, convWrapper);

        // 4. Clear cache
        clearUserCache(userId);

        // 5. Send read receipt via WebSocket to sender(s)
        // Note: In a real implementation, we'd need to track which messages were read
        // and notify the respective senders. For now, we'll log this action.
        log.debug("Messages marked as read, WebSocket notifications would be sent to senders");

        log.debug("Marked messages as read: userId={}, conversationId={}, count={}",
            userId, conversationId, readCount);

        return readCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long userId, Long messageId) {
        // 1. Get message
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 2. Verify ownership
        if (!message.getSenderId().equals(userId)) {
            throw new ServiceException("只能撤回自己发送的消息");
        }

        // 3. Check time limit (2 minutes)
        long timeDiff = System.currentTimeMillis() - message.getCreateTime().getTime();

        if (timeDiff > RECALL_TIMEOUT_MILLIS) {
            throw new ServiceException("超过2分钟，无法撤回");
        }

        // 4. Mark as recalled
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getId, messageId)
            .set(Message::getIsRecalled, true)
            .set(Message::getRecalledAt, LocalDateTime.now());

        messageMapper.update(null, wrapper);

        // 5. Send recall notification via WebSocket to receiver
        if (webSocketHandler.isUserOnline(message.getReceiverId())) {
            webSocketHandler.sendMessageRecalled(message.getReceiverId(), java.util.Map.of(
                "messageId", messageId,
                "senderId", userId,
                "timestamp", System.currentTimeMillis()
            ));
        }

        log.info("Message recalled: userId={}, messageId={}, receiverOnline={}",
            userId, messageId, webSocketHandler.isUserOnline(message.getReceiverId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long userId, Long messageId) {
        // 1. Get message
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 2. Verify permission (can delete if sender or receiver)
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
            throw new ServiceException("无权限删除该消息");
        }

        // 3. Soft delete
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getId, messageId)
            .set(Message::getDeleted, 1)
            .set(Message::getDeletedAt, LocalDateTime.now());

        messageMapper.update(null, wrapper);

        log.info("Message deleted: userId={}, messageId={}", userId, messageId);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Convert Message entity to MessageVO
     */
    private MessageVO convertToMessageVO(Message message) {
        return MessageVO.builder()
            .messageId(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .receiverId(message.getReceiverId())
            .messageType(message.getMessageType())
            .content(message.getContent())
            .mediaUrl(message.getMediaUrl())
            .thumbnailUrl(message.getThumbnailUrl())
            .duration(message.getDuration())
            .status(message.getStatus())
            .isRecalled(message.getIsRecalled())
            .createdAt(message.getCreateTime() != null ?
                LocalDateTime.ofInstant(message.getCreateTime().toInstant(), ZoneId.systemDefault()) : null)
            .build();
    }

    /**
     * Validate message DTO
     */
    private void validateMessage(MessageSendDTO dto) {
        switch (dto.getMessageType()) {
            case "text":
                if (StrUtil.isBlank(dto.getContent())) {
                    throw new ServiceException("文字消息内容不能为空");
                }
                if (dto.getContent().length() > 500) {
                    throw new ServiceException("文字消息不能超过500字符");
                }
                break;
            case "image":
                if (StrUtil.isBlank(dto.getMediaUrl())) {
                    throw new ServiceException("图片消息mediaUrl不能为空");
                }
                break;
            case "voice":
                if (StrUtil.isBlank(dto.getMediaUrl()) || dto.getDuration() == null) {
                    throw new ServiceException("语音消息mediaUrl和duration不能为空");
                }
                if (dto.getDuration() < 1 || dto.getDuration() > 60) {
                    throw new ServiceException("语音时长必须在1-60秒之间");
                }
                break;
            case "video":
                if (StrUtil.isBlank(dto.getMediaUrl()) ||
                    StrUtil.isBlank(dto.getThumbnailUrl()) ||
                    dto.getDuration() == null) {
                    throw new ServiceException("视频消息mediaUrl、thumbnailUrl和duration不能为空");
                }
                if (dto.getDuration() < 1 || dto.getDuration() > 60) {
                    throw new ServiceException("视频时长必须在1-60秒之间");
                }
                break;
            default:
                throw new ServiceException("不支持的消息类型");
        }
    }

    /**
     * Create new conversation
     */
    private Conversation createConversation(Long userId, Long otherUserId) {
        Conversation conversation = Conversation.builder()
            .userId(userId)
            .otherUserId(otherUserId)
            .lastMessage("")
            .lastMessageTime(LocalDateTime.now())
            .unreadCount(0)
            .build();

        conversationMapper.insert(conversation);
        return conversation;
    }

    /**
     * Get message preview for conversation list
     */
    private String getMessagePreview(MessageSendDTO dto) {
        return switch (dto.getMessageType()) {
            case "text" -> dto.getContent();
            case "image" -> "[图片]";
            case "voice" -> "[语音]";
            case "video" -> "[视频]";
            default -> "[消息]";
        };
    }

    /**
     * Update conversation's last message
     */
    private void updateConversationLastMessage(Long conversationId, String lastMessage) {
        LambdaUpdateWrapper<Conversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Conversation::getId, conversationId)
            .set(Conversation::getLastMessage, lastMessage)
            .set(Conversation::getLastMessageTime, LocalDateTime.now());

        conversationMapper.update(null, wrapper);
    }

    /**
     * Increment unread count for receiver
     */
    private void incrementUnreadCount(Long receiverId, Long senderId) {
        // Find or create receiver's conversation
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, receiverId)
            .eq(Conversation::getOtherUserId, senderId)
            .eq(Conversation::getDeleted, 0);

        Conversation conversation = conversationMapper.selectOne(wrapper);
        if (conversation != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, conversation.getId())
                .setSql("unread_count = unread_count + 1");

            conversationMapper.update(null, updateWrapper);
        }
    }

    /**
     * Check if user is online
     */
    private Boolean isUserOnline(Long userId) {
        // First check WebSocket connection (most accurate)
        if (webSocketHandler.isUserOnline(userId)) {
            return true;
        }

        // Fallback to Redis cache (for cases where WebSocket wasn't used)
        String cacheKey = CACHE_ONLINE_STATUS + userId;
        String status = RedisUtils.getCacheObject(cacheKey);
        return "1".equals(status);
    }

    /**
     * Clear user's cache
     */
    private void clearUserCache(Long userId) {
        String unreadKey = CACHE_UNREAD_COUNT + userId;
        String listKeyPattern = CACHE_CONVERSATION_LIST + userId + ":*";

        RedisUtils.deleteObject(unreadKey);
        // TODO: Delete all conversation list cache keys for this user
        log.debug("Cleared cache for user: {}", userId);
    }
}
