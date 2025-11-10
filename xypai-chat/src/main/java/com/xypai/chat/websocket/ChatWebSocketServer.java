package com.xypai.chat.websocket;

import com.alibaba.fastjson2.JSON;
import com.xypai.chat.constant.ChatConstants;
import com.xypai.chat.domain.dto.MessageSendDTO;
import com.xypai.chat.domain.entity.ChatMessage;
import com.xypai.chat.domain.vo.ConversationDetailVO;
import com.xypai.chat.domain.vo.MessageVO;
import com.xypai.chat.service.IChatConversationService;
import com.xypai.chat.service.IChatMessageService;
import com.xypai.chat.utils.WebSocketUtils;
import org.dromara.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket聊天服务�?(v7.1)
 * 
 * 功能�?
 * - 消息实时推�?
 * - 在线状态管�?
 * - 消息去重
 * - 离线消息处理
 * - 正在输入状�?
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}/{token}")
public class ChatWebSocketServer {

    // ========== 静态变�?==========
    
    /**
     * 存储所有在线连�?(userId -> Session)
     */
    private static final ConcurrentHashMap<Long, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 在线人数统计
     */
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    // ========== 依赖注入（WebSocket特殊处理�?==========
    
    private static IChatMessageService chatMessageService;
    private static IChatConversationService chatConversationService;

    @Autowired
    public void setChatMessageService(IChatMessageService service) {
        ChatWebSocketServer.chatMessageService = service;
    }

    @Autowired
    public void setChatConversationService(IChatConversationService service) {
        ChatWebSocketServer.chatConversationService = service;
    }

    // ========== WebSocket生命周期 ==========
    
