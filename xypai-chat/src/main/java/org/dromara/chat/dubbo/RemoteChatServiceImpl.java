package org.dromara.chat.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.chat.api.RemoteChatService;
import org.dromara.chat.api.domain.vo.RemoteChatConversationVo;
import org.dromara.chat.api.domain.vo.RemoteChatMessageVo;
import org.dromara.chat.api.domain.vo.RemoteChatUnreadCountVo;
import org.dromara.chat.domain.entity.Conversation;
import org.dromara.chat.domain.entity.Message;
import org.dromara.chat.domain.vo.UnreadCountVO;
import org.dromara.chat.mapper.ConversationMapper;
import org.dromara.chat.mapper.MessageMapper;
import org.dromara.chat.service.IMessageService;
import org.dromara.chat.websocket.MessageWebSocketHandler;
import org.dromara.common.core.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Remote Chat Service Implementation (Dubbo Provider)
 * 聊天远程服务实现（Dubbo服务提供者）
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteChatServiceImpl implements RemoteChatService {

    private final IMessageService messageService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final MessageWebSocketHandler webSocketHandler;

    @Override
    public RemoteChatUnreadCountVo getUserUnreadCount(Long userId) throws ServiceException {
        try {
            UnreadCountVO unreadCount = messageService.getUnreadCount(userId);

            return RemoteChatUnreadCountVo.builder()
                .chatMessages(unreadCount.getTotal())
                .likes(unreadCount.getLikes())
                .comments(unreadCount.getComments())
                .followers(unreadCount.getFollowers())
                .system(unreadCount.getSystem())
                .total(unreadCount.getTotal() + unreadCount.getLikes() +
                    unreadCount.getComments() + unreadCount.getFollowers() +
                    unreadCount.getSystem())
                .build();

        } catch (Exception e) {
            log.error("Failed to get user unread count via RPC: userId={}", userId, e);
            throw new ServiceException("获取未读消息数失败");
        }
    }

    @Override
    public List<RemoteChatConversationVo> getUserConversations(Long userId, Integer limit) throws ServiceException {
        try {
            LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getDeleted, 0)
                .orderByDesc(Conversation::getLastMessageTime)
                .last("LIMIT " + (limit != null ? limit : 20));

            List<Conversation> conversations = conversationMapper.selectList(wrapper);

            return conversations.stream()
                .map(this::convertToRemoteConversationVo)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to get user conversations via RPC: userId={}, limit={}", userId, limit, e);
            throw new ServiceException("获取会话列表失败");
        }
    }

    @Override
    public RemoteChatConversationVo getConversationById(Long userId, Long conversationId) throws ServiceException {
        try {
            Conversation conversation = conversationMapper.selectById(conversationId);

            if (conversation == null) {
                throw new ServiceException("会话不存在");
            }

            if (!conversation.getUserId().equals(userId)) {
                throw new ServiceException("无权限访问该会话");
            }

            return convertToRemoteConversationVo(conversation);

        } catch (Exception e) {
            log.error("Failed to get conversation via RPC: userId={}, conversationId={}",
                userId, conversationId, e);
            throw new ServiceException("获取会话信息失败");
        }
    }

    @Override
    public List<RemoteChatMessageVo> getConversationMessages(Long userId, Long conversationId, Integer limit)
        throws ServiceException {
        try {
            // Verify conversation access
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                throw new ServiceException("会话不存在或无权限访问");
            }

            // Query messages
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getDeleted, 0)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 50));

            List<Message> messages = messageMapper.selectList(wrapper);

            return messages.stream()
                .map(this::convertToRemoteMessageVo)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to get conversation messages via RPC: userId={}, conversationId={}, limit={}",
                userId, conversationId, limit, e);
            throw new ServiceException("获取消息列表失败");
        }
    }

    @Override
    public RemoteChatMessageVo getMessageById(Long userId, Long messageId) throws ServiceException {
        try {
            Message message = messageMapper.selectById(messageId);

            if (message == null) {
                throw new ServiceException("消息不存在");
            }

            // Verify permission (user must be sender or receiver)
            if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
                throw new ServiceException("无权限访问该消息");
            }

            return convertToRemoteMessageVo(message);

        } catch (Exception e) {
            log.error("Failed to get message via RPC: userId={}, messageId={}", userId, messageId, e);
            throw new ServiceException("获取消息信息失败");
        }
    }

    @Override
    public boolean isUserOnline(Long userId) {
        try {
            return webSocketHandler.isUserOnline(userId);
        } catch (Exception e) {
            log.error("Failed to check user online status via RPC: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();

        try {
            for (Long userId : userIds) {
                result.put(userId, webSocketHandler.isUserOnline(userId));
            }
        } catch (Exception e) {
            log.error("Failed to batch check online status via RPC: userIds={}", userIds, e);
        }

        return result;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Convert Conversation entity to RemoteChatConversationVo
     */
    private RemoteChatConversationVo convertToRemoteConversationVo(Conversation conversation) {
        return RemoteChatConversationVo.builder()
            .conversationId(conversation.getId())
            .userId(conversation.getUserId())
            .otherUserId(conversation.getOtherUserId())
            .lastMessage(conversation.getLastMessage())
            .lastMessageTime(conversation.getLastMessageTime())
            .unreadCount(conversation.getUnreadCount())
            .isOnline(webSocketHandler.isUserOnline(conversation.getOtherUserId()))
            .createTime(conversation.getCreateTime())
            .build();
    }

    /**
     * Convert Message entity to RemoteChatMessageVo
     */
    private RemoteChatMessageVo convertToRemoteMessageVo(Message message) {
        return RemoteChatMessageVo.builder()
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
            .createdAt(message.getCreateTime())
            .build();
    }
}
