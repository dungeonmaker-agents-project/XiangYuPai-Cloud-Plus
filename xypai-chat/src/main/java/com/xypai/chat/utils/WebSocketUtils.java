package com.xypai.chat.utils;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket工具类 (v7.1)
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Slf4j
public class WebSocketUtils {

    /**
     * 发送JSON消息
     * 
     * @param session WebSocket会话
     * @param message 消息对象
     * @return 是否成功
     */
    public static boolean sendJsonMessage(Session session, Object message) {
        if (session == null || !session.isOpen()) {
            log.warn("Session为空或已关闭");
            return false;
        }

        try {
            String json = JSON.toJSONString(message);
            synchronized (session) {
                session.getBasicRemote().sendText(json);
            }
            return true;
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
            return false;
        }
    }

    /**
     * 发送文本消息
     * 
     * @param session WebSocket会话
     * @param text 文本内容
     * @return 是否成功
     */
    public static boolean sendTextMessage(Session session, String text) {
        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            synchronized (session) {
                session.getBasicRemote().sendText(text);
            }
            return true;
        } catch (IOException e) {
            log.error("发送WebSocket文本失败", e);
            return false;
        }
    }

    /**
     * 构建标准WebSocket消息
     * 
     * @param type 消息类型
     * @param data 数据
     * @return 消息对象
     */
    public static Map<String, Object> buildMessage(String type, Map<String, Object> data) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", type);
        message.put("data", data != null ? data : new ConcurrentHashMap<>());
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 构建系统消息
     * 
     * @param content 内容
     * @return 消息对象
     */
    public static Map<String, Object> buildSystemMessage(String content) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("content", content);
        return buildMessage("system", data);
    }

    /**
     * 构建错误消息
     * 
     * @param errorMessage 错误信息
     * @return 消息对象
     */
    public static Map<String, Object> buildErrorMessage(String errorMessage) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("message", errorMessage);
        return buildMessage("error", data);
    }

    /**
     * 构建消息发送成功回执
     * 
     * @param messageId 消息ID
     * @param clientId 客户端ID
     * @return 消息对象
     */
    public static Map<String, Object> buildMessageAck(Long messageId, String clientId) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("messageId", messageId);
        data.put("clientId", clientId);
        data.put("status", "sent");
        return buildMessage("ack", data);
    }

    /**
     * 构建心跳响应
     * 
     * @return 消息对象
     */
    public static Map<String, Object> buildHeartbeatResponse() {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("pong", true);
        data.put("serverTime", System.currentTimeMillis());
        return buildMessage("heartbeat", data);
    }

    /**
     * 安全关闭Session
     * 
     * @param session WebSocket会话
     * @param reason 关闭原因
     */
    public static void closeSession(Session session, String reason) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            // 发送关闭消息
            sendJsonMessage(session, buildSystemMessage("连接即将关闭：" + reason));
            
            // 等待消息发送
            Thread.sleep(100);
            
            // 关闭连接
            session.close();
        } catch (Exception e) {
            log.error("关闭Session失败", e);
        }
    }

    /**
     * 验证Session是否有效
     * 
     * @param session WebSocket会话
     * @return 是否有效
     */
    public static boolean isSessionValid(Session session) {
        return session != null && session.isOpen();
    }

    /**
     * 获取Session参数
     * 
     * @param session WebSocket会话
     * @param paramName 参数名
     * @return 参数值
     */
    public static String getSessionParameter(Session session, String paramName) {
        if (session == null) {
            return null;
        }

        Map<String, List<String>> params = session.getRequestParameterMap();
        if (params == null || !params.containsKey(paramName)) {
            return null;
        }

        List<String> values = params.get(paramName);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
}