    /**
     * 连接建立成功调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId, @PathParam("token") String token) {
        try {
            // v7.1: 验证Token
            if (!validateToken(token, userId)) {
                sendErrorMessage(session, "Token验证失败，连接已关闭");
                session.close();
                return;
            }

            // 保存连接
            Session oldSession = SESSION_MAP.put(userId, session);
            
            // 如果用户已在其他地方登录，关闭旧连接
            if (oldSession != null && oldSession.isOpen()) {
                try {
                    sendMessage(oldSession, buildSystemMessage("您的账号在其他设备登录"));
                    oldSession.close();
                    log.info("用户{}在其他设备登录，关闭旧连接", userId);
                } catch (Exception e) {
                    log.error("关闭旧连接失败", e);
                }
            }

            // 增加在线人数
            int count = ONLINE_COUNT.incrementAndGet();

            // TODO: 更新用户在线状态到数据�?Redis
            // userService.updateOnlineStatus(userId, true);

            log.info("用户{}上线，当前在线人数：{}", userId, count);

            // 发送连接成功消�?
            sendMessage(session, buildSystemMessage("WebSocket连接成功"));

            // TODO: 推送离线消�?
            // pushOfflineMessages(userId, session);

        } catch (Exception e) {
            log.error("WebSocket连接异常，用户ID：{}", userId, e);
        }
    }

    /**
     * 接收客户端消�?
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        try {
            if (StringUtils.isBlank(message)) {
                return;
            }

            log.debug("收到用户{}的消息：{}", userId, message);

            // 解析消息JSON
            WebSocketMessage wsMessage = JSON.parseObject(message, WebSocketMessage.class);
            
            // 根据消息类型处理
            switch (wsMessage.getType()) {
                case "chat":
                    // 聊天消息
                    handleChatMessage(wsMessage, userId);
                    break;
                case "typing":
                    // 正在输入状�?
                    handleTypingStatus(wsMessage, userId);
                    break;
                case "read":
                    // 已读回执
                    handleReadReceipt(wsMessage, userId);
                    break;
                case "heartbeat":
                    // 心跳
                    handleHeartbeat(session, userId);
                    break;
                default:
                    log.warn("未知消息类型：{}", wsMessage.getType());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息异常，用户ID：{}", userId, e);
            sendErrorMessage(session, "消息处理失败：" + e.getMessage());
        }
    }

    /**
     * 连接关闭调用
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        // 移除连接
        SESSION_MAP.remove(userId);
        
        // 减少在线人数
        int count = ONLINE_COUNT.decrementAndGet();

        // TODO: 更新用户离线状�?
        // userService.updateOnlineStatus(userId, false);

        log.info("用户{}下线，当前在线人数：{}", userId, count);
    }

    /**
     * 连接错误调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        log.error("WebSocket错误，用户ID：{}", userId, error);
        
        // 尝试发送错误消息
        sendErrorMessage(session, "连接异常：" + error.getMessage());
    }

    // ========== 消息处理 ==========
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketMessage wsMessage, Long senderId) {
        try {
            MessageSendDTO sendDTO = JSON.parseObject(
                JSON.toJSONString(wsMessage.getData()), MessageSendDTO.class);

            // v7.1: 消息去重检�?
            if (sendDTO.getClientId() == null) {
                log.warn("消息缺少clientId，可能导致重复，发送者：{}", senderId);
            }

            // 保存消息到数据库
            Long messageId = chatMessageService.sendMessage(sendDTO);

            // 查询完整消息
            MessageVO savedMessage = chatMessageService.selectMessageById(messageId);

            // 推送给会话内的所有成�?
            List<Long> memberIds = getMemberIds(sendDTO.getConversationId());
            for (Long memberId : memberIds) {
                if (!memberId.equals(senderId)) {
                    // v7.1: 推送完整消息对�?
                    sendMessageToUser(memberId, buildChatMessage(savedMessage));
                    
                    // v7.1: 已读数量已在Service层自动增�?
                }
            }

            // 给发送者发送成功回�?
            Session senderSession = SESSION_MAP.get(senderId);
            if (senderSession != null) {
                sendMessage(senderSession, buildMessageAck(messageId, sendDTO.getClientId()));
            }

            log.info("WebSocket消息处理成功，消息ID：{}，会话ID：{}，发送者：{}", 
                    messageId, sendDTO.getConversationId(), senderId);

        } catch (Exception e) {
            log.error("处理聊天消息失败，发送者：{}", senderId, e);
            Session session = SESSION_MAP.get(senderId);
            if (session != null) {
                sendErrorMessage(session, "消息发送失败：" + e.getMessage());
            }
        }
    }

    /**
     * 处理正在输入状�?
     */
    private void handleTypingStatus(WebSocketMessage wsMessage, Long userId) {
        try {
            Long conversationId = (Long) wsMessage.getData().get("conversationId");
            Boolean isTyping = (Boolean) wsMessage.getData().get("isTyping");

            if (conversationId == null) {
                return;
            }

            // TODO: 保存到Redis
            // String key = "typing:" + conversationId + ":" + userId;
            // redisTemplate.opsForValue().set(key, "1", 10, TimeUnit.SECONDS);

            // 推送给会话其他成员
            List<Long> memberIds = getMemberIds(conversationId);
            for (Long memberId : memberIds) {
                if (!memberId.equals(userId)) {
                    sendMessageToUser(memberId, buildTypingMessage(conversationId, userId, isTyping));
                }
            }

            log.debug("处理正在输入状态，会话ID：{}，用户ID：{}，是否输入：{}", 
                    conversationId, userId, isTyping);

        } catch (Exception e) {
            log.error("处理输入状态失败，用户ID：{}", userId, e);
        }
    }

