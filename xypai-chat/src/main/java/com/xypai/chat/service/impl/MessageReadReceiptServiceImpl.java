package com.xypai.chat.service.impl;

import com.xypai.chat.domain.entity.ChatMessage;
import com.xypai.chat.domain.entity.ChatParticipant;
import com.xypai.chat.domain.vo.MessageVO;
import com.xypai.chat.mapper.ChatMessageMapper;
import com.xypai.chat.mapper.ChatParticipantMapper;
import com.xypai.chat.service.IMessageReadReceiptService;
import com.xypai.chat.websocket.ChatWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 消息已读回执服务实现 (v7.1新增)
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageReadReceiptServiceImpl implements IMessageReadReceiptService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatParticipantMapper chatParticipantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordMessageRead(Long messageId, Long userId, LocalDateTime readTime) {
        if (messageId == null || userId == null) {
            return false;
        }

        try {
            // v7.1: 记录已读状态（使用单独的已读记录表，或更新消息的read_count）
            int result = chatMessageMapper.updateMessageReadStatus(messageId, userId, readTime);
            
            if (result > 0) {
                log.debug("记录消息已读成功，消息ID：{}，用户ID：{}", messageId, userId);
                
                // 推送已读回执给发送者
                pushReadReceiptToSender(messageId, userId);
                
                return true;
            }
        } catch (Exception e) {
            log.error("记录消息已读失败，消息ID：{}，用户ID：{}", messageId, userId, e);
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRecordMessageRead(List<Long> messageIds, Long userId, LocalDateTime readTime) {
        if (messageIds == null || messageIds.isEmpty() || userId == null) {
            return 0;
        }

        int successCount = 0;
        for (Long messageId : messageIds) {
            if (recordMessageRead(messageId, userId, readTime)) {
                successCount++;
            }
        }

        log.info("批量记录消息已读完成，成功：{}/{}", successCount, messageIds.size());
        return successCount;
    }

    @Override
    public List<MessageVO.ReadStatusVO> getMessageReadStatus(Long messageId) {
        if (messageId == null) {
            return List.of();
        }

        try {
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
        } catch (Exception e) {
            log.error("查询消息已读状态失败，消息ID：{}", messageId, e);
            return List.of();
        }
    }

    @Override
    public int countMessageReadUsers(Long messageId) {
        if (messageId == null) {
            return 0;
        }

        try {
            List<Map<String, Object>> readUsers = chatMessageMapper.selectMessageReadUsers(messageId);
            return (int) readUsers.stream()
                    .filter(user -> user.get("read_at") != null)
                    .count();
        } catch (Exception e) {
            log.error("统计消息已读人数失败，消息ID：{}", messageId, e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeliveryStatus(Long messageId, Integer deliveryStatus) {
        if (messageId == null || deliveryStatus == null) {
            return false;
        }

        try {
            ChatMessage updateMessage = ChatMessage.builder()
                    .id(messageId)
                    .deliveryStatus(deliveryStatus)
                    .build();
            
            int result = chatMessageMapper.updateById(updateMessage);
            
            if (result > 0) {
                log.debug("更新投递状态成功，消息ID：{}，状态：{}", messageId, deliveryStatus);
                return true;
            }
        } catch (Exception e) {
            log.error("更新投递状态失败，消息ID：{}", messageId, e);
        }
        
        return false;
    }

    @Override
    public boolean pushReadReceiptToSender(Long messageId, Long readUserId) {
        if (messageId == null || readUserId == null) {
            return false;
        }

        try {
            // 查询消息
            ChatMessage message = chatMessageMapper.selectById(messageId);
            if (message == null || message.getSenderId() == null) {
                return false;
            }

            // 检查发送者是否在线
            if (!ChatWebSocketServer.isUserOnline(message.getSenderId())) {
                return false;
            }

            // 构建已读回执消息
            Map<String, Object> readReceipt = new ConcurrentHashMap<>();
            readReceipt.put("type", "read_receipt");
            readReceipt.put("messageId", messageId);
            readReceipt.put("conversationId", message.getConversationId());
            readReceipt.put("readUserId", readUserId);
            readReceipt.put("readTime", LocalDateTime.now());

            // 推送给发送者
            ChatWebSocketServer.sendMessageToUser(message.getSenderId(), readReceipt);
            
            log.debug("推送已读回执成功，消息ID：{}，发送者：{}，已读用户：{}", 
                    messageId, message.getSenderId(), readUserId);
            
            return true;
        } catch (Exception e) {
            log.error("推送已读回执失败，消息ID：{}", messageId, e);
            return false;
        }
    }

    @Override
    public boolean isAllMembersRead(Long messageId, Long conversationId) {
        if (messageId == null || conversationId == null) {
            return false;
        }

        try {
            // 查询会话成员数量
            Integer memberCount = chatParticipantMapper.countParticipants(
                    conversationId, ChatParticipant.Status.NORMAL.getCode());
            
            // 查询已读人数
            int readCount = countMessageReadUsers(messageId);
            
            // 排除发送者自己
            return readCount >= (memberCount - 1);
            
        } catch (Exception e) {
            log.error("检查是否所有人已读失败，消息ID：{}", messageId, e);
            return false;
        }
    }
}

