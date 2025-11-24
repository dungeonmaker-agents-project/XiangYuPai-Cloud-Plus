package org.dromara.chat.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message WebSocket Handler
 * 消息WebSocket处理器
 *
 * Handles WebSocket connections for real-time messaging:
 * - Connection/disconnection management
 * - Online status tracking
 * - Message event handling (new_message, message_read, message_recalled, typing, online_status)
 * - Heartbeat mechanism
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    // Cache key prefix for online status
    private static final String CACHE_ONLINE_STATUS = "chat:online:";

    // Online status TTL (5 minutes, refreshed by heartbeat)
    private static final Duration TTL_ONLINE_STATUS = Duration.ofMinutes(5);

    // Session storage: userId -> WebSocketSession
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // Session to userId mapping for reverse lookup
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();

    /**
     * Called after WebSocket connection is established
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // Extract userId from query parameters (e.g., ws://localhost:9404/ws?userId=123&token=xxx)
            Long userId = extractUserId(session);

            if (userId == null) {
                log.warn("WebSocket connection rejected: missing userId or invalid token");
                session.close(CloseStatus.BAD_DATA.withReason("Missing userId or invalid token"));
                return;
            }

            // Store session
            userSessions.put(userId, session);
            sessionToUser.put(session.getId(), userId);

            // Mark user as online in Redis
            setUserOnline(userId, true);

            log.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());

            // Send connection success message
            sendMessageToUser(userId, createEvent("connection_success", Map.of(
                "userId", userId,
                "message", "WebSocket连接成功"
            )));

        } catch (Exception e) {
            log.error("Error establishing WebSocket connection", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * Called when WebSocket connection is closed
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        Long userId = sessionToUser.remove(sessionId);

        if (userId != null) {
            userSessions.remove(userId);

            // Mark user as offline in Redis
            setUserOnline(userId, false);

            log.info("WebSocket disconnected: userId={}, sessionId={}, status={}",
                userId, sessionId, status);
        }
    }

    /**
     * Handle incoming WebSocket messages
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            JSONObject json = JSONUtil.parseObj(payload);

            String eventType = json.getStr("type");
            if (StrUtil.isBlank(eventType)) {
                log.warn("Received message without type: {}", payload);
                return;
            }

            Long userId = sessionToUser.get(session.getId());
            if (userId == null) {
                log.warn("Received message from unknown session: {}", session.getId());
                return;
            }

            // Handle different event types
            switch (eventType) {
                case "heartbeat":
                    handleHeartbeat(userId, session);
                    break;

                case "typing":
                    handleTyping(userId, json);
                    break;

                case "mark_read":
                    handleMarkRead(userId, json);
                    break;

                default:
                    log.debug("Received unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    /**
     * Handle WebSocket transport errors
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: sessionId={}", session.getId(), exception);

        Long userId = sessionToUser.get(session.getId());
        if (userId != null) {
            log.error("Transport error for userId={}", userId);
        }

        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // ==================== Event Handlers ====================

    /**
     * Handle heartbeat event
     */
    private void handleHeartbeat(Long userId, WebSocketSession session) {
        // Refresh online status TTL
        setUserOnline(userId, true);

        // Send heartbeat response
        try {
            session.sendMessage(new TextMessage(createEvent("heartbeat_ack", Map.of(
                "timestamp", System.currentTimeMillis()
            ))));
        } catch (IOException e) {
            log.error("Error sending heartbeat ack to userId={}", userId, e);
        }

        log.debug("Heartbeat received from userId={}", userId);
    }

    /**
     * Handle typing event
     */
    private void handleTyping(Long userId, JSONObject json) {
        Long targetUserId = json.getLong("targetUserId");
        if (targetUserId == null) {
            log.warn("Typing event missing targetUserId");
            return;
        }

        // Notify target user that sender is typing
        sendMessageToUser(targetUserId, createEvent("typing", Map.of(
            "userId", userId,
            "isTyping", true
        )));

        log.debug("Typing event: from userId={} to targetUserId={}", userId, targetUserId);
    }

    /**
     * Handle mark read event
     */
    private void handleMarkRead(Long userId, JSONObject json) {
        Long messageId = json.getLong("messageId");
        if (messageId == null) {
            log.warn("Mark read event missing messageId");
            return;
        }

        // This is handled by the REST API, WebSocket just echoes the event
        log.debug("Mark read event: userId={}, messageId={}", userId, messageId);
    }

    // ==================== Public Methods for Service Layer ====================

    /**
     * Send new message event to receiver
     */
    public void sendNewMessage(Long receiverId, Map<String, Object> messageData) {
        sendMessageToUser(receiverId, createEvent("new_message", messageData));
        log.debug("Sent new_message event to userId={}", receiverId);
    }

    /**
     * Send message read event to sender
     */
    public void sendMessageRead(Long senderId, Map<String, Object> readData) {
        sendMessageToUser(senderId, createEvent("message_read", readData));
        log.debug("Sent message_read event to userId={}", senderId);
    }

    /**
     * Send message recalled event to receiver
     */
    public void sendMessageRecalled(Long receiverId, Map<String, Object> recallData) {
        sendMessageToUser(receiverId, createEvent("message_recalled", recallData));
        log.debug("Sent message_recalled event to userId={}", receiverId);
    }

    /**
     * Send online status change event
     */
    public void sendOnlineStatusChange(Long targetUserId, Long userId, boolean isOnline) {
        sendMessageToUser(targetUserId, createEvent("online_status", Map.of(
            "userId", userId,
            "isOnline", isOnline
        )));
        log.debug("Sent online_status event: userId={}, isOnline={}", userId, isOnline);
    }

    /**
     * Check if user is online (has active WebSocket connection)
     */
    public boolean isUserOnline(Long userId) {
        return userSessions.containsKey(userId);
    }

    /**
     * Get online user count
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Extract userId from WebSocket session
     */
    private Long extractUserId(WebSocketSession session) {
        try {
            // Extract from query parameters
            String query = session.getUri().getQuery();
            if (StrUtil.isBlank(query)) {
                return null;
            }

            // Parse query string
            Map<String, String> params = parseQueryString(query);
            String userIdStr = params.get("userId");

            if (StrUtil.isBlank(userIdStr)) {
                return null;
            }

            // TODO: Validate JWT token from params.get("token")
            // For now, just parse userId (in production, validate authentication)

            return Long.parseLong(userIdStr);

        } catch (Exception e) {
            log.error("Error extracting userId from session", e);
            return null;
        }
    }

    /**
     * Parse query string into map
     */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new ConcurrentHashMap<>();
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                params.put(parts[0], parts[1]);
            }
        }

        return params;
    }

    /**
     * Send message to specific user
     */
    private void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Error sending message to userId={}", userId, e);
            }
        } else {
            log.debug("User not online, cannot send WebSocket message: userId={}", userId);
        }
    }

    /**
     * Create WebSocket event JSON
     */
    private String createEvent(String type, Map<String, Object> data) {
        JSONObject event = new JSONObject();
        event.set("type", type);
        event.set("data", data);
        event.set("timestamp", System.currentTimeMillis());
        return event.toString();
    }

    /**
     * Set user online status in Redis
     */
    private void setUserOnline(Long userId, boolean isOnline) {
        String cacheKey = CACHE_ONLINE_STATUS + userId;

        if (isOnline) {
            RedisUtils.setCacheObject(cacheKey, "1", TTL_ONLINE_STATUS);
        } else {
            RedisUtils.deleteObject(cacheKey);
        }

        log.debug("User online status updated: userId={}, isOnline={}", userId, isOnline);
    }
}