    /**
     * 处理已读回执
     */
    private void handleReadReceipt(WebSocketMessage wsMessage, Long userId) {
        try {
            Long conversationId = (Long) wsMessage.getData().get("conversationId");
            Long messageId = (Long) wsMessage.getData().get("messageId");

            if (conversationId == null) {
                return;
            }

            // v7.1: 标记消息已读（更新精确位置）
            chatMessageService.markMessageAsRead(conversationId, messageId);

            // 推送已读回执给发送�?
            // TODO: 查询消息发送者，推送已读回�?

            log.debug("处理已读回执，会话ID：{}，消息ID：{}，用户ID：{}", 
                    conversationId, messageId, userId);

        } catch (Exception e) {
            log.error("处理已读回执失败，用户ID：{}", userId, e);
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(Session session, Long userId) {
        try {
            // 回复心跳
            sendMessage(session, buildHeartbeatResponse());
            
            // TODO: 更新最后活跃时间到Redis
            // String key = "user:active:" + userId;
            // redisTemplate.opsForValue().set(key, System.currentTimeMillis(), 5, TimeUnit.MINUTES);

            log.trace("心跳响应，用户ID：{}", userId);

        } catch (Exception e) {
            log.error("处理心跳失败，用户ID：{}", userId, e);
        }
    }

    // ========== 消息发送工�?==========
    
    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(Long userId, Object message) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        } else {
            // 用户离线，推送离线消�?
            log.debug("用户{}离线，消息推送到离线队列", userId);
            // TODO: 推送到离线消息队列（APNs/FCM�?
            // offlinePushService.push(userId, message);
        }
    }

    /**
     * 发送消息到Session
     */
    private static void sendMessage(Session session, Object message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            synchronized (session) {
                session.getBasicRemote().sendText(JSON.toJSONString(message));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 发送错误消�?
     */
    private void sendErrorMessage(Session session, String errorMsg) {
        sendMessage(session, WebSocketMessage.builder()
                .type("error")
                .data(new ConcurrentHashMap<String, Object>() {{
                    put("message", errorMsg);
                }})
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * 广播消息给会话所有成�?
     */
    public static void broadcastToConversation(Long conversationId, Object message, Long excludeUserId) {
        // TODO: 查询会话成员
        log.debug("广播消息到会话{}，排除用户：{}", conversationId, excludeUserId);
    }

    // ========== 辅助方法 ==========
    
    /**
     * 验证Token
     */
    private boolean validateToken(String token, Long userId) {
        if (StringUtils.isBlank(token)) {
            log.warn("Token为空，用户ID：{}", userId);
            return false;
        }

        // TODO: 实现JWT Token验证
        // try {
        //     Claims claims = JwtUtils.parseToken(token);
        //     Long tokenUserId = claims.get("userId", Long.class);
        //     return userId.equals(tokenUserId);
        // } catch (Exception e) {
        //     log.error("Token验证失败", e);
        //     return false;
        // }

        // 临时方案：允许所有连接（开发环境）
        log.warn("⚠️ 开发模式：跳过Token验证，用户ID：{}", userId);
        return true;
    }

    /**
     * 获取会话成员ID列表（v7.1优化：使用工具方法）
     */
    private List<Long> getMemberIds(Long conversationId) {
        try {
            // TODO: 优化为从Redis缓存读取
            // String key = ChatConstants.REDIS_KEY_CONVERSATION_MEMBERS + conversationId;
            // Set<String> memberIds = redisTemplate.opsForSet().members(key);
            // if (memberIds != null && !memberIds.isEmpty()) {
            //     return memberIds.stream().map(Long::parseLong).toList();
            // }
            
            // 从数据库查询
            List<ConversationDetailVO.ParticipantVO> participants = 
                chatConversationService.getConversationParticipants(conversationId, false);
            
            return participants.stream()
                    .map(ConversationDetailVO.ParticipantVO::getUserId)
                    .toList();
        } catch (Exception e) {
            log.error("获取会话成员列表失败，会话ID：{}", conversationId, e);
            return List.of();
        }
    }

    /**
     * 构建聊天消息对象
     */
    private WebSocketMessage buildChatMessage(MessageVO message) {
        return WebSocketMessage.builder()
                .type("chat")
                .data(new ConcurrentHashMap<String, Object>() {{
                    put("messageId", message.getId());
                    put("conversationId", message.getConversationId());
                    put("senderId", message.getSenderId());
                    put("messageType", message.getMessageType());
                    put("content", message.getContent());
                    if (message.getMediaData() != null) {
                        put("mediaUrl", message.getMediaData().getUrl());
                        put("thumbnailUrl", message.getMediaData().getThumbnail());
                    }
                    put("clientId", message.getClientId());
                    put("sequenceId", message.getSequenceId());
                    put("deliveryStatus", message.getDeliveryStatus());
                    put("createdAt", message.getCreatedAt());
                }})
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建系统消息
     */
    private WebSocketMessage buildSystemMessage(String content) {
        return WebSocketMessage.builder()
                .type("system")
                .data(new ConcurrentHashMap<String, Object>() {{
                    put("content", content);
                }})
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建消息发送成功回执（v7.1优化：使用工具类）
     */
    private WebSocketMessage buildMessageAck(Long messageId, String clientId) {
        java.util.Map<String, Object> ackData = WebSocketUtils.buildMessageAck(messageId, clientId);
        return WebSocketMessage.builder()
                .type((String) ackData.get("type"))
                .data((ConcurrentHashMap<String, Object>) ackData.get("data"))
                .timestamp((Long) ackData.get("timestamp"))
                .build();
    }

    /**
     * 构建正在输入消息
     */
    private WebSocketMessage buildTypingMessage(Long conversationId, Long userId, Boolean isTyping) {
        return WebSocketMessage.builder()
                .type("typing")
                .data(new ConcurrentHashMap<String, Object>() {{
                    put("conversationId", conversationId);
                    put("userId", userId);
                    put("isTyping", isTyping);
                }})
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建心跳响应（v7.1优化：使用工具类）
     */
    private WebSocketMessage buildHeartbeatResponse() {
        java.util.Map<String, Object> heartbeat = WebSocketUtils.buildHeartbeatResponse();
        return WebSocketMessage.builder()
                .type((String) heartbeat.get("type"))
                .data((ConcurrentHashMap<String, Object>) heartbeat.get("data"))
                .timestamp((Long) heartbeat.get("timestamp"))
                .build();
    }

    /**
     * 构建已读回执消息（v7.1新增�?
     */
    private WebSocketMessage buildReadReceiptMessage(Long messageId, Long conversationId, Long readUserId) {
        return WebSocketMessage.builder()
                .type("read_receipt")
                .data(new ConcurrentHashMap<String, Object>() {{
                    put("messageId", messageId);
                    put("conversationId", conversationId);
                    put("readUserId", readUserId);
                    put("readTime", System.currentTimeMillis());
                }})
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建投递状态更新消息（v7.1新增�?
     */
    private WebSocketMessage buildDeliveryStatusMessage(Long messageId, Integer deliveryStatus) {
        return WebSocketMessage.builder()
                .type("delivery_status")
                .data(new ConcurrentHashMap<String, Object>() {{
                    put("messageId", messageId);
                    put("deliveryStatus", deliveryStatus);
                    put("timestamp", System.currentTimeMillis());
                }})
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ========== 公共静态方�?==========
    
    /**
     * 获取当前在线人数
     */
    public static int getOnlineCount() {
        return ONLINE_COUNT.get();
    }

    /**
     * 检查用户是否在�?
     */
    public static boolean isUserOnline(Long userId) {
        return SESSION_MAP.containsKey(userId);
    }

    /**
     * 获取所有在线用户ID
     */
    public static List<Long> getOnlineUserIds() {
        return SESSION_MAP.keySet().stream().toList();
    }

    /**
     * 强制断开用户连接（管理员功能�?
     */
    public static boolean disconnectUser(Long userId, String reason) {
        Session session = SESSION_MAP.get(userId);
        if (session != null) {
            try {
                sendMessage(session, new ChatWebSocketServer().buildSystemMessage("您已被强制下线：" + reason));
                session.close();
                SESSION_MAP.remove(userId);
                log.info("强制断开用户连接，用户ID：{}，原因：{}", userId, reason);
                return true;
            } catch (Exception e) {
                log.error("强制断开连接失败，用户ID：{}", userId, e);
            }
        }
        return false;
    }

    // ========== WebSocket消息对象 ==========
    
    /**
     * WebSocket消息封装
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WebSocketMessage {
        /**
         * 消息类型(chat/typing/read/heartbeat/system/error/ack)
         */
        private String type;

        /**
         * 消息数据
         */
        private ConcurrentHashMap<String, Object> data;

        /**
         * 时间�?
         */
        private Long timestamp;
    }
}

